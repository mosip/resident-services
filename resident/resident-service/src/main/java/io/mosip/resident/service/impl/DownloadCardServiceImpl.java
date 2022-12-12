package io.mosip.resident.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.controller.ResidentController;
import io.mosip.resident.dto.CredentialReqestDto;
import io.mosip.resident.dto.DownloadCardRequestDTO;
import io.mosip.resident.dto.DownloadPersonalizedCardDto;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentCredentialResponseDto;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.VidDownloadCardResponseDto;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.DownloadCardService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.util.Utilitiy;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.mosip.resident.constant.RegistrationConstants.SUCCESS;
import static io.mosip.resident.constant.TemplateVariablesConstants.NAME;
import static io.mosip.resident.constant.TemplateVariablesConstants.VID;
import static io.mosip.resident.constant.TemplateVariablesConstants.VID_TYPE;

/**
 * @author Kamesh Shekhar Prasad
 * This class is used to create service class implementation of download card api.
 */
@Service
public class DownloadCardServiceImpl implements DownloadCardService {

    private static final String AID = "AID";
    private static final String LANGUAGE = "language";
    private static final String VALUE = "value";
    private static final String MASKED_VID = "maskedVid";
    private static final String EXPIRY_TIMESTAMP = "expiryTimestamp";
    private static final String GENERATED_ON_TIMESTAMP = "genratedOnTimestamp";
    private static final String TRANSACTION_LIMIT = "transactionLimit";
    private static final String TRANSACTION_COUNT = "transactionCount";
    private static final String NEW = "NEW";
    private static final String FIRST_NAME = "firstName";
    private static final String DATE_OF_BIRTH = "dateOfBirth";
    @Autowired
    private ResidentController residentController;

    @Autowired
    private Utilities utilities;

    @Autowired
    private AuditUtil audit;

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

    @Autowired
    private ResidentTransactionRepository residentTransactionRepository;

    @Autowired
    private ResidentVidService vidService;

    private static final Logger logger = LoggerConfiguration.logConfig(DownloadCardServiceImpl.class);

