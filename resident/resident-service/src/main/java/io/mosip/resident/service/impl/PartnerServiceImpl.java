package io.mosip.resident.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.RetrievePartnerDetailsResponse;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.PartnerService;
import io.mosip.resident.util.ResidentServiceRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class PartnerServiceImpl.
 * @author Kamesh Shekhar Prasad
 */
@Component
public class PartnerServiceImpl implements PartnerService {

    @Value("${mosip.pms.pmp.partner.rest.uri}")
    private String partnerServiceUrl;

    @Autowired
    @Qualifier("restClientWithSelfTOkenRestTemplate")
    private ResidentServiceRestClient restClientWithSelfTOkenRestTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public RetrievePartnerDetailsResponse getPartnerDetails(String partnerId) throws ResidentServiceCheckedException {

        try{
            if(partnerId!= null){
                Map<String, String> pathsegments = new HashMap<String, String>();
               URI uri = URI.create(partnerServiceUrl);
               ResponseWrapper<?> responseWrapper = restClientWithSelfTOkenRestTemplate.getApi(uri, ResponseWrapper.class);
                System.out.println("responseWrapper"+responseWrapper);
                Map<String, ?> partnerResponse = new LinkedHashMap<>((Map<String, Object>) responseWrapper.getResponse());
                System.out.println("partnerResponse"+partnerResponse);
                Map<String, ?> identity = (Map<String, ?>) partnerResponse.get("partners");
                         return null;
            }
        } catch (Exception e) {
            throw new ResidentServiceCheckedException(ResidentErrorCode.PARTNER_SERVICE_EXCEPTION.getErrorCode(),
                    ResidentErrorCode.PARTNER_SERVICE_EXCEPTION.getErrorMessage(), e);
        }
        return null;
    }
}
