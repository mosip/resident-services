package io.mosip.resident.service;

import java.io.IOException;
import java.util.List;

import io.mosip.resident.dto.CredentialCancelRequestResponseDto;
import io.mosip.resident.dto.CredentialRequestStatusResponseDto;
import io.mosip.resident.dto.CredentialTypeResponse;
import io.mosip.resident.dto.PartnerCredentialTypePolicyDto;
import io.mosip.resident.dto.ResidentCredentialRequestDto;
import io.mosip.resident.dto.ResidentCredentialResponseDto;
import io.mosip.resident.dto.ResidentCredentialResponseDtoV2;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.SharableAttributesDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import reactor.util.function.Tuple2;

public interface ResidentCredentialService {

	public ResidentCredentialResponseDto reqCredential(ResidentCredentialRequestDto request) throws ResidentServiceCheckedException, OtpValidationFailedException;
	
	public ResidentCredentialResponseDto reqCredential(ResidentCredentialRequestDto request, String id) throws ResidentServiceCheckedException;

	public Tuple2<ResidentCredentialResponseDtoV2, String> shareCredential(ResidentCredentialRequestDto request, String purpose, List<SharableAttributesDTO> sharableAttributes) throws ResidentServiceCheckedException, ApisResourceAccessException;

	public CredentialRequestStatusResponseDto getStatus(String requestId) throws ResidentServiceCheckedException;

	public CredentialTypeResponse getCredentialTypes();

	public CredentialCancelRequestResponseDto cancelCredentialRequest(String requestId);

	public byte[] getCard(String requestId) throws Exception;

	public ResponseWrapper<PartnerCredentialTypePolicyDto> getPolicyByCredentialType(String partnerId,
			String credentialType);

	public byte[] getCard(String requestId, String applicationId, String partnerReferenceId);

	public String getDataShareUrl(String requestId) throws ApisResourceAccessException, IOException;

}
