package io.mosip.resident.util;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Kamesh Shekhar Prasad
 */

@Component
public class PerpetualVidUtility {

    private static final Logger logger = LoggerConfiguration.logConfig(PerpetualVidUtility.class);

    @Autowired
    private IdentityUtil identityUtil;

    @Autowired
    private PerpetualVidUtil perpetualVidUtil;

    public ResponseWrapper<List<Map<String,?>>> retrieveVids(String residentIndividualId, int timeZoneOffset, String locale) throws ResidentServiceCheckedException, ApisResourceAccessException {
        IdentityDTO identityDTO = identityUtil.getIdentity(residentIndividualId);
        return retrieveVids(timeZoneOffset, locale, identityDTO.getUIN());
    }

    public ResponseWrapper<List<Map<String, ?>>> retrieveVids(int timeZoneOffset, String locale, String uin)
            throws ResidentServiceCheckedException, ApisResourceAccessException {
        return perpetualVidUtil.retrieveVidsfromUin(uin, timeZoneOffset, locale);
    }

}