    @Override
    public Tuple2<byte[], String> getDownloadCardPDF(MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO) {
        String rid = null;
        String eventId = "";
        try {
            if (idAuthService.validateOtp(downloadCardRequestDTOMainRequestDTO.getRequest().getTransactionId(),
                    getUINForIndividualId(downloadCardRequestDTOMainRequestDTO.getRequest().getIndividualId())
                    , downloadCardRequestDTOMainRequestDTO.getRequest().getOtp())) {
                String individualId = downloadCardRequestDTOMainRequestDTO.getRequest().getIndividualId();
                String idType = identityService.getIndividualIdType(individualId);
                if (idType.equalsIgnoreCase(AID)) {
                    rid = individualId;
                } else {
                    rid = utilities.getRidByIndividualId(individualId);
                }
				ResidentTransactionEntity residentTransactionEntity = residentTransactionRepository.findByAid(rid);
				if (residentTransactionEntity != null) {
					eventId = residentTransactionEntity.getEventId();
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
        return Tuples.of(residentService.getUINCard(rid), eventId);
    }

    @Override
    public byte[] downloadPersonalizedCard(MainRequestDTO<DownloadPersonalizedCardDto> downloadPersonalizedCardMainRequestDTO) {
        String encodeHtml = downloadPersonalizedCardMainRequestDTO.getRequest().getHtml();
        byte[] decodedData;
        String password=null;
        try {
            decodedData = CryptoUtil.decodePlainBase64(encodeHtml);
            List<String> attributeValues = getAttributeList();
            if(Boolean.parseBoolean(this.environment.getProperty(ResidentConstants.IS_PASSWORD_FLAG_ENABLED))){
                password = utilitiy.getPassword(attributeValues);
            }
        }
        catch (Exception e) {
            audit.setAuditRequestDto(EventEnum.DOWNLOAD_PERSONALIZED_CARD);
            logger.error("Unable to convert html to pdf RootCause- "+e);
            throw new ResidentServiceException(ResidentErrorCode.DOWNLOAD_PERSONALIZED_CARD, e);
        }
        return utilitiy.signPdf(new ByteArrayInputStream(decodedData), password);
    }

    private List<String> getAttributeList() throws ApisResourceAccessException, IOException {
       return getAttributeList(identityService.getResidentIndvidualId());
    }

    private List<String> getAttributeList(String individualId) throws IOException, ApisResourceAccessException {
        Map<String, Object> identityAttributes = null;
        List<String> attributeValues = new ArrayList<>();
        try {
            identityAttributes = (Map<String, Object>) identityService.getIdentityAttributes(
                    individualId, this.environment.getProperty(ResidentConstants.RESIDENT_IDENTITY_SCHEMATYPE));
        } catch (ResidentServiceCheckedException e) {
            audit.setAuditRequestDto(EventEnum.DOWNLOAD_PERSONALIZED_CARD);
            logger.error("Unable to get attributes- "+e);
            throw new ResidentServiceException(ResidentErrorCode.DOWNLOAD_PERSONALIZED_CARD, e);
        } catch (IOException e) {
            audit.setAuditRequestDto(EventEnum.DOWNLOAD_PERSONALIZED_CARD);
            logger.error("Unable to get attributes- "+e);
            throw new IOException(ResidentErrorCode.DOWNLOAD_PERSONALIZED_CARD.getErrorCode(), e);
        }
        String attributeProperty = this.environment.getProperty(ResidentConstants.PASSWORD_ATTRIBUTE);
        if (attributeProperty!=null) {
        List<String> attributeList = List.of(attributeProperty.split("\\|"));

        for (String attribute : attributeList) {
            Object attributeObject = identityAttributes.get(attribute);
            if (attributeObject instanceof List) {
                List<Map<String, Object>> attributeMapObject = (List<Map<String, Object>>) attributeObject;
                for (Map<String, Object> attributeInLanguage : attributeMapObject) {
                    /**
                     * 1st language code is taken from mandatory/optional languages properties
                     */
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
        }
        return attributeValues;
    }

    @Override
    public String getFileName() {
        ResidentTransactionEntity residentTransactionEntity = utilitiy.createEntity();
        String eventId = utilitiy.createEventId();
        residentTransactionEntity.setEventId(eventId);
        residentTransactionEntity.setRequestTypeCode(RequestType.DOWNLOAD_PERSONALIZED_CARD.name());
        try {
            residentTransactionEntity.setRefId(utilitiy.convertToMaskDataFormat(identityService.getResidentIndvidualId()));
            residentTransactionEntity.setTokenId(identityService.getResidentIdaToken());
        } catch (ApisResourceAccessException e) {
            logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                    LoggerFileConstant.APPLICATIONID.toString(),
                    ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode()
                            + ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorMessage()
                            + ExceptionUtils.getStackTrace(e));
            audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.API_NOT_AVAILABLE,
                    eventId, "Download personalized card"));
            throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode(),
                    ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorMessage(), e);
        } catch (ResidentServiceCheckedException e) {
            throw new RuntimeException(e);
        }
        residentTransactionEntity.setRequestSummary(SUCCESS);
        residentTransactionEntity.setStatusCode(NEW);
        residentTransactionRepository.save(residentTransactionEntity);
        return utilitiy.getFileName(eventId, Objects.requireNonNull(this.environment.getProperty
                (ResidentConstants.DOWNLOAD_PERSONALIZED_CARD_NAMING_CONVENTION_PROPERTY)));
    }

    @Override
    public ResponseWrapper<VidDownloadCardResponseDto> getVidCardEventId(String vid) throws BaseCheckedException {
        ResponseWrapper<VidDownloadCardResponseDto> responseWrapper= new ResponseWrapper<>();
        VidDownloadCardResponseDto vidDownloadCardResponseDto = new VidDownloadCardResponseDto();
        String eventId="";
        try{
            RequestWrapper<CredentialReqestDto> requestDto = new RequestWrapper<>();
            CredentialReqestDto credentialReqestDto = new CredentialReqestDto();
            credentialReqestDto.setId(vid);
            credentialReqestDto.setCredentialType(environment.getProperty(ResidentConstants.MOSIP_CREDENTIAL_TYPE_PROPERTY));
            credentialReqestDto.setIssuer(environment.getProperty(ResidentConstants.CREDENTIAL_ISSUER));
            credentialReqestDto.setEncrypt(Boolean.parseBoolean(environment.getProperty(ResidentConstants.CREDENTIAL_ENCRYPTION_FLAG)));
            credentialReqestDto.setEncryptionKey(environment.getProperty(ResidentConstants.CREDENTIAL_ENCRYPTION_KEY));
            Map<String, Object> additionalAttributes = getVidDetails(vid);
            credentialReqestDto.setAdditionalData(additionalAttributes);
            requestDto.setRequest(credentialReqestDto);
            requestDto.setId(this.environment.getProperty(ResidentConstants.CREDENTIAL_REQUEST_SERVICE_ID));
            requestDto.setRequest(credentialReqestDto);
            requestDto.setRequesttime(DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime()));
            requestDto.setVersion(ResidentConstants.CREDENTIAL_REQUEST_SERVICE_VERSION);
            ResponseWrapper<ResidentCredentialResponseDto> responseDto = residentServiceRestClient.postApi(
                    environment.getProperty(ApiName.CREDENTIAL_REQ_URL.name()), MediaType.APPLICATION_JSON, requestDto,
                    ResponseWrapper.class);
            if(responseDto.getErrors().size()==0){
                ResidentCredentialResponseDto residentCredentialResponseDto =
                        JsonUtil.readValue(JsonUtil.writeValueAsString(responseDto.getResponse()),
                        ResidentCredentialResponseDto.class);
                eventId =insertDataForVidCard(residentCredentialResponseDto, vid);
                vidDownloadCardResponseDto.setEventId(eventId);
                vidDownloadCardResponseDto.setStatus(ResidentConstants.SUCCESS);
            }else{
                throw new ResidentServiceCheckedException(String.valueOf(ResidentErrorCode.VID_REQUEST_CARD_FAILED),
                        ResidentErrorCode.VID_REQUEST_CARD_FAILED.getErrorMessage());
            }
        }catch (ApisResourceAccessException e){
            throw new ApisResourceAccessException(ResidentErrorCode.VID_REQUEST_CARD_FAILED.toString(), e);
        }catch (ResidentServiceCheckedException exception){
            throw new ResidentServiceException(ResidentErrorCode.VID_REQUEST_CARD_FAILED.getErrorCode(),
                    ResidentErrorCode.VID_REQUEST_CARD_FAILED.getErrorMessage(), exception);
        }catch (IOException exception){
            throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(), exception.getMessage(), exception);
        }
        responseWrapper.setId(environment.getProperty(ResidentConstants.VID_DOWNLOAD_CARD_ID));
        responseWrapper.setVersion(environment.getProperty(ResidentConstants.VID_DOWNLOAD_CARD_VERSION));
        responseWrapper.setResponsetime(DateUtils.getUTCCurrentDateTimeString());
        responseWrapper.setResponse(vidDownloadCardResponseDto);
        return responseWrapper;
    }

