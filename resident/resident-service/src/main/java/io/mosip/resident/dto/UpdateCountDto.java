package io.mosip.resident.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateCountDto {
	
	private String attributeName;
	private int updateCountLeft;

}
