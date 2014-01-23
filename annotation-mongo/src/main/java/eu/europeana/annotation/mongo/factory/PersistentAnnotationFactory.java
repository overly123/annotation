package eu.europeana.annotation.mongo.factory;

import eu.europeana.annotation.definitions.model.Annotation;
import eu.europeana.annotation.definitions.model.AnnotationTypes;
import eu.europeana.annotation.definitions.model.factory.AbstractAnnotationFactory;
import eu.europeana.annotation.mongo.model.ImageAnnotationImpl;
import eu.europeana.annotation.mongo.model.SemanticTagImpl;

public class PersistentAnnotationFactory extends AbstractAnnotationFactory{

	private static PersistentAnnotationFactory singleton;

	//force singleton usage
	private PersistentAnnotationFactory(){};
	
	public static PersistentAnnotationFactory getInstance() {

		if (singleton == null) {
			synchronized (PersistentAnnotationFactory.class) {
				singleton = new PersistentAnnotationFactory();
			}
		}

		return singleton;

	}

	public Class<? extends Annotation> getAnnotationClass(
			AnnotationTypes annotationType) {
		Class<? extends Annotation> ret = null;

		switch (annotationType) {
		case IMAGE_ANNOTATION:
			ret =  ImageAnnotationImpl.class;
			break;
		case SEMANTIC_TAG:
			ret = SemanticTagImpl.class;
			break;
		default:
			throw new RuntimeException(
					"The given type is not supported by the web model");
		}

		return ret;
	}
}
