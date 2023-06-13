package io.mosip.resident.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.OrderEnum;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.GenderCodeResponseDTO;
import io.mosip.resident.dto.GenderTypeListDTO;
import io.mosip.resident.dto.TemplateResponseDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import static io.mosip.resident.constant.MappingJsonConstants.GENDER;

/**
 * Resident proxy masterdata service implementation class.
 * 
 * @author Ritik Jain
 */
@Component
public class ProxyMasterdataServiceImpl implements ProxyMasterdataService {

	private static final String CODE = "code";

	private static final String DOCUMENTTYPES = "documenttypes";

	private static final String DOCUMENTCATEGORIES = "documentcategories";

	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;

	@Autowired
	private AuditUtil auditUtil;

	@Autowired
	Environment env;

	private static final Logger logger = LoggerConfiguration.logConfig(ProxyMasterdataServiceImpl.class);

	@Override
	public ResponseWrapper<?> getValidDocumentByLangCode(String langCode) throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataServiceImpl::getValidDocumentByLangCode()::entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		Map<String, String> pathsegments = new HashMap<String, String>();
		pathsegments.put("langCode", langCode);
		try {
			responseWrapper = residentServiceRestClient.getApi(ApiName.VALID_DOCUMENT_BY_LANGCODE_URL, pathsegments,
					ResponseWrapper.class);

			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				auditUtil.setAuditRequestDto(EventEnum.GET_VALID_DOCUMENT_EXCEPTION);
				logger.debug(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_VALID_DOCUMENT_EXCEPTION);
			logger.error("Error occured in accessing valid documents %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getValidDocumentByLangCode()::exit");
		return responseWrapper;
	}

	@Override
	@Cacheable(value = "valid-doc-cat-and-type-list", key = "#langCode")
	public Tuple2<List<String>, Map<String, List<String>>> getValidDocCatAndTypeList(String langCode)
			throws ResidentServiceCheckedException {
		ResponseWrapper<?> responseWrapper = getValidDocumentByLangCode(langCode);
		Map<String, Object> response = new LinkedHashMap<>((Map<String, Object>) responseWrapper.getResponse());
		List<Map<String, Object>> validDoc = (List<Map<String, Object>>) response.get(DOCUMENTCATEGORIES);

		List<String> docCatCodes = validDoc.stream()
				.map(map -> ((String) map.get(CODE)).toLowerCase())
				.collect(Collectors.toList());

		Map<String, List<String>> docTypeCodes = validDoc.stream()
				.map(map -> {
					return Map.entry(((String) map.get(CODE)).toLowerCase(),
							getDocTypCodeList((List<Map<String, Object>>) map.get(DOCUMENTTYPES)));
				})
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

		return Tuples.of(docCatCodes, docTypeCodes);
	}

	private List<String> getDocTypCodeList(List<Map<String, Object>> docTypMap){
		return docTypMap.stream()
				.flatMap(map -> {
					return Stream.of(((String) map.get(CODE)).toLowerCase());
					})
				.collect(Collectors.toList());
	}

	@Override
	public ResponseWrapper<?> getLocationHierarchyLevelByLangCode(String langCode)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataServiceImpl::getLocationHierarchyLevelByLangCode()::entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		Map<String, String> pathsegments = new HashMap<String, String>();
		pathsegments.put("langcode", langCode);
		try {
			responseWrapper = residentServiceRestClient.getApi(ApiName.LOCATION_HIERARCHY_LEVEL_BY_LANGCODE_URL,
					pathsegments, ResponseWrapper.class);

			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				auditUtil.setAuditRequestDto(EventEnum.GET_LOCATION_HIERARCHY_LEVEL_EXCEPTION);
				logger.debug(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_LOCATION_HIERARCHY_LEVEL_EXCEPTION);
			logger.error("Error occured in accessing location hierarchy levels %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getLocationHierarchyLevelByLangCode()::exit");
		return responseWrapper;
	}

	@Override
	public ResponseWrapper<?> getImmediateChildrenByLocCodeAndLangCode(String locationCode, String langCode)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataServiceImpl::getImmediateChildrenByLocCodeAndLangCode()::entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		Map<String, String> pathsegments = new HashMap<String, String>();
		pathsegments.put("locationcode", locationCode);
		pathsegments.put("langcode", langCode);
		try {
			responseWrapper = residentServiceRestClient.getApi(
					ApiName.IMMEDIATE_CHILDREN_BY_LOCATIONCODE_AND_LANGCODE_URL, pathsegments, ResponseWrapper.class);

			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				auditUtil.setAuditRequestDto(EventEnum.GET_IMMEDIATE_CHILDREN_EXCEPTION);
				logger.debug(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_IMMEDIATE_CHILDREN_EXCEPTION);
			logger.error("Error occured in accessing immediate children %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getImmediateChildrenByLocCodeAndLangCode()::exit");
		return responseWrapper;
	}

