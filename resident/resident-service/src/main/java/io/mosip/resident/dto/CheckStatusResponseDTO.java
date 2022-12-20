package io.mosip.resident.dto;

import lombok.Data;

/**
 * This class is used to provide response dto of check status for aid.
 * 
 * @author Kamesh Shekhar Prasad
 *
 */

@Data
public class CheckStatusResponseDTO {
	private String individualId;
	private String transactionID;
	private String transactionStage;
	private String aidStatus;
}