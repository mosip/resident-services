package io.mosip.resident.exception;

import java.util.Map;

import io.mosip.resident.constant.ResidentErrorCode;

public class VidCreationException extends BaseResidentUncheckedExceptionWithMetadata {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
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
	
	public VidCreationException(String errorMessage, Throwable cause) {
		super(ResidentErrorCode.VID_CREATION_EXCEPTION.getErrorCode(), errorMessage, cause);
	}

	public VidCreationException(String errorMessage, Throwable rootCause, Map<String, Object> metadata) {
		super(ResidentErrorCode.VID_CREATION_EXCEPTION.getErrorCode(), errorMessage, rootCause, metadata);
	}

}
