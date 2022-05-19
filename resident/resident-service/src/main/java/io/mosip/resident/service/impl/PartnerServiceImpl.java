//package io.mosip.resident.service.impl;
//
//import io.mosip.resident.constant.ResidentErrorCode;
//import io.mosip.resident.dto.RetrievePartnerDetailsResponse;
//import io.mosip.resident.exception.ResidentServiceCheckedException;
//import io.mosip.resident.service.PartnerService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.env.Environment;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
///**
// * The Class PartnerServiceImpl.
// * @author Kamesh Shekhar Prasad
// */
//@Component
//public class PartnerServiceImpl implements PartnerService {
//
//    @Value("${mosip.idrepo.auth.secret-key}")
//    private String secretKey;
//
//    @Value("${mosip.idrepo.auth.client-id}")
//    private String clientId;
//
//    @Value("${partner.service.url}")
//    private String partnerServiceUrl;
//
//    @Value("${mosip.idrepo.auth.app-id}")
//    private String appId;
//
//    @Autowired
//    @Qualifier("selfTokenRestTemplate")
//    RestTemplate restTemplate;
//
//
//    @Override
//    public RetrievePartnerDetailsResponse getPartnerDetails(String partnerId) throws ResidentServiceCheckedException {
//
//        try{
//            if(partnerId!= null){
//                String url = partnerServiceUrl+"/partner/v1.0/partner/{partnerId}";
//                System.out.println(restTemplate.getForObject(url,RetrievePartnerDetailsResponse.class,partnerId));
//                return null;
//            }
//
//        } catch (Exception e) {
//            throw new ResidentServiceCheckedException(ResidentErrorCode.PARTNER_SERVICE_EXCEPTION.getErrorCode(),
//                    ResidentErrorCode.PARTNER_SERVICE_EXCEPTION.getErrorMessage(), e);
//        }
//
//        return null;
//    }
//}
