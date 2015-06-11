package eu.europeana.annotation.client.integration.jsonld;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import eu.europeana.annotation.client.AnnotationJsonLdApiImpl;
import eu.europeana.annotation.definitions.model.Annotation;
import eu.europeana.annotation.definitions.model.util.AnnotationTestObjectBuilder;


public class AnnotationJsonLdApiTest extends AnnotationTestObjectBuilder{

	@Test
	public void createAnnotation() {
		
		AnnotationJsonLdApiImpl annotationJsonLdApi = new AnnotationJsonLdApiImpl();
		
		/**
		 * Create a test annotation object.
		 */
		Annotation testAnnotation = createBaseObjectTagInstance();	
        
        /**
         * convert Annotation object to AnnotationLd object.
         */
//        AnnotationLd annotationLd = new AnnotationLd(originalAnnotation);
//        String initialAnnotationIndent = annotationLd.toString(4);
//        AnnotationLd.toConsole("### initialAnnotation ###", initialAnnotationIndent);

        /**
         * read Annotation object from AnnotationLd object.
         */
//        Annotation annotationFromAnnotationLd = annotationLd.getAnnotation();
        
		String jsonPost = annotationJsonLdApi.getApiConnection().getAnnotationGson().toJson(testAnnotation);
//		String jsonPost = annotationJsonLdApi.apiConnection.convertAnnotationToAnnotationLdString(testAnnotation);

		Annotation annotation = annotationJsonLdApi.createAnnotation(jsonPost);
		assertNotNull(annotation);
	}
	
}