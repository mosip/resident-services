package io.mosip.resident.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.exception.ResidentServiceCheckedException;

/**
 * Resident proxy partner management service class.
 * 
 * @author Ritik Jain
 */
@Service
public interface ProxyPartnerManagementService {

	/**
	 * Get partners by partner type.
	 * 
	 * @param partnerType
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	public ResponseWrapper<?> getPartnersByPartnerType(Optional<String> partnerType)
			throws ResidentServiceCheckedException;
	
	public ResponseWrapper<?> getPartnersByPartnerType(Optional<String> partnerType, ApiName apiUrl)
			throws ResidentServiceCheckedException;
	
	public Map<String, ?> getPartnerDetailFromPartnerId(String partnerId);

}
