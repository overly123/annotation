package eu.europeana.annotation.web.service.impl;

import java.util.Date;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;

import eu.europeana.annotation.config.AnnotationConfiguration;
import eu.europeana.annotation.definitions.model.Annotation;
import eu.europeana.annotation.definitions.model.AnnotationId;
import eu.europeana.annotation.definitions.model.moderation.Summary;
import eu.europeana.annotation.definitions.model.search.SearchProfiles;
import eu.europeana.annotation.definitions.model.vocabulary.AnnotationStates;
import eu.europeana.annotation.definitions.model.vocabulary.MotivationTypes;
import eu.europeana.annotation.mongo.model.internal.PersistentAnnotation;
import eu.europeana.annotation.mongo.service.PersistentAnnotationService;
import eu.europeana.annotation.mongo.service.PersistentModerationRecordService;
import eu.europeana.annotation.solr.exceptions.AnnotationStateException;
import eu.europeana.annotation.solr.service.SolrAnnotationService;
import eu.europeana.annotation.utils.UriUtils;
import eu.europeana.annotation.web.exception.AnnotationIndexingException;
import eu.europeana.annotation.web.exception.authorization.UserAuthorizationException;
import eu.europeana.annotation.web.exception.response.AnnotationNotFoundException;
import eu.europeana.api.common.config.I18nConstants;

public abstract class BaseAnnotationServiceImpl extends BaseAnnotationValidator{

    @Resource
    AnnotationConfiguration configuration;

//	@Resource
//	AuthenticationService authenticationService;

    @Resource
    SolrAnnotationService solrService;

    @Resource
    PersistentAnnotationService mongoPersistance;

    @Resource
    PersistentModerationRecordService mongoModerationRecordPersistance;

    Logger logger = LogManager.getLogger(getClass());

//	public AuthenticationService getAuthenticationService() {
//		return authenticationService;
//	}
//
//	public void setAuthenticationService(AuthenticationService authenticationService) {
//		this.authenticationService = authenticationService;
//	}

    public String getComponentName() {
	return configuration.getComponentName();
    }

    protected AnnotationConfiguration getConfiguration() {
	return configuration;
    }

    public void setConfiguration(AnnotationConfiguration configuration) {
	this.configuration = configuration;
    }

    protected PersistentAnnotationService getMongoPersistence() {
	return mongoPersistance;
    }

    public void setMongoPersistance(PersistentAnnotationService mongoPersistance) {
	this.mongoPersistance = mongoPersistance;
    }

    public PersistentModerationRecordService getMongoModerationRecordPersistence() {
	return mongoModerationRecordPersistance;
    }

    public SolrAnnotationService getSolrService() {
	return solrService;
    }

    public void setSolrService(SolrAnnotationService solrService) {
	this.solrService = solrService;
    }

    @Deprecated
    public Annotation getAnnotationById(AnnotationId annoId, String userId)
	    throws AnnotationNotFoundException, UserAuthorizationException {
	Annotation annotation = getMongoPersistence().find(annoId);
	if (annotation == null)
	    throw new AnnotationNotFoundException(null, I18nConstants.ANNOTATION_NOT_FOUND,
		    new String[] { annoId.toHttpUrl() });

	try {
	    // check visibility
	    checkVisibility(annotation, userId);
	} catch (AnnotationStateException e) {
	    if (annotation.isDisabled())
		throw new UserAuthorizationException(null, I18nConstants.ANNOTATION_NOT_ACCESSIBLE,
			new String[] { annotation.getStatus() }, HttpStatus.GONE, e);
	    else
		// TODO: either change method parameters to accept wsKey or return different
		// exception
		throw new UserAuthorizationException(null, I18nConstants.USER_NOT_AUTHORIZED, new String[] { userId },
			e);
	}

	return annotation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eu.europeana.annotation.web.service.AnnotationService#checkVisibility(eu.
     * europeana.annotation.definitions.model.Annotation, java.lang.String)
     */
    public void checkVisibility(Annotation annotation, String user) throws AnnotationStateException {
	// Annotation res = null;
	// res =
	// getMongoPersistence().find(annotation.getAnnotationId().getProvider(),
	// annotation.getAnnotationId().getIdentifier());
	if (annotation.isDisabled())
	    throw new AnnotationStateException(AnnotationStateException.MESSAGE_NOT_ACCESSIBLE,
		    AnnotationStates.DISABLED);

	// if (annotation.
	// StringUtils.isNotEmpty(user) && !user.equals("null") &&
	// !res.getAnnotatedBy().getName().equals(user))
	// throw new AnnotationStateException("Given user (" + user + ") does
	// not match to the 'annotatedBy' user ("
	// + res.getAnnotatedBy().getName() + ").");
	// TODO update when the authorization concept is specified
	if (annotation.isPrivate() && !annotation.getCreator().getHttpUrl().equals(user))
	    throw new AnnotationStateException(AnnotationStateException.MESSAGE_NOT_ACCESSIBLE,
		    AnnotationStates.PRIVATE);

    }

//	public void indexAnnotation(AnnotationId annoId) {
//		try {
//			Annotation res = getAnnotationById(annoId);
//			getSolrService().delete(res.getAnnotationId());
//			getSolrService().store(res);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}

