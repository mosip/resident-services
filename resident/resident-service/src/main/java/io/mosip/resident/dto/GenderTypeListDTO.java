package io.mosip.resident.dto;

import java.util.List;

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
public class GenderTypeListDTO {

	private List<GenderTypeDTO> genderType;

}
