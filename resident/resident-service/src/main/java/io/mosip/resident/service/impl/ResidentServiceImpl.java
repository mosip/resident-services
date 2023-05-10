package io.mosip.resident.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.commons.khazana.exception.ObjectStoreAdapterException;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectValidationFailedException;
import io.mosip.kernel.core.idobjectvalidator.spi.IdObjectValidator;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.kernel.core.templatemanager.spi.TemplateManagerBuilder;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.constant.ConsentStatusType;
import io.mosip.resident.constant.EventStatus;
import io.mosip.resident.constant.EventStatusFailure;
import io.mosip.resident.constant.EventStatusInProgress;
import io.mosip.resident.constant.EventStatusSuccess;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.NotificationTemplateCode;
import io.mosip.resident.constant.RegistrationExternalStatusCode;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.constant.ServiceType;
import io.mosip.resident.constant.TemplateType;
import io.mosip.resident.constant.TemplateVariablesConstants;
import io.mosip.resident.dto.AidStatusRequestDTO;
import io.mosip.resident.dto.AidStatusResponseDTO;
import io.mosip.resident.dto.AuthHistoryRequestDTO;
import io.mosip.resident.dto.AuthHistoryResponseDTO;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDto;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDtoV2;
import io.mosip.resident.dto.AuthLockStatusResponseDtoV2;
import io.mosip.resident.dto.AuthTxnDetailsDTO;
import io.mosip.resident.dto.AuthTypeStatusDtoV2;
import io.mosip.resident.dto.AuthUnLockRequestDTO;
import io.mosip.resident.dto.BellNotificationDto;
import io.mosip.resident.dto.DocumentResponseDTO;
import io.mosip.resident.dto.EuinRequestDTO;
import io.mosip.resident.dto.EventStatusResponseDTO;
import io.mosip.resident.dto.MachineCreateRequestDTO;
import io.mosip.resident.dto.MachineCreateResponseDTO;
import io.mosip.resident.dto.MachineDto;
import io.mosip.resident.dto.MachineSearchRequestDTO;
import io.mosip.resident.dto.MachineSearchResponseDTO;
import io.mosip.resident.dto.NotificationRequestDto;
import io.mosip.resident.dto.NotificationRequestDtoV2;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.dto.PacketGeneratorResDto;
import io.mosip.resident.dto.PacketSignPublicKeyRequestDTO;
import io.mosip.resident.dto.PacketSignPublicKeyResponseDTO;
import io.mosip.resident.dto.PageDto;
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
import io.mosip.resident.dto.ResidentUpdateResponseDTOV2;
import io.mosip.resident.dto.ResponseDTO;
import io.mosip.resident.dto.ServiceHistoryResponseDto;
import io.mosip.resident.dto.SortType;
import io.mosip.resident.dto.UnreadNotificationDto;
import io.mosip.resident.dto.UserInfoDto;
import io.mosip.resident.entity.ResidentSessionEntity;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.entity.ResidentUserEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.CardNotReadyException;
import io.mosip.resident.exception.EventIdNotPresentException;
import io.mosip.resident.exception.InvalidRequestTypeCodeException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.RIDInvalidException;
import io.mosip.resident.exception.ResidentMachineServiceException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.exception.ResidentServiceTPMSignKeyException;
import io.mosip.resident.exception.ValidationFailedException;
import io.mosip.resident.handler.service.ResidentUpdateService;
import io.mosip.resident.handler.service.UinCardRePrintService;
import io.mosip.resident.repository.ResidentSessionRepository;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.repository.ResidentUserRepository;
import io.mosip.resident.service.DocumentService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.PartnerService;
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.service.ResidentService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.util.UINCardDownloadService;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.util.Utility;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import static io.mosip.resident.constant.EventStatusSuccess.CARD_DOWNLOADED;
import static io.mosip.resident.constant.EventStatusSuccess.LOCKED;
import static io.mosip.resident.constant.EventStatusSuccess.UNLOCKED;
import static io.mosip.resident.constant.MappingJsonConstants.IDSCHEMA_VERSION;
import static io.mosip.resident.constant.RegistrationConstants.UIN_LABEL;
import static io.mosip.resident.constant.ResidentConstants.ATTRIBUTE_LIST_DELIMITER;
import static io.mosip.resident.constant.ResidentConstants.PREFERRED_LANGUAGE;
import static io.mosip.resident.constant.ResidentConstants.RESIDENT;
import static io.mosip.resident.constant.ResidentConstants.RESIDENT_NOTIFICATIONS_DEFAULT_PAGE_SIZE;
import static io.mosip.resident.constant.ResidentErrorCode.MACHINE_MASTER_CREATE_EXCEPTION;
import static io.mosip.resident.constant.ResidentErrorCode.PACKET_SIGNKEY_EXCEPTION;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Service
public class ResidentServiceImpl implements ResidentService {

	private static final String NA = "NA";
	private static final String IDREPO_DUMMY_ONLINE_VERIFICATION_PARTNER_ID = "idrepo-dummy-online-verification-partner-id";
	private static final String ONLINE_VERIFICATION_PARTNER = "Online_Verification_Partner";
	private static final String MOSIP_IDA_PARTNER_TYPE = "mosip.ida.partner.type";
	private static final int UPDATE_COUNT_FOR_NEW_USER_ACTION_ENTITY = 1;
	private static final String AUTH_TYPE_SEPERATOR = "-";
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
	private static final String IMAGE = "mosip.resident.photo.token.claim-photo";
	private static final Logger logger = LoggerConfiguration.logConfig(ResidentServiceImpl.class);
	private static final Integer DEFAULT_PAGE_START = 0;
	private static final Integer DEFAULT_PAGE_COUNT = 10;
	private static final String CLASSPATH = "classpath";
	private static final String ENCODE_TYPE = "UTF-8";
	private static final String UPDATED = " updated";
	private static final String ALL = "ALL";
	private static final int EVENT_STATUS_LIST_LENGTH = 3;
	private static String cardType = "UIN";

	@Autowired
	private UINCardDownloadService uinCardDownloadService;

	@Autowired
	private ResidentUpdateService residentUpdateService;

	@Autowired
	private IdAuthService idAuthService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private PartnerService partnerService;

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
	private TemplateUtil templateUtil;

	@Autowired
	public Utility utility;

	@Autowired
	private Utilities utilities;

	@Autowired
	private EntityManager entityManager;

	@Value("${ida.online-verification-partner-id}")
	private String onlineVerificationPartnerId;

	/** The json validator. */
	@Autowired
	private IdObjectValidator idObjectValidator;

	@Autowired
	private ResidentConfigServiceImpl residentConfigService;

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

	@Value("${resident.service.history.id}")
	private String serviceHistoryId;

	@Value("${resident.service.history.version}")
	private String serviceHistoryVersion;

	@Value("${resident.service.event.id}")
	private String serviceEventId;

	@Value("${resident.service.event.version}")
	private String serviceEventVersion;

	@Value("${digital.card.pdf.encryption.enabled:false}")
	private boolean isDigitalCardPdfEncryptionEnabled;

	@Value("${"+ResidentConstants.PREFERRED_LANG_PROPERTY+":false}")
	private boolean isPreferedLangFlagEnabled;

	@Autowired
	private AuditUtil audit;

	@Autowired
	private DocumentService docService;

	@Autowired
	private IdAuthService idAuthServiceImpl;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ResidentUserRepository residentUserRepository;
	
	@Autowired
	private ResidentSessionRepository residentSessionRepository;

	@Value("${resident.service.unreadnotificationlist.id}")
	private String unreadnotificationlist;

	@Value("${mosip.registration.processor.rid.delimiter}")
	private String ridSuffix;

	private static String authTypes;

	@Autowired
	private ProxyMasterdataService proxyMasterdataService;

	private TemplateManager templateManager;

	@Autowired
	private TemplateManagerBuilder templateManagerBuilder;

	@PostConstruct
	public void idTemplateManagerPostConstruct() {
		templateManager = templateManagerBuilder.encodingType(ENCODE_TYPE).enableCache(false).resourceLoader(CLASSPATH)
				.build();
	}

	@Value("${auth.types.allowed}")
	public void setAuthTypes(String authType) {
		authTypes = authType;
	}

	public static String getAuthTypeBasedOnConfigV2(AuthTypeStatusDtoV2 authTypeStatus) {
		String[] authTypesArray = authTypes.split(",");
		for (String authType : authTypesArray) {
			if (authTypeStatus.getAuthSubType() != null) {
				String authTypeConcat = authTypeStatus.getAuthType() + AUTH_TYPE_SEPERATOR
						+ authTypeStatus.getAuthSubType();
				if (authType.equalsIgnoreCase(authTypeConcat)) {
					return authType;
				}
			} else {
				return authTypeStatus.getAuthType();
			}
		}
		return null;
	}

	public static String getAuthTypeBasedOnConfig(String inputAuthType) {
		String[] authTypesArray = authTypes.split(",");
		for (String authType : authTypesArray) {
			if (authType.equalsIgnoreCase(inputAuthType)) {
				return authType;
			}
		}
		return null;
	}

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
				List<String> authTypes = new ArrayList<String>();
				if (dto.getAuthType() != null && !dto.getAuthType().isEmpty()) {
					for (String authType : dto.getAuthType()) {
						String authTypeString = getAuthTypeBasedOnConfig(authType);
						authTypes.add(authTypeString);
					}
				}
				if (authTypeStatus.equals(AuthTypeStatus.UNLOCK)) {
					AuthUnLockRequestDTO authUnLockRequestDTO = (AuthUnLockRequestDTO) dto;
					unlockForSeconds = Long.parseLong(authUnLockRequestDTO.getUnlockForSeconds());
				}
				boolean isAuthTypeStatusUpdated = idAuthService.authTypeStatusUpdate(dto.getIndividualId(), authTypes,
						authTypeStatus, unlockForSeconds);
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

