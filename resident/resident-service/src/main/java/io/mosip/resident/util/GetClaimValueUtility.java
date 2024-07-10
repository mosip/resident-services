package io.mosip.resident.util;

import io.mosip.resident.exception.ApisResourceAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Kamesh Shekhar Prasad
 */

@Component
public class GetClaimValueUtility {

	@Autowired
	GetAvailableClaimValueUtility getAvailableClaimValueUtility;

    public String getClaimValue(String claim) throws ApisResourceAccessException {
		return getAvailableClaimValueUtility.getClaims(claim).get(claim);
	}
}
