package io.mosip.resident.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class VidResponseDto implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6756239366488227369L;
	private String vid;
    private String message;
}
