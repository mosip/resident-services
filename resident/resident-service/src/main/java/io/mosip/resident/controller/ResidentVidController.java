package io.mosip.resident.controller;

import io.micrometer.core.annotation.Timed;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.BaseVidRevokeRequestDTO;
import io.mosip.resident.dto.IVidRequestDto;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentVidRequestDto;
import io.mosip.resident.dto.ResidentVidRequestDtoV2;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.VidRequestDto;
import io.mosip.resident.dto.VidResponseDto;
import io.mosip.resident.dto.VidRevokeRequestDTO;
import io.mosip.resident.dto.VidRevokeRequestDTOV2;
import io.mosip.resident.dto.VidRevokeResponseDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.util.function.Tuple2;

import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_DESCRIPTION;
import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_ID;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Resident VID controller class.
 * 
 * @Author : Monobikash Das
 */
@RefreshScope
@RestController
@Tag(name = "resident-vid-controller", description = "Resident Vid Controller")
public class ResidentVidController {

	private static final Logger logger = LoggerConfiguration.logConfig(ResidentVidController.class);

	@Autowired
	private ResidentVidService residentVidService;

	@Autowired
	private RequestValidator validator;

	@Autowired
	private AuditUtil auditUtil;

	@Autowired
	private IdentityServiceImpl identityServiceImpl;
	
	@Value("${resident.vid.policy.id}")
	private String vidPolicyId;
	