	@Override
	public ResponseWrapper<?> getLocationDetailsByLocCodeAndLangCode(String locationCode, String langCode)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataServiceImpl::getLocationDetailsByLocCodeAndLangCode()::entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		Map<String, String> pathsegments = new HashMap<String, String>();
		pathsegments.put("locationcode", locationCode);
		pathsegments.put("langcode", langCode);
		try {
			responseWrapper = residentServiceRestClient.getApi(ApiName.LOCATION_INFO_BY_LOCCODE_AND_LANGCODE_URL,
					pathsegments, ResponseWrapper.class);

			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				auditUtil.setAuditRequestDto(EventEnum.GET_LOCATION_DETAILS_EXCEPTION);
				logger.debug(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_LOCATION_DETAILS_EXCEPTION);
			logger.error("Error occured in accessing location details %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getLocationDetailsByLocCodeAndLangCode()::exit");
		return responseWrapper;
	}

	@Override
	public ResponseWrapper<?> getCoordinateSpecificRegistrationCenters(String langCode, double longitude,
			double latitude, int proximityDistance) throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataServiceImpl::getCoordinateSpecificRegistrationCenters()::entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		Map<String, Object> pathsegements = new HashMap<String, Object>();
		pathsegements.put("langcode", langCode);
		pathsegements.put("longitude", longitude);
		pathsegements.put("latitude", latitude);
		pathsegements.put("proximitydistance", proximityDistance);
		try {
			responseWrapper = residentServiceRestClient.getApi(ApiName.COORDINATE_SPECIFIC_REGISTRATION_CENTERS_URL,
					pathsegements, ResponseWrapper.class);

			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				auditUtil.setAuditRequestDto(EventEnum.GET_COORDINATE_SPECIFIC_REG_CENTERS_EXCEPTION);
				logger.debug(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_COORDINATE_SPECIFIC_REG_CENTERS_EXCEPTION);
			logger.error("Error occured in accessing coordinate specific registration centers %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getCoordinateSpecificRegistrationCenters()::exit");
		return responseWrapper;
	}

	@Override
	public ResponseWrapper<?> getApplicantValidDocument(String applicantId, List<String> languages)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataServiceImpl::getApplicantValidDocument()::entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		Map<String, String> pathsegements = new HashMap<String, String>();
		pathsegements.put("applicantId", applicantId);

		List<String> queryParamName = new ArrayList<String>();
		queryParamName.add("languages");

		List<Object> queryParamValue = new ArrayList<>();
		queryParamValue.add(languages.stream().collect(Collectors.joining(",")));

		try {
			responseWrapper = (ResponseWrapper<?>) residentServiceRestClient.getApi(
					ApiName.APPLICANT_VALID_DOCUMENT_URL, pathsegements, queryParamName, queryParamValue,
					ResponseWrapper.class);

			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				logger.debug(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_APPLICANT_VALID_DOCUMENT_EXCEPTION);
			logger.error("Error occured in accessing applicant valid document %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getApplicantValidDocument()::exit");
		return responseWrapper;
	}

	@Override
	public ResponseWrapper<?> getRegistrationCentersByHierarchyLevel(String langCode, Short hierarchyLevel,
			List<String> name) throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataServiceImpl::getRegistrationCentersByHierarchyLevel()::entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		Map<String, Object> pathsegements = new HashMap<String, Object>();
		pathsegements.put("langcode", langCode);
		pathsegements.put("hierarchylevel", hierarchyLevel);

		List<String> queryParamName = new ArrayList<String>();
		queryParamName.add("name");

		List<Object> queryParamValue = new ArrayList<>();
		queryParamValue.add(name.stream().collect(Collectors.joining(",")));

		try {
			responseWrapper = residentServiceRestClient.getApi(ApiName.REGISTRATION_CENTER_FOR_LOCATION_CODE_URL,
					pathsegements, queryParamName, queryParamValue, ResponseWrapper.class);

			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				auditUtil.setAuditRequestDto(EventEnum.GET_REG_CENTERS_FOR_LOCATION_CODE_EXCEPTION);
				logger.debug(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_REG_CENTERS_FOR_LOCATION_CODE_EXCEPTION);
			logger.error("Error occured in accessing registration centers %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getRegistrationCentersByHierarchyLevel()::exit");
		return responseWrapper;
	}

