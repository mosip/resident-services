package io.mosip.resident.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.resident.constant.ResidentErrorCode;

public class InvalidInputException extends BaseUncheckedException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 7102528338044174257L;

	public InvalidInputException() {
        super(ResidentErrorCode.INVALID_INPUT.getErrorCode(), ResidentErrorCode.INVALID_INPUT.getErrorMessage());
    }

    public InvalidInputException(String errorMessage) {
        super(ResidentErrorCode.INVALID_INPUT.getErrorCode(), ResidentErrorCode.INVALID_INPUT.getErrorMessage() + errorMessage);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(ResidentErrorCode.INVALID_INPUT.getErrorCode(), ResidentErrorCode.INVALID_INPUT.getErrorMessage() + message, cause);
    }
}
