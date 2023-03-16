package io.mosip.resident.dto;

import lombok.Data;

@Data
public class VidResponseDtoV2 extends  VidResponseDto{

    /**
	 * 
	 */
	private static final long serialVersionUID = -5655123110778309784L;
	private String maskedPhone;
    private String maskedEmail;
}
