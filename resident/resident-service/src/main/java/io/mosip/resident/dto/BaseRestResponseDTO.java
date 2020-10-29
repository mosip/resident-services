package io.mosip.resident.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Instantiates a new base request response DTO.
 * @author Rishabh Keshari
 */
@Data
public class BaseRestResponseDTO implements Serializable {
	
	private static final long serialVersionUID = 4246582347420843195L;

	/** The id. */
	private String id;
	
	/** The ver. */
	private String version;
	
	/** The timestamp. */
	private String responsetime;

}
