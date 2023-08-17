package io.mosip.resident.controller;

import java.io.IOException;
import java.util.List;

import io.mosip.resident.util.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.OrderEnum;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.swagger.annotations.ApiParam;
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
@Tag(name = "proxy-masterdata-controller", description = "Proxy Masterdata Controller")
public class ProxyMasterdataController {

	@Autowired
	private ProxyMasterdataService proxyMasterdataService;

	@Autowired
	private AuditUtil auditUtil;

	@Autowired
	private Utility utility;

	private static final Logger logger = LoggerConfiguration.logConfig(ProxyMasterdataController.class);

	/**
	 * Get valid documents by language code.
	 * 
	 * @param langCode
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@GetMapping("/proxy/masterdata/validdocuments/{langCode}")
	@Operation(summary = "getValidDocumentByLangCode", description = "getValidDocumentByLangCode", tags = {
			"proxy-masterdata-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<?> getValidDocumentByLangCode(@PathVariable("langCode") String langCode)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataController::getValidDocumentByLangCode()::entry");
		ResponseWrapper<?> responseWrapper;
		try {
			responseWrapper = utility.getValidDocumentByLangCode(langCode);
		} catch (ResidentServiceCheckedException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_VALID_DOCUMENT_EXCEPTION);
			throw e;
		}
		auditUtil.setAuditRequestDto(EventEnum.GET_VALID_DOCUMENT_SUCCESS);
		logger.debug("ProxyMasterdataController::getValidDocumentByLangCode()::exit");
		return responseWrapper;
	}

	/**
	 * Get location hierarchy levels by language code.
	 * 
	 * @param langCode
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@GetMapping("/proxy/masterdata/locationHierarchyLevels/{langcode}")
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
		ResponseWrapper<?> responseWrapper;
		try {
			responseWrapper = proxyMasterdataService.getLocationHierarchyLevelByLangCode(langCode);
		} catch (ResidentServiceCheckedException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_LOCATION_HIERARCHY_LEVEL_EXCEPTION);
			throw e;
		}
		auditUtil.setAuditRequestDto(EventEnum.GET_LOCATION_HIERARCHY_LEVEL_SUCCESS);
		logger.debug("ProxyMasterdataController::getLocationHierarchyLevelByLangCode()::exit");
		return responseWrapper;
	}

	/**
	 * Get immediate children by location code and language code.
	 * 
	 * @param locationCode
	 * @param langCode
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@GetMapping("/proxy/masterdata/locations/immediatechildren/{locationcode}/{langcode}")
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
		ResponseWrapper<?> responseWrapper;
		try {
			responseWrapper = proxyMasterdataService.getImmediateChildrenByLocCodeAndLangCode(locationCode, langCode);
		} catch (ResidentServiceCheckedException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_IMMEDIATE_CHILDREN_EXCEPTION);
			throw e;
		}
		auditUtil.setAuditRequestDto(EventEnum.GET_IMMEDIATE_CHILDREN_SUCCESS);
		logger.debug("ProxyMasterdataController::getImmediateChildrenByLocCodeAndLangCode()::exit");
		return responseWrapper;
	}

	/**
	 * Get location details by location code and language code.
	 * 
	 * @param locationCode
	 * @param langCode
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@GetMapping("/proxy/masterdata/locations/info/{locationcode}/{langcode}")
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
		ResponseWrapper<?> responseWrapper;
		try {
			responseWrapper = proxyMasterdataService.getLocationDetailsByLocCodeAndLangCode(locationCode, langCode);
		} catch (ResidentServiceCheckedException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_LOCATION_DETAILS_EXCEPTION);
			throw e;
		}
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
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@GetMapping("/proxy/masterdata/getcoordinatespecificregistrationcenters/{langcode}/{longitude}/{latitude}/{proximitydistance}")
	@Operation(summary = "getCoordinateSpecificRegistrationCenters", description = "getCoordinateSpecificRegistrationCenters", tags = {
			"proxy-masterdata-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<?> getCoordinateSpecificRegistrationCenters(@PathVariable("langcode") String langCode,
			@PathVariable("longitude") double longitude, @PathVariable("latitude") double latitude,
			@PathVariable("proximitydistance") int proximityDistance) throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataController::getCoordinateSpecificRegistrationCenters()::entry");
		ResponseWrapper<?> responseWrapper;
		try {
			responseWrapper = proxyMasterdataService.getCoordinateSpecificRegistrationCenters(langCode,
					longitude, latitude, proximityDistance);
		} catch (ResidentServiceCheckedException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_COORDINATE_SPECIFIC_REG_CENTERS_EXCEPTION);
			throw e;
		}
		auditUtil.setAuditRequestDto(EventEnum.GET_COORDINATE_SPECIFIC_REG_CENTERS_SUCCESS);
		logger.debug("ProxyMasterdataController::getCoordinateSpecificRegistrationCenters()::exit");
		return responseWrapper;
	}

	/**
	 * Get applicant valid document.
	 * 
	 * @param applicantId
	 * @param languages
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@GetMapping("/proxy/masterdata/applicanttype/{applicantId}/languages")
	@Operation(summary = "getApplicantValidDocument", description = "getApplicantValidDocument", tags = {
			"proxy-masterdata-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<?> getApplicantValidDocument(@PathVariable("applicantId") String applicantId,
			@RequestParam("languages") List<String> languages) throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataController::getApplicantValidDocument()::entry");
		ResponseWrapper<?> responseWrapper;
		try {
			responseWrapper = proxyMasterdataService.getApplicantValidDocument(applicantId, languages);
		} catch (ResidentServiceCheckedException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_APPLICANT_VALID_DOCUMENT_EXCEPTION);
			throw e;
		}
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
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@GetMapping("/proxy/masterdata/registrationcenters/{langcode}/{hierarchylevel}/names")
	@Operation(summary = "getRegistrationCentersByHierarchyLevel", description = "getRegistrationCentersByHierarchyLevel", tags = {
			"proxy-masterdata-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<?> getRegistrationCentersByHierarchyLevel(@PathVariable("langcode") String langCode,
			@PathVariable("hierarchylevel") Short hierarchyLevel, @RequestParam("name") List<String> name)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataController::getRegistrationCentersByHierarchyLevel()::entry");
		ResponseWrapper<?> responseWrapper;
		try {
			responseWrapper = proxyMasterdataService.getRegistrationCentersByHierarchyLevel(langCode,
					hierarchyLevel, name);
		} catch (ResidentServiceCheckedException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_REG_CENTERS_FOR_LOCATION_CODE_EXCEPTION);
			throw e;
		}
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
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@GetMapping("/proxy/masterdata/registrationcenters/page/{langcode}/{hierarchylevel}/{name}")
	@Operation(summary = "getRegistrationCenterByHierarchyLevelAndTextPaginated", description = "getRegistrationCenterByHierarchyLevelAndTextPaginated", tags = {
			"proxy-masterdata-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<?> getRegistrationCenterByHierarchyLevelAndTextPaginated(
			@PathVariable("langcode") String langCode, @PathVariable("hierarchylevel") Short hierarchyLevel,
			@PathVariable("name") String name,
			@RequestParam(name = "pageNumber", defaultValue = "0") @ApiParam(value = "page no for the requested data", defaultValue = "0") int pageNumber,
			@RequestParam(name = "pageSize", defaultValue = "10") @ApiParam(value = "page size for the requested data", defaultValue = "10") int pageSize,
			@RequestParam(name = "sortBy", defaultValue = "createdDateTime") @ApiParam(value = "sort the requested data based on param value", defaultValue = "createdDateTime") String sortBy,
			@RequestParam(name = "orderBy", defaultValue = "desc") @ApiParam(value = "order the requested data based on param", defaultValue = "desc") OrderEnum orderBy)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataController::getRegistrationCenterByHierarchyLevelAndTextPaginated()::entry");
		ResponseWrapper<?> responseWrapper;
		try {
			responseWrapper = proxyMasterdataService.getRegistrationCenterByHierarchyLevelAndTextPaginated(langCode, hierarchyLevel, name, pageNumber,
					pageSize, orderBy, sortBy);
		} catch (ResidentServiceCheckedException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_REG_CENTERS_PAGINATED_EXCEPTION);
			throw e;
		}
		auditUtil.setAuditRequestDto(EventEnum.GET_REG_CENTERS_PAGINATED_SUCCESS);
		logger.debug("ProxyMasterdataController::getRegistrationCenterByHierarchyLevelAndTextPaginated()::exit");
		return responseWrapper;
	}

	/**
	 * Get registration center working days by registration center ID.
	 * 
	 * @param registrationCenterID
	 * @param langCode
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@GetMapping("/proxy/masterdata/workingdays/{registrationCenterID}/{langCode}")
	@Operation(summary = "getRegistrationCenterWorkingDays", description = "getRegistrationCenterWorkingDays", tags = {
			"proxy-masterdata-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<?> getRegistrationCenterWorkingDays(
			@PathVariable("registrationCenterID") String registrationCenterID,
			@PathVariable("langCode") String langCode) throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataController::getRegistrationCenterWorkingDays()::entry");
		ResponseWrapper<?> responseWrapper;
		try {
			responseWrapper = proxyMasterdataService.getRegistrationCenterWorkingDays(registrationCenterID, langCode);
		} catch (ResidentServiceCheckedException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_REG_CENTER_WORKING_DAYS_EXCEPTION);
			throw e;
		}
		auditUtil.setAuditRequestDto(EventEnum.GET_REG_CENTER_WORKING_DAYS_SUCCESS);
		logger.debug("ProxyMasterdataController::getRegistrationCenterWorkingDays()::exit");
		return responseWrapper;
	}

	/**
	 * Get latest ID schema.
	 * 
	 * @param schemaVersion
	 * @param domain
	 * @param type
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@GetMapping("/proxy/masterdata/idschema/latest")
	@Operation(summary = "getLatestIdSchema", description = "getLatestIdSchema", tags = {
			"proxy-masterdata-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<?> getLatestIdSchema(
			@RequestParam(name = "schemaVersion", defaultValue = "0", required = false) @ApiParam(value = "schema version", defaultValue = "0") double schemaVersion,
			@RequestParam(name = "domain", required = false) @ApiParam(value = "domain of the ui spec") String domain,
			@RequestParam(name = "type", required = false) @ApiParam(value = "type of the ui spec. Supported comma separted values") String type)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataController::getLatestIdSchema()::entry");
		ResponseWrapper<?> responseWrapper;
		try {
			responseWrapper = proxyMasterdataService.getLatestIdSchema(schemaVersion, domain, type);
		} catch (ResidentServiceCheckedException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_LATEST_ID_SCHEMA_EXCEPTION);
			throw e;
		}
		auditUtil.setAuditRequestDto(EventEnum.GET_LATEST_ID_SCHEMA_SUCCESS);
		logger.debug("ProxyMasterdataController::getLatestIdSchema()::exit");
		return responseWrapper;
	}
	
	/**
	 * Get templates by language code and template type code.
	 * 
	 * @param langCode
	 * @param templateTypeCode
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@GetMapping("/auth-proxy/masterdata/templates/{langcode}/{templatetypecode}")
	@Operation(summary = "getAllTemplateBylangCodeAndTemplateTypeCode", description = "getAllTemplateBylangCodeAndTemplateTypeCode", tags = {
			"proxy-masterdata-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<?> getAllTemplateBylangCodeAndTemplateTypeCode(@PathVariable("langcode") String langCode,
			@PathVariable("templatetypecode") String templateTypeCode) throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataController::getAllTemplateBylangCodeAndTemplateTypeCode()::entry");
		ResponseWrapper<?> responseWrapper;
		try {
			responseWrapper = proxyMasterdataService.getAllTemplateBylangCodeAndTemplateTypeCode(langCode, templateTypeCode);
		} catch (ResidentServiceCheckedException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_TEMPLATES_EXCEPTION);
			throw e;
		}
		auditUtil.setAuditRequestDto(EventEnum.GET_TEMPLATES_SUCCESS);
		logger.debug("ProxyMasterdataController::getAllTemplateBylangCodeAndTemplateTypeCode()::exit");
		return responseWrapper;
	}
	
	/**
	 * Get gender types by language code.
	 * 
	 * @param langCode
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@GetMapping("/auth-proxy/masterdata/dynamicfields/{fieldName}/{langCode}")
	@Operation(summary = "getDynamicFieldBasedOnLangCodeAndFieldName", description = "Service to fetch  dynamic field based on langcode and field name", tags = {
			"proxy-masterdata-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<?> getDynamicFieldBasedOnLangCodeAndFieldName(@PathVariable("fieldName") String fieldName,
																		 @PathVariable("langCode") String langCode,
																		 @RequestParam(value = "withValue", defaultValue = "false") boolean withValue)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataController::getDynamicFieldBasedOnLangCodeAndFieldName()::entry");
		ResponseWrapper<?> responseWrapper;
		try {
			responseWrapper = proxyMasterdataService.getDynamicFieldBasedOnLangCodeAndFieldName(fieldName, langCode, withValue);
		} catch (ResidentServiceCheckedException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_DYNAMIC_FIELD_BASED_ON_LANG_CODE_AND_FIELD_NAME_EXCEPTION);
			throw e;
		}
		auditUtil.setAuditRequestDto(EventEnum.GET_DYNAMIC_FIELD_BASED_ON_LANG_CODE_AND_FIELD_NAME_SUCCESS);
		logger.debug("ProxyMasterdataController::getDynamicFieldBasedOnLangCodeAndFieldName()::exit");
		return responseWrapper;
	}
	
	/**
	 * Get document types by document category code & language code.
	 * 
	 * @param langCode
	 * @param documentcategorycode 
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@GetMapping("/proxy/masterdata/documenttypes/{documentcategorycode}/{langcode}")
	@Operation(summary = "getDocumentTypesByDocumentCategoryLangCode", description = "getDocumentTypesByDocumentCategoryLangCode", tags = {
			"proxy-masterdata-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<?> getDocumentTypesByDocumentCategoryAndLangCode(@PathVariable("documentcategorycode") String documentcategorycode,@PathVariable("langcode") String langCode)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataController::getDocumentTypesByDocumentCategoryLangCode::entry");
		ResponseWrapper<?> responseWrapper;
		try {
			responseWrapper = proxyMasterdataService.getDocumentTypesByDocumentCategoryAndLangCode(documentcategorycode,langCode);
		} catch (ResidentServiceCheckedException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_DOCUMENT_TYPES_EXCEPTION);
			throw e;
		}
		auditUtil.setAuditRequestDto(EventEnum.GET_DOCUMENT_TYPES_SUCCESS);
		logger.debug("ProxyMasterdataController::getDocumentTypesByDocumentCategoryLangCode::exit");
		return responseWrapper;
	}
	
	/**
	 * Get gender code by gender type & language code.
	 * 
	 * @param langCode
	 * @param gendertype 
	 * @return ResponseWrapper object
	 * @throws IOException 
	 */
	@ResponseFilter
	@GetMapping("/proxy/masterdata/gendercode/{gendertype}/{langcode}")
	@Operation(summary = "getGenderCodeByGenderTypeAndLangCode", description = "getGenderCodeByGenderTypeAndLangCode", tags = {
			"proxy-masterdata-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<?> getGenderCodeByGenderTypeAndLangCode(@PathVariable("gendertype") String gendertype,@PathVariable("langcode") String langCode)
			throws ResidentServiceCheckedException, IOException {
		logger.debug("ProxyMasterdataController::getGenderCodeByGenderTypeAndLangCode::entry");
		ResponseWrapper<?> responseWrapper;
		try {
			responseWrapper = proxyMasterdataService.getGenderCodeByGenderTypeAndLangCode(gendertype,langCode);
		} catch (ResidentServiceCheckedException | IOException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_GENDER_CODE_EXCEPTION);
			throw e;
		}
		auditUtil.setAuditRequestDto(EventEnum.GET_GENDER_CODE_SUCCESS);
		logger.debug("ProxyMasterdataController::getGenderCodeByGenderTypeAndLangCode::exit");
		return responseWrapper;
	}

}
