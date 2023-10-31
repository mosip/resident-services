package io.mosip.resident.controller;

import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_DESCRIPTION;
import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_ID;
import static io.mosip.resident.constant.ResidentConstants.RESIDENT_SERVICE_HISTORY_DOWNLOAD_MAX_COUNT;
import static io.mosip.resident.constant.ResidentConstants.RESIDENT_VIEW_HISTORY_DEFAULT_PAGE_SIZE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.validation.Valid;

import io.mosip.resident.dto.IdResponseDTO1;
import io.mosip.resident.util.Utilities;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;

import io.micrometer.core.annotation.Timed;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.AidStatusRequestDTO;
import io.mosip.resident.dto.AidStatusResponseDTO;
import io.mosip.resident.dto.AuthHistoryRequestDTO;
import io.mosip.resident.dto.AuthHistoryResponseDTO;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDto;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDtoV2;
import io.mosip.resident.dto.AuthUnLockRequestDTO;
import io.mosip.resident.dto.BellNotificationDto;
import io.mosip.resident.dto.EuinRequestDTO;
import io.mosip.resident.dto.EventStatusResponseDTO;
import io.mosip.resident.dto.PageDto;
import io.mosip.resident.dto.RegStatusCheckResponseDTO;
import io.mosip.resident.dto.RequestDTO;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentDemographicUpdateRequestDTO;
import io.mosip.resident.dto.ResidentReprintRequestDto;
import io.mosip.resident.dto.ResidentReprintResponseDto;
import io.mosip.resident.dto.ResidentUpdateRequestDto;
import io.mosip.resident.dto.ResponseDTO;
import io.mosip.resident.dto.ServiceHistoryResponseDto;
import io.mosip.resident.dto.UnreadNotificationDto;
import io.mosip.resident.dto.UserInfoDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.CardNotReadyException;
import io.mosip.resident.exception.EventIdNotPresentException;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.InvalidRequestTypeCodeException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.exception.RIDInvalidException;
import io.mosip.resident.service.ResidentService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.Utility;
import io.mosip.resident.validator.RequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

@RestController
@Tag(name = "resident-controller", description = "Resident Controller")
public class ResidentController {

	@Autowired
	private ResidentService residentService;

	@Autowired
	private RequestValidator validator;

	@Autowired
	private AuditUtil audit;

	@Autowired
	private IdentityServiceImpl identityServiceImpl;
	
	@Autowired
	private Utility utility;

	@Autowired
	private Environment environment;

	@Autowired
	private Utilities utilities;

	@Value("${resident.authLockStatusUpdateV2.id}")
	private String authLockStatusUpdateV2Id;

	@Value("${resident.authLockStatusUpdateV2.version}")
	private String authLockStatusUpdateV2Version;
	
	@Value("${resident.download.card.eventid.id}")
	private String downloadCardEventidId;
	
	@Value("${resident.vid.version.new}")
	private String newVersion;
	
	@Value("${resident.checkstatus.id}")
	private String checkStatusId;
	
	@Value("${resident.service.history.id}")
	private String serviceHistoryId;
	
	@Value("${resident.service.event.id}")
	private String serviceEventId;
	
	@Value("${" + RESIDENT_SERVICE_HISTORY_DOWNLOAD_MAX_COUNT + "}")
	private Integer maxEventsServiceHistoryPageSize;
	
	private static final Logger logger = LoggerConfiguration.logConfig(ResidentController.class);

