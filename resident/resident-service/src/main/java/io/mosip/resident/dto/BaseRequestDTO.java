package io.mosip.resident.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * Instantiates a new base request response DTO.
 * @author Monobikash Das
 */
@Data
public class BaseRequestDTO implements Serializable{
	
	private static final long serialVersionUID = 4373201325809902206L;

	/** The id. */
	private String id;
	
	/** The ver. */
	private String version;
	
	/** The timestamp. */
	private String requesttime;

}
