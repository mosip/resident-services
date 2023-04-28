package io.mosip.resident.dto;

import lombok.Data;

@Data
public class IdAuthRequestDto {

	private String transactionId;
	private String individualId;
	private String otp;

}
