package io.mosip.resident.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.dto.IdAuthRequestDto;
import io.mosip.resident.dto.IdAuthResponseDto;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Resident IdAuth controller class.
 * 
 * @author Ritik Jain
 */
@RestController
@Tag(name = "id-auth-controller", description = "Id Auth Controller")
public class IdAuthController {

	@Autowired
	private IdAuthService idAuthService;

	@Autowired
	private AuditUtil auditUtil;

	private static final Logger logger = LoggerConfiguration.logConfig(IdAuthController.class);

	/**
	 * Validate OTP
	 * 
	 * @param requestWrapper
	 * @return ResponseWrapper<IdAuthResponseDto1> object
	 * @throws OtpValidationFailedException
	 */
	@ResponseFilter
	@PostMapping("/req/validateOTP")
	@Operation(summary = "validateOtp", description = "validateOtp", tags = { "id-auth-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<IdAuthResponseDto> validateOtp(@RequestBody RequestWrapper<IdAuthRequestDto> requestWrapper)
			throws OtpValidationFailedException {
		logger.debug("IdAuthController::validateOtp()::entry");
		auditUtil.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_OTP, requestWrapper.getRequest().getTransactionID(),
				"OTP Validate Request"));
		Boolean authStatus = idAuthService.validateOtp(requestWrapper.getRequest().getTransactionID(),
				requestWrapper.getRequest().getIndividualId(), requestWrapper.getRequest().getOtp());
		auditUtil.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_OTP_SUCCESS, requestWrapper.getRequest().getTransactionID(),
				"OTP Validate Request Success"));
		ResponseWrapper<IdAuthResponseDto> responseWrapper = new ResponseWrapper<IdAuthResponseDto>();
		IdAuthResponseDto idAuthResponseDto = new IdAuthResponseDto();
		idAuthResponseDto.setAuthStatus(authStatus);
		idAuthResponseDto.setTransactionID(requestWrapper.getRequest().getTransactionID());
		responseWrapper.setResponse(idAuthResponseDto);
		logger.debug("IdAuthController::validateOtp()::exit");
		return responseWrapper;
	}

}
