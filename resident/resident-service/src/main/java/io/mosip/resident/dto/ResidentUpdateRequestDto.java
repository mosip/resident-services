package io.mosip.resident.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class ResidentUpdateRequestDto extends ResidentDemographicUpdateRequestDTO {

	private String individualId;
	
	private String individualIdType;

	private String otp;

	private List<ResidentDocuments> documents;
	
	private String identityJson;
	

}
