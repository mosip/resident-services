package io.mosip.resident.dto;



import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AuthTypeUnLockRequestDTO extends AuthTypeLockOrUnLockRequestDto {
	
	private static final long serialVersionUID = 1L;
	
	private String unlockForSeconds;


}
