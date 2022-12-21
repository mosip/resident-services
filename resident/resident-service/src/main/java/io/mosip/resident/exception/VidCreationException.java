package io.mosip.resident.exception;

import java.util.Map;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.util.ObjectWithMetadata;


public class VidCreationException extends BaseUncheckedException implements ObjectWithMetadata {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<String, Object> metadata;
	
	public VidCreationException() {
		super(ResidentErrorCode.VID_CREATION_EXCEPTION.getErrorCode(), ResidentErrorCode.VID_CREATION_EXCEPTION.getErrorMessage());
	}

	/**
	 * Instantiates a new reg proc checked exception.
	 *
	 * @param errorMessage the error message
	 */
	public VidCreationException(String errorMessage) {
		super(ResidentErrorCode.VID_CREATION_EXCEPTION.getErrorCode(), errorMessage);
	}
	
	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public VidCreationException(String errorMessage, Throwable rootCause, Map<String, Object> metadata) {
		super(ResidentErrorCode.VID_CREATION_EXCEPTION.getErrorCode(), errorMessage, rootCause);
		this.metadata = metadata;
	}

}
