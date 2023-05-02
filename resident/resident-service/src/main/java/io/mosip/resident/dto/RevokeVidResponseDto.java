package io.mosip.resident.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The revoke vid response dto.
 * 
 * @author Ritik Jain
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RevokeVidResponseDto extends VidRevokeResponseDTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1973880019812497700L;
	private String status;

}
