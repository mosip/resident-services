package io.mosip.resident.controller;

import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.IndividualIdOtpRequestDTO;
import io.mosip.resident.dto.OtpRequestDTO;
import io.mosip.resident.dto.OtpResponseDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ResidentOtpService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
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
	
	@Value("${mosip.resident.api.id.otp.request}")
	private String otpRequestId;
	
	@Value("${resident.version.new}")
	private String otpRequestVersion;

	@PostMapping(value = "/req/otp")
	@Operation(summary = "reqOtp", description = "reqOtp", tags = { "resident-otp-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public OtpResponseDTO reqOtp(@RequestBody OtpRequestDTO otpRequestDto) throws ResidentServiceCheckedException, NoSuchAlgorithmException {
		audit.setAuditRequestDto(EventEnum.OTP_GEN);
		OtpResponseDTO otpResponseDTO = residentOtpService.generateOtp(otpRequestDto);
		audit.setAuditRequestDto(EventEnum.OTP_GEN_SUCCESS);
		return otpResponseDTO;
	}
	
	@PostMapping(value = "/req/individualId/otp")
	@Operation(summary = "reqIndividualIdOtp", description = "reqIndividualIdOtp", tags = { "resident-otp-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public OtpResponseDTO reqOtpForIndividualId(@RequestBody IndividualIdOtpRequestDTO otpRequestDto) throws ResidentServiceCheckedException, NoSuchAlgorithmException, ApisResourceAccessException {
		audit.setAuditRequestDto(EventEnum.OTP_INDIVIDUALID_GEN);
		if(otpRequestDto.getIndividualId()  == null) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.INVALID_INPUT.getErrorCode(), ResidentErrorCode.INVALID_INPUT.getErrorMessage() + "individualId");
		}
		OtpResponseDTO otpResponseDTO = residentOtpService.generateOtpForIndividualId(otpRequestDto);
		audit.setAuditRequestDto(EventEnum.OTP_INDIVIDUALID_GEN_SUCCESS);
		otpResponseDTO.setId(otpRequestId);
		otpResponseDTO.setVersion(otpRequestVersion);
		return otpResponseDTO;
	}

}
