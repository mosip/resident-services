package io.mosip.resident.util;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Kamesh Shekhar Prasad
 */

@Component
public class GetAvailableClaimValueUtility {

    private static final Logger logger = LoggerConfiguration.logConfig(GetAvailableClaimValueUtility.class);

    @Autowired
    private GetAccessTokenUtility getAccessTokenUtility;

    @Autowired
    private UserInfoUtility userInfoUtility;

    private String getClaimFromUserInfo(Map<String, Object> userInfo, String claim) {
        Object claimValue = userInfo.get(claim);
        if(claimValue == null) {
            throw new ResidentServiceException(ResidentErrorCode.CLAIM_NOT_AVAILABLE, claim);
        }
        return String.valueOf(claimValue);
    }

    public Map<String, String> getClaimsFromToken(Set<String> claims, String token) throws ApisResourceAccessException {
        Map<String, Object> userInfo = userInfoUtility.getUserInfo(token);
        return claims.stream().map(claim -> new AbstractMap.SimpleEntry<>(claim, getClaimFromUserInfo(userInfo, claim)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, String> getClaims(String... claims) throws ApisResourceAccessException {
        return getClaims(Set.of(claims));
    }

    public Map<String, String> getClaims(Set<String> claims) throws ApisResourceAccessException {
        String accessToken = getAccessTokenUtility.getAccessToken();
        if (!Objects.equals(accessToken, "")) {
            return getClaimsFromToken(claims, accessToken);
        }
        return Map.of();
    }

    public String getAvailableClaimValue(String claim) throws ApisResourceAccessException {
        logger.debug("GetAvailableClaimValueUtility::getAvailableClaimValue()::entry");
        String claimValue;
        try {
            claimValue = getClaims(claim).get(claim);
        } catch (ResidentServiceException e) {
            logger.error(e.getMessage());
            claimValue = null;
        }
        logger.debug("GetAvailableClaimValueUtility::getAvailableClaimValue()::exit");
        return claimValue;
    }
}
