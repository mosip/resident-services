package io.mosip.resident.service.impl;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.PartnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The Class PartnerServiceImpl.
 * @author Kamesh Shekhar Prasad
 */
@Component
public class PartnerServiceImpl implements PartnerService {

    @Autowired
    private ProxyPartnerManagementServiceImpl proxyPartnerManagementService;

    private static final Logger logger = LoggerConfiguration.logConfig(PartnerServiceImpl.class);

    @Override
    public ArrayList<String> getPartnerDetails(String partnerId) throws ResidentServiceCheckedException {
    	logger.debug("PartnerServiceImpl::getPartnerDetails()::entry");
        ArrayList<String> partnerIds = new ArrayList<>();
        try {
            if (partnerId != null) {
                ResponseWrapper<?> responseWrapper = proxyPartnerManagementService.getPartnersByPartnerType(Optional.of(partnerId));
                if (responseWrapper != null) {
                    Map<String, Object> partnerResponse = new LinkedHashMap<>((Map<String, Object>) responseWrapper.getResponse());
                    ArrayList<Object> partners = (ArrayList<Object>) partnerResponse.get("partners");
                    for (Object partner : partners) {
                        Map<String, Object> individualPartner = new LinkedHashMap<>((Map<String, Object>) partner);
                        partnerIds.add(individualPartner.get("partnerID").toString());
                    }
                }
            }
        } catch (Exception e) {
        	logger.error("Error occurred in getting partner details %s", e.getMessage());
            throw new ResidentServiceCheckedException(ResidentErrorCode.PARTNER_SERVICE_EXCEPTION.getErrorCode(),
                    ResidentErrorCode.PARTNER_SERVICE_EXCEPTION.getErrorMessage(), e);
        }
        logger.debug("PartnerServiceImpl::getPartnerDetails()::exit");
        return partnerIds;
    }
}
