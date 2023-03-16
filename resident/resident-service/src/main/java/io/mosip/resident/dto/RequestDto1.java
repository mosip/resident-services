package io.mosip.resident.dto;

import java.util.List;

import lombok.Data;

/**
 * The Class RequestDto.
 *
 * @author Nagalakshmi
 */

/**
 * Instantiates a new request dto.
 */
@Data
public class RequestDto1 {

	/** The identity. */
	private Object identity;

	/** The documents. */
	private List<Documents> documents;

	/** The registration id. */
	private String registrationId;

	private String status;

	/** The UIN */
	private String biometricReferenceId;
}
