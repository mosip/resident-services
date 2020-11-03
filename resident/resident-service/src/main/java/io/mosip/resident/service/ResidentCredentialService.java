package io.mosip.resident.service;

import io.mosip.resident.dto.CredentialCancelRequestResponseDto;
import io.mosip.resident.dto.CredentialRequestStatusResponseDto;
import io.mosip.resident.dto.CredentialTypeResponse;
import io.mosip.resident.dto.ResidentCredentialRequestDto;
import io.mosip.resident.dto.ResidentCredentialResponseDto;
import io.mosip.resident.exception.ResidentServiceCheckedException;

public interface ResidentCredentialService {

	public ResidentCredentialResponseDto reqCredential(ResidentCredentialRequestDto request) throws ResidentServiceCheckedException;

	public CredentialRequestStatusResponseDto getStatus(String requestId) throws ResidentServiceCheckedException;

	public CredentialTypeResponse getCredentialTypes();

	public CredentialCancelRequestResponseDto getCancelCredentialRequest(String requestId);
}
