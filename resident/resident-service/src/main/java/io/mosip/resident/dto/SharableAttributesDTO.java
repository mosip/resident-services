package io.mosip.resident.dto;

import lombok.Data;

/**
 * @author Manoj SP
 *
 */
@Data
public class SharableAttributesDTO {

	private String attributeName;
	
	private String format;
	
	private boolean masked;
}
