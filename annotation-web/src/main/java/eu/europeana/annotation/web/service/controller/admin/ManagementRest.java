package eu.europeana.annotation.web.service.controller.admin;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.europeana.annotation.definitions.model.AnnotationId;
import eu.europeana.annotation.definitions.model.impl.BaseAnnotationId;
import eu.europeana.annotation.definitions.model.vocabulary.WebAnnotationFields;
import eu.europeana.annotation.mongo.exception.ApiWriteLockException;
import eu.europeana.annotation.mongo.model.internal.PersistentApiWriteLock;
import eu.europeana.annotation.mongo.service.PersistentApiWriteLockService;
import eu.europeana.annotation.solr.exceptions.AnnotationServiceException;
import eu.europeana.annotation.utils.parse.BaseJsonParser;
import eu.europeana.annotation.web.exception.IndexingJobLockedException;
import eu.europeana.annotation.web.exception.InternalServerException;
import eu.europeana.annotation.web.exception.authorization.OperationAuthorizationException;
import eu.europeana.annotation.web.exception.authorization.UserAuthorizationException;
import eu.europeana.annotation.web.http.SwaggerConstants;
import eu.europeana.annotation.web.model.AnnotationOperationResponse;
import eu.europeana.annotation.web.model.BatchProcessingStatus;
import eu.europeana.annotation.web.model.vocabulary.Actions;
import eu.europeana.annotation.web.model.vocabulary.Operations;
import eu.europeana.annotation.web.service.AdminService;
import eu.europeana.annotation.web.service.controller.BaseRest;
import eu.europeana.api.common.config.swagger.SwaggerSelect;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.api2.utils.JsonWebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Controller
@SwaggerSelect
@Api(tags = "Web Annotation Admin", description = " ", hidden = true)
public class ManagementRest extends BaseRest {

    protected final Logger logger = getLogger();

    @Resource
    private AdminService adminService;

    public AdminService getAdminService() {
	return adminService;
    }

    public void setAdminService(AdminService adminService) {
	this.adminService = adminService;
    }

    @Resource(name = "annotation_db_apilockService")
    private PersistentApiWriteLockService indexingJobService;

    public PersistentApiWriteLockService getApiWriteLockService() {
	return indexingJobService;
    }

    public void setPersistentIndexingJobService(PersistentApiWriteLockService indexingJobService) {
	this.indexingJobService = indexingJobService;
    }

    @RequestMapping(value = "/admin/annotation/delete", method = RequestMethod.DELETE, produces = {
	    HttpHeaders.CONTENT_TYPE_JSON_UTF8, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8 })
    @ApiOperation(value = "Delete Annotation for good", nickname = "deleteAnnotationById", response = java.lang.Void.class)
    public ResponseEntity<String> deleteAnnotationById(
	    @RequestParam(value = WebAnnotationFields.REQ_PARAM_IDENTIFIER, required = true) String identifier,
	    HttpServletRequest request) throws HttpException {

	verifyWriteAccess(Operations.ADMIN_ALL, request);

	deleteAnnotationForGood(identifier);

	AnnotationOperationResponse response;
	response = new AnnotationOperationResponse("admin", "/admin/annotation/delete");
	response.success = true;
	String jsonStr = JsonWebUtils.toJson(response, null);
	logger.info("Delete Annotation for good result: " + jsonStr);
	return buildResponse(jsonStr);
    }

    @RequestMapping(value = "/admin/annotation/deleteset", method = RequestMethod.DELETE, produces = {
	    HttpHeaders.CONTENT_TYPE_JSON_UTF8, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8 })
    @ApiOperation(value = "Delete a set of Annotations for good", nickname = "deleteAnnotationSet", notes = SwaggerConstants.URIS_HELP_NOTE, response = java.lang.Void.class)
    public ResponseEntity<String> deleteAnnotationSet(
	    @RequestBody String uris, HttpServletRequest request) throws HttpException {

	verifyWriteAccess(Operations.ADMIN_ALL, request);

	List<String> uriList = BaseJsonParser.toStringList(uris, true);

	BatchProcessingStatus status = getAdminService().deleteAnnotationSet(uriList);

	AnnotationOperationResponse response;
	response = new AnnotationOperationResponse("admin", "/admin/annotation/deleteset");
	response.setStatus(
		"Success count: " + status.getSuccessCount() + ". Failure count: " + status.getFailureCount());
	response.success = true;

	// return JsonWebUtils.toJson(response, null);
	String jsonStr = JsonWebUtils.toJson(response, null);
	logger.info("Delete a set of Annotations for good result: " + jsonStr);
	return buildResponse(jsonStr);
    }

