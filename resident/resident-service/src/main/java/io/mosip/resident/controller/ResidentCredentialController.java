package io.mosip.resident.controller;

import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_DESCRIPTION;
import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_ID;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.micrometer.core.annotation.Timed;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.UISchemaTypes;
import io.mosip.resident.dto.CredentialCancelRequestResponseDto;
import io.mosip.resident.dto.CredentialRequestStatusResponseDto;
import io.mosip.resident.dto.CredentialTypeResponse;
import io.mosip.resident.dto.PartnerCredentialTypePolicyDto;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentCredentialRequestDto;
import io.mosip.resident.dto.ResidentCredentialResponseDto;
import io.mosip.resident.dto.ResidentCredentialResponseDtoV2;
import io.mosip.resident.dto.SharableAttributesDTO;
import io.mosip.resident.dto.ShareCredentialRequestDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentCredentialServiceException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.ResidentCredentialService;
import io.mosip.resident.service.impl.ResidentConfigServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.AuditEnum;
import io.mosip.resident.validator.RequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.util.function.Tuple2;

@RestController
@Tag(name = "resident-credential-controller", description = "Resident Credential Controller")
public class ResidentCredentialController {

	@Autowired
	private RequestValidator validator;

	@Autowired
	private ResidentCredentialService residentCredentialService;
	
	@Autowired
	private ResidentConfigServiceImpl residentConfigService;

	@Autowired
	private AuditUtil audit;
	
	@Value("${resident.share.credential.id}")
	private String shareCredentialId;
	
	@Value("${resident.share.credential.version}")
	private String shareCredentialVersion;

	private static final Logger logger = LoggerConfiguration.logConfig(ResidentCredentialController.class);

