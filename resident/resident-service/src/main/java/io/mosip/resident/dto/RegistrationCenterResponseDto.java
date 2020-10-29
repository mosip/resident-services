package io.mosip.resident.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
@Data

/**
 * Instantiates a new registration center response dto.
 *
 * @param registrationCenters
 *            the registration centers
 */
@AllArgsConstructor

/**
 * Instantiates a new registration center response dto.
 */
@NoArgsConstructor
public class RegistrationCenterResponseDto {

	/** The registration centers. */
	private List<RegistrationCenterDto> registrationCenters;


}
