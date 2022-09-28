package io.mosip.resident.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The remaining update count dto.
 * 
 * @author Ritik Jain
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateCountDto {
	
	private String attributeName;
	private int updateCountLeft;

}
