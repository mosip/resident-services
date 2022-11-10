package io.mosip.resident.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.controller.ResidentController;
import io.mosip.resident.dto.DownloadCardRequestDTO;
import io.mosip.resident.dto.DownloadHtml2PdfRequestDTO;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.DownloadCardService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.util.Utilitiy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Kamesh Shekhar Prasad
 * This class is used to create service class implementation of download card api.
 */
@Service
public class DownloadCardServiceImpl implements DownloadCardService {

    private static final String AID = "AID";
    private static final String LANGUAGE = "language";
    private static final String VALUE = "value";
    @Autowired
    private ResidentController residentController;

    @Autowired
    private Utilities utilities;

    @Autowired
    private AuditUtil audit;

    @Autowired
    private Environment env;

    @Autowired
    private ObjectStoreHelper objectStoreHelper;

    @Autowired
    private ResidentServiceRestClient residentServiceRestClient;

    @Autowired
    private IdAuthService idAuthService;

    @Autowired
    private ResidentServiceImpl residentService;

    @Autowired
    private Utilitiy utilitiy;

    @Autowired
    private IdentityServiceImpl identityService;

    @Autowired
    private TemplateUtil templateUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Environment environment;

    private static final Logger logger = LoggerConfiguration.logConfig(DownloadCardServiceImpl.class);

    @Override
    public byte[] getDownloadCardPDF(MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO) {
        String rid = null;
        try {
            if (idAuthService.validateOtp(downloadCardRequestDTOMainRequestDTO.getRequest().getTransactionId(),
                    getUINForIndividualId(downloadCardRequestDTOMainRequestDTO.getRequest().getIndividualId())
                    , downloadCardRequestDTOMainRequestDTO.getRequest().getOtp())) {
                String individualId = downloadCardRequestDTOMainRequestDTO.getRequest().getIndividualId();
                String idType = templateUtil.getIndividualIdType(individualId);
                if (idType.equalsIgnoreCase(AID)) {
                    rid = individualId;
                } else {
                    rid = utilities.getRidByIndividualId(individualId);
                }
            } else {
                logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                        LoggerFileConstant.APPLICATIONID.toString(),
                        ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
                audit.setAuditRequestDto(EventEnum.CREDENTIAL_REQ_EXCEPTION);
                throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
                        ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
            }
        } catch (ApisResourceAccessException e) {
            audit.setAuditRequestDto(EventEnum.RID_DIGITAL_CARD_REQ_EXCEPTION);
            throw new ResidentServiceException(
                    ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
                    ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
        } catch (OtpValidationFailedException e) {
            audit.setAuditRequestDto(EventEnum.REQ_CARD);
            throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(), e.getErrorText(),
                    e);
        }
        return residentService.getUINCard(rid);
    }

    @Override
    public byte[] getDownloadHtml2pdf(MainRequestDTO<DownloadHtml2PdfRequestDTO> downloadHtml2PdfRequestDTOMainRequestDTO) {
        String encodeHtml = downloadHtml2PdfRequestDTOMainRequestDTO.getRequest().getHtml();
        byte[] decodedData;
        String password;
        try {
            decodedData = CryptoUtil.decodeURLSafeBase64(encodeHtml);
            Map<String, Object> identityAttributes = (Map<String, Object>) identityService.getIdentityAttributes(
                    identityService.getResidentIndvidualId(), downloadHtml2PdfRequestDTOMainRequestDTO.getRequest().getSchemaType());
            String attributeProperty = this.environment.getProperty(ResidentConstants.PASSWORD_ATTRIBUTE);
            List<String> attributeList = List.of(attributeProperty.split("\\|"));
            List<String> attributeValues = new ArrayList<>();
            for (String attribute : attributeList) {
                Object attributeObject = identityAttributes.get(attribute);
                if (attributeObject instanceof List) {
                    List<Map<String, Object>> attributeMapObject = (List<Map<String, Object>>) attributeObject;
                    for (Map<String, Object> attributeInLanguage : attributeMapObject) {
                        String languageCode = utilities.getLanguageCode();
                        if (attributeInLanguage.containsKey(LANGUAGE) &&
                                attributeInLanguage.get(LANGUAGE).toString().equalsIgnoreCase(languageCode)) {
                            attributeValues.add((String) attributeInLanguage.get(VALUE));
                        }
                    }
                } else {
                    attributeValues.add((String) attributeObject);
                }
            }
            password = getPassword(attributeValues);

        }
        catch (Exception e) {
            audit.setAuditRequestDto(EventEnum.DOWNLOAD_CARD_HTML_2_PDF);
            logger.error("Unable to convert html to pdf RootCause- "+e);
            throw new ResidentServiceException(ResidentErrorCode.DOWNLOAD_CARD_HTML_2_PDF, e);
        }
        return utilitiy.signPdf(new ByteArrayInputStream(decodedData), password);
    }

    private String getPassword(List<String> attributeValues) {
        String pdfPwd = "";
        for(String attribute:attributeValues) {
            attribute = getFormattedPasswordAttribute(attribute);
            pdfPwd = pdfPwd.concat(attribute.substring(0, 4));
        }
        return pdfPwd;
    }

    private String getFormattedPasswordAttribute(String password){
        if(password.length()==3){
            return password=password.concat(password.substring(0,1));
        }else if(password.length()==2){
            return password=password.repeat(2);
        }else if(password.length()==1) {
            return password=password.repeat(4);
        }else {
            return password;
        }
    }

    private String getUINForIndividualId(String individualId)  {
        String idType = templateUtil.getIndividualIdType(individualId);
        if(idType.equalsIgnoreCase(IdType.UIN.toString()) || idType.equalsIgnoreCase(IdType.VID.toString())){
            return individualId;
        } else {
            try {
                return identityService.getIndividualIdForAid(individualId);
            } catch (ResidentServiceCheckedException e) {
                audit.setAuditRequestDto(EventEnum.RID_DIGITAL_CARD_REQ_EXCEPTION);
                throw new ResidentServiceException(
                        ResidentErrorCode.AID_NOT_FOUND.getErrorCode(),
                        ResidentErrorCode.AID_NOT_FOUND.getErrorMessage(), e);
            } catch (ApisResourceAccessException e) {
                audit.setAuditRequestDto(EventEnum.RID_DIGITAL_CARD_REQ_EXCEPTION);
                throw new ResidentServiceException(
                        ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
                        ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
            }
        }
    }

}
