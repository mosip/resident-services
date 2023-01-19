package io.mosip.resident.exception;

import java.util.Map;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.util.ObjectWithMetadata;

public class OtpValidationFailedException extends BaseCheckedException implements ObjectWithMetadata {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
    
    private Map<String,Object> metadata;

    /**
     * Instantiates a new otp validation failed exception.
     */
    public OtpValidationFailedException() {
        super(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(), ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
    }

    /**
     * Instantiates a new otp validation failed exception.
     *
     * @param errorMessage
     *            the error message
     */
    public OtpValidationFailedException(String errorMessage) {
        super(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(), errorMessage);
    }

    /**
     *
     * @param errorMessage
     */
    public OtpValidationFailedException(String errorCode, String errorMessage) {
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
    public OtpValidationFailedException(String message, Throwable cause) {
        super(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(), message, cause);
    }
    
    public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}
	
	public OtpValidationFailedException(String errorMessage, Map<String, Object> metadata) {
		this(errorMessage);
		this.metadata = metadata;
	}
}
