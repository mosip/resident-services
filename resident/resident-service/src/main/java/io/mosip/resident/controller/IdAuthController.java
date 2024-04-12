package io.mosip.resident.controller;

import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_DESCRIPTION;
import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_ID;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.annotation.Timed;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.dto.IdAuthRequestDto;
import io.mosip.resident.dto.IdAuthResponseDto;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ValidateOtpResponseDto;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.AuditEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.util.function.Tuple2;

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
    
    @Value("${mosip.resident.identity.auth.internal.id}")
    private String validateOtpId;

	private static final Logger logger = LoggerConfiguration.logConfig(IdAuthController.class);

	/**
	 * Validate OTP
	 * 
	 * @param requestWrapper
	 * @return ResponseWrapper<IdAuthResponseDto> object
	 * @throws OtpValidationFailedException
	 * @throws ResidentServiceCheckedException 
	 */
	@ResponseFilter
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PostMapping("/validate-otp")
	@Operation(summary = "validateOtp", description = "validateOtp", tags = { "id-auth-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<Object> validateOtp(@RequestBody RequestWrapper<IdAuthRequestDto> requestWrapper)
			throws OtpValidationFailedException, ResidentServiceCheckedException {
		logger.debug("IdAuthController::validateOtp()::entry");
		Tuple2<Boolean, String> tupleResponse = null;
		try {
		tupleResponse = idAuthService.validateOtpV1(requestWrapper.getRequest().getTransactionId(),
				requestWrapper.getRequest().getIndividualId(), requestWrapper.getRequest().getOtp());
		auditUtil.setAuditRequestDto(AuditEnum.getAuditEventWithValue(AuditEnum.VALIDATE_OTP_SUCCESS, requestWrapper.getRequest().getTransactionId(),
				"OTP Validate Request Success"));
		} catch (OtpValidationFailedException e) {
			auditUtil.setAuditRequestDto(AuditEnum.getAuditEventWithValue(AuditEnum.VALIDATE_OTP_FAILURE,
					requestWrapper.getRequest().getTransactionId(), "OTP Validation Failed"));
			throw new OtpValidationFailedException(e.getErrorCode(), e.getErrorText(), e,
					Map.of(ResidentConstants.HTTP_STATUS_CODE, HttpStatus.OK, ResidentConstants.REQ_RES_ID,
							validateOtpId, ResidentConstants.EVENT_ID,
							e.getMetadata().get(ResidentConstants.EVENT_ID)));
		}
		ResponseWrapper<IdAuthResponseDto> responseWrapper = new ResponseWrapper<IdAuthResponseDto>();
		ValidateOtpResponseDto validateOtpResponseDto = new ValidateOtpResponseDto();
		validateOtpResponseDto.setAuthStatus(tupleResponse.getT1());
		validateOtpResponseDto.setTransactionId(requestWrapper.getRequest().getTransactionId());
		validateOtpResponseDto.setStatus(ResidentConstants.SUCCESS);
		responseWrapper.setResponse(validateOtpResponseDto);
		logger.debug("IdAuthController::validateOtp()::exit");
		return ResponseEntity.ok()
				.header(ResidentConstants.EVENT_ID, tupleResponse.getT2())
				.body(responseWrapper);
	}

}
