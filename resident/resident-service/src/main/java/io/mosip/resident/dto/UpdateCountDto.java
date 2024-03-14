package io.mosip.resident.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The remaining update count dto.
 * 
 * @author Kamesh Shekhar Prasad
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateCountDto {
	private String attributeName;
	private int noOfUpdatesLeft;
}
