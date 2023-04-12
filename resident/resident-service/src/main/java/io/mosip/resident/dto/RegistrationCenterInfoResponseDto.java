package io.mosip.resident.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Ritik Jain
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationCenterInfoResponseDto extends RegistrationCenterResponseDto {

	/** The registration centers data. */
	private List<RegistrationCenterDto> data;

}
