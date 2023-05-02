package io.mosip.resident.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthHistoryRequestDTO {

	private String transactionID;

	private String individualId;

	private String otp;
	
	private String pageStart;
	
	private String pageFetch;
	
}
