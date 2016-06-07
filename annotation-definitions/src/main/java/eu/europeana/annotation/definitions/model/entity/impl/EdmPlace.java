package eu.europeana.annotation.definitions.model.entity.impl;

import eu.europeana.annotation.definitions.model.entity.Place;

public class EdmPlace implements Place{

	String latitude;
	String longitude;
	
	@Override
	public String getLatitude() {
		return latitude;
	}
	@Override
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	@Override
	public String getLongitude() {
		return longitude;
	}
	@Override
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
}
