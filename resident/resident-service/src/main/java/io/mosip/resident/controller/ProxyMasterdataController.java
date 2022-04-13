package io.mosip.resident.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Resident proxy masterdata controller class.
 * 
 * @author Ritik Jain
 */
@RestController
@RequestMapping("/proxy/masterdata")
@Tag(name = "proxy-masterdata-controller", description = "Proxy Masterdata Controller")
public class ProxyMasterdataController {

	@Autowired
	private ProxyMasterdataService proxyMasterdataService;

	@Autowired
	private AuditUtil auditUtil;

	private static final Logger logger = LoggerConfiguration.logConfig(ProxyMasterdataController.class);

	/**
	 * Get valid documents by language code.
	 * 
	 * @param langCode
	 * @return ResponseWrapper<?> object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@GetMapping("/validdocuments/{langCode}")
	@Operation(summary = "getValidDocumentByLangCode", description = "getValidDocumentByLangCode", tags = {
			"proxy-masterdata-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<?> getValidDocumentByLangCode(@PathVariable("langCode") String langCode)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataController::getValidDocumentByLangCode():: entry");
		auditUtil.setAuditRequestDto(EventEnum.GET_VALID_DOCUMENT);
		ResponseWrapper<?> responseWrapper = proxyMasterdataService.getValidDocumentByLangCode(langCode);
		auditUtil.setAuditRequestDto(EventEnum.GET_VALID_DOCUMENT_SUCCESS);
		logger.debug("ProxyMasterdataController::getValidDocumentByLangCode():: exit");
		return responseWrapper;
	}

	/**
	 * Get location hierarchy levels by language code.
	 * 
	 * @param langcode
	 * @return ResponseWrapper<?> object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@GetMapping("/locationHierarchyLevels/{langcode}")
	@Operation(summary = "getLocationHierarchyLevelByLangCode", description = "getLocationHierarchyLevelByLangCode", tags = {
			"proxy-masterdata-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<?> getLocationHierarchyLevelByLangCode(@PathVariable("langcode") String langcode)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataController::getLocationHierarchyLevelByLangCode()::entry");
		auditUtil.setAuditRequestDto(EventEnum.GET_LOCATION_HIERARCHY_LEVEL);
		ResponseWrapper<?> responseWrapper = proxyMasterdataService.getLocationHierarchyLevelByLangCode(langcode);
		auditUtil.setAuditRequestDto(EventEnum.GET_LOCATION_HIERARCHY_LEVEL_SUCCESS);
		logger.debug("ProxyMasterdataController::getLocationHierarchyLevelByLangCode()::exit");
		return responseWrapper;
	}

}