    protected void deleteAnnotationForGood(String identifier) throws HttpException {

	// 0. annotation id
	AnnotationId annoId = buildAnnotationId(identifier, false);

	deleteAnnotationForGood(annoId);
    }

    protected void deleteAnnotationForGood(AnnotationId annoId) throws InternalServerException,
	    UserAuthorizationException, ApplicationAuthenticationException, OperationAuthorizationException {

	try {
	    getAdminService().deleteAnnotation(annoId);
	} catch (AnnotationServiceException e) {
	    throw new InternalServerException(e);
	}
    }

    @RequestMapping(value = "/admin/annotation/reindex", method = RequestMethod.GET, produces = {
	    HttpHeaders.CONTENT_TYPE_JSON_UTF8, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8 })
    @ApiOperation(value = "Reindex by annotation id", nickname = Actions.REINDEX_ANNOTATION_BY_ANNOTATION_ID, response = java.lang.Void.class)
    public ResponseEntity<String> reindexAnnotationByAnnotationId(
	    @RequestParam(value = "identifier", required = true, defaultValue = WebAnnotationFields.REST_ANNOTATION_NR) String identifier,
	    HttpServletRequest request) throws UserAuthorizationException, HttpException {

	verifyWriteAccess(Operations.ADMIN_REINDEX, request);

	BaseAnnotationId baseAnnotationId = new BaseAnnotationId(getConfiguration().getAnnotationBaseUrl(), identifier);
	getAdminService().reindexAnnotationById(baseAnnotationId, new Date());

	AnnotationOperationResponse response = new AnnotationOperationResponse("admin", "/admin/reindex");

	String jsonStr = JsonWebUtils.toJson(response, null);
	logger.info("Reindex by annotation id result: " + jsonStr);
	return buildResponse(jsonStr);
    }

    @RequestMapping(value = "/admin/annotation/reindexselection", method = RequestMethod.GET, produces = {
	    HttpHeaders.CONTENT_TYPE_JSON_UTF8, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8 })
    @ApiOperation(value = "Reindex a set of annotations defined by selection criteria", notes = SwaggerConstants.DATE_FORMAT_HELP_NOTE, nickname = Actions.REINDEX_ANNOTATION_SELECTION, response = java.lang.Void.class)
    public ResponseEntity<String> reindexAnnotationSelection(
	    @RequestParam(value = "startDate", required = false) String startDate,
	    @RequestParam(value = "endDate", required = false) String endDate,
	    @RequestParam(value = "startTimestamp", required = false) String startTimestamp,
	    @RequestParam(value = "endTimestamp", required = false) String endTimestamp, HttpServletRequest request)
	    throws UserAuthorizationException, ApiWriteLockException, IndexingJobLockedException, HttpException {

	verifyWriteAccess(Operations.ADMIN_REINDEX, request);

	BatchProcessingStatus status = getAdminService().reindexAnnotationSelection(startDate, endDate, startTimestamp,
		endTimestamp, Actions.REINDEX_ANNOTATION_SELECTION);

	AnnotationOperationResponse response = new AnnotationOperationResponse("admin", "/admin/reindexset");
	response.setStatus(status.toString());

	String jsonStr = JsonWebUtils.toJson(response, null);
	logger.info("Reindex a set of annotations defined by selection criteria result: " + jsonStr);
	return buildResponse(jsonStr);
    }

