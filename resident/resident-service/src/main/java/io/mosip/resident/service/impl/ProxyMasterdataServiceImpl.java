package io.mosip.resident.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.ResidentServiceRestClient;

/**
 * Resident proxy masterdata service implementation class.
 * 
 * @author Ritik Jain
 */
@Component
public class ProxyMasterdataServiceImpl implements ProxyMasterdataService {

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
				logger.debug(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(responseWrapper.getErrors().get(0).getErrorCode(),
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
				throw new ResidentServiceCheckedException(responseWrapper.getErrors().get(0).getErrorCode(),
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
				throw new ResidentServiceCheckedException(responseWrapper.getErrors().get(0).getErrorCode(),
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
				throw new ResidentServiceCheckedException(responseWrapper.getErrors().get(0).getErrorCode(),
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
	public ResponseWrapper<?> getCoordinateSpecificRegistrationCenters(String langCode, String longitude,
			String latitude, String proximityDistance) throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataServiceImpl::getCoordinateSpecificRegistrationCenters()::entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		Map<String, String> pathsegements = new HashMap<String, String>();
		pathsegements.put("langcode", langCode);
		pathsegements.put("longitude", longitude);
		pathsegements.put("latitude", latitude);
		pathsegements.put("proximitydistance", proximityDistance);
		try {
			responseWrapper = residentServiceRestClient.getApi(ApiName.COORDINATE_SPECIFIC_REGISTRATION_CENTERS_URL,
					pathsegements, ResponseWrapper.class);

			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				logger.debug(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(responseWrapper.getErrors().get(0).getErrorCode(),
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
	public ResponseWrapper<?> getApplicantValidDocument(String applicantId, String languages)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataServiceImpl::getApplicantValidDocument()::entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		Map<String, String> pathsegements = new HashMap<String, String>();
		pathsegements.put("applicantId", applicantId);
		try {
			responseWrapper = (ResponseWrapper<?>) residentServiceRestClient.getApi(
					ApiName.APPLICANT_VALID_DOCUMENT_URL, pathsegements, List.of("languages"), List.of(languages),
					ResponseWrapper.class);

			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				logger.debug(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(responseWrapper.getErrors().get(0).getErrorCode(),
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
	public ResponseWrapper<?> getRegistrationCentersByHierarchyLevel(String langCode, String hierarchyLevel,
			String name) throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataServiceImpl::getRegistrationCentersByHierarchyLevel()::entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		Map<String, String> pathsegements = new HashMap<String, String>();
		pathsegements.put("langcode", langCode);
		pathsegements.put("hierarchylevel", hierarchyLevel);
		try {
			responseWrapper = residentServiceRestClient.getApi(ApiName.REGISTRATION_CENTER_FOR_LOCATION_CODE_URL,
					pathsegements, List.of("name"), List.of(name), ResponseWrapper.class);

			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				logger.debug(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(responseWrapper.getErrors().get(0).getErrorCode(),
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
			String hierarchyLevel, String name, String pageNumber, String pageSize, String orderBy, String sortBy)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataServiceImpl::getRegistrationCenterByHierarchyLevelAndTextPaginated()::entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();

		Map<String, String> pathsegements = new HashMap<String, String>();
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
				throw new ResidentServiceCheckedException(responseWrapper.getErrors().get(0).getErrorCode(),
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
				throw new ResidentServiceCheckedException(responseWrapper.getErrors().get(0).getErrorCode(),
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
	public ResponseWrapper<?> getLatestIdSchema(String schemaVersion, String domain, String type)
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
				throw new ResidentServiceCheckedException(responseWrapper.getErrors().get(0).getErrorCode(),
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

}
