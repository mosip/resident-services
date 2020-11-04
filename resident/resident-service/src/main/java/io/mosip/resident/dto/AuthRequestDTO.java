package io.mosip.resident.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AuthRequestDTO extends BaseAuthRequestDTO {
	
	private String individualId;
	
	private String keyIndex;

	private String request;

	private String requestHMAC;
	
	private String requestSessionKey;
	
	private String requestTime;
	
	private AuthTypeDTO requestedAuth;
	
	private String transactionID;
	
	private String version;
	
	private String env;

	private String domainUri;

}