    private String insertDataForVidCard(ResidentCredentialResponseDto responseDto, String vid) throws ApisResourceAccessException, IOException {
        ResidentTransactionEntity residentTransactionEntity = utilitiy.createEntity();
        String eventId = utilitiy.createEventId();
        String uin = utilities.getUinByVid(vid);
        residentTransactionEntity.setEventId(eventId);
        residentTransactionEntity.setRequestTypeCode(RequestType.VID_CARD_DOWNLOAD.name());
        residentTransactionEntity.setRefId(utilitiy.convertToMaskDataFormat(uin));
        residentTransactionEntity.setTokenId(identityService.getIDAToken(uin));
        residentTransactionEntity.setCredentialRequestId(responseDto.getRequestId());
        residentTransactionEntity.setStatusCode(NEW);
        residentTransactionEntity.setRequestSummary(RequestType.VID_CARD_DOWNLOAD.name());
        /**
         * Here we are setting vid in aid column.
         */
        residentTransactionEntity.setAid(vid);
        residentTransactionRepository.save(residentTransactionEntity);
        return eventId;
    }

    private Map<String, Object> getVidDetails(String vid) throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
        Map<String, Object> additionalAttributes = new HashMap<>();
        String uin =utilities.getUinByVid(vid);
        String name=null;
        ResponseWrapper<List<Map<String,?>>> vidResponse = null;
        if(uin!=null){
            vidResponse =vidService.retrieveVids(uin);
            name = getFullName(uin);
        }
        if (vidResponse!=null){
            List<Map<String, ?>> vidList = vidResponse.getResponse();
            if(vidList.size()>0){
                for(Map<String, ?> vidData: vidList){
                    if(vidData.get(VID).toString().equalsIgnoreCase(vid)){
                        additionalAttributes.put(VID, vid);
                        additionalAttributes.put(VID_TYPE, vidData.get(VID_TYPE));
                        additionalAttributes.put(MASKED_VID, vidData.get(MASKED_VID));
                        additionalAttributes.put(EXPIRY_TIMESTAMP, vidData.get(EXPIRY_TIMESTAMP));
                        additionalAttributes.put(GENERATED_ON_TIMESTAMP, vidData.get(GENERATED_ON_TIMESTAMP));
                        additionalAttributes.put(TRANSACTION_LIMIT, vidData.get(TRANSACTION_LIMIT));
                        additionalAttributes.put(TRANSACTION_COUNT, vidData.get(TRANSACTION_COUNT));
                        if(name!=null){
                            additionalAttributes.put(NAME, name);
                        }
                        break;
                    }
                }
            }
        }
        return additionalAttributes;
    }

    private String getFullName(String uin) throws IOException, ApisResourceAccessException {
        List<String> attributeList =getAttributeList(uin);
        if(attributeList.size()>0){
            return attributeList.get(0);
        }
        return null;
    }

    private String getUINForIndividualId(String individualId)  {
        String idType = identityService.getIndividualIdType(individualId);
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
