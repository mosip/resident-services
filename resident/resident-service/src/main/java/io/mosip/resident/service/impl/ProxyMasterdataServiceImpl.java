package io.mosip.resident.service.impl;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.OrderEnum;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.GenderCodeResponseDTO;
import io.mosip.resident.dto.LocationImmediateChildrenResponseDto;
import io.mosip.resident.dto.TemplateResponseDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.util.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	private static final String GENDER_NAME = "genderName";
	private static final Object VALUES = "values";

	private Map<String, List<Map<String, Object>>> cache = new ConcurrentHashMap<>();



	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;

	@Autowired
	Environment env;

	@Autowired
	Utility utility;

	private static final Logger logger = LoggerConfiguration.logConfig(ProxyMasterdataServiceImpl.class);

	@Autowired
	private Utilities utilities;

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
				logger.error(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
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
		logger.debug("ProxyMasterdataServiceImpl::getValidDocCatAndTypeList()::entry");
		ResponseWrapper<?> responseWrapper = utility.getValidDocumentByLangCode(langCode);
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
		logger.debug("ProxyMasterdataServiceImpl::getValidDocCatAndTypeList()::exit");
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
	@Cacheable(value = "getLocationHierarchyLevelByLangCode", key = "#langCode")
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
				logger.error(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			logger.error("Error occured in accessing location hierarchy levels %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getLocationHierarchyLevelByLangCode()::exit");
		return responseWrapper;
	}

	@Override
	@Cacheable(value = "getImmediateChildrenByLocCodeAndLangCode", key = "#locationCode + '_' + #langCode")
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
				logger.error(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			logger.error("Error occured in accessing immediate children %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getImmediateChildrenByLocCodeAndLangCode()::exit");
		return responseWrapper;
	}

	@Override
	@Cacheable(value = "getLocationDetailsByLocCodeAndLangCode", key = "#locationCode + '_' + #langCode")
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
				logger.error(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			logger.error("Error occured in accessing location details %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getLocationDetailsByLocCodeAndLangCode()::exit");
		return responseWrapper;
	}

	@Override
	@Cacheable(value = "getCoordinateSpecificRegistrationCenters", key = "{#langCode, #longitude, #latitude, #proximityDistance}")
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
				logger.error(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			logger.error("Error occured in accessing coordinate specific registration centers %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getCoordinateSpecificRegistrationCenters()::exit");
		return responseWrapper;
	}

	@Override
	@Cacheable(value = "getApplicantValidDocument", key = "{#applicantId, #languages}")
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
				logger.error(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			logger.error("Error occured in accessing applicant valid document %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getApplicantValidDocument()::exit");
		return responseWrapper;
	}

	@Override
	@Cacheable(value = "getRegistrationCentersByHierarchyLevel", key = "{#langCode, #hierarchyLevel, #name}")
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
				logger.error(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			logger.error("Error occured in accessing registration centers %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getRegistrationCentersByHierarchyLevel()::exit");
		return responseWrapper;
	}

	@Override
	@Cacheable(value = "getRegistrationCenterByHierarchyLevelAndTextPaginated", key = "{#langCode, #hierarchyLevel, #name, #pageNumber," +
			" #pageSize, #orderBy, #sortBy}")
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
				logger.error(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			logger.error("Error occured in accessing registration centers paginated %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getRegistrationCenterByHierarchyLevelAndTextPaginated()::exit");
		return responseWrapper;
	}

	@Override
	@Cacheable(value = "getRegistrationCenterWorkingDays", key = "{#registrationCenterID, #langCode}")
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
				logger.error(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			logger.error("Error occured in accessing registration center working days %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getRegistrationCenterWorkingDays()::exit");
		return responseWrapper;
	}

	@Override
	@Cacheable(value = "getLatestIdSchema", key = "{#schemaVersion, #domain, #type}")
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
				logger.error(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
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
				logger.error(response.getErrors().get(0).toString());
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
			logger.error("Error occured in accessing templates %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		} catch (IOException e) {
			logger.error("Error occured in accessing templates %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		}
	}

	@Override
	@Cacheable(value = "templateCache", key = "#languageCode + '_' + #templateTypeCode")
	public String getTemplateValueFromTemplateTypeCodeAndLangCode(String languageCode, String templateTypeCode) {
		try {
			ResponseWrapper<?> proxyResponseWrapper = getAllTemplateBylangCodeAndTemplateTypeCode(languageCode, templateTypeCode);
			logger.debug(String.format("Template data from DB:- %s", proxyResponseWrapper.getResponse()));
			Map<String, String> templateResponse = new LinkedHashMap<>(
					(Map<String, String>) proxyResponseWrapper.getResponse());
			return templateResponse.get(ResidentConstants.FILE_TEXT);
		} catch (ResidentServiceCheckedException e) {
			throw new ResidentServiceException(ResidentErrorCode.TEMPLATE_EXCEPTION, e);
		}
	}

	@Override
	@Cacheable(value = "getLocationHierarchyLevels")
	public ResponseWrapper<?> getLocationHierarchyLevels(String lastUpdated) throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataServiceImpl::getLocationHierarchyLevels()::entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		List<String> pathsegements = null;
		List<String> queryParamName = new ArrayList<String>();
		List<Object> queryParamValue = new ArrayList<>();
		if(lastUpdated!=null){
			queryParamName.add("lastUpdated");
			queryParamValue.add(lastUpdated);
		}
		try {
			responseWrapper = (ResponseWrapper<?>) residentServiceRestClient.getApi(ApiName.LOCATION_HIERARCHY,
					pathsegements, queryParamName, queryParamValue, ResponseWrapper.class);
			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				logger.error(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			logger.error("Error occured in accessing location hierarchy level %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getLocationHierarchyLevels()::exit");
		return responseWrapper;
	}

	@Override
	@Cacheable(value = "getAllDynamicFieldByName", key = "#fieldName")
	public ResponseWrapper<?> getAllDynamicFieldByName(String fieldName) throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataServiceImpl::getAllDynamicFieldByName()::entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		Map<String, String> pathsegments = new HashMap<String, String>();
		pathsegments.put("fieldName", fieldName);
		try {
			responseWrapper = residentServiceRestClient.getApi(ApiName.DYNAMIC_FIELD_BASED_ON_FIELD_NAME,
					pathsegments, ResponseWrapper.class);
			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				logger.error(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException | ResidentServiceCheckedException e) {
			logger.error("Error occured in accessing dynamic data %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getDynamicFieldBasedOnLangCodeAndFieldName()::exit");
		return responseWrapper;
	}

	@Override
	public LocationImmediateChildrenResponseDto getImmediateChildrenByLocCode(String locationCode, List<String> languageCodes) throws ResidentServiceCheckedException {
		List<String> cacheKeyList = new ArrayList<>();
		for(String languageCode:languageCodes){
			cacheKeyList.add(locationCode +"_"+ languageCode);
		}
		LocationImmediateChildrenResponseDto result =new LocationImmediateChildrenResponseDto();
		Map<String, List<Map<String, Object>>> locations = new HashMap<>();
		List<String> languageCodesNotCached = new ArrayList<>();
		if(!cache.isEmpty()) {
			for (String cacheKeyLanguage : cacheKeyList) {
				List<Map<String, Object>> cachedResult = cache.get(cacheKeyLanguage);
				String languageCode = List.of(cacheKeyLanguage.split("_")).get(1);
				if (cachedResult != null) {
					locations.put(languageCode, cachedResult);
				} else {
					languageCodesNotCached.add(languageCode);
				}
			}
		}
		if(cache.isEmpty()){
			languageCodesNotCached.addAll(languageCodes);
		}
		if(!languageCodesNotCached.isEmpty()) {
			LocationImmediateChildrenResponseDto responseDto = getImmediateChildrenByLocCodeAndLanguageCodes(locationCode, languageCodes);
			for(String languageCodeNotCached:languageCodesNotCached){
				locations.put(languageCodeNotCached, responseDto.getLocations().get(languageCodeNotCached));
				cache.put(locationCode+"_"+languageCodeNotCached, responseDto.getLocations().get(languageCodeNotCached));
			}
		}
		result.setLocations(locations);
		return result;
	}

	public LocationImmediateChildrenResponseDto getImmediateChildrenByLocCodeAndLanguageCodes(String locationCode, List<String> languageCodes) throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataServiceImpl::getImmediateChildrenByLocCode()::entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();

		List<String> pathsegements = new ArrayList<>();
		pathsegements.add(locationCode);

		List<String> queryParamName = new ArrayList<String>();
		queryParamName.add("languageCodes");

		List<Object> queryParamValue = new ArrayList<>();
		queryParamValue.add(String.join(ResidentConstants.COMMA, languageCodes));

		try {
			responseWrapper = (ResponseWrapper<?>) residentServiceRestClient.getApi(ApiName.IMMEDIATE_CHILDREN_BY_LOCATION_CODE,
					pathsegements, queryParamName, queryParamValue, ResponseWrapper.class);
			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				logger.error(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException | ResidentServiceCheckedException e) {
			logger.error("Error occured in accessing latest id schema %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		Map<Object, Object> locationResponse = (Map<Object, Object>) responseWrapper.getResponse();
		List<Map<String, Object>> locationList = (List<Map<String, Object>>) locationResponse.get("locations");

		Map<String, List<Map<String, Object>>> groupedLocations = locationList.stream()
				.collect(Collectors.groupingBy(map -> (String) map.get("langCode")));

		LocationImmediateChildrenResponseDto locationImmediateChildrenResponseDto = new LocationImmediateChildrenResponseDto();
		locationImmediateChildrenResponseDto.setLocations(groupedLocations);

		logger.debug("ProxyMasterdataServiceImpl::getImmediateChildrenByLocCode()::exit");
		return locationImmediateChildrenResponseDto;
	}

	@CacheEvict(value = "templateCache", allEntries = true)
	@Scheduled(fixedRateString = "${resident.cache.expiry.time.millisec.templateCache}")
	public void emptyTemplateCache() {
		logger.info("Emptying Template cache");
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
				logger.error(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			logger.error("Error occured in accessing dynamic data %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getDynamicFieldBasedOnLangCodeAndFieldName()::exit");
		return responseWrapper;
	}
	
	@Override
	@Cacheable(value = "getDocumentTypesByDocumentCategoryAndLangCode", key = "{#documentcategorycode, #langCode}")
	public ResponseWrapper<?> getDocumentTypesByDocumentCategoryAndLangCode(String documentcategorycode, String langCode) throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataServiceImpl::getDocumentTypesByDocumentCategoryAndLangCode()::entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		Map<String, String> pathsegments = new HashMap<String, String>();
		pathsegments.put("documentcategorycode", documentcategorycode);
		pathsegments.put("langcode", langCode);
		try {
			responseWrapper=residentServiceRestClient.getApi(ApiName.DOCUMENT_TYPE_BY_DOCUMENT_CATEGORY_AND_LANG_CODE, pathsegments, ResponseWrapper.class);
			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				logger.error(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			logger.error("Error occured in accessing document types %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getDocumentTypesByDocumentCategoryAndLangCode()::exit");
		return responseWrapper;
	}

	@Override
	@Cacheable(value = "getGenderCodeByGenderTypeAndLangCode", key = "{#genderName, #langCode}")
	public ResponseWrapper<GenderCodeResponseDTO> getGenderCodeByGenderTypeAndLangCode(String genderName,
			String langCode) throws ResidentServiceCheckedException, IOException {
		logger.debug("ProxyMasterdataServiceImpl::getGenderCodeByGenderTypeAndLangCode()::entry");
		ResponseWrapper<GenderCodeResponseDTO> responseWrapper = new ResponseWrapper<>();
		GenderCodeResponseDTO genderCodeResponseDTO = new GenderCodeResponseDTO();
		ResponseWrapper<?> res = utilities.getDynamicFieldBasedOnLangCodeAndFieldName(GENDER, langCode, true);
		Map response = (Map) res.getResponse();
		ArrayList<Map> responseValues = (ArrayList<Map>) response.get(VALUES);
		String genderCode = responseValues.stream()
				.filter(responseMap -> responseMap.get(ResidentConstants.VALUE).toString().equalsIgnoreCase(genderName))
				.map(responseMap -> responseMap.get(CODE).toString())
				.findFirst()
				.orElse(null);
		if (genderCode!=null) {
			genderCodeResponseDTO.setGenderCode(genderCode);
		} else {
			throw new  InvalidInputException(GENDER_NAME);
		}
		responseWrapper.setResponse(genderCodeResponseDTO);
		logger.debug("ProxyMasterdataServiceImpl::getGenderCodeByGenderTypeAndLangCode()::exit");
		return responseWrapper;
	}

}