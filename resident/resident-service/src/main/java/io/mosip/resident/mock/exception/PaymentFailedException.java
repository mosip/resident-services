package io.mosip.resident.mock.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.resident.constant.ResidentErrorCode;

/**
 * @author Kamesh Shekhar Prasad
 * This class is used to throw exception for payment failed use case.
 */
public class PaymentFailedException extends BaseUncheckedException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new otp validation failed exception.
     */
    public PaymentFailedException() {
        super(ResidentErrorCode.PAYMENT_FAILED.getErrorCode(), ResidentErrorCode.PAYMENT_FAILED.getErrorMessage());
    }

    /**
     * Instantiates a new otp validation failed exception.
     *
     * @param errorMessage
     *            the error message
     */
    public PaymentFailedException(String errorMessage) {
        super(ResidentErrorCode.PAYMENT_FAILED.getErrorCode(), errorMessage);
    }

    /**
     *
     * @param errorMessage
     */
    public PaymentFailedException(String errorCode, String errorMessage) {
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
    public PaymentFailedException(String message, Throwable cause) {
        super(ResidentErrorCode.PAYMENT_FAILED.getErrorCode(), message, cause);
    }
}
