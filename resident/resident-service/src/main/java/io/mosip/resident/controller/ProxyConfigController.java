package io.mosip.resident.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.handler.service.ResidentConfigService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
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
		auditUtil.setAuditRequestDto(EventEnum.GET_CONFIGURATION_PROPERTIES);
		ResponseWrapper<?> propertiesResponse = residentConfigService.getUIProperties();
		auditUtil.setAuditRequestDto(EventEnum.GET_CONFIGURATION_PROPERTIES_SUCCESS);
		logger.debug("ProxyConfigController::getResidentProperties()::exit");
		return propertiesResponse;
	}

	@GetMapping("/auth-proxy/config/ui-schema/{schemaType}")
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
		auditUtil.setAuditRequestDto(EventEnum.GET_CONFIGURATION_PROPERTIES);
		String propertiesResponse = residentConfigService.getUISchema(schemaType);
		auditUtil.setAuditRequestDto(EventEnum.GET_CONFIGURATION_PROPERTIES_SUCCESS);
		logger.debug("ProxyConfigController::getResidentUISchema()::exit");
		return propertiesResponse;
	}

	@GetMapping("/auth-proxy/config/identity-mapping")
	@Operation(summary = "getIdentityMapping", description = "Get the identity-mapping", tags = {
			"proxy-config-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public String getIdentityMapping() throws ResidentServiceCheckedException {
		logger.debug("ProxyConfigController::getIdentityMapping()::entry");
		auditUtil.setAuditRequestDto(EventEnum.GET_CONFIGURATION_PROPERTIES);
		String propertiesResponse=residentConfigService.getIdentityMapping();
		auditUtil.setAuditRequestDto(EventEnum.GET_CONFIGURATION_PROPERTIES_SUCCESS);
		logger.debug("ProxyConfigController::getIdentityMapping()::exit");
		return propertiesResponse;
	}

}
