package io.mosip.resident.controller;

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

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.RequestIdType;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
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
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ResidentCredentialService;
import io.mosip.resident.service.impl.ResidentConfigServiceImpl;
import io.mosip.resident.service.impl.UISchemaTypes;
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

	@ResponseFilter
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
		audit.setAuditRequestDto(EventEnum.CREDENTIAL_REQ);
		validator.validateReqCredentialRequest(requestDTO);
		ResponseWrapper<ResidentCredentialResponseDto> response = new ResponseWrapper<>();
		response.setResponse(residentCredentialService.reqCredential(requestDTO.getRequest()));
		audit.setAuditRequestDto(EventEnum.CREDENTIAL_REQ_SUCCESS);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@ResponseFilter
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
		validator.validateRequestNewApi(requestDTO, RequestIdType.SHARE_CREDENTIAL);
		validator.validateSharableAttributes(requestDTO.getRequest().getSharableAttributes());
		validator.validatePurpose(requestDTO.getRequest().getPurpose());
		String purpose = requestDTO.getRequest().getPurpose();
		audit.setAuditRequestDto(EventEnum.CREDENTIAL_REQ);
		RequestWrapper<ResidentCredentialRequestDto> request = new RequestWrapper<ResidentCredentialRequestDto>();
		ResidentCredentialRequestDto credentialRequestDto = new ResidentCredentialRequestDto();
		credentialRequestDto.setIssuer(requestDTO.getRequest().getPartnerId());
		credentialRequestDto.setConsent(requestDTO.getRequest().getConsent());
		request.setRequest(credentialRequestDto);
		buildAdditionalMetadata(requestDTO, request);
		ResponseWrapper<ResidentCredentialResponseDtoV2> response = new ResponseWrapper<>();
		Tuple2<ResidentCredentialResponseDtoV2, String> tupleResponse;
		tupleResponse = residentCredentialService.shareCredential(request.getRequest(), purpose);
		response.setId(shareCredentialId);
		response.setVersion(shareCredentialVersion);
		response.setResponse(tupleResponse.getT1());
		audit.setAuditRequestDto(EventEnum.CREDENTIAL_REQ_SUCCESS);
		return ResponseEntity.status(HttpStatus.OK)
				.header(ResidentConstants.EVENT_ID, tupleResponse.getT2())
				.body(response);
	}
	
	@GetMapping(value = "req/credential/status/{requestId}")
	@Operation(summary = "getCredentialStatus", description = "getCredentialStatus", tags = { "resident-credential-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<Object> getCredentialStatus(@PathVariable("requestId") String requestId)
			throws ResidentServiceCheckedException {
		audit.setAuditRequestDto(EventEnum.CREDENTIAL_REQ_STATUS);
		ResponseWrapper<CredentialRequestStatusResponseDto> response = new ResponseWrapper<>();
		response.setResponse(residentCredentialService.getStatus(requestId));
		audit.setAuditRequestDto(EventEnum.CREDENTIAL_REQ_STATUS_SUCCESS);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping(value = "req/card/{requestId}")
	@Operation(summary = "getCard", description = "getCard", tags = { "resident-credential-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<Object> getCard(@PathVariable("requestId") String requestId)
			throws Exception {
		audit.setAuditRequestDto(EventEnum.REQ_CARD);
		byte[] pdfBytes = residentCredentialService.getCard(requestId);
		InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(pdfBytes));
		audit.setAuditRequestDto(EventEnum.REQ_CARD_SUCCESS);
		return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/pdf"))
				.header("Content-Disposition", "attachment; filename=\"" + requestId + ".pdf\"")
				.body((Object) resource);
	}
	
	@GetMapping(value = "credential/types")
	@Operation(summary = "getCredentialTypes", description = "getCredentialTypes", tags = { "resident-credential-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<Object> getCredentialTypes()
			throws ResidentServiceCheckedException {
		audit.setAuditRequestDto(EventEnum.CREDENTIAL_TYPES);
		ResponseWrapper<CredentialTypeResponse> response = new ResponseWrapper<>();
		response.setResponse(residentCredentialService.getCredentialTypes());
		audit.setAuditRequestDto(EventEnum.CREDENTIAL_TYPES_SUCCESS);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}


	@GetMapping(value = "req/credential/cancel/{requestId}")
	@Operation(summary = "cancelCredentialRequest", description = "cancelCredentialRequest", tags = { "resident-credential-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<Object> cancelCredentialRequest(@PathVariable("requestId") String requestId)
			throws ResidentServiceCheckedException {
		audit.setAuditRequestDto(EventEnum.CREDENTIAL_CANCEL_REQ);
		ResponseWrapper<CredentialCancelRequestResponseDto> response = new ResponseWrapper<>();
		response.setResponse(residentCredentialService.cancelCredentialRequest(requestId));
		audit.setAuditRequestDto(EventEnum.CREDENTIAL_CANCEL_REQ_SUCCESS);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping(value = "req/policy/partnerId/{partnerId}/credentialType/{credentialType}")
	@Operation(summary = "getPolicyByCredentialType", description = "getPolicyByCredentialType", tags = { "resident-credential-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<Object> getPolicyByCredentialType(@PathVariable @Valid String partnerId,
			@PathVariable @Valid String credentialType) throws ResidentServiceCheckedException {
		audit.setAuditRequestDto(EventEnum.REQ_POLICY);
		io.mosip.resident.dto.ResponseWrapper<PartnerCredentialTypePolicyDto> response = residentCredentialService
				.getPolicyByCredentialType(partnerId, credentialType);
		audit.setAuditRequestDto(EventEnum.REQ_POLICY_SUCCESS);
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
