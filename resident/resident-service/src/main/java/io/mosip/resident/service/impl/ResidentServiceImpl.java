package io.mosip.resident.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.NotificationTemplateCode;
import io.mosip.resident.constant.RegistrationExternalStatusCode;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.AuthHistoryRequestDTO;
import io.mosip.resident.dto.AuthHistoryResponseDTO;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDto;
import io.mosip.resident.dto.AuthTxnDetailsDTO;
import io.mosip.resident.dto.EuinRequestDTO;
import io.mosip.resident.dto.NotificationRequestDto;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.dto.PacketGeneratorResDto;
import io.mosip.resident.dto.RegProcRePrintRequestDto;
import io.mosip.resident.dto.RegStatusCheckResponseDTO;
import io.mosip.resident.dto.RegistrationStatusRequestDTO;
import io.mosip.resident.dto.RegistrationStatusResponseDTO;
import io.mosip.resident.dto.RegistrationStatusSubRequestDto;
import io.mosip.resident.dto.RegistrationType;
import io.mosip.resident.dto.RequestDTO;
import io.mosip.resident.dto.ResidentDocuments;
import io.mosip.resident.dto.ResidentIndividialIDType;
import io.mosip.resident.dto.ResidentReprintRequestDto;
import io.mosip.resident.dto.ResidentReprintResponseDto;
import io.mosip.resident.dto.ResidentUpdateDto;
import io.mosip.resident.dto.ResidentUpdateRequestDto;
import io.mosip.resident.dto.ResidentUpdateResponseDTO;
import io.mosip.resident.dto.ResponseDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.RIDInvalidException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.handler.service.ResidentUpdateService;
import io.mosip.resident.handler.service.UinCardRePrintService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ResidentService;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TokenGenerator;
import io.mosip.resident.util.UINCardDownloadService;
import io.mosip.resident.util.Utilitiy;

@Service
public class ResidentServiceImpl implements ResidentService {

	private static final String DATETIME_PATTERN = "mosip.utc-datetime-pattern";
	private static final String STATUS_CHECK_ID = "mosip.resident.service.status.check.id";
	private static final String STATUS_CHECEK_VERSION = "mosip.resident.service.status.check.version";
	private static final String REJECTED_MESSAGE = "REJECTED - PLEASE VISIT THE NEAREST CENTER FOR DETAILS.";
	private static final String REREGISTER_MESSAGE = "FAILED - PLEASE VISIT THE NEAREST CENTER FOR DETAILS.";
	private static final String RESEND_MESSAGE = "UNDER PROCESSING - PLEASE CHECK BACK AGAIN LATER.";
	private static final String PROCESSING_MESSAGE = "UNDER PROCESSING - PLEASE CHECK BACK AGAIN LATER.";
	private static final String PROOF_OF_ADDRESS = "poa";
	private static final String PROOF_OF_DOB = "pob";
	private static final String PROOF_OF_RELATIONSHIP = "por";
	private static final String PROOF_OF_IDENTITY = "poi";
	private static final String IDENTITY = "identity";
	private static final String VALUE = "value";

	private static final Logger logger = LoggerConfiguration.logConfig(ResidentServiceImpl.class);

	@Autowired
	private UINCardDownloadService uinCardDownloadService;

	@Autowired
	private ResidentUpdateService residentUpdateService;

	@Autowired
	private IdAuthService idAuthService;

	@Autowired
	NotificationService notificationService;

	@Autowired
	private TokenGenerator tokenGenerator;

	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;

	@Autowired
	private UinCardRePrintService rePrintService;

	@Autowired
	Environment env;

	@Autowired
	private Utilitiy utility;

	@Value("${resident.center.id}")
	private String centerId;

	@Value("${resident.machine.id}")
	private String machineId;

	@Override
	public RegStatusCheckResponseDTO getRidStatus(RequestDTO request) {
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(), "ResidentServiceImpl::getRidStatus():: entry");

		RegStatusCheckResponseDTO response = null;
		RegistrationStatusResponseDTO responseWrapper = null;
		RegistrationStatusRequestDTO dto = new RegistrationStatusRequestDTO();
		List<RegistrationStatusSubRequestDto> rids = new ArrayList<>();
		RegistrationStatusSubRequestDto rid = new RegistrationStatusSubRequestDto(request.getIndividualId());

