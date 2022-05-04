package io.mosip.resident.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * This class is used to provide response for Channel verification.
 * 
 * @author Kamesh shekhar prasad
 *
 */

@Data
public class VerificationResponseDTO {

	/** Variable to hold id */
	private String id;

	/** Variable to hold id */
	private String version;

	/** Variable to hold id */
	private String responseTime;

	private Map<String, Object> metadata;

	/** List to hold response */
	private VerificationStatusDTO response;

	/** List to hold errors */
	private List<AuthError> errors;
}