    @RequestMapping(value = "/admin/annotation/reindexset", method = RequestMethod.POST, produces = {
	    HttpHeaders.CONTENT_TYPE_JSON_UTF8, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8 })
    @ApiOperation(value = "Reindex a set of annotations", nickname = "reindexAnnotationByAnnotationId", notes = SwaggerConstants.URIS_HELP_NOTE, response = java.lang.Void.class)
    public ResponseEntity<String> reindexAnnotationSet(
	    @RequestParam(value = WebAnnotationFields.PARAM_WSKEY, required = false) String apiKey,
	    @RequestParam(value = WebAnnotationFields.USER_TOKEN, required = false, defaultValue = WebAnnotationFields.USER_ANONYMOUNS) String userToken,
	    @RequestBody String uris, HttpServletRequest request) throws UserAuthorizationException, HttpException {

	verifyWriteAccess(Operations.ADMIN_REINDEX, request);

	List<String> uriList = BaseJsonParser.toStringList(uris, true);

	BatchProcessingStatus status;
	try {
	    status = getAdminService().reindexAnnotationSet(uriList, false, "/admin/annotation/reindexset");
	} catch (ApiWriteLockException e) {
	    throw new InternalServerException("Cannot reindex annotation selection", e);
	}

	AnnotationOperationResponse response;
	response = new AnnotationOperationResponse(apiKey, "/admin/annotation/reindexset");
	response.setStatus(
		"Success count: " + status.getSuccessCount() + ". Failure count: " + status.getFailureCount());
	// only if at least one item was successfully reindexed
	response.success = (status.getSuccessCount() > 0);

	String jsonStr = JsonWebUtils.toJson(response, null);
	HttpStatus httpStatus = response.success ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
	logger.info("Reindex a set of annotations result: " + jsonStr + "(HTTP status: " + httpStatus.toString() + ")");
	return buildResponse(jsonStr, httpStatus);
    }

    @RequestMapping(value = "/admin/annotation/reindexall", method = RequestMethod.GET, produces = {
	    HttpHeaders.CONTENT_TYPE_JSON_UTF8, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8 })
    @ApiOperation(value = "Reindex all annotations", nickname = Actions.REINDEX_ALL, response = java.lang.Void.class)
    public ResponseEntity<String> reindexAll(
	    HttpServletRequest request) throws UserAuthorizationException, HttpException {

	verifyWriteAccess(Operations.ADMIN_REINDEX, request);

	BatchProcessingStatus status;
	try {
	    status = getAdminService().reindexAll();
	} catch (ApiWriteLockException e) {
	    throw new InternalServerException("Cannot reindex annotation selection", e);
	}

	AnnotationOperationResponse response;
	response = new AnnotationOperationResponse("admin", "/admin/annotation/reindexset");
	response.setStatus(
		"Success count: " + status.getSuccessCount() + ". Failure count: " + status.getFailureCount());
	response.success = true;

	String jsonStr = JsonWebUtils.toJson(response, null);
	logger.info("Reindex all annotations result: " + jsonStr);
	return buildResponse(jsonStr);
    }

    @RequestMapping(value = "/admin/annotation/reindexoutdated", method = RequestMethod.GET, produces = {
	    HttpHeaders.CONTENT_TYPE_JSON_UTF8, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8 })
    @ApiOperation(value = "Index new and reindex outdated annotations", nickname = Actions.REINDEX_OUTDATED, response = java.lang.Void.class)
    public ResponseEntity<String> reindexOutdated(HttpServletRequest request)
	    throws UserAuthorizationException, HttpException {

	verifyWriteAccess(Operations.ADMIN_REINDEX, request);

	AnnotationOperationResponse response = new AnnotationOperationResponse("admin",
		"/admin/annotation/reindexoutdated");
	BatchProcessingStatus status;
	try {
	    status = getAdminService().reindexOutdated();

	} catch (ApiWriteLockException e) {
	    throw new InternalServerException("Cannot reindex annotation selection", e);
	}

	String successMsg = "Outdated annotations reindexed. " + status.toString();
	response.setStatus(successMsg);
	response.success = true;
	HttpStatus httpStatus = HttpStatus.OK;
	logger.info(successMsg);

	String jsonStr = JsonWebUtils.toJson(response, null);
	return buildResponse(jsonStr, httpStatus);
    }

