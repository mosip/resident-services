package io.mosip.resident.controller;

import java.io.ByteArrayInputStream;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.dto.AuthHistoryRequestDTO;
import io.mosip.resident.dto.AuthHistoryResponseDTO;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDto;
import io.mosip.resident.dto.EuinRequestDTO;
import io.mosip.resident.dto.RegStatusCheckResponseDTO;
import io.mosip.resident.dto.RequestDTO;
import io.mosip.resident.dto.RequestWrapper;
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
import io.mosip.resident.validator.RequestValidator;

@RestController
public class ResidentController {

	@Autowired
	private ResidentService residentService;

	@Autowired
	private RequestValidator validator;
	
	@Autowired
	private AuditUtil audit;

	@ResponseFilter
	@PostMapping(value = "/rid/check-status")
	public ResponseWrapper<RegStatusCheckResponseDTO> getRidStatus(
			@Valid @RequestBody RequestWrapper<RequestDTO> requestDTO) throws ApisResourceAccessException {
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST,"get Rid status API"));
		validator.validateRequestDTO(requestDTO);
		ResponseWrapper<RegStatusCheckResponseDTO> response = new ResponseWrapper<>();
		audit.setAuditRequestDto(EventEnum.RID_STATUS);
		response.setResponse(residentService.getRidStatus(requestDTO.getRequest()));
		audit.setAuditRequestDto(EventEnum.RID_STATUS_SUCCESS);
		return response;
	}

	@PostMapping(value = "/req/euin")
	public ResponseEntity<Object> reqEuin(@Valid @RequestBody RequestWrapper<EuinRequestDTO> requestDTO)
			throws ResidentServiceCheckedException {
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST,"request Euin API"));
		validator.validateEuinRequest(requestDTO);
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_EUIN,requestDTO.getRequest().getTransactionID()));
		byte[] pdfbytes = residentService.reqEuin(requestDTO.getRequest());
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_EUIN_SUCCESS,requestDTO.getRequest().getTransactionID()));
		InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(pdfbytes));

		return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/pdf"))
				.header("Content-Disposition",
						"attachment; filename=\"" + requestDTO.getRequest().getIndividualId() + ".pdf\"")
				.body((Object) resource);
	}

	@ResponseFilter
	@PostMapping(value = "/req/print-uin")
	public ResponseEntity<Object> reqPrintUin(@Valid @RequestBody RequestWrapper<ResidentReprintRequestDto> requestDTO)
			throws ResidentServiceCheckedException {
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST,"request print Uin API"));
		validator.validateReprintRequest(requestDTO);
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_PRINTUIN,requestDTO.getRequest().getTransactionID()));
		ResponseWrapper<ResidentReprintResponseDto> response = new ResponseWrapper<>();
		response.setResponse(residentService.reqPrintUin(requestDTO.getRequest()));
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_PRINTUIN_SUCCESS,requestDTO.getRequest().getTransactionID()));
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@ResponseFilter
	@PostMapping(value = "/req/auth-lock")
	public ResponseWrapper<ResponseDTO> reqAauthLock(
			@Valid @RequestBody RequestWrapper<AuthLockOrUnLockRequestDto> requestDTO)
			throws ResidentServiceCheckedException {
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST,"request auth lock API"));
		validator.validateAuthLockOrUnlockRequest(requestDTO, AuthTypeStatus.LOCK);
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_LOCK,requestDTO.getRequest().getTransactionID()));
		ResponseWrapper<ResponseDTO> response = new ResponseWrapper<>();
		response.setResponse(residentService.reqAauthTypeStatusUpdate(requestDTO.getRequest(), AuthTypeStatus.LOCK));
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_LOCK_SUCCESS,requestDTO.getRequest().getTransactionID()));
		return response;
	}

	@ResponseFilter
	@PostMapping(value = "/req/auth-unlock")
	public ResponseWrapper<ResponseDTO> reqAuthUnlock(
			@Valid @RequestBody RequestWrapper<AuthLockOrUnLockRequestDto> requestDTO)
			throws ResidentServiceCheckedException {
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST,"request auth unlock  API"));
		validator.validateAuthLockOrUnlockRequest(requestDTO, AuthTypeStatus.UNLOCK);
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_UNLOCK,requestDTO.getRequest().getTransactionID()));
		ResponseWrapper<ResponseDTO> response = new ResponseWrapper<>();
		response.setResponse(residentService.reqAauthTypeStatusUpdate(requestDTO.getRequest(), AuthTypeStatus.UNLOCK));
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_UNLOCK_SUCCESS,requestDTO.getRequest().getTransactionID()));
		return response;
	}

	@ResponseFilter
	@PostMapping(value = "/req/auth-history")
	public ResponseWrapper<AuthHistoryResponseDTO> reqAuthHistory(
			@Valid @RequestBody RequestWrapper<AuthHistoryRequestDTO> requestDTO)
			throws ResidentServiceCheckedException {
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST,"request auth history"));
		validator.validateAuthHistoryRequest(requestDTO);
		ResponseWrapper<AuthHistoryResponseDTO> response = new ResponseWrapper<>();
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_HISTORY,requestDTO.getRequest().getTransactionID()));
		response.setResponse(residentService.reqAuthHistory(requestDTO.getRequest()));
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_HISTORY_SUCCESS,requestDTO.getRequest().getTransactionID()));
		return response;
	}

	@ResponseFilter
	@PostMapping(value = "/req/update-uin")
	public ResponseWrapper<ResidentUpdateResponseDTO> updateUin(
			@Valid @RequestBody RequestWrapper<ResidentUpdateRequestDto> requestDTO)
			throws ResidentServiceCheckedException {
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST,"update Uin API"));
		validator.validateUpdateRequest(requestDTO);
		ResponseWrapper<ResidentUpdateResponseDTO> response = new ResponseWrapper<>();
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.UPDATE_UIN,requestDTO.getRequest().getTransactionID()));
		response.setResponse(residentService.reqUinUpdate(requestDTO.getRequest()));
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.UPDATE_UIN_SUCCESS,requestDTO.getRequest().getTransactionID()));
		return response;
	}
}