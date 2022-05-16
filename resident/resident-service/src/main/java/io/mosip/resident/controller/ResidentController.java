package io.mosip.resident.controller;

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.AuthHistoryRequestDTO;
import io.mosip.resident.dto.AuthHistoryResponseDTO;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDto;
import io.mosip.resident.dto.AuthUnLockRequestDTO;
import io.mosip.resident.dto.EuinRequestDTO;
import io.mosip.resident.dto.RegStatusCheckResponseDTO;
import io.mosip.resident.dto.RequestDTO;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentDemographicUpdateRequestDTO;
import io.mosip.resident.dto.ResidentReprintRequestDto;
import io.mosip.resident.dto.ResidentReprintResponseDto;
import io.mosip.resident.dto.ResidentUpdateRequestDto;
import io.mosip.resident.dto.ResidentUpdateResponseDTO;
import io.mosip.resident.dto.ResponseDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ResidentService;
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
		validator.validateRequestDTO(requestDTO);
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

	@ResponseFilter
	@PostMapping(value = "/req/auth-type-lock")
	@Operation(summary = "reqAauthTypeLock", description = "reqAauthTypeLock", tags = { "resident-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<ResponseDTO> reqAauthTypeLock(
			@Valid @RequestBody RequestWrapper<AuthLockOrUnLockRequestDto> requestDTO)
			throws ResidentServiceCheckedException {
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST,"request auth Type lock API"));
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_LOCK,requestDTO.getRequest().getTransactionID()));
		ResponseWrapper<ResponseDTO> response = new ResponseWrapper<>();
		response.setResponse(residentService.reqAauthTypeStatusUpdateV2(requestDTO.getRequest(), AuthTypeStatus.LOCK));
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_LOCK_SUCCESS,requestDTO.getRequest().getTransactionID()));
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
	 */
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
			throws ResidentServiceCheckedException {
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "update Uin API"));
		RequestWrapper<ResidentUpdateRequestDto> requestWrapper = JsonUtil.convertValue(requestDTO,
				new TypeReference<RequestWrapper<ResidentUpdateRequestDto>>() {
				});
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
	
	@GetMapping(path = "/auth-lock-status/{individualId}")
	public ResponseWrapper<Object> getAuthLockStatus(@PathVariable(name = "individualId") String individualId) {
		audit.setAuditRequestDto(
				EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "request auth lock status  API"));
		ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>();
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
	 * > It returns the type of the ID passed to it
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
}