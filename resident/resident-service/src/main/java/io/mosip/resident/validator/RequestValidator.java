package io.mosip.resident.validator;

import io.mosip.kernel.core.idvalidator.exception.InvalidIDException;
import io.mosip.kernel.core.idvalidator.spi.RidValidator;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.preregistration.application.dto.TransliterationRequestDTO;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.constant.CardType;
import io.mosip.resident.constant.EventStatus;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.RequestIdType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.constant.ServiceType;
import io.mosip.resident.constant.TemplateVariablesConstants;
import io.mosip.resident.dto.AidStatusRequestDTO;
import io.mosip.resident.dto.AuthHistoryRequestDTO;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDto;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDtoV2;
import io.mosip.resident.dto.AuthTypeStatusDtoV2;
import io.mosip.resident.dto.AuthUnLockRequestDTO;
import io.mosip.resident.dto.BaseVidRequestDto;
import io.mosip.resident.dto.BaseVidRevokeRequestDTO;
import io.mosip.resident.dto.DownloadCardRequestDTO;
import io.mosip.resident.dto.DownloadPersonalizedCardDto;
import io.mosip.resident.dto.EuinRequestDTO;
import io.mosip.resident.dto.GrievanceRequestDTO;
import io.mosip.resident.dto.IVidRequestDto;
import io.mosip.resident.dto.IndividualIdOtpRequestDTO;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.dto.OtpRequestDTOV2;
import io.mosip.resident.dto.OtpRequestDTOV3;
import io.mosip.resident.dto.RequestDTO;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentReprintRequestDto;
import io.mosip.resident.dto.ResidentUpdateRequestDto;
import io.mosip.resident.dto.SharableAttributesDTO;
import io.mosip.resident.dto.SortType;
import io.mosip.resident.dto.VidRequestDto;
import io.mosip.resident.dto.VidRevokeRequestDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.EidNotBelongToSessionException;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.mosip.resident.constant.RegistrationConstants.ID;
import static io.mosip.resident.constant.RegistrationConstants.MESSAGE_CODE;
import static io.mosip.resident.constant.RegistrationConstants.VERSION;
import static io.mosip.resident.constant.ResidentConstants.PAGE_NUMBER_ERROR;
import static io.mosip.resident.constant.ResidentConstants.PAGE_START_PAGE_FETCH;
import static io.mosip.resident.constant.ResidentConstants.REQUEST;
import static io.mosip.resident.constant.ResidentConstants.REQUEST_PRINT_UIN;
import static io.mosip.resident.constant.ResidentConstants.REQUEST_REVOKE_VID;
import static io.mosip.resident.constant.ResidentConstants.REQUEST_TIME;
import static io.mosip.resident.service.impl.ResidentOtpServiceImpl.EMAIL_CHANNEL;
import static io.mosip.resident.service.impl.ResidentOtpServiceImpl.PHONE_CHANNEL;

@Component
public class RequestValidator {

	private static final int EVENT_ID_LENGTH = 16;
	private static final String VALIDATE_EVENT_ID = "Validating Event Id.";
	@Autowired
	private UinValidator<String> uinValidator;

	@Autowired
	private VidValidator<String> vidValidator;

	@Autowired
	private RidValidator<String> ridValidator;

	@Autowired
	private AuditUtil audit;

	@Autowired
	private Environment environment;

	@Autowired
	private IdentityServiceImpl identityService;

	@Autowired
	private ResidentTransactionRepository residentTransactionRepository;

	private String euinId;

	private String reprintId;

	private String authUnLockId;

	private String authHstoryId;

	private String authLockId;

	private String uinUpdateId;

	@Value("${resident.updateuin.id}")
	public void setUinUpdateId(String uinUpdateId) {
		this.uinUpdateId = uinUpdateId;
	}

	@Value("${resident.vid.id}")
	private String id;

	@Value("${resident.vid.id.generate}")
	private String generateId;

	@Value("${resident.revokevid.id}")
	private String revokeVidId;
	
	@Value("${mosip.resident.revokevid.id}")
	private String revokeVidIdNew;

	@Value("${resident.vid.version}")
	private String version;
	
	@Value("${resident.vid.version.new}")
	private String newVersion;
	
	@Value("${resident.revokevid.version.new}")
	private String revokeVidVersion;

	@Value("${resident.authlock.id}")
	public void setAuthLockId(String authLockId) {
		this.authLockId = authLockId;
	}

	@Value("${resident.euin.id}")
	public void setEuinIdString(String euinId) {
		this.euinId = euinId;
	}

	@Value("${resident.authhistory.id}")
	public void setAuthHstoryId(String authHstoryId) {
		this.authHstoryId = authHstoryId;
	}

	@Value("${auth.types.allowed}")
	private String authTypes;

	@Value("${resident.authunlock.id}")
	public void setAuthUnlockId(String authUnLockId) {
		this.authUnLockId = authUnLockId;
	}

	@Value("${mosip.id.validation.identity.phone}")
	private String phoneRegex;

	@Value("${mosip.id.validation.identity.email}")
	private String emailRegex;

	@Value("${resident.checkstatus.id}")
	private String checkStatusID;
	
	@Value("${resident.share.credential.id}")
	private String shareCredentialId;
	
	@Value("${mosip.resident.request.response.version}")
	private String reqResVersion;

	private Map<RequestIdType, String> map;

	@Value("${resident.printuin.id}")
	public void setReprintId(String reprintId) {
		this.reprintId = reprintId;
	}

	@Value("${mosip.mandatory-languages}")
	private String mandatoryLanguages;

	@Value("${mosip.optional-languages}")
	private String optionalLanguages;

	@Value("${mosip.resident.transliteration.transliterate.id}")
	private String transliterateId;

	@Value("${otpChannel.mobile}")
	private String mobileChannel;

	@Value("${otpChannel.email}")
	private String emailChannel;
	
	@Value("${resident.authLockStatusUpdateV2.id}")
	private String authLockStatusUpdateV2Id;

	@PostConstruct
	public void setMap() {
		map = new EnumMap<>(RequestIdType.class);
		map.put(RequestIdType.RE_PRINT_ID, reprintId);
		map.put(RequestIdType.AUTH_LOCK_ID, authLockId);
		map.put(RequestIdType.AUTH_UNLOCK_ID, authUnLockId);
		map.put(RequestIdType.E_UIN_ID, euinId);
		map.put(RequestIdType.AUTH_HISTORY_ID, authHstoryId);
		map.put(RequestIdType.RES_UPDATE, uinUpdateId);
		map.put(RequestIdType.CHECK_STATUS, checkStatusID);
		map.put(RequestIdType.SHARE_CREDENTIAL, shareCredentialId);
		map.put(RequestIdType.AUTH_LOCK_UNLOCK, authLockStatusUpdateV2Id);
	}

