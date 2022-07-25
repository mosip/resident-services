package io.mosip.resident.service.impl;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.constant.*;
import io.mosip.resident.dto.*;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.*;
import io.mosip.resident.handler.service.ResidentUpdateService;
import io.mosip.resident.handler.service.UinCardRePrintService;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.*;
import io.mosip.resident.util.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static io.mosip.resident.constant.ResidentErrorCode.MACHINE_MASTER_CREATE_EXCEPTION;
import static io.mosip.resident.constant.ResidentErrorCode.PACKET_SIGNKEY_EXCEPTION;

@Service
public class ResidentServiceImpl implements ResidentService {

	private static final String PROCESSED = "PROCESSED";
	private static final String DATETIME_PATTERN = "mosip.utc-datetime-pattern";
	private static final String STATUS_CHECK_ID = "mosip.resident.service.status.check.id";
	private static final String STATUS_CHECEK_VERSION = "mosip.resident.service.status.check.version";
	private static final String PROCESSING_MESSAGE = "UNDER PROCESSING";
	private static final String WAITING_MESSAGE = "WAITING FOR ADDITIONAL INFORMATION FROM APPLICANT";
	private static final String PROOF_OF_ADDRESS = "poa";
	private static final String PROOF_OF_DOB = "pob";
	private static final String PROOF_OF_RELATIONSHIP = "por";
	private static final String PROOF_OF_IDENTITY = "poi";
	private static final String IDENTITY = "identity";
	private static final String VALUE = "value";
	private static final String DOCUMENT = "documents";
	private static final String SERVER_PROFILE_SIGN_KEY = "PROD";
	private static final String UIN = "uin";

	private static final Logger logger = LoggerConfiguration.logConfig(ResidentServiceImpl.class);
	private static final Integer DEFAULT_PAGE_START = 1;
	private static final Integer DEFAULT_PAGE_COUNT = 10;
	private static final String AVAILABLE = "AVAILABLE";
	private static final String PRINTING = "PRINTING";

	@Autowired
	private UINCardDownloadService uinCardDownloadService;

	@Autowired
	private ResidentUpdateService residentUpdateService;

	@Autowired
	private IdAuthService idAuthService;

	@Autowired
	NotificationService notificationService;

	@Autowired
	PartnerService partnerService;

	@Autowired
	private IdentityServiceImpl identityServiceImpl;

	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;

	@Autowired
	private UinCardRePrintService rePrintService;

	@Autowired
	private ResidentTransactionRepository residentTransactionRepository;

	@Autowired
	Environment env;

	@Autowired
	private Utilitiy utility;

	@Autowired
	private Utilities utilities;

	@Value("${resident.center.id}")
	private String centerId;

	@Value("${resident.machine.id}")
	private String machineId;

	@Value("${resident.update-uin.machine-name-prefix}")
	private String residentMachinePrefix;

	@Value("${resident.update-uin.machine-spec-id}")
	private String machineSpecId;

	@Value("${resident.update-uin.machine-zone-code}")
	private String zoneCode;

	@Value("${mosip.vid.only:false}")
	private boolean vidOnly;

	@Autowired
	private AuditUtil audit;

	@Autowired
	private DocumentService docService;
	
	@Autowired
	private ResidentTransactionRepository txnRepo;

	@Autowired
	private PartnerService partnerServiceImpl;
	
	@Autowired
	private IdAuthService idAuthServiceImpl;

	@Autowired
	private ResidentCredentialServiceImpl residentCredentialServiceImpl;

	@Override
	public RegStatusCheckResponseDTO getRidStatus(RequestDTO request) {
		return getRidStatus(request.getIndividualId());
	}

	@Override
	public RegStatusCheckResponseDTO getRidStatus(String ridValue) {
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(), "ResidentServiceImpl::getRidStatus():: entry");

		RegStatusCheckResponseDTO response = null;
		RegistrationStatusResponseDTO responseWrapper = null;
		RegistrationStatusRequestDTO dto = new RegistrationStatusRequestDTO();
		List<RegistrationStatusSubRequestDto> rids = new ArrayList<>();
		RegistrationStatusSubRequestDto rid = new RegistrationStatusSubRequestDto(ridValue);

