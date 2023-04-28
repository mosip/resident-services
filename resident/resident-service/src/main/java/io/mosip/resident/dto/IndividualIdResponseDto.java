/**
 * 
 */
package io.mosip.resident.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * @author  Aiham Hasan
 *
 */
@Data
public class IndividualIdResponseDto {

	private String id;
	
	private String version;
	
	private String transactionId;
	
	private String responseTime;
	
	private List<AuthError> errors;

	private MaskedResponseDTO response;
	
	private Map<String, Object> metadata;
}
