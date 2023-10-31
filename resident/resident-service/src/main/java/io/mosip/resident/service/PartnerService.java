package io.mosip.resident.service;

import java.util.ArrayList;

import org.springframework.stereotype.Service;

import io.mosip.resident.exception.ResidentServiceCheckedException;

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
