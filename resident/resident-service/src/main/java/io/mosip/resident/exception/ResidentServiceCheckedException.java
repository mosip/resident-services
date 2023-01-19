package io.mosip.resident.exception;

import java.util.Map;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.util.ObjectWithMetadata;
/**
 * 
 * @author Girish Yarru
 *
 */
public class ResidentServiceCheckedException extends BaseCheckedException implements ObjectWithMetadata {
	
	private static final long serialVersionUID = -1561461793874550645L;
	
	private Map<String, Object> metadata;

	public ResidentServiceCheckedException() {
		super();
	}
	
	public ResidentServiceCheckedException(ResidentErrorCode error) {
		this(error.getErrorCode(), error.getErrorMessage());
	}

	public ResidentServiceCheckedException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	public ResidentServiceCheckedException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

	public ResidentServiceCheckedException(ResidentErrorCode error, ApisResourceAccessException e) {
		this(error.getErrorCode(), error.getErrorMessage(), e);
	}
	
	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}
	
	public ResidentServiceCheckedException(ResidentErrorCode err, Map<String, Object> metadata) {
		this(err.getErrorCode(), err.getErrorMessage());
		this.metadata = metadata;
	}

}
