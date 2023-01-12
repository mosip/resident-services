package io.mosip.resident.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * This class is used to provide request for OTP generation.
 * 
 * @author Dinesh Karuppiah
 *
 */

@Data
@EqualsAndHashCode(callSuper=true)
public class AidOtpRequestDTO extends OtpRequestDTO{

	/** Variable to hold individualID */
	private String aid;

}