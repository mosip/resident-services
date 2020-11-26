package io.mosip.resident.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CredentialRequestStatusDto {

	private String id;

	private String requestId;

	private String statusCode;

	private String url;
}
