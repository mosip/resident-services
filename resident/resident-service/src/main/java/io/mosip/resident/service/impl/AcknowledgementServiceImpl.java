package io.mosip.resident.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.pdfgenerator.spi.PDFGenerator;
import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.kernel.core.templatemanager.spi.TemplateManagerBuilder;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.signature.dto.PDFSignatureRequestDto;
import io.mosip.kernel.signature.dto.SignatureResponseDto;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.AuthResponseDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.AcknowledgementService;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TemplateUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.mosip.resident.constant.RegistrationConstants.DATETIME_PATTERN;

/**
 * This class is used to create service class implementation for getting acknowledgement API.
 * @Author Kamesh Shekhar Prasad
 */
@Service
public class AcknowledgementServiceImpl implements AcknowledgementService {

    private static final Logger logger = LoggerConfiguration.logConfig(AcknowledgementServiceImpl.class);

    @Autowired
    private ResidentTransactionRepository residentTransactionRepository;

    @Autowired
    private ProxyMasterdataServiceImpl proxyMasterdataServiceImpl;

    @Autowired
    private TemplateUtil templateUtil;

    @Autowired
    private Environment environment;

    @Autowired
    private ResidentServiceRestClient residentServiceRestClient;

    @Value("${resident.template.ack.share-cred-with-partner}")
    private String shareCredWithPartnerTemplate;

    @Value("${resident.template.ack.manage-my-vid}")
    private String manageMyVidTemplate;

    @Value("${resident.template.ack.order-a-physical-card}")
    private String orderAPhysicalCard;

    @Value("${resident.template.ack.download-a-personalized-card}")
    private String downloadAPersonalizedCard;

    @Value("${resident.template.ack.update-demographic-data}")
    private String updateDemographicData;

    @Value("${resident.template.ack.verify-email-id-or-phone-number}")
    private String verifyEmailIdOrPhoneNumber;

    @Value("${resident.template.ack.secure-my-id}")
    private String secureMyId;

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

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public byte[] getAcknowledgementPDF(String eventId, String languageCode) throws ResidentServiceCheckedException, IOException {
        logger.debug("AcknowledgementServiceImpl::getAcknowledgementPDF()::entry");

            Optional<ResidentTransactionEntity> residentTransactionEntity = residentTransactionRepository
                    .findById(eventId);
            String requestTypeCode;
            if (residentTransactionEntity.isPresent()) {
                requestTypeCode = residentTransactionEntity.get().getRequestTypeCode();
            } else {
                throw new ResidentServiceCheckedException(ResidentErrorCode.EVENT_STATUS_NOT_FOUND);
            }
            String requestProperty = getRequestTypeProperty(requestTypeCode);

            ResponseWrapper<?> responseWrapper = proxyMasterdataServiceImpl.
                    getAllTemplateBylangCodeAndTemplateTypeCode(languageCode, requestProperty);

            Map<String, Object> templateResponse = new LinkedHashMap<>((Map<String, Object>) responseWrapper.getResponse());
            String fileText = (String) templateResponse.get("fileText");
            Map<String, String> templateVariables = RequestType.valueOf(requestTypeCode).getAckTemplateVariables(templateUtil, eventId);
            InputStream stream = new ByteArrayInputStream(fileText.getBytes(StandardCharsets.UTF_8));
            InputStream templateValue = templateManager.merge(stream, convertMapValueFromStringToObject(templateVariables));
            ByteArrayOutputStream pdfValue= (ByteArrayOutputStream)pdfGenerator.generate(templateValue);
            logger.debug("AcknowledgementServiceImpl::getAcknowledgementPDF()::exit");
            return signAcknowledgementPdf(pdfValue, null);

    }