	@Override
	public ResponseWrapper<?> getRegistrationCenterByHierarchyLevelAndTextPaginated(String langCode,
			Short hierarchyLevel, String name, int pageNumber, int pageSize, OrderEnum orderBy, String sortBy)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataServiceImpl::getRegistrationCenterByHierarchyLevelAndTextPaginated()::entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();

		Map<String, Object> pathsegements = new HashMap<String, Object>();
		pathsegements.put("langcode", langCode);
		pathsegements.put("hierarchylevel", hierarchyLevel);
		pathsegements.put("name", name);

		List<String> queryParamName = new ArrayList<String>();
		queryParamName.add("pageNumber");
		queryParamName.add("pageSize");
		queryParamName.add("orderBy");
		queryParamName.add("sortBy");

		List<Object> queryParamValue = new ArrayList<>();
		queryParamValue.add(pageNumber);
		queryParamValue.add(pageSize);
		queryParamValue.add(orderBy);
		queryParamValue.add(sortBy);

		try {
			responseWrapper = residentServiceRestClient.getApi(
					ApiName.REGISTRATION_CENTER_BY_LOCATION_TYPE_AND_SEARCH_TEXT_PAGINATED_URL, pathsegements,
					queryParamName, queryParamValue, ResponseWrapper.class);

			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				auditUtil.setAuditRequestDto(EventEnum.GET_REG_CENTERS_PAGINATED_EXCEPTION);
				logger.debug(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_REG_CENTERS_PAGINATED_EXCEPTION);
			logger.error("Error occured in accessing registration centers paginated %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getRegistrationCenterByHierarchyLevelAndTextPaginated()::exit");
		return responseWrapper;
	}

	@Override
	public ResponseWrapper<?> getRegistrationCenterWorkingDays(String registrationCenterID, String langCode)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataServiceImpl::getRegistrationCenterWorkingDays()::entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		Map<String, String> pathsegements = new HashMap<String, String>();
		pathsegements.put("registrationCenterID", registrationCenterID);
		pathsegements.put("langCode", langCode);
		try {
			responseWrapper = residentServiceRestClient.getApi(ApiName.WORKING_DAYS_BY_REGISTRATION_ID, pathsegements,
					ResponseWrapper.class);

			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				auditUtil.setAuditRequestDto(EventEnum.GET_REG_CENTER_WORKING_DAYS_EXCEPTION);
				logger.debug(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_REG_CENTER_WORKING_DAYS_EXCEPTION);
			logger.error("Error occured in accessing registration center working days %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getRegistrationCenterWorkingDays()::exit");
		return responseWrapper;
	}

	@Override
	public ResponseWrapper<?> getLatestIdSchema(double schemaVersion, String domain, String type)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataServiceImpl::getLatestIdSchema()::entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();

		List<String> pathsegements = null;

		List<String> queryParamName = new ArrayList<String>();
		queryParamName.add("schemaVersion");
		queryParamName.add("domain");
		queryParamName.add("type");

		List<Object> queryParamValue = new ArrayList<>();
		queryParamValue.add(schemaVersion);
		queryParamValue.add(domain);
		queryParamValue.add(type);

		try {
			responseWrapper = (ResponseWrapper<?>) residentServiceRestClient.getApi(ApiName.LATEST_ID_SCHEMA_URL,
					pathsegements, queryParamName, queryParamValue, ResponseWrapper.class);
			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				auditUtil.setAuditRequestDto(EventEnum.GET_LATEST_ID_SCHEMA_EXCEPTION);
				logger.debug(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_LATEST_ID_SCHEMA_EXCEPTION);
			logger.error("Error occured in accessing latest id schema %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getLatestIdSchema()::exit");
		return responseWrapper;
	}

	@Override
	public ResponseWrapper<?> getAllTemplateBylangCodeAndTemplateTypeCode(String langCode, String templateTypeCode)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataServiceImpl::getAllTemplateBylangCodeAndTemplateTypeCode()::entry");
		ResponseWrapper<TemplateResponseDto> response = new ResponseWrapper<>();
		Map<String, String> pathsegments = new HashMap<String, String>();
		pathsegments.put("langcode", langCode);
		pathsegments.put("templatetypecode", templateTypeCode);

