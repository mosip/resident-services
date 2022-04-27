package io.mosip.resident.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * This class is used to provide request for OTP generation using RID.
 * 
 * @author Vishwanath V
 *
 */

@Data
public class RIDOtpRequestDTO {

	/** Variable to hold id */
	private String id;

	/** Variable to hold version */
	private String version;

	/** Variable to hold Transaction ID */
	private String transactionID;

	/** Variable to hold Request time */
	private String requestTime;

	/** Variable to hold individualID */
	private String individualId;

	/** Variable to hold otpChannel */
	private List<String> otpChannel;
	
	/** Variable to hold metadata */
	private Map<String, Object> metadata;

}