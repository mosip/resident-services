package io.mosip.resident.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * @author Manoj SP
 *
 */
@Data
public class SharableAttributesDTO {

	private String attributeName;
	
	private String format;
	
	@JsonProperty("isMasked")
	private boolean isMasked;
}
