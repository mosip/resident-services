package io.mosip.resident.mock.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.resident.constant.ResidentErrorCode;

/**
 * @author Kamesh Shekhar Prasad
 * This class is used to throw exception for technical error use case.
 */
public class TechnicalErrorException extends BaseUncheckedException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new otp validation failed exception.
     */
    public TechnicalErrorException() {
        super(ResidentErrorCode.TECHNICAL_ERROR.getErrorCode(), ResidentErrorCode.TECHNICAL_ERROR.getErrorMessage());
    }

    /**
     * Instantiates a new otp validation failed exception.
     *
     * @param errorMessage
     *            the error message
     */
    public TechnicalErrorException(String errorMessage) {
        super(ResidentErrorCode.TECHNICAL_ERROR.getErrorCode(), errorMessage);
    }

    /**
     *
     * @param errorMessage
     */
    public TechnicalErrorException(String errorCode, String errorMessage) {
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
    public TechnicalErrorException(String message, Throwable cause) {
        super(ResidentErrorCode.TECHNICAL_ERROR.getErrorCode(), message, cause);
    }
}
