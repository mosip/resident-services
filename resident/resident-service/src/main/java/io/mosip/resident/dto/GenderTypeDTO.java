package io.mosip.resident.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resident proxy masterdata Gender API DTO.
 * 
 * @author Neha Farheen
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenderTypeDTO {

	private String code;
	private String genderName;
	private String langCode;
	private String isActive;

}
