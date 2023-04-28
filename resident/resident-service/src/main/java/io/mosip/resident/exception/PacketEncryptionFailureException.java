package io.mosip.resident.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.resident.constant.ResidentErrorCode;

/**
 * The Class PacketEncryptionFailureException.
 * @author Kamesh Shekhar Prasad
 */

public class PacketEncryptionFailureException extends BaseUncheckedException {
	private static final long serialVersionUID = 1L;

	public PacketEncryptionFailureException() {
		super(ResidentErrorCode.PACKET_ENCRYPTION_FAILURE_EXCEPTION.getErrorCode(),
				ResidentErrorCode.PACKET_ENCRYPTION_FAILURE_EXCEPTION.getErrorMessage());
	}

	public PacketEncryptionFailureException(Throwable t) {
		super(ResidentErrorCode.PACKET_ENCRYPTION_FAILURE_EXCEPTION.getErrorCode(), ResidentErrorCode.PACKET_ENCRYPTION_FAILURE_EXCEPTION.getErrorMessage(), t);
	}

	public PacketEncryptionFailureException(String message, Throwable cause) {
		super(ResidentErrorCode.PACKET_ENCRYPTION_FAILURE_EXCEPTION.getErrorCode(), message, cause);
	}

	public PacketEncryptionFailureException(String errorMessage) {
		super(ResidentErrorCode.PACKET_ENCRYPTION_FAILURE_EXCEPTION.getErrorCode(), errorMessage);
	}
}
