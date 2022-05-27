package io.mosip.resident.service;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.dto.*;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;

import java.util.List;

public interface ResidentService {

	public RegStatusCheckResponseDTO getRidStatus(RequestDTO dto) throws ApisResourceAccessException;

	public byte[] reqEuin(EuinRequestDTO euinRequestDTO) throws ResidentServiceCheckedException;

	public ResidentReprintResponseDto reqPrintUin(ResidentReprintRequestDto dto) throws ResidentServiceCheckedException;

	public ResponseDTO reqAauthTypeStatusUpdate(AuthLockOrUnLockRequestDto dto, AuthTypeStatus authTypeStatus)
			throws ResidentServiceCheckedException;

	public AuthHistoryResponseDTO reqAuthHistory(AuthHistoryRequestDTO dto) throws ResidentServiceCheckedException;
	
	public ResidentUpdateResponseDTO reqUinUpdate(ResidentUpdateRequestDto dto) throws ResidentServiceCheckedException;

	ResponseDTO reqAauthTypeStatusUpdateV2(AuthLockOrUnLockRequestDto request, AuthTypeStatus lock) throws ResidentServiceCheckedException;

	public ResponseWrapper<Object> getAuthLockStatus(String individualId) throws ResidentServiceCheckedException;;

	List<AutnTxnDto> getAuthTxnDetails(String individualId, Integer pageStart, Integer pageFetch, String idType) throws ResidentServiceCheckedException;
}