	public void validateVidCreateRequest(IVidRequestDto<? extends BaseVidRequestDto> requestDto, boolean otpValidationRequired, String individualId) {

		try {
			DateUtils.parseToLocalDateTime(requestDto.getRequesttime());
		} catch (Exception e) {
			throwInvalidInputException(REQUEST_TIME,
					REQUEST_TIME + ResidentConstants.MUST_NOT_BE_EMPTY);
		}
		if (StringUtils.isEmpty(requestDto.getId()) || !requestDto.getId().equalsIgnoreCase(id)) {
			throwInvalidInputException(ResidentConstants.ID,
					ResidentConstants.ID + ResidentConstants.MUST_NOT_BE_EMPTY);
		}
		if (StringUtils.isEmpty(requestDto.getVersion()) || !requestDto.getVersion().equalsIgnoreCase(version)) {
			throwInvalidInputException(ResidentConstants.VERSION,
					ResidentConstants.VERSION + ResidentConstants.MUST_NOT_BE_EMPTY);
		}
		if (requestDto.getRequest() == null) {
			throwInvalidInputException(REQUEST,
					REQUEST + ResidentConstants.MUST_NOT_BE_EMPTY);
		}
		if (StringUtils.isEmpty(individualId)
				|| !validateIndividualIdvIdWithoutIdType(individualId)) {
			throwInvalidInputException(ResidentConstants.INDIVIDUAL_ID,
					ResidentConstants.INDIVIDUAL_ID + ResidentConstants.MUST_NOT_BE_EMPTY);
		}
		BaseVidRequestDto vidRequestDto = requestDto.getRequest();
		if(vidRequestDto instanceof VidRequestDto) {
			if (otpValidationRequired && StringUtils.isEmpty(((VidRequestDto)vidRequestDto).getOtp())) {
				throwInvalidInputException(TemplateVariablesConstants.OTP,
						TemplateVariablesConstants.OTP + ResidentConstants.MUST_NOT_BE_EMPTY);
			}
		}
		if (StringUtils.isEmpty(requestDto.getRequest().getTransactionID())) {
			throwInvalidInputException(TemplateVariablesConstants.TRANSACTION_ID,
					TemplateVariablesConstants.TRANSACTION_ID + ResidentConstants.MUST_NOT_BE_EMPTY);
		}
	}
	
	public void validateVidCreateV2Request(IVidRequestDto<? extends BaseVidRequestDto> requestDto, boolean otpValidationRequired, String individualId) {

		try {
			DateUtils.parseToLocalDateTime(requestDto.getRequesttime());
		} catch (Exception e) {
			throwInvalidInputException(REQUEST_TIME,
					ResidentConstants.REQUEST_GENERATE_VID);
		}

		if (StringUtils.isEmpty(requestDto.getId()) || !requestDto.getId().equalsIgnoreCase(generateId)) {
			throwInvalidInputException(ResidentConstants.GENERATE_ID,
					ResidentConstants.REQUEST_GENERATE_VID);
		}
		if (StringUtils.isEmpty(requestDto.getVersion()) || !requestDto.getVersion().equalsIgnoreCase(newVersion)) {
			throwInvalidInputException(ResidentConstants.VERSION,
					ResidentConstants.REQUEST_GENERATE_VID);
		}
		if (requestDto.getRequest() == null) {
			throwInvalidInputException(REQUEST,
					ResidentConstants.REQUEST_GENERATE_VID);
		}
		if (StringUtils.isEmpty(individualId)
				|| !validateIndividualIdvIdWithoutIdType(individualId)) {
			throwInvalidInputException(ResidentConstants.INDIVIDUAL_ID,
					ResidentConstants.REQUEST_GENERATE_VID);
		}
		BaseVidRequestDto vidRequestDto = requestDto.getRequest();
		if(vidRequestDto instanceof VidRequestDto) {
			if (otpValidationRequired && StringUtils.isEmpty(((VidRequestDto)vidRequestDto).getOtp())) {
				validateOTP(((VidRequestDto)vidRequestDto).getOtp());
			}
		}
		if (StringUtils.isEmpty(requestDto.getRequest().getTransactionID())) {
			validateTransactionId(requestDto.getRequest().getTransactionID());
		}
	}

	public void validateAuthLockOrUnlockRequestV2(RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestDto) {
		validateRequestNewApi(requestDto, RequestIdType.AUTH_LOCK_UNLOCK);
		validateAuthTypeV2(requestDto.getRequest().getAuthTypes());
	}

	private void validateAuthTypeV2(List<AuthTypeStatusDtoV2> authType) {
		if (authType == null || authType.isEmpty()) {
			throwInvalidInputException(ResidentConstants.AUTH_TYPES,
					ResidentConstants.AUTH_TYPES + ResidentConstants.MUST_NOT_BE_EMPTY);
		}
		String[] authTypesArray = authTypes.split(",");
		List<String> authTypesAllowed = new ArrayList<>(Arrays.asList(authTypesArray));
		for (AuthTypeStatusDtoV2 authTypeStatusDto : authType) {
			String authTypeString = ResidentServiceImpl.getAuthTypeBasedOnConfigV2(authTypeStatusDto);
			if (StringUtils.isEmpty(authTypeString) || !authTypesAllowed.contains(authTypeString)) {
				throwInvalidInputException(ResidentConstants.AUTH_TYPES,
						ResidentConstants.REQUEST_GENERATE_VID);
			}
			if(!isValidUnlockForSeconds(authTypeStatusDto.getUnlockForSeconds())) {
				throwInvalidInputException(ResidentConstants.UNLOCK_FOR_SECONDS,
						ResidentConstants.REQUEST_GENERATE_VID);
			}
			List<String> authTypes = Arrays.asList(authTypeString);
			validateAuthType(authTypes,
					"Request auth " + authTypes.toString().toLowerCase() + " API");
		}
	}

	private boolean isValidUnlockForSeconds(Long unlockForSeconds) {
		if(unlockForSeconds == null) {
			return true;
		}
		return unlockForSeconds.longValue() > 0;
	}

