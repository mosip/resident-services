package io.mosip.resident.util;

import io.mosip.idrepository.core.util.TokenIDGenerator;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AvailableClaimUtility {

    private static final Logger logger = LoggerConfiguration.logConfig(IdentityServiceImpl.class);

    @Autowired
    private GetAccessTokenUtility getAccessTokenUtility;

    @Autowired
    private UserInfoUtility userInfoUtility;


    @Autowired
    private TokenIDGenerator tokenIDGenerator;

    @Value("${ida.online-verification-partner-id}")
    private String onlineVerificationPartnerId;

    private static final String INDIVIDUAL_ID = "individual_id";

    @Autowired
    private UinVidValidator uinVidValidator;

    public String getIDAToken(String uin) {
        return getIDAToken(uin, onlineVerificationPartnerId);
    }

    public String getIDAToken(String uin, String olvPartnerId) {
		return tokenIDGenerator.generateTokenID(uin, olvPartnerId);
	}


    public  String getIDATokenForIndividualId(String idvid) throws ResidentServiceCheckedException {
        return getIDAToken(uinVidValidator.getUinForIndividualId(idvid));
    }

    public String getResidentIdaToken() throws ApisResourceAccessException, ResidentServiceCheckedException {
		return getIDATokenForIndividualId(getResidentIndvidualIdFromSession());
	}

    public String getResidentIndvidualIdFromSession() throws ApisResourceAccessException {
        return  getClaimValue(INDIVIDUAL_ID);
    }

    public String getClaimValue(String claim) throws ApisResourceAccessException {
		return getClaims(claim).get(claim);
	}

    public String getAvailableClaimValue(String claim) throws ApisResourceAccessException {
        logger.debug("IdentityServiceImpl::getAvailableClaimValue()::entry");
        String claimValue;
        try {
            claimValue = getClaims(claim).get(claim);
        } catch (ResidentServiceException e) {
            logger.error(e.getMessage());
            claimValue = null;
        }
        logger.debug("IdentityServiceImpl::getAvailableClaimValue()::exit");
        return claimValue;
    }

    public Map<String, String> getClaims(String... claims) throws ApisResourceAccessException {
        return getClaims(Set.of(claims));
    }

    private Map<String, String> getClaims(Set<String> claims) throws ApisResourceAccessException {
        String accessToken = getAccessTokenUtility.getAccessToken();
        if (!Objects.equals(accessToken, "")) {
            return getClaimsFromToken(claims, accessToken);
        }
        return Map.of();
    }

    public Map<String, String> getClaimsFromToken(Set<String> claims, String token) throws ApisResourceAccessException {
        Map<String, Object> userInfo = userInfoUtility.getUserInfo(token);
        return claims.stream().map(claim -> new AbstractMap.SimpleEntry<>(claim, getClaimFromUserInfo(userInfo, claim)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private String getClaimFromUserInfo(Map<String, Object> userInfo, String claim) {
        Object claimValue = userInfo.get(claim);
        if(claimValue == null) {
            throw new ResidentServiceException(ResidentErrorCode.CLAIM_NOT_AVAILABLE, claim);
        }
        return String.valueOf(claimValue);
    }
}
