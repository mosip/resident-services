/**
 * 
 */
package io.mosip.resident.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.resident.constant.ResidentErrorCode;

/**
 * @author Aiham Hasan
 *This class is used to throw exception for Digital Card RID not found use case.
 *
 */
public class DigitalCardRidNotFoundException extends BaseUncheckedException {
	
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
    
	public DigitalCardRidNotFoundException () {
		super(ResidentErrorCode.DIGITAL_CARD_RID_NOT_FOUND.getErrorCode(), ResidentErrorCode.DIGITAL_CARD_RID_NOT_FOUND.getErrorMessage());
	}
	
	public DigitalCardRidNotFoundException(String errorCode, String errorMessage) {
		super(ResidentErrorCode.DIGITAL_CARD_RID_NOT_FOUND.getErrorCode(), ResidentErrorCode.DIGITAL_CARD_RID_NOT_FOUND.getErrorMessage());
	}

}
