package io.mosip.resident.dto;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class RequestDTO {

	private String individualId;

	private String individualIdType;

}