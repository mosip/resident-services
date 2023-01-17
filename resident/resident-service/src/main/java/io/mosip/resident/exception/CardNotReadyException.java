package io.mosip.resident.exception;

import java.util.Map;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.util.ObjectWithMetadata;

/**
 * @author Kamesh Shekhar Prasad
 * This class is used to throw exception for card not available use case.
 */
public class CardNotReadyException extends BaseUncheckedException implements ObjectWithMetadata {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
    
    private Map<String,Object> metadata;

    /**
     * Instantiates a new otp validation failed exception.
     */
    public CardNotReadyException() {
        super(ResidentErrorCode.CARD_NOT_READY.getErrorCode(), ResidentErrorCode.CARD_NOT_READY.getErrorMessage());
    }

    /**
     * Instantiates a new otp validation failed exception.
     *
     * @param errorMessage
     *            the error message
     */
    public CardNotReadyException(String errorMessage) {
        super(ResidentErrorCode.CARD_NOT_READY.getErrorCode(), errorMessage);
    }

    /**
     *
     * @param errorMessage
     */
    public CardNotReadyException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    /**
     * Instantiates a new otp validation failed exception.
     *
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public CardNotReadyException(String message, Throwable cause) {
        super(ResidentErrorCode.CARD_NOT_READY.getErrorCode(), message, cause);
    }
    
    public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}
	
	public CardNotReadyException(Map<String, Object> metadata) {
		this();
		this.metadata = metadata;
	}
}
