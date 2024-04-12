package io.mosip.resident.controller;

import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_DESCRIPTION;
import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_ID;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.annotation.Timed;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ProxyPartnerManagementService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.AuditEnum;
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
	
	@Autowired
	private Environment env;

	private static final Logger logger = LoggerConfiguration.logConfig(ProxyPartnerManagementController.class);

	/**
	 * Get partners by partner type.
	 * 
	 * @param partnerType
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @RequestMapping(method = RequestMethod.GET)
	@Operation(summary = "getPartnersByPartnerType", description = "getPartnersByPartnerType", tags = {
			"proxy-partner-management-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<?> getPartnersByPartnerType(@RequestParam(name = "partnerType", required = false) String partnerType)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyPartnerManagementController::getPartnersByPartnerType()::entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		try {
			responseWrapper = proxyPartnerManagementService.getPartnersByPartnerType(partnerType);
		} catch (ResidentServiceCheckedException e) {
			auditUtil.setAuditRequestDto(AuditEnum.GET_PARTNERS_BY_PARTNER_TYPE_EXCEPTION);
			e.setMetadata(
					Map.of(ResidentConstants.REQ_RES_ID, env.getProperty(ResidentConstants.AUTH_PROXY_PARTNERS_ID)));
			throw e;
		}
		auditUtil.setAuditRequestDto(AuditEnum.GET_PARTNERS_BY_PARTNER_TYPE_SUCCESS);
		logger.debug("ProxyPartnerManagementController::getPartnersByPartnerType()::exit");
		return responseWrapper;
	}

}
