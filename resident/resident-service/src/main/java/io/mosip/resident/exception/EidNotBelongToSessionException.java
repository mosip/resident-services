/**
 * 
 */
package io.mosip.resident.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.resident.constant.ResidentErrorCode;

/**
 * @author Aiham Hasan
 *This class is used to throw exception for EID not belong to session use case.
 *
 */
public class EidNotBelongToSessionException extends BaseUncheckedException {
	
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
	
	public EidNotBelongToSessionException () {
		super(ResidentErrorCode.EID_NOT_BELONG_TO_SESSION.getErrorCode(), ResidentErrorCode.EID_NOT_BELONG_TO_SESSION.getErrorMessage());
	}

	public EidNotBelongToSessionException(ResidentErrorCode eidNotBelongToSession) {
		super(ResidentErrorCode.EID_NOT_BELONG_TO_SESSION.getErrorCode());
	}

	public EidNotBelongToSessionException(ResidentErrorCode eidNotBelongToSession, String errorMessage) {
		super(ResidentErrorCode.EID_NOT_BELONG_TO_SESSION.getErrorCode(), ResidentErrorCode.EID_NOT_BELONG_TO_SESSION.getErrorMessage());
	}

}
