package io.mosip.resident.service.impl;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.AcknowledgementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AcknowledgementServiceImpl implements AcknowledgementService {

    private static final Logger logger = LoggerConfiguration.logConfig(AcknowledgementServiceImpl.class);

    @Autowired
    private ResidentTransactionRepository residentTransactionRepository;

    @Autowired
    private ProxyMasterdataServiceImpl proxyMasterdataServiceImpl;

    @Override
    public byte[] getAcknowledgementPDF(String eventId, String languageCode) {
        logger.debug("AcknowledgementServiceImpl::getAcknowledgementPDF()::entry");
        try{
//            String requestTypeCode = residentTransactionRepository.findById(eventId).get().getRequestTypeCode();
            String requestTypeCode = "tnc-order-a-physical-card";
            ResponseWrapper<?> responseWrapper = proxyMasterdataServiceImpl.
                    getAllTemplateBylangCodeAndTemplateTypeCode(languageCode, requestTypeCode);
            System.out.println(responseWrapper.getResponse());
            Map<String, Object> templateResponse = new LinkedHashMap<>((Map<String, Object>) responseWrapper.getResponse());
            String fileText = (String) templateResponse.get("fileText");
            System.out.println(fileText);
        }catch (Exception e){
            logger.error("AcknowledgementServiceImpl::getAcknowledgementPDF()::error::"+e.getMessage());
        }

        return new byte[0];
    }

}

