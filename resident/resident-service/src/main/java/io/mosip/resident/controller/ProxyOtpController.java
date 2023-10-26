package io.mosip.resident.controller;

import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_DESCRIPTION;
import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_ID;

import java.util.Map;

import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.annotation.Timed;
import io.mosip.kernel.core.authmanager.model.AuthNResponse;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.dto.MainResponseDTO;
import io.mosip.resident.dto.OtpRequestDTOV2;
import io.mosip.resident.dto.OtpRequestDTOV3;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.ProxyOtpService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.validator.RequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.util.function.Tuple2;

/**
 * This class provides different api to perform operation for login
 *
 * @author Kamesh Shekhar Prasad
 * @since 1.0.0
 *
 */
@RestController
@Tag(name = "login-controller", description = "Login Controller")
public class ProxyOtpController {

	private Logger log = LoggerConfiguration.logConfig(ProxyOtpController.class);

	@Autowired
	private ProxyOtpService proxyOtpService;

	@Autowired
	private RequestValidator requestValidator;

	@Autowired
	private Environment environment;
	
    @Autowired
    private AuditUtil audit;

	@Autowired
	private IdentityService identityService;

	/**
	 * This Post api use to send otp to the user by email or sms
	 *
	 * @param userOtpRequest
	 * @return AuthNResponse
	 */

	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PostMapping(value = "/contact-details/send-otp", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "sendOTP", description = "Send Otp to UserId", tags = "login-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<MainResponseDTO<AuthNResponse>> sendOTP(
			@Validated @RequestBody MainRequestDTO<OtpRequestDTOV2> userOtpRequest) throws ApisResourceAccessException, ResidentServiceCheckedException {
		log.debug("ProxyOtpController::sendOTP()::entry");
		ResponseEntity<MainResponseDTO<AuthNResponse>> responseEntity;
		String userid = userOtpRequest.getRequest().getUserId();
		try {
			IdentityDTO identityDTO = identityService.getIdentity(identityService.getResidentIndvidualIdFromSession());
			requestValidator.validateProxySendOtpRequest(userOtpRequest, identityDTO);
			responseEntity = proxyOtpService.sendOtp(userOtpRequest, identityDTO);
		}
		catch (InvalidInputException e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_OTP_FAILURE, userid, "Send OTP"));
			throw new ResidentServiceException(e.getErrorCode(), e.getErrorText(), e,
					Map.of(ResidentConstants.REQ_RES_ID,
							environment.getProperty(ResidentConstants.RESIDENT_CONTACT_DETAILS_SEND_OTP_ID)));
		} catch (ApisResourceAccessException e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_OTP_FAILURE, userid, "Send OTP"));
			throw new ApisResourceAccessException(ResidentErrorCode.CLAIM_NOT_AVAILABLE.getErrorCode(),
					ResidentErrorCode.CLAIM_NOT_AVAILABLE.getErrorMessage(), e);
		} catch (ResidentServiceException | ResidentServiceCheckedException e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_OTP_FAILURE, userid, "Send OTP"));
			throw e;
		}
		log.debug("ProxyOtpController::sendOTP()::exit");
		return responseEntity;
	}



	/**
	 * This Post api use to validate userid and otp
	 *
	 * @param userIdOtpRequest
	 * @return AuthNResponse
	 */
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PostMapping(value = "/contact-details/update-data", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "validateWithUserIdOtp", description = "Validate UserId and Otp", tags = "login-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<MainResponseDTO<AuthNResponse>> validateWithUserIdOtp(
			@Validated @RequestBody MainRequestDTO<OtpRequestDTOV3> userIdOtpRequest) {
		log.debug("ProxyOtpController::validateWithUserIdOtp()::entry");
		String userId = userIdOtpRequest.getRequest().getUserId();
		try {
			requestValidator.validateUpdateDataRequest(userIdOtpRequest);
		} catch (InvalidInputException e) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.OTP_VALIDATION_FAILED, userId, "Validate OTP Failed"));
			throw new ResidentServiceException(e.getErrorCode(), e.getErrorText(), e,
					Map.of(ResidentConstants.REQ_RES_ID,
							environment.getProperty(ResidentConstants.RESIDENT_CONTACT_DETAILS_UPDATE_ID)));
		}
		Tuple2<MainResponseDTO<AuthNResponse>, String> tupleResponse = proxyOtpService.validateWithUserIdOtp(userIdOtpRequest);
		log.debug("ProxyOtpController::validateWithUserIdOtp()::exit");
		return ResponseEntity.ok()
				.header(ResidentConstants.EVENT_ID, tupleResponse.getT2())
				.body(tupleResponse.getT1());
	}
}
