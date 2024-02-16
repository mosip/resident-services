package io.mosip.resident.service.impl;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.validator.RequestValidator;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.mosip.resident.constant.ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION;

/**
 * @author Manoj SP
 *
 */
@Service
public class ProxyIdRepoServiceImpl implements ProxyIdRepoService {

	private static final Logger logger = LoggerConfiguration.logConfig(ProxyIdRepoServiceImpl.class);
	private static final String NO_RECORDS_FOUND_ID_REPO_ERROR_CODE = "IDR-IDC-007";
	private static final int ZERO = 0;
	private static final String INVALID_INPUT_PARAMETER_ID_REPO_ERROR_CODE = "IDR-IDC-002";

	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;
	
	@Autowired
	private IdentityServiceImpl identityServiceImpl;

	@Autowired
	private RequestValidator requestValidator;

	@Override
	public ResponseWrapper<?> getRemainingUpdateCountByIndividualId(List<String> attributeList)
			throws ResidentServiceCheckedException {
		try {
			logger.debug("ProxyIdRepoServiceImpl::getRemainingUpdateCountByIndividualId()::entry");
			String individualId=identityServiceImpl.getResidentIndvidualIdFromSession();
			Map<String, Object> pathsegements = new HashMap<String, Object>();
			pathsegements.put("individualId", individualId);
			
			List<String> queryParamName = new ArrayList<String>();
			queryParamName.add("attribute_list");

			List<Object> queryParamValue = new ArrayList<>();
			queryParamValue.add(Objects.isNull(attributeList) ? "" : attributeList.stream().collect(Collectors.joining(",")));
			
			ResponseWrapper<?> responseWrapper = residentServiceRestClient.getApi(ApiName.IDREPO_IDENTITY_UPDATE_COUNT,
					pathsegements, queryParamName, queryParamValue, ResponseWrapper.class);
			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()){
				if(responseWrapper.getErrors().get(ZERO) != null && !responseWrapper.getErrors().get(ZERO).toString().isEmpty() &&
						responseWrapper.getErrors().get(ZERO).getErrorCode() != null &&
						!responseWrapper.getErrors().get(ZERO).getErrorCode().isEmpty() &&
						responseWrapper.getErrors().get(ZERO).getErrorCode().equalsIgnoreCase(NO_RECORDS_FOUND_ID_REPO_ERROR_CODE)) {
					throw new ResidentServiceCheckedException(ResidentErrorCode.NO_RECORDS_FOUND);
				}else {
					throw new ResidentServiceCheckedException(ResidentErrorCode.UNKNOWN_EXCEPTION);
				}
			}
			logger.debug("ProxyIdRepoServiceImpl::getRemainingUpdateCountByIndividualId()::exit");
			return responseWrapper;
			
		} catch (ApisResourceAccessException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new ResidentServiceCheckedException(API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
	}

	@Override
	public ResponseWrapper<?> getPendingDrafts() throws ResidentServiceCheckedException {
		try {
			logger.debug("ProxyIdRepoServiceImpl::getPendingDrafts()::entry");
			String individualId=identityServiceImpl.getResidentIndvidualIdFromSession();
			if(!requestValidator.validateUin(individualId)){
				individualId = identityServiceImpl.getUinForIndividualId(individualId);
			}
			Map<String, Object> pathsegements = new HashMap<String, Object>();
			pathsegements.put("UIN", individualId);

			ResponseWrapper<?> responseWrapper = residentServiceRestClient.getApi(ApiName.IDREPO_IDENTITY_GET_DRAFT_UIN,
					pathsegements, ResponseWrapper.class);
			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()){
				if(responseWrapper.getErrors().get(ZERO) != null && !responseWrapper.getErrors().get(ZERO).toString().isEmpty() &&
						responseWrapper.getErrors().get(ZERO).getErrorCode() != null &&
						!responseWrapper.getErrors().get(ZERO).getErrorCode().isEmpty() &&
						responseWrapper.getErrors().get(ZERO).getErrorCode().equalsIgnoreCase(INVALID_INPUT_PARAMETER_ID_REPO_ERROR_CODE)) {
					throw new InvalidInputException(IdType.UIN.name());
				}else {
					throw new ResidentServiceCheckedException(ResidentErrorCode.UNKNOWN_EXCEPTION);
				}
			}
			logger.debug("ProxyIdRepoServiceImpl::getPendingDrafts()::exit");
			return responseWrapper;

		} catch (ApisResourceAccessException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new ResidentServiceCheckedException(API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
	}

}
