package io.mosip.resident.exception;

import io.mosip.kernel.core.exception.BaseCheckedException;

public class PacketManagerException extends BaseCheckedException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4181242224801724711L;

	public PacketManagerException(String errorCode, String message) {
        super(errorCode, message);
    }

    public PacketManagerException(String errorCode, String message, Throwable t) {
        super(errorCode, message, t);
    }
}
