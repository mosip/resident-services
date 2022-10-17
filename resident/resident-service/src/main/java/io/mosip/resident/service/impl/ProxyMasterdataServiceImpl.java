package io.mosip.resident.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.pdfgenerator.spi.PDFGenerator;
import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.kernel.core.templatemanager.spi.TemplateManagerBuilder;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.OrderEnum;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.RegistrationCenterDto;
import io.mosip.resident.dto.RegistrationCenterResponseDto;
import io.mosip.resident.dto.TemplateResponseDto;
import io.mosip.resident.dto.WorkingDaysDto;
import io.mosip.resident.dto.WorkingDaysResponseDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;

/**
 * Resident proxy masterdata service implementation class.
 * 
 * @author Ritik Jain
 */
@Component
public class ProxyMasterdataServiceImpl implements ProxyMasterdataService {

	private static final String CLASSPATH = "classpath";
	private static final String ENCODE_TYPE = "UTF-8";
	private static final String REGISTRATION_CENTER_TEMPLATE_NAME = "registration-centers-list";
	
	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;

	@Autowired
	private AuditUtil auditUtil;

	@Autowired
	Environment env;
	
	@Autowired
	private ProxyMasterdataService proxyMasterdataService;
	
	private TemplateManager templateManager;

	@Autowired
	private TemplateManagerBuilder templateManagerBuilder;

	@PostConstruct
	public void idTemplateManagerPostConstruct() {
		templateManager = templateManagerBuilder.encodingType(ENCODE_TYPE).enableCache(false).resourceLoader(CLASSPATH)
				.build();
	}

	@Autowired
	private PDFGenerator pdfGenerator;

	/** The mapper. */
	@Autowired
	private ObjectMapper mapper;
	
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
				logger.debug(response.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(ResidentErrorCode.TEMPLATE_EXCEPTION);
			}
			TemplateResponseDto templateResponse = JsonUtil
					.readValue(JsonUtil.writeValueAsString(response.getResponse()), TemplateResponseDto.class);
			String template = templateResponse.getTemplates().get(0).getFileText();
			ResponseWrapper<Map> responseWrapper = new ResponseWrapper<>();
			Map<String, String> responseMap = new HashMap<>();
			responseMap.put("fileText", template);
			responseWrapper.setResponse(responseMap);
			logger.debug("ProxyMasterdataServiceImpl::getAllTemplateBylangCodeAndTemplateTypeCode()::exit");
			return responseWrapper;

		} catch (ApisResourceAccessException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_TEMPLATES_EXCEPTION);
			logger.error("Error occured in accessing templates %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		} catch (IOException e) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		}
	}
	
	/**
	 * download registration centers based on language code, hierarchyLevel and center names
	 */
	public byte[] downloadRegistrationCentersByHierarchyLevel(String langCode, Short hierarchyLevel,
			List<String> name) throws ResidentServiceCheckedException,IOException,Exception {		
		logger.debug("ResidentServiceImpl::getResidentServicePDF()::entry");
		ResponseWrapper<?> proxyResponseWrapper = proxyMasterdataService
				.getAllTemplateBylangCodeAndTemplateTypeCode(langCode, REGISTRATION_CENTER_TEMPLATE_NAME);		
		ResponseWrapper<?> regCentResponseWrapper = proxyMasterdataService.getRegistrationCentersByHierarchyLevel(langCode,hierarchyLevel, name);
		Map<String,Object> regCentersMap = new LinkedHashMap<>();
		List<RegistrationCenterDto >regCentersDtlsList=Collections.EMPTY_LIST;
		int serialNumber = 1;
		if (regCentResponseWrapper != null) {
			RegistrationCenterResponseDto registrationCentersDtls = mapper.readValue(
					mapper.writeValueAsString(regCentResponseWrapper.getResponse()), RegistrationCenterResponseDto.class);
			regCentersDtlsList = registrationCentersDtls.getRegistrationCenters();
			for (RegistrationCenterDto regCenterDto : regCentersDtlsList) {
				List<WorkingDaysDto> workingDaysList = Collections.EMPTY_LIST;
				String workingHours = "";
				String fullAddress = getFullAddress(regCenterDto.getAddressLine1(), regCenterDto.getAddressLine2(),
						regCenterDto.getAddressLine3());
				regCenterDto.setSerialNumber(serialNumber++);
				regCenterDto.setFullAddress(fullAddress);				
				workingDaysList = getRegCenterWorkingDays(regCenterDto.getId(), regCenterDto.getLangCode());
				workingHours = workingDaysList.get(0).getName() + "-" + workingDaysList.get(1).getName() + "|"
						+ regCenterDto.getCenterStartTime().substring(0, 5) + " " + "am" + "-"
						+ regCenterDto.getCenterEndTime().substring(0, 5) + " " + "pm";
				regCenterDto.setWorkingHours(workingHours);
			}
		}				
		regCentersMap.put("regCentersDtlsList", regCentersDtlsList);
		logger.debug("template data from DB:" + proxyResponseWrapper.getResponse());
		Map<String, Object> templateResponse = new LinkedHashMap<>((Map<String, Object>) proxyResponseWrapper.getResponse());
		String fileText = (String) templateResponse.get("fileText");
		InputStream downLoadRegCenterTemplate = new ByteArrayInputStream(fileText.getBytes(StandardCharsets.UTF_8));
		InputStream downLoadRegCenterTemplateData = templateManager.merge(downLoadRegCenterTemplate, regCentersMap);
		
		StringWriter writer = new StringWriter();
		IOUtils.copy(downLoadRegCenterTemplateData, writer, "UTF-8");
		ByteArrayOutputStream pdfValue = (ByteArrayOutputStream) pdfGenerator.generate(writer.toString());
		logger.debug("ResidentServiceImpl::residentServiceHistoryPDF()::exit");
		return pdfValue.toByteArray();
	}
	
	/**
	 * return the full address
	 * @param address1
	 * @param address2
	 * @param address3
	 * @return
	 */
	private String getFullAddress(String address1, String address2, String address3) {
		StringBuilder fullAddress = new StringBuilder();
		fullAddress.append(address1 + "," + address2 + "," + address3);
		return fullAddress.toString();
	}
	
	/**
	 * return the starting and ending working day details
	 * @param regCenterId
	 * @param langCode
	 * @return
	 * @throws ResidentServiceCheckedException
	 * @throws Exception
	 */
	private List<WorkingDaysDto> getRegCenterWorkingDays(String regCenterId, String langCode) throws ResidentServiceCheckedException,Exception {
		ResponseWrapper<?> responseWrapper;
		responseWrapper = proxyMasterdataService.getRegistrationCenterWorkingDays(regCenterId, langCode);
		WorkingDaysResponseDto workingDaysResponeDtls = mapper.readValue(mapper.writeValueAsString(responseWrapper.getResponse()), WorkingDaysResponseDto.class);		
		List<WorkingDaysDto> workingDaysList=workingDaysResponeDtls.getWorkingdays();
		
		WorkingDaysDto startDay = workingDaysList.stream().min(Comparator.comparing(WorkingDaysDto::getOrder))
				.orElseThrow(NoSuchElementException::new);
		
		WorkingDaysDto endDay = workingDaysList.stream().max(Comparator.comparing(WorkingDaysDto::getOrder))
				.orElseThrow(NoSuchElementException::new);
		
		List<WorkingDaysDto> workingDaysHoursList = new ArrayList<>();
		workingDaysHoursList.add(startDay);
		workingDaysHoursList.add(endDay);
		return workingDaysHoursList;
	}
}
