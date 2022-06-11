package io.mosip.resident.service;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.dto.*;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;

import java.time.LocalDateTime;
import java.util.List;

public interface ResidentService {

	public RegStatusCheckResponseDTO getRidStatus(RequestDTO dto) throws ApisResourceAccessException;

	public byte[] reqEuin(EuinRequestDTO euinRequestDTO) throws ResidentServiceCheckedException;

	public ResidentReprintResponseDto reqPrintUin(ResidentReprintRequestDto dto) throws ResidentServiceCheckedException;

	public ResponseDTO reqAauthTypeStatusUpdate(AuthLockOrUnLockRequestDto dto, AuthTypeStatus authTypeStatus)
			throws ResidentServiceCheckedException;

	public AuthHistoryResponseDTO reqAuthHistory(AuthHistoryRequestDTO dto) throws ResidentServiceCheckedException;
	
	public ResidentUpdateResponseDTO reqUinUpdate(ResidentUpdateRequestDto dto) throws ResidentServiceCheckedException;

	ResponseDTO reqAauthTypeStatusUpdateV2(AuthLockOrUnLockRequestDtoV2 request) throws ResidentServiceCheckedException, ApisResourceAccessException;

	public ResponseWrapper<Object> getAuthLockStatus(String individualId) throws ResidentServiceCheckedException;;

	List<AutnTxnDto> getAuthTxnDetails(String individualId, Integer pageStart, Integer pageFetch, String idType
	, LocalDateTime fromDateTime, LocalDateTime toDateTime) throws ResidentServiceCheckedException;

	RegStatusCheckResponseDTO getRidStatus(String rid);

	AidStatusResponseDTO getAidStatus(AidStatusRequestDTO reqDto)
			throws ResidentServiceCheckedException, ApisResourceAccessException, OtpValidationFailedException;
}
