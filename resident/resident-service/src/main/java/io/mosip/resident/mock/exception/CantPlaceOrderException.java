package io.mosip.resident.mock.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.resident.constant.ResidentErrorCode;

/**
 * @author Kamesh Shekhar Prasad
 * This class is used to throw exception for can't place order use case.
 */
public class CantPlaceOrderException extends BaseUncheckedException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new otp validation failed exception.
     */
    public CantPlaceOrderException() {
        super(ResidentErrorCode.CAN_T_PLACE_ORDER.getErrorCode(), ResidentErrorCode.CAN_T_PLACE_ORDER.getErrorMessage());
    }

    /**
     * Instantiates a new otp validation failed exception.
     *
     * @param errorMessage
     *            the error message
     */
    public CantPlaceOrderException(String errorMessage) {
        super(ResidentErrorCode.CAN_T_PLACE_ORDER.getErrorCode(), errorMessage);
    }

    /**
     *
     * @param errorMessage
     */
    public CantPlaceOrderException(String errorCode, String errorMessage) {
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
    public CantPlaceOrderException(String message, Throwable cause) {
        super(ResidentErrorCode.CAN_T_PLACE_ORDER.getErrorCode(), message, cause);
    }
}
