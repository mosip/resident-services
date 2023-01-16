package io.mosip.resident.dto;

import lombok.Data;

@Data
public class AidStatusRequestDTO {

	private String individualId;
	private String otp;
	private String transactionId;
	
}
