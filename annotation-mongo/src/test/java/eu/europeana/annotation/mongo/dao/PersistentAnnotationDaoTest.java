package eu.europeana.annotation.mongo.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.europeana.annotation.definitions.model.AnnotationId;
import eu.europeana.annotation.mongo.model.internal.GeneratedAnnotationIdImpl;
import eu.europeana.annotation.mongo.model.internal.PersistentAnnotation;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/annotation-mongo-context.xml", "/annotation-mongo-test.xml"})
public class PersistentAnnotationDaoTest {

	@Resource(name = "annotation_db_annotationDao")
	PersistentAnnotationDao<PersistentAnnotation, AnnotationId> annotationDao;
	
	public final Integer SEQUENCE_LENGTH = 10;
	
	/**
	 * Initialize the testing session
	 * 
	 * @throws IOException
	 */
	@Before
	public void setup() throws IOException {
		//annotationDao.getDatastore().getCollection(GeneratedAnnotationIdImpl.class).drop();
	}

	/**
	 * Cleaning the testing session's data
	 * 
	 * @throws IOException
	 */
	@After
	public void tearDown() throws IOException {
		// annotationDao.getDatastore().getCollection(GeneratedAnnotationIdImpl.class).drop();
	}

	@Test
	public void testGenerateAnnotationId(){
		String testProvider = "test_provider";
		AnnotationId id1 = annotationDao.generateNextAnnotationId(testProvider);
		assertTrue(Long.parseLong(id1.getIdentifier()) > 0);
		
		AnnotationId id2 = annotationDao.generateNextAnnotationId(testProvider);
		assertTrue(Long.parseLong(id1.getIdentifier()) +1 == Long.parseLong(id2.getIdentifier()));
		
	}
	

	@Test
	public void testGenerateAnnotationIds(){
		String testProvider = "test_batch_provider";
		
		List<AnnotationId> ids = annotationDao.generateNextAnnotationIds(testProvider, 10);
		assertEquals(SEQUENCE_LENGTH.intValue(), ids.size());
		
		GeneratedAnnotationIdImpl lastAnnoInSequence = (GeneratedAnnotationIdImpl)ids.get(SEQUENCE_LENGTH-1);
		
		Long persistedAnnoNumber = annotationDao.getLastAnnotationNr(testProvider);
		
		assertTrue(persistedAnnoNumber > 0);
		assertTrue(lastAnnoInSequence.getAnnotationNr() > 0);
		
		assertEquals(persistedAnnoNumber, lastAnnoInSequence.getAnnotationNr());
		
	}

}
