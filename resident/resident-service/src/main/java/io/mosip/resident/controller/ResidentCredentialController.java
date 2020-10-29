package io.mosip.resident.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.dto.CredentialCancelRequestResponseDto;
import io.mosip.resident.dto.CredentialRequestStatusResponseDto;
import io.mosip.resident.dto.CredentialTypeResponse;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentCredentialRequestDto;
import io.mosip.resident.dto.ResidentCredentialResponseDto;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ResidentCredentialService;
import io.mosip.resident.validator.RequestValidator;

@RestController
public class ResidentCredentialController {

	@Autowired
	private RequestValidator validator;

	@Autowired
	private ResidentCredentialService residentCredentialService;

	@ResponseFilter
	@PostMapping(value = "/req/credential")
	public ResponseEntity<Object> reqCredential(
			@Valid @RequestBody RequestWrapper<ResidentCredentialRequestDto> requestDTO)
			throws ResidentServiceCheckedException {
		//validator.validateCredentialRequest(requestDTO);
		ResponseWrapper<ResidentCredentialResponseDto> response = new ResponseWrapper<>();
		response.setResponse(residentCredentialService.reqCredential(requestDTO.getRequest()));
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@GetMapping(value = "req/credential/status/{requestId}")
	public ResponseEntity<Object> getCredentialStatus(@PathVariable("requestId") String requestId)
			throws ResidentServiceCheckedException {
		ResponseWrapper<CredentialRequestStatusResponseDto> response = new ResponseWrapper<>();
		response.setResponse(residentCredentialService.getStatus(requestId));
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@GetMapping(value = "credential/types")
	public ResponseEntity<Object> getCredentialTypes()
			throws ResidentServiceCheckedException {
		ResponseWrapper<CredentialTypeResponse> response = new ResponseWrapper<>();
		response.setResponse(residentCredentialService.getCredentialTypes());
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@GetMapping(value = "req/credential/cancel/{requestId}")
	public ResponseEntity<Object> getCancelCredentialRequest(@PathVariable("requestId") String requestId)
			throws ResidentServiceCheckedException {
		ResponseWrapper<CredentialCancelRequestResponseDto> response = new ResponseWrapper<>();
		response.setResponse(residentCredentialService.getCancelCredentialRequest(requestId));
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
