package io.mosip.resident.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class VidResponseDto implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6756239366488227369L;
	private String vid;
    private String message;
}
