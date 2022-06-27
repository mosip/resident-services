package io.mosip.resident.controller;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import io.mosip.resident.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.type.TypeReference;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
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
	
	private static final Logger logger = LoggerConfiguration.logConfig(ResidentController.class);

	@ResponseFilter
	@PostMapping(value = "/rid/check-status")
	@Operation(summary = "getRidStatus", description = "getRidStatus", tags = { "resident-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
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
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
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
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
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
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
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
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
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

	@PreAuthorize("@scopeValidator.hasAllScopes("
			+ "@authorizedScopes.getPostAuthTypeStatus()"
		+ ")")
	@ResponseFilter
	@PostMapping(value = "/req/auth-type-status")
	@Operation(summary = "reqAuthTypeStatus", description = "reqAuthTypeStatus", tags = { "resident-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<ResponseDTO> reqAauthTypeStatusUpdateV2(
			@Valid @RequestBody RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestDTO)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST,"update auth Type status API"));
		String individualId = identityServiceImpl.getResidentIndvidualId();
		validator.validateAuthLockOrUnlockRequestV2(requestDTO);
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_LOCK, individualId));
		ResponseWrapper<ResponseDTO> response = new ResponseWrapper<>();
		response.setResponse(residentService.reqAauthTypeStatusUpdateV2(requestDTO.getRequest()));
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_LOCK_SUCCESS,individualId));
		return response;
	}

	@ResponseFilter
	@PostMapping(value = "/req/auth-history")
	@Operation(summary = "reqAuthHistory", description = "reqAuthHistory", tags = { "resident-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
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

	@PreAuthorize("@scopeValidator.hasAllScopes("
			+ "@authorizedScopes.getGetCredentialRequestStatus()"
			+ ")")
	@GetMapping(path="/check-status/aid/{AID}")
	@Operation(summary = "getGetCheckAidStatus", description = "checkAidStatus", tags = { "resident-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<ResponseDTO> checkAidStatus(
			@PathVariable(name = "AID") String AID) throws ResidentServiceCheckedException {
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "checkAidStatus"));
		logger.debug("checkAidStatus controller entry");
		validator.validateCheckAidStatusRequest(AID);
		ResponseWrapper<ResponseDTO> response = new ResponseWrapper<>();
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.CHECK_AID_STATUS_REQUEST,
				AID));
		ResponseDTO responseDTO = new ResponseDTO();
		responseDTO.setStatus(residentService.checkAidStatus(AID));
		responseDTO.setMessage("Credential Request Status got Successfully");
		response.setResponse(responseDTO);
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.CHECK_AID_STATUS_REQUEST_SUCCESS,
				AID));
		return response;
	}

	@PreAuthorize("@scopeValidator.hasAllScopes("
			+ "@authorizedScopes.getGetServiceAuthHistoryRoles()"
			+ ")")
	@GetMapping(path="/getServiceHistory")
	@Operation(summary = "getServiceHistory", description = "getServiceHistory", tags = { "resident-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<ServiceTypeResponseDto> getServiceHistory(@RequestParam(name = "pageStart", required = false) Integer pageStart,
																	@RequestParam(name = "pageFetch", required = false) Integer pageFetch,
																	@RequestParam(name = "fromDateTime", required = false)
												   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDateTime,
																	@RequestParam(name = "toDateTime", required = false)
												   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDateTime,
																	@RequestParam(name = "sortType", required = false) String sortType,
																	@RequestParam(name = "serviceType", required = false) String serviceType) throws ResidentServiceCheckedException, ApisResourceAccessException {
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "getServiceHistory"));
		List<ServiceHistoryResponseDto> residentServiceServiceHistory =residentService.getServiceHistory(pageStart, pageFetch, fromDateTime, toDateTime, serviceType, sortType);
		validator.validateServiceHistoryRequest(pageStart, pageFetch, fromDateTime, toDateTime, sortType, serviceType);
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.GET_SERVICE_HISTORY,
				"getServiceHistory"));
		ServiceTypeResponseDto serviceTypeResponseDto = new ServiceTypeResponseDto();
		Map<String, List<ServiceHistoryResponseDto>> serviceTypeMap = new HashMap<>();
		serviceTypeMap.put("serviceType", residentServiceServiceHistory);
		serviceTypeResponseDto.setResponse(serviceTypeMap);
		serviceTypeResponseDto.setResponseTime(String.valueOf(LocalDateTime.now()));
		return new ResponseEntity<>(serviceTypeResponseDto, HttpStatus.OK);
	}

	@PreAuthorize("@scopeValidator.hasAllScopes("
			+ "@authorizedScopes.getGetServiceRequestUpdate()"
			+ ")")
	@GetMapping(path="/get/service-request-update")
	@Operation(summary = "getServiceRequestUpdate", description = "getServiceRequestUpdate", tags = { "resident-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<List<ResidentServiceHistoryResponseDto>> getServiceRequestUpdate(@RequestParam(name = "pageStart", required = false) Integer pageStart,
																@RequestParam(name = "pageFetch", required = false) Integer pageFetch) throws ResidentServiceCheckedException {
		logger.info("getServiceRequestUpdate :: entry");
		ResponseWrapper<List<ResidentServiceHistoryResponseDto>> response = new ResponseWrapper<>();
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_SERVICE_REQUEST_UPDATE, "Get Service request update"));
		List<ResidentServiceHistoryResponseDto> residentServiceHistoryResponseDtoList =  residentService.getServiceRequestUpdate(pageStart, pageFetch);
		response.setResponse(residentServiceHistoryResponseDtoList);
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_SERVICE_REQUEST_UPDATE_SUCCESS,
				"Get Service request update"));
		return response;
	}

	@Deprecated
	@ResponseFilter
	@PostMapping(value = "/req/update-uin")
	@Operation(summary = "updateUin", description = "updateUin", tags = { "resident-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
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
	@PreAuthorize("@scopeValidator.hasAllScopes("
			+ "@authorizedScopes.getPatchUpdateUin()"
		+ ")")
	@ResponseFilter
	@PatchMapping(value = "/req/update-uin")
	@Operation(summary = "updateUin", description = "updateUin", tags = { "resident-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
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
		if(requestDTO.getRequest() != null) {
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
	
	@PreAuthorize("@scopeValidator.hasAllScopes("
			+ "@authorizedScopes.getGetAuthLockStatus()"
		+ ")")
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

	/**
	 *  It returns the type of the ID passed to it
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
	@PostMapping("/aid/status")
	@Operation(summary = "checkAidStatus", description = "Get AID Status", tags = {
			"resident-controller" })
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
	
	
}