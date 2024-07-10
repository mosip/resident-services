package io.mosip.resident.util;

import io.mosip.idrepository.core.util.TokenIDGenerator;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Kamesh Shekhar Prasad
 */

@Component
public class AvailableClaimUtility {

    private static final Logger logger = LoggerConfiguration.logConfig(AvailableClaimUtility.class);

    @Autowired
    private TokenIDGenerator tokenIDGenerator;

    @Value("${ida.online-verification-partner-id}")
    private String onlineVerificationPartnerId;

    private static final String INDIVIDUAL_ID = "individual_id";

    @Autowired
    private UinForIndividualId uinForIndividualId;

    @Autowired
    private GetClaimValueUtility getClaimValueUtility;

    public String getIDAToken(String uin) {
        return getIDAToken(uin, onlineVerificationPartnerId);
    }

    public String getIDAToken(String uin, String olvPartnerId) {
		return tokenIDGenerator.generateTokenID(uin, olvPartnerId);
	}


    public  String getIDATokenForIndividualId(String idvid) throws ResidentServiceCheckedException {
        return getIDAToken(uinForIndividualId.getUinForIndividualId(idvid));
    }

    public String getResidentIdaToken() throws ApisResourceAccessException, ResidentServiceCheckedException {
		return getIDATokenForIndividualId(getResidentIndvidualIdFromSession());
	}

    public String getResidentIndvidualIdFromSession() throws ApisResourceAccessException {
        return  getClaimValueUtility.getClaimValue(INDIVIDUAL_ID);
    }
}
