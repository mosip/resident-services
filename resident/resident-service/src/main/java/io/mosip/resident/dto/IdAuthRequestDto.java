package io.mosip.resident.dto;

import lombok.Data;

@Data
public class IdAuthRequestDto {

	private String transactionID;
	private String individualId;
	private String otp;

}
