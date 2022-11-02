package io.mosip.resident.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * @author Kamesh Shekhar Prasad
 */
public class EventIdNotPresentException extends BaseUncheckedException {


	private static final long serialVersionUID = 5320581589143112542L;

	public EventIdNotPresentException() {
        super();
    }

    /**
     * Instantiates a new exception.
     *
     * @param errorCode    the error code
     * @param errorMessage the error message
     */
    public EventIdNotPresentException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
