package io.mosip.resident.dto;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.mosip.resident.constant.IdType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResidentUpdateRequestDto {

	private String transactionID;

	private String individualId;

	private String individualIdType;

	private String otp;

	private String identityJson;
	private List<ResidentDocuments> documents;

}
