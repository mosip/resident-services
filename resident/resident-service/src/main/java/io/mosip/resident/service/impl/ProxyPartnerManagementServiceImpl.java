package io.mosip.resident.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.mosip.resident.util.GetPartnersByPartnerTypeCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.ProxyPartnerManagementService;

/**
 * Resident proxy partner management service implementation class.
 * 
 * @author Ritik Jain
 */
@Component
public class ProxyPartnerManagementServiceImpl implements ProxyPartnerManagementService {

    private static final Logger logger = LoggerConfiguration.logConfig(ProxyPartnerManagementServiceImpl.class);

	@Autowired
	private GetPartnersByPartnerTypeCache getPartnersByPartnerTypeCache;

	@Override
	public ResponseWrapper<?> getPartnersByPartnerType(String partnerType)
			throws ResidentServiceCheckedException {
		return getPartnersByPartnerTypeCache.getPartnersByPartnerType(partnerType, ApiName.PARTNER_API_URL);
	}

	@SuppressWarnings("unchecked")
	@Cacheable(value = "partnerDetailCache", key = "#partnerId")
	@Override
	public Map<String, ?> getPartnerDetailFromPartnerIdAndPartnerType(String partnerId, String partnerType) {
		ResponseWrapper<?> response = null;
		try {
			response = getPartnersByPartnerTypeCache.getPartnersByPartnerType(partnerType, ApiName.PARTNER_DETAILS_NEW_URL);
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
}
