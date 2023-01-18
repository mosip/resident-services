package io.mosip.resident.exception;

import java.util.Map;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.util.ObjectWithMetadata;

/**
 * @author Ritik Jain
 */
public class BaseResidentUncheckedExceptionWithMetadata extends BaseUncheckedException implements ObjectWithMetadata {

	/** Generated serial version id. */
	private static final long serialVersionUID = 8621530697947108810L;
	
	private Map<String,Object> metadata;
	
	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public BaseResidentUncheckedExceptionWithMetadata() {
        super();
    }

	/**
	 * Constructor the initialize Handler exception.
	 *
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 */
	public BaseResidentUncheckedExceptionWithMetadata(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructor the initialize Handler exception.
	 *
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 * @param rootCause    the specified cause
	 */
	public BaseResidentUncheckedExceptionWithMetadata(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

	public BaseResidentUncheckedExceptionWithMetadata(ResidentErrorCode err, Map<String, Object> metadata) {
		this(err.getErrorCode(), err.getErrorMessage());
		this.metadata = metadata;
	}

	public BaseResidentUncheckedExceptionWithMetadata(ResidentErrorCode err, Throwable rootCause, Map<String, Object> metadata) {
		this(err.getErrorCode(), err.getErrorMessage(), rootCause);
		this.metadata = metadata;
	}

	public BaseResidentUncheckedExceptionWithMetadata(String errorCode, String errorMessage, Throwable rootCause,
			Map<String, Object> metadata) {
		this(errorCode, errorMessage, rootCause);
		this.metadata = metadata;
	}

}
