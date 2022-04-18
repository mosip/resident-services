package io.mosip.resident.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
	 * @param langCode
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
	public ResponseWrapper<?> getLocationHierarchyLevelByLangCode(@PathVariable("langcode") String langCode)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataController::getLocationHierarchyLevelByLangCode()::entry");
		auditUtil.setAuditRequestDto(EventEnum.GET_LOCATION_HIERARCHY_LEVEL);
		ResponseWrapper<?> responseWrapper = proxyMasterdataService.getLocationHierarchyLevelByLangCode(langCode);
		auditUtil.setAuditRequestDto(EventEnum.GET_LOCATION_HIERARCHY_LEVEL_SUCCESS);
		logger.debug("ProxyMasterdataController::getLocationHierarchyLevelByLangCode()::exit");
		return responseWrapper;
	}

	/**
	 * Get immediate children by location code and language code.
	 * 
	 * @param locationCode
	 * @param langCode
	 * @return ResponseWrapper<?> object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@GetMapping("/locations/immediatechildren/{locationcode}/{langcode}")
	@Operation(summary = "getImmediateChildrenByLocCodeAndLangCode", description = "getImmediateChildrenByLocCodeAndLangCode", tags = {
			"proxy-masterdata-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<?> getImmediateChildrenByLocCodeAndLangCode(
			@PathVariable("locationcode") String locationCode, @PathVariable("langcode") String langCode)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataController::getImmediateChildrenByLocCodeAndLangCode()::entry");
		auditUtil.setAuditRequestDto(EventEnum.GET_IMMEDIATE_CHILDREN);
		ResponseWrapper<?> responseWrapper = proxyMasterdataService
				.getImmediateChildrenByLocCodeAndLangCode(locationCode, langCode);
		auditUtil.setAuditRequestDto(EventEnum.GET_IMMEDIATE_CHILDREN_SUCCESS);
		logger.debug("ProxyMasterdataController::getImmediateChildrenByLocCodeAndLangCode()::exit");
		return responseWrapper;
	}

	/**
	 * Get location details by location code and language code.
	 * 
	 * @param locationCode
	 * @param langCode
	 * @return ResponseWrapper<?> object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@GetMapping("/locations/info/{locationcode}/{langcode}")
	@Operation(summary = "getLocationDetailsByLocCodeAndLangCode", description = "getLocationDetailsByLocCodeAndLangCode", tags = {
			"proxy-masterdata-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<?> getLocationDetailsByLocCodeAndLangCode(@PathVariable("locationcode") String locationCode,
			@PathVariable("langcode") String langCode) throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataController::getLocationDetailsByLocCodeAndLangCode()::entry");
		auditUtil.setAuditRequestDto(EventEnum.GET_LOCATION_DETAILS);
		ResponseWrapper<?> responseWrapper = proxyMasterdataService.getLocationDetailsByLocCodeAndLangCode(locationCode,
				langCode);
		auditUtil.setAuditRequestDto(EventEnum.GET_LOCATION_DETAILS_SUCCESS);
		logger.debug("ProxyMasterdataController::getLocationDetailsByLocCodeAndLangCode()::exit");
		return responseWrapper;
	}

	/**
	 * Get coordinate specific registration centers
	 * 
	 * @param langCode
	 * @param longitude
	 * @param latitude
	 * @param proximityDistance
	 * @return ResponseWrapper<?> object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@GetMapping("/getcoordinatespecificregistrationcenters/{langcode}/{longitude}/{latitude}/{proximitydistance}")
	@Operation(summary = "getCoordinateSpecificRegistrationCenters", description = "getCoordinateSpecificRegistrationCenters", tags = {
			"proxy-masterdata-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<?> getCoordinateSpecificRegistrationCenters(@PathVariable("langcode") String langCode,
			@PathVariable("longitude") String longitude, @PathVariable("latitude") String latitude,
			@PathVariable("proximitydistance") String proximityDistance) throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataController::getCoordinateSpecificRegistrationCenters()::entry");
		auditUtil.setAuditRequestDto(EventEnum.GET_COORDINATE_SPECIFIC_REG_CENTERS);
		ResponseWrapper<?> responseWrapper = proxyMasterdataService.getCoordinateSpecificRegistrationCenters(langCode,
				longitude, latitude, proximityDistance);
		auditUtil.setAuditRequestDto(EventEnum.GET_COORDINATE_SPECIFIC_REG_CENTERS_SUCCESS);
		logger.debug("ProxyMasterdataController::getCoordinateSpecificRegistrationCenters()::exit");
		return responseWrapper;
	}

	/**
	 * Get applicant valid document.
	 * 
	 * @param applicantId
	 * @param languages
	 * @return ResponseWrapper<?> object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@GetMapping("/applicanttype/{applicantId}/languages")
	@Operation(summary = "getApplicantValidDocument", description = "getApplicantValidDocument", tags = {
			"proxy-masterdata-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<?> getApplicantValidDocument(@PathVariable("applicantId") String applicantId,
			@RequestParam("languages") String languages) throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataController::getApplicantValidDocument()::entry");
		auditUtil.setAuditRequestDto(EventEnum.GET_APPLICANT_VALID_DOCUMENT);
		ResponseWrapper<?> responseWrapper = proxyMasterdataService.getApplicantValidDocument(applicantId, languages);
		auditUtil.setAuditRequestDto(EventEnum.GET_APPLICANT_VALID_DOCUMENT_SUCCESS);
		logger.debug("ProxyMasterdataController::getApplicantValidDocument()::exit");
		return responseWrapper;
	}

	/**
	 * Get registration centers by hierarchy level.
	 * 
	 * @param langCode
	 * @param hierarchyLevel
	 * @param name
	 * @return ResponseWrapper<?> object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@GetMapping("/registrationcenters/{langcode}/{hierarchylevel}/names")
	@Operation(summary = "getRegistrationCentersByHierarchyLevel", description = "getRegistrationCentersByHierarchyLevel", tags = {
			"proxy-masterdata-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<?> getRegistrationCentersByHierarchyLevel(@PathVariable("langcode") String langCode,
			@PathVariable("hierarchylevel") String hierarchyLevel, @RequestParam("name") String name)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataController::getRegistrationCentersByHierarchyLevel()::entry");
		auditUtil.setAuditRequestDto(EventEnum.GET_REG_CENTERS_FOR_LOCATION_CODE);
		ResponseWrapper<?> responseWrapper = proxyMasterdataService.getRegistrationCentersByHierarchyLevel(langCode,
				hierarchyLevel, name);
		auditUtil.setAuditRequestDto(EventEnum.GET_REG_CENTERS_FOR_LOCATION_CODE_SUCCESS);
		logger.debug("ProxyMasterdataController::getRegistrationCentersByHierarchyLevel()::exit");
		return responseWrapper;
	}

	/**
	 * Get registration centers by hierarchy level and text-paginated.
	 * 
	 * @param langCode
	 * @param hierarchyLevel
	 * @param name
	 * @param pageNumber
	 * @param pageSize
	 * @param orderBy
	 * @param sortBy
	 * @return ResponseWrapper<?> object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@GetMapping("/registrationcenters/page/{langcode}/{hierarchylevel}/{name}")
	@Operation(summary = "getRegistrationCenterByHierarchyLevelAndTextPaginated", description = "getRegistrationCenterByHierarchyLevelAndTextPaginated", tags = {
			"proxy-masterdata-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<?> getRegistrationCenterByHierarchyLevelAndTextPaginated(
			@PathVariable("langcode") String langCode, @PathVariable("hierarchylevel") String hierarchyLevel,
			@PathVariable("name") String name, @RequestParam("pageNumber") String pageNumber,
			@RequestParam("pageSize") String pageSize, @RequestParam("orderBy") String orderBy,
			@RequestParam("sortBy") String sortBy) throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataController::getRegistrationCenterByHierarchyLevelAndTextPaginated()::entry");
		auditUtil.setAuditRequestDto(EventEnum.GET_REG_CENTERS_PAGINATED);
		ResponseWrapper<?> responseWrapper = proxyMasterdataService
				.getRegistrationCenterByHierarchyLevelAndTextPaginated(langCode, hierarchyLevel, name, pageNumber,
						pageSize, orderBy, sortBy);
		auditUtil.setAuditRequestDto(EventEnum.GET_REG_CENTERS_PAGINATED_SUCCESS);
		logger.debug("ProxyMasterdataController::getRegistrationCenterByHierarchyLevelAndTextPaginated()::exit");
		return responseWrapper;
	}

}
