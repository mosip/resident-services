package io.mosip.resident.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RIDDigitalCardRequestDto {

	private String transactionID;

	private String individualId;

	private String otp;

}
