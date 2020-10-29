package io.mosip.resident.dto;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * The Class IdResponseDTO.
 *
 * @author M1048358 Alok
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonFilter("responseFilter")
public class IdResponseDTO extends BaseIdRequestResponseDTO {

	/** The err. */
	private List<ErrorDTO> error;

	private Object metadata;

	/** The response. */
	@JsonFilter("responseFilter")
	private ResponseDTO response;
}
