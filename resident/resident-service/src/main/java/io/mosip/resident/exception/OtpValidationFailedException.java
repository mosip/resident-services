package io.mosip.resident.exception;

import java.util.Map;

import io.mosip.resident.constant.ResidentErrorCode;

public class OtpValidationFailedException extends BaseResidentCheckedExceptionWithMetadata {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
    
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

	public OtpValidationFailedException(String errorMessage, Map<String, Object> metadata) {
		super(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(), errorMessage, metadata);
	}
}
