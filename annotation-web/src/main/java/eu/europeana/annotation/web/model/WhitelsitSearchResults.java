package eu.europeana.annotation.web.model;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import eu.europeana.api2.web.model.json.abstracts.AbstractSearchResults;

@JsonSerialize(include = Inclusion.NON_EMPTY)
public class WhitelsitSearchResults<T> extends AbstractSearchResults<T> {

	public WhitelsitSearchResults(String apiKey, String action){
		super(apiKey, action);
	}
}
