package io.mosip.resident.exception;

import java.util.Map;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.util.ObjectWithMetadata;

/**
 * The Class ResidentServiceException.
 */
public class ResidentServiceException extends BaseUncheckedException implements ObjectWithMetadata {

	/** Generated serial version id. */
	private static final long serialVersionUID = 8621530697947108810L;
	
	private Map<String,Object> metadata;

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
	
	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}
	
	public ResidentServiceException(ResidentErrorCode err, Throwable rootCause, Map<String, Object> metadata) {
		this(err, rootCause);
		this.metadata = metadata;
	}
	
	public ResidentServiceException(ResidentErrorCode err, Map<String, Object> metadata) {
		this(err);
		this.metadata = metadata;
	}

}
