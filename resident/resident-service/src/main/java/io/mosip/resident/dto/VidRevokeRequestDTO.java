package io.mosip.resident.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@EqualsAndHashCode(callSuper=true)
public class VidRevokeRequestDTO extends BaseVidRevokeRequestDTO implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3672610022968150191L;
	private String individualId;
	private String otp;
}
