package io.mosip.resident.controller;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.AuthTypeStatus;
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
import io.mosip.resident.dto.ResidentServiceHistoryResponseDto;
import io.mosip.resident.dto.ResidentUpdateRequestDto;
import io.mosip.resident.dto.ResidentUpdateResponseDTO;
import io.mosip.resident.dto.ResponseDTO;
import io.mosip.resident.dto.ServiceHistoryResponseDto;
import io.mosip.resident.dto.UnreadNotificationDto;
import io.mosip.resident.dto.UnreadServiceNotificationDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ResidentService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.validator.RequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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

	@Value("${resident.authLockStatusUpdateV2.id}")
	private String authLockStatusUpdateV2Id;

	@Value("${resident.authLockStatusUpdateV2.version}")
	private String authLockStatusUpdateV2Version;

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
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "get Rid status API"));
		validator.validateRidCheckStatusRequestDTO(requestDTO);
		ResponseWrapper<RegStatusCheckResponseDTO> response = new ResponseWrapper<>();
		audit.setAuditRequestDto(EventEnum.RID_STATUS);
		response.setResponse(residentService.getRidStatus(requestDTO.getRequest()));
		audit.setAuditRequestDto(EventEnum.RID_STATUS_SUCCESS);
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
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "request Euin API"));
		validator.validateEuinRequest(requestDTO);
		audit.setAuditRequestDto(
				EventEnum.getEventEnumWithValue(EventEnum.REQ_EUIN, requestDTO.getRequest().getTransactionID()));
		byte[] pdfbytes = residentService.reqEuin(requestDTO.getRequest());
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_EUIN_SUCCESS,
				requestDTO.getRequest().getTransactionID()));
		InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(pdfbytes));

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
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "request print Uin API"));
		validator.validateReprintRequest(requestDTO);
		audit.setAuditRequestDto(
				EventEnum.getEventEnumWithValue(EventEnum.REQ_PRINTUIN, requestDTO.getRequest().getTransactionID()));
		ResponseWrapper<ResidentReprintResponseDto> response = new ResponseWrapper<>();
		response.setResponse(residentService.reqPrintUin(requestDTO.getRequest()));
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_PRINTUIN_SUCCESS,
				requestDTO.getRequest().getTransactionID()));
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
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "request auth lock API"));
		validator.validateAuthLockOrUnlockRequest(requestDTO, AuthTypeStatus.LOCK);
		audit.setAuditRequestDto(
				EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_LOCK, requestDTO.getRequest().getTransactionID()));
		ResponseWrapper<ResponseDTO> response = new ResponseWrapper<>();
		response.setResponse(residentService.reqAauthTypeStatusUpdate(requestDTO.getRequest(), AuthTypeStatus.LOCK));
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_LOCK_SUCCESS,
				requestDTO.getRequest().getTransactionID()));
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
		audit.setAuditRequestDto(
				EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "request auth unlock  API"));
		validator.validateAuthUnlockRequest(requestDTO, AuthTypeStatus.UNLOCK);
		audit.setAuditRequestDto(
				EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_UNLOCK, requestDTO.getRequest().getTransactionID()));
		ResponseWrapper<ResponseDTO> response = new ResponseWrapper<>();
		response.setResponse(residentService.reqAauthTypeStatusUpdate(requestDTO.getRequest(), AuthTypeStatus.UNLOCK));
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_UNLOCK_SUCCESS,
				requestDTO.getRequest().getTransactionID()));
		return response;
	}

	@PreAuthorize("@scopeValidator.hasAllScopes(" + "@authorizedScopes.getPostAuthTypeStatus()" + ")")
	@ResponseFilter
	@PostMapping(value = "/req/auth-type-status")
	@Operation(summary = "reqAuthTypeStatus", description = "reqAuthTypeStatus", tags = { "resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<ResponseDTO> reqAauthTypeStatusUpdateV2(
			@Valid @RequestBody RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestDTO)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		audit.setAuditRequestDto(
				EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "update auth Type status API"));
		String individualId = identityServiceImpl.getResidentIndvidualId();
		validator.validateAuthLockOrUnlockRequestV2(requestDTO);
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_LOCK, individualId));
		ResponseWrapper<ResponseDTO> response = new ResponseWrapper<>();
		response.setResponse(residentService.reqAauthTypeStatusUpdateV2(requestDTO.getRequest()));
		response.setId(authLockStatusUpdateV2Id);
		response.setVersion(authLockStatusUpdateV2Version);
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_LOCK_SUCCESS, individualId));
		return response;
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
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "request auth history"));
		validator.validateAuthHistoryRequest(requestDTO);
		ResponseWrapper<AuthHistoryResponseDTO> response = new ResponseWrapper<>();
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_HISTORY,
				requestDTO.getRequest().getTransactionID()));
		response.setResponse(residentService.reqAuthHistory(requestDTO.getRequest()));
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_HISTORY_SUCCESS,
				requestDTO.getRequest().getTransactionID()));
		return response;
	}

	@PreAuthorize("@scopeValidator.hasAllScopes(" + "@authorizedScopes.getGetEventIdStatus()" + ")")
	@GetMapping(path = "/events/{event-id}")
	@Operation(summary = "getGetCheckEventIdStatus", description = "checkEventIdStatus", tags = {
			"resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<EventStatusResponseDTO> checkAidStatus(@PathVariable(name = "event-id") String eventId,
			@RequestParam(name = "langCode") String languageCode) throws ResidentServiceCheckedException {
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "checkAidStatus"));
		logger.debug("checkAidStatus controller entry");
		validator.validateEventIdLanguageCode(eventId, languageCode);
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.CHECK_AID_STATUS_REQUEST, eventId));
		ResponseWrapper<EventStatusResponseDTO> responseWrapper = residentService.getEventStatus(eventId, languageCode);
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.CHECK_AID_STATUS_REQUEST_SUCCESS, eventId));
		return responseWrapper;
	}

	@PreAuthorize("@scopeValidator.hasAllScopes(" + "@authorizedScopes.getGetServiceAuthHistoryRoles()" + ")")
	@GetMapping(path = "/service-history")
	@Operation(summary = "getServiceHistory", description = "getServiceHistory", tags = { "resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<PageDto<ServiceHistoryResponseDto>> getServiceHistory(
			@RequestParam(name = "pageStart", required = false) Integer pageStart,
			@RequestParam(name = "pageFetch", required = false) Integer pageFetch,
			@RequestParam(name = "fromDateTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDateTime,
			@RequestParam(name = "toDateTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDateTime,
			@RequestParam(name = "sortType", required = false) String sortType,
			@RequestParam(name = "serviceType", required = false) String serviceType,
			@RequestParam(name = "statusFilter", required = false) String statusFilter,
			@RequestParam(name = "searchText", required = false) String searchText)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "getServiceHistory"));
		validator.validateServiceHistoryRequest(fromDateTime, toDateTime, sortType, serviceType, statusFilter);
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.GET_SERVICE_HISTORY, "getServiceHistory"));
		ResponseWrapper<PageDto<ServiceHistoryResponseDto>> responseWrapper = residentService.getServiceHistory(
				pageStart, pageFetch, fromDateTime, toDateTime, serviceType, sortType, statusFilter, searchText);
		;
		return responseWrapper;
	}

	@PreAuthorize("@scopeValidator.hasAllScopes(" + "@authorizedScopes.getGetServiceRequestUpdate()" + ")")
	@GetMapping(path = "/get/service-request-update")
	@Operation(summary = "getServiceRequestUpdate", description = "getServiceRequestUpdate", tags = {
			"resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<List<ResidentServiceHistoryResponseDto>> getServiceRequestUpdate(
			@RequestParam(name = "pageStart", required = false) Integer pageStart,
			@RequestParam(name = "pageFetch", required = false) Integer pageFetch)
			throws ResidentServiceCheckedException {
		logger.info("getServiceRequestUpdate :: entry");
		ResponseWrapper<List<ResidentServiceHistoryResponseDto>> response = new ResponseWrapper<>();
		audit.setAuditRequestDto(
				EventEnum.getEventEnumWithValue(EventEnum.REQ_SERVICE_REQUEST_UPDATE, "Get Service request update"));
		List<ResidentServiceHistoryResponseDto> residentServiceHistoryResponseDtoList = residentService
				.getServiceRequestUpdate(pageStart, pageFetch);
		response.setResponse(residentServiceHistoryResponseDtoList);
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_SERVICE_REQUEST_UPDATE_SUCCESS,
				"Get Service request update"));
		return response;
	}

	@Deprecated
	@ResponseFilter
	@PostMapping(value = "/req/update-uin")
	@Operation(summary = "updateUin", description = "updateUin", tags = { "resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<ResidentUpdateResponseDTO> updateUin(
			@Valid @RequestBody RequestWrapper<ResidentUpdateRequestDto> requestDTO)
			throws ResidentServiceCheckedException {
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "update Uin API"));
		validator.validateUpdateRequest(requestDTO, false);
		ResponseWrapper<ResidentUpdateResponseDTO> response = new ResponseWrapper<>();
		audit.setAuditRequestDto(
				EventEnum.getEventEnumWithValue(EventEnum.UPDATE_UIN, requestDTO.getRequest().getTransactionID()));
		response.setResponse(residentService.reqUinUpdate(requestDTO.getRequest()));
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.UPDATE_UIN_SUCCESS,
				requestDTO.getRequest().getTransactionID()));
		return response;
	}

	/**
	 * This function is used to update the UIN of a resident
	 * 
	 * @param requestDTO The request object that is passed to the API.
	 * @return ResponseWrapper<ResidentUpdateResponseDTO>
	 * @throws ApisResourceAccessException
	 */
	@PreAuthorize("@scopeValidator.hasAllScopes(" + "@authorizedScopes.getPatchUpdateUin()" + ")")
	@ResponseFilter
	@PatchMapping(value = "/req/update-uin")
	@Operation(summary = "updateUin", description = "updateUin", tags = { "resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<ResidentUpdateResponseDTO> updateUinDemographics(
			@Valid @RequestBody RequestWrapper<ResidentDemographicUpdateRequestDTO> requestDTO)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "update UIN API"));
		RequestWrapper<ResidentUpdateRequestDto> requestWrapper = JsonUtil.convertValue(requestDTO,
				new TypeReference<RequestWrapper<ResidentUpdateRequestDto>>() {
				});
		String individualId = identityServiceImpl.getResidentIndvidualId();
		if (requestDTO.getRequest() != null) {
			requestDTO.getRequest().setIndividualId(individualId);
		}
		requestWrapper.getRequest().setIndividualIdType(getIdType(requestWrapper.getRequest().getIndividualId()));
		validator.validateUpdateRequest(requestWrapper, true);
		ResponseWrapper<ResidentUpdateResponseDTO> response = new ResponseWrapper<>();
		audit.setAuditRequestDto(
				EventEnum.getEventEnumWithValue(EventEnum.UPDATE_UIN, requestDTO.getRequest().getTransactionID()));
		response.setResponse(residentService.reqUinUpdate(requestWrapper.getRequest()));
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.UPDATE_UIN_SUCCESS,
				requestDTO.getRequest().getTransactionID()));
		return response;
	}

	@PreAuthorize("@scopeValidator.hasAllScopes(" + "@authorizedScopes.getGetAuthLockStatus()" + ")")
	@GetMapping(path = "/auth-lock-status")
	public ResponseWrapper<Object> getAuthLockStatus() throws ApisResourceAccessException {
		audit.setAuditRequestDto(
				EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "request auth lock status  API"));
		ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>();
		String individualId = identityServiceImpl.getResidentIndvidualId();
		try {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_LOCK_STATUS, individualId));
			responseWrapper = residentService.getAuthLockStatus(individualId);
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_LOCK_STATUS_SUCCESS, individualId));
			return responseWrapper;
		} catch (ResidentServiceCheckedException e) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_LOCK_STATUS_FAILED, individualId));
			responseWrapper.setErrors(List.of(new ServiceError(ResidentErrorCode.AUTH_LOCK_STATUS_FAILED.getErrorCode(),
					ResidentErrorCode.AUTH_LOCK_STATUS_FAILED.getErrorMessage())));
		}
		return responseWrapper;
	}

	@PreAuthorize("@scopeValidator.hasAllScopes(" + "@authorizedScopes.getGetDownloadCard()" + ")")
	@GetMapping(path = "/download-card/individualId/{individualId}")
	public ResponseWrapper<List<ResidentServiceHistoryResponseDto>> downloadCard(
			@PathVariable("individualId") String individualId) throws ResidentServiceCheckedException {
		audit.setAuditRequestDto(
				EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "request auth lock status  API"));
		validator.validateIndividualId(individualId);
		ResponseWrapper<List<ResidentServiceHistoryResponseDto>> response = new ResponseWrapper<>();
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.RID_DIGITAL_CARD_REQ, individualId));
		response.setResponse(residentService.downloadCard(individualId, getIdType(individualId)));
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.RID_DIGITAL_CARD_REQ_SUCCESS, individualId));
		return response;
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
	@PostMapping("/aid/get-individual-id")
	@Operation(summary = "checkAidStatus", description = "Get AID Status", tags = { "resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<AidStatusResponseDTO> checkAidStatus(@RequestBody RequestWrapper<AidStatusRequestDTO> reqDto)
			throws ResidentServiceCheckedException, ApisResourceAccessException, OtpValidationFailedException {
		logger.debug("ResidentController::getAidStatus()::entry");
		validator.validateAidStatusRequestDto(reqDto);
		audit.setAuditRequestDto(EventEnum.AID_STATUS);
		AidStatusResponseDTO resp = residentService.getAidStatus(reqDto.getRequest());
		audit.setAuditRequestDto(EventEnum.AID_STATUS_SUCCESS);
		logger.debug("ResidentController::getAidStatus()::exit");
		ResponseWrapper<AidStatusResponseDTO> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(resp);
		return responseWrapper;
	}

	@ResponseFilter
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
		logger.debug("ResidentController::getunreadnotificationCount()::entry");
		String individualId = identityServiceImpl.getResidentIdaToken();

		ResponseWrapper<UnreadNotificationDto> count = residentService.getnotificationCount(individualId);
		logger.debug("ResidentController::getunreadnotificationCount()::exit");

		return count;
	}

	@ResponseFilter
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
		logger.debug("ResidentController::getnotificationclickdttimes()::entry");
		String individualId = identityServiceImpl.getResidentIdaToken();
		ResponseWrapper<BellNotificationDto> response = residentService.getbellClickdttimes(individualId);
		logger.debug("ResidentController::getnotificationclickdttimes::exit");

		return response;
	}

	@ResponseFilter
	@PreAuthorize("@scopeValidator.hasAllScopes(" + "@authorizedScopes.getGetupdatedttimes()" + ")")
	@PutMapping(path = "/bell/updatedttime")
	@Operation(summary = "updatebellClickdttimes", description = "updatedttimes")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))), })
	public int bellupdateClickdttimes() throws ResidentServiceCheckedException, ApisResourceAccessException {
		logger.debug("ResidentController::updatedttime()::entry");
		String individualId = identityServiceImpl.getResidentIdaToken();
		int response = residentService.updatebellClickdttimes(individualId);
		logger.debug("ResidentController::updatedttime()::exit");
		return response;
	}

	@ResponseFilter
	@PreAuthorize("@scopeValidator.hasAllScopes(" + "@authorizedScopes.getGetUnreadServiceList()" + ")")
	@GetMapping("/unread/service-list")
	@Operation(summary = "get", description = "Get unread-service-list", tags = { "resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })

	public ResponseWrapper<List<UnreadServiceNotificationDto>> unreadServiceNotification()
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		logger.debug("ResidentController::getunreadServiceList()::entry");
		String Id = identityServiceImpl.getResidentIdaToken();
		ResponseWrapper<List<UnreadServiceNotificationDto>> unreadServiceNotificationDtoList = residentService
				.getUnreadnotifylist(Id);
		logger.debug("ResidentController::getunreadServiceList()::exit");
		return unreadServiceNotificationDtoList;
	}

}
