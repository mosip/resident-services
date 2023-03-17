package io.mosip.resident.dto;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Instantiates a new packet receiver response DTO.
 * @author Rishabh Keshari
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RegSyncResponseDTO extends BaseRestResponseDTO {
	
	/** The response. */
	private List<SyncResponseDto> response;
	
	/** The error. */
	private List<SyncErrorDTO> errors;
	
}