	private NotificationResponseDTO sendNotificationV2(String id, RequestType requestType, TemplateType templateType,
			String eventId, Map<String, Object> additionalAttributes) throws ResidentServiceCheckedException {

		NotificationRequestDtoV2 notificationRequestDtoV2 = new NotificationRequestDtoV2();
		notificationRequestDtoV2.setId(id);
		notificationRequestDtoV2.setRequestType(requestType);
		notificationRequestDtoV2.setTemplateType(templateType);
		notificationRequestDtoV2.setEventId(eventId);
		notificationRequestDtoV2.setAdditionalAttributes(additionalAttributes);
		return notificationService.sendNotification(notificationRequestDtoV2);
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
	public Tuple2<Object, String> reqUinUpdate(ResidentUpdateRequestDto dto) throws ResidentServiceCheckedException {
		byte[] decodedDemoJson = CryptoUtil.decodeURLSafeBase64(dto.getIdentityJson());
		JSONObject demographicJsonObject;
		try {
			demographicJsonObject = JsonUtil.readValue(new String(decodedDemoJson), JSONObject.class);
			JSONObject demographicIdentity = JsonUtil.getJSONObject(demographicJsonObject, IDENTITY);
			return reqUinUpdate(dto, demographicIdentity, false);
		} catch (IOException e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.IO_EXCEPTION, dto.getTransactionID(),
					"Request for UIN update"));

			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					dto.getTransactionID(), "Request for UIN update"));
			throw new ResidentServiceException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		}
	}

	@Override
	public Tuple2<Object, String> reqUinUpdate(ResidentUpdateRequestDto dto, JSONObject demographicIdentity, boolean validateIdObject)
			throws ResidentServiceCheckedException {
		Object responseDto = null;
		ResidentUpdateResponseDTO residentUpdateResponseDTO = null;
		ResidentUpdateResponseDTOV2 residentUpdateResponseDTOV2 = null;
		String eventId = null;
		ResidentTransactionEntity residentTransactionEntity = null;
		try {
			if (Utility.isSecureSession()) {
				demographicIdentity = getLanguageNameBasedOnFlag(demographicIdentity);
				residentUpdateResponseDTOV2 = new ResidentUpdateResponseDTOV2();
				responseDto = residentUpdateResponseDTOV2;
				residentTransactionEntity = createResidentTransEntity(dto);
				if (residentTransactionEntity != null) {
	    			eventId = residentTransactionEntity.getEventId();
	    		}
				if (dto.getConsent() == null || dto.getConsent().equalsIgnoreCase(ConsentStatusType.DENIED.name())
						|| dto.getConsent().trim().isEmpty() || dto.getConsent().equals("null")
						|| !dto.getConsent().equalsIgnoreCase(ConsentStatusType.ACCEPTED.name())) {
					residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
					residentTransactionEntity.setRequestSummary("failed");
					throw new ResidentServiceException(ResidentErrorCode.CONSENT_DENIED,
							Map.of(ResidentConstants.EVENT_ID, eventId));
				}
			} else {
				residentUpdateResponseDTO = new ResidentUpdateResponseDTO();
				responseDto = residentUpdateResponseDTO;
			}
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
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(IDENTITY, demographicIdentity);
			String encodedIdentityJson = CryptoUtil.encodeToURLSafeBase64(jsonObject.toJSONString().getBytes());
			regProcReqUpdateDto.setIdentityJson(encodedIdentityJson);
			String mappingJson = utility.getMappingJson();

			if(validateIdObject) {
				JSONObject obj = utilities.retrieveIdrepoJson(dto.getIndividualId());
				String idSchemaVersionStr = String.valueOf(obj.get("IDSchemaVersion"));
				Double idSchemaVersion = Double.parseDouble(idSchemaVersionStr);
				ResponseWrapper<?> idSchemaResponse = proxyMasterdataService.getLatestIdSchema(idSchemaVersion, null, null);
				Object idSchema = idSchemaResponse.getResponse();
				Map<String, ?> map = objectMapper.convertValue(idSchema, Map.class);
				String schemaJson = (String) map.get("schemaJson");
				try {
					idObjectValidator.validateIdObject(schemaJson, jsonObject);
				} catch (IdObjectValidationFailedException e) {
					Optional<String> error = e.getErrorTexts().stream()
							.filter(t -> t.contains(ResidentConstants.INVALID_INPUT_PARAMETER)).findAny();
					if (error.isPresent()) {
						String errorMessage = error.get();
						throw new ResidentServiceException(ResidentErrorCode.INVALID_INPUT.getErrorCode(),
								errorMessage);
					}
				}
			}
			
			if (demographicIdentity == null || demographicIdentity.isEmpty() || mappingJson == null
					|| mappingJson.trim().isEmpty()) {
				audit.setAuditRequestDto(
						EventEnum.getEventEnumWithValue(EventEnum.JSON_PARSING_EXCEPTION, dto.getTransactionID()));
				if (Utility.isSecureSession()) {
					throw new ResidentServiceException(ResidentErrorCode.JSON_PROCESSING_EXCEPTION,
							Map.of(ResidentConstants.EVENT_ID, eventId));
				} else {
					throw new ResidentServiceException(ResidentErrorCode.JSON_PROCESSING_EXCEPTION.getErrorCode(),
							ResidentErrorCode.JSON_PROCESSING_EXCEPTION.getErrorMessage());
				}
			}
			JSONObject mappingJsonObject = JsonUtil.readValue(mappingJson, JSONObject.class);
			validateAuthIndividualIdWithUIN(dto.getIndividualId(), dto.getIndividualIdType(), mappingJsonObject,
					demographicIdentity);
			JSONObject mappingDocument = JsonUtil.getJSONObject(mappingJsonObject, DOCUMENT);
			List<ResidentDocuments> documents;
			if (Utility.isSecureSession()) {
				documents = getResidentDocuments(dto, mappingDocument);
			} else {
				documents = dto.getDocuments();
			}
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

			NotificationResponseDTO notificationResponseDTO;
			if (Utility.isSecureSession()) {
				updateResidentTransaction(residentTransactionEntity, response);
				notificationResponseDTO = sendNotificationV2(dto.getIndividualId(), RequestType.UPDATE_MY_UIN,
						TemplateType.REQUEST_RECEIVED, eventId, additionalAttributes);
				residentUpdateResponseDTOV2.setStatus(ResidentConstants.SUCCESS);
				residentUpdateResponseDTOV2.setMessage(notificationResponseDTO.getMessage());
			} else {
				notificationResponseDTO = sendNotification(dto.getIndividualId(),
						NotificationTemplateCode.RS_UIN_UPDATE_SUCCESS, additionalAttributes);
				if (residentUpdateResponseDTO != null) {
					residentUpdateResponseDTO.setMessage(notificationResponseDTO.getMessage());
					residentUpdateResponseDTO.setRegistrationId(response.getRegistrationId());
				}
			}
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_SUCCESS,
					dto.getTransactionID(), "Request for UIN update"));
		} catch (OtpValidationFailedException e) {
			sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_UIN_UPDATE_FAILURE, null);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.OTP_VALIDATION_FAILED,
					dto.getTransactionID(), "Request for UIN update"));

			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					dto.getTransactionID(), "Request for UIN update"));
			throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(), e.getErrorText(),
					e);

		} catch (ValidationFailedException e) {
			if (Utility.isSecureSession()) {
				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
				residentTransactionEntity.setRequestSummary("failed");
				sendNotificationV2(dto.getIndividualId(), RequestType.UPDATE_MY_UIN, TemplateType.FAILURE,
						eventId, null);
			} else {
				sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_UIN_UPDATE_FAILURE, null);
			}
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATION_FAILED_EXCEPTION,
					e.getMessage() + " Transaction id: " + dto.getTransactionID(), "Request for UIN update"));

			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					dto.getTransactionID(), "Request for UIN update"));
			if (Utility.isSecureSession()) {
				throw new ResidentServiceException(e.getErrorCode(), e.getMessage(), e,
						Map.of(ResidentConstants.EVENT_ID, eventId));
			} else {
				throw new ResidentServiceException(e.getErrorCode(), e.getMessage(), e);
			}

		} catch (ApisResourceAccessException e) {
			if (Utility.isSecureSession()) {
				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
				residentTransactionEntity.setRequestSummary("failed");
				sendNotificationV2(dto.getIndividualId(), RequestType.UPDATE_MY_UIN, TemplateType.FAILURE,
						eventId, null);
			} else {
				sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_UIN_UPDATE_FAILURE, null);
			}
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.API_RESOURCE_UNACCESS,
					dto.getTransactionID(), "Request for UIN update"));

			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					dto.getTransactionID(), "Request for UIN update"));
			if (e.getCause() instanceof HttpClientErrorException) {
				HttpClientErrorException httpClientException = (HttpClientErrorException) e.getCause();
				if (Utility.isSecureSession()) {
					throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
							httpClientException.getResponseBodyAsString(), e,
							Map.of(ResidentConstants.EVENT_ID, eventId));
				} else {
					throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
							httpClientException.getResponseBodyAsString());
				}
			} else if (e.getCause() instanceof HttpServerErrorException) {
				HttpServerErrorException httpServerException = (HttpServerErrorException) e.getCause();
				if (Utility.isSecureSession()) {
					throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
							httpServerException.getResponseBodyAsString(), e,
							Map.of(ResidentConstants.EVENT_ID, eventId));
				} else {
					throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
							httpServerException.getResponseBodyAsString());
				}
			} else {
				if (Utility.isSecureSession()) {
					throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
							ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage() + e.getMessage(), e,
							Map.of(ResidentConstants.EVENT_ID, eventId));
				} else {
					throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
							ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage() + e.getMessage(), e);
				}
			}
		} catch (IOException e) {
			if (Utility.isSecureSession()) {
				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
				residentTransactionEntity.setRequestSummary("failed");
				sendNotificationV2(dto.getIndividualId(), RequestType.UPDATE_MY_UIN, TemplateType.FAILURE,
						eventId, null);
			} else {
				sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_UIN_UPDATE_FAILURE, null);
			}
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.IO_EXCEPTION, dto.getTransactionID(),
					"Request for UIN update"));

			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					dto.getTransactionID(), "Request for UIN update"));
			if (Utility.isSecureSession()) {
				throw new ResidentServiceException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
						ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e,
						Map.of(ResidentConstants.EVENT_ID, eventId));
			} else {
				throw new ResidentServiceException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
						ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
			}
		} catch (ResidentServiceCheckedException e) {
			if (Utility.isSecureSession()) {
				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
				residentTransactionEntity.setRequestSummary("failed");
				sendNotificationV2(dto.getIndividualId(), RequestType.UPDATE_MY_UIN, TemplateType.FAILURE,
						eventId, null);
			} else {
				sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_UIN_UPDATE_FAILURE, null);
			}
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					dto.getTransactionID(), "Request for UIN update"));
			if (Utility.isSecureSession()) {
				throw new ResidentServiceException(
						ResidentErrorCode.NO_DOCUMENT_FOUND_FOR_TRANSACTION_ID.getErrorCode(),
						ResidentErrorCode.NO_DOCUMENT_FOUND_FOR_TRANSACTION_ID.getErrorMessage() + dto.getTransactionID(),
						e, Map.of(ResidentConstants.EVENT_ID, eventId));
			} else {
				throw new ResidentServiceException(ResidentErrorCode.NO_DOCUMENT_FOUND_FOR_TRANSACTION_ID.getErrorCode(),
						ResidentErrorCode.NO_DOCUMENT_FOUND_FOR_TRANSACTION_ID.getErrorMessage() + dto.getTransactionID(),
						e);
			}

		} catch (BaseCheckedException e) {
			if (Utility.isSecureSession()) {
				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
				residentTransactionEntity.setRequestSummary("failed");
				sendNotificationV2(dto.getIndividualId(), RequestType.UPDATE_MY_UIN, TemplateType.FAILURE,
						eventId, null);
			} else {
				sendNotification(dto.getIndividualId(), NotificationTemplateCode.RS_UIN_UPDATE_FAILURE, null);
			}
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.BASE_EXCEPTION, dto.getTransactionID(),
					"Request for UIN update"));

			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					dto.getTransactionID(), "Request for UIN update"));
			if (Utility.isSecureSession()) {
				throw new ResidentServiceException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(),
						ResidentErrorCode.BASE_EXCEPTION.getErrorMessage(), e,
						Map.of(ResidentConstants.EVENT_ID, eventId));
			} else {
				throw new ResidentServiceException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(),
						ResidentErrorCode.BASE_EXCEPTION.getErrorMessage(), e);
			}

		}

		finally {
			if (Utility.isSecureSession() && residentTransactionEntity != null) {
				// if the status code will come as null, it will set it as failed.
				if (residentTransactionEntity.getStatusCode() == null) {
					residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
				}
				if (residentTransactionEntity.getRequestSummary() == null) {
					residentTransactionEntity.setRequestSummary("failed");
				}
				residentTransactionRepository.save(residentTransactionEntity);
			}
		}
		if(eventId == null) {
			eventId = ResidentConstants.NOT_AVAILABLE;
		}
		return Tuples.of(responseDto, eventId);
	}

	private JSONObject getLanguageNameBasedOnFlag(JSONObject demographicIdentity) {
		String preferredLangValueInIdentityMapping ="";
		try {
			Map<String, Object> identityMappingMap = residentConfigService.getIdentityMappingMap();
			if(identityMappingMap.containsKey(PREFERRED_LANGUAGE)) {
				preferredLangValueInIdentityMapping = String.valueOf(((Map) identityMappingMap.get(PREFERRED_LANGUAGE)).get(VALUE));
			}
		} catch (ResidentServiceCheckedException | IOException e ) {
			throw new RuntimeException(e);
		}
		String preferredLang = (String) demographicIdentity.get(preferredLangValueInIdentityMapping);
		if(preferredLang ==null || preferredLang.isEmpty()){
			return demographicIdentity;
		}
		String preferredLangValue = "";
		if(isPreferedLangFlagEnabled){
			preferredLangValue = utility.getPreferredLanguageCodeForLanguageNameBasedOnFlag(preferredLangValueInIdentityMapping, preferredLang);
			if(preferredLangValue!=null && !preferredLangValue.isEmpty()){
				demographicIdentity.put(preferredLangValueInIdentityMapping, preferredLangValue);
			} else {
				throw new ResidentServiceException(ResidentErrorCode.INVALID_LANGUAGE_NAME, ResidentErrorCode.INVALID_LANGUAGE_NAME.getErrorMessage());
			}
		}
		return demographicIdentity;
	}

	private ResidentTransactionEntity createResidentTransEntity(ResidentUpdateRequestDto dto)
			throws ApisResourceAccessException, IOException, ResidentServiceCheckedException {
		ResidentTransactionEntity residentTransactionEntity = utility.createEntity();
		residentTransactionEntity.setEventId(utility.createEventId());
		residentTransactionEntity.setRequestTypeCode(RequestType.UPDATE_MY_UIN.name());
		residentTransactionEntity.setRefId(utility.convertToMaskDataFormat(dto.getIndividualId()));
		residentTransactionEntity.setIndividualId(dto.getIndividualId());
		residentTransactionEntity.setTokenId(identityServiceImpl.getResidentIdaToken());
		residentTransactionEntity.setAuthTypeCode(identityServiceImpl.getResidentAuthenticationMode());
		Map<String, ?> identityMap;
		if (dto.getIdentityJson() != null) {
			byte[] decodedIdJson = CryptoUtil.decodeURLSafeBase64(dto.getIdentityJson());
			identityMap = (Map<String, ?>) objectMapper.readValue(decodedIdJson, Map.class).get(IDENTITY);
		} else {
			identityMap = dto.getIdentity();
		}
		
		String attributeList = identityMap.keySet().stream()
				.filter(key -> !key.equals(IDSCHEMA_VERSION) && !key.equals(UIN_LABEL))
				.collect(Collectors.joining(ATTRIBUTE_LIST_DELIMITER));
		residentTransactionEntity.setAttributeList(attributeList);
		residentTransactionEntity.setConsent(dto.getConsent());
		residentTransactionEntity.setStatusCode(EventStatusInProgress.NEW.name());
		residentTransactionEntity.setStatusComment(attributeList+UPDATED);
		return residentTransactionEntity;
	}

	private void updateResidentTransaction(ResidentTransactionEntity residentTransactionEntity,
			PacketGeneratorResDto response) {
		String rid = response.getRegistrationId();
		residentTransactionEntity.setAid(rid);
		residentTransactionEntity.setCredentialRequestId(rid + ridSuffix);
		residentTransactionEntity.setStatusCode(EventStatusInProgress.NEW.name());
		residentTransactionEntity.setRequestSummary(EventStatusInProgress.NEW.name());
	}

	private List<ResidentDocuments> getResidentDocuments(ResidentUpdateRequestDto dto, JSONObject mappingDocument)
			throws ResidentServiceCheckedException {
		if (Objects.nonNull(dto.getDocuments())) {
			return dto.getDocuments();
		}
		if (dto.getTransactionID() == null) {
			return Collections.emptyList();
		}
		try {
			Map<DocumentResponseDTO, String> documentsWithMetadata = docService
					.getDocumentsWithMetadata(dto.getTransactionID());
			return documentsWithMetadata.entrySet().stream()
					.map(doc -> new ResidentDocuments(getDocumentName(mappingDocument, doc.getKey().getDocCatCode()),
							doc.getValue()))
					.collect(Collectors.toList());
		} catch (ResidentServiceCheckedException | ObjectStoreAdapterException e) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.FAILED_TO_RETRIEVE_DOC.getErrorCode(),
					ResidentErrorCode.FAILED_TO_RETRIEVE_DOC.getErrorMessage(), e);
		}
	}

	@Override
	public Tuple2<ResponseDTO, String> reqAauthTypeStatusUpdateV2(AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(), "ResidentServiceImpl::reqAauthTypeStatusUpdate():: entry");
		ResponseDTO response = new ResponseDTO();
		String individualId = identityServiceImpl.getResidentIndvidualIdFromSession();
		boolean isTransactionSuccessful = false;
		List<ResidentTransactionEntity> residentTransactionEntities = List.of();
		String eventId = ResidentConstants.NOT_AVAILABLE;
		try {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.REQ_AUTH_TYPE_LOCK, "Request for Auth Type Lock"));
			ArrayList<String> partnerIds = partnerService.getPartnerDetails(env.getProperty(MOSIP_IDA_PARTNER_TYPE,ONLINE_VERIFICATION_PARTNER));
			String dummyOnlineVerificationPartnerId = env.getProperty(IDREPO_DUMMY_ONLINE_VERIFICATION_PARTNER_ID, NA);
			residentTransactionEntities = partnerIds.stream()
					.filter(partnerId -> !dummyOnlineVerificationPartnerId.equalsIgnoreCase(partnerId))
					.map(partnerId -> {
				try {
					return createResidentTransactionEntity(individualId, partnerId);
				} catch (ApisResourceAccessException e) {
					logger.error("Error occured in creating entities %s", e.getMessage());
					throw new ResidentServiceException(ResidentErrorCode.UNKNOWN_EXCEPTION, e);
				} catch (ResidentServiceCheckedException e) {
					throw new RuntimeException(e);
				}
			}).collect(Collectors.toList());
			if (!residentTransactionEntities.isEmpty()) {
    			eventId = residentTransactionEntities.get(0).getEventId();
    		}

			List<AuthTypeStatusDtoV2> authTypesStatusList = authLockOrUnLockRequestDtoV2.getAuthTypes();
			String authType = authTypesStatusList.stream().map(dto ->ResidentServiceImpl.getAuthTypeBasedOnConfigV2(dto)
							+ResidentConstants.COLON+ (dto.getLocked()? LOCKED:UNLOCKED))
					.collect(Collectors.joining(ResidentConstants.AUTH_TYPE_LIST_DELIMITER));

			Map<String, AuthTypeStatus> authTypeStatusMap = authTypesStatusList.stream()
					.collect(Collectors.toMap(ResidentServiceImpl::getAuthTypeBasedOnConfigV2,
							dto -> dto.getLocked() ? AuthTypeStatus.LOCK : AuthTypeStatus.UNLOCK));

			Map<String, Long> unlockForSecondsMap = authTypesStatusList.stream()
					.filter(dto -> dto.getUnlockForSeconds() != null).collect(Collectors.toMap(
							ResidentServiceImpl::getAuthTypeBasedOnConfigV2, AuthTypeStatusDtoV2::getUnlockForSeconds));

			String requestId = idAuthService.authTypeStatusUpdateForRequestId(individualId, authTypeStatusMap,
					unlockForSecondsMap);

			residentTransactionEntities.forEach(residentTransactionEntity -> {
				if (requestId != null) {
					residentTransactionEntity.setRequestSummary(EventStatusInProgress.NEW.name());
					residentTransactionEntity.setPurpose(authType);
				} else {
					residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
					residentTransactionEntity.setRequestSummary(EventStatusFailure.FAILED.name());
				}
				residentTransactionEntity.setRequestTrnId(requestId);
			});

			if (requestId != null) {
				isTransactionSuccessful = true;
			} else {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REQUEST_FAILED,
						"Request for auth " + authLockOrUnLockRequestDtoV2.getAuthTypes() + " lock failed"));
				throw new ResidentServiceException(ResidentErrorCode.REQUEST_FAILED,
						Map.of(ResidentConstants.EVENT_ID, eventId));
			}

		} catch (ApisResourceAccessException e) {
			residentTransactionEntities.forEach(residentTransactionEntity -> {
				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
				residentTransactionEntity.setRequestSummary("failed");
			});

			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode()
							+ ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorMessage()
							+ ExceptionUtils.getStackTrace(e));
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.API_NOT_AVAILABLE,
					"Request for auth" + authLockOrUnLockRequestDtoV2.getAuthTypes() + " lock failed"));
			throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_UNAVAILABLE, e,
					Map.of(ResidentConstants.EVENT_ID, eventId));
		} finally {
			residentTransactionRepository.saveAll(residentTransactionEntities);

			RequestType requestType = RequestType.AUTH_TYPE_LOCK_UNLOCK;
			TemplateType templateType = isTransactionSuccessful ? TemplateType.REQUEST_RECEIVED : TemplateType.FAILURE;

			NotificationResponseDTO notificationResponseDTO = sendNotificationV2(individualId, requestType,
					templateType, eventId, null);

			if (isTransactionSuccessful) {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_SUCCESS,
						"Request for auth " + authLockOrUnLockRequestDtoV2.getAuthTypes() + " lock success"));
				response.setMessage("The chosen authentication types have been successfully locked/unlocked.");
			} else {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
						"Request for auth " + authLockOrUnLockRequestDtoV2.getAuthTypes() + " lock failed"));
				response.setMessage("The chosen authentication types haven't been successfully locked/unlocked.");
			}
			response.setStatus(ResidentConstants.SUCCESS);
		}
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(), "ResidentServiceImpl::reqAauthTypeStatusUpdate():: exit");
		return Tuples.of(response, eventId);
	}

	private ResidentTransactionEntity createResidentTransactionEntity(String individualId, String partnerId)
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResidentTransactionEntity residentTransactionEntity;
		residentTransactionEntity = utility.createEntity();
		residentTransactionEntity.setEventId(utility.createEventId());
		residentTransactionEntity.setStatusCode(EventStatusInProgress.NEW.name());
		residentTransactionEntity.setStatusComment(EventStatusInProgress.NEW.name());
		residentTransactionEntity.setRequestTypeCode(RequestType.AUTH_TYPE_LOCK_UNLOCK.name());
		residentTransactionEntity.setRequestSummary("Updating auth type lock status");
		residentTransactionEntity.setRefId(utility.convertToMaskDataFormat(individualId));
		residentTransactionEntity.setIndividualId(individualId);
		residentTransactionEntity.setTokenId(identityServiceImpl.getResidentIdaToken());
		residentTransactionEntity.setAuthTypeCode(identityServiceImpl.getResidentAuthenticationMode());
		residentTransactionEntity.setOlvPartnerId(partnerId);
		residentTransactionEntity.setStatusComment("Updating auth type lock status");
		residentTransactionEntity.setLangCode(this.env.getProperty(ResidentConstants.MANDATORY_LANGUAGE));
		residentTransactionEntity.setRefIdType(identityServiceImpl.getIndividualIdType(individualId));
		return residentTransactionEntity;
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
		MachineSearchRequestDTO machineSearchRequestDTO = MachineSearchRequestDTO.builder().version("1.0")
				// .requesttime(DateUtils.getUTCCurrentDateTimeString()) //TODO fix this
				.request(
						MachineSearchRequestDTO.MachineSearchRequest.builder()
								.filters(List.of(searchFilterName, searchFilterPublicKey)).sort(List.of(searchSort))
								.pagination(MachineSearchRequestDTO.MachineSearchPagination.builder().pageStart(0)
										.pageFetch(10).build())
								.languageCode(utilities.getLanguageCode()).build())
				.build();
		MachineSearchResponseDTO machineSearchResponseDTO;
		try {
			HttpEntity<MachineSearchRequestDTO> httpEntity = new HttpEntity<>(machineSearchRequestDTO);
			machineSearchResponseDTO = residentServiceRestClient.postApi(env.getProperty(ApiName.MACHINESEARCH.name()),
					MediaType.APPLICATION_JSON, httpEntity, MachineSearchResponseDTO.class);
			logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
					residentMachinePrefix,
					"ResidentServiceImpl::reqUinUpdate():: MACHINESEARCH POST service call ended with response data "
							+ machineSearchResponseDTO.toString());
			if (machineSearchResponseDTO.getErrors() != null && !machineSearchResponseDTO.getErrors().isEmpty()) {
				throw new ResidentMachineServiceException(machineSearchResponseDTO.getErrors().get(0).getErrorCode(),
						machineSearchResponseDTO.getErrors().get(0).getMessage());
			}
		} catch (Exception e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
					residentMachinePrefix, "ResidentServiceImpl::reqUinUpdate():: MACHINESEARCH POST service call"
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
					residentMachinePrefix, "ResidentServiceImpl::reqUinUpdate():: MACHINECREATE POST service call"
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
		if (Utility.isSecureSession()) {
			demographicIdentity.put(uinMapping, uin);
		}
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
	public ResponseWrapper<AuthLockOrUnLockRequestDtoV2> getAuthLockStatus(String individualId)
			throws ResidentServiceCheckedException {
		ResponseWrapper<AuthLockOrUnLockRequestDtoV2> response = new ResponseWrapper<>();
		try {
			ResponseWrapper<AuthLockStatusResponseDtoV2> responseWrapper = JsonUtil.convertValue(
					residentServiceRestClient.getApi(ApiName.AUTHTYPESTATUSUPDATE, List.of(individualId), List.of(),
							List.of(), ResponseWrapper.class),
					new TypeReference<ResponseWrapper<AuthLockStatusResponseDtoV2>>() {
					});
			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				throw new ResidentServiceCheckedException(ResidentErrorCode.AUTH_LOCK_STATUS_FAILED);
			}
			AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
			List<AuthTypeStatusDtoV2> dtoListV2;
			if (responseWrapper.getResponse().getAuthTypes().isEmpty()) {
				dtoListV2 = new ArrayList<>();
				AuthTypeStatusDtoV2 dtoV2;
				String[] authTypesArray = authTypes.split(",");
				for (String authType : authTypesArray) {
					String[] authSplitArray = authType.split("-");
					List<String> authTypeList = new ArrayList<String>(Arrays.asList(authSplitArray));
					dtoV2 = new AuthTypeStatusDtoV2();
					dtoV2.setAuthType(authTypeList.get(0));
					dtoV2.setAuthSubType(authTypeList.size() > 1 ? authTypeList.get(1) : null);
					dtoV2.setLocked(Boolean.FALSE);
					dtoV2.setUnlockForSeconds(null);
					dtoListV2.add(dtoV2);
				}
			} else {
				dtoListV2 = responseWrapper.getResponse().getAuthTypes().stream().map(dto -> {
					AuthTypeStatusDtoV2 dtoV2 = new AuthTypeStatusDtoV2();
					dtoV2.setAuthType(dto.getAuthType());
					dtoV2.setAuthSubType(dto.getAuthSubType());
					dtoV2.setLocked(dto.getLocked());
					dtoV2.setUnlockForSeconds(dto.getUnlockForSeconds());
					return dtoV2;
				}).collect(Collectors.toList());
			}
			authLockOrUnLockRequestDtoV2.setAuthTypes(dtoListV2);
			response.setResponse(authLockOrUnLockRequestDtoV2);
			return response;
		} catch (ApisResourceAccessException e) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
	}
	
	@Override
	public ResponseWrapper<PageDto<ServiceHistoryResponseDto>> getServiceHistory(Integer pageStart, Integer pageFetch,
			 LocalDate fromDateTime, LocalDate toDateTime, String serviceType, String sortType,
			 String statusFilter, String searchText, String langCode, int timeZoneOffset)
		throws ResidentServiceCheckedException, ApisResourceAccessException {
				return getServiceHistory(pageStart, pageFetch, fromDateTime, toDateTime, serviceType, sortType, statusFilter,
						searchText, langCode, timeZoneOffset, null, null);
	}
	
	@Override
	public ResponseWrapper<PageDto<ServiceHistoryResponseDto>> getServiceHistory(Integer pageStart, Integer pageFetch,
																				 LocalDate fromDateTime, LocalDate toDateTime, String serviceType, String sortType,
																				 String statusFilter, String searchText, String langCode, int timeZoneOffset,
																				 String defaultPageSizeProperty, List<String> statusCodeList)
			throws ResidentServiceCheckedException, ApisResourceAccessException {

		if (pageStart == null) {
			//By default page start is 0
			pageStart = DEFAULT_PAGE_START;
		}
		
		if (pageFetch == null) {
			// Get the default page size based on the property if mentioned otherwise it
			// default would be 10
			pageFetch = getDefaultPageSize(defaultPageSizeProperty);
		}
		
		if (pageStart < 0) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INVALID_PAGE_START_VALUE,
					pageStart.toString(), "Invalid page start value"));
			throw new ResidentServiceCheckedException(ResidentErrorCode.INVALID_PAGE_START_VALUE);
		} else if(pageFetch <=0){
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INVALID_PAGE_FETCH_VALUE,
					pageFetch.toString(), "Invalid Page fetch value"));
			throw new ResidentServiceCheckedException(ResidentErrorCode.INVALID_PAGE_FETCH_VALUE);
		}

		ResponseWrapper<PageDto<ServiceHistoryResponseDto>> serviceHistoryResponseDtoList = getServiceHistoryDetails(
				sortType, pageStart, pageFetch, fromDateTime, toDateTime, serviceType, statusFilter, searchText,
				langCode, timeZoneOffset, statusCodeList);
		return serviceHistoryResponseDtoList;
	}

	private Integer getDefaultPageSize(String defaultPageSizeProperty) {
		return defaultPageSizeProperty != null
				? env.getProperty(defaultPageSizeProperty, Integer.class, DEFAULT_PAGE_COUNT)
				: DEFAULT_PAGE_COUNT;
	}

	@Override
	public String getFileName(String eventId, int timeZoneOffset) {
		if (cardType.equalsIgnoreCase(IdType.UIN.toString())) {
			return utility.getFileName(eventId, Objects
					.requireNonNull(this.env.getProperty(ResidentConstants.UIN_CARD_NAMING_CONVENTION_PROPERTY)), timeZoneOffset);
		} else {
			return utility.getFileName(eventId, Objects
					.requireNonNull(this.env.getProperty(ResidentConstants.VID_CARD_NAMING_CONVENTION_PROPERTY)), timeZoneOffset);
		}
	}

	@Override
	public byte[] downloadCard(String eventId) {
		try {
			Optional<ResidentTransactionEntity> residentTransactionEntity = residentTransactionRepository
					.findById(eventId);
			if (residentTransactionEntity.isPresent()) {
				String requestTypeCode = residentTransactionEntity.get().getRequestTypeCode();
				RequestType requestType = RequestType.getRequestTypeFromString(requestTypeCode);
				if (requestType.name().equalsIgnoreCase(RequestType.UPDATE_MY_UIN.name())) {
					cardType = IdType.UIN.name();
					return downloadCardFromDataShareUrl(residentTransactionEntity.get());
				} else if (requestType.name().equalsIgnoreCase(RequestType.VID_CARD_DOWNLOAD.toString())
				|| requestType.name().equalsIgnoreCase(RequestType.GET_MY_ID.name())) {
					cardType = IdType.VID.name();
					return downloadCardFromDataShareUrl(residentTransactionEntity.get());
				} else {
					throw new InvalidRequestTypeCodeException(ResidentErrorCode.INVALID_REQUEST_TYPE_CODE.toString(),
							ResidentErrorCode.INVALID_REQUEST_TYPE_CODE.getErrorMessage());
				}
			} else {
				throw new EventIdNotPresentException(ResidentErrorCode.EVENT_STATUS_NOT_FOUND.toString(),
						ResidentErrorCode.EVENT_STATUS_NOT_FOUND.getErrorMessage());
			}
		} catch (EventIdNotPresentException e) {
			audit.setAuditRequestDto(EventEnum.IDA_TOKEN_NOT_FOUND);
			throw new EventIdNotPresentException(ResidentErrorCode.EVENT_STATUS_NOT_FOUND.getErrorCode(),
					ResidentErrorCode.EVENT_STATUS_NOT_FOUND.getErrorMessage());
		} catch (InvalidRequestTypeCodeException e) {
			audit.setAuditRequestDto(EventEnum.INVALID_REQUEST_TYPE_CODE);
			throw new InvalidRequestTypeCodeException(ResidentErrorCode.INVALID_REQUEST_TYPE_CODE.toString(),
					ResidentErrorCode.INVALID_REQUEST_TYPE_CODE.getErrorMessage());
		} catch (Exception e) {
			throw new ResidentServiceException(ResidentErrorCode.CARD_NOT_FOUND.getErrorCode(),
					ResidentErrorCode.CARD_NOT_FOUND.getErrorMessage(), e);
		}
	}

	public byte[] downloadCardFromDataShareUrl(ResidentTransactionEntity residentTransactionEntity) {
		try {
			if (residentTransactionEntity.getReferenceLink() != null
					&& !residentTransactionEntity.getReferenceLink().isEmpty() && residentTransactionEntity
							.getStatusCode().equals(EventStatusSuccess.CARD_READY_TO_DOWNLOAD.name())) {
				URI dataShareUri = URI.create(residentTransactionEntity.getReferenceLink());
				byte[] pdfBytes = residentServiceRestClient.getApi(dataShareUri, byte[].class);
				if (pdfBytes.length == 0) {
					throw new CardNotReadyException();
				}
				residentTransactionEntity.setRequestSummary(ResidentConstants.SUCCESS);
				residentTransactionEntity.setStatusCode(EventStatusSuccess.CARD_DOWNLOADED.name());
				residentTransactionEntity.setStatusComment(CARD_DOWNLOADED.name());
				residentTransactionEntity.setUpdBy(RESIDENT);
				residentTransactionEntity.setUpdDtimes(DateUtils.getUTCCurrentDateTime());
				residentTransactionRepository.save(residentTransactionEntity);
				return pdfBytes;
			}
		} catch (Exception e) {
			audit.setAuditRequestDto(EventEnum.RID_DIGITAL_CARD_REQ_EXCEPTION);
			throw new ResidentServiceException(ResidentErrorCode.CARD_NOT_READY.getErrorCode(),
					ResidentErrorCode.CARD_NOT_READY.getErrorMessage(), e);
		}
		return new byte[0];
	}

	private ResponseWrapper<PageDto<ServiceHistoryResponseDto>> getServiceHistoryDetails(String sortType,
																						 Integer pageStart, Integer pageFetch, LocalDate fromDateTime, LocalDate toDateTime,
																						 String serviceType, String statusFilter, String searchText, String langCode, int timeZoneOffset, List<String> statusCodeList)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<PageDto<ServiceHistoryResponseDto>> responseWrapper = new ResponseWrapper<>();
		String idaToken = identityServiceImpl.getResidentIdaToken();
		responseWrapper.setResponse(getServiceHistoryResponse(sortType, pageStart, pageFetch, idaToken, statusFilter,
				searchText, fromDateTime, toDateTime, serviceType, langCode, timeZoneOffset, statusCodeList));
		responseWrapper.setId(serviceHistoryId);
		responseWrapper.setVersion(serviceHistoryVersion);
		responseWrapper.setResponsetime(DateUtils.getUTCCurrentDateTime());

		return responseWrapper;
	}

	public PageDto<ServiceHistoryResponseDto> getServiceHistoryResponse(String sortType, Integer pageStart,
																		Integer pageFetch, String idaToken, String statusFilter, String searchText, LocalDate fromDateTime,
																		LocalDate toDateTime, String serviceType, String langCode, int timeZoneOffset, List<String> statusCodeList)
			throws ResidentServiceCheckedException {
		String nativeQueryString = getDynamicNativeQueryStringForServiceHistory(sortType, idaToken, pageStart, pageFetch, statusFilter,
				searchText, fromDateTime, toDateTime, serviceType, timeZoneOffset, statusCodeList);
		Query nativeQuery = entityManager.createNativeQuery(nativeQueryString, ResidentTransactionEntity.class);
		List<ResidentTransactionEntity> residentTransactionEntityList = (List<ResidentTransactionEntity>) nativeQuery
				.getResultList();
		String[] split = nativeQueryString.split("order by");
		String nativeQueryStringWithoutOrderBy = split[0];
		nativeQueryStringWithoutOrderBy = nativeQueryStringWithoutOrderBy.replace("*", "count(*)");
		Query nativeQuery2 = entityManager.createNativeQuery(nativeQueryStringWithoutOrderBy);
		BigInteger count = (BigInteger) nativeQuery2.getSingleResult();
		int size = count.intValue();
		return new PageDto<>(pageStart, pageFetch, size, (size / pageFetch) + 1,
				convertResidentEntityListToServiceHistoryDto(residentTransactionEntityList, langCode, timeZoneOffset));
	}

	public String getDynamicNativeQueryStringForServiceHistory(String sortType, String idaToken, Integer pageStart, Integer pageFetch,
															   String statusFilter, String searchText, LocalDate fromDateTime, LocalDate toDateTime,
															   String serviceType, int timeZoneOffset, List<String> statusCodeList) {
		String query = "SELECT * FROM resident_transaction  where token_id = '"
				+ idaToken+"'";
		String dynamicQuery = "";
		if (fromDateTime != null && toDateTime != null && serviceType != null && !serviceType.equalsIgnoreCase("ALL")
				&& statusFilter != null && searchText != null) {
			dynamicQuery = getDateQuery(fromDateTime, toDateTime, timeZoneOffset) + getServiceQuery(serviceType)
					+ getStatusFilterQuery(statusFilter, statusCodeList) + getSearchQuery(searchText);
		} else if (fromDateTime != null && toDateTime != null && serviceType != null
				&& !serviceType.equalsIgnoreCase("ALL") && statusFilter != null) {
			dynamicQuery = getDateQuery(fromDateTime, toDateTime, timeZoneOffset) + getServiceQuery(serviceType )
					+ getStatusFilterQuery(statusFilter, statusCodeList);
		} else if (fromDateTime != null && toDateTime != null && serviceType != null
				&& !serviceType.equalsIgnoreCase("ALL") && searchText != null) {
			dynamicQuery = getDateQuery(fromDateTime, toDateTime, timeZoneOffset) + getServiceQuery(serviceType )
					+ getSearchQuery(searchText );
		} else if (fromDateTime != null && toDateTime != null && statusFilter != null && searchText != null) {
			dynamicQuery = getDateQuery(fromDateTime, toDateTime, timeZoneOffset) + getStatusFilterQuery(statusFilter, statusCodeList)
					+ getSearchQuery(searchText );
		} else if (serviceType != null && !serviceType.equalsIgnoreCase("ALL") && statusFilter != null
				&& searchText != null) {
			dynamicQuery = getServiceQuery(serviceType ) + getStatusFilterQuery(statusFilter, statusCodeList)
					+ getSearchQuery(searchText );
		} else if (serviceType != null && !serviceType.equalsIgnoreCase("ALL") && statusFilter != null) {
			dynamicQuery = getServiceQuery(serviceType ) + getStatusFilterQuery(statusFilter, statusCodeList);
		} else if (serviceType != null && !serviceType.equalsIgnoreCase("ALL") && searchText != null) {
			dynamicQuery = getServiceQuery(serviceType ) + getSearchQuery(searchText );
		} else if (statusFilter != null && searchText != null) {
			dynamicQuery = getStatusFilterQuery(statusFilter, statusCodeList) + getSearchQuery(searchText );
		} else if (fromDateTime != null && toDateTime != null && searchText != null) {
			dynamicQuery = getDateQuery(fromDateTime, toDateTime, timeZoneOffset) + getSearchQuery(searchText );
		} else if (fromDateTime != null && toDateTime != null && statusFilter != null) {
			dynamicQuery = getDateQuery(fromDateTime, toDateTime, timeZoneOffset) + getStatusFilterQuery(statusFilter, statusCodeList);
		} else if (fromDateTime != null && toDateTime != null && serviceType != null
				&& !serviceType.equalsIgnoreCase("ALL")) {
			dynamicQuery = getDateQuery(fromDateTime, toDateTime, timeZoneOffset) + getServiceQuery(serviceType );
		} else if (fromDateTime != null && toDateTime != null) {
			dynamicQuery = getDateQuery(fromDateTime, toDateTime, timeZoneOffset);
		} else if (serviceType != null && !serviceType.equalsIgnoreCase("ALL")) {
			dynamicQuery = getServiceQuery(serviceType );
		} else if (statusFilter != null) {
			dynamicQuery = getStatusFilterQuery(statusFilter, statusCodeList);
		} else if (searchText != null) {
			dynamicQuery = getSearchQuery(searchText );
		}
		if(serviceType == null){
			dynamicQuery = dynamicQuery + getServiceQueryForNullServiceType();
		}
		dynamicQuery = dynamicQuery + getOlvPartnerIdQuery();
		if (sortType == null) {
			sortType = SortType.DESC.toString();
		}

		String orderByQuery = " order by pinned_status desc, " + "cr_dtimes " + sortType + " limit " + pageFetch
				+ " offset " + (pageStart) * pageFetch;
		return query + dynamicQuery + orderByQuery;
	}
	private String getOlvPartnerIdQuery() {
		return " AND (olv_partner_id is null OR olv_partner_id='" + onlineVerificationPartnerId + "')";
	}

	private String getServiceQueryForNullServiceType() {
		return " and request_type_code in (" + convertServiceTypeListToString(
				(List<String>) convertListOfRequestTypeToListOfString(ServiceType.ALL.getRequestType())) +")";
	}

	private String getServiceQuery(String serviceType) {
		List<String> requestTypeList = convertServiceTypeToResidentTransactionType(serviceType);
		String requestTypeListString = convertServiceTypeListToString(requestTypeList);
		return " and request_type_code in (" + requestTypeListString + ")";
	}

	private String getDateQuery(LocalDate fromDate, LocalDate toDate, int timeZoneOffset) {
		//Converting local time to UTC before using in db query
		LocalDateTime fromDateTime = fromDate.atStartOfDay().plusMinutes(timeZoneOffset);
		LocalDateTime toDateTime = toDate.plusDays(1).atStartOfDay().plusMinutes(timeZoneOffset);
		return " and cr_dtimes between '" +  fromDateTime + "' and '" +
				toDateTime+ "'";
	}

	private String getSearchQuery(String searchText) {
		String text= "%" + searchText.replace(AUTH_TYPE_SEPERATOR, "") + "%";
		return " and Replace(event_id,'-','') like '"+text+ "'";
	}

	public String getStatusFilterQuery(String statusFilter, List<String> statusCodeList) {
		List<String> statusFilterList = List.of(statusFilter.split(",")).stream().map(String::trim)
				.collect(Collectors.toList());
		String statusFilterListString = "";
		List<String> statusFilterListContainingAllStatus = new ArrayList<>();
		if(statusCodeList == null || statusCodeList.isEmpty()) {
			for (String status : statusFilterList) {
				if (status.equalsIgnoreCase(EventStatus.SUCCESS.getStatus())) {
					statusFilterListContainingAllStatus.addAll(RequestType.getAllSuccessStatusList(env));
				} else if (status.equalsIgnoreCase(EventStatus.FAILED.getStatus())) {
					statusFilterListContainingAllStatus.addAll(RequestType.getAllFailedStatusList(env));
				} else if (status.equalsIgnoreCase(EventStatus.IN_PROGRESS.getStatus())) {
					statusFilterListContainingAllStatus.addAll(RequestType.getAllNewOrInprogressStatusList(env));
				}
			}
		}else {
			statusFilterListContainingAllStatus.addAll(statusFilterList);
		}
		statusFilterListString = convertStatusFilterListToString(statusFilterListContainingAllStatus);
		return " and status_code in (" + statusFilterListString + ")";
	}

	public String convertStatusFilterListToString(List<String> statusFilterListContainingALlStatus) {
		String statusFilterListString = "";
		for (String status : statusFilterListContainingALlStatus) {
			statusFilterListString = statusFilterListString + "'" + status + "',";
		}
		return statusFilterListString.substring(0, statusFilterListString.length() - 1);
	}

	public String convertServiceTypeListToString(List<String> serviceTypeList) {
		String serviceTypeListString = "";
		for (String serviceType : serviceTypeList) {
			serviceTypeListString = serviceTypeListString + "'" + serviceType + "',";
		}
		return serviceTypeListString.substring(0, serviceTypeListString.length() - 1);
	}

	private List<String> convertServiceTypeToResidentTransactionType(String serviceType) {
		List<String> residentTransactionTypeList = new ArrayList<>();
		if (serviceType != null) {
			List<String> serviceTypeList = List.of(serviceType.split(",")).stream().map(String::toUpperCase)
					.collect(Collectors.toList());
			for (String service : serviceTypeList) {
				ServiceType type = ServiceType.valueOf(service);
				residentTransactionTypeList.addAll(convertListOfRequestTypeToListOfString(type.getRequestType()));
			}
		}
		return residentTransactionTypeList;
	}

	private Collection<String> convertListOfRequestTypeToListOfString(List<RequestType> requestType) {
		return requestType.stream().map(Enum::name).collect(Collectors.toList());
	}

	private List<ServiceHistoryResponseDto> convertResidentEntityListToServiceHistoryDto(
			List<ResidentTransactionEntity> residentTransactionEntityList, String langCode, int timeZoneOffset)
			throws ResidentServiceCheckedException {
		List<ServiceHistoryResponseDto> serviceHistoryResponseDtoList = new ArrayList<>();
		for (ResidentTransactionEntity residentTransactionEntity : residentTransactionEntityList) {
			String statusCode = getEventStatusCode(residentTransactionEntity.getStatusCode());
			RequestType requestType = RequestType
					.getRequestTypeFromString(residentTransactionEntity.getRequestTypeCode());
			Optional<String> serviceType = ServiceType.getServiceTypeFromRequestType(requestType);

			ServiceHistoryResponseDto serviceHistoryResponseDto = new ServiceHistoryResponseDto();
			serviceHistoryResponseDto.setRequestType(requestType.name());
			serviceHistoryResponseDto.setEventId(residentTransactionEntity.getEventId());
			serviceHistoryResponseDto.setEventStatus(statusCode);
			if (residentTransactionEntity.getUpdDtimes() != null
					&& residentTransactionEntity.getUpdDtimes().isAfter(residentTransactionEntity.getCrDtimes())) {
				serviceHistoryResponseDto.setTimeStamp(utility.formatWithOffsetForUI(timeZoneOffset, residentTransactionEntity.getUpdDtimes()));
			} else {
				serviceHistoryResponseDto.setTimeStamp(utility.formatWithOffsetForUI(timeZoneOffset, residentTransactionEntity.getCrDtimes()));
			}
			if (serviceType.isPresent()) {
				if (!serviceType.get().equals(ServiceType.ALL.name())) {
					serviceHistoryResponseDto.setServiceType(serviceType.get());
					serviceHistoryResponseDto
							.setDescription(getDescriptionForLangCode(residentTransactionEntity, langCode, statusCode, requestType));
				}
			} else {
				serviceHistoryResponseDto.setDescription(requestType.name());
			}
			serviceHistoryResponseDto.setPinnedStatus(residentTransactionEntity.getPinnedStatus());
			serviceHistoryResponseDto.setReadStatus(residentTransactionEntity.isReadStatus());
			serviceHistoryResponseDtoList.add(serviceHistoryResponseDto);
		}
		return serviceHistoryResponseDtoList;
	}

	public String getDescriptionForLangCode(ResidentTransactionEntity residentTransactionEntity, String langCode, String statusCode, RequestType requestType)
			throws ResidentServiceCheckedException {
		TemplateType templateType;
		if (statusCode.equalsIgnoreCase(EventStatus.SUCCESS.toString())) {
			templateType = TemplateType.SUCCESS;
		} else {
			templateType = TemplateType.FAILURE;
		}
		String templateTypeCode = templateUtil.getPurposeTemplateTypeCode(requestType, templateType);
		String fileText = templateUtil.getTemplateValueFromTemplateTypeCodeAndLangCode(langCode, templateTypeCode);
		return replacePlaceholderValueInTemplate(residentTransactionEntity, fileText, requestType, langCode);
	}

	private String replacePlaceholderValueInTemplate(ResidentTransactionEntity residentTransactionEntity, String fileText, RequestType requestType, String langCode) {
		return requestType.getDescriptionTemplateVariables(templateUtil, residentTransactionEntity, fileText, langCode);
	}

	public String getSummaryForLangCode(ResidentTransactionEntity residentTransactionEntity, String langCode, String statusCode, RequestType requestType)
			throws ResidentServiceCheckedException {
		TemplateType templateType;
		if (statusCode.equalsIgnoreCase(EventStatus.SUCCESS.toString())) {
			templateType = TemplateType.SUCCESS;
			String templateTypeCode = templateUtil.getSummaryTemplateTypeCode(requestType, templateType);
			String fileText = templateUtil.getTemplateValueFromTemplateTypeCodeAndLangCode(langCode, templateTypeCode);
			return replacePlaceholderValueInTemplate(residentTransactionEntity, fileText, requestType, langCode);
		} else {
			return getDescriptionForLangCode(residentTransactionEntity, langCode, statusCode, requestType);
		}
	}

	public String getEventStatusCode(String statusCode) {
		if (EventStatusSuccess.containsStatus(statusCode)) {
			return EventStatus.SUCCESS.getStatus();
		} else if (EventStatusFailure.containsStatus(statusCode)) {
			return EventStatus.FAILED.getStatus();
		} else {
			return EventStatus.IN_PROGRESS.getStatus();
		}
	}

	@Override
	public AidStatusResponseDTO getAidStatus(AidStatusRequestDTO reqDto)
			throws ResidentServiceCheckedException, ApisResourceAccessException, OtpValidationFailedException {
		return getAidStatus(reqDto, true);
	}

	@Override
	public AidStatusResponseDTO getAidStatus(AidStatusRequestDTO reqDto, boolean performOtpValidation)
			throws ResidentServiceCheckedException, ApisResourceAccessException, OtpValidationFailedException {
		try {
			String individualId = identityServiceImpl.getIndividualIdForAid(reqDto.getIndividualId());
			boolean validStatus = individualId != null;
			if (performOtpValidation) {
				validStatus = idAuthServiceImpl.validateOtp(reqDto.getTransactionId(), individualId, reqDto.getOtp());
			}
			if (validStatus) {
				AidStatusResponseDTO aidStatusResponseDTO = new AidStatusResponseDTO();
				aidStatusResponseDTO.setIndividualId(individualId);
				aidStatusResponseDTO.setAidStatus(PROCESSED);
				aidStatusResponseDTO.setTransactionId(reqDto.getTransactionId());
				return aidStatusResponseDTO;
			}
			throw new ResidentServiceCheckedException(ResidentErrorCode.AID_STATUS_IS_NOT_READY);
		} catch (ResidentServiceCheckedException | ApisResourceAccessException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					"ResidentServiceImpl::getAidStatus()::" + e.getClass().getSimpleName() + " :" + e.getMessage());
			RegStatusCheckResponseDTO ridStatus = null;
			try {
				ridStatus = getRidStatus(reqDto.getIndividualId());
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
		try {
			String uin = identityServiceImpl.getUinForIndividualId(aid);
			if (uin == null) {
				throw new ResidentServiceCheckedException(ResidentErrorCode.AID_NOT_FOUND);
			}
			AidStatusRequestDTO aidStatusRequestDTO = new AidStatusRequestDTO();
			aidStatusRequestDTO.setIndividualId(aid);
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

	@Override
	public ResponseWrapper<EventStatusResponseDTO> getEventStatus(String eventId, String languageCode, int timeZoneOffset)
			throws ResidentServiceCheckedException {
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(), "ResidentServiceImpl::getEventStatus()::Start");
		ResponseWrapper<EventStatusResponseDTO> responseWrapper = new ResponseWrapper<>();
		try {
			Optional<ResidentTransactionEntity> residentTransactionEntity = residentTransactionRepository
					.findById(eventId);
			String requestTypeCode;
			String statusCode;
			if (residentTransactionEntity.isPresent()) {
				String idaToken = identityServiceImpl.getResidentIdaToken();
				if (!idaToken.equals(residentTransactionEntity.get().getTokenId())) {
					throw new ResidentServiceCheckedException(ResidentErrorCode.EID_NOT_BELONG_TO_SESSION);
				}
				residentTransactionRepository.updateReadStatus(eventId);
				requestTypeCode = residentTransactionEntity.get().getRequestTypeCode();
				statusCode = getEventStatusCode(residentTransactionEntity.get().getStatusCode());
			} else {
				audit.setAuditRequestDto(EventEnum.CHECK_AID_STATUS_REQUEST_FAILED);
				throw new ResidentServiceCheckedException(ResidentErrorCode.EVENT_STATUS_NOT_FOUND);
			}
			RequestType requestType = RequestType.getRequestTypeFromString(requestTypeCode);
			Optional<String> serviceType = ServiceType.getServiceTypeFromRequestType(requestType);
			Map<String, String> eventStatusMap;

			eventStatusMap = requestType.getAckTemplateVariables(templateUtil, eventId, languageCode, timeZoneOffset).getT1();

			EventStatusResponseDTO eventStatusResponseDTO = new EventStatusResponseDTO();
			eventStatusResponseDTO.setEventId(eventId);
			eventStatusResponseDTO.setEventType(eventStatusMap.get(TemplateVariablesConstants.EVENT_TYPE));
			eventStatusResponseDTO.setEventStatus(eventStatusMap.get(TemplateVariablesConstants.EVENT_STATUS));
			eventStatusResponseDTO.setIndividualId(eventStatusMap.get(TemplateVariablesConstants.INDIVIDUAL_ID));
			eventStatusResponseDTO.setTimestamp(eventStatusMap.get(TemplateVariablesConstants.TIMESTAMP));
			eventStatusResponseDTO.setSummary(eventStatusMap.get(TemplateVariablesConstants.SUMMARY));

			/**
			 * Removed map value from eventStatusMap to put outside of info in
			 * EventStatusResponseDTO
			 */
			eventStatusMap.remove(TemplateVariablesConstants.EVENT_ID);
			eventStatusMap.remove(TemplateVariablesConstants.EVENT_TYPE);
			eventStatusMap.remove(TemplateVariablesConstants.EVENT_STATUS);
			eventStatusMap.remove(TemplateVariablesConstants.INDIVIDUAL_ID);
			eventStatusMap.remove(TemplateVariablesConstants.SUMMARY);
			eventStatusMap.remove(TemplateVariablesConstants.TIMESTAMP);
			eventStatusMap.remove(TemplateVariablesConstants.TRACK_SERVICE_REQUEST_LINK);

			String name = identityServiceImpl.getClaimValue(env.getProperty(ResidentConstants.NAME_FROM_PROFILE));
			eventStatusMap.put(env.getProperty(ResidentConstants.APPLICANT_NAME_PROPERTY), name);
			eventStatusMap.put(env.getProperty(ResidentConstants.AUTHENTICATION_MODE_PROPERTY), eventStatusMap.get(TemplateVariablesConstants.AUTHENTICATION_MODE));

			if (serviceType.isPresent()) {
				if (!serviceType.get().equals(ServiceType.ALL.name())) {
					eventStatusMap.put(TemplateVariablesConstants.DESCRIPTION,
							getDescriptionForLangCode(residentTransactionEntity.get(), languageCode, statusCode, requestType));
				}
			} else {
				eventStatusMap.put(TemplateVariablesConstants.DESCRIPTION, requestType.name());
			}
			eventStatusResponseDTO.setInfo(eventStatusMap);
			responseWrapper.setId(serviceEventId);
			responseWrapper.setVersion(serviceEventVersion);
			responseWrapper.setResponsetime(DateUtils.getUTCCurrentDateTime());
			responseWrapper.setResponse(eventStatusResponseDTO);

		} catch (ApisResourceAccessException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), "ResidentServiceImpl::getEventStatus():: Exception");
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		return responseWrapper;
	}

	@Override
	public ResponseWrapper<UnreadNotificationDto> getnotificationCount(String idaToken) throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResponseWrapper<UnreadNotificationDto> responseWrapper = new ResponseWrapper<>();
		LocalDateTime time = null;
		Long countOfUnreadNotifications;
		Optional<ResidentUserEntity> residentUserEntity = residentUserRepository.findById(idaToken);
		List<String> asyncRequestTypes = getAsyncRequestTypes();
		if (residentUserEntity.isPresent()) {
			//Get the last bell notification click time
			time = residentUserEntity.get().getLastbellnotifDtimes();
			//Get count of unread events after bell notification click time
			countOfUnreadNotifications = residentTransactionRepository
					.countByIdAndUnreadStatusForRequestTypesAfterNotificationClick(idaToken, time, asyncRequestTypes, onlineVerificationPartnerId);
		} else {
			//Get count of all unread events
			countOfUnreadNotifications = residentTransactionRepository.countByIdAndUnreadStatusForRequestTypes(idaToken,
					asyncRequestTypes, onlineVerificationPartnerId);
		}
		UnreadNotificationDto notification = new UnreadNotificationDto();
		notification.setUnreadCount(countOfUnreadNotifications);
		responseWrapper.setId(serviceEventId);
		responseWrapper.setVersion(serviceEventVersion);
		responseWrapper.setResponse(notification);
		return responseWrapper;
	}

	private List<String> getAsyncRequestTypes() {
		return ServiceType.ASYNC.getRequestType()
				.stream()
				.map(RequestType::name)
				.collect(Collectors.toList());
	}

	@Override
	public ResponseWrapper<BellNotificationDto> getbellClickdttimes(String idaToken) {
		ResponseWrapper<BellNotificationDto> responseWrapper = new ResponseWrapper<>();
		BellNotificationDto bellnotifdttimes = new BellNotificationDto();
		Optional<ResidentUserEntity> residentUserEntity = residentUserRepository.findById(idaToken);
		if (residentUserEntity.isPresent()) {
			LocalDateTime time = residentUserEntity.get().getLastbellnotifDtimes();
			bellnotifdttimes.setLastbellnotifclicktime(time);
		}
		responseWrapper.setId(serviceEventId);
		responseWrapper.setVersion(serviceEventVersion);
		responseWrapper.setResponse(bellnotifdttimes);
		return responseWrapper;
	}

	@Override
	public int updatebellClickdttimes(String idaToken) throws ApisResourceAccessException, ResidentServiceCheckedException {
		LocalDateTime dt = DateUtils.getUTCCurrentDateTime();
		Optional<ResidentUserEntity> entity = residentUserRepository.findById(idaToken);
		if (entity.isPresent()) {
			return residentUserRepository.updateByIdLastbellnotifDtimes(idaToken, dt);
		} else {
			ResidentUserEntity newUserData = new ResidentUserEntity(idaToken, dt);
			residentUserRepository.save(newUserData);
			return UPDATE_COUNT_FOR_NEW_USER_ACTION_ENTITY;
		}

	}

	public ResponseWrapper<PageDto<ServiceHistoryResponseDto>> getNotificationList(Integer pageStart,
			Integer pageFetch, String id, String languageCode, int timeZoneOffset) throws ResidentServiceCheckedException, ApisResourceAccessException {
		List<RequestType> requestTypeList = ServiceType.ASYNC.getRequestType();
		List<String> statusCodeList = requestTypeList.stream()
				.flatMap(requestType -> requestType.getNotificationStatusList(env))
				.collect(Collectors.toCollection(ArrayList::new));
		ResponseWrapper<PageDto<ServiceHistoryResponseDto>> responseWrapper = getServiceHistory(pageStart, pageFetch,
																				 null, null, ServiceType.ASYNC.name(), null,
																				 null, null, languageCode, timeZoneOffset,
				RESIDENT_NOTIFICATIONS_DEFAULT_PAGE_SIZE, statusCodeList);
		responseWrapper.setId(unreadnotificationlist);
		responseWrapper.setVersion(serviceEventVersion);
		return responseWrapper;
	}

	@Override
	/**
	 * create the template for service history PDF and converted template into PDF
	 */
	public byte[] downLoadServiceHistory(ResponseWrapper<PageDto<ServiceHistoryResponseDto>> responseWrapper,
										 String languageCode, LocalDateTime eventReqDateTime, LocalDate fromDate, LocalDate toDate,
										 String serviceType, String statusFilter, int timeZoneOffset) throws ResidentServiceCheckedException, IOException {

		logger.debug("ResidentServiceImpl::getResidentServicePDF()::entry");
		String templateTypeCode = this.env.getProperty(ResidentConstants.SERVICE_HISTORY_PROPERTY_TEMPLATE_TYPE_CODE);
		String fileText = templateUtil.getTemplateValueFromTemplateTypeCodeAndLangCode(languageCode, templateTypeCode);
		// for avoiding null values in PDF
		List<ServiceHistoryResponseDto> serviceHistoryDtlsList = responseWrapper.getResponse().getData();
		if (serviceHistoryDtlsList != null && !serviceHistoryDtlsList.isEmpty()) {
			IntStream.range(0, serviceHistoryDtlsList.size()).forEach(i -> {
					addServiceHistoryDtls(i, serviceHistoryDtlsList.get(i));
			});
		}
		if(serviceHistoryDtlsList!=null){
			for (ServiceHistoryResponseDto dto : serviceHistoryDtlsList) {
				if (dto.getDescription() == null)
					dto.setDescription("");
			}
		}
		Map<String, Object> servHistoryMap = new HashMap<>();
		if(eventReqDateTime == null){
			eventReqDateTime = DateUtils.getUTCCurrentDateTime();
		}
		if(fromDate == null){
			fromDate = LocalDate.of(LocalDate.now().getYear(), Month.JANUARY, 1);
		}
		if(toDate == null){
			toDate = LocalDate.now();
		}
		if(statusFilter == null){
			statusFilter = ALL;
		}

		if(serviceType == null){
			serviceType = ALL;
		}
		servHistoryMap.put("eventReqTimeStamp", utility.formatWithOffsetForUI(timeZoneOffset, eventReqDateTime));
		servHistoryMap.put("fromDate", fromDate);
		servHistoryMap.put("toDate", toDate);
		servHistoryMap.put("statusFilter", statusFilter);
		servHistoryMap.put("serviceType", serviceType);
		servHistoryMap.put("serviceHistoryDtlsList", serviceHistoryDtlsList);

		InputStream serviceHistTemplate = new ByteArrayInputStream(fileText.getBytes(StandardCharsets.UTF_8));
		InputStream serviceHistTemplateData = templateManager.merge(serviceHistTemplate, servHistoryMap);
		StringWriter writer = new StringWriter();
		IOUtils.copy(serviceHistTemplateData, writer, "UTF-8");
		logger.debug("ResidentServiceImpl::residentServiceHistoryPDF()::exit");
		return utility.signPdf(new ByteArrayInputStream(writer.toString().getBytes()), null);
	}

	@Override
	public ResponseWrapper<UserInfoDto> getUserinfo(String idaToken, int timeZoneOffset) throws ApisResourceAccessException {
		String name = identityServiceImpl.getAvailableclaimValue(env.getProperty(ResidentConstants.NAME_FROM_PROFILE));
		String photo = identityServiceImpl.getAvailableclaimValue(env.getProperty(IMAGE));
		String email = identityServiceImpl.getAvailableclaimValue(env.getProperty(ResidentConstants.EMAIL_FROM_PROFILE));
		String phone = identityServiceImpl.getAvailableclaimValue(env.getProperty(ResidentConstants.PHONE_FROM_PROFILE));
		ResponseWrapper<UserInfoDto> responseWrapper = new ResponseWrapper<UserInfoDto>();
		UserInfoDto user = new UserInfoDto();
		Map<String, Object> data = new HashMap<>();
		responseWrapper.setId(env.getProperty(ResidentConstants.RESIDENT_USER_PROFILE_ID));
		responseWrapper.setVersion(env.getProperty(ResidentConstants.REQ_RES_VERSION));
		responseWrapper.setResponsetime(DateUtils.getUTCCurrentDateTime());
		//Return the second element
		List<ResidentSessionEntity> lastTwoLoginEntities = residentSessionRepository.findFirst2ByIdaTokenOrderByLoginDtimesDesc(idaToken);
		if (!lastTwoLoginEntities.isEmpty()) {
			data.put("data", photo);
			user.setFullName(name);
			user.setPhone(phone);
			user.setEmail(email);
			
			LocalDateTime lastLoginDateTime;
			if (lastTwoLoginEntities.size() > 1) {
				lastLoginDateTime = lastTwoLoginEntities.get(1).getLoginDtimes();
			} else {
				lastLoginDateTime = lastTwoLoginEntities.get(0).getLoginDtimes();
			}
			
			user.setLastLogin(utility.applyTimeZoneOffsetOnDateTime(timeZoneOffset, lastLoginDateTime));
			user.setPhoto(data);
			responseWrapper.setResponse(user);
			return responseWrapper;
		} else {
			throw new ResidentServiceException(ResidentErrorCode.NO_RECORDS_FOUND.getErrorCode(),
					ResidentErrorCode.NO_RECORDS_FOUND.getErrorMessage());
		}

	}
	
	/**
	 * 
	 * @param index
	 */
	private void addServiceHistoryDtls(int index, ServiceHistoryResponseDto serviceHistoryDto) {
		serviceHistoryDto.setSerialNumber(index + 1);
		if (serviceHistoryDto.getDescription() == null)
			serviceHistoryDto.setDescription("");
	}
}