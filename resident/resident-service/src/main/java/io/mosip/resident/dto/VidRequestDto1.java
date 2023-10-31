package io.mosip.resident.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@ApiModel(description = "Model representing a Vid Request")
public class VidRequestDto1 {

	private String vidType;

	@JsonProperty("UIN")
	private String UIN;

	private String vidStatus;

}