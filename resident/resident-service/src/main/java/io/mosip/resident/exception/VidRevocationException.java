package io.mosip.resident.exception;

import java.util.Map;

import io.mosip.resident.constant.ResidentErrorCode;

public class VidRevocationException extends BaseResidentUncheckedExceptionWithMetadata {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public VidRevocationException() {
		super(ResidentErrorCode.VID_REVOCATION_EXCEPTION.getErrorCode(), ResidentErrorCode.VID_REVOCATION_EXCEPTION.getErrorMessage());
	}

	/**
	 * Instantiates a new reg proc checked exception.
	 *
	 * @param errorMessage the error message
	 */
	public VidRevocationException(String errorMessage) {
		super(ResidentErrorCode.VID_REVOCATION_EXCEPTION.getErrorCode(), errorMessage);
	}
	public VidRevocationException(String errorMessage, Throwable rootCause) {
		super(ResidentErrorCode.VID_REVOCATION_EXCEPTION.getErrorCode(), errorMessage, rootCause);
	}
	
	public VidRevocationException(String errorMessage, Throwable rootCause, Map<String, Object> metadata) {
		super(ResidentErrorCode.VID_REVOCATION_EXCEPTION.getErrorCode(), errorMessage, rootCause, metadata);
	}

}
