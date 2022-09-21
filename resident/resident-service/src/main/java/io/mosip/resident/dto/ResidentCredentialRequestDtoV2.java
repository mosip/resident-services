package io.mosip.resident.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Manoj SP
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResidentCredentialRequestDtoV2 {
	private String transactionID;

	private String individualId;

	private String otp;

	private String credentialType;

	private boolean encrypt;

	private String issuer;

	private String encryptionKey;

	private String recepiant;

	private String user;

	private String purpose;

	private List<SharableAttributesDTO> sharableAttributes;

	private Map<String, Object> additionalData;

	private String consent;

}
