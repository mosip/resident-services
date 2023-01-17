package io.mosip.resident.exception;

import java.util.Map;

import io.mosip.resident.constant.ResidentErrorCode;

/**
 * The Class ResidentServiceException.
 */
public class ResidentServiceException extends BaseResidentUncheckedExceptionWithMetadata {

	/** Generated serial version id. */
	private static final long serialVersionUID = 8621530697947108810L;
	
	/**
	 * Constructor the initialize Handler exception.
	 *
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 */
	public ResidentServiceException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
	
	/**
	 * Instantiates a new resident service exception.
	 *
	 * @param err the err
	 */
	public ResidentServiceException(ResidentErrorCode err) {
		this(err.getErrorCode(), err.getErrorMessage());
	}
	
	public ResidentServiceException(ResidentErrorCode err, String... args) {
		this(err.getErrorCode(), String.format(err.getErrorMessage(), args));
	}

	/**
	 * Constructor the initialize Handler exception.
	 *
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 * @param rootCause    the specified cause
	 */
	public ResidentServiceException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

	/**
	 * Instantiates a new resident service exception.
	 *
	 * @param err the err
	 * @param rootCause the root cause
	 */
	public ResidentServiceException(ResidentErrorCode err, Throwable rootCause) {
		this(err.getErrorCode(), err.getErrorMessage(), rootCause);
	}
	
	public ResidentServiceException(ResidentErrorCode err, Throwable rootCause, Map<String, Object> metadata) {
		super(err, rootCause, metadata);
	}
	
	public ResidentServiceException(ResidentErrorCode err, Map<String, Object> metadata) {
		super(err, metadata);
	}
	
	public ResidentServiceException(String errorCode, String errorMessage, Throwable rootCause,
			Map<String, Object> metadata) {
		super(errorCode, errorMessage, rootCause, metadata);
	}

}
