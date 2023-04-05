package io.mosip.resident.exception;

import java.util.Map;

import io.mosip.resident.constant.ResidentErrorCode;

public class VidAlreadyPresentException extends BaseResidentUncheckedExceptionWithMetadata {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5320581589143112542L;

	public VidAlreadyPresentException() {
        super();
    }

    /**
     * Instantiates a new exception.
     *
     * @param errorCode    the error code
     * @param errorMessage the error message
     */
    public VidAlreadyPresentException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

	public VidAlreadyPresentException(String errorMessage, Throwable rootCause, Map<String, Object> metadata) {
		super(ResidentErrorCode.VID_ALREADY_PRESENT.getErrorCode(), errorMessage, rootCause, metadata);
	}
}
