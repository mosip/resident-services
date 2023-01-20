package io.mosip.resident.exception;

import java.util.Map;

import io.mosip.resident.constant.ResidentErrorCode;

public class ResidentCredentialServiceException extends BaseResidentUncheckedExceptionWithMetadata {

	/**
	 * Generated serial version id
	 */
	private static final long serialVersionUID = 8621530697947108810L;
	
	/**
	 * Constructor the initialize Handler exception
	 * 
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 */
	public ResidentCredentialServiceException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructor the initialize Handler exception
	 * 
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 * @param rootCause    the specified cause
	 */
	public ResidentCredentialServiceException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

	public ResidentCredentialServiceException(ResidentErrorCode err, Throwable rootCause, Map<String, Object> metadata) {
		super(err.getErrorCode(), err.getErrorMessage(), rootCause, metadata);
	}
}
