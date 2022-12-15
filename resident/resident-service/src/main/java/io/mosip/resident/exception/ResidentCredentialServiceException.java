package io.mosip.resident.exception;

import java.util.Map;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.util.ObjectWithMetadata;

public class ResidentCredentialServiceException extends BaseUncheckedException implements ObjectWithMetadata {

	/**
	 * Generated serial version id
	 */
	private static final long serialVersionUID = 8621530697947108810L;
	
	private Map<String,Object> metadata;

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
	
	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}
	
	public ResidentCredentialServiceException(ResidentErrorCode err, Throwable rootCause, Map<String, Object> metadata) {
		this(err.getErrorCode(), err.getErrorMessage(), rootCause);
		this.metadata = metadata;
	}
}
