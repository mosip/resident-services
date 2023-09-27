package io.mosip.resident.service.impl;

import static io.mosip.resident.constant.EventStatusSuccess.CARD_DOWNLOADED;
import static io.mosip.resident.constant.ResidentConstants.SEMI_COLON;
import static io.mosip.resident.constant.TemplateVariablesConstants.VID;
import static io.mosip.resident.constant.TemplateVariablesConstants.VID_TYPE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.mosip.resident.dto.IdentityDTO;
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
import io.mosip.resident.exception.ResidentCredentialServiceException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.DownloadCardService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ResidentCredentialService;
import io.mosip.resident.service.ResidentVidService;
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
		logger.debug("DownloadCardServiceImpl::getDownloadCardPDF()::entry");
		String rid = "";
		String individualId = downloadCardRequestDTOMainRequestDTO.getRequest().getIndividualId();
		String eventId = ResidentConstants.NOT_AVAILABLE;
		ResidentTransactionEntity residentTransactionEntity = null;
		byte[] pdfBytes = new byte[0];
		IdentityDTO identityDTO = null;
		try {
			String transactionId = downloadCardRequestDTOMainRequestDTO.getRequest().getTransactionId();
			identityDTO = identityService.getIdentity(individualId);
			String uin = identityDTO.getUIN();
			Tuple2<Boolean, ResidentTransactionEntity> tupleResponse = idAuthService.validateOtpV2(transactionId, uin,
					downloadCardRequestDTOMainRequestDTO.getRequest().getOtp(), RequestType.GET_MY_ID);
			residentTransactionEntity = tupleResponse.getT2();
			if (residentTransactionEntity != null) {
				eventId = residentTransactionEntity.getEventId();
				if (tupleResponse.getT1()) {
					rid = getRidForIndividualId(individualId);
					pdfBytes = residentCredentialService.getCard(rid + ridSuffix, null, null);
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
					logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
							LoggerFileConstant.APPLICATIONID.toString(),
							ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
					throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
							ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
				}
			}
		} catch (ApisResourceAccessException | ResidentCredentialServiceException e) {
			throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e,
					Map.of(ResidentConstants.EVENT_ID, eventId));
		} catch (OtpValidationFailedException e) {
			throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(), e.getErrorText(),
					e, Map.of(ResidentConstants.EVENT_ID, eventId));
		} catch (Exception e) {
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
				updateResidentTransaction(individualId, residentTransactionEntity);
				residentTransactionRepository.save(residentTransactionEntity);

				TemplateType templateType = (residentTransactionEntity.getStatusCode().equals(CARD_DOWNLOADED.name()))
						? TemplateType.SUCCESS
						: TemplateType.FAILURE;

				sendNotificationV2(individualId, RequestType.GET_MY_ID, templateType, eventId, null, identityDTO);
			}
		}
		logger.debug("DownloadCardServiceImpl::getDownloadCardPDF()::exit");
		return Tuples.of(pdfBytes, eventId);
	}

	private void updateResidentTransaction(String individualId, ResidentTransactionEntity residentTransactionEntity) {
		residentTransactionEntity.setRefId(utility.convertToMaskData(individualId));
		residentTransactionEntity.setIndividualId(individualId);
		residentTransactionEntity.setRefIdType(identityService.getIndividualIdType(individualId));
		residentTransactionEntity.setUpdBy(utility.getSessionUserName());
		residentTransactionEntity.setUpdDtimes(DateUtils.getUTCCurrentDateTime());
	}

	@Override
	public Tuple2<byte[], String> downloadPersonalizedCard(
			MainRequestDTO<DownloadPersonalizedCardDto> downloadPersonalizedCardMainRequestDTO, int timeZoneOffset,
			String locale) throws ResidentServiceCheckedException {
		logger.debug("DownloadCardServiceImpl::downloadPersonalizedCard()::entry");
		String encodeHtml = downloadPersonalizedCardMainRequestDTO.getRequest().getHtml();
		byte[] decodedData = new byte[0];
		String password = null;
		String individualId = "";
		String eventId = ResidentConstants.NOT_AVAILABLE;
		ResidentTransactionEntity residentTransactionEntity = null;
		Tuple2<List<String>, Map<String, Object>> identityAttribute = null;
		try {
			individualId = identityService.getResidentIndvidualIdFromSession();
			Map<String, Object> identityAttributes = getIdentityData(individualId);
			residentTransactionEntity = createResidentTransactionEntity(individualId,
					downloadPersonalizedCardMainRequestDTO.getRequest(), (String) identityAttributes.get(IdType.UIN.name()));
			if (residentTransactionEntity != null) {
				eventId = residentTransactionEntity.getEventId();
				decodedData = CryptoUtil.decodePlainBase64(encodeHtml);
				identityAttribute = getAttributeList(identityAttributes);
				List<String> attributeValues = identityAttribute.getT1();
				if (Boolean.parseBoolean(this.environment.getProperty(ResidentConstants.IS_PASSWORD_FLAG_ENABLED))) {
					password = utility.getPassword(attributeValues);
				}
				residentTransactionEntity.setRequestSummary(
						RequestType.DOWNLOAD_PERSONALIZED_CARD.name() + " - " + ResidentConstants.SUCCESS);
				residentTransactionEntity.setStatusCode(CARD_DOWNLOADED.name());
				residentTransactionEntity.setStatusComment(CARD_DOWNLOADED.name());
			}
		} catch (Exception e) {
			if (residentTransactionEntity != null) {
				residentTransactionEntity.setRequestSummary(
						RequestType.DOWNLOAD_PERSONALIZED_CARD.name() + " - " + ResidentConstants.FAILED);
				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
				residentTransactionEntity
						.setStatusComment(ResidentErrorCode.DOWNLOAD_PERSONALIZED_CARD.getErrorMessage());
			}
			logger.error("Unable to convert html to pdf RootCause- " + e);
			throw new ResidentServiceException(ResidentErrorCode.DOWNLOAD_PERSONALIZED_CARD, e,
					Map.of(ResidentConstants.EVENT_ID, eventId));
		} finally {
			if (residentTransactionEntity != null) {
				// if the status code will come as null, it will set it as failed.
				if (residentTransactionEntity.getStatusCode() == null) {
					residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
					residentTransactionEntity
							.setStatusComment(ResidentErrorCode.DOWNLOAD_PERSONALIZED_CARD.getErrorMessage());
					residentTransactionEntity.setRequestSummary(
							RequestType.DOWNLOAD_PERSONALIZED_CARD.name() + " - " + ResidentConstants.FAILED);
				}
				residentTransactionRepository.save(residentTransactionEntity);
				TemplateType templateType = (residentTransactionEntity.getStatusCode().equals(CARD_DOWNLOADED.name()))
						? TemplateType.SUCCESS
						: TemplateType.FAILURE;
				if(identityAttribute!=null) {
					sendNotificationV2(individualId, RequestType.DOWNLOAD_PERSONALIZED_CARD, templateType, eventId,
							null, identityAttribute.getT2());
				} else {
					sendNotificationV2(individualId, RequestType.DOWNLOAD_PERSONALIZED_CARD, templateType, eventId,
							null, null);
				}
			}
		}
		logger.debug("DownloadCardServiceImpl::downloadPersonalizedCard()::exit");
		return Tuples.of(utility.signPdf(new ByteArrayInputStream(decodedData), password), eventId);
	}

	Map<String, Object> getIdentityData(String individualId) throws IOException {
		Map<String, Object> identityAttributes = null;
		try {
			identityAttributes = (Map<String, Object>) identityService.getIdentityAttributes(individualId, null);
		} catch (ResidentServiceCheckedException e) {
			logger.error("Unable to get attributes- " + e);
			throw new ResidentServiceException(ResidentErrorCode.DOWNLOAD_PERSONALIZED_CARD, e);
		} catch (IOException e) {
			logger.error("Unable to get attributes- " + e);
			throw new IOException(ResidentErrorCode.DOWNLOAD_PERSONALIZED_CARD.getErrorCode(), e);
		}
		return identityAttributes;
	}

	private ResidentTransactionEntity createResidentTransactionEntity(String individualId,
																	  DownloadPersonalizedCardDto downloadPersonalizedCardDto, String uin)
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResidentTransactionEntity residentTransactionEntity = utility
				.createEntity(RequestType.DOWNLOAD_PERSONALIZED_CARD);
		String eventId = utility.createEventId();
		residentTransactionEntity.setEventId(eventId);
		residentTransactionEntity.setAuthTypeCode(identityService.getResidentAuthenticationMode());
		residentTransactionEntity.setRefId(utility.convertToMaskData(individualId));
		residentTransactionEntity.setIndividualId(individualId);
		residentTransactionEntity.setTokenId(identityService.getIDAToken(uin));
		if (downloadPersonalizedCardDto.getAttributes() != null) {
			residentTransactionEntity.setAttributeList(
					downloadPersonalizedCardDto.getAttributes().stream().collect(Collectors.joining(SEMI_COLON)));
		}
		return residentTransactionEntity;
	}

	private Tuple2<List<String>, Map<String, Object>> getAttributeList(Map<String, Object> identityAttributes) {
		List<String> attributeValues = new ArrayList<>();
		String attributeProperty = this.environment.getProperty(ResidentConstants.PASSWORD_ATTRIBUTE);
		if (attributeProperty != null) {
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
						if (attributeInLanguage.containsKey(LANGUAGE)
								&& attributeInLanguage.get(LANGUAGE).toString().equalsIgnoreCase(languageCode)) {
							attributeValues.add((String) attributeInLanguage.get(VALUE));
						}
					}
				} else {
					attributeValues.add((String) attributeObject);
				}
			}
		}
		return Tuples.of(attributeValues, identityAttributes);
	}

	@Override
	public Tuple2<ResponseWrapper<VidDownloadCardResponseDto>, String> getVidCardEventId(String vid, int timeZoneOffset,
			String locale) throws BaseCheckedException {
		logger.debug("DownloadCardServiceImpl::getVidCardEventId()::entry");
		ResponseWrapper<VidDownloadCardResponseDto> responseWrapper = new ResponseWrapper<>();
		VidDownloadCardResponseDto vidDownloadCardResponseDto = new VidDownloadCardResponseDto();
		String eventId = ResidentConstants.NOT_AVAILABLE;
		ResidentTransactionEntity residentTransactionEntity = null;
		String uinForVid = "";
		IdentityDTO identityDTO = null;
		try {
			identityDTO = identityService.getIdentity(identityService.getResidentIndvidualIdFromSession());
			if(identityDTO!=null) {
				uinForVid = utilities.getUinByVid(vid);
			}
			residentTransactionEntity = insertDataForVidCard(vid, identityDTO.getUIN());
			if (residentTransactionEntity != null) {
				eventId = residentTransactionEntity.getEventId();
				String uinForIndividualId = identityService.
						getIdentity(identityService.getResidentIndvidualIdFromSession()).getUIN();
				if (!uinForIndividualId.equals(uinForVid)) {
					residentTransactionEntity.setRequestSummary(ResidentConstants.FAILED);
					residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
					logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
							LoggerFileConstant.APPLICATIONID.toString(),
							ResidentErrorCode.VID_NOT_BELONG_TO_USER.getErrorMessage());
					throw new ResidentServiceCheckedException(ResidentErrorCode.VID_NOT_BELONG_TO_USER,
							Map.of(ResidentConstants.EVENT_ID, eventId));
				}
			}
			RequestWrapper<CredentialReqestDto> requestDto = new RequestWrapper<>();
			CredentialReqestDto credentialReqestDto = new CredentialReqestDto();
			credentialReqestDto.setId(vid);
			credentialReqestDto
					.setCredentialType(environment.getProperty(ResidentConstants.MOSIP_CREDENTIAL_TYPE_PROPERTY));
			credentialReqestDto.setIssuer(environment.getProperty(ResidentConstants.CREDENTIAL_ISSUER));
			credentialReqestDto.setEncrypt(
					Boolean.parseBoolean(environment.getProperty(ResidentConstants.CREDENTIAL_ENCRYPTION_FLAG)));
			credentialReqestDto.setEncryptionKey(environment.getProperty(ResidentConstants.CREDENTIAL_ENCRYPTION_KEY));
			Map<String, Object> additionalAttributes = getVidDetails(vid, identityDTO, timeZoneOffset, locale);
			additionalAttributes.put(TEMPLATE_TYPE_CODE,
					this.environment.getProperty(ResidentConstants.VID_CARD_TEMPLATE_PROPERTY));
			additionalAttributes.put(APPLICANT_PHOTO,
					identityService.getAvailableclaimValue(environment.getProperty(ResidentConstants.IMAGE)));
			credentialReqestDto.setAdditionalData(additionalAttributes);
			requestDto.setId(this.environment.getProperty(ResidentConstants.CREDENTIAL_REQUEST_SERVICE_ID));
			requestDto.setRequest(credentialReqestDto);
			requestDto.setRequesttime(DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime()));
			requestDto.setVersion(ResidentConstants.CREDENTIAL_REQUEST_SERVICE_VERSION);
			ResponseWrapper<ResidentCredentialResponseDto> responseDto = residentServiceRestClient.postApi(
					environment.getProperty(ApiName.CREDENTIAL_REQ_URL.name()), MediaType.APPLICATION_JSON, requestDto,
					ResponseWrapper.class);
			if (responseDto.getErrors().size() == 0) {
				ResidentCredentialResponseDto residentCredentialResponseDto = JsonUtil.readValue(
						JsonUtil.writeValueAsString(responseDto.getResponse()), ResidentCredentialResponseDto.class);
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
		} catch (IOException exception) {
			if (residentTransactionEntity != null) {
				residentTransactionEntity.setRequestSummary(ResidentConstants.FAILED);
				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
			}
			throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(), exception.getMessage(),
					exception);
		} finally {
			if (residentTransactionEntity != null) {
				// if the status code will come as null, it will set it as failed.
				if (residentTransactionEntity.getStatusCode() == null) {
					residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
					residentTransactionEntity.setRequestSummary(ResidentConstants.FAILED);
				}
				residentTransactionRepository.save(residentTransactionEntity);

				TemplateType templateType = (residentTransactionEntity.getStatusCode()
						.equals(EventStatusInProgress.NEW.name())) ? TemplateType.REQUEST_RECEIVED
								: TemplateType.FAILURE;

				sendNotificationV2(identityDTO.getUIN(), RequestType.VID_CARD_DOWNLOAD, templateType,
						eventId, null, identityDTO);
			}
		}
		responseWrapper.setId(environment.getProperty(ResidentConstants.VID_DOWNLOAD_CARD_ID));
		responseWrapper.setVersion(environment.getProperty(ResidentConstants.VID_DOWNLOAD_CARD_VERSION));
		responseWrapper.setResponsetime(DateUtils.getUTCCurrentDateTimeString());
		responseWrapper.setResponse(vidDownloadCardResponseDto);
		logger.debug("DownloadCardServiceImpl::getVidCardEventId()::exit");
		return Tuples.of(responseWrapper, eventId);
	}

	@Override
	public ResponseWrapper<CheckStatusResponseDTO> getIndividualIdStatus(String individualId)
			throws ApisResourceAccessException, IOException, ResidentServiceCheckedException {
		logger.debug("DownloadCardServiceImpl::getIndividualIdStatus()::entry");
		String rid = getRidForIndividualId(individualId);
		Map<String, String> packetStatusMap = utilities.getPacketStatus(rid);
		try {
			String transactionTypeCode = packetStatusMap.get(ResidentConstants.TRANSACTION_TYPE_CODE);
			String aidStatus = packetStatusMap.get(ResidentConstants.AID_STATUS);
			if (transactionTypeCode.equalsIgnoreCase(TransactionStage.CARD_READY_TO_DOWNLOAD.name())
					&& aidStatus.equalsIgnoreCase(PacketStatus.SUCCESS.getName())) {
				residentCredentialService.getDataShareUrl(rid + ridSuffix);
			}
		} catch (ResidentCredentialServiceException e) {
			logger.info("Since datashare URL is not available, marking the aid status as in-progress.");
			packetStatusMap.put(ResidentConstants.AID_STATUS, PacketStatus.IN_PROGRESS.getName());
		}
		logger.debug("DownloadCardServiceImpl::getIndividualIdStatus()::exit");
		return getCheckStatusResponse(packetStatusMap);
	}

	private String getRidForIndividualId(String individualId) {
		String idType = identityService.getIndividualIdType(individualId);
		if (idType.equalsIgnoreCase(IdType.AID.name())) {
			return individualId;
		} else {
			try {
				return utilities.getRidByIndividualId(individualId);
			} catch (ApisResourceAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private ResponseWrapper<CheckStatusResponseDTO> getCheckStatusResponse(Map<String, String> packetStatusMap) {
		ResponseWrapper<CheckStatusResponseDTO> checkStatusResponseDTOResponseWrapper = new ResponseWrapper<>();
		CheckStatusResponseDTO checkStatusResponseDTO = new CheckStatusResponseDTO();
		String aidStatus = packetStatusMap.get(ResidentConstants.AID_STATUS);
		String transactionStage = packetStatusMap.get(ResidentConstants.TRANSACTION_TYPE_CODE);
		checkStatusResponseDTO.setAidStatus(aidStatus);
		checkStatusResponseDTO.setTransactionStage(transactionStage);
		checkStatusResponseDTOResponseWrapper.setResponse(checkStatusResponseDTO);
		checkStatusResponseDTOResponseWrapper
				.setId(this.environment.getProperty(ResidentConstants.CHECK_STATUS_INDIVIDUAL_ID));
		checkStatusResponseDTOResponseWrapper
				.setVersion(this.environment.getProperty(ResidentConstants.CHECKSTATUS_INDIVIDUALID_VERSION));
		checkStatusResponseDTOResponseWrapper
				.setResponsetime(DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime()));
		return checkStatusResponseDTOResponseWrapper;
	}

	private ResidentTransactionEntity insertDataForVidCard(String vid, String uin)
			throws ApisResourceAccessException, IOException, ResidentServiceCheckedException {
		ResidentTransactionEntity residentTransactionEntity = utility.createEntity(RequestType.VID_CARD_DOWNLOAD);
		residentTransactionEntity.setEventId(utility.createEventId());
		residentTransactionEntity.setAuthTypeCode(identityService.getResidentAuthenticationMode());
		residentTransactionEntity.setRefId(utility.convertToMaskData(uin));
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

	private Map<String, Object> getVidDetails(String vid, IdentityDTO identityDTO, int timeZoneOffset, String locale)
			throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
		Map<String, Object> additionalAttributes = new HashMap<>();
		String name = null;
		ResponseWrapper<List<Map<String, ?>>> vidResponse = null;
		String uin=null;
		if(identityDTO!=null){
			uin = identityDTO.getUIN();
			name = identityDTO.getFullName();
		}
		if (uin != null) {
			vidResponse = vidService.retrieveVids(timeZoneOffset, locale, uin);
		}
		if (vidResponse != null) {
			List<Map<String, ?>> vidList = vidResponse.getResponse();
			if (vidList.size() > 0) {
				for (Map<String, ?> vidData : vidList) {
					if (vidData.get(VID).toString().equalsIgnoreCase(vid)) {
						additionalAttributes.put(VID, vid);
						additionalAttributes.put(VID_TYPE, vidData.get(VID_TYPE));
						additionalAttributes.put(MASKED_VID, vidData.get(MASKED_VID));
						additionalAttributes.put(EXPIRY_TIMESTAMP,
								replaceNullValueWithNA(vidData.get(EXPIRY_TIMESTAMP)));
						additionalAttributes.put(GENERATED_ON_TIMESTAMP,
								replaceNullValueWithNA(vidData.get(GENERATED_ON_TIMESTAMP)));
						additionalAttributes.put(TRANSACTION_LIMIT,
								replaceNullValueWithNA(vidData.get(TRANSACTION_LIMIT)));
						additionalAttributes.put(TRANSACTION_COUNT,
								replaceNullValueWithNA(vidData.get(TRANSACTION_COUNT)));
						additionalAttributes.put(CARD_FORMAT, VID_CARD);
						if(identityDTO!=null) {
							additionalAttributes.put(DATE_OF_BIRTH, identityDTO.getDateOfBirth());
						}
						if (name != null) {
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
		if (o == null) {
			return NA;
		}
		return o;
	}

	private void sendNotificationV2(String id, RequestType requestType, TemplateType templateType, String eventId,
									Map<String, Object> additionalAttributes, Map identity) throws ResidentServiceCheckedException {
		NotificationRequestDtoV2 notificationRequestDtoV2 = new NotificationRequestDtoV2();
		notificationRequestDtoV2.setId(id);
		notificationRequestDtoV2.setRequestType(requestType);
		notificationRequestDtoV2.setTemplateType(templateType);
		notificationRequestDtoV2.setEventId(eventId);
		notificationRequestDtoV2.setAdditionalAttributes(additionalAttributes);
		notificationService.sendNotification(notificationRequestDtoV2, identity);
	}

}