    @RequestMapping(value = "/admin/annotation/updateRecordId", method = RequestMethod.POST, produces = {
	    HttpHeaders.CONTENT_TYPE_JSON_UTF8, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8 })
    @ApiOperation(value = "Update record identifiers with new ones", nickname = "updateRecordId", response = java.lang.Void.class)
    public ResponseEntity<String> updateRecordId(
	    @RequestParam(value = WebAnnotationFields.OLD_RECORD_ID, required = true) String oldId,
	    @RequestParam(value = WebAnnotationFields.NEW_RECORD_ID, required = true) String newId,
	    HttpServletRequest request) throws HttpException {
	
	verifyWriteAccess(Operations.ADMIN_ALL, request);
	
	if (oldId.isEmpty() || newId.isEmpty()) {
	    throw new HttpException("Both newId and oldId parameters must be provided!", null, null,
		    HttpStatus.BAD_REQUEST);
	}

	// 2. update record id
	BatchProcessingStatus status = getAdminService().updateRecordId(oldId, newId);

	AnnotationOperationResponse response;
	response = new AnnotationOperationResponse("admin", "/admin/annotation/updateRecordId");
	response.setStatus("Updated record IDs. " + status.toString());
	response.success = true;
	String jsonStr = JsonWebUtils.toJson(response, null);
	logger.info("Update record ID operation result. \n Old ID: " + oldId + "\nnewId : " + newId + "\nResult: "
		+ jsonStr);
	return buildResponse(jsonStr);

    }

    @RequestMapping(value = "/admin/lock", method = RequestMethod.POST, produces = { HttpHeaders.CONTENT_TYPE_JSON_UTF8,
	    HttpHeaders.CONTENT_TYPE_JSONLD_UTF8 })
    @ApiOperation(value = "Lock write operations", nickname = "lockWriteOperations", notes = SwaggerConstants.URIS_HELP_NOTE, response = java.lang.Void.class)
    public ResponseEntity<String> lockWriteOperations(
	    HttpServletRequest request) throws UserAuthorizationException, HttpException, ApiWriteLockException {

	verifyWriteAccess(Operations.ADMIN_ALL, request);

	// get last active lock check if start date is correct and end date does
	// not exist
	PersistentApiWriteLock lastActiveLock = getApiWriteLockService().getLastActiveLock("lockWriteOperations");
	boolean success = (lastActiveLock.getStarted() instanceof Date && lastActiveLock.getEnded() == null);

	AnnotationOperationResponse response;
	response = new AnnotationOperationResponse("admin", "/admin/lock");

	response.setStatus(success ? "Server is now locked for changes" : "Unable to set lock");
	response.success = success;

	String jsonStr = JsonWebUtils.toJson(response, null);
	HttpStatus httpStatus = response.success ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
	logger.info("Lock write operations result: " + jsonStr + "(HTTP status: " + httpStatus.toString() + ")");
	return buildResponse(jsonStr, httpStatus);
    }

    @RequestMapping(value = "/admin/unlock", method = RequestMethod.POST, produces = {
	    HttpHeaders.CONTENT_TYPE_JSON_UTF8 })
    @ApiOperation(value = "Unlock write operations", nickname = "unlockWriteOperations", notes = SwaggerConstants.URIS_HELP_NOTE, response = java.lang.Void.class)
    public ResponseEntity<String> unlockWriteOperations(
	    HttpServletRequest request) throws UserAuthorizationException, HttpException, ApiWriteLockException {

	verifyWriteAccess(Operations.ADMIN_UNLOCK, request);

	AnnotationOperationResponse response;
	response = new AnnotationOperationResponse("admin", "/admin/unlock");

	PersistentApiWriteLock activeLock = getApiWriteLockService().getLastActiveLock("lockWriteOperations");
	if (activeLock != null && activeLock.getName().equals("lockWriteOperations") && activeLock.getEnded() == null) {
	    getApiWriteLockService().unlock(activeLock);
	    PersistentApiWriteLock lock = getApiWriteLockService().getLastActiveLock("lockWriteOperations");
	    if (lock == null) {
		response.setStatus("Server is now unlocked for changes");
		response.success = true;
	    } else {
		response.setStatus("Unlocking write operations failed");
		response.success = false;
	    }
	} else {
	    response.setStatus("No write lock in effect (remains unlocked)");
	    response.success = true;
	}

	String jsonStr = JsonWebUtils.toJson(response, null);
	HttpStatus httpStatus = response.success ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
	logger.info("Unlock write operations result: " + jsonStr + "(HTTP status: " + httpStatus.toString() + ")");
	return buildResponse(jsonStr, httpStatus);
    }

}
