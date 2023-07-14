package io.mosip.resident.service.impl;

import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.ConsentStatusType;
import io.mosip.resident.constant.EventStatusFailure;
import io.mosip.resident.constant.EventStatusInProgress;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.NotificationTemplateCode;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.constant.TemplateType;
import io.mosip.resident.dto.CredentialCancelRequestResponseDto;
import io.mosip.resident.dto.CredentialReqestDto;
import io.mosip.resident.dto.CredentialRequestStatusDto;
import io.mosip.resident.dto.CredentialRequestStatusResponseDto;
import io.mosip.resident.dto.CredentialTypeResponse;
import io.mosip.resident.dto.CryptomanagerRequestDto;
import io.mosip.resident.dto.CryptomanagerResponseDto;
import io.mosip.resident.dto.NotificationRequestDto;
import io.mosip.resident.dto.NotificationRequestDtoV2;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.dto.PartnerCredentialTypePolicyDto;
import io.mosip.resident.dto.PartnerResponseDto;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentCredentialRequestDto;
import io.mosip.resident.dto.ResidentCredentialResponseDto;
import io.mosip.resident.dto.ResidentCredentialResponseDtoV2;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.SharableAttributesDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentCredentialServiceException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ResidentCredentialService;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utility;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Service
public class ResidentCredentialServiceImpl implements ResidentCredentialService {

	private static final String PARTNER_TYPE = "partnerType";
	private static final String ORGANIZATION_NAME = "organizationName";

	@Autowired
	IdAuthService idAuthService;

	@Value("${crypto.PrependThumbprint.enable:true}")
	private boolean isPrependThumbprintEnabled;

	@Value("${PARTNER_REFERENCE_Id}")
	private String partnerReferenceId;

	@Value("${APPLICATION_Id}")
	private String applicationId;

	@Value("${mosip.resident.pin.max:999999}")
	private int max;

	@Value("${mosip.resident.pin.min:100000}")
	private int min;

	@Autowired
	private ObjectMapper mapper;

	private static final Logger logger = LoggerConfiguration.logConfig(ResidentCredentialServiceImpl.class);

	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;

	@Autowired
	Environment env;

	@Autowired
	private Utility utility;

	@Autowired
	private IdentityServiceImpl identityServiceImpl;

	@Autowired
	private ResidentTransactionRepository residentTransactionRepository;

	@Autowired
	NotificationService notificationService;
	
	@Autowired
    private ProxyPartnerManagementServiceImpl proxyPartnerManagementServiceImpl;

	private SecureRandom random;
	
	@Value("${mosip.resident.request.credential.credentialType}")
	private String credentialType;
	
	@Value("${mosip.resident.request.credential.isEncrypt}")
	private boolean isEncrypt;
	
	@Value("${mosip.resident.request.credential.encryption.key}")
	private String encryptionKey;

	@Value("${mosip.registration.processor.rid.delimiter}")
	private String ridSuffix;
	
