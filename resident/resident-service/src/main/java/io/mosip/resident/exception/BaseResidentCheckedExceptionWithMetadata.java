package io.mosip.resident.exception;

import java.util.Map;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.util.ObjectWithMetadata;

/**
 * @author Ritik Jain
 */
public class BaseResidentCheckedExceptionWithMetadata extends BaseCheckedException implements ObjectWithMetadata {
	
	private static final long serialVersionUID = -1561461793874550645L;
	
	private Map<String, Object> metadata;
	
	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public BaseResidentCheckedExceptionWithMetadata() {
		super();
	}

	public BaseResidentCheckedExceptionWithMetadata(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	public BaseResidentCheckedExceptionWithMetadata(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

	public BaseResidentCheckedExceptionWithMetadata(ResidentErrorCode err, Map<String, Object> metadata) {
		this(err.getErrorCode(), err.getErrorMessage());
		this.metadata = metadata;
	}

	public BaseResidentCheckedExceptionWithMetadata(String errorCode, String errorMessage, Map<String, Object> metadata) {
		this(errorCode, errorMessage);
		this.metadata = metadata;
	}

	public BaseResidentCheckedExceptionWithMetadata(String errorCode, String errorMessage, Throwable rootCause,
			Map<String, Object> metadata) {
		this(errorCode, errorMessage, rootCause);
		this.metadata = metadata;
	}

}
