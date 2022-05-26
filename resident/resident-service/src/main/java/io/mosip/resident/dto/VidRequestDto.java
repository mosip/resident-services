package io.mosip.resident.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class VidRequestDto extends BaseVidRequestDto {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8496426638724225974L;
	
	private String individualId;
    private String otp;

}
