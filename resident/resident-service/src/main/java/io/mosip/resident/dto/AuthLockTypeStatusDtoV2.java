package io.mosip.resident.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * This dto is used for conversion of an object.
 * 
 * @author Ritik Jain
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class AuthLockTypeStatusDtoV2 extends AuthTypeStatusDtoV2 {
	
	private String requestId;
	private String metadata;

}
