package io.mosip.resident.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ProxyPartnerManagementService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.ResidentServiceRestClient;

/**
 * Resident proxy partner management service implementation class.
 * 
 * @author Ritik Jain
 */
@Component
public class ProxyPartnerManagementServiceImpl implements ProxyPartnerManagementService {

	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;

	@Autowired
	private AuditUtil auditUtil;

	private static final Logger logger = LoggerConfiguration.logConfig(ProxyPartnerManagementServiceImpl.class);

	@Override
	public ResponseWrapper<?> getPartnersByPartnerType(Optional<String> partnerType)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyPartnerManagementServiceImpl::getPartnersByPartnerType()::entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();

		List<String> pathsegements = null;

		List<String> queryParamName = new ArrayList<String>();
		queryParamName.add("partnerType");

		List<Object> queryParamValue = new ArrayList<>();
		if(partnerType.isPresent()) {
			queryParamValue.add(partnerType.get());
		}

		try {
			responseWrapper = (ResponseWrapper<?>) residentServiceRestClient.getApi(ApiName.PARTNER_API_URL,
					pathsegements, queryParamName, queryParamValue, ResponseWrapper.class);

			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				logger.debug(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(responseWrapper.getErrors().get(0).getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_PARTNERS_BY_PARTNER_TYPE_EXCEPTION);
			logger.error("Error occured in accessing partners list %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyPartnerManagementServiceImpl::getPartnersByPartnerType()::exit");
		return responseWrapper;
	}

}
