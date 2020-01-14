package io.mosip.resident.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import io.mosip.resident.constant.IdType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthHistoryRequestDTO {

	private String transactionID;

	private String individualId;

	private String individualIdType;

	private String otp;
	
	private String pageStart;
	
	private String pageFetch;
	
}
