package io.mosip.resident.service;

import java.io.IOException;
import java.time.LocalDate;
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
import io.mosip.resident.dto.ResidentUpdateRequestDto;
import io.mosip.resident.dto.ResponseDTO;
import io.mosip.resident.dto.ServiceHistoryResponseDto;
import io.mosip.resident.dto.UnreadNotificationDto;
import io.mosip.resident.dto.UserInfoDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import reactor.util.function.Tuple2;

public interface ResidentService {

	public RegStatusCheckResponseDTO getRidStatus(RequestDTO dto) throws ApisResourceAccessException;

	public byte[] reqEuin(EuinRequestDTO euinRequestDTO) throws ResidentServiceCheckedException;

	public ResidentReprintResponseDto reqPrintUin(ResidentReprintRequestDto dto) throws ResidentServiceCheckedException;

	public ResponseDTO reqAauthTypeStatusUpdate(AuthLockOrUnLockRequestDto dto, AuthTypeStatus authTypeStatus)
			throws ResidentServiceCheckedException;

	public AuthHistoryResponseDTO reqAuthHistory(AuthHistoryRequestDTO dto) throws ResidentServiceCheckedException;

	public Tuple2<Object, String> reqUinUpdate(ResidentUpdateRequestDto dto) throws ResidentServiceCheckedException;
	
	public Tuple2<Object, String> reqUinUpdate(ResidentUpdateRequestDto dto, JSONObject demographicJsonObject) throws ResidentServiceCheckedException;
	
	public Tuple2<ResponseDTO, String> reqAauthTypeStatusUpdateV2(AuthLockOrUnLockRequestDtoV2 request)
			throws ResidentServiceCheckedException, ApisResourceAccessException;

	public ResponseWrapper<AuthLockOrUnLockRequestDtoV2> getAuthLockStatus(String individualId) throws ResidentServiceCheckedException;;

	RegStatusCheckResponseDTO getRidStatus(String rid);

	AidStatusResponseDTO getAidStatus(AidStatusRequestDTO reqDto)
			throws ResidentServiceCheckedException, ApisResourceAccessException, OtpValidationFailedException;

	ResponseWrapper<PageDto<ServiceHistoryResponseDto>> getServiceHistory(Integer pageStart, Integer pageFetch,
																		  LocalDate fromDateTime, LocalDate toDateTime, String serviceType, String sortType,
																		  String searchColumn, String searchText, String langCode, int timeZoneOffset) throws ResidentServiceCheckedException, ApisResourceAccessException;

	byte[] downloadCard(String individualId, String idType)
			throws ResidentServiceCheckedException;

	AidStatusResponseDTO getAidStatus(AidStatusRequestDTO reqDto, boolean performOtpValidation)
			throws ResidentServiceCheckedException, ApisResourceAccessException, OtpValidationFailedException;

	String checkAidStatus(String aid) throws ResidentServiceCheckedException;

	ResponseWrapper<EventStatusResponseDTO> getEventStatus(String id, String eventId, int timeZoneOffset)
			throws ResidentServiceCheckedException;

	ResponseWrapper<UnreadNotificationDto> getnotificationCount(String Id) throws ApisResourceAccessException, ResidentServiceCheckedException;

	ResponseWrapper<BellNotificationDto> getbellClickdttimes(String idaToken);

	int updatebellClickdttimes(String idaToken) throws ApisResourceAccessException, ResidentServiceCheckedException;

	ResponseWrapper<PageDto<ServiceHistoryResponseDto>> getNotificationList(Integer pageStart, Integer pageFetch, String Id, String languageCode, int timeZoneOffset) throws ResidentServiceCheckedException, ApisResourceAccessException;
	
	byte[] downLoadServiceHistory(ResponseWrapper<PageDto<ServiceHistoryResponseDto>> responseWrapper,
								  String languageCode, LocalDateTime eventReqDateTime, LocalDate fromDateTime, LocalDate toDateTime,
								  String serviceType, String statusFilter, int timeZoneOffset) throws ResidentServiceCheckedException, IOException;

	public ResponseWrapper<UserInfoDto> getUserinfo(String Id, int timeZoneOffset) throws ApisResourceAccessException;

	public String getFileName(String eventId, int timeZoneOffset);

}

