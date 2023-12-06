package io.mosip.resident.service.impl;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.util.ResidentServiceRestClient;
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

	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;
	
	@Autowired
	private IdentityServiceImpl identityServiceImpl;

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

			logger.debug("ProxyIdRepoServiceImpl::getRemainingUpdateCountByIndividualId()::exit");
			return responseWrapper;
			
		} catch (ApisResourceAccessException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new ResidentServiceCheckedException(API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
	}

}
