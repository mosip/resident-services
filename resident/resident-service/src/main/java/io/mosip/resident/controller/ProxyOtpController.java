package io.mosip.resident.controller;

import io.mosip.kernel.core.authmanager.model.AuthNResponse;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.dto.MainResponseDTO;
import io.mosip.resident.dto.OtpRequestDTOV2;
import io.mosip.resident.service.ProxyOtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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

	/**
	 * This Post api use to send otp to the user by email or sms
	 *
	 * @param userOtpRequest
	 * @return AuthNResponse
	 */

	@PreAuthorize("@scopeValidator.hasAllScopes("
			+ "@authorizedScopes.getPostSendOtp()"
			+ ")")
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
			@Validated @RequestBody MainRequestDTO<OtpRequestDTOV2> userOtpRequest) {
		return proxyOtpService.sendOtp(userOtpRequest);
	}



//	/**
//	 * This Post api use to validate userid and otp
//	 *
//	 * @param userIdOtpRequest
//	 * @param errors
//	 * @return AuthNResponse
//	 */
//	@PostMapping(value = "/validateOtp", produces = MediaType.APPLICATION_JSON_VALUE)
//	@Operation(summary = "validateWithUserIdOtp", description = "Validate UserId and Otp", tags = "login-controller")
//	@ApiResponses(value = {
//			@ApiResponse(responseCode = "200", description = "OK"),
//			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
//			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
//			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
//			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
//	public ResponseEntity<MainResponseDTO<AuthNResponse>> validateWithUserIdOtp(
//			@Validated @RequestBody MainRequestDTO<User> userIdOtpRequest, @ApiIgnore Errors errors,
//			HttpServletResponse res, HttpServletRequest req) {
//
//		log.debug("User ID: {}", userIdOtpRequest.getRequest().getUserId());
//		loginValidator.validateId(VALIDATEOTP, userIdOtpRequest.getId(), errors);
//		DataValidationUtil.validate(errors, VALIDATEOTP);
//		MainResponseDTO<AuthNResponse> responseBody = loginService.validateWithUserIdOtp(userIdOtpRequest);
//		if (responseBody.getResponse() != null && responseBody.getErrors() == null) {
//			Cookie responseCookie = new Cookie("Authorization",
//					loginService.getLoginToken(userIdOtpRequest.getRequest().getUserId(), req.getRequestURI()));
//			responseCookie.setMaxAge((int) -1);
//			responseCookie.setHttpOnly(true);
//			responseCookie.setSecure(true);
//			responseCookie.setPath(cookieContextPath);
//			res.addCookie(responseCookie);
//		}
//		return ResponseEntity.status(HttpStatus.OK).body(responseBody);
//	}
}