	@Override
	public ResidentCredentialResponseDto reqCredential(ResidentCredentialRequestDto dto)
			throws ResidentServiceCheckedException {
		Map<String, Object> additionalAttributes = new HashMap<>();
		try {
			if (idAuthService.validateOtp(dto.getTransactionID(), dto.getIndividualId(), dto.getOtp())) {
				return reqCredential(dto, null);
			} else {
				logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
						LoggerFileConstant.APPLICATIONID.toString(),
						ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
				sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_CRE_REQ_FAILURE,
						additionalAttributes);
				throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
						ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
			}
		} catch (OtpValidationFailedException e) {
			trySendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_CRE_REQ_FAILURE,
					additionalAttributes);
			throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(), e.getErrorText(),
					e);
		}
	}

	@Override
	public ResidentCredentialResponseDto reqCredential(ResidentCredentialRequestDto dto, String individualId)
			throws ResidentServiceCheckedException {
		logger.debug("ResidentCredentialServiceImpl::reqCredential()::entry");
		ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();
		RequestWrapper<CredentialReqestDto> requestDto = new RequestWrapper<>();
		ResponseWrapper<PartnerResponseDto> parResponseDto = new ResponseWrapper<PartnerResponseDto>();
		PartnerResponseDto partnerResponseDto = new PartnerResponseDto();
		CredentialReqestDto credentialReqestDto = new CredentialReqestDto();
		Map<String, Object> additionalAttributes = new HashMap<>();
		String partnerUrl = env.getProperty(ApiName.PARTNER_API_URL.name()) + "/" + dto.getIssuer();
		URI partnerUri = URI.create(partnerUrl);
		try {
				credentialReqestDto = prepareCredentialRequest(dto, individualId);
				requestDto.setId("mosip.credential.request.service.id");
				requestDto.setRequest(credentialReqestDto);
				requestDto.setRequesttime(DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime()));
				requestDto.setVersion("1.0");
				parResponseDto = residentServiceRestClient.getApi(partnerUri, ResponseWrapper.class);
				partnerResponseDto = JsonUtil.readValue(JsonUtil.writeValueAsString(parResponseDto.getResponse()),
						PartnerResponseDto.class);
				additionalAttributes.put("partnerName", partnerResponseDto.getOrganizationName());
				additionalAttributes.put("encryptionKey", credentialReqestDto.getEncryptionKey());
				additionalAttributes.put("credentialName", credentialReqestDto.getCredentialType());

				ResponseWrapper<ResidentCredentialResponseDto> responseDto = residentServiceRestClient.postApi(
						env.getProperty(ApiName.CREDENTIAL_REQ_URL.name()), MediaType.APPLICATION_JSON, requestDto,
						ResponseWrapper.class);
				residentCredentialResponseDto = JsonUtil.readValue(
						JsonUtil.writeValueAsString(responseDto.getResponse()), ResidentCredentialResponseDto.class);
				additionalAttributes.put("RID", residentCredentialResponseDto.getRequestId());
				if(!Utility.isSecureSession()){
					sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_CRE_REQ_SUCCESS,
							additionalAttributes);
				}
				
		} catch (ResidentServiceCheckedException | ApisResourceAccessException e) {
			if(!Utility.isSecureSession()){
				sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_CRE_REQ_FAILURE, additionalAttributes);
			}
			throw new ResidentCredentialServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		} catch (IOException e) {
			if(!Utility.isSecureSession()){
				sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_CRE_REQ_FAILURE, additionalAttributes);
			}
			throw new ResidentCredentialServiceException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ResidentCredentialServiceImpl::reqCredential()::exit");
		return residentCredentialResponseDto;
	}

	@Override
	public Tuple2<ResidentCredentialResponseDtoV2, String> shareCredential(ResidentCredentialRequestDto dto,
			String purpose, List<SharableAttributesDTO> sharableAttributes) throws ResidentServiceCheckedException, ApisResourceAccessException {
		logger.debug("ResidentCredentialServiceImpl::shareCredential()::entry");
		ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();
		ResidentCredentialResponseDtoV2 residentCredentialResponseDtoV2=new ResidentCredentialResponseDtoV2();
		RequestWrapper<CredentialReqestDto> requestDto = new RequestWrapper<>();
		ResponseWrapper<PartnerResponseDto> parResponseDto = new ResponseWrapper<PartnerResponseDto>();
		PartnerResponseDto partnerResponseDto = new PartnerResponseDto();
		CredentialReqestDto credentialReqestDto = new CredentialReqestDto();
		Map<String, Object> additionalAttributes = new HashMap<>();
		String partnerUrl = env.getProperty(ApiName.PARTNER_API_URL.name()) + "/" + dto.getIssuer();
		URI partnerUri = URI.create(partnerUrl);
		String individualId = identityServiceImpl.getResidentIndvidualIdFromSession();
		String eventId = ResidentConstants.NOT_AVAILABLE;
		ResidentTransactionEntity residentTransactionEntity = null;
		try {
			
			residentTransactionEntity = createResidentTransactionEntity(dto, individualId, purpose, sharableAttributes);
			if (residentTransactionEntity != null) {
    			eventId = residentTransactionEntity.getEventId();
    		}
			if (dto.getConsent() == null || dto.getConsent().trim().isEmpty()
					|| !dto.getConsent().equalsIgnoreCase(ConsentStatusType.ACCEPTED.name())) {
				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
				residentTransactionEntity.setRequestSummary(String.format("%s - %s", RequestType.SHARE_CRED_WITH_PARTNER.getName(), EventStatusFailure.FAILED.name()));
				throw new ResidentServiceException(ResidentErrorCode.CONSENT_DENIED.getErrorCode(),
						ResidentErrorCode.CONSENT_DENIED.getErrorMessage());
			}
			credentialReqestDto = prepareCredentialRequest(dto, individualId);
			requestDto.setId("mosip.credential.request.service.id");
			requestDto.setRequest(credentialReqestDto);
			requestDto.setRequesttime(DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime()));
			requestDto.setVersion("1.0");
			parResponseDto = residentServiceRestClient.getApi(partnerUri, ResponseWrapper.class);
			partnerResponseDto = JsonUtil.readValue(JsonUtil.writeValueAsString(parResponseDto.getResponse()),
					PartnerResponseDto.class);
			additionalAttributes.put("partnerName", partnerResponseDto.getOrganizationName());
			additionalAttributes.put("encryptionKey", credentialReqestDto.getEncryptionKey());
			additionalAttributes.put("credentialName", credentialReqestDto.getCredentialType());

			ResponseWrapper<ResidentCredentialResponseDto> responseDto = residentServiceRestClient.postApi(
					env.getProperty(ApiName.CREDENTIAL_REQ_URL.name()), MediaType.APPLICATION_JSON, requestDto,
					ResponseWrapper.class);
			residentCredentialResponseDto = JsonUtil.readValue(JsonUtil.writeValueAsString(responseDto.getResponse()),
					ResidentCredentialResponseDto.class);
			additionalAttributes.put("RID", residentCredentialResponseDto.getRequestId());
			sendNotificationV2(individualId, RequestType.SHARE_CRED_WITH_PARTNER, TemplateType.REQUEST_RECEIVED,
					eventId, additionalAttributes);

			updateResidentTransaction(dto, residentCredentialResponseDto, residentTransactionEntity);
			residentCredentialResponseDtoV2.setStatus(ResidentConstants.SUCCESS);
		} catch (ResidentServiceCheckedException | ApisResourceAccessException e) {
			if (residentTransactionEntity != null) {
				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
				residentTransactionEntity.setRequestSummary(String.format("%s - %s", RequestType.SHARE_CRED_WITH_PARTNER.getName(), EventStatusFailure.FAILED.name()));
			}
			sendNotificationV2(individualId, RequestType.SHARE_CRED_WITH_PARTNER, TemplateType.FAILURE,
					eventId, additionalAttributes);
			throw new ResidentCredentialServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION, e,
					Map.of(ResidentConstants.EVENT_ID, eventId));
		} catch (IOException e) {
			if (residentTransactionEntity != null) {
				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
				residentTransactionEntity.setRequestSummary(String.format("%s - %s", RequestType.SHARE_CRED_WITH_PARTNER.getName(), EventStatusFailure.FAILED.name()));
			}
			sendNotificationV2(individualId, RequestType.SHARE_CRED_WITH_PARTNER, TemplateType.FAILURE,
					eventId, additionalAttributes);
			throw new ResidentCredentialServiceException(ResidentErrorCode.IO_EXCEPTION, e,
					Map.of(ResidentConstants.EVENT_ID, eventId));
		} finally {
			if (residentTransactionEntity != null) {
				//if the status code or request summary will come as null, it will set it as failed.
				if(residentTransactionEntity.getStatusCode() == null || residentTransactionEntity.getRequestSummary() == null) {
					residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
					residentTransactionEntity.setStatusComment(EventStatusFailure.FAILED.name());
					residentTransactionEntity.setRequestSummary(String.format("%s - %s", RequestType.SHARE_CRED_WITH_PARTNER.getName(), EventStatusFailure.FAILED.name()));
				}
				residentTransactionRepository.save(residentTransactionEntity);
			}
		}
		logger.debug("ResidentCredentialServiceImpl::shareCredential()::exit");
		return Tuples.of(residentCredentialResponseDtoV2, eventId);
	}

	private ResidentTransactionEntity createResidentTransactionEntity(ResidentCredentialRequestDto dto,
			String individualId, String purpose, List<SharableAttributesDTO> sharableAttributes) throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResidentTransactionEntity residentTransactionEntity = utility.createEntity(RequestType.SHARE_CRED_WITH_PARTNER);
		residentTransactionEntity.setEventId(utility.createEventId());
		residentTransactionEntity.setRefId(utility.convertToMaskData(individualId));
		residentTransactionEntity.setIndividualId(individualId);
		residentTransactionEntity.setTokenId(identityServiceImpl.getResidentIdaToken());
		residentTransactionEntity.setAuthTypeCode(identityServiceImpl.getResidentAuthenticationMode());
		if (purpose != null) {
			residentTransactionEntity.setPurpose(purpose);
		}
		if (sharableAttributes != null && !sharableAttributes.isEmpty()) {
			String data = sharableAttributes.stream().map(map -> {
				if (map.getFormat() != null && !map.getFormat().isEmpty()) {
					return String.format("%s%s%s", map.getAttributeName(), ResidentConstants.COLON, map.getFormat());
				} else {
					return map.getAttributeName();
				}
			}).collect(Collectors.joining(ResidentConstants.SEMI_COLON));
			residentTransactionEntity.setAttributeList(data);
		}
		residentTransactionEntity.setRequestedEntityId(dto.getIssuer());
		Map<String, ?> partnerDetail = proxyPartnerManagementServiceImpl.getPartnerDetailFromPartnerId(dto.getIssuer());
		residentTransactionEntity.setRequestedEntityName((String) partnerDetail.get(ORGANIZATION_NAME));
		residentTransactionEntity.setRequestedEntityType((String) partnerDetail.get(PARTNER_TYPE));
		residentTransactionEntity.setConsent(dto.getConsent());
		return residentTransactionEntity;
	}

	private void updateResidentTransaction(ResidentCredentialRequestDto dto,
			ResidentCredentialResponseDto residentCredentialResponseDto,
			ResidentTransactionEntity residentTransactionEntity) {
		// TODO: need to fix transaction ID (need partner's end transactionId)
		residentTransactionEntity.setRequestTrnId(dto.getTransactionID());
		residentTransactionEntity.setStatusCode(EventStatusInProgress.NEW.name());
		residentTransactionEntity.setStatusComment(EventStatusInProgress.NEW.name());
		residentTransactionEntity.setRequestSummary(String.format("%s - %s", RequestType.SHARE_CRED_WITH_PARTNER.getName(), EventStatusInProgress.NEW.name()));
		residentTransactionEntity.setAid(residentCredentialResponseDto.getRequestId());
		residentTransactionEntity.setCredentialRequestId(residentCredentialResponseDto.getRequestId());
	}
	@Override
	public byte[] getCard(String requestId) throws Exception {
		return getCard(requestId, applicationId, partnerReferenceId);
	}
	@Override
	public byte[] getCard(String requestId, String appId, String partnerRefId) throws Exception {
		try {
			String dataShareUrl = getDataShareUrl(requestId);
			URI dataShareUri = URI.create(dataShareUrl);
			if(appId!=null){
				return getDataShareData(appId, partnerRefId, dataShareUri);
			}else {
				return residentServiceRestClient.getApi(dataShareUri, byte[].class);
			}
		} catch (ApisResourceAccessException e) {
			throw new ResidentCredentialServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new ResidentCredentialServiceException(ResidentErrorCode.INVALID_ID.getErrorCode(),
					ResidentErrorCode.INVALID_ID.getErrorMessage(), e);
		} catch (IOException e) {
			throw new ResidentCredentialServiceException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		}

	}

	@Override
	public String getDataShareUrl(String requestId) throws ApisResourceAccessException, IOException {
		logger.debug("ResidentCredentialServiceImpl::getDataShareUrl()::entry");
		ResponseWrapper<CredentialRequestStatusDto> responseDto = null;
		CredentialRequestStatusDto credentialRequestStatusResponseDto = new CredentialRequestStatusDto();
		String credentialUrl = "";
		if(requestId.contains(ridSuffix)) {
			credentialUrl = env.getProperty(ApiName.CREDENTIAL_STATUS_URL.name()) + requestId;
		} else {
			UUID requestUUID = UUID.fromString(requestId);
			credentialUrl = env.getProperty(ApiName.CREDENTIAL_STATUS_URL.name()) + requestUUID;
		}
		URI credentailStatusUri = URI.create(credentialUrl);
		responseDto = residentServiceRestClient.getApi(credentailStatusUri, ResponseWrapper.class);
		credentialRequestStatusResponseDto = JsonUtil.readValue(
				JsonUtil.writeValueAsString(responseDto.getResponse()), CredentialRequestStatusDto.class);
		if(credentialRequestStatusResponseDto == null || credentialRequestStatusResponseDto.getUrl() == null
		|| credentialRequestStatusResponseDto.getUrl().isEmpty()){
			logger.error("Data share URL is not available.");
			throw new ResidentCredentialServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage());
		}
		logger.debug("ResidentCredentialServiceImpl::getDataShareUrl()::exit");
		return credentialRequestStatusResponseDto.getUrl();
	}

	public byte[] getDataShareData(String appId, String partnerRefId, URI dataShareUri)
			throws ApisResourceAccessException, JsonProcessingException, JsonMappingException {
		logger.debug("ResidentCredentialServiceImpl::getDataShareData()::entry");
		String downloadedData = residentServiceRestClient.getApi(dataShareUri, String.class);
		RequestWrapper<CryptomanagerRequestDto> request = new RequestWrapper<>();
		CryptomanagerRequestDto cryptomanagerRequestDto = new CryptomanagerRequestDto();
		cryptomanagerRequestDto.setApplicationId(appId);
		cryptomanagerRequestDto.setData(downloadedData);
		cryptomanagerRequestDto.setReferenceId(partnerRefId);
		cryptomanagerRequestDto.setPrependThumbprint(isPrependThumbprintEnabled);
		LocalDateTime localdatetime = DateUtils.getUTCCurrentDateTime();
		request.setRequesttime(DateUtils.formatToISOString(localdatetime));
		cryptomanagerRequestDto.setTimeStamp(localdatetime);
		request.setRequest(cryptomanagerRequestDto);
		String response = residentServiceRestClient.postApi(env.getProperty(ApiName.DECRYPT_API_URL.name()),
				MediaType.APPLICATION_JSON, request, String.class);
		CryptomanagerResponseDto responseObject = mapper.readValue(response, CryptomanagerResponseDto.class);
		logger.debug("ResidentCredentialServiceImpl::getDataShareData()::exit");
		return CryptoUtil.decodeURLSafeBase64(responseObject.getResponse().getData());
	}

	@Override
	public CredentialRequestStatusResponseDto getStatus(String requestId) {
		logger.debug("ResidentCredentialServiceImpl::getStatus()::entry");
		ResponseWrapper<CredentialRequestStatusDto> responseDto = null;
		CredentialRequestStatusDto credentialRequestStatusDto = new CredentialRequestStatusDto();
		Map<String, Object> additionalAttributes = new HashedMap();
		CredentialRequestStatusResponseDto credentialRequestStatusResponseDto = new CredentialRequestStatusResponseDto();
		try {
			UUID requestUUID = UUID.fromString(requestId);
			String credentialUrl = env.getProperty(ApiName.CREDENTIAL_STATUS_URL.name()) + requestUUID;
			URI credentailStatusUri = URI.create(credentialUrl);
			responseDto = residentServiceRestClient.getApi(credentailStatusUri, ResponseWrapper.class);
			credentialRequestStatusDto = JsonUtil.readValue(JsonUtil.writeValueAsString(responseDto.getResponse()),
					CredentialRequestStatusDto.class);
			credentialRequestStatusResponseDto.setId(credentialRequestStatusDto.getId());
			credentialRequestStatusResponseDto.setRequestId(credentialRequestStatusDto.getRequestId());
			credentialRequestStatusResponseDto.setStatusCode(credentialRequestStatusDto.getStatusCode());
			additionalAttributes.put("RID", credentialRequestStatusResponseDto.getRequestId());
			additionalAttributes.put("status", credentialRequestStatusResponseDto.getStatusCode());
			sendNotification(credentialRequestStatusResponseDto.getId(), NotificationTemplateCode.RS_CRE_STATUS,
					additionalAttributes);

		} catch (ApisResourceAccessException e) {
			throw new ResidentCredentialServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		} catch (IOException e) {
			throw new ResidentCredentialServiceException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new ResidentCredentialServiceException(ResidentErrorCode.INVALID_ID.getErrorCode(),
					ResidentErrorCode.INVALID_ID.getErrorMessage(), e);
		} catch (ResidentServiceCheckedException e) {
			throw new ResidentCredentialServiceException(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ResidentCredentialServiceImpl::getStatus()::exit");
		return credentialRequestStatusResponseDto;
	}

	private CredentialReqestDto prepareCredentialRequest(ResidentCredentialRequestDto residentCreDto, String individualId) {
		logger.debug("ResidentCredentialServiceImpl::prepareCredentialRequest()::entry");
		CredentialReqestDto crDto = new CredentialReqestDto();
		if(Utility.isSecureSession()){
			crDto.setId(individualId);
			crDto.setCredentialType(credentialType);
			crDto.setEncrypt(isEncrypt);
			crDto.setEncryptionKey(encryptionKey);
		} else {
			crDto.setId(residentCreDto.getIndividualId());
			crDto.setCredentialType(residentCreDto.getCredentialType());
			crDto.setEncrypt(residentCreDto.isEncrypt());
			if (residentCreDto.getEncryptionKey() == null || residentCreDto.getEncryptionKey().isEmpty()) {
				crDto.setEncryptionKey(generatePin());
			} else {
				crDto.setEncryptionKey(residentCreDto.getEncryptionKey());
			}
			crDto.setRecepiant(residentCreDto.getRecepiant());
			crDto.setUser(residentCreDto.getUser());
		}
		crDto.setSharableAttributes(residentCreDto.getSharableAttributes());
		crDto.setAdditionalData(residentCreDto.getAdditionalData());
		crDto.setIssuer(residentCreDto.getIssuer());
		logger.debug("ResidentCredentialServiceImpl::prepareCredentialRequest()::exit");
		return crDto;
	}

	@Override
	public CredentialCancelRequestResponseDto cancelCredentialRequest(String requestId) {
		logger.debug("ResidentCredentialServiceImpl::cancelCredentialRequest()::exit");
		ResponseWrapper<CredentialCancelRequestResponseDto> response = new ResponseWrapper<CredentialCancelRequestResponseDto>();
		Map<String, Object> additionalAttributes = new HashedMap();
		CredentialCancelRequestResponseDto credentialCancelRequestResponseDto = new CredentialCancelRequestResponseDto();
		try {
			UUID requestUUID = UUID.fromString(requestId);
			String credentialReqCancelUrl = env.getProperty(ApiName.CREDENTIAL_CANCELREQ_URL.name()) + requestUUID;
			URI credentailReqCancelUri = URI.create(credentialReqCancelUrl);
			response = residentServiceRestClient.getApi(credentailReqCancelUri, ResponseWrapper.class);
			if (response.getErrors() != null && !response.getErrors().isEmpty()) {
				throw new ResidentCredentialServiceException(response.getErrors().get(0).getErrorCode(),
						response.getErrors().get(0).getMessage());
			}
			credentialCancelRequestResponseDto = JsonUtil.readValue(JsonUtil.writeValueAsString(response.getResponse()),
					CredentialCancelRequestResponseDto.class);
			additionalAttributes.put("RID", credentialCancelRequestResponseDto.getRequestId());
			sendNotification(credentialCancelRequestResponseDto.getId(), NotificationTemplateCode.RS_CRE_CANCEL_SUCCESS,
					additionalAttributes);

		} catch (ApisResourceAccessException e) {
			throw new ResidentCredentialServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new ResidentCredentialServiceException(ResidentErrorCode.INVALID_ID.getErrorCode(),
					ResidentErrorCode.INVALID_ID.getErrorMessage(), e);
		} catch (IOException e) {
			throw new ResidentCredentialServiceException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		} catch (ResidentServiceCheckedException e) {
			throw new ResidentCredentialServiceException(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorMessage());
		}
		logger.debug("ResidentCredentialServiceImpl::cancelCredentialRequest()::exit");
		return credentialCancelRequestResponseDto;
	}

	@Override
	public CredentialTypeResponse getCredentialTypes() {
		logger.debug("ResidentCredentialServiceImpl::getCredentialTypes()::entry");
		CredentialTypeResponse credentialTypeResponse = new CredentialTypeResponse();
		URI credentailTypesUri = URI.create(env.getProperty(ApiName.CREDENTIAL_TYPES_URL.name()));
		try {
			credentialTypeResponse = residentServiceRestClient.getApi(credentailTypesUri, CredentialTypeResponse.class);
		} catch (ApisResourceAccessException e) {
			throw new ResidentCredentialServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ResidentCredentialServiceImpl::getCredentialTypes()::exit");
		return credentialTypeResponse;
	}

	@Override
	public ResponseWrapper<PartnerCredentialTypePolicyDto> getPolicyByCredentialType(String partnerId,
			String credentialType) {
		logger.debug("ResidentCredentialServiceImpl::getPolicyByCredentialType()::entry");
		ResponseWrapper<PartnerCredentialTypePolicyDto> response = new ResponseWrapper<PartnerCredentialTypePolicyDto>();
		Map<String, String> pathsegments = new HashMap<>();
		pathsegments.put("partnerId", partnerId);
		pathsegments.put("credentialType", credentialType);
		try {
			response = residentServiceRestClient.getApi(ApiName.POLICY_REQ_URL, pathsegments, ResponseWrapper.class);
		} catch (ApisResourceAccessException e) {
			throw new ResidentCredentialServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ResidentCredentialServiceImpl::getPolicyByCredentialType()::exit");
		return response;
	}

	public String generatePin() {
		if (random == null)
			instantiate();
		int randomInteger = random.nextInt(max - min) + min;
		return String.valueOf(randomInteger);
	}

	@Scheduled(fixedDelayString = "${mosip.resident.pingeneration.refresh.millisecs:1800000}", initialDelayString = "${mosip.resident.pingeneration.refresh.delay-on-startup.millisecs:5000}")
	public void instantiate() {
		logger.debug("Instantiating SecureRandom for credential pin generation............");
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			logger.error("Could not instantiate SecureRandom for pin generation", e);
		}
	}

	private NotificationResponseDTO trySendNotification(String id, NotificationTemplateCode templateTypeCode,
			Map<String, Object> additionalAttributes) throws ResidentServiceCheckedException {
		try {
			return sendNotification(id, templateTypeCode, additionalAttributes);
		} catch (Exception e1) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					ResidentErrorCode.NOTIFICATION_FAILURE.getErrorCode()
							+ ResidentErrorCode.NOTIFICATION_FAILURE.getErrorMessage()
							+ ExceptionUtils.getStackTrace(e1));
		}
		return null;
	}

	private NotificationResponseDTO sendNotification(String id, NotificationTemplateCode templateTypeCode,
			Map<String, Object> additionalAttributes) throws ResidentServiceCheckedException {
		NotificationRequestDto notificationRequest = new NotificationRequestDto(id, templateTypeCode,
				additionalAttributes);
		return notificationService.sendNotification(notificationRequest, null);
	}

	private NotificationResponseDTO sendNotificationV2(String id, RequestType requestType, TemplateType templateType,
			String eventId, Map<String, Object> additionalAttributes) throws ResidentServiceCheckedException {

		NotificationRequestDtoV2 notificationRequestDtoV2 = new NotificationRequestDtoV2();
		notificationRequestDtoV2.setId(id);
		notificationRequestDtoV2.setRequestType(requestType);
		notificationRequestDtoV2.setTemplateType(templateType);
		notificationRequestDtoV2.setEventId(eventId);
		notificationRequestDtoV2.setAdditionalAttributes(additionalAttributes);
		return notificationService.sendNotification(notificationRequestDtoV2, null);
	}
}
