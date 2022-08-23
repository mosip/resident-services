package io.mosip.resident.service.impl;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.pdfgenerator.spi.PDFGenerator;
import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.kernel.core.templatemanager.spi.TemplateManagerBuilder;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.AcknowledgementService;
import io.mosip.resident.util.TemplateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AcknowledgementServiceImpl implements AcknowledgementService {

    private static final Logger logger = LoggerConfiguration.logConfig(AcknowledgementServiceImpl.class);

    @Autowired
    private ResidentTransactionRepository residentTransactionRepository;

    @Autowired
    private ProxyMasterdataServiceImpl proxyMasterdataServiceImpl;

    @Autowired
    private TemplateUtil templateUtil;

    @Value("${resident.template.ack.share-cred-with-partner}")
    private String shareCredWithPartnerTemplate;

    private static final String CLASSPATH = "classpath";
    private static final String ENCODE_TYPE = "UTF-8";

    private TemplateManager templateManager;

    @Autowired
    private TemplateManagerBuilder templateManagerBuilder;

    @PostConstruct
    public void idTemplateManagerPostConstruct() {
        templateManager = templateManagerBuilder.encodingType(ENCODE_TYPE).enableCache(false).resourceLoader(CLASSPATH)
                .build();
    }

    @Autowired
    private PDFGenerator pdfGenerator;

    @Override
    public byte[] getAcknowledgementPDF(String eventId, String languageCode) {
        logger.debug("AcknowledgementServiceImpl::getAcknowledgementPDF()::entry");
        try{
//            String requestTypeCode = residentTransactionRepository.findById(eventId).get().getRequestTypeCode();
            String requestTypeCode = "SHARE_CRED_WITH_PARTNER";
            String requestProperty = "";
            if(requestTypeCode.equals(RequestType.SHARE_CRED_WITH_PARTNER.toString())){
                requestProperty = shareCredWithPartnerTemplate;
            }
            ResponseWrapper<?> responseWrapper = proxyMasterdataServiceImpl.
                    getAllTemplateBylangCodeAndTemplateTypeCode(languageCode, requestProperty);
            System.out.println(responseWrapper.getResponse());
            Map<String, Object> templateResponse = new LinkedHashMap<>((Map<String, Object>) responseWrapper.getResponse());
            String fileText = (String) templateResponse.get("fileText");

            Map<String, String> templateVariables = RequestType.valueOf(requestTypeCode).getAckTemplateVariables(templateUtil, eventId);
            InputStream stream = new ByteArrayInputStream(fileText.getBytes(StandardCharsets.UTF_8));
            InputStream templateValue = templateManager.merge(stream, convertMapValueFromStringToObject(templateVariables));
            System.out.println(templateValue);

            ByteArrayOutputStream pdfValue= (ByteArrayOutputStream)pdfGenerator.generate(templateValue);
            logger.debug("AcknowledgementServiceImpl::getAcknowledgementPDF()::exit");
            return pdfValue.toByteArray();
        }catch (Exception e){
            logger.error("AcknowledgementServiceImpl::getAcknowledgementPDF()::error::"+e.getMessage());
        }

        return new byte[0];
    }

    private Map<String, Object> convertMapValueFromStringToObject(Map<String, String> templateVariables) {
        Map<String, Object> templateMapObject = new HashMap<>();
        for(Map.Entry<String, String> entry: templateVariables.entrySet()){
            templateMapObject.put(entry.getKey(), entry.getValue());
        }
        return templateMapObject;
    }

}

