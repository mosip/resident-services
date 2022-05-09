package io.mosip.resident.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentVidRequestDto;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.VidResponseDto;
import io.mosip.resident.dto.VidRevokeRequestDTO;
import io.mosip.resident.dto.VidRevokeResponseDTO;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.validator.RequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Resident VID controller class.
 * 
 * @Author : Monobikash Das
 */
@RefreshScope
@RestController
@Tag(name = "Resident Service", description = "Resident Vid Controller")
public class ResidentVidController {

	Logger logger = LoggerConfiguration.logConfig(ResidentVidController.class);

	@Autowired
	private ResidentVidService residentVidService;

	@Autowired
	private RequestValidator validator;

	@Autowired
	private AuditUtil auditUtil;

	@GetMapping(path = "/vid/policy")
	@Operation(summary = "Retrieve VID policy", description = "Retrieve VID policy", tags = { "Resident Service" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "VID Policy retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResidentVidRequestDto.class)))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<ResponseWrapper<String>> getVidPolicy() {
		ResponseWrapper<String> response = new ResponseWrapper<>();
		try {
			response.setResponse(residentVidService.getVidPolicy());
		} catch (ResidentServiceCheckedException e) {
			response.setErrors(List.of(new ServiceError(ResidentErrorCode.POLICY_EXCEPTION.getErrorCode(),
					ResidentErrorCode.POLICY_EXCEPTION.getErrorMessage())));
		}
		return ResponseEntity.ok().body(response);
	}

	@PostMapping(path = "/vid", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "generateVid", description = "generateVid", tags = { "Resident Service" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "VID successfully generated", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResidentVidRequestDto.class)))),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "400", description = "Unable to generate VID", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<Object> generateVid(@RequestBody(required = true) ResidentVidRequestDto requestDto)
			throws OtpValidationFailedException, ResidentServiceCheckedException {
		return generateVid(requestDto, true);
	}

	@PostMapping(path = "/generate-vid", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "generateVid", description = "generateVid", tags = { "Resident Service" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "VID successfully generated", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResidentVidRequestDto.class)))),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "400", description = "Unable to generate VID", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<Object> generateVidWithoutOTP(@RequestBody(required = true) ResidentVidRequestDto requestDto)
			throws ResidentServiceCheckedException, OtpValidationFailedException {
		requestDto.getRequest().setOtp(null);
		return generateVid(requestDto, false);
	}

	private ResponseEntity<Object> generateVid(ResidentVidRequestDto requestDto, boolean isOtpValidationRequired)
			throws OtpValidationFailedException, ResidentServiceCheckedException {
		auditUtil.setAuditRequestDto(
				EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "Request to generate VID"));
		validator.validateVidCreateRequest(requestDto, isOtpValidationRequired);
		auditUtil.setAuditRequestDto(
				EventEnum.getEventEnumWithValue(EventEnum.GENERATE_VID, requestDto.getRequest().getIndividualId()));
		ResponseWrapper<VidResponseDto> vidResponseDto = residentVidService.generateVid(requestDto.getRequest());
		auditUtil.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.GENERATE_VID_SUCCESS,
				requestDto.getRequest().getIndividualId()));
		return ResponseEntity.ok().body(vidResponseDto);
	}

	@PatchMapping(path = "/vid/{vid}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Revoke VID", description = "Revoke VID", tags = { "Resident Service" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "VID successfully revoked", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseWrapper.class)))),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "400", description = "Unable to revoke VID", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<Object> revokeVid(
			@RequestBody(required = true) RequestWrapper<VidRevokeRequestDTO> requestDto, @PathVariable String vid)
			throws OtpValidationFailedException, ResidentServiceCheckedException {
		return revokeVid(requestDto, vid, true);
	}

	@PatchMapping(path = "/revoke-vid/{vid}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Revoke VID", description = "Revoke VID", tags = { "Resident Service" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "VID successfully revoked", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseWrapper.class)))),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "400", description = "Unable to revoke VID", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<Object> revokeVidWithoutOTP(
			@RequestBody(required = true) RequestWrapper<VidRevokeRequestDTO> requestDto, @PathVariable String vid)
			throws OtpValidationFailedException, ResidentServiceCheckedException {
		requestDto.getRequest().setOtp(null);
		return revokeVid(requestDto, vid, false);
	}

	private ResponseEntity<Object> revokeVid(RequestWrapper<VidRevokeRequestDTO> requestDto, String vid,
			boolean isOtpValidationRequired) throws OtpValidationFailedException, ResidentServiceCheckedException {
		auditUtil.setAuditRequestDto(
				EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "Request to revoke VID"));
		validator.validateVidRevokeRequest(requestDto, isOtpValidationRequired);
		auditUtil.setAuditRequestDto(
				EventEnum.getEventEnumWithValue(EventEnum.REVOKE_VID, requestDto.getRequest().getIndividualId()));
		ResponseWrapper<VidRevokeResponseDTO> vidResponseDto = residentVidService.revokeVid(requestDto.getRequest(),
				vid);
		auditUtil.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REVOKE_VID_SUCCESS,
				requestDto.getRequest().getIndividualId()));
		return ResponseEntity.ok().body(vidResponseDto);
	}
}