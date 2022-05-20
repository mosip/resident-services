package io.mosip.resident.service;

import io.mosip.resident.dto.RetrievePartnerDetailsResponse;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Partner service class.
 * @author Kamesh Shekhar Prasad
 */

@Service
public interface PartnerService {

    /**
     * Get partner details by partner id.
     * @param partnerId
     * @return partner details
     */
    public ArrayList<String> getPartnerDetails(String partnerId) throws ResidentServiceCheckedException;

}
