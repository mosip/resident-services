package io.mosip.resident.service.impl;

import static io.mosip.resident.constant.EventStatusSuccess.CARD_DOWNLOADED;
import static io.mosip.resident.constant.TemplateVariablesConstants.NAME;
import static io.mosip.resident.constant.TemplateVariablesConstants.VID;
import static io.mosip.resident.constant.TemplateVariablesConstants.VID_TYPE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mosip.resident.dto.CheckStatusResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.EventStatus;
import io.mosip.resident.constant.EventStatusFailure;
import io.mosip.resident.constant.EventStatusInProgress;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
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
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.DownloadCardService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.ResidentCredentialService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.util.Utilitiy;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

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
    private static final String CARD_FORMAT = "cardFormat";
    private static final Object VID_CARD = "vidCard";
    private static final String CARD_READY_TO_DOWNLOAD = "Card ready to download";
    private static final String IN_PROGRESS = "IN-PROGRESS";

    @Autowired
    private Utilities utilities;

    @Autowired
    private AuditUtil audit;

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
    private Environment environment;

    @Autowired
    private ResidentTransactionRepository residentTransactionRepository;

    @Autowired
    private ResidentVidService vidService;

    @Autowired
    private ResidentCredentialService residentCredentialService;

    private static final Logger logger = LoggerConfiguration.logConfig(DownloadCardServiceImpl.class);

    @Override
    public Tuple3<byte[], String, ResponseWrapper<CheckStatusResponseDTO>> getDownloadCardPDF(MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO) {
        String rid = "";
        String eventId = utilitiy.createEventId();
        byte[] pdfBytes = new byte[0];
        ResponseWrapper<CheckStatusResponseDTO> checkStatusResponseDTOResponseWrapper = new ResponseWrapper<>();
        try {
            if (idAuthService.validateOtp(downloadCardRequestDTOMainRequestDTO.getRequest().getTransactionId(),
                    getUINForIndividualId(downloadCardRequestDTOMainRequestDTO.getRequest().getIndividualId())
                    , downloadCardRequestDTOMainRequestDTO.getRequest().getOtp())) {
                String individualId = downloadCardRequestDTOMainRequestDTO.getRequest().getIndividualId();
                String idType = identityService.getIndividualIdType(individualId);
                if (idType.equalsIgnoreCase(AID)) {
                    rid = individualId;
                    pdfBytes = residentService.getUINCard(rid);
                } else if (idType.equalsIgnoreCase(VID)) {
                    ResidentTransactionEntity residentTransactionEntity = residentTransactionRepository.findTopByAidOrderByCrDtimesDesc(individualId);
                    if(residentTransactionEntity !=null ){
                        String credentialRequestId = residentTransactionEntity.getCredentialRequestId();
                        if (credentialRequestId != null) {
                            pdfBytes =residentCredentialService.getCard
                                    (credentialRequestId, null, null);
                        }
                    }
                } else {
                    rid = utilities.getRidByIndividualId(individualId);
                    pdfBytes = residentService.getUINCard(rid);
                }
                if(pdfBytes.length==0){
                    insertDataForDownloadCard(downloadCardRequestDTOMainRequestDTO, eventId, EventStatus.FAILED.name());
                } else {
                    insertDataForDownloadCard(downloadCardRequestDTOMainRequestDTO, eventId, EventStatus.SUCCESS.name());
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
        } catch (ResidentServiceException e) {
            audit.setAuditRequestDto(EventEnum.DOWNLOAD_PERSONALIZED_CARD);
            logger.error("Unable to get attributes- "+e);
            throw new ResidentServiceException(ResidentErrorCode.DOWNLOAD_PERSONALIZED_CARD, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Tuples.of(pdfBytes, eventId, checkStatusResponseDTOResponseWrapper);
    }

    private ResponseWrapper<CheckStatusResponseDTO> getCheckStatusResponse(HashMap<String, String> packetStatusMap) {
        ResponseWrapper<CheckStatusResponseDTO> checkStatusResponseDTOResponseWrapper = new ResponseWrapper<>();
        CheckStatusResponseDTO checkStatusResponseDTO = new CheckStatusResponseDTO();
        String aidStatus = packetStatusMap.get(ResidentConstants.AID_STATUS);
        String transactionStage = packetStatusMap.get(ResidentConstants.TRANSACTION_TYPE_CODE);
        checkStatusResponseDTO.setAidStatus(aidStatus);
        checkStatusResponseDTO.setTransactionStage(transactionStage);
        checkStatusResponseDTOResponseWrapper.setResponse(checkStatusResponseDTO);
        checkStatusResponseDTOResponseWrapper.setId(this.environment.getProperty(ResidentConstants.CHECK_STATUS_INDIVIDUAL_ID));
        checkStatusResponseDTOResponseWrapper.setVersion(this.environment.getProperty(ResidentConstants.CHECKSTATUS_INDIVIDUALID_VERSION));
        return checkStatusResponseDTOResponseWrapper;
    }

    private void insertDataForDownloadCard(MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO,
                                           String eventId, String status) throws ApisResourceAccessException, ResidentServiceCheckedException {
        ResidentTransactionEntity residentTransactionEntity = utilitiy.createEntity();
        residentTransactionEntity.setEventId(eventId);
        residentTransactionEntity.setRequestTypeCode(RequestType.GET_MY_ID.name());
        residentTransactionEntity.setRequestSummary(RequestType.GET_MY_ID.name());
        residentTransactionEntity.setStatusCode(status);
        residentTransactionEntity.setStatusComment(String.valueOf(CARD_DOWNLOADED));
        residentTransactionEntity.setRefId(utilitiy.convertToMaskDataFormat(
                downloadCardRequestDTOMainRequestDTO.getRequest().getIndividualId()));
        residentTransactionEntity.setTokenId(identityService.getIDAToken(
                downloadCardRequestDTOMainRequestDTO.getRequest().getIndividualId()));
        residentTransactionEntity.setRequestTrnId(downloadCardRequestDTOMainRequestDTO.getRequest().getTransactionId());
        residentTransactionRepository.save(residentTransactionEntity);

    }

    @Override
    public Tuple2<byte[], String> downloadPersonalizedCard(MainRequestDTO<DownloadPersonalizedCardDto> downloadPersonalizedCardMainRequestDTO) {
        String encodeHtml = downloadPersonalizedCardMainRequestDTO.getRequest().getHtml();
        byte[] decodedData;
        String password=null;
        String eventId = ResidentConstants.NOT_AVAILABLE;
        ResidentTransactionEntity residentTransactionEntity = null;
        try {
        	residentTransactionEntity = createResidentTransactionEntity();
        	if (residentTransactionEntity != null) {
    			eventId = residentTransactionEntity.getEventId(); 
    		}
            decodedData = CryptoUtil.decodePlainBase64(encodeHtml);
            List<String> attributeValues = getAttributeList();
            if(Boolean.parseBoolean(this.environment.getProperty(ResidentConstants.IS_PASSWORD_FLAG_ENABLED))){
                password = utilitiy.getPassword(attributeValues);
            }
            residentTransactionEntity.setRequestSummary(ResidentConstants.SUCCESS);
            residentTransactionEntity.setStatusCode(EventStatusInProgress.NEW.name());
        }
        catch (Exception e) {
        	if (residentTransactionEntity != null) {
        		residentTransactionEntity.setRequestSummary(ResidentConstants.FAILED);
            	residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
        	}
            audit.setAuditRequestDto(EventEnum.DOWNLOAD_PERSONALIZED_CARD);
            logger.error("Unable to convert html to pdf RootCause- "+e);
			throw new ResidentServiceException(ResidentErrorCode.DOWNLOAD_PERSONALIZED_CARD, e,
					Map.of(ResidentConstants.EVENT_ID, eventId));
        } finally {
        	if(residentTransactionEntity != null) {
        		//if the status code will come as null, it will set it as failed.
            	if(residentTransactionEntity.getStatusCode()==null) {
    				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
    				residentTransactionEntity.setRequestSummary(ResidentConstants.FAILED);
    			}
    			residentTransactionRepository.save(residentTransactionEntity);
        	}
		}
        return Tuples.of(utilitiy.signPdf(new ByteArrayInputStream(decodedData), password), eventId);
    }

    private ResidentTransactionEntity createResidentTransactionEntity() throws ApisResourceAccessException, ResidentServiceCheckedException {
    	ResidentTransactionEntity residentTransactionEntity = utilitiy.createEntity();
        String eventId = utilitiy.createEventId();
        residentTransactionEntity.setEventId(eventId);
        residentTransactionEntity.setRequestTypeCode(RequestType.DOWNLOAD_PERSONALIZED_CARD.name());
        residentTransactionEntity.setRefId(utilitiy.convertToMaskDataFormat(identityService.getResidentIndvidualId()));
        residentTransactionEntity.setTokenId(identityService.getResidentIdaToken());
        return residentTransactionEntity;
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
    public Tuple2<ResponseWrapper<VidDownloadCardResponseDto>, String> getVidCardEventId(String vid) throws BaseCheckedException {
        ResponseWrapper<VidDownloadCardResponseDto> responseWrapper= new ResponseWrapper<>();
        VidDownloadCardResponseDto vidDownloadCardResponseDto = new VidDownloadCardResponseDto();
        String eventId = ResidentConstants.NOT_AVAILABLE;
        ResidentTransactionEntity residentTransactionEntity = null;
        try{
        	residentTransactionEntity = insertDataForVidCard(vid);
        	if (residentTransactionEntity != null) {
    			eventId = residentTransactionEntity.getEventId(); 
    		}
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
                residentTransactionEntity.setCredentialRequestId(residentCredentialResponseDto.getRequestId());
                vidDownloadCardResponseDto.setStatus(ResidentConstants.SUCCESS);
			} else {
            	if (residentTransactionEntity != null) {
            		residentTransactionEntity.setRequestSummary(ResidentConstants.FAILED);
                	residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
            	}
				throw new ResidentServiceCheckedException(ResidentErrorCode.VID_REQUEST_CARD_FAILED,
						Map.of(ResidentConstants.EVENT_ID, eventId));
            }
        } catch (ApisResourceAccessException e) {
        	if (residentTransactionEntity != null) {
        		residentTransactionEntity.setRequestSummary(ResidentConstants.FAILED);
            	residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
        	}
			throw new ApisResourceAccessException(ResidentErrorCode.VID_REQUEST_CARD_FAILED.toString(), e,
					Map.of(ResidentConstants.EVENT_ID, eventId));
        } catch (ResidentServiceCheckedException exception) {
        	if (residentTransactionEntity != null) {
        		residentTransactionEntity.setRequestSummary(ResidentConstants.FAILED);
            	residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
        	}
			throw new ResidentServiceException(ResidentErrorCode.VID_REQUEST_CARD_FAILED, exception,
					Map.of(ResidentConstants.EVENT_ID, eventId));
        } catch (IOException exception) {
        	if (residentTransactionEntity != null) {
        		residentTransactionEntity.setRequestSummary(ResidentConstants.FAILED);
            	residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
        	}
            throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(), exception.getMessage(), exception);
        } finally {
        	if(residentTransactionEntity != null) {
        		//if the status code will come as null, it will set it as failed.
            	if(residentTransactionEntity.getStatusCode()==null) {
    				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
    				residentTransactionEntity.setRequestSummary(ResidentConstants.FAILED);
    			}
    			residentTransactionRepository.save(residentTransactionEntity);
        	}
		}
        responseWrapper.setId(environment.getProperty(ResidentConstants.VID_DOWNLOAD_CARD_ID));
        responseWrapper.setVersion(environment.getProperty(ResidentConstants.VID_DOWNLOAD_CARD_VERSION));
        responseWrapper.setResponsetime(DateUtils.getUTCCurrentDateTimeString());
        responseWrapper.setResponse(vidDownloadCardResponseDto);
        return Tuples.of(responseWrapper, eventId);
    }

    @Override
    public ResponseWrapper<CheckStatusResponseDTO> getIndividualIdStatus(String individualId) throws ApisResourceAccessException, IOException {
        HashMap<String, String> packetStatusMap = utilities.getPacketStatus(individualId);
        return getCheckStatusResponse(packetStatusMap);
    }

    private ResidentTransactionEntity insertDataForVidCard(String vid) throws ApisResourceAccessException, IOException {
        ResidentTransactionEntity residentTransactionEntity = utilitiy.createEntity();
        String uin = utilities.getUinByVid(vid);
        residentTransactionEntity.setEventId(utilitiy.createEventId());
        residentTransactionEntity.setRequestTypeCode(RequestType.VID_CARD_DOWNLOAD.name());
        residentTransactionEntity.setRefId(utilitiy.convertToMaskDataFormat(uin));
        residentTransactionEntity.setTokenId(identityService.getIDAToken(uin));
        residentTransactionEntity.setStatusCode(EventStatusInProgress.NEW.name());
        residentTransactionEntity.setRequestSummary(RequestType.VID_CARD_DOWNLOAD.name());
        /**
         * Here we are setting vid in aid column.
         */
        residentTransactionEntity.setAid(vid);
        return residentTransactionEntity;
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
                        additionalAttributes.put(CARD_FORMAT, VID_CARD);
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
        if(idType.equalsIgnoreCase(IdType.UIN.toString())){
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
