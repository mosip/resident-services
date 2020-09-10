package io.mosip.resident.dto;

import lombok.Data;

import java.util.List;

/**
 * The Class ResponseDTO.
 *
 * @author M1048358 Alok
 */
@Data
public class ResponseDTO1 {

	/** The entity. */
	private String entity;
	
	/** The identity. */
	private Object identity;
	
	private List<Documents> documents;
	
	/** The status. */
	private String status;

}
