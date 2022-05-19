package io.mosip.resident.service;

import io.mosip.resident.dto.RetrievePartnerDetailsResponse;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.springframework.stereotype.Service;

/**
 * Partner service class.
 * @author Kamesh Shekhar Prasad
 */

@Service
public interface PartnerService {

    /**
     * Get partner details by partner id.
     * @param partnerId
     * @return RetrievePartnerDetailsResponse object
     */
    public RetrievePartnerDetailsResponse getPartnerDetails(String partnerId) throws ResidentServiceCheckedException;

}
