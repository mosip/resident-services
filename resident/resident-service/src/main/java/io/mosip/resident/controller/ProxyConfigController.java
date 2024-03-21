package io.mosip.resident.controller;

import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_DESCRIPTION;
import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_ID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.annotation.Timed;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.handler.service.ResidentConfigService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.AuditEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * The Class ProxyConfigController.
 * 
 * @author Loganathan. S
 */
@RestController
@Tag(name = "proxy-config-controller", description = "Proxy Config Controller")
public class ProxyConfigController {

	/** The Constant logger. */
	private static final Logger logger = LoggerConfiguration.logConfig(ProxyConfigController.class);

	/** The audit util. */
	@Autowired
	private AuditUtil auditUtil;

	@Autowired
	private ResidentConfigService residentConfigService;

	/**
	 * Gets the resident properties.
	 *
	 * @return the resident properties
	 * @throws ResidentServiceCheckedException the resident service checked
	 *                                         exception
	 */
	@ResponseFilter
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @GetMapping("/proxy/config/ui-properties")
	@Operation(summary = "getResidentUIProperties", description = "Get the Resident-UI properties", tags = {
			"proxy-config-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<?> getResidentProperties() throws ResidentServiceCheckedException {
		logger.debug("ProxyConfigController::getResidentProperties()::entry");
		ResponseWrapper<?> propertiesResponse = residentConfigService.getUIProperties();
		auditUtil.setAuditRequestDto(AuditEnum.GET_CONFIGURATION_PROPERTIES_SUCCESS);
		logger.debug("ProxyConfigController::getResidentProperties()::exit");
		return propertiesResponse;
	}

	@GetMapping("/auth-proxy/config/ui-schema/{schemaType}")
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @Operation(summary = "getResidentUISchema", description = "Get the Resident-UI Schema", tags = {
			"proxy-config-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public String getResidentUISchema(
			@PathVariable String schemaType) throws ResidentServiceCheckedException {
		logger.debug("ProxyConfigController::getResidentUISchema()::entry");
		String propertiesResponse;
		try {
			propertiesResponse = residentConfigService.getUISchema(schemaType);
		} catch (ResidentServiceException e) {
			auditUtil.setAuditRequestDto(AuditEnum.GET_CONFIGURATION_PROPERTIES_EXCEPTION);
			throw e;
		}
		auditUtil.setAuditRequestDto(AuditEnum.GET_CONFIGURATION_PROPERTIES_SUCCESS);
		logger.debug("ProxyConfigController::getResidentUISchema()::exit");
		return propertiesResponse;
	}

	@GetMapping("/auth-proxy/config/identity-mapping")
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @Operation(summary = "getIdentityMapping", description = "Get the identity-mapping", tags = {
			"proxy-config-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public String getIdentityMapping() throws ResidentServiceCheckedException {
		logger.debug("ProxyConfigController::getIdentityMapping()::entry");
		String propertiesResponse=residentConfigService.getIdentityMapping();
		auditUtil.setAuditRequestDto(AuditEnum.GET_CONFIGURATION_PROPERTIES_SUCCESS);
		logger.debug("ProxyConfigController::getIdentityMapping()::exit");
		return propertiesResponse;
	}

}
