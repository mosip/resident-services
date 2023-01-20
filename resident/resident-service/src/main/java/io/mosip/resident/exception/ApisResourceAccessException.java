package io.mosip.resident.exception;
	
import java.util.Map;

import io.mosip.resident.constant.ResidentErrorCode;

/**
 * The Class ApisResourceAccessException.
 * 
 */
public class ApisResourceAccessException extends BaseResidentCheckedExceptionWithMetadata {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Instantiates a new apis resource access exception.
	 */
	public ApisResourceAccessException() {
		super();
	}

	/**
	 * Instantiates a new apis resource access exception.
	 *
	 * @param message the message
	 */
	public ApisResourceAccessException(String message) {
		super(ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode(), message);
	}

	/**
	 * Instantiates a new apis resource access exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public ApisResourceAccessException(String message, Throwable cause) {
		super(ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode(), message, cause);
	}

	public ApisResourceAccessException(String errorCode, String errorMessage, Exception exception) {
		super(errorCode, errorMessage, exception);
	}
	
	public ApisResourceAccessException(String err, Throwable rootCause, Map<String, Object> metadata) {
		super(ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode(), err, rootCause, metadata);
	}
}