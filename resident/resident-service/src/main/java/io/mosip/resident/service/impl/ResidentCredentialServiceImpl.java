package io.mosip.resident.service.impl;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.NotificationTemplateCode;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.CredentialCancelRequestResponseDto;
import io.mosip.resident.dto.CredentialReqestDto;
import io.mosip.resident.dto.CredentialRequestStatusResponseDto;
import io.mosip.resident.dto.CredentialTypeResponse;
import io.mosip.resident.dto.NotificationRequestDto;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.dto.RegProcCommonResponseDto;
import io.mosip.resident.dto.RegProcRePrintRequestDto;
import io.mosip.resident.dto.RegProcUpdateRequestDTO;
import io.mosip.resident.dto.RegistrationStatusResponseDTO;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentCredentialRequestDto;
import io.mosip.resident.dto.ResidentCredentialResponseDto;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
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
	
	private static final Logger logger = LoggerConfiguration.logConfig(ResidentCredentialServiceImpl.class);
	
	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;
	
	@Autowired
	private TokenGenerator tokenGenerator;
	

	@Autowired
	NotificationService notificationService;
	
	@Override
	public ResidentCredentialResponseDto reqCredential(ResidentCredentialRequestDto dto) throws ResidentServiceCheckedException {
		ResidentCredentialResponseDto residentCredentialResponseDto=new ResidentCredentialResponseDto();
		RequestWrapper<CredentialReqestDto> requestDto = new RequestWrapper<>();
		CredentialReqestDto credentialReqestDto=new CredentialReqestDto();
		boolean flag=true;
		try {
			/*if (idAuthService.validateOtp(dto.getTransactionID(), dto.getIndividualId(),
					"UIN", dto.getOtp())) {*/
			if(flag) {
				    credentialReqestDto=prepareCredentialRequest(dto);
					System.out.println(">>>tu>>>>>>>>>>>"+credentialReqestDto);
					requestDto.setId("mosip.credential.request.service.id");
					requestDto.setRequest(credentialReqestDto);
					requestDto.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
					requestDto.setVersion("1.0");
					ResponseWrapper<ResidentCredentialResponseDto> responseDto=residentServiceRestClient.postApi(credentailReqUrl, MediaType.APPLICATION_JSON, requestDto,
							ResponseWrapper.class, tokenGenerator.getToken());
					residentCredentialResponseDto = JsonUtil
							.readValue(JsonUtil.writeValueAsString(responseDto.getResponse()), ResidentCredentialResponseDto.class);
				
						//sendNotification(dto.getIndividualId(), IdType.valueOf("UIN"),
							//	NotificationTemplateCode.RS_CREDENTIAL_SUCCESS, null);
			   } else {
				logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
						LoggerFileConstant.APPLICATIONID.toString(),
						ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
			//	sendNotification(dto.getIndividualId(), IdType.valueOf("UIN"),
				//		NotificationTemplateCode.RS_CREDENTIAL_FAILURE, null);
				//throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
					//	ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
			}
		}/* catch (OtpValidationFailedException e) {
			sendNotification(dto.getIndividualId(), IdType.valueOf("UIN"),
					NotificationTemplateCode.RS_CREDENTIAL_FAILURE, null);
			throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
					e.getErrorText(), e);
		}*/
	/*	catch (ResidentServiceCheckedException e) {	
			//sendNotification(dto.getIndividualId(), IdType.valueOf("UIN"),
					//NotificationTemplateCode.RS_CREDENTIAL_FAILURE, null);
			throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}*/
		catch (ApisResourceAccessException e) {
		//	sendNotification(dto.getIndividualId(), IdType.valueOf("UIN"),
			//		NotificationTemplateCode.RS_CREDENTIAL_FAILURE, null);
			throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		catch (IOException e) {
			//sendNotification(dto.getIndividualId(), IdType.valueOf("RID"),
				//	NotificationTemplateCode.RS_CREDENTIAL_FAILURE, null);
			throw new ResidentServiceException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		}

		return residentCredentialResponseDto;
	}
	
	@Override
	public CredentialRequestStatusResponseDto getStatus(String requestId)
			throws ResidentServiceCheckedException {
		ResponseWrapper<CredentialRequestStatusResponseDto> responseDto = null;
		CredentialRequestStatusResponseDto credentialRequestStatusResponseDto=new CredentialRequestStatusResponseDto();
		try {
			String credentialUrl=credentailStatusUrl+requestId;
			URI credentailStatusUri = URI.create(credentialUrl);
			responseDto =residentServiceRestClient.getApi(credentailStatusUri, ResponseWrapper.class, tokenGenerator.getToken());
			credentialRequestStatusResponseDto = JsonUtil
						.readValue(JsonUtil.writeValueAsString(responseDto.getResponse()), CredentialRequestStatusResponseDto.class);
		} catch (ApisResourceAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return credentialRequestStatusResponseDto;
	}

	public CredentialReqestDto prepareCredentialRequest(ResidentCredentialRequestDto residentCreDto) {
		CredentialReqestDto crDto=new CredentialReqestDto();
		crDto.setAdditionalData(residentCreDto.getAdditionalData());
		crDto.setCredentialType(residentCreDto.getCredentialType());
		crDto.setEncrypt(residentCreDto.isEncrypt());
		crDto.setEncryptionKey(residentCreDto.getEncryptionKey());
		crDto.setId(residentCreDto.getIndividualId());
		crDto.setRecepiant(residentCreDto.getRecepiant());
		crDto.setSharableAttributes(residentCreDto.getSharableAttributes());
		crDto.setUser(residentCreDto.getUser());
		return crDto;
		
	}

	/*
	
	@Override
	public CredentialCancelRequestResponseDto getCancelCredentialRequest(String requestId) {
	
		ResponseWrapper<CredentialCancelRequestResponseDto> responseDto = null;
		CredentialCancelRequestResponseDto credentialCancelRequestResponseDto=new CredentialCancelRequestResponseDto();
		try {
			String credentialReqCancelUrl=credentailCancelReqUrl+requestId;
			URI credentailReqCancelUri = URI.create(credentialReqCancelUrl);
			responseDto =residentServiceRestClient.getApi(credentailReqCancelUri, ResponseWrapper.class, tokenGenerator.getToken());
			System.out.println(".>>>>>>>>"+responseDto);
			credentialCancelRequestResponseDto = JsonUtil
						.readValue(JsonUtil.writeValueAsString(responseDto.getResponse()), CredentialCancelRequestResponseDto.class);
		} catch (ApisResourceAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return credentialCancelRequestResponseDto;
		
	}*/

	@Override
	public CredentialCancelRequestResponseDto getCancelCredentialRequest(String requestId) {
		ResponseWrapper<CredentialCancelRequestResponseDto> responseDto = null;
		CredentialCancelRequestResponseDto credentialCancelRequestResponseDto=new CredentialCancelRequestResponseDto();
		boolean flag=true;
		try {
			/*if (idAuthService.validateOtp(dto.getTransactionID(), dto.getIndividualId(),
					"UIN", dto.getOtp())) {*/
			if(flag) {
				
					String credentialReqCancelUrl=credentailCancelReqUrl+requestId;
					URI credentailReqCancelUri = URI.create(credentialReqCancelUrl);
					System.out.println(">>>>>>>>>>>>>>>>()"+credentailReqCancelUri);
					responseDto =residentServiceRestClient.getApi(credentailReqCancelUri, ResponseWrapper.class, tokenGenerator.getToken());
					System.out.println(".>>>>>>>>"+responseDto);
					credentialCancelRequestResponseDto = JsonUtil
								.readValue(JsonUtil.writeValueAsString(responseDto.getResponse()), CredentialCancelRequestResponseDto.class);
								//sendNotification(dto.getIndividualId(), IdType.valueOf("UIN"),
							//	NotificationTemplateCode.RS_CREDENTIAL_SUCCESS, null);
			   } else {
				logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
						LoggerFileConstant.APPLICATIONID.toString(),
						ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
				//sendNotification(dto.getIndividualId(), IdType.valueOf("UIN"),
					//	NotificationTemplateCode.RS_CREDENTIAL_FAILURE, null);
				//throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
					//	ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
			}
		}/* catch (OtpValidationFailedException e) {
			sendNotification(dto.getIndividualId(), IdType.valueOf("UIN"),
					NotificationTemplateCode.RS_CREDENTIAL_FAILURE, null);
			throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
					e.getErrorText(), e);
		}*/
		catch (ApisResourceAccessException e) {
			//sendNotification(dto.getIndividualId(), IdType.valueOf("UIN"),
					//NotificationTemplateCode.RS_CREDENTIAL_FAILURE, null);
			throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		catch (IOException e) {
			//sendNotification(dto.getIndividualId(), IdType.valueOf("RID"),
				//	NotificationTemplateCode.RS_CREDENTIAL_FAILURE, null);
			throw new ResidentServiceException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		}

		return credentialCancelRequestResponseDto;
	}

	@Override
	public CredentialTypeResponse getCredentialTypes() {
		CredentialTypeResponse credentialTypeResponse=new CredentialTypeResponse();
		URI credentailTypesUri = URI.create(credentailTypesUrl);
		System.out.println("???????"+credentailTypesUri);
		try {
			credentialTypeResponse=residentServiceRestClient.getApi(credentailTypesUri, CredentialTypeResponse.class, tokenGenerator.getToken());
		} catch (ApisResourceAccessException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 catch (IOException e) {
				e.printStackTrace();
			}
		return credentialTypeResponse;
	}


	private NotificationResponseDTO sendNotification(String id, IdType idType,
			NotificationTemplateCode templateTypeCode, Map<String, Object> additionalAttributes)
			throws ResidentServiceCheckedException {
		NotificationRequestDto notificationRequest = new NotificationRequestDto(id, idType, templateTypeCode,
				additionalAttributes);
		return notificationService.sendNotification(notificationRequest);
	}
}