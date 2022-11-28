package io.mosip.resident.service;

import io.mosip.resident.dto.CredentialCancelRequestResponseDto;
import io.mosip.resident.dto.CredentialRequestStatusResponseDto;
import io.mosip.resident.dto.CredentialTypeResponse;
import io.mosip.resident.dto.PartnerCredentialTypePolicyDto;
import io.mosip.resident.dto.ResidentCredentialRequestDto;
import io.mosip.resident.dto.ResidentCredentialResponseDto;
import io.mosip.resident.dto.ResidentCredentialResponseDtoV2;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;

public interface ResidentCredentialService {

	public ResidentCredentialResponseDto reqCredential(ResidentCredentialRequestDto request) throws ResidentServiceCheckedException;
	
	public ResidentCredentialResponseDto reqCredential(ResidentCredentialRequestDto request, String id) throws ResidentServiceCheckedException;
	
	public ResidentCredentialResponseDtoV2 shareCredential(ResidentCredentialRequestDto request, String requestType) throws ResidentServiceCheckedException, ApisResourceAccessException;
	
	public ResidentCredentialResponseDtoV2 shareCredential(ResidentCredentialRequestDto request, String requestType, String purpose) throws ResidentServiceCheckedException, ApisResourceAccessException;
	
	public CredentialRequestStatusResponseDto getStatus(String requestId) throws ResidentServiceCheckedException;

	public CredentialTypeResponse getCredentialTypes();

	public CredentialCancelRequestResponseDto cancelCredentialRequest(String requestId);

	public byte[] getCard(String requestId) throws Exception;

	public ResponseWrapper<PartnerCredentialTypePolicyDto> getPolicyByCredentialType(String partnerId,
			String credentialType);

	public byte[] getCard(String requestId, String applicationId, String partnerReferenceId) throws Exception;

}
