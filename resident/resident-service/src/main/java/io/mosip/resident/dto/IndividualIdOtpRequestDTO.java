package io.mosip.resident.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * This class is used to provide request for OTP generation.
 * 
 * @author Aiham Hasan
 *
 */

@Data
@EqualsAndHashCode(callSuper=true)
public class IndividualIdOtpRequestDTO extends OtpRequestDTO{

	/** Variable to hold individualID */
	private String individualId;

}