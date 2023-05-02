package io.mosip.resident.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Instantiates a new packet receiver response DTO.
 * 
 * @author Rishabh Keshari
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PacketReceiverResponseDTO extends BaseRestResponseDTO implements Serializable {

	private static final long serialVersionUID = -6943589722277098292L;

	/** The response. */
	private PacketReceiverSubResponseDTO response;

	/** The errors. */
	private List<ErrorDTO> errors;

}
