package io.mosip.resident.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * @author Kamesh
 */
public class InvalidRequestTypeCodeException extends BaseUncheckedException {

    private static final long serialVersionUID = 5320581589143112542L;

    public InvalidRequestTypeCodeException() {
        super();
    }

    /**
     * Instantiates a new exception.
     *
     * @param errorCode    the error code
     * @param errorMessage the error message
     */
    public InvalidRequestTypeCodeException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}

