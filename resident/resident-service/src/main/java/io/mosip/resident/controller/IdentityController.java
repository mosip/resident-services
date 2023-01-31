package io.mosip.resident.controller;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.IdentityService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.validator.RequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/identity")
@Tag(name = "identity-controller", description = "IdentityController Controller")
public class IdentityController {
	
	/** The Constant logger. */
	private static final Logger logger = LoggerConfiguration.logConfig(IdentityController.class);

	/** The audit util. */
	@Autowired
	private AuditUtil auditUtil;
	
	@Autowired
	private IdentityService idServiceImpl;
	
	@Autowired
	private RequestValidator validator;
	
	@Value("${resident.identity.info.id}")
	private String residentIdentityInfoId;

	@Value("${resident.identity.info.version}")
	private String residentIdentityInfoVersion;
	
	@ResponseFilter
	@PreAuthorize("@scopeValidator.hasAllScopes("
    				+ "@authorizedScopes.getGetinputattributevalues()"
    			+ ")")
	@GetMapping("/info/type/{schemaType}")
	@Operation(summary = "getInputAttributeValues", description = "Get the Resident-UI Schema", tags = {
			"identity-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<Object> getInputAttributeValues(@PathVariable("schemaType") String schemaType)
			throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
		logger.debug("IdentityController::getInputAttributeValues()::entry");
		auditUtil.setAuditRequestDto(EventEnum.GET_INPUT_ATTRIBUTES);
		try {
			validator.validateSchemaType(schemaType);
		} catch (InvalidInputException e) {
			throw new ResidentServiceException(e.getErrorCode(), e.getErrorText(), e,
					Map.of(ResidentConstants.REQ_RES_ID, residentIdentityInfoId));
		}
		ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>();
		String id = getIdFromUser();
		Map<String, ?> propertiesResponse = idServiceImpl.getIdentityAttributes(id, schemaType, List.of());
		auditUtil.setAuditRequestDto(EventEnum.GET_INPUT_ATTRIBUTES_SUCCESS);
		logger.debug("IdentityController::getInputAttributeValues()::exit");
		responseWrapper.setResponse(propertiesResponse);
		responseWrapper.setId(residentIdentityInfoId);
		responseWrapper.setVersion(residentIdentityInfoVersion);

		return responseWrapper;
	}

	private String getIdFromUser() throws ApisResourceAccessException {
		return idServiceImpl.getResidentIndvidualId();
	}
	

}
