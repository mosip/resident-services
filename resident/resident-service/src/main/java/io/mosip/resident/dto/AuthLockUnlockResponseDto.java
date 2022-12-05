package io.mosip.resident.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The Auth Lock Unlock response dto.
 * 
 * @author Ritik Jain
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuthLockUnlockResponseDto extends ResponseDTO {

	private String eventId;

}