		rids.add(rid);
		dto.setRequest(rids);
		dto.setId(env.getProperty(STATUS_CHECK_ID));
		dto.setVersion(env.getProperty(STATUS_CHECEK_VERSION));
		dto.setRequesttime(DateUtils.getUTCCurrentDateTimeString(env.getProperty(DATETIME_PATTERN)));
		audit.setAuditRequestDto(EventEnum.GETTING_RID_STATUS);
		try {
			responseWrapper = (RegistrationStatusResponseDTO) residentServiceRestClient.postApi(
					env.getProperty(ApiName.REGISTRATIONSTATUSSEARCH.name()), MediaType.APPLICATION_JSON, dto,
					RegistrationStatusResponseDTO.class);
			if (responseWrapper == null) {
				logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
						LoggerFileConstant.APPLICATIONID.toString(), "In valid response from Registration status API");
				audit.setAuditRequestDto(EventEnum.INVALID_API_RESPONSE);
				throw new RIDInvalidException(ResidentErrorCode.INVALID_API_RESPONSE.getErrorCode(),
						ResidentErrorCode.INVALID_API_RESPONSE.getErrorMessage()
								+ ApiName.REGISTRATIONSTATUSSEARCH.name());
			}

			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
						LoggerFileConstant.APPLICATIONID.toString(), responseWrapper.getErrors().get(0).toString());
				audit.setAuditRequestDto(EventEnum.RID_NOT_FOUND);
				throw new RIDInvalidException(ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorCode(),
						ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorMessage());
			}
			if ((responseWrapper.getResponse() == null || responseWrapper.getResponse().isEmpty())) {
				logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
						LoggerFileConstant.APPLICATIONID.toString(), "In valid response from Registration status API");
				audit.setAuditRequestDto(EventEnum.INVALID_API_RESPONSE);
				throw new RIDInvalidException(ResidentErrorCode.INVALID_API_RESPONSE.getErrorCode(),
						ResidentErrorCode.INVALID_API_RESPONSE.getErrorMessage() + ApiName.REGISTRATIONSTATUSSEARCH);
			}

			String status = validateResponse(responseWrapper.getResponse().get(0).getStatusCode());
			response = new RegStatusCheckResponseDTO();
			response.setRidStatus(status);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.RID_STATUS_RESPONSE, status));

		} catch (ApisResourceAccessException e) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithDynamicName(EventEnum.API_RESOURCE_UNACCESS, "checking RID status"));
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
			return statusCode.toUpperCase();
		if (statusCode.equalsIgnoreCase(RegistrationExternalStatusCode.REREGISTER.name()))
			return statusCode.toUpperCase();
		if (statusCode.equalsIgnoreCase(RegistrationExternalStatusCode.RESEND.name()))
			return statusCode.toUpperCase();
		if (statusCode.equalsIgnoreCase(RegistrationExternalStatusCode.PROCESSING.name()))
			return PROCESSING_MESSAGE;
		if (statusCode.equalsIgnoreCase(RegistrationExternalStatusCode.UIN_GENERATED.name()))
			return RegistrationExternalStatusCode.PROCESSED.name();
		if (statusCode.equalsIgnoreCase(RegistrationExternalStatusCode.AWAITING_INFORMATION.name()))
			return WAITING_MESSAGE;
		return PROCESSING_MESSAGE;

	}

	@Override
	public byte[] reqEuin(EuinRequestDTO dto) throws ResidentServiceCheckedException {
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(), "ResidentServiceImpl::reqEuin():: entry");
		byte[] response = null;
		IdType idtype = getIdType(dto.getIndividualIdType());
		try {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_OTP, dto.getTransactionID(), "Request EUIN"));
			if (idAuthService.validateOtp(dto.getTransactionID(), dto.getIndividualId(), dto.getOtp())) {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_OTP_SUCCESS,
						dto.getTransactionID(), "Request EUIN"));
				response = uinCardDownloadService.getUINCard(dto.getIndividualId(), dto.getCardType(), idtype);
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_SUCCESS,
						dto.getTransactionID(), "Request EUIN"));
				sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_DOW_UIN_SUCCESS, null);
			} else {
				logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
						LoggerFileConstant.APPLICATIONID.toString(),
						ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
				sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_DOW_UIN_FAILURE, null);
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
						dto.getTransactionID(), "Request EUIN"));
				throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
						ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
			}
		} catch (ApisResourceAccessException e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.API_RESOURCE_UNACCESS,
					dto.getTransactionID(), "Request EUIN"));
			sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_DOW_UIN_FAILURE, null);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					dto.getTransactionID(), "Request EUIN"));
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
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.NOTIFICATION_FAILED,
					dto.getTransactionID(), "Request EUIN"));
			throw new ResidentServiceException(ResidentErrorCode.NOTIFICATION_FAILURE.getErrorCode(),
					ResidentErrorCode.NOTIFICATION_FAILURE.getErrorMessage(), e);
		} catch (OtpValidationFailedException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode()
							+ ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage()
							+ ExceptionUtils.getStackTrace(e));
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.OTP_VALIDATION_FAILED,
					dto.getTransactionID(), "Request EUIN"));
			trySendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_DOW_UIN_FAILURE, null);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					dto.getTransactionID(), "Request EUIN"));
			throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(), e.getErrorText(),
					e);
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
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_OTP, dto.getTransactionID(),
					"Request for print UIN"));
			if (!idAuthService.validateOtp(dto.getTransactionID(), dto.getIndividualId(), dto.getOtp())) {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.OTP_VALIDATION_FAILED,
						dto.getTransactionID(), "Request for print UIN"));
				sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_UIN_RPR_FAILURE, null);
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
						dto.getTransactionID(), "Request for print UIN"));
				throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
						ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
			}
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_OTP_SUCCESS,
					dto.getTransactionID(), "Request for print UIN"));
			RegProcRePrintRequestDto rePrintReq = new RegProcRePrintRequestDto();
			rePrintReq.setCardType(dto.getCardType());
			rePrintReq.setCenterId(centerId);
			rePrintReq.setMachineId(machineId);
			rePrintReq.setId(dto.getIndividualId());
			rePrintReq.setIdType(dto.getIndividualIdType());
			rePrintReq.setReason("resident");
			rePrintReq.setRegistrationType(RegistrationType.RES_REPRINT.name());

			PacketGeneratorResDto resDto = rePrintService.createPacket(rePrintReq);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.OBTAINED_RID, dto.getTransactionID()));
			Map<String, Object> additionalAttributes = new HashMap<>();
			additionalAttributes.put(IdType.RID.name(), resDto.getRegistrationId());

			NotificationResponseDTO notificationResponseDTO = sendNotification(dto.getIndividualId(),
					NotificationTemplateCode.RS_UIN_RPR_SUCCESS, additionalAttributes);
			reprintResponse.setRegistrationId(resDto.getRegistrationId());
			reprintResponse.setMessage(notificationResponseDTO.getMessage());
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_SUCCESS,
					dto.getTransactionID(), "Request for print UIN"));

		} catch (OtpValidationFailedException e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.OTP_VALIDATION_FAILED,
					dto.getTransactionID(), "Request for print UIN"));
			trySendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_UIN_RPR_FAILURE, null);

			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					dto.getTransactionID(), "Request for print UIN"));
			throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(), e.getErrorText(),
					e);
		} catch (ApisResourceAccessException e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.API_RESOURCE_UNACCESS,
					dto.getTransactionID(), "Request for print UIN"));
			sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_UIN_RPR_FAILURE, null);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					dto.getTransactionID(), "Request for print UIN"));

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
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithDynamicName(EventEnum.IO_EXCEPTION, "request for print UIN"));
			sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_UIN_RPR_FAILURE, null);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					dto.getTransactionID(), "Request for print UIN"));

			throw new ResidentServiceException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		} catch (ResidentServiceCheckedException e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.NOTIFICATION_FAILED,
					dto.getTransactionID(), "Request for print UIN"));
			sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_UIN_RPR_FAILURE, null);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					dto.getTransactionID(), "Request for print UIN"));

			throw new ResidentServiceException(ResidentErrorCode.NOTIFICATION_FAILURE.getErrorCode(),
					ResidentErrorCode.NOTIFICATION_FAILURE.getErrorMessage(), e);
		} catch (BaseCheckedException e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.BASE_EXCEPTION, dto.getTransactionID(),
					"Request for print UIN"));
			sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_UIN_RPR_FAILURE, null);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					dto.getTransactionID(), "Request for print UIN"));

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
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_OTP, dto.getTransactionID(),
					"Request for auth " + authTypeStatus.toString().toLowerCase()));
			if (idAuthService.validateOtp(dto.getTransactionID(), dto.getIndividualId(), dto.getOtp())) {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_OTP_SUCCESS,
						dto.getTransactionID(), "Request for auth " + authTypeStatus.toString().toLowerCase()));
				Long unlockForSeconds = null;
				if (authTypeStatus.equals(AuthTypeStatus.UNLOCK)) {
					AuthUnLockRequestDTO authUnLockRequestDTO = (AuthUnLockRequestDTO) dto;
					unlockForSeconds = Long.parseLong(authUnLockRequestDTO.getUnlockForSeconds());
				}
				boolean isAuthTypeStatusUpdated = idAuthService.authTypeStatusUpdate(dto.getIndividualId(),
						dto.getAuthType(), authTypeStatus, unlockForSeconds);
				if (isAuthTypeStatusUpdated) {
					isTransactionSuccessful = true;
				} else {
					audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQUEST_FAILED,
							dto.getTransactionID(), "Request for auth " + authTypeStatus.toString().toLowerCase()));
					throw new ResidentServiceException(ResidentErrorCode.REQUEST_FAILED.getErrorCode(),
							ResidentErrorCode.REQUEST_FAILED.getErrorMessage());
				}
			} else {

				logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
						LoggerFileConstant.APPLICATIONID.toString(),
						ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.OTP_VALIDATION_FAILED,
						dto.getTransactionID(), "Request for auth " + authTypeStatus.toString().toLowerCase()));
				throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
						ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
			}

		} catch (ApisResourceAccessException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode()
							+ ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorMessage()
							+ ExceptionUtils.getStackTrace(e));
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.API_NOT_AVAILABLE,
					dto.getTransactionID(), "Request for auth" + authTypeStatus.toString().toLowerCase()));
			throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorMessage(), e);
		} catch (OtpValidationFailedException e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.OTP_VALIDATION_FAILED,
					dto.getTransactionID(), "Request for auth " + authTypeStatus.toString().toLowerCase()));
			throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(), e.getErrorText(),
					e);
		} finally {
			NotificationTemplateCode templateCode;
			if (authTypeStatus.equals(AuthTypeStatus.LOCK)) {
				templateCode = isTransactionSuccessful ? NotificationTemplateCode.RS_LOCK_AUTH_SUCCESS
						: NotificationTemplateCode.RS_LOCK_AUTH_FAILURE;
			} else {
				templateCode = isTransactionSuccessful ? NotificationTemplateCode.RS_UNLOCK_AUTH_SUCCESS
						: NotificationTemplateCode.RS_UNLOCK_AUTH_FAILURE;
			}

			NotificationResponseDTO notificationResponseDTO = sendNotification(dto.getIndividualId(), templateCode,
					null);
			if (isTransactionSuccessful)
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_SUCCESS,
						dto.getTransactionID(), "Request for auth " + authTypeStatus.toString().toLowerCase()));
			else
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
						dto.getTransactionID(), "Request for auth " + authTypeStatus.toString().toLowerCase()));
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

		try {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_OTP, dto.getTransactionID(),
					"Request for auth history"));
			if (idAuthService.validateOtp(dto.getTransactionID(), dto.getIndividualId(), dto.getOtp())) {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_OTP_SUCCESS,
						dto.getTransactionID(), "Request for auth history"));
				List<AuthTxnDetailsDTO> details = idAuthService.getAuthHistoryDetails(dto.getIndividualId(),
						dto.getPageStart(), dto.getPageFetch());
				if (details != null) {
					response.setAuthHistory(details);

					NotificationResponseDTO notificationResponseDTO = sendNotification(dto.getIndividualId(),
							NotificationTemplateCode.RS_AUTH_HIST_SUCCESS, null);
					audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_SUCCESS,
							dto.getTransactionID(), "Request for auth history"));
					response.setMessage(notificationResponseDTO.getMessage());
				} else {
					audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQUEST_FAILED,
							dto.getTransactionID(), "Request for auth history"));
					sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_AUTH_HIST_FAILURE, null);
					audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
							dto.getTransactionID(), "Request for auth history"));
					throw new ResidentServiceException(ResidentErrorCode.REQUEST_FAILED.getErrorCode(),
							ResidentErrorCode.REQUEST_FAILED.getErrorMessage());
				}
			} else {
				logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
						LoggerFileConstant.APPLICATIONID.toString(),
						ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.OTP_VALIDATION_FAILED,
						dto.getTransactionID(), "Request for auth history"));
				sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_AUTH_HIST_FAILURE, null);
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
						dto.getTransactionID(), "Request for auth history"));
				throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
						ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
			}

		} catch (OtpValidationFailedException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode()
							+ ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage()
							+ ExceptionUtils.getStackTrace(e));
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.OTP_VALIDATION_FAILED,
					dto.getTransactionID(), "Request for auth history"));
			trySendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_AUTH_HIST_FAILURE, null);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					dto.getTransactionID(), "Request for auth history"));
			throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(), e.getErrorText(),
					e);
		} catch (ResidentServiceCheckedException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					ResidentErrorCode.NOTIFICATION_FAILURE.getErrorCode()
							+ ResidentErrorCode.NOTIFICATION_FAILURE.getErrorMessage()
							+ ExceptionUtils.getStackTrace(e));
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.NOTIFICATION_FAILED,
					dto.getTransactionID(), "Request for auth history"));
			throw new ResidentServiceException(ResidentErrorCode.NOTIFICATION_FAILURE.getErrorCode(),
					ResidentErrorCode.NOTIFICATION_FAILURE.getErrorMessage(), e);
		} catch (ApisResourceAccessException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode()
							+ ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorMessage()
							+ ExceptionUtils.getStackTrace(e));
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.API_NOT_AVAILABLE,
					dto.getTransactionID(), "Request for auth history"));
			sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_AUTH_HIST_FAILURE, null);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					dto.getTransactionID(), "Request for auth history"));
			throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorMessage(), e);
		}
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(), "ResidentServiceImpl::reqAuthHistory():: exit");
		return response;
	}

	private NotificationResponseDTO sendNotification(String id, NotificationTemplateCode templateTypeCode,
			Map<String, Object> additionalAttributes) throws ResidentServiceCheckedException {

		NotificationRequestDto notificationRequest = new NotificationRequestDto(id, templateTypeCode,
				additionalAttributes);
		return notificationService.sendNotification(notificationRequest);
	}
	
	private NotificationResponseDTO trySendNotification(String id, NotificationTemplateCode templateTypeCode,
			Map<String, Object> additionalAttributes) {		
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

	@Override
	public ResidentUpdateResponseDTO reqUinUpdate(ResidentUpdateRequestDto dto)
			throws ResidentServiceCheckedException {
		ResidentUpdateResponseDTO responseDto = new ResidentUpdateResponseDTO();
		try {
			if (Objects.nonNull(dto.getOtp())) {
				if (!idAuthService.validateOtp(dto.getTransactionID(), dto.getIndividualId(), dto.getOtp())) {
					audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_OTP,
							dto.getTransactionID(), "Request for UIN update"));
					audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.OTP_VALIDATION_FAILED,
							dto.getTransactionID(), "Request for UIN update"));
					sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_UIN_UPDATE_FAILURE, null);
					audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
							dto.getTransactionID(), "Request for UIN update"));
					throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
							ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
				}
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_OTP_SUCCESS,
						dto.getTransactionID(), "Request for UIN update"));
			}

			final String publicKey = getPublicKeyFromKeyManager();
			MachineSearchResponseDTO machineSearchResponseDTO = searchMachineInMasterService(residentMachinePrefix,
					publicKey);
			String machineId = getMachineId(machineSearchResponseDTO, publicKey);
			if (machineId == null) {
				machineId = createNewMachineInMasterService(residentMachinePrefix, machineSpecId, zoneCode, centerId,
						publicKey);
			}

			ResidentUpdateDto regProcReqUpdateDto = new ResidentUpdateDto();
			regProcReqUpdateDto.setIdValue(dto.getIndividualId());
			regProcReqUpdateDto.setIdType(ResidentIndividialIDType.valueOf(dto.getIndividualIdType().toUpperCase()));
			regProcReqUpdateDto.setCenterId(centerId);
			regProcReqUpdateDto.setMachineId(machineId);
			regProcReqUpdateDto.setIdentityJson(dto.getIdentityJson());
			byte[] decodedDemoJson = CryptoUtil.decodeURLSafeBase64(dto.getIdentityJson());
			JSONObject demographicJsonObject = JsonUtil.readValue(new String(decodedDemoJson), JSONObject.class);
			JSONObject demographicIdentity = JsonUtil.getJSONObject(demographicJsonObject, IDENTITY);
			String mappingJson = utility.getMappingJson();
			if (demographicIdentity == null || demographicIdentity.isEmpty() || mappingJson == null
					|| mappingJson.trim().isEmpty()) {
				audit.setAuditRequestDto(
						EventEnum.getEventEnumWithValue(EventEnum.JSON_PARSING_EXCEPTION, dto.getTransactionID()));
				throw new ResidentServiceException(ResidentErrorCode.JSON_PROCESSING_EXCEPTION.getErrorCode(),
						ResidentErrorCode.JSON_PROCESSING_EXCEPTION.getErrorMessage());
			}
			JSONObject mappingJsonObject = JsonUtil.readValue(mappingJson, JSONObject.class);
			validateAuthIndividualIdWithUIN(dto.getIndividualId(), dto.getIndividualIdType(),
					mappingJsonObject, demographicIdentity);
			JSONObject mappingDocument = JsonUtil.getJSONObject(mappingJsonObject, DOCUMENT);
			List<ResidentDocuments> documents = getResidentDocuments(dto, mappingDocument);
			String poaMapping = getDocumentName(mappingDocument, PROOF_OF_ADDRESS);
			String poiMapping = getDocumentName(mappingDocument, PROOF_OF_IDENTITY);
			String porMapping = getDocumentName(mappingDocument, PROOF_OF_RELATIONSHIP);
			String pobMapping = getDocumentName(mappingDocument, PROOF_OF_DOB);
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
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.OBTAINED_RID_UIN_UPDATE, dto.getTransactionID()));
			NotificationResponseDTO notificationResponseDTO = sendNotification(dto.getIndividualId(),
					NotificationTemplateCode.RS_UIN_UPDATE_SUCCESS, additionalAttributes);
			responseDto.setMessage(notificationResponseDTO.getMessage());
			responseDto.setRegistrationId(response.getRegistrationId());
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_SUCCESS,
					dto.getTransactionID(), "Request for UIN update"));
			updateResidentTransaction(dto, response);
		} catch (OtpValidationFailedException e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.OTP_VALIDATION_FAILED,
					dto.getTransactionID(), "Request for UIN update"));
			sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_UIN_UPDATE_FAILURE, null);

			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					dto.getTransactionID(), "Request for UIN update"));
			throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(), e.getErrorText(),
					e);

		} catch (ValidationFailedException e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATION_FAILED_EXCEPTION,
					e.getMessage() + " Transaction id: " + dto.getTransactionID(), "Request for UIN update"));
			sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_UIN_UPDATE_FAILURE, null);

			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					dto.getTransactionID(), "Request for UIN update"));
			throw new ResidentServiceException(e.getErrorCode(), e.getMessage(), e);

		} catch (ApisResourceAccessException e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.API_RESOURCE_UNACCESS,
					dto.getTransactionID(), "Request for UIN update"));
			sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_UIN_UPDATE_FAILURE, null);

			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					dto.getTransactionID(), "Request for UIN update"));
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
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.IO_EXCEPTION, dto.getTransactionID(),
					"Request for UIN update"));
			sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_UIN_UPDATE_FAILURE, null);

			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					dto.getTransactionID(), "Request for UIN update"));
			throw new ResidentServiceException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		} catch (BaseCheckedException | NoSuchAlgorithmException e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.BASE_EXCEPTION, dto.getTransactionID(),
					"Request for UIN update"));
			sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_UIN_UPDATE_FAILURE, null);

			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					dto.getTransactionID(), "Request for UIN update"));
			throw new ResidentServiceException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(),
					ResidentErrorCode.BASE_EXCEPTION.getErrorMessage(), e);
		}
		return responseDto;
	}

	private void updateResidentTransaction(ResidentUpdateRequestDto dto, PacketGeneratorResDto response)
			throws NoSuchAlgorithmException {
		ResidentTransactionEntity txn = new ResidentTransactionEntity();
		txn.setEventId(UUID.randomUUID().toString());
		txn.setAid(HMACUtils2.digestAsPlainText(response.getRegistrationId().getBytes()));
		txn.setRequestDtimes(DateUtils.getUTCCurrentDateTime());
		txn.setResponseDtime(DateUtils.getUTCCurrentDateTime());
		txn.setRequestTrnId(dto.getTransactionID());
		txn.setRequestTypeCode("UIN_UPDATED");
		txn.setAuthTypeCode(ResidentTransactionType.DATA_UPDATE_REQUEST.toString());
		txn.setRequestSummary("Uin updated successfully");
		txn.setStatusCode("UIN_UPDATED");
		txn.setStatusComment("Uin updated successfully");
		txn.setLangCode("");
		txn.setRefIdType("INDIVIDUAL_ID");
		txn.setRefId(HMACUtils2.digestAsPlainText(dto.getIndividualId().getBytes()));
		txn.setCrBy("mosip");
		txn.setCrDtimes(DateUtils.getUTCCurrentDateTime());
		txnRepo.save(txn);
	}
  
	private List<ResidentDocuments> getResidentDocuments(ResidentUpdateRequestDto dto, JSONObject mappingDocument) {
		if (Objects.nonNull(dto.getDocuments())) {
			return dto.getDocuments();
		}
		try {
			Map<DocumentResponseDTO, String> documentsWithMetadata = docService
					.getDocumentsWithMetadata(dto.getTransactionID());
			return documentsWithMetadata.entrySet().stream()
					.map(doc -> new ResidentDocuments(getDocumentName(mappingDocument, doc.getKey().getDocCatCode()),
							doc.getValue()))
					.collect(Collectors.toList());
		} catch (ResidentServiceCheckedException e) {
			throw new ResidentServiceException(ResidentErrorCode.FAILED_TO_RETRIEVE_DOC.getErrorCode(),
					ResidentErrorCode.FAILED_TO_RETRIEVE_DOC.getErrorMessage(), e);
		}
	}

	@Override
	public ResponseDTO reqAauthTypeStatusUpdateV2(AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(), "ResidentServiceImpl::reqAauthTypeStatusUpdate():: entry");
		ResponseDTO response = new ResponseDTO();
		String individualId = identityServiceImpl.getResidentIndvidualId();
		boolean isTransactionSuccessful = false;
		try {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_TYPE_LOCK,
					"Request for Auth Type Lock"));

			for (AuthTypeStatusDto authTypeStatusDto : authLockOrUnLockRequestDtoV2.getAuthTypes()) {
				boolean isAuthTypeStatusUpdated = idAuthService.authTypeStatusUpdate(individualId,
						List.of(authTypeStatusDto.getAuthType().split(",")), 
						authTypeStatusDto.getLocked()?AuthTypeStatus.LOCK:AuthTypeStatus.UNLOCK, authTypeStatusDto.getUnlockForSeconds());

				if (isAuthTypeStatusUpdated) {
					isTransactionSuccessful = true;
					insertAuthStatusInDb(isTransactionSuccessful, authLockOrUnLockRequestDtoV2, individualId);
				} else {
					audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQUEST_FAILED
							, "Request for auth " + authLockOrUnLockRequestDtoV2.getAuthTypes() + " lock failed"));
					throw new ResidentServiceException(ResidentErrorCode.REQUEST_FAILED.getErrorCode(),
							ResidentErrorCode.REQUEST_FAILED.getErrorMessage());
				}
			}
		} catch (ApisResourceAccessException | NoSuchAlgorithmException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode()
							+ ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorMessage()
							+ ExceptionUtils.getStackTrace(e));
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.API_NOT_AVAILABLE,
					"Request for auth" + authLockOrUnLockRequestDtoV2.getAuthTypes() + " lock failed"));
			throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorMessage(), e);
		} finally {
			NotificationTemplateCode templateCode = null;
			for (AuthTypeStatusDto authTypeStatusDto : authLockOrUnLockRequestDtoV2.getAuthTypes()) {
				if (authTypeStatusDto.getLocked()) {
					templateCode = isTransactionSuccessful ? NotificationTemplateCode.RS_LOCK_AUTH_SUCCESS
							: NotificationTemplateCode.RS_LOCK_AUTH_FAILURE;
				} else {
					templateCode = isTransactionSuccessful ? NotificationTemplateCode.RS_UNLOCK_AUTH_SUCCESS
							: NotificationTemplateCode.RS_UNLOCK_AUTH_FAILURE;
				}
			}
			NotificationResponseDTO notificationResponseDTO = sendNotification(individualId, templateCode,
					null);
			if (isTransactionSuccessful)
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_SUCCESS,
						"Request for auth " + authLockOrUnLockRequestDtoV2.getAuthTypes() + " lock success"));
			else
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
						"Request for auth " + authLockOrUnLockRequestDtoV2.getAuthTypes() + " lock failed"));
			if (notificationResponseDTO != null) {
				response.setMessage(notificationResponseDTO.getMessage());
			}
		}
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(), "ResidentServiceImpl::reqAauthTypeStatusUpdate():: exit");
		return response;
	}


	private void insertAuthStatusInDb(boolean isAuthSuccess, AuthLockOrUnLockRequestDtoV2 dto, String individualId) throws ResidentServiceCheckedException, NoSuchAlgorithmException, ApisResourceAccessException {
		ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();

		ArrayList<String> partnerIds =partnerService.getPartnerDetails("Online_Verification_Partner");

		for(String partner: partnerIds) {
			String id= individualId+partner;
			byte[] idBytes = id.getBytes();
			String hash = HMACUtils2.digestAsPlainText(idBytes);
			residentTransactionEntity.setEventId(UUID.randomUUID().toString());
			residentTransactionEntity.setAid(hash);
			residentTransactionEntity.setRequestDtimes(LocalDateTime.now());
			residentTransactionEntity.setResponseDtime(LocalDateTime.now());
			residentTransactionEntity.setRequestTypeCode(ResidentTransactionType.AUTHENTICATION_REQUEST.toString());
			residentTransactionEntity.setRequestSummary(ResidentTransactionType.AUTHENTICATION_REQUEST.toString());
			residentTransactionEntity.setAuthTypeCode(ResidentTransactionType.AUTHENTICATION_REQUEST.toString());
			residentTransactionEntity.setStatusCode("NEW");
			residentTransactionEntity.setStatusComment(isAuthSuccess ? "Success" : "Failure");
			residentTransactionEntity.setLangCode("eng");
			residentTransactionEntity.setRefIdType("");
			residentTransactionEntity.setTokenId(identityServiceImpl.getIDAToken(identityServiceImpl.getResidentIndvidualId()));
			residentTransactionEntity.setCrBy("RESIDENT");
			residentTransactionEntity.setCrDtimes(LocalDateTime.now());
			residentTransactionEntity.setOlvPartnerId(partner);
			residentTransactionRepository.save(residentTransactionEntity);
		}
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
		if (individualType.equalsIgnoreCase(IdType.UIN.name())) {
			return IdType.UIN;
		}
		if (individualType.equalsIgnoreCase(IdType.VID.name())) {
			return IdType.VID;
		}
		if (individualType.equalsIgnoreCase(IdType.RID.name())) {
			return IdType.RID;
		}
		return null;
	}

	private String getPublicKeyFromKeyManager() throws ApisResourceAccessException {
		PacketSignPublicKeyRequestDTO signKeyRequestDto = PacketSignPublicKeyRequestDTO.builder()
				.request(PacketSignPublicKeyRequestDTO.PacketSignPublicKeyRequest.builder()
						.serverProfile(SERVER_PROFILE_SIGN_KEY).build())
				.build();
		PacketSignPublicKeyResponseDTO signKeyResponseDTO;
		try {
			HttpEntity<PacketSignPublicKeyRequestDTO> httpEntity = new HttpEntity<>(signKeyRequestDto);
			signKeyResponseDTO = residentServiceRestClient.postApi(env.getProperty(ApiName.PACKETSIGNPUBLICKEY.name()),
					MediaType.APPLICATION_JSON, httpEntity, PacketSignPublicKeyResponseDTO.class);
			logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
					SERVER_PROFILE_SIGN_KEY,
					"ResidentServiceImpl::reqUinUpdate():: PACKETSIGNPUBLICKEY POST service call ended with response data "
							+ signKeyResponseDTO.toString());
			if (signKeyResponseDTO.getErrors() != null && !signKeyResponseDTO.getErrors().isEmpty()) {
				throw new ResidentServiceTPMSignKeyException(signKeyResponseDTO.getErrors().get(0).getErrorCode(),
						signKeyResponseDTO.getErrors().get(0).getMessage());
			}
			if (signKeyResponseDTO.getResponse() == null) {
				throw new ResidentServiceTPMSignKeyException(PACKET_SIGNKEY_EXCEPTION.getErrorCode(),
						PACKET_SIGNKEY_EXCEPTION.getErrorMessage());
			}
		} catch (Exception e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
					SERVER_PROFILE_SIGN_KEY,
					"ResidentServiceImpl::reqUinUpdate():: PACKETSIGNPUBLICKEY POST service call"
							+ ExceptionUtils.getStackTrace(e));
			throw new ApisResourceAccessException("Could not fetch public key from kernel keymanager", e);
		}
		return signKeyResponseDTO.getResponse().getPublicKey();
	}

	private MachineSearchResponseDTO searchMachineInMasterService(String residentMachinePrefix, String publicKey)
			throws ApisResourceAccessException {
		MachineSearchRequestDTO.MachineSearchFilter searchFilterName = MachineSearchRequestDTO.MachineSearchFilter
				.builder().columnName("name").type("contains").value(residentMachinePrefix).build();
		MachineSearchRequestDTO.MachineSearchFilter searchFilterPublicKey = MachineSearchRequestDTO.MachineSearchFilter
				.builder().columnName("signPublicKey").type("equals").value(publicKey).build();
		MachineSearchRequestDTO.MachineSearchSort searchSort = MachineSearchRequestDTO.MachineSearchSort.builder()
				.sortType("desc").sortField("createdDateTime").build();
		MachineSearchRequestDTO machineSearchRequestDTO = MachineSearchRequestDTO.builder()
				.version("1.0")
				// .requesttime(DateUtils.getUTCCurrentDateTimeString()) //TODO fix this
				.request(MachineSearchRequestDTO.MachineSearchRequest.builder()
						.filters(List.of(searchFilterName, searchFilterPublicKey))
						.sort(List.of(searchSort))
						.pagination(MachineSearchRequestDTO.MachineSearchPagination.builder().pageStart(0).pageFetch(10)
								.build())
						.languageCode(utilities.getLanguageCode())
						.build())
				.build();
		MachineSearchResponseDTO machineSearchResponseDTO;
		try {
			HttpEntity<MachineSearchRequestDTO> httpEntity = new HttpEntity<>(machineSearchRequestDTO);
			machineSearchResponseDTO = residentServiceRestClient.postApi(env.getProperty(ApiName.MACHINESEARCH.name()), MediaType.APPLICATION_JSON, httpEntity, MachineSearchResponseDTO.class);
			logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), residentMachinePrefix,
					"ResidentServiceImpl::reqUinUpdate():: MACHINESEARCH POST service call ended with response data "
							+ machineSearchResponseDTO.toString());
			if (machineSearchResponseDTO.getErrors() != null && !machineSearchResponseDTO.getErrors().isEmpty()) {
				throw new ResidentMachineServiceException(machineSearchResponseDTO.getErrors().get(0).getErrorCode(),
						machineSearchResponseDTO.getErrors().get(0).getMessage());
			}
		} catch (Exception e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
					residentMachinePrefix,
					"ResidentServiceImpl::reqUinUpdate():: MACHINESEARCH POST service call"
							+ ExceptionUtils.getStackTrace(e));
			throw new ApisResourceAccessException("Could not fetch machines from master data", e);
		}
		return machineSearchResponseDTO;
	}

	private String getMachineId(MachineSearchResponseDTO machineSearchResponseDTO, final String publicKey) {
		if (machineSearchResponseDTO.getResponse() != null) {
			List<MachineDto> fetchedMachines = machineSearchResponseDTO.getResponse().getData();
			if (fetchedMachines != null && !fetchedMachines.isEmpty()) {
				List<MachineDto> machines = fetchedMachines.stream()
						.filter(mac -> mac.getSignPublicKey().equals(publicKey)).collect(Collectors.toList());
				if (!machines.isEmpty()) {
					return machines.get(0).getId();
				}
			}
		}
		return null;
	}

	private String createNewMachineInMasterService(String residentMachinePrefix, String machineSpecId, String zoneCode,
			String regCenterId, String publicKey) throws ApisResourceAccessException {
		MachineCreateRequestDTO machineCreateRequestDTO = MachineCreateRequestDTO.builder()
				// .requesttime(DateUtils.getUTCCurrentDateTimeString()) //TODO fix this
				.request(MachineDto.builder().serialNum(null).macAddress(null).ipAddress("0.0.0.0").isActive(true)
						.validityDateTime(DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime().plusYears(3)))
						.name(residentMachinePrefix + System.currentTimeMillis()).machineSpecId(machineSpecId)
						.zoneCode(zoneCode).regCenterId(regCenterId).publicKey(publicKey).signPublicKey(publicKey)
						.build())
				.build();
		MachineCreateResponseDTO machineCreateResponseDTO;
		try {
			HttpEntity<MachineCreateRequestDTO> httpEntity = new HttpEntity<>(machineCreateRequestDTO);
			machineCreateResponseDTO = residentServiceRestClient.postApi(env.getProperty(ApiName.MACHINECREATE.name()),
					MediaType.APPLICATION_JSON, httpEntity, MachineCreateResponseDTO.class);
			logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
					residentMachinePrefix,
					"ResidentServiceImpl::reqUinUpdate():: MACHINECREATE POST service call ended with response data "
							+ machineCreateResponseDTO.toString());
			if (machineCreateResponseDTO.getErrors() != null && !machineCreateResponseDTO.getErrors().isEmpty()) {
				throw new ResidentMachineServiceException(machineCreateResponseDTO.getErrors().get(0).getErrorCode(),
						machineCreateResponseDTO.getErrors().get(0).getMessage());
			}
			if (machineCreateResponseDTO.getResponse() == null) {
				throw new ResidentMachineServiceException(MACHINE_MASTER_CREATE_EXCEPTION.getErrorCode(),
						MACHINE_MASTER_CREATE_EXCEPTION.getErrorMessage());
			}
		} catch (Exception e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
					residentMachinePrefix,
					"ResidentServiceImpl::reqUinUpdate():: MACHINECREATE POST service call"
							+ ExceptionUtils.getStackTrace(e));
			throw new ApisResourceAccessException("Could not create machine in master data", e);
		}
		return machineCreateResponseDTO.getResponse().getId();
	}

	private void validateAuthIndividualIdWithUIN(String individualId, String individualIdType,
			JSONObject mappingJsonObject, JSONObject demographicIdentity)
			throws ApisResourceAccessException, ValidationFailedException, IOException {
		String uin = "";
		if (ResidentIndividialIDType.UIN.toString().equals(individualIdType))
			uin = individualId;
		else if (ResidentIndividialIDType.VID.toString().equals(individualIdType)) {
			uin = utilities.getUinByVid(individualId);
		} else {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					"ResidentServiceImpl::validateAuthIndividualIdWithUIN():: Individual id type is invalid");
			throw new ValidationFailedException(ResidentErrorCode.INDIVIDUAL_ID_TYPE_INVALID.getErrorCode(),
					ResidentErrorCode.INDIVIDUAL_ID_TYPE_INVALID.getErrorMessage());
		}
		JSONObject identityMappingJsonObject = JsonUtil.getJSONObject(mappingJsonObject, IDENTITY);
		String uinMapping = getDocumentName(identityMappingJsonObject, UIN);
		String identityJsonUIN = JsonUtil.getJSONValue(demographicIdentity, uinMapping);
		if (!identityJsonUIN.equals(uin)) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					"ResidentServiceImpl::validateAuthIndividualIdWithUIN():: Validation failed");
			throw new ValidationFailedException(ResidentErrorCode.INDIVIDUAL_ID_UIN_MISMATCH.getErrorCode(),
					ResidentErrorCode.INDIVIDUAL_ID_UIN_MISMATCH.getErrorMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseWrapper<Object> getAuthLockStatus(String individualId) throws ResidentServiceCheckedException {
		try {
			ResponseWrapper<Object> response = (ResponseWrapper<Object>) residentServiceRestClient.getApi(
					ApiName.AUTHTYPESTATUSUPDATE, List.of(individualId), List.of(), List.of(), ResponseWrapper.class);
			if (Objects.nonNull(response.getErrors()) && !response.getErrors().isEmpty()) {
				throw new ResidentServiceCheckedException(ResidentErrorCode.AUTH_LOCK_STATUS_FAILED);
			}
			return response;
		} catch (ApisResourceAccessException e) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
	}

	@Override
	public List<ServiceHistoryResponseDto> getServiceHistory(Integer pageStart, Integer pageFetch, LocalDateTime fromDateTime,
															 LocalDateTime toDateTime, String serviceType, String sortType, String searchColumn, String searchText) throws ResidentServiceCheckedException, ApisResourceAccessException {

		if(pageStart == null) {
			if(pageFetch == null) {
				//If both Page start and page fetch values are null return all records
				pageStart = DEFAULT_PAGE_START;
				pageFetch = DEFAULT_PAGE_COUNT;
			} else {
				pageStart = DEFAULT_PAGE_START;
			}
		} else {
			if(pageFetch == null) {
				pageFetch = DEFAULT_PAGE_COUNT;
			}
		}
		if(pageStart < 0) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.INVALID_PAGE_START_VALUE);
		} else if(pageFetch < 0) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.INVALID_PAGE_FETCH_VALUE);
		}
		PageRequest pageRequest = null;
		if(sortType == null) {
			pageRequest = PageRequest.of(pageStart-1, pageFetch, Sort.by(Sort.Direction.ASC, "crDtimes"));
		} else if(sortType.equalsIgnoreCase(SortType.ASC.toString())) {
			pageRequest = PageRequest.of(pageStart-1, pageFetch, Sort.by(Sort.Direction.ASC, "crDtimes"));
		} else if(sortType.equalsIgnoreCase(SortType.DESC.toString())) {
			pageRequest = PageRequest.of(pageStart-1, pageFetch, Sort.by(Sort.Direction.DESC, "crDtimes"));
		}
		List<ServiceHistoryResponseDto> serviceHistoryResponseDtoList = getServiceHistoryForEachPartner(pageRequest, fromDateTime, toDateTime, serviceType, searchColumn, searchText);
		return serviceHistoryResponseDtoList;
	}

	@Override
	public List<ResidentServiceHistoryResponseDto> getServiceRequestUpdate(Integer pageStart, Integer pageFetch) throws ResidentServiceCheckedException{
		return getServiceRequestUpdate(pageStart, pageFetch, null);
	}

	@Override
	public List<ResidentServiceHistoryResponseDto> getServiceRequestUpdate(Integer pageStart, Integer pageFetch, String individualId) throws ResidentServiceCheckedException {
		logger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(), "ResidentServiceImpl::getServiceRequestUpdate()::entry");
		ResponseDTO responseDTO = new ResponseDTO();
		List<ResidentServiceHistoryResponseDto> residentServiceHistoryResponseDtoList = new ArrayList<>();

		try{
			if(pageStart == null) {
				if(pageFetch == null) {
					pageStart = DEFAULT_PAGE_START;
					pageFetch = DEFAULT_PAGE_COUNT;
				} else {
					pageStart = DEFAULT_PAGE_START;
				}
			} else {
				if(pageFetch == null) {
					pageFetch = DEFAULT_PAGE_COUNT;
				}
			}
			if(pageStart < 0) {
				throw new ResidentServiceCheckedException(ResidentErrorCode.INVALID_PAGE_START_VALUE);
			} else if(pageFetch < 0) {
				throw new ResidentServiceCheckedException(ResidentErrorCode.INVALID_PAGE_FETCH_VALUE);
			}
			PageRequest pageRequest = PageRequest.of(pageStart-1, pageFetch);
			String idaToken = identityServiceImpl.getResidentIdaToken();
			List<ResidentTransactionEntity> residentTransactionEntities = residentTransactionRepository.findRequestIdByToken(idaToken, ResidentTransactionType.SERVICE_REQUEST.toString(),pageRequest);
			if(residentTransactionEntities != null) {
				for(ResidentTransactionEntity residentTransactionEntity: residentTransactionEntities) {
					String requestId = residentTransactionEntity.getAid();
					CredentialRequestStatusResponseDto credentialRequestStatusResponseDto = residentCredentialServiceImpl.getStatus(requestId);
					if(credentialRequestStatusResponseDto !=null) {
						if(credentialRequestStatusResponseDto.getStatusCode().equalsIgnoreCase("NEW")){
							insertServiceRequestInDb(credentialRequestStatusResponseDto);
						}
						residentServiceHistoryResponseDtoList.add(convertCredentialResponseDtoToServiceHistoryResponseDto(
								credentialRequestStatusResponseDto, individualId));
					}

				}
			}
		}
		catch (ResidentServiceCheckedException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), "ResidentServiceImpl::getServiceRequestUpdate()::Exception");
			throw new ResidentServiceCheckedException(ResidentErrorCode.REQUEST_ID_NOT_FOUND.getErrorCode(),
					ResidentErrorCode.REQUEST_ID_NOT_FOUND.getErrorMessage());

		}
		catch (ApisResourceAccessException e) {
			audit.setAuditRequestDto(EventEnum.IDA_TOKEN_NOT_FOUND);
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		return residentServiceHistoryResponseDtoList;
	}

	@Override
	public List<ResidentServiceHistoryResponseDto> downloadCard(String individualId, String idType) throws ResidentServiceCheckedException {
		return getServiceRequestUpdate(null, null, individualId);
	}

	private void insertServiceRequestInDb(CredentialRequestStatusResponseDto credentialRequestStatusResponseDto) throws ApisResourceAccessException {
		ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
		residentTransactionEntity.setEventId(UUID.randomUUID().toString());
		residentTransactionEntity.setAid(credentialRequestStatusResponseDto.getRequestId());
		residentTransactionEntity.setRequestDtimes(LocalDateTime.now());
		residentTransactionEntity.setResponseDtime(LocalDateTime.now());
		residentTransactionEntity.setRequestTypeCode(ResidentTransactionType.SERVICE_REQUEST.toString());
		residentTransactionEntity.setRequestSummary(ResidentTransactionType.SERVICE_REQUEST.toString());
		residentTransactionEntity.setAuthTypeCode(ResidentTransactionType.SERVICE_REQUEST.toString());
		residentTransactionEntity.setStatusCode(credentialRequestStatusResponseDto.getStatusCode());
		residentTransactionEntity.setStatusComment(credentialRequestStatusResponseDto.getStatusCode());
		residentTransactionEntity.setLangCode("eng");
		residentTransactionEntity.setRefIdType("");
		residentTransactionEntity.setTokenId(identityServiceImpl.getIDAToken(identityServiceImpl.getResidentIndvidualId()));
		residentTransactionEntity.setCrBy("RESIDENT");
		residentTransactionEntity.setCrDtimes(LocalDateTime.now());
		residentTransactionEntity.setOlvPartnerId("");
		residentTransactionRepository.save(residentTransactionEntity);
	}

	public ResidentServiceHistoryResponseDto convertCredentialResponseDtoToServiceHistoryResponseDto(CredentialRequestStatusResponseDto credentialRequestStatusResponseDto, String individualId) {
		ResidentServiceHistoryResponseDto residentServiceHistoryResponseDto = new ResidentServiceHistoryResponseDto();
		residentServiceHistoryResponseDto.setRequestId(credentialRequestStatusResponseDto.getRequestId());
		residentServiceHistoryResponseDto.setStatusCode(credentialRequestStatusResponseDto.getStatusCode());
		residentServiceHistoryResponseDto.setId(credentialRequestStatusResponseDto.getId());
		if(credentialRequestStatusResponseDto.getStatusCode().equalsIgnoreCase(PRINTING)) {
			residentServiceHistoryResponseDto.setCardUrl(env.getProperty(ApiName.RESIDENT_REQ_CREDENTIAL_URL.name())+credentialRequestStatusResponseDto.getRequestId());
			}else if(credentialRequestStatusResponseDto.getStatusCode().equalsIgnoreCase(AVAILABLE)) {
			residentServiceHistoryResponseDto.setCardUrl(env.getProperty(ApiName.DIGITAL_CARD_STATUS_URL.name())+individualId);
		} else{
			residentServiceHistoryResponseDto.setCardUrl("");
		}
		return residentServiceHistoryResponseDto;
	}

	private List<ServiceHistoryResponseDto> getServiceHistoryForEachPartner(PageRequest pageRequest, LocalDateTime fromDateTime, LocalDateTime toDateTime, String serviceType, String searchColumn, String searchText) throws ResidentServiceCheckedException, ApisResourceAccessException {
		List<String> residentTransactionTypeList = convertServiceTypeToResidentTransactionType(serviceType);
		List<List<ResidentTransactionEntity>> residentTransactionEntityLists = new ArrayList<>();
		ArrayList<String> partnerIds= partnerServiceImpl.getPartnerDetails("Online_Verification_Partner");
		if(searchText==null) {
			searchText="";
		}
		if(partnerIds != null) {
			for (String partnerId : partnerIds) {
				String idaToken = identityServiceImpl.getIDAToken(identityServiceImpl.getResidentIndvidualId(), partnerId);
				if (idaToken != null) {
					if (fromDateTime != null && toDateTime != null) {
						residentTransactionEntityLists.add(residentTransactionRepository.
								findByToken(idaToken, fromDateTime, toDateTime, residentTransactionTypeList
										, pageRequest, searchText));
					} else {
						residentTransactionEntityLists.add(residentTransactionRepository.findByTokenWithoutDate(idaToken, residentTransactionTypeList, pageRequest, searchText));
					}
				}
			}
		}
		List<ResidentTransactionEntity> residentTransactionEntityList = residentTransactionEntityLists.stream().flatMap(List::stream).collect(Collectors.toList());
		return convertResidentEntityListToServiceHistoryDto(residentTransactionEntityList);
	}

	private List<String> convertServiceTypeToResidentTransactionType(String serviceType) {
		List<String> residentTransactionTypeList = new ArrayList<>();
		if(serviceType == null) {
			residentTransactionTypeList.addAll(Arrays.asList(AUTHENTICATION_REQUEST.values()).stream().map(Enum::name).collect(Collectors.toList()));
			residentTransactionTypeList.addAll(Arrays.asList(SERVICE_REQUEST.values()).stream().map(Enum::name).collect(Collectors.toList()));
			residentTransactionTypeList.addAll(Arrays.asList(DATA_UPDATE_REQUEST.values()).stream().map(Enum::name).collect(Collectors.toList()));
			residentTransactionTypeList.addAll(Arrays.asList(ID_MANAGEMENT_REQUEST.values()).stream().map(Enum::name).collect(Collectors.toList()));
			residentTransactionTypeList.addAll(Arrays.asList(DATA_SHARE_REQUEST.values()).stream().map(Enum::name).collect(Collectors.toList()));
		} else {
			List<String> serviceTypeList = List.of(serviceType.split(",")).stream().map(String::toUpperCase).collect(Collectors.toList());
			for (String service : serviceTypeList) {
				if(service.equalsIgnoreCase(ResidentTransactionType.AUTHENTICATION_REQUEST.toString())) {
					residentTransactionTypeList.addAll(Arrays.asList(AUTHENTICATION_REQUEST.values()).stream().map(Enum::name).collect(Collectors.toList()));
				} else if(service.equalsIgnoreCase(ResidentTransactionType.SERVICE_REQUEST.toString())) {
					residentTransactionTypeList.addAll(Arrays.asList(SERVICE_REQUEST.values()).stream().map(Enum::name).collect(Collectors.toList()));
				} else if(service.equalsIgnoreCase(ResidentTransactionType.DATA_UPDATE_REQUEST.toString())) {
					residentTransactionTypeList.addAll(Arrays.asList(DATA_UPDATE_REQUEST.values()).stream().map(Enum::name).collect(Collectors.toList()));
				} else if(service.equalsIgnoreCase(ResidentTransactionType.ID_MANAGEMENT_REQUEST.toString())) {
					residentTransactionTypeList.addAll(Arrays.asList(ID_MANAGEMENT_REQUEST.values()).stream().map(Enum::name).collect(Collectors.toList()));
				} else if(service.equalsIgnoreCase(ResidentTransactionType.DATA_SHARE_REQUEST.toString())) {
					residentTransactionTypeList.addAll(Arrays.asList(DATA_SHARE_REQUEST.values()).stream().map(Enum::name).collect(Collectors.toList()));
				}
			}
		}
		return residentTransactionTypeList;
	}

	private List<ServiceHistoryResponseDto> convertResidentEntityListToServiceHistoryDto(List<ResidentTransactionEntity> residentTransactionEntityList) {
		List<ServiceHistoryResponseDto> serviceHistoryResponseDtoList = new ArrayList<>();
		for (ResidentTransactionEntity residentTransactionEntity : residentTransactionEntityList) {
			ServiceHistoryResponseDto serviceHistoryResponseDto = new ServiceHistoryResponseDto();
			serviceHistoryResponseDto.setApplicationId(residentTransactionEntity.getEventId());
			serviceHistoryResponseDto.setAdditionalInformation(residentTransactionEntity.getStatusComment());
			serviceHistoryResponseDto.setStatus(residentTransactionEntity.getStatusCode());
			if(residentTransactionEntity.getUpdDtimes()!= null && residentTransactionEntity.getUpdDtimes().isAfter(residentTransactionEntity.getCrDtimes())) {
				serviceHistoryResponseDto.setTimeStamp(residentTransactionEntity.getUpdDtimes().toString());
			} else {
				serviceHistoryResponseDto.setTimeStamp(residentTransactionEntity.getCrDtimes().toString());
			}
			serviceHistoryResponseDtoList.add(serviceHistoryResponseDto);
		}
		return serviceHistoryResponseDtoList;
	}

	@Override
	public AidStatusResponseDTO getAidStatus(AidStatusRequestDTO reqDto) throws ResidentServiceCheckedException, ApisResourceAccessException, OtpValidationFailedException {
		return getAidStatus(reqDto, true);
	}

	@Override
	public AidStatusResponseDTO getAidStatus(AidStatusRequestDTO reqDto, boolean performOtpValidation) throws ResidentServiceCheckedException, ApisResourceAccessException, OtpValidationFailedException {
		try {
			String individualId = identityServiceImpl.getIndividualIdForAid(reqDto.getAid());
			boolean validStatus = individualId != null;
			if(performOtpValidation){
				validStatus = idAuthServiceImpl.validateOtp(reqDto.getTransactionID(), individualId, reqDto.getOtp());
			}
			if(validStatus) {
				AidStatusResponseDTO aidStatusResponseDTO = new AidStatusResponseDTO();
				aidStatusResponseDTO.setIndividualId(individualId);
				aidStatusResponseDTO.setAidStatus(PROCESSED);
				aidStatusResponseDTO.setTransactionID(reqDto.getTransactionID());
				return aidStatusResponseDTO;
			}
			throw new ResidentServiceCheckedException(ResidentErrorCode.AID_STATUS_IS_NOT_READY);
		} catch (ResidentServiceCheckedException | ApisResourceAccessException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					"ResidentServiceImpl::getAidStatus()::" + e.getClass().getSimpleName()+" :" + e.getMessage());
			RegStatusCheckResponseDTO ridStatus =null;
			try {
				ridStatus = getRidStatus(reqDto.getAid());
			} catch (RIDInvalidException ex) {
				throw new ResidentServiceCheckedException(ResidentErrorCode.AID_NOT_FOUND);
			}
			AidStatusResponseDTO aidStatusResponseDTO = new AidStatusResponseDTO();
			aidStatusResponseDTO.setAidStatus(ridStatus.getRidStatus());
			return aidStatusResponseDTO;
		}
	}

	@Override
	public String checkAidStatus(String aid) throws ResidentServiceCheckedException {

		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(), "ResidentServiceImpl::checkAidStatus()::Start");
		try{
			String uin = identityServiceImpl.getUinForIndividualId(aid);
			if(uin == null){
				throw new ResidentServiceCheckedException(ResidentErrorCode.AID_NOT_FOUND);
			}
			AidStatusRequestDTO aidStatusRequestDTO = new AidStatusRequestDTO();
			aidStatusRequestDTO.setAid(aid);
			AidStatusResponseDTO aidStatusResponseDTO = getAidStatus(aidStatusRequestDTO, false);
			return aidStatusResponseDTO.getAidStatus();
		} catch (ApisResourceAccessException | OtpValidationFailedException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					"ResidentServiceImpl::checkAidStatus():: ApisResourceAccessException");
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
	}

}