	@ResponseFilter
	@PostMapping(value = "/rid/check-status")
	@Operation(summary = "getRidStatus", description = "getRidStatus", tags = { "resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<RegStatusCheckResponseDTO> getRidStatus(
			@Valid @RequestBody RequestWrapper<RequestDTO> requestDTO) throws ApisResourceAccessException {
		logger.debug("ResidentController::getRidStatus()::entry");
		ResponseWrapper<RegStatusCheckResponseDTO> response = new ResponseWrapper<>();
		try {
			validator.validateRidCheckStatusRequestDTO(requestDTO);
			logger.debug("ResidentController::Request for checking RID status");
			response.setResponse(residentService.getRidStatus(requestDTO.getRequest()));
		} catch (InvalidInputException | ApisResourceAccessException | ResidentServiceException | RIDInvalidException e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.RID_STATUS_FAILURE, e.getMessage()));
			e.setMetadata(Map.of(ResidentConstants.REQ_RES_ID, checkStatusId));
			throw e;
		}
		audit.setAuditRequestDto(EventEnum.RID_STATUS_SUCCESS);
		logger.debug("ResidentController::getRidStatus()::exit");
		return response;
	}

	@Deprecated
	@PostMapping(value = "/req/euin")
	@Operation(summary = "reqEuin", description = "reqEuin", tags = { "resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<Object> reqEuin(@Valid @RequestBody RequestWrapper<EuinRequestDTO> requestDTO)
			throws ResidentServiceCheckedException {
		logger.debug("ResidentController::reqEuin()::entry");
		validator.validateEuinRequest(requestDTO);
		logger.debug(String.format("ResidentController::Requesting euin for transaction id: %s", requestDTO.getRequest().getTransactionID()));
		byte[] pdfbytes = residentService.reqEuin(requestDTO.getRequest());
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_EUIN_SUCCESS,
				requestDTO.getRequest().getTransactionID()));
		InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(pdfbytes));
		logger.debug("ResidentController::reqEuin()::exit");
		return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/pdf"))
				.header("Content-Disposition",
						"attachment; filename=\"" + requestDTO.getRequest().getIndividualId() + ".pdf\"")
				.body((Object) resource);
	}

	@Deprecated
	@ResponseFilter
	@PostMapping(value = "/req/print-uin")
	@Operation(summary = "reqPrintUin", description = "reqPrintUin", tags = { "resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<Object> reqPrintUin(@Valid @RequestBody RequestWrapper<ResidentReprintRequestDto> requestDTO)
			throws ResidentServiceCheckedException {
		logger.debug("ResidentController::reqPrintUin()::entry");
		validator.validateReprintRequest(requestDTO);
		logger.debug(String.format("ResidentController::Requesting print uin for transaction id: %s", requestDTO.getRequest().getTransactionID()));
		ResponseWrapper<ResidentReprintResponseDto> response = new ResponseWrapper<>();
		response.setResponse(residentService.reqPrintUin(requestDTO.getRequest()));
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_PRINTUIN_SUCCESS,
				requestDTO.getRequest().getTransactionID()));
		logger.debug("ResidentController::reqPrintUin()::exit");
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@Deprecated
	@ResponseFilter
	@PostMapping(value = "/req/auth-lock")
	@Operation(summary = "reqAauthLock", description = "reqAauthLock", tags = { "resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<ResponseDTO> reqAauthLock(
			@Valid @RequestBody RequestWrapper<AuthLockOrUnLockRequestDto> requestDTO)
			throws ResidentServiceCheckedException {
		logger.debug("ResidentController::reqAauthLock()::entry");
		validator.validateAuthLockOrUnlockRequest(requestDTO, AuthTypeStatus.LOCK);
		logger.debug(String.format("ResidentController::Requesting auth lock for transaction id: %s", requestDTO.getRequest().getTransactionID()));
		ResponseWrapper<ResponseDTO> response = new ResponseWrapper<>();
		response.setResponse(residentService.reqAauthTypeStatusUpdate(requestDTO.getRequest(), AuthTypeStatus.LOCK));
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_LOCK_SUCCESS,
				requestDTO.getRequest().getTransactionID()));
		logger.debug("ResidentController::reqAauthLock()::exit");
		return response;
	}

	@Deprecated
	@ResponseFilter
	@PostMapping(value = "/req/auth-unlock")
	@Operation(summary = "reqAuthUnlock", description = "reqAuthUnlock", tags = { "resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<ResponseDTO> reqAuthUnlock(
			@Valid @RequestBody RequestWrapper<AuthUnLockRequestDTO> requestDTO)
			throws ResidentServiceCheckedException {
		logger.debug("ResidentController::reqAuthUnlock()::entry");
		validator.validateAuthUnlockRequest(requestDTO, AuthTypeStatus.UNLOCK);
		logger.debug(String.format("ResidentController::Requesting auth unlock for transaction id: %s", requestDTO.getRequest().getTransactionID()));
		ResponseWrapper<ResponseDTO> response = new ResponseWrapper<>();
		response.setResponse(residentService.reqAauthTypeStatusUpdate(requestDTO.getRequest(), AuthTypeStatus.UNLOCK));
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_UNLOCK_SUCCESS,
				requestDTO.getRequest().getTransactionID()));
		logger.debug("ResidentController::reqAuthUnlock()::exit");
		return response;
	}

	@PreAuthorize("@scopeValidator.hasAllScopes(" + "@authorizedScopes.getPostAuthTypeStatus()" + ")")
	@ResponseFilter
	@PostMapping(value = "/auth-lock-unlock")
	@Operation(summary = "reqAuthTypeStatus", description = "reqAuthTypeStatus", tags = { "resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<Object> reqAauthTypeStatusUpdateV2(
			@Valid @RequestBody RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestDTO)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		logger.debug("ResidentController::reqAauthTypeStatusUpdateV2()::entry");
		ResponseWrapper<ResponseDTO> response = new ResponseWrapper<>();
		Tuple2<ResponseDTO, String> tupleResponse = null;
		try {
			validator.validateAuthLockOrUnlockRequestV2(requestDTO);
			logger.debug("ResidentController::Requesting auth lock/unlock api");
			tupleResponse = residentService.reqAauthTypeStatusUpdateV2(requestDTO.getRequest());
			response.setResponse(tupleResponse.getT1());
			response.setId(authLockStatusUpdateV2Id);
			response.setVersion(authLockStatusUpdateV2Version);
			audit.setAuditRequestDto(EventEnum.REQ_AUTH_LOCK_UNLOCK_SUCCESS);
		} catch (InvalidInputException | ResidentServiceCheckedException e) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.REQUEST_FAILED, "Request for auth lock failed"));
			e.setMetadata(Map.of(ResidentConstants.REQ_RES_ID, authLockStatusUpdateV2Id));
			throw e;
		}
		logger.debug("ResidentController::reqAauthTypeStatusUpdateV2()::exit");
		return ResponseEntity.ok()
				.header(ResidentConstants.EVENT_ID, tupleResponse.getT2())
				.body(response);
		
	}

	@ResponseFilter
	@PostMapping(value = "/req/auth-history")
	@Operation(summary = "reqAuthHistory", description = "reqAuthHistory", tags = { "resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<AuthHistoryResponseDTO> reqAuthHistory(
			@Valid @RequestBody RequestWrapper<AuthHistoryRequestDTO> requestDTO)
			throws ResidentServiceCheckedException {
		logger.debug("ResidentController::reqAuthHistory()::entry");
		ResponseWrapper<AuthHistoryResponseDTO> response = new ResponseWrapper<>();
		try {
			validator.validateAuthHistoryRequest(requestDTO);
			logger.debug(String.format("ResidentController::Requesting auth history for transaction id %s", requestDTO.getRequest().getTransactionID()));
			response = new ResponseWrapper<>();
			response.setResponse(residentService.reqAuthHistory(requestDTO.getRequest()));
		} catch (InvalidInputException | ResidentServiceCheckedException e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQUEST_FAILED,
					requestDTO.getRequest().getTransactionID(), "Request for auth history"));
			e.setMetadata(Map.of(ResidentConstants.REQ_RES_ID, ResidentConstants.AUTH_HISTORY_ID));
			throw e;
		}
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_HISTORY_SUCCESS,
				requestDTO.getRequest().getTransactionID()));
		logger.debug("ResidentController::reqAuthHistory()::exit");
		return response;
	}

	@GetMapping(path = "/events/{event-id}")
	@Operation(summary = "checkEventIdStatus", description = "checkEventIdStatus", tags = {
			"resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<EventStatusResponseDTO> checkEventIdStatus(@PathVariable(name = "event-id") String eventId,
			@RequestParam(name = "langCode") String languageCode,
			@RequestHeader(name = "time-zone-offset", required = false, defaultValue = "0") int timeZoneOffset,
            @RequestHeader(name = "locale", required = false) String locale) throws ResidentServiceCheckedException {
		logger.debug("ResidentController::checkEventIdStatus()::entry");
		ResponseWrapper<EventStatusResponseDTO> responseWrapper = new ResponseWrapper<>();
		try {
			validator.validateEventIdLanguageCode(eventId, languageCode);
			logger.debug(String.format("ResidentController::Requesting application status for event id: %s", eventId));
			responseWrapper = residentService.getEventStatus(eventId, languageCode, timeZoneOffset, locale);
		} catch (InvalidInputException | ResidentServiceCheckedException e) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.CHECK_AID_STATUS_REQUEST_FAILED, eventId));
			e.setMetadata(Map.of(ResidentConstants.REQ_RES_ID, ResidentConstants.EVENTS_EVENTID_ID));
			throw e;
		}
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.CHECK_AID_STATUS_REQUEST_SUCCESS, eventId));
		logger.debug("ResidentController::checkEventIdStatus()::exit");
		return responseWrapper;
	}

	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
	@PreAuthorize("@scopeValidator.hasAllScopes(" + "@authorizedScopes.getGetServiceAuthHistoryRoles()" + ")")
	@GetMapping(path = "/service-history/{langCode}")
	@Operation(summary = "getServiceHistory", description = "getServiceHistory", tags = { "resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<PageDto<ServiceHistoryResponseDto>> getServiceHistory(@PathVariable("langCode") String langCode,
			@RequestParam(name = "pageIndex", required = false) Integer pageIndex,
			@RequestParam(name = "pageSize", required = false) Integer pageSize,
			@RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			@RequestParam(name = "sortType", required = false) String sortType,
			@RequestParam(name = "serviceType", required = false) String serviceType,
			@RequestParam(name = "statusFilter", required = false) String statusFilter,
			@RequestParam(name = "searchText", required = false) String searchText,
			@RequestHeader(name = "time-zone-offset", required = false, defaultValue = "0") int timeZoneOffset,
            @RequestHeader(name = "locale", required = false) String locale)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		logger.debug("ResidentController::getServiceHistory()::entry");
		logger.info("TimeZone-offset: " + timeZoneOffset);
		ResponseWrapper<PageDto<ServiceHistoryResponseDto>> responseWrapper = new ResponseWrapper<>();
		try {
			validator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType, statusFilter, langCode, searchText);
			responseWrapper = residentService.getServiceHistory(pageIndex, pageSize, fromDate, toDate, serviceType,
					sortType, statusFilter, searchText, langCode, timeZoneOffset, locale,
					RESIDENT_VIEW_HISTORY_DEFAULT_PAGE_SIZE, null);
		} catch (InvalidInputException | ResidentServiceCheckedException e) {
			audit.setAuditRequestDto(EventEnum.GET_SERVICE_HISTORY_FAILURE);
			e.setMetadata(Map.of(ResidentConstants.REQ_RES_ID, serviceHistoryId));
			throw e;
		}
		audit.setAuditRequestDto(EventEnum.GET_SERVICE_HISTORY_SUCCESS);
		logger.debug("ResidentController::getServiceHistory()::exit");
		return responseWrapper;
	}	

	@Deprecated
	@ResponseFilter
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PostMapping(value = "/req/update-uin")
	@Operation(summary = "updateUin", description = "updateUin", tags = { "resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<Object> updateUin(
			@Valid @RequestBody RequestWrapper<ResidentUpdateRequestDto> requestDTO)
			throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
		logger.debug("ResidentController::updateUin()::exit");
		validator.validateUpdateRequest(requestDTO, false, null);
		ResponseWrapper<Object> response = new ResponseWrapper<>();
		logger.debug(String.format("ResidentController::Requesting update uin api for transaction id %s", requestDTO.getRequest().getTransactionID()));
		response.setResponse(residentService.reqUinUpdate(requestDTO.getRequest()).getT1());
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.UPDATE_UIN_SUCCESS,
				requestDTO.getRequest().getTransactionID()));
		logger.debug("ResidentController::updateUin()::exit");
		return response;
	}

	/**
	 * This function is used to update the UIN of a resident
	 * 
	 * @param requestDTO The request object that is passed to the API.
	 * @return ResponseWrapper<ResidentUpdateResponseDTO>
	 * @throws ApisResourceAccessException
	 */
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PreAuthorize("@scopeValidator.hasAllScopes(" + "@authorizedScopes.getPatchUpdateUin()" + ")")
	@ResponseFilter
	@PatchMapping(value = "/update-uin")
	@Operation(summary = "updateUin", description = "updateUin", tags = { "resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<Object> updateUinDemographics(
			@Valid @RequestBody RequestWrapper<ResidentDemographicUpdateRequestDTO> requestDTO)
			throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
		logger.debug("ResidentController::updateUinDemographics()::entry");
		RequestWrapper<ResidentUpdateRequestDto> requestWrapper = JsonUtil.convertValue(requestDTO,
				new TypeReference<RequestWrapper<ResidentUpdateRequestDto>>() {
				});
		String individualId = identityServiceImpl.getResidentIndvidualIdFromSession();
		ResponseWrapper<Object> response = new ResponseWrapper<>();
		Tuple2<Object, String> tupleResponse = null;
		ResidentUpdateRequestDto request = requestWrapper.getRequest();
		if (request != null) {
			request.setIndividualId(individualId);
			request.setIndividualIdType(getIdType(individualId));
		}
		try {
			Tuple3<JSONObject, String, IdResponseDTO1> identityData = utilities.
					getIdentityDataFromIndividualID(individualId);
			JSONObject idRepoJson = identityData.getT1();
			String schemaJson = identityData.getT2();
			validator.validateUpdateRequest(requestWrapper, true, schemaJson);
			logger.debug(String.format("ResidentController::Requesting update uin api for transaction id %s", requestDTO.getRequest().getTransactionID()));
			requestDTO.getRequest().getIdentity().put(IdType.UIN.name(),
					idRepoJson.get(IdType.UIN.name()));
			tupleResponse = residentService.reqUinUpdate(request, requestDTO.getRequest().getIdentity(), true,
					idRepoJson, schemaJson, identityData.getT3());
			response.setId(requestDTO.getId());
			response.setVersion(requestDTO.getVersion());
			response.setResponse(tupleResponse.getT1());
		} catch (InvalidInputException | ResidentServiceCheckedException e) {
			audit.setAuditRequestDto(EventEnum.UPDATE_UIN_FAILURE);
			e.setMetadata(Map.of(ResidentConstants.REQ_RES_ID, ResidentConstants.UPDATE_UIN_ID));
			throw e;
		}
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.UPDATE_UIN_SUCCESS,
				requestDTO.getRequest().getTransactionID()));
		logger.debug("ResidentController::updateUinDemographics()::exit");
		return ResponseEntity.ok().header(ResidentConstants.EVENT_ID, tupleResponse.getT2()).body(response);
	}

	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PreAuthorize("@scopeValidator.hasAllScopes(" + "@authorizedScopes.getGetAuthLockStatus()" + ")")
	@GetMapping(path = "/auth-lock-status")
	public ResponseWrapper<AuthLockOrUnLockRequestDtoV2> getAuthLockStatus() throws ApisResourceAccessException {
		logger.debug("ResidentController::getAuthLockStatus()::entry");
		ResponseWrapper<AuthLockOrUnLockRequestDtoV2> responseWrapper = new ResponseWrapper<>();
		String individualId = identityServiceImpl.getResidentIndvidualIdFromSession();
		try {
			responseWrapper = residentService.getAuthLockStatus(individualId);
			audit.setAuditRequestDto(EventEnum.REQ_AUTH_LOCK_STATUS_SUCCESS);
			return responseWrapper;
		} catch (ResidentServiceCheckedException e) {
			audit.setAuditRequestDto(EventEnum.REQ_AUTH_LOCK_STATUS_FAILED);
			responseWrapper.setErrors(List.of(new ServiceError(ResidentErrorCode.AUTH_LOCK_STATUS_FAILED.getErrorCode(),
					ResidentErrorCode.AUTH_LOCK_STATUS_FAILED.getErrorMessage())));
		}
		logger.debug("ResidentController::getAuthLockStatus()::exit");
		return responseWrapper;
	}

	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PreAuthorize("@scopeValidator.hasAllScopes(" + "@authorizedScopes.getGetDownloadCard()" + ")")
	@GetMapping(path = "/download-card/event/{eventId}")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Card successfully downloaded", content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
			@ApiResponse(responseCode = "400", description = "Download card failed", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<Object> downloadCard(
			@PathVariable("eventId") String eventId,
			@RequestHeader(name = "time-zone-offset", required = false, defaultValue = "0") int timeZoneOffset,
            @RequestHeader(name = "locale", required = false) String locale) throws ResidentServiceCheckedException {
		logger.debug("ResidentController::downloadCard()::entry");
		InputStreamResource resource = null;
		Tuple2<byte[], IdType> pdfBytesAndCardType;
		try {
			validator.validateEventId(eventId);
			logger.debug(String.format("ResidentController::Requesting download digital card for event id: %s", eventId));
			pdfBytesAndCardType = residentService.downloadCard(eventId);
			resource = new InputStreamResource(new ByteArrayInputStream(pdfBytesAndCardType.getT1()));
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.RID_DIGITAL_CARD_REQ_SUCCESS, eventId));
		} catch (CardNotReadyException | ResidentServiceException | EventIdNotPresentException
				| InvalidRequestTypeCodeException | InvalidInputException e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.RID_DIGITAL_CARD_REQ_FAILURE, eventId));
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), ExceptionUtils.getStackTrace(e));
			throw new ResidentServiceException(e.getErrorCode(), e.getErrorText(), e,
					Map.of(ResidentConstants.HTTP_STATUS_CODE, HttpStatus.BAD_REQUEST, ResidentConstants.REQ_RES_ID,
							downloadCardEventidId));
		}
		logger.debug("ResidentController::downloadCard()::exit");
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
				.header("Content-Disposition", "attachment; filename=\"" + residentService.getFileName(eventId, pdfBytesAndCardType.getT2(), timeZoneOffset, locale) + ".pdf\"")
				.header(ResidentConstants.EVENT_ID, eventId)
				.body(resource);
	}

	/**
	 * It returns the type of the ID passed to it
	 * 
	 * @param id The ID of the resident.
	 * @return The method is returning the type of ID.
	 */
	private String getIdType(String id) {
		if (validator.validateUin(id))
			return "UIN";
		if (validator.validateVid(id))
			return "VID";
		return "RID";
	}

	@ResponseFilter
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PostMapping("/aid/status")
	@Operation(summary = "checkAidStatus", description = "Get AID Status", tags = { "resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<AidStatusResponseDTO> checkAidStatus(@RequestBody RequestWrapper<AidStatusRequestDTO> reqDto)
			throws ResidentServiceCheckedException, ApisResourceAccessException, OtpValidationFailedException {
		logger.debug("ResidentController::getAidStatus()::entry");
		AidStatusResponseDTO resp = new AidStatusResponseDTO();
		try {
			validator.validateAidStatusRequestDto(reqDto);
			logger.debug("ResidentController::Request for checking AID status");
			resp = residentService.getAidStatus(reqDto.getRequest(), true);
		} catch (ResidentServiceCheckedException | ApisResourceAccessException | OtpValidationFailedException e) {
			audit.setAuditRequestDto(EventEnum.AID_STATUS_FAILURE);
			throw new ResidentServiceException(e.getErrorCode(), e.getErrorText(), e,
					Map.of(ResidentConstants.REQ_RES_ID, checkStatusId));
		}
		audit.setAuditRequestDto(EventEnum.AID_STATUS_SUCCESS);
		logger.debug("ResidentController::getAidStatus()::exit");
		ResponseWrapper<AidStatusResponseDTO> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(resp);
		responseWrapper.setId(checkStatusId);
		responseWrapper.setVersion(newVersion);
		return responseWrapper;
	}

	@ResponseFilter
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PreAuthorize("@scopeValidator.hasAllScopes(" + "@authorizedScopes.getGetNotificationCount()" + ")")
	@GetMapping("/unread/notification-count")
	@Operation(summary = "unreadnotification-count", description = "Get notification count", tags = {
			"resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<UnreadNotificationDto> notificationCount()
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<UnreadNotificationDto> count = new ResponseWrapper<>();
		logger.debug("ResidentController::notificationCount()::entry");
		String individualId = identityServiceImpl.getResidentIdaToken();
		try {
			count = residentService.getnotificationCount(individualId);
		} catch (ResidentServiceCheckedException e) {
			audit.setAuditRequestDto(EventEnum.UNREAD_NOTIF_COUNT_FAILURE);
			e.setMetadata(Map.of(ResidentConstants.REQ_RES_ID, serviceEventId));
			throw e;
		}
		audit.setAuditRequestDto(EventEnum.UNREAD_NOTIF_COUNT_SUCCESS);
		logger.debug("ResidentController::notificationCount()::exit");
		return count;
	}

	@ResponseFilter
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PreAuthorize("@scopeValidator.hasAllScopes(" + "@authorizedScopes.getGetNotificationClick()" + ")")
	@GetMapping("/bell/notification-click")
	@Operation(summary = "checkLastClickdttimes", description = "Get notification-clickdttimes", tags = {
			"resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<BellNotificationDto> bellClickdttimes()
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		logger.debug("ResidentController::bellClickdttimes()::entry");
		String idaToken = null;
		ResponseWrapper<BellNotificationDto> response = new ResponseWrapper<>();
		try {
			idaToken = identityServiceImpl.getResidentIdaToken();
			response = residentService.getbellClickdttimes(idaToken);
		} catch (ResidentServiceCheckedException | ApisResourceAccessException e) {
			audit.setAuditRequestDto(EventEnum.GET_NOTIF_CLICK_FAILURE);
			e.setMetadata(Map.of(ResidentConstants.REQ_RES_ID, ResidentConstants.NOTIFICATION_CLICK_ID));
			throw e;
		}
		audit.setAuditRequestDto(EventEnum.GET_NOTIF_CLICK_SUCCESS);
		logger.debug("ResidentController::bellClickdttimes::exit");
		return response;
	}

	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PreAuthorize("@scopeValidator.hasAllScopes(" + "@authorizedScopes.getGetupdatedttimes()" + ")")
	@PutMapping(path = "/bell/updatedttime")
	@Operation(summary = "updatebellClickdttimes", description = "updatedttimes")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))), })
	public int bellupdateClickdttimes() throws ResidentServiceCheckedException, ApisResourceAccessException {
		logger.debug("ResidentController::bellupdateClickdttimes()::entry");
		String idaToken = identityServiceImpl.getResidentIdaToken();
		int response = residentService.updatebellClickdttimes(idaToken);
		logger.debug("ResidentController::bellupdateClickdttimes()::exit");
		return response;
	}

	@ResponseFilter
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PreAuthorize("@scopeValidator.hasAllScopes(" + "@authorizedScopes.getGetUnreadServiceList()" + ")")
	@GetMapping("/notifications/{langCode}")
	@Operation(summary = "get", description = "Get unread-service-list", tags = { "resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<?> getNotificationsList(@PathVariable("langCode") String langCode,
			@RequestParam(name = "pageIndex", required = false) Integer pageIndex,
			@RequestParam(name = "pageSize", required = false) Integer pageSize,
			@RequestHeader(name = "time-zone-offset", required = false, defaultValue = "0") int timeZoneOffset,
            @RequestHeader(name = "locale", required = false) String locale)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		logger.debug("ResidentController::getNotificationsList()::entry");
		String id = null;
		ResponseWrapper<PageDto<ServiceHistoryResponseDto>> notificationDtoList = new ResponseWrapper<>();
		try {
			validator.validateLanguageCode(langCode);
			id = identityServiceImpl.getResidentIdaToken();
			notificationDtoList = residentService.getNotificationList(pageIndex, pageSize, id, langCode,
					timeZoneOffset, locale);
		} catch (ResidentServiceCheckedException | ApisResourceAccessException | InvalidInputException e) {
			audit.setAuditRequestDto(EventEnum.GET_NOTIFICATION_FAILURE);
			e.setMetadata(Map.of(ResidentConstants.REQ_RES_ID, ResidentConstants.NOTIFICATION_ID));
			throw e;
		}
		audit.setAuditRequestDto(EventEnum.GET_NOTIFICATION_SUCCESS);
		logger.debug("ResidentController::getNotificationsList()::exit");
		return notificationDtoList;
	}

	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @GetMapping(path = "/download/service-history")
	public ResponseEntity<Object> downLoadServiceHistory(
			@RequestParam(name = "eventReqDateTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime eventReqDateTime,
			@RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			@RequestParam(name = "sortType", required = false) String sortType,
			@RequestParam(name = "serviceType", required = false) String serviceType,
			@RequestParam(name = "statusFilter", required = false) String statusFilter,
			@RequestParam(name = "searchText", required = false) String searchText,
			@RequestParam(name = "languageCode", required = true) String languageCode,
			@RequestHeader(name = "time-zone-offset", required = false, defaultValue = "0") int timeZoneOffset,
            @RequestHeader(name = "locale", required = false) String locale)
			throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
		logger.debug("ResidentController::downLoadServiceHistory::entry");
		InputStreamResource resource = null;
		try {
			validator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType, statusFilter, languageCode, searchText);
			ResponseWrapper<PageDto<ServiceHistoryResponseDto>> responseWrapper = residentService.getServiceHistory(
					null, maxEventsServiceHistoryPageSize, fromDate, toDate, serviceType, sortType, statusFilter,
					searchText, languageCode, timeZoneOffset, locale);
			logger.debug("after response wrapper size of   " + responseWrapper.getResponse().getData().size());
			byte[] pdfBytes = residentService.downLoadServiceHistory(responseWrapper, languageCode, eventReqDateTime,
					fromDate, toDate, serviceType, statusFilter, timeZoneOffset, locale);
			resource = new InputStreamResource(new ByteArrayInputStream(pdfBytes));
		} catch (ResidentServiceCheckedException | ApisResourceAccessException | InvalidInputException e) {
			audit.setAuditRequestDto(EventEnum.DOWNLOAD_SERVICE_HISTORY_FAILURE);
			e.setMetadata(Map.of(ResidentConstants.REQ_RES_ID, ResidentConstants.SERVICE_HISTORY_ID));
			throw e;
		}
		audit.setAuditRequestDto(EventEnum.DOWNLOAD_SERVICE_HISTORY_SUCCESS);
		logger.debug("ResidentController::downLoadServiceHistory::exit");
		return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/pdf"))
				.header("Content-Disposition",
						"attachment; filename=\"" + utility.getFileName(null,
								Objects.requireNonNull(this.environment.getProperty(
										ResidentConstants.DOWNLOAD_SERVICE_HISTORY_FILE_NAME_CONVENTION_PROPERTY)),
								timeZoneOffset, locale) + ".pdf\"")
				.body(resource);
	}
	
	@ResponseFilter
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @GetMapping("/profile")
	@Operation(summary = "get", description = "Get unread-service-list", tags = { "resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })

	public ResponseWrapper<UserInfoDto> userinfo(@RequestParam(name = "languageCode", required = false) String languageCode,
			@RequestHeader(name = "time-zone-offset", required = false, defaultValue = "0") int timeZoneOffset,
            @RequestHeader(name = "locale", required = false) String locale)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		logger.debug("ResidentController::getuserinfo()::entry");
		ResponseWrapper<UserInfoDto> userInfoDto = new ResponseWrapper<>();
		try {
			validator.validateProfileApiRequest(languageCode);
			String idaToken = identityServiceImpl.getResidentIdaToken();
			userInfoDto = residentService.getUserinfo(idaToken, languageCode, timeZoneOffset, locale);
		} catch (ResidentServiceCheckedException | ApisResourceAccessException | InvalidInputException e) {
			audit.setAuditRequestDto(EventEnum.GET_PROFILE_FAILURE);
			e.setMetadata(Map.of(ResidentConstants.REQ_RES_ID, ResidentConstants.PROFILE_ID));
			throw e;
		}
		audit.setAuditRequestDto(EventEnum.GET_PROFILE_SUCCESS);
		logger.debug("ResidentController::getuserinfo()::exit");
		return userInfoDto;
	}
	
}
