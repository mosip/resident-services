package io.mosip.resident.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Instantiates a new base request response DTO.
 * @author Rishabh Keshari
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class BaseRestResponseDTO implements Serializable {
	
	private static final long serialVersionUID = 4246582347420843195L;

	/** The id. */
	private String id;
	
	/** The ver. */
	private String version;
	
	/** The timestamp. */
	private String responsetime;

}
