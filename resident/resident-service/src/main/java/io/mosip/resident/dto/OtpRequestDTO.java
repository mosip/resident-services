package io.mosip.resident.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * This class is used to provide request for OTP generation.
 * 
 * @author Dinesh Karuppiah
 *
 */

@Data
public class OtpRequestDTO {

	/** Variable to hold id */
	private String id;

	/** Variable to hold version */
	private String version;

	/** Variable to hold Transaction ID */
	private String transactionId;

	/** Variable to hold Request time */
	private String requestTime;

	/** Variable to hold individualID */
	private String individualId;

	private List<String> otpChannel;
	
	private Map<String, Object> metadata;

}