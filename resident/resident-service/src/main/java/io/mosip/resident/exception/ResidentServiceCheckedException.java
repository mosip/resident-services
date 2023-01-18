package io.mosip.resident.exception;

import java.util.Map;

import io.mosip.resident.constant.ResidentErrorCode;

/**
 * 
 * @author Girish Yarru
 *
 */
public class ResidentServiceCheckedException extends BaseResidentCheckedExceptionWithMetadata {
	
	private static final long serialVersionUID = -1561461793874550645L;
	
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
	
	public ResidentServiceCheckedException(ResidentErrorCode err, Map<String, Object> metadata) {
		super(err, metadata);
	}
	
	public ResidentServiceCheckedException(String errorCode, String errorMessage, Throwable rootCause,
			Map<String, Object> metadata) {
		super(errorCode, errorMessage, rootCause, metadata);
	}

}
