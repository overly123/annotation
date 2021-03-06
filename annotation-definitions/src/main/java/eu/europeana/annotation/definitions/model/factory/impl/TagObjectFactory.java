package eu.europeana.annotation.definitions.model.factory.impl;

import eu.europeana.annotation.definitions.exception.AnnotationAttributeInstantiationException;
import eu.europeana.annotation.definitions.model.factory.AbstractModelObjectFactory;
import eu.europeana.annotation.definitions.model.resource.selector.Selector;
import eu.europeana.annotation.definitions.model.resource.selector.impl.BaseSvgSelector;
import eu.europeana.annotation.definitions.model.resource.selector.impl.BaseTextPositionSelector;
import eu.europeana.annotation.definitions.model.resource.selector.impl.SvgRectangleSelector;
import eu.europeana.annotation.definitions.model.vocabulary.SelectorTypes;

public class TagObjectFactory extends AbstractModelObjectFactory<Selector, SelectorTypes>{

	private static TagObjectFactory singleton;

	//force singleton usage
	private TagObjectFactory(){};
	
	public static TagObjectFactory getInstance() {

		if (singleton == null) {
			synchronized (TagObjectFactory.class) {
				singleton = new TagObjectFactory();
			}
		}

		return singleton;

	}

	@Override
	public Class<? extends Selector> getClassForType(Enum<SelectorTypes> modelType) {
				Class<? extends Selector> returnType = null;
				SelectorTypes selectorType = SelectorTypes.valueOf(modelType.name());
				switch (selectorType){
				case TEXT_POSITION_SELECTOR:
					returnType = BaseTextPositionSelector.class;
					break;
				case SVG_RECTANGLE_SELECTOR:
					returnType = SvgRectangleSelector.class;
					break;
				case SVG_SELECTOR:
					returnType = BaseSvgSelector.class;
					break;
					
				default:
					throw new AnnotationAttributeInstantiationException("Not Supported target type: " + modelType);
				
				} 
				
				return returnType;
	}

	@Override
	public Class<SelectorTypes> getEnumClass() {
		return SelectorTypes.class;
	}

	
}
