package io.mosip.resident.exception;

/*
@author Kamesh Shekhar Prasad
 */
import io.mosip.resident.constant.ResidentErrorCode;

import java.util.Map;

/**
 * The Class NoSuchAlgorithmException.
 * 
 */
public class NoSuchAlgorithmException extends BaseResidentCheckedExceptionWithMetadata {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a no such algorithm exception.
	 */
	public NoSuchAlgorithmException() {
		super();
	}

	/**
	 * Instantiates a new apis resource access exception.
	 *
	 * @param message the message
	 */
	public NoSuchAlgorithmException(String message) {
		super(ResidentErrorCode.NO_SUCH_ALGORITHM_EXCEPTION.getErrorCode(), message);
	}

	/**
	 * Instantiates a no such algorithm exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public NoSuchAlgorithmException(String message, Throwable cause) {
		super(ResidentErrorCode.NO_SUCH_ALGORITHM_EXCEPTION.getErrorCode(), message, cause);
	}

	public NoSuchAlgorithmException(String errorCode, String errorMessage, Exception exception) {
		super(errorCode, errorMessage, exception);
	}

	public NoSuchAlgorithmException(String err, Throwable rootCause, Map<String, Object> metadata) {
		super(ResidentErrorCode.NO_SUCH_ALGORITHM_EXCEPTION.getErrorCode(), err, rootCause, metadata);
	}
}