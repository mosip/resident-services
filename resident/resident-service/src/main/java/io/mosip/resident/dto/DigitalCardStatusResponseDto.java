package io.mosip.resident.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Kamesh Shekhar Prasad
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigitalCardStatusResponseDto {

	private String id;

	private String statusCode;

	private String url;
}
