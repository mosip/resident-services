package io.mosip.resident.dto;



import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AuthUnLockRequestDTO extends AuthLockOrUnLockRequestDto {
	
	private static final long serialVersionUID = 1L;
	
	private Long unlockForSeconds;


}
