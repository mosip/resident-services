package io.mosip.resident.service.impl;

import static io.mosip.resident.constant.EventStatusSuccess.CARD_DOWNLOADED;
import static io.mosip.resident.constant.ResidentConstants.SEMI_COLON;
import static io.mosip.resident.constant.TemplateVariablesConstants.OTP;
import static io.mosip.resident.constant.TemplateVariablesConstants.VID;
import static io.mosip.resident.constant.TemplateVariablesConstants.VID_TYPE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.EventStatusFailure;
import io.mosip.resident.constant.EventStatusInProgress;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.PacketStatus;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.constant.TemplateType;
import io.mosip.resident.constant.TransactionStage;
import io.mosip.resident.dto.CheckStatusResponseDTO;
import io.mosip.resident.dto.CredentialReqestDto;
import io.mosip.resident.dto.DownloadCardRequestDTO;
import io.mosip.resident.dto.DownloadPersonalizedCardDto;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.dto.NotificationRequestDtoV2;
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
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ResidentCredentialService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.util.Utility;
import reactor.util.function.Tuple2;
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
    private static final String TRANSACTION_COUNT = "transactionsLeftCount";
    private static final String CARD_FORMAT = "cardFormat";
    private static final Object VID_CARD = "vidCard";
    private static final String TEMPLATE_TYPE_CODE = "templateTypeCode";
    private static final String APPLICANT_PHOTO = "ApplicantPhoto";
    private static final Object NA = "NA";
    private static final String DATE_OF_BIRTH = "dob";

    @Autowired
    private Utilities utilities;

    @Autowired
    private AuditUtil audit;

    @Autowired
    private ResidentServiceRestClient residentServiceRestClient;

    @Autowired
    private IdAuthService idAuthService;

    @Autowired
    private Utility utility;

    @Autowired
    private IdentityServiceImpl identityService;

    @Autowired
	private NotificationService notificationService;

    @Autowired
    private Environment environment;

    @Autowired
    private ResidentTransactionRepository residentTransactionRepository;

    @Autowired
    private ResidentVidService vidService;

    @Autowired
    private ResidentCredentialService residentCredentialService;

    @Value("${mosip.registration.processor.rid.delimiter}")
	private String ridSuffix;

    private static final Logger logger = LoggerConfiguration.logConfig(DownloadCardServiceImpl.class);

    @Override
	public Tuple2<byte[], String> getDownloadCardPDF(
			MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO)
			throws ResidentServiceCheckedException {
		String rid = "";
		String individualId = downloadCardRequestDTOMainRequestDTO.getRequest().getIndividualId();
		String eventId = ResidentConstants.NOT_AVAILABLE;
		ResidentTransactionEntity residentTransactionEntity = null;
		byte[] pdfBytes = new byte[0];
		try {
			String transactionId = downloadCardRequestDTOMainRequestDTO.getRequest().getTransactionId();
			residentTransactionEntity = insertDataForGetMyUin(individualId, transactionId);
			if (residentTransactionEntity != null) {
				eventId = residentTransactionEntity.getEventId();
				if (idAuthService.validateOtpv2(transactionId, getIndividualIdForAid(individualId),
						downloadCardRequestDTOMainRequestDTO.getRequest().getOtp())) {
					String idType = identityService.getIndividualIdType(individualId);
					if (idType.equalsIgnoreCase(AID)) {
						rid = individualId;
						Map<String, String> ridStatus = utilities.getPacketStatus(rid);
						String transactionTypeCode = ridStatus.get(ResidentConstants.TRANSACTION_TYPE_CODE);
						String aidStatus = ridStatus.get(ResidentConstants.AID_STATUS);
						if (transactionTypeCode.equalsIgnoreCase(TransactionStage.CARD_READY_TO_DOWNLOAD.name())
								&& aidStatus.equalsIgnoreCase(PacketStatus.SUCCESS.getName())) {
							pdfBytes = residentCredentialService.getCard(rid + ridSuffix, null, null);
						} else {
							throw new ResidentServiceException(ResidentErrorCode.CARD_NOT_READY.getErrorCode(),
									ResidentErrorCode.CARD_NOT_READY.getErrorMessage());
						}
					} else {
						rid = utilities.getRidByIndividualId(individualId);
						pdfBytes = residentCredentialService.getCard(rid + ridSuffix, null, null);
					}
					if (pdfBytes.length == 0) {
						residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
						residentTransactionEntity.setStatusComment(
								ResidentErrorCode.CARD_NOT_READY.getErrorMessage() + " - zero length");
						residentTransactionEntity
								.setRequestSummary(RequestType.GET_MY_ID.name() + " - " + ResidentConstants.FAILED);
					} else {
						residentTransactionEntity.setStatusCode(CARD_DOWNLOADED.name());
						residentTransactionEntity.setStatusComment(CARD_DOWNLOADED.name());
						residentTransactionEntity
								.setRequestSummary(RequestType.GET_MY_ID.name() + " - " + ResidentConstants.SUCCESS);
					}
				} else {
					logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
							LoggerFileConstant.APPLICATIONID.toString(),
							ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
					audit.setAuditRequestDto(EventEnum.CREDENTIAL_REQ_EXCEPTION);
					residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
					residentTransactionEntity
							.setStatusComment(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
					throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
							ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
				}
			}
		} catch (ApisResourceAccessException e) {
			audit.setAuditRequestDto(EventEnum.RID_DIGITAL_CARD_REQ_EXCEPTION);
			throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e,
					Map.of(ResidentConstants.EVENT_ID, eventId));
		} catch (OtpValidationFailedException e) {
			audit.setAuditRequestDto(EventEnum.RID_DIGITAL_CARD_REQ_EXCEPTION);
			if (residentTransactionEntity != null) {
				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
				residentTransactionEntity.setStatusComment(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
			}
			throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(), e.getErrorText(),
					e, Map.of(ResidentConstants.EVENT_ID, eventId));
		} catch (Exception e) {
			audit.setAuditRequestDto(EventEnum.RID_DIGITAL_CARD_REQ_EXCEPTION);
			throw new ResidentServiceException(ResidentErrorCode.CARD_NOT_READY.getErrorCode(),
					ResidentErrorCode.CARD_NOT_READY.getErrorMessage(), e, Map.of(ResidentConstants.EVENT_ID, eventId));
		} finally {
			if (residentTransactionEntity != null) {
				/**
				 * Here we are setting RID in AID column.
				 */
				residentTransactionEntity.setAid(rid);
				// if the status code will come as null, it will set it as failed.
				if (residentTransactionEntity.getStatusCode() == null) {
					residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
					residentTransactionEntity.setStatusComment(ResidentErrorCode.CARD_NOT_READY.getErrorMessage());
				}
				if (residentTransactionEntity.getRequestSummary() == null) {
					residentTransactionEntity
							.setRequestSummary(RequestType.GET_MY_ID.name() + " - " + ResidentConstants.FAILED);
				}
				residentTransactionRepository.save(residentTransactionEntity);

				TemplateType templateType = (residentTransactionEntity.getStatusCode()
						.equals(CARD_DOWNLOADED.name())) ? TemplateType.SUCCESS
								: TemplateType.FAILURE;

				sendNotificationV2(individualId, RequestType.GET_MY_ID, templateType, eventId, null);
			}
		}
		return Tuples.of(pdfBytes, eventId);
	}

    private ResponseWrapper<CheckStatusResponseDTO> getCheckStatusResponse(Map<String, String> packetStatusMap) {
        ResponseWrapper<CheckStatusResponseDTO> checkStatusResponseDTOResponseWrapper = new ResponseWrapper<>();
        CheckStatusResponseDTO checkStatusResponseDTO = new CheckStatusResponseDTO();
        String aidStatus = packetStatusMap.get(ResidentConstants.AID_STATUS);
        String transactionStage = packetStatusMap.get(ResidentConstants.TRANSACTION_TYPE_CODE);
        checkStatusResponseDTO.setAidStatus(aidStatus);
        checkStatusResponseDTO.setTransactionStage(transactionStage);
        checkStatusResponseDTOResponseWrapper.setResponse(checkStatusResponseDTO);
        checkStatusResponseDTOResponseWrapper.setId(this.environment.getProperty(ResidentConstants.CHECK_STATUS_INDIVIDUAL_ID));
        checkStatusResponseDTOResponseWrapper.setVersion(this.environment.getProperty(ResidentConstants.CHECKSTATUS_INDIVIDUALID_VERSION));
        checkStatusResponseDTOResponseWrapper.setResponsetime(DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime()));
        return checkStatusResponseDTOResponseWrapper;
    }

    private ResidentTransactionEntity insertDataForGetMyUin(String individualId, String transactionId) throws ResidentServiceCheckedException {
        ResidentTransactionEntity residentTransactionEntity = utility.createEntity(RequestType.GET_MY_ID.name());
        residentTransactionEntity.setEventId(utility.createEventId());
        residentTransactionEntity.setAuthTypeCode(OTP);
        residentTransactionEntity.setRefId(utility.convertToMaskDataFormat(individualId));
        residentTransactionEntity.setIndividualId(individualId);
        residentTransactionEntity.setTokenId(identityService.getIDATokenForIndividualId(getIndividualIdForAid(individualId)));
        residentTransactionEntity.setRequestTrnId(transactionId);
        return residentTransactionEntity;
    }

    @Override
	public Tuple2<byte[], String> downloadPersonalizedCard(
			MainRequestDTO<DownloadPersonalizedCardDto> downloadPersonalizedCardMainRequestDTO, int timeZoneOffset)
			throws ResidentServiceCheckedException {
        String encodeHtml = downloadPersonalizedCardMainRequestDTO.getRequest().getHtml();
        byte[] decodedData = new byte[0];
        String password=null;
        String individualId = "";
        String eventId = ResidentConstants.NOT_AVAILABLE;
        ResidentTransactionEntity residentTransactionEntity = null;
        try {
			individualId = identityService.getResidentIndvidualIdFromSession();
			residentTransactionEntity = createResidentTransactionEntity(individualId,
					downloadPersonalizedCardMainRequestDTO.getRequest());
			if (residentTransactionEntity != null) {
				eventId = residentTransactionEntity.getEventId();
				decodedData = CryptoUtil.decodePlainBase64(encodeHtml);
				List<String> attributeValues = getAttributeList();
				if (Boolean.parseBoolean(this.environment.getProperty(ResidentConstants.IS_PASSWORD_FLAG_ENABLED))) {
					password = utility.getPassword(attributeValues);
				}
				residentTransactionEntity.setRequestSummary(
						RequestType.DOWNLOAD_PERSONALIZED_CARD.name() + " - " + ResidentConstants.SUCCESS);
				residentTransactionEntity.setStatusCode(CARD_DOWNLOADED.name());
				residentTransactionEntity.setStatusComment(CARD_DOWNLOADED.name());
			}
        }
        catch (Exception e) {
        	if (residentTransactionEntity != null) {
				residentTransactionEntity.setRequestSummary(
						RequestType.DOWNLOAD_PERSONALIZED_CARD.name() + " - " + ResidentConstants.FAILED);
				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
				residentTransactionEntity.setStatusComment(ResidentErrorCode.DOWNLOAD_PERSONALIZED_CARD.getErrorMessage());
        	}
            audit.setAuditRequestDto(EventEnum.DOWNLOAD_PERSONALIZED_CARD);
            logger.error("Unable to convert html to pdf RootCause- "+e);
			throw new ResidentServiceException(ResidentErrorCode.DOWNLOAD_PERSONALIZED_CARD, e,
					Map.of(ResidentConstants.EVENT_ID, eventId));
        } finally {
        	if(residentTransactionEntity != null) {
        		//if the status code will come as null, it will set it as failed.
				if (residentTransactionEntity.getStatusCode() == null) {
					residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
					residentTransactionEntity.setStatusComment(ResidentErrorCode.DOWNLOAD_PERSONALIZED_CARD.getErrorMessage());
					residentTransactionEntity.setRequestSummary(
							RequestType.DOWNLOAD_PERSONALIZED_CARD.name() + " - " + ResidentConstants.FAILED);
				}
    			residentTransactionRepository.save(residentTransactionEntity);
    			TemplateType templateType = (residentTransactionEntity.getStatusCode()
						.equals(CARD_DOWNLOADED.name())) ? TemplateType.SUCCESS
								: TemplateType.FAILURE;

				sendNotificationV2(individualId, RequestType.DOWNLOAD_PERSONALIZED_CARD, templateType, eventId, null);
        	}
		}
        return Tuples.of(utility.signPdf(new ByteArrayInputStream(decodedData), password), eventId);
    }

	private ResidentTransactionEntity createResidentTransactionEntity(String individualId,
			DownloadPersonalizedCardDto downloadPersonalizedCardDto)
			throws ApisResourceAccessException, ResidentServiceCheckedException {
    	ResidentTransactionEntity residentTransactionEntity = utility.createEntity(RequestType.DOWNLOAD_PERSONALIZED_CARD.name());
        String eventId = utility.createEventId();
        residentTransactionEntity.setEventId(eventId);
        residentTransactionEntity.setAuthTypeCode(identityService.getResidentAuthenticationMode());
        residentTransactionEntity.setRefId(utility.convertToMaskDataFormat(individualId));
        residentTransactionEntity.setIndividualId(individualId);
        residentTransactionEntity.setTokenId(identityService.getResidentIdaToken());
		if (downloadPersonalizedCardDto.getAttributes() != null) {
			residentTransactionEntity.setAttributeList(
					downloadPersonalizedCardDto.getAttributes().stream().collect(Collectors.joining(SEMI_COLON)));
		}
        return residentTransactionEntity;
	}

	private List<String> getAttributeList() throws ApisResourceAccessException, IOException {
       return getAttributeList(identityService.getResidentIndvidualIdFromSession());
    }

    private List<String> getAttributeList(String individualId) throws IOException, ApisResourceAccessException {
        Map<String, Object> identityAttributes = null;
        List<String> attributeValues = new ArrayList<>();
        try {
			identityAttributes = (Map<String, Object>) identityService.getIdentityAttributes(individualId, null);
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
    public Tuple2<ResponseWrapper<VidDownloadCardResponseDto>, String> getVidCardEventId(String vid, int timeZoneOffset) throws BaseCheckedException {
        ResponseWrapper<VidDownloadCardResponseDto> responseWrapper= new ResponseWrapper<>();
        VidDownloadCardResponseDto vidDownloadCardResponseDto = new VidDownloadCardResponseDto();
        String eventId = ResidentConstants.NOT_AVAILABLE;
        ResidentTransactionEntity residentTransactionEntity = null;
        String uinForVid = "";
        try{
        	uinForVid = utilities.getUinByVid(vid);
        	residentTransactionEntity = insertDataForVidCard(vid, uinForVid);
        	if (residentTransactionEntity != null) {
    			eventId = residentTransactionEntity.getEventId();
    			String uinForIndividualId = identityService.getUinForIndividualId(identityService.getResidentIndvidualIdFromSession());
    			if(!uinForIndividualId.equals(uinForVid)) {
                		residentTransactionEntity.setRequestSummary(ResidentConstants.FAILED);
                    	residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
                    	audit.setAuditRequestDto(EventEnum.RID_DIGITAL_CARD_REQ_FAILURE);
                    	throw new ResidentServiceCheckedException(ResidentErrorCode.VID_NOT_BELONG_TO_SESSION,
    							Map.of(ResidentConstants.EVENT_ID, eventId));
    			}
    		}
            RequestWrapper<CredentialReqestDto> requestDto = new RequestWrapper<>();
            CredentialReqestDto credentialReqestDto = new CredentialReqestDto();
            credentialReqestDto.setId(vid);
            credentialReqestDto.setCredentialType(environment.getProperty(ResidentConstants.MOSIP_CREDENTIAL_TYPE_PROPERTY));
            credentialReqestDto.setIssuer(environment.getProperty(ResidentConstants.CREDENTIAL_ISSUER));
            credentialReqestDto.setEncrypt(Boolean.parseBoolean(environment.getProperty(ResidentConstants.CREDENTIAL_ENCRYPTION_FLAG)));
            credentialReqestDto.setEncryptionKey(environment.getProperty(ResidentConstants.CREDENTIAL_ENCRYPTION_KEY));
            Map<String, Object> additionalAttributes = getVidDetails(vid, uinForVid, timeZoneOffset);
            additionalAttributes.put(TEMPLATE_TYPE_CODE, this.environment.getProperty(ResidentConstants.VID_CARD_TEMPLATE_PROPERTY));
            additionalAttributes.put(APPLICANT_PHOTO, identityService.getAvailableclaimValue(environment.getProperty(ResidentConstants.IMAGE)));
            credentialReqestDto.setAdditionalData(additionalAttributes);
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
        	audit.setAuditRequestDto(EventEnum.RID_DIGITAL_CARD_REQ_EXCEPTION);
        	if (residentTransactionEntity != null) {
        		residentTransactionEntity.setRequestSummary(ResidentConstants.FAILED);
            	residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
        	}
			throw new ApisResourceAccessException(ResidentErrorCode.VID_REQUEST_CARD_FAILED.toString(), e,
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

				TemplateType templateType = (residentTransactionEntity.getStatusCode()
						.equals(EventStatusInProgress.NEW.name())) ? TemplateType.REQUEST_RECEIVED
								: TemplateType.FAILURE;

				sendNotificationV2(uinForVid, RequestType.VID_CARD_DOWNLOAD, templateType, eventId, null);
        	}
		}
        responseWrapper.setId(environment.getProperty(ResidentConstants.VID_DOWNLOAD_CARD_ID));
        responseWrapper.setVersion(environment.getProperty(ResidentConstants.VID_DOWNLOAD_CARD_VERSION));
        responseWrapper.setResponsetime(DateUtils.getUTCCurrentDateTimeString());
        responseWrapper.setResponse(vidDownloadCardResponseDto);
        return Tuples.of(responseWrapper, eventId);
    }

    @Override
    public ResponseWrapper<CheckStatusResponseDTO> getIndividualIdStatus(String individualId) throws ApisResourceAccessException, IOException, ResidentServiceCheckedException {
        individualId = getRidForIndividualId(individualId);
        Map<String, String> packetStatusMap = utilities.getPacketStatus(individualId);
        return getCheckStatusResponse(packetStatusMap);
    }

    private String getRidForIndividualId(String individualId) {
        String idType = identityService.getIndividualIdType(individualId);
        if(idType.equalsIgnoreCase(AID)){
            return individualId;
        } else{
            try {
                return utilities.getRidByIndividualId(individualId);
            } catch (ApisResourceAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private ResidentTransactionEntity insertDataForVidCard(String vid, String uin) throws ApisResourceAccessException, IOException, ResidentServiceCheckedException {
        ResidentTransactionEntity residentTransactionEntity = utility.createEntity(RequestType.VID_CARD_DOWNLOAD.name());
        residentTransactionEntity.setEventId(utility.createEventId());
        residentTransactionEntity.setAuthTypeCode(identityService.getResidentAuthenticationMode());
        residentTransactionEntity.setRefId(utility.convertToMaskDataFormat(uin));
        residentTransactionEntity.setIndividualId(uin);
        residentTransactionEntity.setTokenId(identityService.getIDAToken(uin));
        residentTransactionEntity.setStatusCode(EventStatusInProgress.NEW.name());
        residentTransactionEntity.setStatusComment(EventStatusInProgress.NEW.name());
        residentTransactionEntity.setRequestSummary(EventStatusInProgress.NEW.name());
        /**
         * Here we are setting vid in aid column.
         */
        residentTransactionEntity.setAid(vid);
        return residentTransactionEntity;
    }

    private Map<String, Object> getVidDetails(String vid, String uin, int timeZoneOffset) throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
        Map<String, Object> additionalAttributes = new HashMap<>();
        String name=null;
        ResponseWrapper<List<Map<String,?>>> vidResponse = null;
        if(uin!=null){
            vidResponse =vidService.retrieveVids(uin, timeZoneOffset);
            name = getFullName(uin);
        }
        if (vidResponse!=null){
            List<Map<String, ?>> vidList = vidResponse.getResponse();
            if(vidList.size()>0){
                for(Map<String, ?> vidData: vidList){
                    if(vidData.get(VID).toString().equalsIgnoreCase(vid)){
                        additionalAttributes.put(ResidentConstants.VID, vid);
                        additionalAttributes.put(VID_TYPE, vidData.get(VID_TYPE));
                        additionalAttributes.put(MASKED_VID, vidData.get(MASKED_VID));
                        additionalAttributes.put(EXPIRY_TIMESTAMP, replaceNullValueWithNA(vidData.get(EXPIRY_TIMESTAMP)));
                        additionalAttributes.put(GENERATED_ON_TIMESTAMP, replaceNullValueWithNA(vidData.get(GENERATED_ON_TIMESTAMP)));
                        additionalAttributes.put(TRANSACTION_LIMIT, replaceNullValueWithNA(vidData.get(TRANSACTION_LIMIT)));
                        additionalAttributes.put(TRANSACTION_COUNT, replaceNullValueWithNA(vidData.get(TRANSACTION_COUNT)));
                        additionalAttributes.put(CARD_FORMAT, VID_CARD);
                        additionalAttributes.put(DATE_OF_BIRTH, identityService.getIdentity(identityService.getResidentIndvidualIdFromSession()).getDateOfBirth());
                        if(name!=null){
                            additionalAttributes.put(ResidentConstants.NAME, name);
                        }
                        break;
                    }
                }
            }
        }
        return additionalAttributes;
    }

    private Object replaceNullValueWithNA(Object o) {
        if(o == null){
            return NA;
        }
        return o;
    }

    private String getFullName(String uin) throws IOException, ApisResourceAccessException {
        List<String> attributeList =getAttributeList(uin);
        if(attributeList.size()>0){
            return attributeList.get(0);
        }
        return null;
    }

    /**
     * @param individualId - it can be UIN, VID or AID.
     * @return UIN or VID based on the flag "useVidOnly"
     */
    private String getIndividualIdForAid(String individualId)  {
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

    private void sendNotificationV2(String id, RequestType requestType, TemplateType templateType,
			String eventId, Map<String, Object> additionalAttributes) throws ResidentServiceCheckedException {
		NotificationRequestDtoV2 notificationRequestDtoV2 = new NotificationRequestDtoV2();
		notificationRequestDtoV2.setId(id);
		notificationRequestDtoV2.setRequestType(requestType);
		notificationRequestDtoV2.setTemplateType(templateType);
		notificationRequestDtoV2.setEventId(eventId);
		notificationRequestDtoV2.setAdditionalAttributes(additionalAttributes);
		notificationService.sendNotification(notificationRequestDtoV2);
	}

}