    /**
     * Returns true by successful reindexing.
     * 
     * @param res
     * @return reindexing success status
     * @throws AnnotationIndexingException
     */
    protected boolean reindexAnnotation(Annotation res, Date lastIndexing) throws AnnotationIndexingException {

	if (!getConfiguration().isIndexingEnabled()) {
	    getLogger().warn("Annotation was not reindexed, indexing is disabled. See configuration properties!");
	    return false;
	}

	try {
	    Summary summary = getMongoModerationRecordPersistence()
		    .getModerationSummaryByAnnotationId(res.getAnnotationId());
	    getSolrService().update(res, summary);
	    updateLastIndexingTime(res, lastIndexing);

	    return true;
	} catch (Exception e) {
	    throw new AnnotationIndexingException("cannot reindex annotation with ID: " + res.getAnnotationId(), e);
	}
    }

    /**
     * Returns true by successful reindexing.
     * 
     * @param res
     * @return reindexing success status
     */
    public boolean reindexAnnotationById(AnnotationId annoId, Date lastIndexing) {
	boolean success = false;
	try {
//			Annotation res = getAnnotationById(annoId);
	    Annotation annotation = getMongoPersistence().find(annoId);
	    success = reindexAnnotation(annotation, lastIndexing);
	    System.out.println(annoId);
	} catch (Exception e) {
	    getLogger().error(e.getMessage(), e);
	    return false;
	}
	return success;
    }

    protected void updateLastIndexingTime(Annotation res, Date lastIndexing) {
	try {
	    getMongoPersistence().updateIndexingTime(res, lastIndexing);
	    ((PersistentAnnotation) res).setLastIndexed(lastIndexing);
	} catch (Exception e) {
	    getLogger().warn("The time of the last SOLR indexing could not be saved in the Mongo database. ", e);
	}
    }

    public Logger getLogger() {
	return logger;
    }

    public PersistentAnnotationService getMongoPersistance() {
	return mongoPersistance;
    }

    protected Annotation updateAndReindex(PersistentAnnotation persistentAnnotation) {
	Annotation res = getMongoPersistence().update(persistentAnnotation);

	// reindex annotation
	try {
	    reindexAnnotation(res, res.getLastUpdate());
	} catch (AnnotationIndexingException e) {
	    getLogger().warn(
		    "The annotation could not be reindexed successfully: " + persistentAnnotation.getAnnotationId(), e);
	}
	return res;
    }

    boolean isSemanticTag(Annotation annotation) {
	return MotivationTypes.TAGGING.equals(annotation.getMotivationType()) && hasBodyUrl(annotation);
    }

    boolean hasBodyUrl(Annotation annotation) {
	return UriUtils.isUrl(annotation.getBody().getValue());
    }

    boolean isDereferenceProfile(SearchProfiles searchProfile) {
	return SearchProfiles.DEREFERENCE.equals(searchProfile);
    }

}
