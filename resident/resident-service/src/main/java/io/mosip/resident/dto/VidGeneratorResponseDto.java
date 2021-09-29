package io.mosip.resident.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@ApiModel(description = "Model representing a Vid Response")
public class VidGeneratorResponseDto {
	
	private String vidStatus;
	
	@JsonProperty("VID")
	private String VID;
	
	private VidGeneratorResponseDto restoredVid;
	
	
	@JsonProperty("UIN")
	private String UIN;
	

}