	@ResponseFilter
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PostMapping(value = "/req/credential")
	@Operation(summary = "reqCredential", description = "reqCredential", tags = { "resident-credential-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<Object> reqCredential(@RequestBody RequestWrapper<ResidentCredentialRequestDto> requestDTO)
			throws ResidentServiceCheckedException {
		logger.debug("ResidentCredentialController::reqCredential()::entry");
		ResponseWrapper<ResidentCredentialResponseDto> response = new ResponseWrapper<>();
		try {
			validator.validateReqCredentialRequest(requestDTO);
			response.setResponse(residentCredentialService.reqCredential(requestDTO.getRequest()));
		} catch (InvalidInputException | ResidentServiceException | ResidentCredentialServiceException | ResidentServiceCheckedException e) {
			audit.setAuditRequestDto(AuditEnum.CREDENTIAL_REQ_EXCEPTION);
			e.setMetadata(Map.of(ResidentConstants.REQ_RES_ID, ResidentConstants.CREDENTIAL_STORE_ID));
			throw e;
		}
		audit.setAuditRequestDto(AuditEnum.CREDENTIAL_REQ_SUCCESS);
		logger.debug("ResidentCredentialController::reqCredential()::exit");
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@ResponseFilter
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PreAuthorize("@scopeValidator.hasAllScopes(" + "@authorizedScopes.getPostRequestShareCredWithPartner()" + ")")
	@PostMapping(value = "/share-credential")
	@Operation(summary = "requestShareCredWithPartner", description = "requestShareCredWithPartner", tags = { "resident-credential-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<Object> requestShareCredWithPartner(
			@RequestBody RequestWrapper<ShareCredentialRequestDto> requestDTO)
			throws ResidentServiceCheckedException, ApisResourceAccessException, JsonParseException, JsonMappingException, IOException {
		logger.debug("ResidentCredentialController::requestShareCredWithPartner()::entry");
		ResponseWrapper<ResidentCredentialResponseDtoV2> response = new ResponseWrapper<>();
		Tuple2<ResidentCredentialResponseDtoV2, String> tupleResponse;
		try {
			validator.validateShareCredentialRequest(requestDTO);
			String purpose = requestDTO.getRequest().getPurpose();
			RequestWrapper<ResidentCredentialRequestDto> request = new RequestWrapper<ResidentCredentialRequestDto>();
			ResidentCredentialRequestDto credentialRequestDto = new ResidentCredentialRequestDto();
			credentialRequestDto.setIssuer(requestDTO.getRequest().getPartnerId());
			credentialRequestDto.setConsent(requestDTO.getRequest().getConsent());
			request.setRequest(credentialRequestDto);
			buildAdditionalMetadata(requestDTO, request);
			tupleResponse = residentCredentialService.shareCredential(request.getRequest(), purpose,
					requestDTO.getRequest().getSharableAttributes());
		} catch (InvalidInputException | ResidentServiceCheckedException | ResidentCredentialServiceException e) {
			audit.setAuditRequestDto(AuditEnum.CREDENTIAL_REQ_EXCEPTION);
			e.setMetadata(Map.of(ResidentConstants.REQ_RES_ID, shareCredentialId));
			throw e;
		}
		response.setId(shareCredentialId);
		response.setVersion(shareCredentialVersion);
		response.setResponse(tupleResponse.getT1());
		audit.setAuditRequestDto(AuditEnum.CREDENTIAL_REQ_SUCCESS);
		logger.debug("ResidentCredentialController::requestShareCredWithPartner()::exit");
		return ResponseEntity.status(HttpStatus.OK).header(ResidentConstants.EVENT_ID, tupleResponse.getT2())
				.body(response);
	}
	
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @GetMapping(value = "req/credential/status/{requestId}")
	@Operation(summary = "getCredentialStatus", description = "getCredentialStatus", tags = { "resident-credential-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<Object> getCredentialStatus(@PathVariable("requestId") String requestId)
			throws ResidentServiceCheckedException {
		logger.debug("ResidentCredentialController::getCredentialStatus()::entry");
		ResponseWrapper<CredentialRequestStatusResponseDto> response = new ResponseWrapper<>();
		try {
			response.setResponse(residentCredentialService.getStatus(requestId));
		} catch (ResidentCredentialServiceException e) {
			audit.setAuditRequestDto(AuditEnum.CREDENTIAL_REQ_STATUS_EXCEPTION);
			throw e;
		}
		audit.setAuditRequestDto(AuditEnum.CREDENTIAL_REQ_STATUS_SUCCESS);
		logger.debug("ResidentCredentialController::getCredentialStatus()::exit");
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @GetMapping(value = "req/card/{requestId}")
	@Operation(summary = "getCard", description = "getCard", tags = { "resident-credential-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<Object> getCard(@PathVariable("requestId") String requestId)
			throws Exception {
		logger.debug("ResidentCredentialController::getCard()::entry");
		byte[] pdfBytes;
		try {
			pdfBytes = residentCredentialService.getCard(requestId);
		} catch (ResidentCredentialServiceException e) {
			audit.setAuditRequestDto(AuditEnum.REQ_CARD_EXCEPTION);
			throw e;
		}
		InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(pdfBytes));
		audit.setAuditRequestDto(AuditEnum.REQ_CARD_SUCCESS);
		logger.debug("ResidentCredentialController::getCard()::exit");
		return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/pdf"))
				.header("Content-Disposition", "attachment; filename=\"" + requestId + ".pdf\"")
				.body((Object) resource);
	}
	
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @GetMapping(value = "credential/types")
	@Operation(summary = "getCredentialTypes", description = "getCredentialTypes", tags = { "resident-credential-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<Object> getCredentialTypes()
			throws ResidentServiceCheckedException {
		logger.debug("ResidentCredentialController::getCredentialTypes()::entry");
		ResponseWrapper<CredentialTypeResponse> response = new ResponseWrapper<>();
		try {
			response.setResponse(residentCredentialService.getCredentialTypes());
		} catch (ResidentCredentialServiceException e) {
			audit.setAuditRequestDto(AuditEnum.CREDENTIAL_TYPES_EXCEPTION);
			throw e;
		}
		audit.setAuditRequestDto(AuditEnum.CREDENTIAL_TYPES_SUCCESS);
		logger.debug("ResidentCredentialController::getCredentialTypes()::exit");
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}


	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @GetMapping(value = "req/credential/cancel/{requestId}")
	@Operation(summary = "cancelCredentialRequest", description = "cancelCredentialRequest", tags = { "resident-credential-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<Object> cancelCredentialRequest(@PathVariable("requestId") String requestId)
			throws ResidentServiceCheckedException {
		logger.debug("ResidentCredentialController::cancelCredentialRequest()::entry");
		ResponseWrapper<CredentialCancelRequestResponseDto> response = new ResponseWrapper<>();
		try {
			response.setResponse(residentCredentialService.cancelCredentialRequest(requestId));
		} catch (ResidentCredentialServiceException e) {
			audit.setAuditRequestDto(AuditEnum.CREDENTIAL_CANCEL_REQ_EXCEPTION);
			throw e;
		}
		audit.setAuditRequestDto(AuditEnum.CREDENTIAL_CANCEL_REQ_SUCCESS);
		logger.debug("ResidentCredentialController::cancelCredentialRequest()::exit");
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @GetMapping(value = "req/policy/partnerId/{partnerId}/credentialType/{credentialType}")
	@Operation(summary = "getPolicyByCredentialType", description = "getPolicyByCredentialType", tags = { "resident-credential-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<Object> getPolicyByCredentialType(@PathVariable @Valid String partnerId,
			@PathVariable @Valid String credentialType) throws ResidentServiceCheckedException {
		logger.debug("ResidentCredentialController::getPolicyByCredentialType()::entry");
		io.mosip.resident.dto.ResponseWrapper<PartnerCredentialTypePolicyDto> response;
		try {
			response = residentCredentialService.getPolicyByCredentialType(partnerId, credentialType);
		} catch (ResidentCredentialServiceException e) {
			audit.setAuditRequestDto(AuditEnum.REQ_POLICY_EXCEPTION);
			throw e;
		}
		audit.setAuditRequestDto(AuditEnum.REQ_POLICY_SUCCESS);
		logger.debug("ResidentCredentialController::getPolicyByCredentialType()::exit");
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	private void buildAdditionalMetadata(RequestWrapper<ShareCredentialRequestDto> requestDTO,
			RequestWrapper<ResidentCredentialRequestDto> request)
			throws JsonParseException, JsonMappingException, ResidentServiceCheckedException, IOException {
		List<String> sharableAttr = residentConfigService.getSharableAttributesList(
				requestDTO.getRequest().getSharableAttributes(), UISchemaTypes.SHARE_CREDENTIAL.getFileIdentifier());
		if (Objects.nonNull(requestDTO.getRequest().getSharableAttributes())) {
			request.getRequest().setSharableAttributes(sharableAttr);
			Map<String, String> formattingAttributes = requestDTO.getRequest().getSharableAttributes()
															.stream()
															.filter(attrib -> attrib.getFormat() != null && !attrib.getFormat().isEmpty())
															.collect(Collectors.toMap(SharableAttributesDTO::getAttributeName, SharableAttributesDTO::getFormat));
			request.getRequest()
					.setAdditionalData(Map.of("formatingAttributes", formattingAttributes,
							"maskingAttributes",
							requestDTO.getRequest().getSharableAttributes().stream()
									.filter(attr -> attr.isMasked())
									.map(attr -> attr.getAttributeName())
									.collect(Collectors.toList())));

		}
	}
	
}
