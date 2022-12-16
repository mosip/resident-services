package io.mosip.resident.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The validate otp response dto.
 * 
 * @author Ritik Jain
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ValidateOtpResponseDto extends IdAuthResponseDto {

	private String status;

}
