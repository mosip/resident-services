package io.mosip.resident.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Instantiates a new response DTO.
 * @author Rishabh Keshari
 */
@Data
public class PacketReceiverSubResponseDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3501660956959221378L;
	
	/** The status. */
	private String status;
	
}