	@Value("${resident.vid.version}")
	private String version;
	
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @GetMapping(path = "/vid/policy")
	@Operation(summary = "Retrieve VID policy", description = "Retrieve VID policy", tags = { "Resident Service" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "VID Policy retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResidentVidRequestDto.class)))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public String getVidPolicy() throws ResidentServiceCheckedException {
		logger.debug("ResidentVidController::getVidPolicy()::entry");
		String policyResponse;
		try {
			policyResponse = residentVidService.getVidPolicy();
		} catch (ResidentServiceCheckedException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_VID_POLICY_FAILURE);
			throw new ResidentServiceCheckedException(ResidentErrorCode.POLICY_EXCEPTION);
		}
		auditUtil.setAuditRequestDto(EventEnum.GET_VID_POLICY_SUCCESS);
		logger.debug("ResidentVidController::getVidPolicy()::exit");
		return policyResponse;
	}

	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
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
			throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException {
		return generateVid(requestDto, true);
	}

	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PreAuthorize("@scopeValidator.hasAllScopes("
			+ "@authorizedScopes.getPostgeneratevid()"
		+ ")")
	@PostMapping(path = "/generate-vid", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "generateVid", description = "generateVid", tags = { "Resident Service" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "VID successfully generated", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResidentVidRequestDto.class)))),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "400", description = "Unable to generate VID", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<Object> generateVidV2(@RequestBody(required = true) ResidentVidRequestDtoV2 requestDto)
			throws ResidentServiceCheckedException, OtpValidationFailedException, ApisResourceAccessException {
		return generateVidV2Version(requestDto, false);
	}

	private ResponseEntity<Object> generateVid(IVidRequestDto<?> requestDto, boolean isOtpValidationRequired)
			throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException {
		logger.debug("ResidentVidController::generateVid()::entry");
		String residentIndividualId = !(requestDto.getRequest() instanceof VidRequestDto)? null : ((VidRequestDto)requestDto.getRequest()).getIndividualId();
		if(residentIndividualId == null && requestDto.getRequest() != null) {
			residentIndividualId = getResidentIndividualId();
		}
		validator.validateVidCreateRequest(requestDto, isOtpValidationRequired, residentIndividualId);
		ResponseWrapper<VidResponseDto> vidResponseDto = residentVidService.generateVid(requestDto.getRequest(), residentIndividualId);
		auditUtil.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.GENERATE_VID_SUCCESS,
				residentIndividualId));
		logger.debug("ResidentVidController::generateVid()::exit");
		return ResponseEntity.ok().body(vidResponseDto);
	}
	
	private ResponseEntity<Object> generateVidV2Version(IVidRequestDto<?> requestDto, boolean isOtpValidationRequired)
			throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException {
		logger.debug("ResidentVidController::generateVidV2Version()::entry");
		String residentIndividualId = !(requestDto.getRequest() instanceof VidRequestDto)? null : ((VidRequestDto)requestDto.getRequest()).getIndividualId();
		if(residentIndividualId == null && requestDto.getRequest() != null) {
			residentIndividualId = getResidentIndividualId();
		}
		validator.validateVidCreateV2Request(requestDto, isOtpValidationRequired, residentIndividualId);
		Tuple2<ResponseWrapper<VidResponseDto>, String> tupleResponse = residentVidService.generateVidV2(requestDto.getRequest(), residentIndividualId);
		auditUtil.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.GENERATE_VID_SUCCESS,
				residentIndividualId));
		logger.debug("ResidentVidController::generateVidV2Version()::exit");
		return ResponseEntity.ok()
				.header(ResidentConstants.EVENT_ID, tupleResponse.getT2())
				.body(tupleResponse.getT1());
	}

	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
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
			throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException, IOException {
		return revokeVid(requestDto, vid, true);
	}

	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PreAuthorize("@scopeValidator.hasAllScopes("
			+ "@authorizedScopes.getPatchrevokevid()"
		+ ")")
	@PatchMapping(path = "/revoke-vid/{vid}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Revoke VID", description = "Revoke VID", tags = { "Resident Service" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "VID successfully revoked", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseWrapper.class)))),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "400", description = "Unable to revoke VID", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<Object> revokeVidV2(
			@RequestBody(required = true) RequestWrapper<VidRevokeRequestDTOV2> requestDto, @PathVariable String vid)
			throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException, IOException {
		return revokeVidV2Version(requestDto, vid, false);
	}

	private ResponseEntity<Object> revokeVid(RequestWrapper<? extends BaseVidRevokeRequestDTO> requestDto, String vid,
			boolean isOtpValidationRequired) throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException, IOException {
		logger.debug("ResidentVidController::revokeVid()::entry");
		String residentIndividualId = !(requestDto.getRequest() instanceof VidRevokeRequestDTO)? null : ((VidRevokeRequestDTO)requestDto.getRequest()).getIndividualId();
				
		if(residentIndividualId == null && requestDto.getRequest() != null) {
			residentIndividualId = getResidentIndividualId();
		}
		if (residentIndividualId !=null && residentIndividualId.equals(vid)) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.VID_VALIDATION);
		}
		validator.validateVidRevokeRequest(requestDto, isOtpValidationRequired, residentIndividualId);
		requestDto.getRequest().setVidStatus(requestDto.getRequest().getVidStatus().toUpperCase());
		ResponseWrapper<VidRevokeResponseDTO> vidResponseDto = residentVidService.revokeVid(requestDto.getRequest(),
				vid, residentIndividualId);
		auditUtil.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REVOKE_VID_SUCCESS,
				residentIndividualId));
		logger.debug("ResidentVidController::revokeVid()::exit");
		return ResponseEntity.ok().body(vidResponseDto);
	}
	
	private ResponseEntity<Object> revokeVidV2Version(RequestWrapper<? extends BaseVidRevokeRequestDTO> requestDto, String vid,
			boolean isOtpValidationRequired) throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException, IOException {
		logger.debug("ResidentVidController::revokeVidV2Version()::entry");
		String residentIndividualId = !(requestDto.getRequest() instanceof VidRevokeRequestDTO)? null : ((VidRevokeRequestDTO)requestDto.getRequest()).getIndividualId();
				
		if(residentIndividualId == null && requestDto.getRequest() != null) {
			residentIndividualId = getResidentIndividualId();
		}
		if (residentIndividualId !=null && residentIndividualId.equals(vid)) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.VID_VALIDATION);
		}
		validator.validateVidRevokeV2Request(requestDto, isOtpValidationRequired, residentIndividualId);
		requestDto.getRequest().setVidStatus(requestDto.getRequest().getVidStatus().toUpperCase());
		Tuple2<ResponseWrapper<VidRevokeResponseDTO>, String> tupleResponse = residentVidService.revokeVidV2(requestDto.getRequest(),
				vid, residentIndividualId);
		auditUtil.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REVOKE_VID_SUCCESS,
				residentIndividualId));
		logger.debug("ResidentVidController::revokeVidV2Version()::exit");
		return ResponseEntity.ok()
				.header(ResidentConstants.EVENT_ID, tupleResponse.getT2())
				.body(tupleResponse.getT1());
	}
	
	private String getResidentIndividualId() throws ApisResourceAccessException {
		return identityServiceImpl.getResidentIndvidualIdFromSession();
	}
	
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PreAuthorize("@scopeValidator.hasAllScopes("
			+ "@authorizedScopes.getGetvids()"
		+ ")")
	@GetMapping(path = "/vids", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "retrieveVids", description = "retrieveVids", tags = { "vid-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<?> retrieveVids(@RequestHeader(name = "time-zone-offset", required = false, defaultValue = "0") int timeZoneOffset,
            @RequestHeader(name = "locale", required = false) String locale) throws ResidentServiceException, ApisResourceAccessException, ResidentServiceCheckedException  {
		logger.debug("ResidentVidController::retrieveVids()::entry");
		ResponseWrapper<List<Map<String, ?>>> retrieveVids = new ResponseWrapper<>();
		String residentIndividualId = getResidentIndividualId();
		try {
			retrieveVids = residentVidService.retrieveVids(residentIndividualId, timeZoneOffset, locale);
		} catch (ResidentServiceException | ApisResourceAccessException | ResidentServiceCheckedException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_VIDS_EXCEPTION);
			e.setMetadata(Map.of(ResidentConstants.REQ_RES_ID, ResidentConstants.GET_VIDS_ID));
			throw e;
		}
		auditUtil.setAuditRequestDto(EventEnum.GET_VIDS_SUCCESS);
		logger.debug("ResidentVidController::retrieveVids()::exit");
		return retrieveVids;
	}
}