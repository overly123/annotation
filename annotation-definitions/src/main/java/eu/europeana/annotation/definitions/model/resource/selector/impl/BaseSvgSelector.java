package eu.europeana.annotation.definitions.model.resource.selector.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eu.europeana.annotation.definitions.model.resource.selector.SvgSelector;
import eu.europeana.annotation.definitions.model.selector.shape.Point;
import eu.europeana.annotation.definitions.model.selector.shape.impl.PointImpl;
import eu.europeana.annotation.definitions.model.vocabulary.SelectorTypes;

public abstract class BaseSvgSelector extends BaseSelector implements SvgSelector{

//	private String selectorType;
	private Point origin;
	private List<Point> points;
//	private Map<String, Integer> dimensionMap;
	
	/* Style elements */
	String cssClass;
	String style;
	

	@Override
	public String getSelectorType() {
		return super.getSelectorType();
	}

	@Override
	public void setSelectorType(String selectorType) {
		super.setSelectorType(selectorType);
	}
	
	@Override
	public void setSelectorTypeEnum(SelectorTypes selectorType) {
		super.setSelectorTypeEnum(selectorType);
	}

	@Override
	public List<Point> getPoints() {
		return points;
	}

	@Override
	public void setPoints(List<Point> points) {
		this.points = points;
	}

	@Override
	public Point getOrigin() {
		return origin;
	}

	@Override
	public void setOrigin(Point origin) {
		this.origin = origin;
	}

	@Override
	public Map<String, Integer> getDimensionMap() {
		return super.getDimensionMap();
//		if (dimensionMap == null)
//			dimensionMap = new HashMap<String, Integer>();
//		return dimensionMap;
	}

	@Override
	public void setDimensionMap(Map<String, Integer> dimensionMap) {
		super.setDimensionMap(dimensionMap);
//		this.dimensionMap = dimensionMap;
	}

	@Override
	public String getCssClass() {
		return cssClass;
	}

	@Override
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	@Override
	public String getStyle() {
		return style;
	}

	@Override
	public void setStyle(String style) {
		this.style = style;
	}
	
	protected BaseSvgSelector(){
		super();
		origin = new PointImpl(0, 0);
		if(hasPoints())
			points = new ArrayList<Point>();
	}

	/**
	 * To be overwritten by selector that define additional points to the Origin like Poligons or Polilines 
	 * @return
	 */
	private boolean hasPoints() {
		return false;
	}
}
