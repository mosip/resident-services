package io.mosip.resident.exception;

import java.util.Map;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.util.ObjectWithMetadata;

public class VidAlreadyPresentException extends BaseUncheckedException implements ObjectWithMetadata {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5320581589143112542L;
	private Map<String, Object> metadata;

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
    
	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public VidAlreadyPresentException(String errorMessage, Throwable rootCause, Map<String, Object> metadata) {
		super(ResidentErrorCode.VID_ALREADY_PRESENT.getErrorCode(), errorMessage, rootCause);
		this.metadata = metadata;
	}
}
