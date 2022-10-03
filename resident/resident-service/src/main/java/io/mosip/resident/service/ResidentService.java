package io.mosip.resident.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.json.simple.JSONObject;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.dto.AidStatusRequestDTO;
import io.mosip.resident.dto.AidStatusResponseDTO;
import io.mosip.resident.dto.AuthHistoryRequestDTO;
import io.mosip.resident.dto.AuthHistoryResponseDTO;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDto;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDtoV2;
import io.mosip.resident.dto.BellNotificationDto;
import io.mosip.resident.dto.EuinRequestDTO;
import io.mosip.resident.dto.EventStatusResponseDTO;
import io.mosip.resident.dto.PageDto;
import io.mosip.resident.dto.RegStatusCheckResponseDTO;
import io.mosip.resident.dto.RequestDTO;
import io.mosip.resident.dto.ResidentReprintRequestDto;
import io.mosip.resident.dto.ResidentReprintResponseDto;
import io.mosip.resident.dto.ResidentServiceHistoryResponseDto;
import io.mosip.resident.dto.ResidentUpdateRequestDto;
import io.mosip.resident.dto.ResponseDTO;
import io.mosip.resident.dto.ServiceHistoryRequestDto;
import io.mosip.resident.dto.ServiceHistoryResponseDto;
import io.mosip.resident.dto.UnreadNotificationDto;
import io.mosip.resident.dto.UnreadServiceNotificationDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;

public interface ResidentService {

	public RegStatusCheckResponseDTO getRidStatus(RequestDTO dto) throws ApisResourceAccessException;

	public byte[] reqEuin(EuinRequestDTO euinRequestDTO) throws ResidentServiceCheckedException;

	public ResidentReprintResponseDto reqPrintUin(ResidentReprintRequestDto dto) throws ResidentServiceCheckedException;

	public ResponseDTO reqAauthTypeStatusUpdate(AuthLockOrUnLockRequestDto dto, AuthTypeStatus authTypeStatus)
			throws ResidentServiceCheckedException;

	public AuthHistoryResponseDTO reqAuthHistory(AuthHistoryRequestDTO dto) throws ResidentServiceCheckedException;

	public Object reqUinUpdate(ResidentUpdateRequestDto dto) throws ResidentServiceCheckedException;
	
	public Object reqUinUpdate(ResidentUpdateRequestDto dto, JSONObject demographicJsonObject) throws ResidentServiceCheckedException;
	
	ResponseDTO reqAauthTypeStatusUpdateV2(AuthLockOrUnLockRequestDtoV2 request)
			throws ResidentServiceCheckedException, ApisResourceAccessException;

	public ResponseWrapper<Object> getAuthLockStatus(String individualId) throws ResidentServiceCheckedException;;

	RegStatusCheckResponseDTO getRidStatus(String rid);

	AidStatusResponseDTO getAidStatus(AidStatusRequestDTO reqDto)
			throws ResidentServiceCheckedException, ApisResourceAccessException, OtpValidationFailedException;

	ResponseWrapper<PageDto<ServiceHistoryResponseDto>> getServiceHistory(Integer pageStart, Integer pageFetch,
			LocalDateTime fromDateTime, LocalDateTime toDateTime, String serviceType, String sortType,
			String searchColumn, String searchText) throws ResidentServiceCheckedException, ApisResourceAccessException;

	List<ResidentServiceHistoryResponseDto> getServiceRequestUpdate(Integer pageStart, Integer pageFetch)
			throws ResidentServiceCheckedException;

	List<ResidentServiceHistoryResponseDto> getServiceRequestUpdate(Integer pageStart, Integer pageFetch,
			String individualId) throws ResidentServiceCheckedException;

	List<ResidentServiceHistoryResponseDto> downloadCard(String individualId, String idType)
			throws ResidentServiceCheckedException;

	AidStatusResponseDTO getAidStatus(AidStatusRequestDTO reqDto, boolean performOtpValidation)
			throws ResidentServiceCheckedException, ApisResourceAccessException, OtpValidationFailedException;

	String checkAidStatus(String aid) throws ResidentServiceCheckedException;

	ResponseWrapper<EventStatusResponseDTO> getEventStatus(String id, String eventId)
			throws ResidentServiceCheckedException;

	ResponseWrapper<UnreadNotificationDto> getnotificationCount(String Id);

	ResponseWrapper<BellNotificationDto> getbellClickdttimes(String Id);

	int updatebellClickdttimes(String individualId);

	ResponseWrapper<List<UnreadServiceNotificationDto>> getUnreadnotifylist(String Id);
	
	byte[] getServiceHistoryPDF(ResponseWrapper<PageDto<ServiceHistoryResponseDto>> responseWrapper,
			String languageCode, LocalDateTime eventReqDateTime, LocalDateTime fromDateTime, LocalDateTime toDateTime,
			String serviceType, String statusFilter) throws ResidentServiceCheckedException, IOException;
}
