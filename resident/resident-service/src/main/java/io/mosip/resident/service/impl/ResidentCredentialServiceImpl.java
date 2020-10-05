package io.mosip.resident.service.impl;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.NotificationTemplateCode;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.CredentialCancelRequestResponseDto;
import io.mosip.resident.dto.CredentialReqestDto;
import io.mosip.resident.dto.CredentialRequestStatusResponseDto;
import io.mosip.resident.dto.CredentialTypeResponse;
import io.mosip.resident.dto.NotificationRequestDto;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.dto.PartnerResponseDto;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentCredentialRequestDto;
import io.mosip.resident.dto.ResidentCredentialResponseDto;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentCredentialServiceException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ResidentCredentialService;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TokenGenerator;

@Service
public class ResidentCredentialServiceImpl implements ResidentCredentialService {

	@Autowired
	IdAuthService idAuthService;
	
	@Value("${CREDENTIAL_REQ_URL}")
	private String credentailReqUrl;
	
	@Value("${CREDENTIAL_STATUS_URL}")
	private String credentailStatusUrl;
	
	@Value("${CREDENTIAL_TYPES_URL}")
	private String credentailTypesUrl;
	
	@Value("${CREDENTIAL_CANCELREQ_URL}")
	private String credentailCancelReqUrl;
	
	@Value("${PARTNER_API_URL}")
	private String partnerReqUrl;

	private static final Logger logger = LoggerConfiguration.logConfig(ResidentCredentialServiceImpl.class);
	
	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;
	
	@Autowired
	private TokenGenerator tokenGenerator;
	

	@Autowired
	NotificationService notificationService;
	
