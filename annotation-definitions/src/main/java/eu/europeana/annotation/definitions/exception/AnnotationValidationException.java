package eu.europeana.annotation.definitions.exception;

/**
 * This class is used represent validation errors for the annotation class hierarchy 
 * @author Sergiu Gordea 
 *
 */
public class AnnotationValidationException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6895963160368650224L;
	public static final String ERROR_NULL_EUROPEANA_ID = "europeanaId must not be null";
	public static final String ERROR_NOT_NULL_OBJECT_ID = "Object ID must be null";

	public AnnotationValidationException(String message){
		super(message);
	}
	
	public AnnotationValidationException(String message, Throwable th){
		super(message, th);
	}
	
	
}
