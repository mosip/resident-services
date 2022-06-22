package io.mosip.resident.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.NotificationTemplateCode;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.*;
import io.mosip.resident.exception.*;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ResidentCredentialService;
import io.mosip.resident.util.*;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ResidentCredentialServiceImpl implements ResidentCredentialService {

	private static final String INDIVIDUAL_ID = "individualId";
	private static final Object AVAILABLE = "AVAILABLE";

	@Autowired
	IdAuthService idAuthService;

	@Autowired
	private TokenGenerator tokenGenerator;

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

	@Autowired
	private AuditUtil audit;

	private static final Logger logger = LoggerConfiguration.logConfig(ResidentCredentialServiceImpl.class);
	
	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;

	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	Environment env;

	@Autowired
	NotificationService notificationService;

	private SecureRandom random;
	
	@Override
	public ResidentCredentialResponseDto reqCredential(ResidentCredentialRequestDto dto)
			throws ResidentServiceCheckedException {
		ResidentCredentialResponseDto residentCredentialResponseDto=new ResidentCredentialResponseDto();
		RequestWrapper<CredentialReqestDto> requestDto = new RequestWrapper<>();
		ResponseWrapper<PartnerResponseDto> parResponseDto = new ResponseWrapper<PartnerResponseDto>();
		PartnerResponseDto partnerResponseDto = new PartnerResponseDto();
		CredentialReqestDto credentialReqestDto=new CredentialReqestDto();
		Map<String, Object> additionalAttributes = new HashMap<>();
		String partnerUrl = env.getProperty(ApiName.PARTNER_API_URL.name()) + "/" + dto.getIssuer();
		URI partnerUri = URI.create(partnerUrl);
		try {
			if(StringUtils.isBlank(dto.getIndividualId())) {
				throw new ResidentServiceException(ResidentErrorCode.INVALID_INPUT.getErrorCode(), ResidentErrorCode.INVALID_INPUT.getErrorMessage() + INDIVIDUAL_ID);
			}

			if (idAuthService.validateOtp(dto.getTransactionID(), dto.getIndividualId(), dto.getOtp())) {

				    credentialReqestDto=prepareCredentialRequest(dto);
					requestDto.setId("mosip.credential.request.service.id");
					requestDto.setRequest(credentialReqestDto);
					requestDto.setRequesttime(DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime()));
					requestDto.setVersion("1.0");
					parResponseDto = residentServiceRestClient.getApi(partnerUri, ResponseWrapper.class);
					partnerResponseDto = JsonUtil.readValue(JsonUtil.writeValueAsString(parResponseDto.getResponse()),
							PartnerResponseDto.class);
					additionalAttributes.put("partnerName",
							partnerResponseDto.getOrganizationName());
					additionalAttributes.put("encryptionKey", credentialReqestDto.getEncryptionKey());
					additionalAttributes.put("credentialName", credentialReqestDto.getCredentialType());

					ResponseWrapper<ResidentCredentialResponseDto> responseDto = residentServiceRestClient.postApi(
							env.getProperty(ApiName.CREDENTIAL_REQ_URL.name()), MediaType.APPLICATION_JSON, requestDto,
							ResponseWrapper.class);
					residentCredentialResponseDto = JsonUtil.readValue(
							JsonUtil.writeValueAsString(responseDto.getResponse()),
							ResidentCredentialResponseDto.class);
					additionalAttributes.put("RID", residentCredentialResponseDto.getRequestId());
					sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_CRE_REQ_SUCCESS,
							additionalAttributes);

				} else {
					logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
							LoggerFileConstant.APPLICATIONID.toString(),
							ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
					sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_CRE_REQ_FAILURE,
							additionalAttributes);
					audit.setAuditRequestDto(EventEnum.CREDENTIAL_REQ_EXCEPTION);
					throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
							ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
				}

			} catch (OtpValidationFailedException e) {
				audit.setAuditRequestDto(EventEnum.CREDENTIAL_REQ_EXCEPTION);
				trySendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_CRE_REQ_FAILURE,
						additionalAttributes);
				throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
						e.getErrorText(), e);
			}

		catch (ResidentServiceCheckedException e) {

			sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_CRE_REQ_FAILURE, additionalAttributes);
			audit.setAuditRequestDto(EventEnum.CREDENTIAL_REQ_EXCEPTION);
			throw new ResidentCredentialServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		catch (ApisResourceAccessException e) {
			sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_CRE_REQ_FAILURE, additionalAttributes);
			audit.setAuditRequestDto(EventEnum.CREDENTIAL_REQ_EXCEPTION);
			throw new ResidentCredentialServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		catch (IOException e) {
			sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_CRE_REQ_FAILURE, additionalAttributes);
			audit.setAuditRequestDto(EventEnum.CREDENTIAL_REQ_EXCEPTION);
			throw new ResidentCredentialServiceException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		}

		return residentCredentialResponseDto;
	}
	
	@Override
	public byte[] getCard(String requestId) throws Exception {
		// TODO Auto-generated method stub
		ResponseWrapper<CredentialRequestStatusDto> responseDto = null;
		CredentialRequestStatusDto credentialRequestStatusResponseDto = new CredentialRequestStatusDto();
		try {
			UUID requestUUID = UUID.fromString(requestId);
			String credentialUrl = env.getProperty(ApiName.CREDENTIAL_STATUS_URL.name()) + requestUUID;
			URI credentailStatusUri = URI.create(credentialUrl);
			responseDto = residentServiceRestClient.getApi(credentailStatusUri, ResponseWrapper.class);
			credentialRequestStatusResponseDto = JsonUtil.readValue(
					JsonUtil.writeValueAsString(responseDto.getResponse()), CredentialRequestStatusDto.class);
			URI dataShareUri = URI.create(credentialRequestStatusResponseDto.getUrl());
			String encryptedData = residentServiceRestClient.getApi(dataShareUri, String.class);
			RequestWrapper<CryptomanagerRequestDto> request = new RequestWrapper<>();
			CryptomanagerRequestDto cryptomanagerRequestDto = new CryptomanagerRequestDto();
			cryptomanagerRequestDto.setApplicationId(applicationId);
			cryptomanagerRequestDto.setData(encryptedData);
			cryptomanagerRequestDto.setReferenceId(partnerReferenceId);
			cryptomanagerRequestDto.setPrependThumbprint(isPrependThumbprintEnabled);
			LocalDateTime localdatetime = DateUtils.getUTCCurrentDateTime();
			request.setRequesttime(DateUtils.formatToISOString(localdatetime));
			cryptomanagerRequestDto.setTimeStamp(localdatetime);
			request.setRequest(cryptomanagerRequestDto);
			String response = residentServiceRestClient.postApi(
					env.getProperty(ApiName.DECRYPT_API_URL.name()), MediaType.APPLICATION_JSON, request,
					String.class);
			CryptomanagerResponseDto responseObject = mapper.readValue(response, CryptomanagerResponseDto.class);
			return CryptoUtil.decodeURLSafeBase64(responseObject.getResponse().getData());
		} catch (ApisResourceAccessException e) {
			audit.setAuditRequestDto(EventEnum.REQ_CARD_EXCEPTION);
			throw new ResidentCredentialServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		} catch (IllegalArgumentException e) {
			audit.setAuditRequestDto(EventEnum.REQ_CARD_EXCEPTION);
			throw new ResidentCredentialServiceException(ResidentErrorCode.INVALID_ID.getErrorCode(),
					ResidentErrorCode.INVALID_ID.getErrorMessage(), e);
		} catch (IOException e) {
			audit.setAuditRequestDto(EventEnum.REQ_CARD_EXCEPTION);
			throw new ResidentCredentialServiceException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		}

	}

	@Override
	public CredentialRequestStatusResponseDto getStatus(String requestId) {
		ResponseWrapper<CredentialRequestStatusDto> responseDto = null;
		CredentialRequestStatusDto credentialRequestStatusDto = new CredentialRequestStatusDto();
		Map<String, Object> additionalAttributes = new HashedMap();
		CredentialRequestStatusResponseDto credentialRequestStatusResponseDto=new CredentialRequestStatusResponseDto();
		try {
			UUID requestUUID = UUID.fromString(requestId);
			String credentialUrl = env.getProperty(ApiName.CREDENTIAL_STATUS_URL.name()) + requestUUID;
			URI credentailStatusUri = URI.create(credentialUrl);
			responseDto =residentServiceRestClient.getApi(credentailStatusUri, ResponseWrapper.class);
			credentialRequestStatusDto = JsonUtil
						.readValue(JsonUtil.writeValueAsString(responseDto.getResponse()), CredentialRequestStatusDto.class);
			credentialRequestStatusResponseDto.setId(credentialRequestStatusDto.getId());
			credentialRequestStatusResponseDto.setRequestId(credentialRequestStatusDto.getRequestId());
			credentialRequestStatusResponseDto.setStatusCode(credentialRequestStatusDto.getStatusCode());
			additionalAttributes.put("RID", credentialRequestStatusResponseDto.getRequestId());
			additionalAttributes.put("status", credentialRequestStatusResponseDto.getStatusCode());
			sendNotification(credentialRequestStatusResponseDto.getId(), NotificationTemplateCode.RS_CRE_STATUS,
					additionalAttributes);

		} catch (ApisResourceAccessException e) {
			audit.setAuditRequestDto(EventEnum.CREDENTIAL_REQ_STATUS_EXCEPTION);
			throw new ResidentCredentialServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		catch(IOException e) {
			audit.setAuditRequestDto(EventEnum.CREDENTIAL_REQ_STATUS_EXCEPTION);
			throw new ResidentCredentialServiceException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		} 
		catch (IllegalArgumentException e) {
			audit.setAuditRequestDto(EventEnum.CREDENTIAL_REQ_STATUS_EXCEPTION);
			throw new ResidentCredentialServiceException(ResidentErrorCode.INVALID_ID.getErrorCode(),
					ResidentErrorCode.INVALID_ID.getErrorMessage(), e);
		} 
		catch (ResidentServiceCheckedException e) {
			audit.setAuditRequestDto(EventEnum.CREDENTIAL_REQ_STATUS_EXCEPTION);
			throw new ResidentCredentialServiceException(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorMessage(), e);
		}
		return credentialRequestStatusResponseDto;
	}

	public CredentialReqestDto prepareCredentialRequest(ResidentCredentialRequestDto residentCreDto) {
		CredentialReqestDto crDto=new CredentialReqestDto();
		crDto.setAdditionalData(residentCreDto.getAdditionalData());
		crDto.setCredentialType(residentCreDto.getCredentialType());
		crDto.setEncrypt(residentCreDto.isEncrypt());
		crDto.setId(residentCreDto.getIndividualId());
		crDto.setRecepiant(residentCreDto.getRecepiant());
		crDto.setSharableAttributes(residentCreDto.getSharableAttributes());
		crDto.setUser(residentCreDto.getUser());
		crDto.setIssuer(residentCreDto.getIssuer());
		if (residentCreDto.getEncryptionKey().isEmpty()) {
			crDto.setEncryptionKey(generatePin());
		} else {
			crDto.setEncryptionKey(residentCreDto.getEncryptionKey());
		}
		return crDto;
		
	}


	@Override
	public CredentialCancelRequestResponseDto cancelCredentialRequest(String requestId) {
		ResponseWrapper<CredentialCancelRequestResponseDto> response = new ResponseWrapper<CredentialCancelRequestResponseDto>();
		Map<String, Object> additionalAttributes = new HashedMap();
		CredentialCancelRequestResponseDto credentialCancelRequestResponseDto=new CredentialCancelRequestResponseDto();
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
				sendNotification(credentialCancelRequestResponseDto.getId(),
						NotificationTemplateCode.RS_CRE_CANCEL_SUCCESS, additionalAttributes);

		} catch (ApisResourceAccessException e) {
			audit.setAuditRequestDto(EventEnum.CREDENTIAL_CANCEL_REQ_EXCEPTION);
			throw new ResidentCredentialServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		catch (IllegalArgumentException e) {
			audit.setAuditRequestDto(EventEnum.CREDENTIAL_REQ_STATUS_EXCEPTION);
			throw new ResidentCredentialServiceException(ResidentErrorCode.INVALID_ID.getErrorCode(),
					ResidentErrorCode.INVALID_ID.getErrorMessage(), e);
		} 
		catch (IOException e) {
			audit.setAuditRequestDto(EventEnum.CREDENTIAL_CANCEL_REQ_EXCEPTION);
			throw new ResidentCredentialServiceException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		} catch (ResidentServiceCheckedException e) {
			audit.setAuditRequestDto(EventEnum.CREDENTIAL_CANCEL_REQ_EXCEPTION);
			throw new ResidentCredentialServiceException(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorMessage());
		}


		return credentialCancelRequestResponseDto;
	}

	@Override
	public CredentialTypeResponse getCredentialTypes() {
		CredentialTypeResponse credentialTypeResponse=new CredentialTypeResponse();
		URI credentailTypesUri = URI.create(env.getProperty(ApiName.CREDENTIAL_TYPES_URL.name()));
		try {
			credentialTypeResponse=residentServiceRestClient.getApi(credentailTypesUri, CredentialTypeResponse.class);
		} catch (ApisResourceAccessException  e) {
			throw new ResidentCredentialServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		return credentialTypeResponse;
	}

	@Override
	public ResponseWrapper<PartnerCredentialTypePolicyDto> getPolicyByCredentialType(String partnerId,
			String credentialType)
	{
		ResponseWrapper<PartnerCredentialTypePolicyDto> response = new ResponseWrapper<PartnerCredentialTypePolicyDto>();
		Map<String, String> pathsegments = new HashMap<>();
		pathsegments.put("partnerId", partnerId);
		pathsegments.put("credentialType", credentialType);
		try {
			response = residentServiceRestClient.getApi(ApiName.POLICY_REQ_URL, pathsegments,
					ResponseWrapper.class);
		} catch (ApisResourceAccessException e) {
			audit.setAuditRequestDto(EventEnum.REQ_POLICY_EXCEPTION);
			throw new ResidentCredentialServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		return response;
	}

	@Override
	public byte[] getRIDDigitalCardV2(String rid)  {
		try {
			DigitalCardStatusResponseDto digitalCardStatusResponseDto = getDigitalCardStatus(rid);
			if(!digitalCardStatusResponseDto.getStatusCode().equals(AVAILABLE)) {
				audit.setAuditRequestDto(EventEnum.RID_DIGITAL_CARD_REQ_EXCEPTION);
				throw new ResidentServiceException(
						ResidentErrorCode.DIGITAL_CARD_RID_NOT_FOUND.getErrorCode(),
						ResidentErrorCode.DIGITAL_CARD_RID_NOT_FOUND.getErrorMessage());
			}
			URI dataShareUri = URI.create(digitalCardStatusResponseDto.getUrl());
			String data = residentServiceRestClient.getApi(dataShareUri, String.class);
			return CryptoUtil.decodeURLSafeBase64(data);
		} catch (ApisResourceAccessException e) {
			audit.setAuditRequestDto(EventEnum.RID_DIGITAL_CARD_REQ_EXCEPTION);
			throw new ResidentCredentialServiceException(
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}catch (IOException e) {
			audit.setAuditRequestDto(EventEnum.RID_DIGITAL_CARD_REQ_EXCEPTION);
			throw new ResidentCredentialServiceException(
					ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		}

	}

	private DigitalCardStatusResponseDto getDigitalCardStatus(String individualId)
			throws ApisResourceAccessException, IOException {

		Map<String, String> pathSegments = new HashMap<>();
		pathSegments.put("rid", individualId);
		ResponseWrapper<DigitalCardStatusResponseDto> responseDto =
				residentServiceRestClient.getApi(ApiName.DIGITAL_CARD_STATUS_URL, pathSegments, ResponseWrapper.class);
		DigitalCardStatusResponseDto digitalCardStatusResponseDto = JsonUtil.readValue(
				JsonUtil.writeValueAsString(responseDto.getResponse()), DigitalCardStatusResponseDto.class);
		return digitalCardStatusResponseDto;
	}

	public String generatePin() {
		if (random == null)
			instantiate();
		int randomInteger = random.nextInt(max - min) + min;
		return String.valueOf(randomInteger);
	}


	@Scheduled(fixedDelayString = "${mosip.resident.pingeneration.refresh.millisecs:1800000}",
			initialDelayString = "${mosip.resident.pingeneration.refresh.delay-on-startup.millisecs:5000}")
	public void instantiate() {
		logger.debug("Instantiating SecureRandom for credential pin generation............");
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			logger.error("Could not instantiate SecureRandom for pin generation", e);
		}
	} 
	
	private NotificationResponseDTO trySendNotification(String id,
			NotificationTemplateCode templateTypeCode, Map<String, Object> additionalAttributes)
			throws ResidentServiceCheckedException {
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

	private NotificationResponseDTO sendNotification(String id,
			NotificationTemplateCode templateTypeCode, Map<String, Object> additionalAttributes)
			throws ResidentServiceCheckedException {
		NotificationRequestDto notificationRequest = new NotificationRequestDto(id, templateTypeCode,
				additionalAttributes);
		return notificationService.sendNotification(notificationRequest);
	}
	/*
	 * private PartnerCredentialTypePolicyResponseDto policyMapper(
	 * PartnerCredentialTypePolicyDto partnerCredentialTypePolicyDto) {
	 * PartnerCredentialTypePolicyResponseDto policy = new
	 * PartnerCredentialTypePolicyResponseDto();
	 * policy.setCr_by(partnerCredentialTypePolicyDto.getCr_by());
	 * policy.setCr_dtimes(partnerCredentialTypePolicyDto.getCr_dtimes());
	 * policy.setCredentialType(partnerCredentialTypePolicyDto.getCredentialType());
	 * policy.setIs_Active(partnerCredentialTypePolicyDto.getIs_Active());
	 * policy.setPartnerId(partnerCredentialTypePolicyDto.getPartnerId());
	 * policy.setPolicyDesc(partnerCredentialTypePolicyDto.getPolicyDesc());
	 * policy.setPolicyId(policyId); policy.setPolicyName(policyName);
	 * policy.setPolicyType(policyType); policy.setPublishDate(publishDate);
	 * policy.setSchema(schema); policy.setStatus(status); policy.setUp_by(up_by);
	 * policy.setUpd_dtimes(upd_dtimes); policy.setVersion(version);
	 * policy.setValidTill(validTill);
	 *
	 * }
	 */
}