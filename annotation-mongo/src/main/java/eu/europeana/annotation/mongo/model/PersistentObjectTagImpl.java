package eu.europeana.annotation.mongo.model;

import eu.europeana.annotation.definitions.model.ObjectTag;
import eu.europeana.annotation.definitions.model.vocabulary.AnnotationTypes;
import eu.europeana.annotation.definitions.model.vocabulary.MotivationTypes;

public class PersistentObjectTagImpl extends PersistentAnnotationImpl implements ObjectTag{
	
	
	public PersistentObjectTagImpl(){
		super();
		setInternalType(AnnotationTypes.OBJECT_TAG.name());
		setMotivation(MotivationTypes.TAGGING.getOaType());
	}

}