		rids.add(rid);
		dto.setRequest(rids);
		dto.setId(env.getProperty(STATUS_CHECK_ID));
		dto.setVersion(env.getProperty(STATUS_CHECEK_VERSION));
		dto.setRequesttime(DateUtils.getUTCCurrentDateTimeString(env.getProperty(DATETIME_PATTERN)));

		try {
			responseWrapper = (RegistrationStatusResponseDTO) residentServiceRestClient.postApi(
					env.getProperty(ApiName.REGISTRATIONSTATUSSEARCH.name()), MediaType.APPLICATION_JSON, dto,
					RegistrationStatusResponseDTO.class, tokenGenerator.getToken());
			if (responseWrapper == null) {
				logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
						LoggerFileConstant.APPLICATIONID.toString(), "In valid response from Registration status API");
				throw new RIDInvalidException(ResidentErrorCode.INVALID_API_RESPONSE.getErrorCode(),
						ResidentErrorCode.INVALID_API_RESPONSE.getErrorMessage()
								+ ApiName.REGISTRATIONSTATUSSEARCH.name());
			}

			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
						LoggerFileConstant.APPLICATIONID.toString(), responseWrapper.getErrors().get(0).toString());
				throw new RIDInvalidException(ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorCode(),
						ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorMessage()
								+ responseWrapper.getErrors().get(0).toString());
			}
			if ((responseWrapper.getResponse() == null || responseWrapper.getResponse().isEmpty()) ) {
				logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
						LoggerFileConstant.APPLICATIONID.toString(), "In valid response from Registration status API");
				throw new RIDInvalidException(ResidentErrorCode.INVALID_API_RESPONSE.getErrorCode(),
						ResidentErrorCode.INVALID_API_RESPONSE.getErrorMessage() + ApiName.REGISTRATIONSTATUSSEARCH);
			}

			String status = validateResponse(responseWrapper.getResponse().get(0).getStatusCode());
			response = new RegStatusCheckResponseDTO();
			response.setRidStatus(status);

		} catch (IOException e) {
			throw new ResidentServiceException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		} catch (ApisResourceAccessException e) {
			if (e.getCause() instanceof HttpClientErrorException) {
				HttpClientErrorException httpClientException = (HttpClientErrorException) e.getCause();
				throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
						httpClientException.getResponseBodyAsString());

			} else if (e.getCause() instanceof HttpServerErrorException) {
				HttpServerErrorException httpServerException = (HttpServerErrorException) e.getCause();
				throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
						httpServerException.getResponseBodyAsString());
			} else {
				throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
						ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage() + e.getMessage(), e);
			}

		}

		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(), "ResidentServiceImpl::getRidStatus():: exit");
		return response;
	}

	private String validateResponse(String statusCode) {
		if (statusCode.equalsIgnoreCase(RegistrationExternalStatusCode.PROCESSED.name()))
			return statusCode.toUpperCase();
		if (statusCode.equalsIgnoreCase(RegistrationExternalStatusCode.REJECTED.name()))
			return REJECTED_MESSAGE;
		if (statusCode.equalsIgnoreCase(RegistrationExternalStatusCode.REREGISTER.name()))
			return REREGISTER_MESSAGE;
		if (statusCode.equalsIgnoreCase(RegistrationExternalStatusCode.RESEND.name()))
			return RESEND_MESSAGE;
		if (statusCode.equalsIgnoreCase(RegistrationExternalStatusCode.PROCESSING.name()))
			return PROCESSING_MESSAGE;
		return PROCESSING_MESSAGE;

	}

	@Override
	public byte[] reqEuin(EuinRequestDTO dto) throws ResidentServiceCheckedException {
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(), "ResidentServiceImpl::reqEuin():: entry");
		byte[] response = null;
		IdType idtype=getIdType(dto.getIndividualIdType());
		try {
			if (idAuthService.validateOtp(dto.getTransactionID(), dto.getIndividualId(),
					dto.getIndividualIdType(), dto.getOtp())) {

				response = uinCardDownloadService.getUINCard(dto.getIndividualId(), dto.getCardType(),
						idtype);
				
					sendNotification(dto.getIndividualId(), idtype,
							NotificationTemplateCode.RS_DOW_UIN_SUCCESS, null);
               } else {
				logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
						LoggerFileConstant.APPLICATIONID.toString(),
						ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
				sendNotification(dto.getIndividualId(), idtype,
						NotificationTemplateCode.RS_DOW_UIN_FAILURE, null);
				throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
						ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
			}
		} catch (ApisResourceAccessException e) {
			sendNotification(dto.getIndividualId(), idtype,
					NotificationTemplateCode.RS_DOW_UIN_FAILURE, null);
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode()
							+ ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorMessage()
							+ ExceptionUtils.getStackTrace(e));
			throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorMessage(), e);
		} catch (ResidentServiceCheckedException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					ResidentErrorCode.NOTIFICATION_FAILURE.getErrorCode()
							+ ResidentErrorCode.NOTIFICATION_FAILURE.getErrorMessage()
							+ ExceptionUtils.getStackTrace(e));
			throw new ResidentServiceException(ResidentErrorCode.NOTIFICATION_FAILURE.getErrorCode(),
					ResidentErrorCode.NOTIFICATION_FAILURE.getErrorMessage(), e);
		} catch (OtpValidationFailedException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode()
							+ ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage()
							+ ExceptionUtils.getStackTrace(e));
			sendNotification(dto.getIndividualId(), idtype,
					NotificationTemplateCode.RS_DOW_UIN_FAILURE, null);
			throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
					e.getErrorText(), e);
		}

		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(), "ResidentServiceImpl::reqEuin():: exit");
		return response;
	}

	@Override
	public ResidentReprintResponseDto reqPrintUin(ResidentReprintRequestDto dto)
			throws ResidentServiceCheckedException {
		ResidentReprintResponseDto reprintResponse = new ResidentReprintResponseDto();

		try {
			if (!idAuthService.validateOtp(dto.getTransactionID(), dto.getIndividualId(),
					dto.getIndividualIdType(), dto.getOtp())) {
				sendNotification(dto.getIndividualId(), IdType.valueOf(dto.getIndividualIdType()),
						NotificationTemplateCode.RS_UIN_RPR_FAILURE, null);
				throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
						ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
			}
			RegProcRePrintRequestDto rePrintReq = new RegProcRePrintRequestDto();
			rePrintReq.setCardType(dto.getCardType());
			rePrintReq.setCenterId(centerId);
			rePrintReq.setMachineId(machineId);
			rePrintReq.setId(dto.getIndividualId());
			rePrintReq.setIdType(dto.getIndividualIdType());
			rePrintReq.setReason("resident");
			rePrintReq.setRegistrationType(RegistrationType.RES_REPRINT.name());

			PacketGeneratorResDto resDto = rePrintService.createPacket(rePrintReq);

			Map<String, Object> additionalAttributes = new HashMap<>();
			additionalAttributes.put(IdType.RID.name(), resDto.getRegistrationId());
			NotificationResponseDTO notificationResponseDTO = sendNotification(dto.getIndividualId(),
					IdType.valueOf(dto.getIndividualIdType()), NotificationTemplateCode.RS_UIN_RPR_SUCCESS,
					additionalAttributes);
			reprintResponse.setRegistrationId(resDto.getRegistrationId());
			reprintResponse.setMessage(notificationResponseDTO.getMessage());

		} catch (OtpValidationFailedException e) {
			sendNotification(dto.getIndividualId(), IdType.valueOf(dto.getIndividualIdType()),
					NotificationTemplateCode.RS_UIN_RPR_FAILURE, null);
			throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
					e.getErrorText(), e);
		} catch (ApisResourceAccessException e) {
			sendNotification(dto.getIndividualId(), IdType.valueOf(dto.getIndividualIdType()),
					NotificationTemplateCode.RS_UIN_RPR_FAILURE, null);
			if (e.getCause() instanceof HttpClientErrorException) {
				HttpClientErrorException httpClientException = (HttpClientErrorException) e.getCause();
				throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
						httpClientException.getResponseBodyAsString());

			} else if (e.getCause() instanceof HttpServerErrorException) {
				HttpServerErrorException httpServerException = (HttpServerErrorException) e.getCause();
				throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
						httpServerException.getResponseBodyAsString());
			} else {
				throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
						ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage() + e.getMessage(), e);
			}
		} catch (IOException e) {
			sendNotification(dto.getIndividualId(), IdType.valueOf(dto.getIndividualIdType()),
					NotificationTemplateCode.RS_UIN_RPR_FAILURE, null);
			throw new ResidentServiceException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		} catch (ResidentServiceCheckedException e) {
			sendNotification(dto.getIndividualId(), IdType.valueOf(dto.getIndividualIdType()),
					NotificationTemplateCode.RS_UIN_RPR_FAILURE, null);
			throw new ResidentServiceException(ResidentErrorCode.NOTIFICATION_FAILURE.getErrorCode(),
					ResidentErrorCode.NOTIFICATION_FAILURE.getErrorMessage(), e);
		} catch (BaseCheckedException e) {
			sendNotification(dto.getIndividualId(), IdType.valueOf(dto.getIndividualIdType()),
					NotificationTemplateCode.RS_UIN_RPR_FAILURE, null);
			throw new ResidentServiceException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(),
					ResidentErrorCode.BASE_EXCEPTION.getErrorMessage(), e);
		}

		return reprintResponse;
	}

	@Override
	public ResponseDTO reqAauthTypeStatusUpdate(AuthLockOrUnLockRequestDto dto, AuthTypeStatus authTypeStatus)
			throws ResidentServiceCheckedException {
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(), "ResidentServiceImpl::reqAauthTypeStatusUpdate():: entry");

		ResponseDTO response = new ResponseDTO();
		boolean isTransactionSuccessful = false;
		try {

			if (idAuthService.validateOtp(dto.getTransactionID(), dto.getIndividualId(),
					dto.getIndividualIdType(), dto.getOtp())) {

				boolean isAuthTypeStatusUpdated = idAuthService.authTypeStatusUpdate(dto.getIndividualId(),
						dto.getIndividualIdType(), dto.getAuthType(), authTypeStatus);
				if (isAuthTypeStatusUpdated) {
					isTransactionSuccessful = true;

				} else {
					throw new ResidentServiceException(ResidentErrorCode.REQUEST_FAILED.getErrorCode(),
							ResidentErrorCode.REQUEST_FAILED.getErrorMessage());
				}
			} else {

				logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
						LoggerFileConstant.APPLICATIONID.toString(),
						ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
				throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
						ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
			}

		} catch (ApisResourceAccessException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode()
							+ ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorMessage()
							+ ExceptionUtils.getStackTrace(e));
			throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorMessage(), e);
		} catch (OtpValidationFailedException e) {
			throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
					e.getErrorText(), e);
		} finally {
			NotificationTemplateCode templateCode;
			if (authTypeStatus.equals(AuthTypeStatus.LOCK)) {
				templateCode = isTransactionSuccessful ? NotificationTemplateCode.RS_LOCK_AUTH_SUCCESS
						: NotificationTemplateCode.RS_LOCK_AUTH_FAILURE;
			} else {
				templateCode = isTransactionSuccessful ? NotificationTemplateCode.RS_UNLOCK_AUTH_SUCCESS
						: NotificationTemplateCode.RS_UNLOCK_AUTH_FAILURE;
			}

			NotificationResponseDTO notificationResponseDTO = sendNotification(dto.getIndividualId(),
					IdType.valueOf(dto.getIndividualIdType()), templateCode, null);
			if (notificationResponseDTO != null) {
				response.setMessage(notificationResponseDTO.getMessage());
			}
		}
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(), "ResidentServiceImpl::reqAauthTypeStatusUpdate():: exit");
		return response;
	}

	@Override
	public AuthHistoryResponseDTO reqAuthHistory(AuthHistoryRequestDTO dto) throws ResidentServiceCheckedException {
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(), "ResidentServiceImpl::reqAuthHistory():: entry");

		AuthHistoryResponseDTO response = new AuthHistoryResponseDTO();
		IdType idtype=getIdType(dto.getIndividualIdType());
		try {

			if (idAuthService.validateOtp(dto.getTransactionID(), dto.getIndividualId(),
					dto.getIndividualIdType(), dto.getOtp())) {
				List<AuthTxnDetailsDTO> details = idAuthService.getAuthHistoryDetails(dto.getIndividualId(),
						dto.getIndividualIdType(), dto.getPageStart(), dto.getPageFetch());
				if (details != null) {
					response.setAuthHistory(details);

					NotificationResponseDTO notificationResponseDTO = sendNotification(dto.getIndividualId(),
							idtype, NotificationTemplateCode.RS_AUTH_HIST_SUCCESS, null);
					response.setMessage(notificationResponseDTO.getMessage());
				} else {
					sendNotification(dto.getIndividualId(), idtype,
							NotificationTemplateCode.RS_AUTH_HIST_FAILURE, null);
					throw new ResidentServiceException(ResidentErrorCode.REQUEST_FAILED.getErrorCode(),
							ResidentErrorCode.REQUEST_FAILED.getErrorMessage());
				}
			} else {
				logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
						LoggerFileConstant.APPLICATIONID.toString(),
						ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
				sendNotification(dto.getIndividualId(), idtype,
						NotificationTemplateCode.RS_AUTH_HIST_FAILURE, null);
				throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
						ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
			}

		} catch (OtpValidationFailedException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode()
							+ ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage()
							+ ExceptionUtils.getStackTrace(e));
			sendNotification(dto.getIndividualId(), idtype,
					NotificationTemplateCode.RS_AUTH_HIST_FAILURE, null);
			throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
					e.getErrorText(), e);
		} catch (ResidentServiceCheckedException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					ResidentErrorCode.NOTIFICATION_FAILURE.getErrorCode()
							+ ResidentErrorCode.NOTIFICATION_FAILURE.getErrorMessage()
							+ ExceptionUtils.getStackTrace(e));
			throw new ResidentServiceException(ResidentErrorCode.NOTIFICATION_FAILURE.getErrorCode(),
					ResidentErrorCode.NOTIFICATION_FAILURE.getErrorMessage(), e);
		} catch (ApisResourceAccessException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode()
							+ ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorMessage()
							+ ExceptionUtils.getStackTrace(e));
			sendNotification(dto.getIndividualId(), idtype,
					NotificationTemplateCode.RS_AUTH_HIST_FAILURE, null);
			throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorMessage(), e);
		}
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(), "ResidentServiceImpl::reqAuthHistory():: exit");
		return response;
	}

	private NotificationResponseDTO sendNotification(String id, IdType idType,
			NotificationTemplateCode templateTypeCode, Map<String, Object> additionalAttributes)
			throws ResidentServiceCheckedException {
		NotificationRequestDto notificationRequest = new NotificationRequestDto(id, templateTypeCode,
				additionalAttributes);
		return notificationService.sendNotification(notificationRequest);
	}

	@Override
	public ResidentUpdateResponseDTO reqUinUpdate(ResidentUpdateRequestDto dto) throws ResidentServiceCheckedException {
		ResidentUpdateResponseDTO responseDto = new ResidentUpdateResponseDTO();
		try {
			if (!idAuthService.validateOtp(dto.getTransactionID(), dto.getIndividualId(),
					dto.getIndividualIdType(), dto.getOtp())) {
				sendNotification(dto.getIndividualId(), IdType.valueOf(dto.getIndividualIdType()),
						NotificationTemplateCode.RS_UIN_UPDATE_FAILURE, null);
				throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
						ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
			}
			ResidentUpdateDto regProcReqUpdateDto = new ResidentUpdateDto();
			regProcReqUpdateDto.setIdValue(dto.getIndividualId());
			regProcReqUpdateDto.setIdType(ResidentIndividialIDType.valueOf(dto.getIndividualIdType().toUpperCase()));
			regProcReqUpdateDto.setCenterId(centerId);
			regProcReqUpdateDto.setMachineId(machineId);
			regProcReqUpdateDto.setIdentityJson(dto.getIdentityJson());
			List<ResidentDocuments> documents = dto.getDocuments();
			byte[] decodedDemoJson = CryptoUtil.decodeBase64(dto.getIdentityJson());
			JSONObject demographicJsonObject = JsonUtil.readValue(new String(decodedDemoJson), JSONObject.class);
			JSONObject demographicIdentity = JsonUtil.getJSONObject(demographicJsonObject, IDENTITY);
			String mappingJson = utility.getMappingJson();
			if(demographicIdentity==null || demographicIdentity.isEmpty()|| mappingJson==null || mappingJson.trim().isEmpty()) {
				throw new ResidentServiceException(ResidentErrorCode.JSON_PROCESSING_EXCEPTION.getErrorCode(),
						ResidentErrorCode.JSON_PROCESSING_EXCEPTION.getErrorMessage() );
			}
			JSONObject mappingJsonObject = JsonUtil.readValue(mappingJson, JSONObject.class);
			JSONObject mappingIdentity = JsonUtil.getJSONObject(mappingJsonObject, IDENTITY);
			String poaMapping = getDocumentName(mappingIdentity, PROOF_OF_ADDRESS);
			String poiMapping = getDocumentName(mappingIdentity, PROOF_OF_IDENTITY);
			String porMapping = getDocumentName(mappingIdentity, PROOF_OF_RELATIONSHIP);
			String pobMapping = getDocumentName(mappingIdentity, PROOF_OF_DOB);
			JSONObject proofOfAddressJson = JsonUtil.getJSONObject(demographicIdentity, poaMapping);
			regProcReqUpdateDto.setProofOfAddress(getDocumentValue(proofOfAddressJson, documents));
			JSONObject proofOfIdentityJson = JsonUtil.getJSONObject(demographicIdentity, poiMapping);
			regProcReqUpdateDto.setProofOfIdentity(getDocumentValue(proofOfIdentityJson, documents));
			JSONObject proofOfrelationJson = JsonUtil.getJSONObject(demographicIdentity, porMapping);
			regProcReqUpdateDto.setProofOfRelationship(getDocumentValue(proofOfrelationJson, documents));
			JSONObject proofOfBirthJson = JsonUtil.getJSONObject(demographicIdentity, pobMapping);
			regProcReqUpdateDto.setProofOfDateOfBirth(getDocumentValue(proofOfBirthJson, documents));


			PacketGeneratorResDto response = residentUpdateService.createPacket(regProcReqUpdateDto);
			Map<String, Object> additionalAttributes = new HashMap<>();
			additionalAttributes.put("RID", response.getRegistrationId());
			NotificationResponseDTO notificationResponseDTO = sendNotification(dto.getIndividualId(),
					IdType.valueOf(dto.getIndividualIdType()), NotificationTemplateCode.RS_UIN_UPDATE_SUCCESS, additionalAttributes);
			responseDto.setMessage(notificationResponseDTO.getMessage());
			responseDto.setRegistrationId(response.getRegistrationId());

		} catch (OtpValidationFailedException e) {
			sendNotification(dto.getIndividualId(), IdType.valueOf(dto.getIndividualIdType()),
					NotificationTemplateCode.RS_UIN_UPDATE_FAILURE, null);
			throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
					e.getErrorText(), e);

		} catch (ApisResourceAccessException e) {
			sendNotification(dto.getIndividualId(),  IdType.valueOf(dto.getIndividualIdType()),
					NotificationTemplateCode.RS_UIN_UPDATE_FAILURE, null);
			if (e.getCause() instanceof HttpClientErrorException) {
				HttpClientErrorException httpClientException = (HttpClientErrorException) e.getCause();
				throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
						httpClientException.getResponseBodyAsString());

			} else if (e.getCause() instanceof HttpServerErrorException) {
				HttpServerErrorException httpServerException = (HttpServerErrorException) e.getCause();
				throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
						httpServerException.getResponseBodyAsString());
			} else {
				throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
						ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage() + e.getMessage(), e);
			}
		} catch (IOException e) {
			sendNotification(dto.getIndividualId(),  IdType.valueOf(dto.getIndividualIdType()),
					NotificationTemplateCode.RS_UIN_UPDATE_FAILURE, null);
			throw new ResidentServiceException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		} catch (BaseCheckedException e) {
			sendNotification(dto.getIndividualId(),  IdType.valueOf(dto.getIndividualIdType()),
					NotificationTemplateCode.RS_UIN_UPDATE_FAILURE, null);
			throw new ResidentServiceException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(),
					ResidentErrorCode.BASE_EXCEPTION.getErrorMessage(), e);
		}
		return responseDto;
	}

	// get name of document
	private String getDocumentName(JSONObject identityJson, String name) {
		JSONObject docJson = JsonUtil.getJSONObject(identityJson, name);
		return JsonUtil.getJSONValue(docJson, VALUE);
	}

	// get document content
	private String getDocumentValue(JSONObject documentJsonObject, List<ResidentDocuments> documents) {
		if (documentJsonObject == null || documents == null || documents.isEmpty())
			return null;
		String documentName = JsonUtil.getJSONValue(documentJsonObject, VALUE);
		Optional<ResidentDocuments> residentDocument = documents.parallelStream()
				.filter(document -> document.getName().equals(documentName)).findAny();
		if (residentDocument.isPresent())
			return residentDocument.get().getValue();
		else
			throw new ResidentServiceException(ResidentErrorCode.DOCUMENT_NOT_FOUND.getErrorCode(),
					ResidentErrorCode.DOCUMENT_NOT_FOUND.getErrorMessage());

	}
	private IdType getIdType(String individualType) {
		if(individualType.equalsIgnoreCase(IdType.UIN.name())) {
			return IdType.UIN;
		}
		if(individualType.equalsIgnoreCase(IdType.VID.name())) {
			return IdType.VID;
		}
		if(individualType.equalsIgnoreCase(IdType.RID.name())) {
			return IdType.RID;
		}
		return null;
	}
}