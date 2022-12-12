package io.mosip.resident.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The generate vid response dto.
 * 
 * @author Ritik Jain
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GenerateVidResponseDto extends VidResponseDtoV2 {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5728940858748492895L;
	private String eventId;
	private String status;

}
