package io.mosip.resident.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ProxyPartnerManagementService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Resident proxy partner management controller class.
 * 
 * @author Ritik Jain
 */
@RestController
@RequestMapping("/auth-proxy/partners")
@Tag(name = "proxy-partner-management-controller", description = "Proxy Partner Management Controller")
public class ProxyPartnerManagementController {

	@Autowired
	private ProxyPartnerManagementService proxyPartnerManagementService;

	@Autowired
	private AuditUtil auditUtil;

	private static final Logger logger = LoggerConfiguration.logConfig(ProxyPartnerManagementController.class);

	/**
	 * Get partners by partner type.
	 * 
	 * @param partnerType
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@RequestMapping(method = RequestMethod.GET)
	@Operation(summary = "getPartnersByPartnerType", description = "getPartnersByPartnerType", tags = {
			"proxy-partner-management-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<?> getPartnersByPartnerType(@RequestParam("partnerType") Optional<String> partnerType)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyPartnerManagementController::getPartnersByPartnerType():: entry");
		auditUtil.setAuditRequestDto(EventEnum.GET_PARTNERS_BY_PARTNER_TYPE);
		ResponseWrapper<?> responseWrapper = proxyPartnerManagementService.getPartnersByPartnerType(partnerType);
		auditUtil.setAuditRequestDto(EventEnum.GET_PARTNERS_BY_PARTNER_TYPE_SUCCESS);
		logger.debug("ProxyPartnerManagementController::getPartnersByPartnerType():: exit");
		return responseWrapper;
	}

}
