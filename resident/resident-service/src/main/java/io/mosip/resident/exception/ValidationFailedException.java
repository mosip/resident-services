package io.mosip.resident.exception;

import io.mosip.kernel.core.exception.BaseCheckedException;

public class ValidationFailedException extends BaseCheckedException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new validation failed exception.
     */
    public ValidationFailedException() {
        super();
    }

    /**
     * Instantiates a new validation failed exception.
     * 
     * @param errorCode
     *            the errorCode
     * @param errorMessage
     *            the errorMessage
     */
    public ValidationFailedException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    /**
     * Instantiates a new validation failed exception.
     *
     * @param errorCode
     *            the errorCode
     * @param errorMessage
     *            the errorMessage
     * @param cause
     *            the cause
     */
    public ValidationFailedException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }
}
