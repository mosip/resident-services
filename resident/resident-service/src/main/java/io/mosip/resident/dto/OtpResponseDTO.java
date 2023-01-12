package io.mosip.resident.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * This class is used to provide response for OTP generation.
 * 
 * @author Dinesh Karuppiah
 *
 */

@Data
public class OtpResponseDTO {

	/** Variable to hold id */
	private String id;

	/** Variable to hold id */
	private String version;

	/** Variable to hold id */
	private String transactionID;

	/** Variable to hold id */
	private String responseTime;

	/** List to hold errors */
	private List<AuthError> errors;

	/** List to hold response */
	private MaskedResponseDTO response;
	
	private Map<String, Object> metadata;

}
