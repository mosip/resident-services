package io.mosip.resident.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.resident.constant.ResidentErrorCode;

/**
 * The Class ResidentServiceException.
 */
public class ResidentServiceException extends BaseUncheckedException {

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

}
