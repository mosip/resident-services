package io.mosip.resident.dto;

import lombok.Data;

@Data
public class AidStatusResponseDTO {

	private String individualId;
	private String individualIdType;
	private String transactionId;
	private String aidStatus;
	
}
