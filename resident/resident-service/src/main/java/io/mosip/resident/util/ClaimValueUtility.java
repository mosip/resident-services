package io.mosip.resident.util;

import io.mosip.resident.exception.ApisResourceAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Kamesh Shekhar Prasad
 */

@Component
public class ClaimValueUtility {

	@Autowired
    AvailableClaimValueUtility availableClaimValueUtility;

    public String getClaimValue(String claim) throws ApisResourceAccessException {
		return availableClaimValueUtility.getClaims(claim).get(claim);
	}
}
