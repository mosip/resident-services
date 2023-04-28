package io.mosip.resident.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.resident.constant.ResidentErrorCode;

/**
 * @author Kamesh Shekhar Prasad
 * This class is used to throw exception if invalid id is found.
 */

public class IndividualIdNotFoundException extends BaseUncheckedException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public IndividualIdNotFoundException() {
        super(ResidentErrorCode.INVALID_INDIVIDUAL_ID.getErrorCode(), ResidentErrorCode.INVALID_INDIVIDUAL_ID.getErrorMessage());
    }

    public IndividualIdNotFoundException(String errorMessage) {
        super(ResidentErrorCode.INVALID_INDIVIDUAL_ID.getErrorCode(), errorMessage);
    }

    public IndividualIdNotFoundException(String errorCode, String errorMessage){
        super(errorCode, errorMessage);
    }


}