    public String getRequestTypeProperty(String requestTypeCode) throws ResidentServiceCheckedException {
        if(requestTypeCode.equals(RequestType.SHARE_CRED_WITH_PARTNER.toString())){
            return shareCredWithPartnerTemplate;
        }else if(requestTypeCode.equals(RequestType.GENERATE_VID.toString()) || requestTypeCode.equals(RequestType.REVOKE_VID.toString())){
            return manageMyVidTemplate;
        }else if(requestTypeCode.equals(RequestType.ORDER_PHYSICAL_CARD.toString())){
            return orderAPhysicalCard;
        }else if(requestTypeCode.equals(RequestType.DOWNLOAD_PERSONALIZED_CARD.toString())){
            return downloadAPersonalizedCard;
        }else if(requestTypeCode.equals(RequestType.UPDATE_MY_UIN.toString())){
            return updateDemographicData;
        }else if(requestTypeCode.equals(RequestType.VERIFY_PHONE_EMAIL.toString())){
            return verifyEmailIdOrPhoneNumber;
        }else if(requestTypeCode.equals(RequestType.AUTH_TYPE_LOCK_UNLOCK.toString())){
            return secureMyId;
        }else {
            throw new ResidentServiceCheckedException(ResidentErrorCode.ACK_PROPERTY_NOT_FOUND);
        }
    }

    public Map<String, Object> convertMapValueFromStringToObject(Map<String, String> templateVariables) {
        Map<String, Object> templateMapObject = new HashMap<>();
        for(Map.Entry<String, String> entry: templateVariables.entrySet()){
            templateMapObject.put(entry.getKey(), entry.getValue());
        }
        return templateMapObject;
    }

    public byte[] signAcknowledgementPdf(ByteArrayOutputStream out, String password) {
        logger.debug("UinCardGeneratorImpl::generateUinCard()::entry");
        byte[] pdfSignatured=null;
        try {
            PDFSignatureRequestDto request = new PDFSignatureRequestDto(
                    Integer.parseInt(Objects.requireNonNull(environment.getProperty(ResidentConstants.LOWER_LEFT_X))),
                    Integer.parseInt(Objects.requireNonNull(environment.getProperty(ResidentConstants.LOWER_LEFT_Y))),
                    Integer.parseInt(Objects.requireNonNull(environment.getProperty(ResidentConstants.UPPER_RIGHT_X))),
                    Integer.parseInt(Objects.requireNonNull(environment.getProperty(ResidentConstants.UPPER_RIGHT_Y))),
                    environment.getProperty(ResidentConstants.REASON), 1, password);
            request.setApplicationId("KERNEL");
            request.setReferenceId("SIGN");
            request.setData(Base64.encodeBase64String(out.toByteArray()));
            DateTimeFormatter format = DateTimeFormatter.ofPattern(Objects.requireNonNull(environment.getProperty(DATETIME_PATTERN)));
            LocalDateTime localdatetime = LocalDateTime
                    .parse(DateUtils.getUTCCurrentDateTimeString(Objects.requireNonNull(environment.getProperty(DATETIME_PATTERN))), format);

            request.setTimeStamp(DateUtils.getUTCCurrentDateTimeString());
            RequestWrapper<PDFSignatureRequestDto> requestWrapper = new RequestWrapper<>();

            requestWrapper.setRequest(request);
            requestWrapper.setRequesttime(localdatetime);
            ResponseWrapper<?> responseWrapper;
            SignatureResponseDto signatureResponseDto;

            responseWrapper= residentServiceRestClient.postApi(environment.getProperty(ApiName.PDFSIGN.name())
                    , MediaType.APPLICATION_JSON,requestWrapper, ResponseWrapper.class);

            if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
                ServiceError error = responseWrapper.getErrors().get(0);
                throw new ResidentServiceException(ResidentErrorCode.valueOf(error.getMessage()));
            }
            signatureResponseDto = objectMapper.readValue(objectMapper.writeValueAsString(responseWrapper.getResponse()),
                    SignatureResponseDto.class);

            pdfSignatured = Base64.decodeBase64(signatureResponseDto.getData());

        } catch (Exception e) {
            logger.error(io.mosip.kernel.pdfgenerator.itext.constant.PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorMessage(),e.getMessage()
                    + ExceptionUtils.getStackTrace(e));
        }
        logger.debug("UinCardGeneratorImpl::generateUinCard()::exit");

        return pdfSignatured;
    }

}

