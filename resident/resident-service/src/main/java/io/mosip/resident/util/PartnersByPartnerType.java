package io.mosip.resident.util;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Kamesh Shekhar Prasad
 */

@Component
public class PartnersByPartnerType {

    private static final Logger logger = LoggerConfiguration.logConfig(PartnersByPartnerType.class);

    @Autowired
    private ResidentServiceRestClient residentServiceRestClient;

    public ResponseWrapper<?> getPartnersByPartnerType(Optional<String> partnerType, ApiName apiUrl)
            throws ResidentServiceCheckedException {
        logger.debug("GetPartnersByPartnerType::getPartnersByPartnerType()::entry");
        ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();

        List<String> pathsegements = null;

        List<String> queryParamName = new ArrayList<String>();
        if(partnerType.isPresent()) {
            queryParamName.add(ResidentConstants.PARTNER_TYPE);
        }

        List<Object> queryParamValue = new ArrayList<>();
        if(partnerType.isPresent()) {
            queryParamValue.add(partnerType.get());
        }

        try {
            responseWrapper = (ResponseWrapper<?>) residentServiceRestClient.getApi(apiUrl,
                    pathsegements, queryParamName, queryParamValue, ResponseWrapper.class);

            if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
                logger.error(responseWrapper.getErrors().get(0).toString());
                throw new ResidentServiceCheckedException(responseWrapper.getErrors().get(0).getErrorCode(),
                        responseWrapper.getErrors().get(0).getMessage());
            }
        } catch (ApisResourceAccessException e) {
            logger.error("Error occured in accessing partners list %s", e.getMessage());
            throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
                    ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
        }
        logger.debug("GetPartnersByPartnerType::getPartnersByPartnerType()::exit");
        return responseWrapper;
    }
}