	@Override
	public ResidentCredentialResponseDto reqCredential(ResidentCredentialRequestDto dto)
			throws ResidentServiceCheckedException {
		ResidentCredentialResponseDto residentCredentialResponseDto=new ResidentCredentialResponseDto();
		RequestWrapper<CredentialReqestDto> requestDto = new RequestWrapper<>();
		ResponseWrapper<PartnerResponseDto> parResponseDto = new ResponseWrapper<PartnerResponseDto>();
		PartnerResponseDto partnerResponseDto = new PartnerResponseDto();
		CredentialReqestDto credentialReqestDto=new CredentialReqestDto();
		Map<String, Object> additionalAttributes = new HashedMap();
		String partnerUrl = partnerReqUrl + dto.getIssuer();
		URI partnerUri = URI.create(partnerUrl);
		try {
			if (idAuthService.validateOtp(dto.getTransactionID(), dto.getIndividualId(), "UIN", dto.getOtp())) {

				    credentialReqestDto=prepareCredentialRequest(dto);
					requestDto.setId("mosip.credential.request.service.id");
					requestDto.setRequest(credentialReqestDto);
					requestDto.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
					requestDto.setVersion("1.0");
					parResponseDto = residentServiceRestClient.getApi(partnerUri, ResponseWrapper.class,
							tokenGenerator.getToken());
					partnerResponseDto = JsonUtil.readValue(JsonUtil.writeValueAsString(parResponseDto.getResponse()),
							PartnerResponseDto.class);
					additionalAttributes.put("partnerName",
							partnerResponseDto.getOrganizationName());
					additionalAttributes.put("encryptionKey", credentialReqestDto.getEncryptionKey());
					ResponseWrapper<ResidentCredentialResponseDto> responseDto = residentServiceRestClient.postApi(
							credentailReqUrl, MediaType.APPLICATION_JSON, requestDto, ResponseWrapper.class,
							tokenGenerator.getToken());
					residentCredentialResponseDto = JsonUtil.readValue(
							JsonUtil.writeValueAsString(responseDto.getResponse()),
							ResidentCredentialResponseDto.class);
					sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_CRE_REQ_SUCCESS,
							additionalAttributes);
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
			sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_CRE_REQ_FAILURE, additionalAttributes);
			throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
					e.getErrorText(), e);
		}
		catch (ResidentServiceCheckedException e) {

			sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_CRE_REQ_FAILURE, additionalAttributes);
			throw new ResidentCredentialServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		catch (ApisResourceAccessException e) {
			sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_CRE_REQ_FAILURE, additionalAttributes);
			throw new ResidentCredentialServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		catch (IOException e) {
			sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_CRE_REQ_FAILURE, additionalAttributes);
			throw new ResidentCredentialServiceException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		}

		return residentCredentialResponseDto;
	}
	
	@Override
	public CredentialRequestStatusResponseDto getStatus(String requestId) {
		ResponseWrapper<CredentialRequestStatusResponseDto> responseDto = null;
		Map<String, Object> additionalAttributes = new HashedMap();
		CredentialRequestStatusResponseDto credentialRequestStatusResponseDto=new CredentialRequestStatusResponseDto();
		try {
			String credentialUrl=credentailStatusUrl+requestId;
			URI credentailStatusUri = URI.create(credentialUrl);
			responseDto =residentServiceRestClient.getApi(credentailStatusUri, ResponseWrapper.class, tokenGenerator.getToken());
			credentialRequestStatusResponseDto = JsonUtil
						.readValue(JsonUtil.writeValueAsString(responseDto.getResponse()), CredentialRequestStatusResponseDto.class);
			additionalAttributes.put("requestId", credentialRequestStatusResponseDto.getRequestId());
			additionalAttributes.put("statusCode", credentialRequestStatusResponseDto.getStatusCode());
			sendNotification(credentialRequestStatusResponseDto.getId(), NotificationTemplateCode.RS_CRE_STATUS,
					additionalAttributes);
		} catch (ApisResourceAccessException e) {
			throw new ResidentCredentialServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		catch(IOException e) {
			throw new ResidentCredentialServiceException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		} catch (ResidentServiceCheckedException e) {
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
	public CredentialCancelRequestResponseDto getCancelCredentialRequest(String requestId) {
		ResponseWrapper<CredentialCancelRequestResponseDto> responseDto = null;
		Map<String, Object> additionalAttributes = new HashedMap();
		CredentialCancelRequestResponseDto credentialCancelRequestResponseDto=new CredentialCancelRequestResponseDto();
		boolean flag=true;
		try {
			/*if (idAuthService.validateOtp(dto.getTransactionID(), dto.getIndividualId(),
					"UIN", dto.getOtp())) {*/
			if(flag) {
				
					String credentialReqCancelUrl=credentailCancelReqUrl+requestId;
					URI credentailReqCancelUri = URI.create(credentialReqCancelUrl);
					responseDto =residentServiceRestClient.getApi(credentailReqCancelUri, ResponseWrapper.class, tokenGenerator.getToken());
					credentialCancelRequestResponseDto = JsonUtil
								.readValue(JsonUtil.writeValueAsString(responseDto.getResponse()), CredentialCancelRequestResponseDto.class);
					additionalAttributes.put("requestId", credentialCancelRequestResponseDto.getRequestId());
					sendNotification(credentialCancelRequestResponseDto.getId(),
							NotificationTemplateCode.RS_CRE_CANCEL_SUCCESS, additionalAttributes);
			   } else {
				logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
						LoggerFileConstant.APPLICATIONID.toString(),
						ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());

				throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
						ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
			}
		} /*
			 * catch (OtpValidationFailedException e) { throw new
			 * ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode
			 * (), e.getErrorText(), e); }
			 */
		catch (ApisResourceAccessException e) {
			throw new ResidentCredentialServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		catch (IOException e) {
			throw new ResidentCredentialServiceException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		} catch (ResidentServiceCheckedException e) {

			throw new ResidentCredentialServiceException(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorMessage());
		}

		return credentialCancelRequestResponseDto;
	}

	@Override
	public CredentialTypeResponse getCredentialTypes() {
		CredentialTypeResponse credentialTypeResponse=new CredentialTypeResponse();
		URI credentailTypesUri = URI.create(credentailTypesUrl);
		try {
			credentialTypeResponse=residentServiceRestClient.getApi(credentailTypesUri, CredentialTypeResponse.class, tokenGenerator.getToken());
		} catch (ApisResourceAccessException  e) {
			throw new ResidentCredentialServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		 catch (IOException e) {
				throw new ResidentCredentialServiceException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
						ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
			}
		return credentialTypeResponse;
	}
	public String generatePin() {
		return RandomStringUtils.randomNumeric(6);
	}


	private NotificationResponseDTO sendNotification(String id,
			NotificationTemplateCode templateTypeCode, Map<String, Object> additionalAttributes)
			throws ResidentServiceCheckedException {
		NotificationRequestDto notificationRequest = new NotificationRequestDto(id, templateTypeCode,
				additionalAttributes);
		return notificationService.sendNotification(notificationRequest);
	}
}