		try {
			response = residentServiceRestClient.getApi(ApiName.TEMPLATES_BY_LANGCODE_AND_TEMPLATETYPECODE_URL,
					pathsegments, ResponseWrapper.class);
			if (response.getErrors() != null && !response.getErrors().isEmpty()) {
				auditUtil.setAuditRequestDto(EventEnum.GET_TEMPLATES_EXCEPTION);
				logger.debug(response.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.TEMPLATE_EXCEPTION);
			}
			TemplateResponseDto templateResponse = JsonUtil
					.readValue(JsonUtil.writeValueAsString(response.getResponse()), TemplateResponseDto.class);
			String template = templateResponse.getTemplates().get(0).getFileText();
			ResponseWrapper<Map> responseWrapper = new ResponseWrapper<>();
			Map<String, String> responseMap = new HashMap<>();
			responseMap.put(ResidentConstants.FILE_TEXT, template);
			responseWrapper.setResponse(responseMap);
			logger.debug("ProxyMasterdataServiceImpl::getAllTemplateBylangCodeAndTemplateTypeCode()::exit");
			return responseWrapper;

		} catch (ApisResourceAccessException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_TEMPLATES_EXCEPTION);
			logger.error("Error occured in accessing templates %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		} catch (IOException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_TEMPLATES_EXCEPTION);
			throw new ResidentServiceCheckedException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		}
	}

	@Override
	public ResponseWrapper<?> getDynamicFieldBasedOnLangCodeAndFieldName(String fieldName, String langCode, boolean withValue) throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataServiceImpl::getDynamicFieldBasedOnLangCodeAndFieldName()::entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		Map<String, String> pathsegments = new HashMap<String, String>();
		pathsegments.put("langcode", langCode);
		pathsegments.put("fieldName", fieldName);
		List<String> queryParamName = new ArrayList<String>();
		queryParamName.add("withValue");

		List<Object> queryParamValue = new ArrayList<>();
		queryParamValue.add(withValue);
		try {
			responseWrapper = residentServiceRestClient.getApi(ApiName.DYNAMIC_FIELD_BASED_ON_LANG_CODE_AND_FIELD_NAME, pathsegments, queryParamName,
					queryParamValue, ResponseWrapper.class);
			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				auditUtil.setAuditRequestDto(EventEnum.GET_GENDER_TYPES_EXCEPTION);
				logger.debug(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_GENDER_TYPES_EXCEPTION);
			logger.error("Error occured in accessing gender types %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getDynamicFieldBasedOnLangCodeAndFieldName()::exit");
		return responseWrapper;
	}
	
	@Override
	public ResponseWrapper<?> getDocumentTypesByDocumentCategoryAndLangCode(String documentcategorycode, String langCode) throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataServiceImpl::getDocumentTypesByDocumentCategoryAndLangCode()::entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		Map<String, String> pathsegments = new HashMap<String, String>();
		pathsegments.put("documentcategorycode", documentcategorycode);
		pathsegments.put("langcode", langCode);
		try {
			responseWrapper=residentServiceRestClient.getApi(ApiName.DOCUMENT_TYPE_BY_DOCUMENT_CATEGORY_AND_LANG_CODE, pathsegments, ResponseWrapper.class);
			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				auditUtil.setAuditRequestDto(EventEnum.GET_DOCUMENT_TYPES_EXCEPTION);
				logger.debug(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_DOCUMENT_TYPES_EXCEPTION);
			logger.error("Error occured in accessing document types %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getDocumentTypesByDocumentCategoryAndLangCode()::exit");
		return responseWrapper;
	}

	@Override
	public ResponseWrapper<GenderCodeResponseDTO> getGenderCodeByGenderTypeAndLangCode(String genderName,
			String langCode) throws ResidentServiceCheckedException, IOException {
		logger.debug("ProxyMasterdataServiceImpl::getGenderCodeByGenderTypeAndLangCode()::entry");
		ResponseWrapper<GenderCodeResponseDTO> responseWrapper = new ResponseWrapper<>();
		GenderCodeResponseDTO genderCodeResponseDTO = new GenderCodeResponseDTO();
		ResponseWrapper<?> res = getDynamicFieldBasedOnLangCodeAndFieldName(GENDER, langCode, true);
		GenderTypeListDTO response = JsonUtil.readValue(JsonUtil.writeValueAsString(res.getResponse()),
				GenderTypeListDTO.class);
		Optional<String> genderCode = response.getGenderType().stream()
				.filter(map -> map.getGenderName().equalsIgnoreCase(genderName))
				.map(map -> map.getCode())
				.findAny();
		if (genderCode.isPresent()) {
			genderCodeResponseDTO.setGenderCode(genderCode.get());
		}
		responseWrapper.setResponse(genderCodeResponseDTO);
		logger.debug("ProxyMasterdataServiceImpl::getGenderCodeByGenderTypeAndLangCode()::exit");
		return responseWrapper;
	}

}