package io.mosip.resident.mock.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.resident.constant.ResidentErrorCode;

/**
 * @author Kamesh Shekhar Prasad
 * This class is used to throw exception for payment canceled use case.
 */
public class PaymentCanceledException extends BaseUncheckedException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new otp validation failed exception.
     */
    public PaymentCanceledException() {
        super(ResidentErrorCode.PAYMENT_CANCELED.getErrorCode(), ResidentErrorCode.PAYMENT_CANCELED.getErrorMessage());
    }

    /**
     * Instantiates a new otp validation failed exception.
     *
     * @param errorMessage
     *            the error message
     */
    public PaymentCanceledException(String errorMessage) {
        super(ResidentErrorCode.PAYMENT_CANCELED.getErrorCode(), errorMessage);
    }

    /**
     *
     * @param errorMessage
     */
    public PaymentCanceledException(String errorCode, String errorMessage) {
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
    public PaymentCanceledException(String message, Throwable cause) {
        super(ResidentErrorCode.PAYMENT_CANCELED.getErrorCode(), message, cause);
    }
}
