package io.mosip.resident.dto;

import lombok.Data;

/**
 * The Class IdRequestDTO.
 *
 * @author Nagalakshmi
 */
@Data
public class IdRequestDto {

	/** The id. */
	private String id;

	/** The request. */
	private RequestDto1 request;

	/** The time stamp. */
	private String requesttime;

	/** The version. */
	private String version;

	private Object metadata;

}
