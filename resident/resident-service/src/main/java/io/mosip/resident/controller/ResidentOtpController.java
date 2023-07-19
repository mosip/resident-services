package io.mosip.resident.controller;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.dto.IndividualIdOtpRequestDTO;
import io.mosip.resident.dto.IndividualIdResponseDto;
import io.mosip.resident.dto.OtpRequestDTO;
import io.mosip.resident.dto.OtpResponseDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.ResidentOtpService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.validator.RequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "resident-otp-controller", description = "Resident Otp Controller")
public class ResidentOtpController {

	@Autowired
	private ResidentOtpService residentOtpService;

	@Autowired
	private AuditUtil audit;
	
	@Autowired
	private RequestValidator requestValidator;
	
	@Value("${mosip.resident.api.id.otp.request}")
	private String otpRequestId;
	
	@Value("${resident.version.new}")
	private String otpRequestVersion;

	private static final Logger logger = LoggerConfiguration.logConfig(ResidentOtpController.class);

	@PostMapping(value = "/req/otp")
	@Operation(summary = "reqOtp", description = "reqOtp", tags = { "resident-otp-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public OtpResponseDTO reqOtp(@RequestBody OtpRequestDTO otpRequestDto) throws ResidentServiceCheckedException, NoSuchAlgorithmException {
		logger.debug("ResidentOtpController::reqOtp()::entry");
		OtpResponseDTO otpResponseDTO;
		try {
			otpResponseDTO = residentOtpService.generateOtp(otpRequestDto);
		} catch (ResidentServiceException e) {
			audit.setAuditRequestDto(EventEnum.OTP_GEN_EXCEPTION);
			throw e;
		}
		audit.setAuditRequestDto(EventEnum.OTP_GEN_SUCCESS);
		logger.debug("ResidentOtpController::reqOtp()::exit");
		return otpResponseDTO;
	}
	
	@PostMapping(value = "/individualId/otp")
	@Operation(summary = "reqIndividualIdOtp", description = "reqIndividualIdOtp", tags = { "resident-otp-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public IndividualIdResponseDto reqOtpForIndividualId(@RequestBody IndividualIdOtpRequestDTO individualIdRequestDto) throws ResidentServiceCheckedException, NoSuchAlgorithmException, ApisResourceAccessException {
		logger.debug("ResidentOtpController::reqOtpForIndividualId()::entry");
		IndividualIdResponseDto individualIdResponseDto;
		try {
			requestValidator.validateReqOtp(individualIdRequestDto);
			individualIdResponseDto = residentOtpService.generateOtpForIndividualId(individualIdRequestDto);
		} catch (ResidentServiceCheckedException | ApisResourceAccessException e) {
			audit.setAuditRequestDto(EventEnum.OTP_AID_GEN_EXCEPTION);
			throw new ResidentServiceException(e.getErrorCode(), e.getErrorText(), e,
					Map.of(ResidentConstants.REQ_RES_ID, otpRequestId));
		} catch (ResidentServiceException | InvalidInputException e) {
			audit.setAuditRequestDto(EventEnum.OTP_AID_GEN_EXCEPTION);
			e.setMetadata(Map.of(ResidentConstants.REQ_RES_ID, otpRequestId));
			throw e;
		}
		audit.setAuditRequestDto(EventEnum.OTP_INDIVIDUALID_GEN_SUCCESS);
		individualIdResponseDto.setId(otpRequestId);
		individualIdResponseDto.setVersion(otpRequestVersion);
		logger.debug("ResidentOtpController::reqOtpForIndividualId()::exit");
		return individualIdResponseDto;
	}

}