	public void validateAuthLockOrUnlockRequest(RequestWrapper<AuthLockOrUnLockRequestDto> requestDTO,
			AuthTypeStatus authTypeStatus) {
		validateAuthorUnlockId(requestDTO, authTypeStatus);
		String individualId = requestDTO.getRequest().getIndividualId();
		if (StringUtils.isEmpty(individualId)
				|| !validateIndividualIdvIdWithoutIdType(individualId)) {
			throwInvalidInputException(ResidentConstants.INDIVIDUAL_ID, "Request auth " +
					authTypeStatus.toString().toLowerCase() + " API");
		}
		if (StringUtils.isEmpty(requestDTO.getRequest().getOtp())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "otp",
					"Request auth " + authTypeStatus.toString().toLowerCase() + " API"));
			validateOTP(requestDTO.getRequest().getOtp());
		}

		if (StringUtils.isEmpty(requestDTO.getRequest().getTransactionID())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "transactionId",
					"Request auth " + authTypeStatus.toString().toLowerCase() + " API"));
			validateTransactionId(requestDTO.getRequest().getTransactionID());
		}
		List<String> authTypes = new ArrayList<String>();
		if (requestDTO.getRequest().getAuthType() != null && !requestDTO.getRequest().getAuthType().isEmpty()) {
			for(String authType:requestDTO.getRequest().getAuthType()) {
				String authTypeString = ResidentServiceImpl.getAuthTypeBasedOnConfig(authType);
				 authTypes.add(authTypeString);
			}
		}
		validateAuthType(authTypes,
				"Request auth " + authTypeStatus.toString().toLowerCase() + " API");
	}

	private void validateAuthorUnlockId(RequestWrapper<AuthLockOrUnLockRequestDto> requestDTO,
			AuthTypeStatus authTypeStatus) {
		if (authTypeStatus.equals(AuthTypeStatus.LOCK)) {
			validateRequest(requestDTO, RequestIdType.AUTH_LOCK_ID);
		} else {
			validateRequest(requestDTO, RequestIdType.AUTH_UNLOCK_ID);
		}
	}

	public void validateEuinRequest(RequestWrapper<EuinRequestDTO> requestDTO) {
		validateRequest(requestDTO, RequestIdType.E_UIN_ID);
		validateIndividualIdType(requestDTO.getRequest().getIndividualIdType(), "");
		if (StringUtils.isEmpty(requestDTO.getRequest().getIndividualId())
				|| (!validateIndividualIdvIdWithoutIdType(requestDTO.getRequest().getIndividualId()))) {
			throwInvalidInputException(ResidentConstants.INDIVIDUAL_ID, ResidentConstants.REQUEST_FOR_EUIN);
		}
		if (StringUtils.isEmpty(requestDTO.getRequest().getCardType())
				|| (!requestDTO.getRequest().getCardType().equalsIgnoreCase(CardType.UIN.name())
						&& !requestDTO.getRequest().getCardType().equalsIgnoreCase(CardType.MASKED_UIN.name()))) {
			throwInvalidInputException(ResidentConstants.CARD_TYPE, ResidentConstants.REQUEST_FOR_EUIN);
		}
		if (StringUtils.isEmpty(requestDTO.getRequest().getOtp())) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "otp", "Request for EUIN"));
			validateOTP(requestDTO.getRequest().getOtp());
		}
		if (StringUtils.isEmpty(requestDTO.getRequest().getTransactionID())) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "transactionId", "Request for EUIN"));
			validateTransactionId(requestDTO.getRequest().getTransactionID());
		}
	}

	public void validateAuthHistoryRequest(@Valid RequestWrapper<AuthHistoryRequestDTO> requestDTO) {
		validateRequest(requestDTO, RequestIdType.AUTH_HISTORY_ID);
		if (StringUtils.isEmpty(requestDTO.getRequest().getIndividualId())) {
			validateIndividualIdV2(requestDTO.getRequest().getIndividualId());
		}
		if (StringUtils.isEmpty(requestDTO.getRequest().getOtp())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "otp", "Request for auth history"));
			validateOTP(requestDTO.getRequest().getOtp());
		}

		if (StringUtils.isEmpty(requestDTO.getRequest().getTransactionID())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "transactionId",
					"Request for auth history"));
			validateTransactionId(requestDTO.getRequest().getTransactionID());
		}
		validatePagefetchAndPageStart(requestDTO, "Request for auth history");
	}

	public void validatePagefetchAndPageStart(RequestWrapper<AuthHistoryRequestDTO> requestDTO, String msg) {
		if (requestDTO.getRequest().getPageFetch() != null && requestDTO.getRequest().getPageFetch().trim().isEmpty()
				&& requestDTO.getRequest().getPageStart() != null
				&& requestDTO.getRequest().getPageStart().trim().isEmpty()) {
			throwInvalidInputException(PAGE_START_PAGE_FETCH, PAGE_NUMBER_ERROR);
		}
		if (requestDTO.getRequest().getPageFetch() != null && requestDTO.getRequest().getPageFetch().trim().isEmpty()
				&& StringUtils.isEmpty(requestDTO.getRequest().getPageStart())) {
			throwInvalidInputException(PAGE_START_PAGE_FETCH, PAGE_NUMBER_ERROR);
		}
		validatePageFetchAndPageStartEmptyCheck(requestDTO, msg);
		validatePageFetchAndPageStartFormat(requestDTO, msg);
	}

	private void validatePageFetchAndPageStartEmptyCheck(RequestWrapper<AuthHistoryRequestDTO> requestDTO, String msg) {
		if (StringUtils.isEmpty(requestDTO.getRequest().getPageFetch())
				&& requestDTO.getRequest().getPageStart() != null
				&& requestDTO.getRequest().getPageStart().trim().isEmpty()) {
			throwInvalidInputException(PAGE_START_PAGE_FETCH, PAGE_NUMBER_ERROR);
		}
		if (StringUtils.isEmpty(requestDTO.getRequest().getPageFetch())
				&& StringUtils.isNotEmpty(requestDTO.getRequest().getPageStart())) {
			throwInvalidInputException(PAGE_START_PAGE_FETCH, PAGE_NUMBER_ERROR);
		}
		if (StringUtils.isEmpty(requestDTO.getRequest().getPageStart())
				&& StringUtils.isNotEmpty(requestDTO.getRequest().getPageFetch())) {
			throwInvalidInputException(PAGE_START_PAGE_FETCH, PAGE_NUMBER_ERROR);
		}
	}

	public void validatePageFetchAndPageStartFormat(RequestWrapper<AuthHistoryRequestDTO> requestDTO, String msg) {

		if (!(StringUtils.isEmpty(requestDTO.getRequest().getPageStart())
				|| StringUtils.isEmpty(requestDTO.getRequest().getPageFetch()))) {
			if (isNumeric(requestDTO.getRequest().getPageStart())) {
				throwInvalidInputException(ResidentConstants.PAGE_START,
						ResidentConstants.PAGE_START + ResidentConstants.MUST_NOT_BE_EMPTY);
			}
			if (isNumeric(requestDTO.getRequest().getPageFetch())) {
				throwInvalidInputException(ResidentConstants.PAGE_FETCH,
						ResidentConstants.PAGE_FETCH + ResidentConstants.MUST_NOT_BE_EMPTY);
			}
			if (Integer.parseInt(requestDTO.getRequest().getPageStart()) < 1
					|| Integer.parseInt(requestDTO.getRequest().getPageFetch()) < 1) {
				throwInvalidInputException(PAGE_START_PAGE_FETCH,
						ResidentConstants.PAGE_FETCH_PAGE_START_GREATER_THAN_ZERO);
			}
		}
	}

	public void validateAuthType(List<String> authType, String msg) {
		if (authType == null || authType.isEmpty()) {
			throwInvalidInputException(ResidentConstants.AUTH_TYPES, msg);
		}
		String[] authTypesArray = authTypes.split(",");
		List<String> authTypesAllowed = new ArrayList<>(Arrays.asList(authTypesArray));
		for (String type : authType) {
			if (!authTypesAllowed.contains(type)) {
				throwInvalidInputException(ResidentConstants.AUTH_TYPES, msg);
			}
		}
	}

	public boolean phoneValidator(String phone) {
		return phone.matches(phoneRegex);
	}

	public boolean emailValidator(String email) {
		return email.matches(emailRegex);
	}

	public boolean validateVid(String individualId) {
		try {
			return vidValidator.validateId(individualId);
		} catch (InvalidIDException e) {
			return false;
		}
	}

	public boolean validateUin(String individualId) {
		try {
			return uinValidator.validateId(individualId);
		} catch (InvalidIDException e) {
			return false;
		}
	}

	public boolean validateRid(String individualId) {
		try {
			return ridValidator.validateId(individualId);
		} catch (InvalidIDException e) {
			return false;
		}
	}

	public void validateVidRevokeRequest(RequestWrapper<? extends BaseVidRevokeRequestDTO> requestDto, boolean isOtpValidationRequired, String individualId) {
		validateRevokeVidRequestWrapper(requestDto,"Request to revoke VID");
		if (StringUtils.isEmpty(requestDto.getRequest().getVidStatus())
				|| !requestDto.getRequest().getVidStatus().equalsIgnoreCase("REVOKED")) {
			throwInvalidInputException(ResidentConstants.VID_STATUS, REQUEST_REVOKE_VID);
		}
		if(requestDto.getRequest() instanceof VidRevokeRequestDTO) {
			VidRevokeRequestDTO vidRevokeRequestDTO = (VidRevokeRequestDTO) requestDto.getRequest();
			if (StringUtils.isEmpty(vidRevokeRequestDTO.getIndividualId())
					|| (!validateIndividualIdvIdWithoutIdType(vidRevokeRequestDTO.getIndividualId()))) {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individualId", "Request to revoke VID"));
				validateIndividualIdV2(vidRevokeRequestDTO.getIndividualId());
			}
			if (isOtpValidationRequired && StringUtils.isEmpty(vidRevokeRequestDTO.getOtp())) {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "otp", "Request to revoke VID"));
				validateOTP(vidRevokeRequestDTO.getOtp());
			}
		}
		if (StringUtils.isEmpty(requestDto.getRequest().getTransactionID())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "transactionId", "Request to revoke VID"));
			validateTransactionId(requestDto.getRequest().getTransactionID());
		}
	}

	public void validateRevokeVidRequestWrapper(RequestWrapper<?> request,String msg) {
		if (StringUtils.isEmpty(request.getId()) || !request.getId().equalsIgnoreCase(revokeVidId)) {
			throwInvalidInputException(ResidentConstants.REVOKE_VID, ResidentConstants.REVOKE_VID);
		}
		try {
			DateUtils.parseToLocalDateTime(request.getRequesttime());
		} catch (Exception e) {
			throwInvalidInputException(REQUEST_TIME, REQUEST_TIME);
		}
		if (StringUtils.isEmpty(request.getVersion()) || !request.getVersion().equalsIgnoreCase(version)) {
			throwInvalidInputException(ResidentConstants.VERSION, ResidentConstants.VERSION);
		}
		if (request.getRequest() == null) {
			audit.setAuditRequestDto(EventEnum.INPUT_DOESNT_EXISTS);
			throwInvalidInputException(REQUEST, REQUEST);
		}
	}
	
	public void validateVidRevokeV2Request(RequestWrapper<? extends BaseVidRevokeRequestDTO> requestDto, boolean isOtpValidationRequired, String individualId) {
		validateRevokeVidV2RequestWrapper(requestDto,"Request to revoke VID");
		if (StringUtils.isEmpty(requestDto.getRequest().getVidStatus())
				|| !requestDto.getRequest().getVidStatus().equalsIgnoreCase("REVOKED")) {
			throwInvalidInputException(ResidentConstants.VID_STATUS, REQUEST_REVOKE_VID);
		}

		if(requestDto.getRequest() instanceof VidRevokeRequestDTO) {
			VidRevokeRequestDTO vidRevokeRequestDTO = (VidRevokeRequestDTO) requestDto.getRequest();
			if (StringUtils.isEmpty(vidRevokeRequestDTO.getIndividualId())) {
				validateIndividualIdV2(vidRevokeRequestDTO.getIndividualId());
			}
			if (isOtpValidationRequired && StringUtils.isEmpty(vidRevokeRequestDTO.getOtp())) {
				validateOTP(vidRevokeRequestDTO.getOtp());
			}
		}
		if (StringUtils.isEmpty(requestDto.getRequest().getTransactionID())) {
			validateTransactionId(requestDto.getRequest().getTransactionID());
		}
	}

	public void validateRevokeVidV2RequestWrapper(RequestWrapper<?> request,String msg) {
		if (StringUtils.isEmpty(request.getId()) || !request.getId().equalsIgnoreCase(revokeVidIdNew)) {
			throwInvalidInputException(ResidentConstants.REVOKE_VID, REQUEST_REVOKE_VID);
		}
		try {
			DateUtils.parseToLocalDateTime(request.getRequesttime());
		} catch (Exception e) {
			throwInvalidInputException(REQUEST_TIME, REQUEST_TIME);
		}
		if (StringUtils.isEmpty(request.getVersion()) || !request.getVersion().equalsIgnoreCase(revokeVidVersion)) {
			throwInvalidInputException(ResidentConstants.VERSION, REQUEST_REVOKE_VID);
		}
		if (request.getRequest() == null) {
			throwInvalidInputException(REQUEST, REQUEST);
		}
	}

	public boolean validateRequest(RequestWrapper<?> request, RequestIdType requestIdType) {
		if (StringUtils.isEmpty(request.getId()) || !request.getId().equals(map.get(requestIdType))){
			throwInvalidInputException(ResidentConstants.ID, ResidentConstants.ID);
		}
		try {
			DateUtils.parseToLocalDateTime(request.getRequesttime());
		} catch (Exception e) {
			throwInvalidInputException("requesttime", "requesttime");
		}
		if (StringUtils.isEmpty(request.getVersion()) || !request.getVersion().equals(version))
			throwInvalidInputException(VERSION, VERSION);
		
		if (request.getRequest() == null) {
			audit.setAuditRequestDto(EventEnum.INPUT_DOESNT_EXISTS);
			throwInvalidInputException(REQUEST, REQUEST);
		}
		return true;

	}
	
	public boolean validateAidStatusRequest(RequestWrapper<?> request, RequestIdType requestIdType) {
		if (StringUtils.isEmpty(request.getId()) || !request.getId().equals(map.get(requestIdType)))
			throwInvalidInputException(ResidentConstants.ID, ResidentConstants.ID);
		try {
			DateUtils.parseToLocalDateTime(request.getRequesttime());
		} catch (Exception e) {
			throwInvalidInputException(REQUEST_TIME, REQUEST_TIME);
		}
		if (StringUtils.isEmpty(request.getVersion()) || !request.getVersion().equals(newVersion))
			throwInvalidInputException(VERSION, VERSION);
		if (request.getRequest() == null) {
			audit.setAuditRequestDto(EventEnum.INPUT_DOESNT_EXISTS);
			throwInvalidInputException(REQUEST, REQUEST);
		}
		return true;

	}

	public static boolean isNumeric(String strNum) {
		return !strNum.matches(("[0-9]+"));
	}

	public void validateReprintRequest(RequestWrapper<ResidentReprintRequestDto> requestDTO) {
		validateRequest(requestDTO, RequestIdType.RE_PRINT_ID);
		validateIndividualIdType(requestDTO.getRequest().getIndividualIdType(), "Request for print UIN API");
		validateIndividualIdV2(requestDTO.getRequest().getIndividualId());
		if (StringUtils.isEmpty(requestDTO.getRequest().getCardType())
				|| (!requestDTO.getRequest().getCardType().equalsIgnoreCase(CardType.UIN.name())
						&& !requestDTO.getRequest().getCardType().equalsIgnoreCase(CardType.MASKED_UIN.name()))) {
			throwInvalidInputException(ResidentConstants.CARD_TYPE, REQUEST_PRINT_UIN);
		}
		if (StringUtils.isEmpty(requestDTO.getRequest().getOtp())) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "otp", "Request for print UIN API"));
			validateOTP(requestDTO.getRequest().getOtp());
		}
		if (StringUtils.isEmpty(requestDTO.getRequest().getTransactionID())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "transactionId",
					"Request for print UIN API"));
			validateTransactionId(requestDTO.getRequest().getTransactionID());
		}
	}

	public void validateUpdateRequest(RequestWrapper<ResidentUpdateRequestDto> requestDTO, boolean isPatch) {
		if (!isPatch) {
			validateRequest(requestDTO, RequestIdType.RES_UPDATE);
			validateIndividualIdType(requestDTO.getRequest().getIndividualIdType(), "Request for update uin");
			validateIndividualIdV2(requestDTO.getRequest().getIndividualId());
		} else {
			validateRequestNewApi(requestDTO, RequestIdType.RES_UPDATE);
			validateIndividualIdvIdWithoutIdType(requestDTO.getRequest().getIndividualId());
		}
		if (!isPatch && StringUtils.isEmpty(requestDTO.getRequest().getOtp())) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "otp", "Request for update uin"));
			validateOTP(requestDTO.getRequest().getOtp());
		}
		if (StringUtils.isEmpty(requestDTO.getRequest().getTransactionID())) {
			validateTransactionId(requestDTO.getRequest().getTransactionID());
		}
		if(!isPatch) {
			if (requestDTO.getRequest().getIdentityJson() == null || requestDTO.getRequest().getIdentityJson().isEmpty()) {
				throwInvalidInputException(ResidentConstants.IDENTITY_JSON, "Request for update uin");
			}
		}
	}

	public void validateRidCheckStatusRequestDTO(RequestWrapper<RequestDTO> requestDTO) {
		validateRequest(requestDTO, RequestIdType.CHECK_STATUS);
		if (StringUtils.isEmpty(requestDTO.getRequest().getIndividualIdType())
				|| (!requestDTO.getRequest().getIndividualIdType().equalsIgnoreCase(IdType.RID.name()))) {
			throwInvalidInputException("individual type", "get RID status");
		}
		if (StringUtils.isEmpty(requestDTO.getRequest().getIndividualId())) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individual Id", "get RID status"));
			validateIndividualIdV2(requestDTO.getRequest().getIndividualId());
		}
	}

	private void validateIndividualIdType(String individualIdType, String typeofRequest) {
		if (StringUtils.isEmpty(individualIdType) || (!individualIdType.equalsIgnoreCase(IdType.UIN.name())
				&& !individualIdType.equalsIgnoreCase(IdType.VID.name()))) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individual type", typeofRequest));
			throwInvalidInputException("individual type", typeofRequest);
		}
	}

	public void validateAuthUnlockRequest(RequestWrapper<AuthUnLockRequestDTO> requestDTO,
			AuthTypeStatus authTypeStatus) {
		validateRequest(requestDTO, RequestIdType.AUTH_UNLOCK_ID);
		String individualId = requestDTO.getRequest().getIndividualId();
		if (StringUtils.isEmpty(individualId)
				|| !validateIndividualIdvIdWithoutIdType(individualId)) {
			throwInvalidInputException(ResidentConstants.INDIVIDUAL_ID,
					"Request auth " + authTypeStatus.toString().toLowerCase() + " API");
		}
		if (StringUtils.isEmpty(requestDTO.getRequest().getOtp())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "otp",
					"Request auth " + authTypeStatus.toString().toLowerCase() + " API"));
			validateOTP(requestDTO.getRequest().getOtp());
		}
		if (StringUtils.isEmpty(requestDTO.getRequest().getTransactionID())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "transactionId",
					"Request auth " + authTypeStatus.toString().toLowerCase() + " API"));
			validateTransactionId(requestDTO.getRequest().getTransactionID());
		}
		List<String> authTypes = new ArrayList<String>();
		if (requestDTO.getRequest().getAuthType() != null && !requestDTO.getRequest().getAuthType().isEmpty()) {
			for(String authType:requestDTO.getRequest().getAuthType()) {
				String authTypeString = ResidentServiceImpl.getAuthTypeBasedOnConfig(authType);
				 authTypes.add(authTypeString);
			}
		}
		validateAuthType(authTypes,
				"Request auth " + authTypeStatus.toString().toLowerCase() + " API");
		if (StringUtils.isEmpty(requestDTO.getRequest().getUnlockForSeconds()) || isNumeric(requestDTO.getRequest().getUnlockForSeconds())) {
			throwInvalidInputException(ResidentConstants.UNLOCK_FOR_SECONDS,
					"Request auth " + authTypeStatus.toString().toLowerCase() + " API");
		}
		long unlockSeconds = Long.parseLong(requestDTO.getRequest().getUnlockForSeconds());
		validateUnlockForSeconds(unlockSeconds,
				"Request auth " + authTypeStatus.toString().toLowerCase() + " API");

	}

	private void validateUnlockForSeconds(Long unlockForSeconds, String message) {
		if (unlockForSeconds != null) {
			if (unlockForSeconds < 0) {
				throwInvalidInputException(ResidentConstants.UNLOCK_FOR_SECONDS,
						"UnlockForSeconds must be greater than or equal to 0 "+message);
			}
		}
	}

	private boolean validateIndividualIdvIdWithoutIdType(String individualId) {
		try {
			return this.validateUin(individualId) || this.validateVid(individualId) || this.validateRid(individualId);
		} catch (InvalidIDException e) {
			return false;
		}
	}

	public void validateAidStatusRequestDto(RequestWrapper<AidStatusRequestDTO> reqDto) throws ResidentServiceCheckedException {
		validateAidStatusRequest(reqDto, RequestIdType.CHECK_STATUS);
		validateIndividualIdV2(reqDto.getRequest().getIndividualId());
	}

    public void validateChannelVerificationStatus(String channel, String individualId) {
		if (StringUtils.isEmpty(channel) || !channel.equalsIgnoreCase(PHONE_CHANNEL)
				&& !channel.equalsIgnoreCase(EMAIL_CHANNEL) ) {
			throwInvalidInputException("channel", "Request channel verification API");
		}
		validateIndividualIdV2(individualId);
    }

    public void validateServiceHistoryRequest(LocalDate fromDateTime, LocalDate toDateTime, String sortType, String serviceType, String statusFilter) {
		validateServiceType(serviceType, "Request service history API");
		validateSortType(sortType, "Request service history API");
		validateStatusFilter(statusFilter, "Request service history API");
		validateFromDateTimeToDateTime(fromDateTime, toDateTime, "Request service history API");
		if(!isValidDate(fromDateTime) || !isValidDate(toDateTime)) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "fromDateTime", "Request service history API"));
			throw new InvalidInputException("DateTime");
		}
	}

	public void validateFromDateTimeToDateTime(LocalDate fromDateTime, LocalDate toDateTime, String request_service_history_api) {
		if(fromDateTime == null && toDateTime != null) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, ResidentConstants.FROM_DATE_TIME,
					request_service_history_api));
			throw new InvalidInputException(ResidentConstants.FROM_DATE_TIME);
		} else if(fromDateTime != null && toDateTime == null) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, ResidentConstants.TO_DATE_TIME,
					request_service_history_api));
			throw new InvalidInputException(ResidentConstants.TO_DATE_TIME);
		}
	}

	private void validateStatusFilter(String statusFilter, String request_service_history_api) {
		if(statusFilter != null) {
			List<String> statusFilterList = Arrays.asList(statusFilter.split(","));
			for (String status : statusFilterList) {
				if (EventStatus.getEventStatusForText(status).isEmpty()) {
					throwInvalidInputException("statusFilter", "statusFilter");
				}
			}
		}
	}

	private boolean isValidDate(LocalDate localDateTime) {
		if(localDateTime!=null) {
			if (localDateTime.getYear() < 0 || localDateTime.getMonthValue() < 0 || localDateTime.getDayOfMonth() < 0) {
				return false;
			}
		}
		return true;
	}

	private void validateSortType(String sortType, String requestServiceHistoryApi) {
		if(sortType!=null) {
			if (!sortType.equalsIgnoreCase(SortType.ASC.toString())
					&& !sortType.equalsIgnoreCase(SortType.DESC.toString())) {
				throwInvalidInputException("sortType", "sortType");
			}
		}
	}

	private void validateServiceType(String serviceType, String requestServiceHistoryApi) {
		if(serviceType!=null) {
			List<String> serviceTypes = List.of(serviceType.split(","));
			for (String service : serviceTypes) {
				Optional<ServiceType> serviceOptional = ServiceType.getServiceTypeFromString(service);
				if(serviceOptional.isEmpty()) {
					throwInvalidInputException("serviceType", "serviceType");
				}
			}
		}
	}

	public void validateEventId(String eventId) {
		validateMissingInputParameter(eventId, TemplateVariablesConstants.EVENT_ID);
		if (isNumeric(eventId) || eventId.length()!=EVENT_ID_LENGTH) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					TemplateVariablesConstants.EVENT_ID, VALIDATE_EVENT_ID));
			throw new InvalidInputException(TemplateVariablesConstants.EVENT_ID);
		}
	}

	private void validateMissingInputParameter(String variableValue, String variableName) {
		if (variableValue==null || StringUtils.isEmpty(variableValue)) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					variableName, VALIDATE_EVENT_ID));
			throw new ResidentServiceException(ResidentErrorCode.MISSING_INPUT_PARAMETER, variableName);
		}
	}

	public void validateEventIdLanguageCode(String eventId, String languageCode) {
		validateEventId(eventId);
		validateLanguageCode(languageCode);
	}

	public void validateOnlyLanguageCode(String languageCode) {
		validateLanguageCode(languageCode);
	}
	
	private void validateLanguageCode(String languageCode) {
		List<String> allowedMandatoryLanguage = List.of(mandatoryLanguages.split(","));
		List<String> allowedOptionalLanguage = List.of(optionalLanguages.split(","));
		if(StringUtils.isEmpty(languageCode)) {
			throwInvalidInputException("languageCode", "languageCode");
		}
		if(!allowedMandatoryLanguage.contains(languageCode) && !allowedOptionalLanguage.contains(languageCode)) {
			throwInvalidInputException("languageCode", "languageCode");
		}
	}

	public void validateId(io.mosip.preregistration.core.common.dto.MainRequestDTO<TransliterationRequestDTO> requestDTO) {
		if (Objects.nonNull(requestDTO.getId())) {
			if (!requestDTO.getId().equals(transliterateId)) {
				throwInvalidInputException(ResidentConstants.ID, "Invalid Transliterate id");
			}
		} else {
			throwInvalidInputException(ResidentConstants.ID, "id is null");
		}
	}

    public List<String> validateUserIdAndTransactionId(String userId, String transactionID) {
		validateTransactionId(transactionID);
		List<String> list = new ArrayList<>();
		if (userId == null || userId.isEmpty()) {
			throwInvalidInputException("userId", "userId is null");
		}
		if (phoneValidator(userId)) {
			list.add(mobileChannel);
			return list;
		} else if (emailValidator(userId)) {
			list.add(emailChannel);
			return list;
		}
		throw new InvalidInputException("userId");
    }

	public void validateTransactionId(String transactionID) {
		if(transactionID== null || transactionID.isEmpty()){
			throwInvalidInputException(TemplateVariablesConstants.TRANSACTION_ID, "transactionID must not be null");
		} else if(isNumeric(transactionID) || transactionID.length()!=10){
			throwInvalidInputException(TemplateVariablesConstants.TRANSACTION_ID,
					"transactionID must be 10 digit containing numbers");
		}
	}
	
	public void validateProxySendOtpRequest(MainRequestDTO<OtpRequestDTOV2> userOtpRequest) {
		validateRequestType(userOtpRequest.getId(), this.environment.getProperty(ResidentConstants.RESIDENT_CONTACT_DETAILS_SEND_OTP_ID), ID);
		validateVersion(userOtpRequest.getVersion());
		validateDate(userOtpRequest.getRequesttime());
		validateUserIdAndTransactionId(userOtpRequest.getRequest().getUserId(), userOtpRequest.getRequest().getTransactionId());
	}

	public void validateUpdateDataRequest(MainRequestDTO<OtpRequestDTOV3> userIdOtpRequest) {
		String inputRequestId = userIdOtpRequest.getId();
		String requestIdStoredInProperty = this.environment.getProperty(ResidentConstants.RESIDENT_CONTACT_DETAILS_UPDATE_ID);
		validateRequestType(inputRequestId, requestIdStoredInProperty, ID);
		validateVersion(userIdOtpRequest.getVersion());
		validateDate(userIdOtpRequest.getRequesttime());
		validateUserIdAndTransactionId(userIdOtpRequest.getRequest().getUserId(), userIdOtpRequest.getRequest().getTransactionId());
		validateOTP(userIdOtpRequest.getRequest().getOtp());
	}

	public void validateOTP(String otp) {
		if(otp==null || otp.isEmpty()){
			throwInvalidInputException(TemplateVariablesConstants.OTP,
					"otp must not be null");
		} else if(isNumeric(otp)){
			throwInvalidInputException(TemplateVariablesConstants.OTP,
					"otp id invalid");
		}
	}

	public void validateRequestType(String inputRequestType, String requestTypeStoredInProperty, String type) {
		if(inputRequestType==null){
			throwInvalidInputException("request "+type, type+" must not be null");
		} else if(!inputRequestType.equalsIgnoreCase(requestTypeStoredInProperty)){
			throwInvalidInputException("request "+type, type+" is invalid");
		}
	}

	public void validateDate(Date requesttime) {
		if(requesttime==null) {
			throwInvalidInputException(REQUEST_TIME, "Request time invalid");
		}
	}

	public void validateDownloadCardRequest(MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO) {
		validateRequestType(downloadCardRequestDTOMainRequestDTO.getId(), this.environment.getProperty(ResidentConstants.DOWNLOAD_UIN_CARD_ID), ID);
		validateDate(downloadCardRequestDTOMainRequestDTO.getRequesttime());
		validateTransactionId(downloadCardRequestDTOMainRequestDTO.getRequest().getTransactionId());
		validateOTP(downloadCardRequestDTOMainRequestDTO.getRequest().getOtp());
		validateIndividualIdV2(downloadCardRequestDTOMainRequestDTO.getRequest().getIndividualId());
	}

	private void validateIndividualIdV2(String individualId) {
		if (individualId == null || StringUtils.isEmpty(individualId) || !validateIndividualIdvIdWithoutIdType(individualId)) {
			throwInvalidInputException(ResidentConstants.INDIVIDUAL_ID,
					ResidentConstants.INDIVIDUAL_ID + ResidentConstants.MUST_NOT_BE_EMPTY);
		}
	}

    public void validateDownloadPersonalizedCard(MainRequestDTO<DownloadPersonalizedCardDto> downloadPersonalizedCardMainRequestDTO) {
		validateRequestType(downloadPersonalizedCardMainRequestDTO.getId(),
				this.environment.getProperty(ResidentConstants.MOSIP_RESIDENT_DOWNLOAD_PERSONALIZED_CARD_ID), ID);
		validateVersion(downloadPersonalizedCardMainRequestDTO.getVersion());
		validateDate(downloadPersonalizedCardMainRequestDTO.getRequesttime());
		validateString(downloadPersonalizedCardMainRequestDTO.getRequest().getHtml(), "html");
		validateEncodedString(downloadPersonalizedCardMainRequestDTO.getRequest().getHtml());
		validateAttributeList(downloadPersonalizedCardMainRequestDTO.getRequest().getAttributes());
    }

	public void validateVersion(String requestVersion) {
		if (StringUtils.isEmpty(requestVersion) || !requestVersion.equals(reqResVersion))
			throwInvalidInputException(ResidentConstants.VERSION, ResidentConstants.VERSION);
	}

	private void validateEncodedString(String html) {
		try{
			CryptoUtil.decodePlainBase64(html);
		}catch (Exception e){
			throwInvalidInputException("html", "html"+e);
		}
	}

	private void validateString(String string, String variableName) {
		if(string == null || string.isEmpty()){
			audit.setAuditRequestDto(EventEnum.INPUT_INVALID);
			throw new InvalidInputException(variableName);
		}
	}

	public void validateDownloadCardVid(String vid) {
		if(!validateVid(vid)){
			audit.setAuditRequestDto(EventEnum.INPUT_INVALID);
			throwInvalidInputException(TemplateVariablesConstants.VID, TemplateVariablesConstants.VID + ResidentConstants.MUST_NOT_BE_EMPTY);
		}
	}
	
	public boolean validateRequestNewApi(RequestWrapper<?> request, RequestIdType requestIdType) {
		if (StringUtils.isEmpty(request.getId()) || !request.getId().equals(map.get(requestIdType)))
			throwInvalidInputException(ResidentConstants.ID, ResidentConstants.ID + ResidentConstants.MUST_NOT_BE_EMPTY);
		try {
			DateUtils.parseToLocalDateTime(request.getRequesttime());
		} catch (Exception e) {
			throwInvalidInputException(REQUEST_TIME, REQUEST_TIME + ResidentConstants.MUST_NOT_BE_EMPTY);
		}
		if (StringUtils.isEmpty(request.getVersion()) || !request.getVersion().equals(reqResVersion)) {
			throwInvalidInputException(ResidentConstants.VERSION, ResidentConstants.VERSION + ResidentConstants.MUST_NOT_BE_EMPTY);
		}
		if (request.getRequest() == null) {
			throwInvalidInputException(REQUEST, REQUEST + ResidentConstants.MUST_NOT_BE_EMPTY);
		}
		return true;
	}

    public void validateGrievanceRequestDto(MainRequestDTO<GrievanceRequestDTO> grievanceRequestDTOMainRequestDTO) throws ResidentServiceCheckedException, ApisResourceAccessException {
		validateRequestType(grievanceRequestDTOMainRequestDTO.getId(),
				this.environment.getProperty(ResidentConstants.GRIEVANCE_REQUEST_ID), ID);
		validateRequestType(grievanceRequestDTOMainRequestDTO.getVersion(),
				this.environment.getProperty(ResidentConstants.GRIEVANCE_REQUEST_VERSION), VERSION);
		validateDate(grievanceRequestDTOMainRequestDTO.getRequesttime());
		validateEventId(grievanceRequestDTOMainRequestDTO.getRequest().getEventId());
		validateEventIdBelongToSameSession(grievanceRequestDTOMainRequestDTO.getRequest().getEventId());
		validateEmailId(grievanceRequestDTOMainRequestDTO.getRequest().getEmailId());
		validateEmailId(grievanceRequestDTOMainRequestDTO.getRequest().getAlternateEmailId());
		validatePhoneNumber(grievanceRequestDTOMainRequestDTO.getRequest().getPhoneNo());
		validatePhoneNumber(grievanceRequestDTOMainRequestDTO.getRequest().getAlternatePhoneNo());
		validateMessage(grievanceRequestDTOMainRequestDTO.getRequest().getMessage());
    }

	private void validateMessage(String message) {
		validateMissingInputParameter(message, MESSAGE_CODE);
		if(message.length()>Integer.parseInt(Objects.requireNonNull(this.environment.getProperty(
				ResidentConstants.MESSAGE_CODE_MAXIMUM_LENGTH)))){
			throwInvalidInputException(MESSAGE_CODE, MESSAGE_CODE+ResidentConstants.MUST_NOT_BE_EMPTY);
		}
	}

	private void validatePhoneNumber(String phoneNo) {
		if(phoneNo!=null){
			if(!phoneValidator(phoneNo)){
				throwInvalidInputException(PHONE_CHANNEL, PHONE_CHANNEL+ResidentConstants.MUST_NOT_BE_EMPTY);
			}
		}
	}

	private void validateEmailId(String emailId) {
		if(emailId!=null){
			if(!emailValidator(emailId)){
				throwInvalidInputException(EMAIL_CHANNEL, EMAIL_CHANNEL+ResidentConstants.MUST_NOT_BE_EMPTY);
			}
		}
	}

	private void validateEventIdBelongToSameSession(String eventId) throws ResidentServiceCheckedException, ApisResourceAccessException {
		Optional<ResidentTransactionEntity> residentTransactionEntity = residentTransactionRepository.findById(eventId);
		if(residentTransactionEntity.isPresent()){
			String tokenId = residentTransactionEntity.get().getTokenId();
			String sessionToken = identityService.getResidentIdaToken();
			if(!tokenId.equals(sessionToken)){
				throw new EidNotBelongToSessionException(ResidentErrorCode.EID_NOT_BELONG_TO_SESSION,
						ResidentErrorCode.EID_NOT_BELONG_TO_SESSION.getErrorMessage());
			}
		}
	}

	public void validateReqOtp(IndividualIdOtpRequestDTO individualIdRequestDto) {
		validateIndividualIdV2(individualIdRequestDto.getIndividualId());
		validateTransactionId(individualIdRequestDto.getTransactionId());
	}

	public void validateSharableAttributes(List<SharableAttributesDTO> sharableAttributes) {
		if(sharableAttributes.isEmpty()){
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					sharableAttributes.toString(), VALIDATE_EVENT_ID));
			throw new ResidentServiceException(ResidentErrorCode.MISSING_INPUT_PARAMETER, sharableAttributes.toString());
		}
	}

	private void validateAttributeList(List<String> attributes) {
		if(attributes.isEmpty()){
			throwInvalidInputException(TemplateVariablesConstants.ATTRIBUTES,
					TemplateVariablesConstants.ATTRIBUTES + ResidentConstants.MUST_NOT_BE_EMPTY);
		}
	}

	private void throwInvalidInputException(String variableName, String errorDescription){
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
				variableName, errorDescription));
		throw new InvalidInputException(variableName);
	}

	public void validatePurpose(String purpose) {
		if(purpose.isEmpty() || validateStringWithAlphaNumericCharacter(purpose)){
			validateString(purpose, TemplateVariablesConstants.PURPOSE);
		}
	}

	private boolean validateStringWithAlphaNumericCharacter(String purpose) {
		return !purpose.matches("[A-Za-z0-9 ]+");
	}
}
