package io.mosip.resident.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.mosip.resident.util.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.ProxyPartnerManagementService;
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
	private Utility utility;

	private static final Logger logger = LoggerConfiguration.logConfig(ProxyPartnerManagementServiceImpl.class);

	@Override
	public ResponseWrapper<?> getPartnersByPartnerType(String partnerType)
			throws ResidentServiceCheckedException {
		return utility.getPartnersByPartnerType(partnerType, ApiName.PARTNER_API_URL);
	}

	@Override
	public ResponseWrapper<?> getPartnersByPartnerType(Optional<String> partnerType, ApiName apiUrl)
			throws ResidentServiceCheckedException {
		logger.debug("ProxyPartnerManagementServiceImpl::getPartnersByPartnerType()::entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();

		List<String> pathsegements = null;

		List<String> queryParamName = new ArrayList<String>();
		if(partnerType.isPresent()) {
			queryParamName.add(ResidentConstants.PARTNER_TYPE);
		}

		List<Object> queryParamValue = new ArrayList<>();
		if(partnerType.isPresent()) {
			queryParamValue.add(partnerType.get());
		}

		try {
			responseWrapper = (ResponseWrapper<?>) residentServiceRestClient.getApi(apiUrl,
					pathsegements, queryParamName, queryParamValue, ResponseWrapper.class);

			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				logger.error(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(responseWrapper.getErrors().get(0).getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			logger.error("Error occured in accessing partners list %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyPartnerManagementServiceImpl::getPartnersByPartnerType()::exit");
		return responseWrapper;
	}
	
	@SuppressWarnings("unchecked")
	@Cacheable(value = "partnerDetailCache", key = "#partnerId")
	@Override
	public Map<String, ?> getPartnerDetailFromPartnerIdAndPartnerType(String partnerId, String partnerType) {
		ResponseWrapper<?> response = null;
		try {
			response = utility.getPartnersByPartnerType(partnerType, ApiName.PARTNER_DETAILS_NEW_URL);
		} catch (ResidentServiceCheckedException e) {
			throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		Map<String, Object> partnerResponse = new LinkedHashMap<>((Map<String, Object>) response.getResponse());
        List<Map<String,?>> partners = (List<Map<String, ?>>) partnerResponse.get(ResidentConstants.PARTNERS);
        return partners.stream()
        		.filter(map -> ((String)map.get(ResidentConstants.PMS_PARTNER_ID)).equals(partnerId))
        		.findAny()
        		.orElse(Map.of());
	}

	@CacheEvict(value = "partnerDetailCache", allEntries = true)
	@Scheduled(fixedRateString = "${resident.cache.expiry.time.millisec.partnerCache}")
	public void emptyPartnerDetailCache() {
		logger.info("Emptying Partner detail cache");
	}
}
