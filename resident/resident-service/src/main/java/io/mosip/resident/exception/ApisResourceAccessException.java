package io.mosip.resident.exception;
	
import java.util.Map;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.util.ObjectWithMetadata;

/**
 * The Class ApisResourceAccessException.
 * 
 */
public class ApisResourceAccessException extends BaseCheckedException implements ObjectWithMetadata {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	private Map<String, Object> metadata;

	/**
	 * Instantiates a new apis resource access exception.
	 */
	public ApisResourceAccessException() {
		super();
	}

	/**
	 * Instantiates a new apis resource access exception.
	 *
	 * @param message the message
	 */
	public ApisResourceAccessException(String message) {
		super(ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode(), message);
	}

	/**
	 * Instantiates a new apis resource access exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public ApisResourceAccessException(String message, Throwable cause) {
		super(ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode(), message, cause);
	}
	
	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}
	
	public ApisResourceAccessException(String err, Throwable rootCause, Map<String, Object> metadata) {
		this(err, rootCause);
		this.metadata = metadata;
	}
}