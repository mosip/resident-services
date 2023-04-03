package io.mosip.resident.validator;

import static io.mosip.resident.constant.RegistrationConstants.MESSAGE_CODE;
import static io.mosip.resident.service.impl.ResidentOtpServiceImpl.EMAIL_CHANNEL;
import static io.mosip.resident.service.impl.ResidentOtpServiceImpl.PHONE_CHANNEL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

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
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.dto.IndividualIdOtpRequestDTO;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.dto.OtpRequestDTOV2;
import io.mosip.resident.dto.OtpRequestDTOV3;
import io.mosip.resident.dto.RequestDTO;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentCredentialRequestDto;
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
import io.mosip.resident.service.impl.UISchemaTypes;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;

@Component
public class RequestValidator {

	private static final String ID = "id";
	private static final String VERSION = "version";
	private static final String REQUESTTIME = "requesttime";
	private static final String REQUEST = "request";
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
	
	@Value("${resident.grievance-redressal.alt-email.chars.limit}")
	private int emailCharsLimit;
	
	@Value("${resident.grievance-redressal.alt-phone.chars.limit}")
	private int phoneCharsLimit;
	
	@Value("${resident.grievance-redressal.comments.chars.limit}")
	private int messageCharsLimit;   
	
	@Value("${resident.share-credential.purpose.chars.limit}")
	private int purposeCharsLimit;
	
	@Value("${mosip.resident.eventid.searchtext.length}")
	private int searchTextLength;
	
	@Value("${mosip.kernel.vid.length}")
	private int vidLength;
	
	@Value("${mosip.kernel.otp.default-length}")  
	private int otpLength;
	
	@Value("${resident.message.allowed.special.char.regex}") 
	private String messageAllowedSpecialCharRegex;
	
	@Value("${resident.purpose.allowed.special.char.regex}") 
	private String purposeAllowedSpecialCharRegex;
	
	@Value("${resident.id.allowed.special.char.regex}") 
	private String idAllowedSpecialCharRegex;

	@Value("${resident.validation.is-numeric.regex}")
	private String numericDataRegex;

	@Value("${resident.otp.validation.transaction-id.regex}")
	private String transactionIdRegex;

	@Value("${resident.validation.event-id.regex}")
	private String eventIdRegex;

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
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, REQUESTTIME, "Request to generate VID"));

			throw new InvalidInputException(REQUESTTIME);
		}

		if (StringUtils.isEmpty(requestDto.getId()) || !requestDto.getId().equalsIgnoreCase(id)) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, ID, "Request to generate VID"));

			throw new InvalidInputException(ID);
		}

		if (StringUtils.isEmpty(requestDto.getVersion()) || !requestDto.getVersion().equalsIgnoreCase(version)) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, VERSION, "Request to generate VID"));

			throw new InvalidInputException(VERSION);
		}

		if (requestDto.getRequest() == null) {
			audit.setAuditRequestDto(EventEnum.INPUT_DOESNT_EXISTS);
			throw new InvalidInputException(REQUEST);
		}

		if (StringUtils.isEmpty(individualId)
				|| !validateIndividualIdvIdWithoutIdType(individualId)) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individualId",
					"Request generate VID API"));
			throw new InvalidInputException("individualId");
		}

		BaseVidRequestDto vidRequestDto = requestDto.getRequest();
		if(vidRequestDto instanceof VidRequestDto) {
			if (otpValidationRequired && StringUtils.isEmpty(((VidRequestDto)vidRequestDto).getOtp())) {
				audit.setAuditRequestDto(
						EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "otp", "Request to generate VID"));

				throw new InvalidInputException("otp");
			}
		}

		if (StringUtils.isEmpty(requestDto.getRequest().getTransactionID())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "transactionId",
					"Request to generate VID"));

			throw new InvalidInputException("transactionId");
		}
	}

	public void validateVidCreateV2Request(IVidRequestDto<? extends BaseVidRequestDto> requestDto, boolean otpValidationRequired, String individualId) {

		try {
			DateUtils.parseToLocalDateTime(requestDto.getRequesttime());
		} catch (Exception e) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, REQUESTTIME, "Request to generate VID"));

			throw new InvalidInputException(REQUESTTIME);
		}

		if (StringUtils.isEmpty(requestDto.getId()) || !requestDto.getId().equalsIgnoreCase(generateId)) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "generateId", "Request to generate VID"));

			throw new InvalidInputException("generateId");
		}

		if (StringUtils.isEmpty(requestDto.getVersion()) || !requestDto.getVersion().equalsIgnoreCase(newVersion)) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "newVersion", "Request to generate VID"));

			throw new InvalidInputException("newVersion");
		}

		if (requestDto.getRequest() == null) {
			audit.setAuditRequestDto(EventEnum.INPUT_DOESNT_EXISTS);
			throw new InvalidInputException(REQUEST);
		}

		if (StringUtils.isEmpty(individualId)
				|| !validateIndividualIdvIdWithoutIdType(individualId)) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individualId",
					"Request generate VID API"));
			throw new InvalidInputException("individualId");
		}

		BaseVidRequestDto vidRequestDto = requestDto.getRequest();
		if(vidRequestDto instanceof VidRequestDto) {
			if (otpValidationRequired && StringUtils.isEmpty(((VidRequestDto)vidRequestDto).getOtp())) {
				audit.setAuditRequestDto(
						EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "otp", "Request to generate VID"));

				throw new InvalidInputException("otp");
			}
		}

		if (StringUtils.isEmpty(requestDto.getRequest().getTransactionID())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "transactionId",
					"Request to generate VID"));

			throw new InvalidInputException("transactionId");
		}
	}

	public void validateAuthLockOrUnlockRequestV2(RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestDto) {
		validateRequestNewApi(requestDto, RequestIdType.AUTH_LOCK_UNLOCK);
		validateAuthTypeV2(requestDto.getRequest().getAuthTypes());
	}

	private void validateAuthTypeV2(List<AuthTypeStatusDtoV2> authType) {
		if (authType == null || authType.isEmpty()) {
			audit.setAuditRequestDto(EventEnum.INPUT_DOESNT_EXISTS);
			throw new InvalidInputException("authTypes");
		}
		String[] authTypesArray = authTypes.split(",");
		List<String> authTypesAllowed = new ArrayList<>(Arrays.asList(authTypesArray));
		for (AuthTypeStatusDtoV2 authTypeStatusDto : authType) {
			String authTypeString = ResidentServiceImpl.getAuthTypeBasedOnConfigV2(authTypeStatusDto);
			if (StringUtils.isEmpty(authTypeString) || !authTypesAllowed.contains(authTypeString)) {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "authTypes",
						"Request to generate VID"));
				throw new InvalidInputException("authTypes");
			}
			if(!isValidUnlockForSeconds(authTypeStatusDto.getUnlockForSeconds())) {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "unlockForSeconds",
						"Request to generate VID"));
				throw new InvalidInputException("unlockForSeconds");
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
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individualId",
					"Request auth " + authTypeStatus.toString().toLowerCase() + " API"));
			throw new InvalidInputException("individualId");
		}
		if (StringUtils.isEmpty(requestDTO.getRequest().getOtp())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "otp",
					"Request auth " + authTypeStatus.toString().toLowerCase() + " API"));
			throw new InvalidInputException("otp");
		}

		if (StringUtils.isEmpty(requestDTO.getRequest().getTransactionID())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "transactionId",
					"Request auth " + authTypeStatus.toString().toLowerCase() + " API"));
			throw new InvalidInputException("transactionId");
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

		validateIndividualIdType(requestDTO.getRequest().getIndividualIdType(), "Request for EUIN");

		if (StringUtils.isEmpty(requestDTO.getRequest().getIndividualId())
				|| (!validateIndividualIdvIdWithoutIdType(requestDTO.getRequest().getIndividualId()))) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individualId", "Request for EUIN"));
			throw new InvalidInputException("individualId");
		}

		if (StringUtils.isEmpty(requestDTO.getRequest().getCardType())
				|| (!requestDTO.getRequest().getCardType().equalsIgnoreCase(CardType.UIN.name())
				&& !requestDTO.getRequest().getCardType().equalsIgnoreCase(CardType.MASKED_UIN.name()))) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "cardType", "Request for EUIN"));
			throw new InvalidInputException("cardType");
		}
		if (StringUtils.isEmpty(requestDTO.getRequest().getOtp())) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "otp", "Request for EUIN"));
			throw new InvalidInputException("otp");
		}
		if (StringUtils.isEmpty(requestDTO.getRequest().getTransactionID())) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "transactionId", "Request for EUIN"));
			throw new InvalidInputException("transactionId");
		}
	}

	public void validateAuthHistoryRequest(@Valid RequestWrapper<AuthHistoryRequestDTO> requestDTO) {
		validateRequest(requestDTO, RequestIdType.AUTH_HISTORY_ID);

		if (StringUtils.isEmpty(requestDTO.getRequest().getIndividualId()))
			throw new InvalidInputException("individualId");

		if (StringUtils.isEmpty(requestDTO.getRequest().getOtp())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "otp", "Request for auth history"));
			throw new InvalidInputException("otp");
		}

		if (StringUtils.isEmpty(requestDTO.getRequest().getTransactionID())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "transactionId",
					"Request for auth history"));
			throw new InvalidInputException("transactionId");
		}
		validatePagefetchAndPageStart(requestDTO, "Request for auth history");
	}

	public void validatePagefetchAndPageStart(RequestWrapper<AuthHistoryRequestDTO> requestDTO, String msg) {
		if (requestDTO.getRequest().getPageFetch() != null && requestDTO.getRequest().getPageFetch().trim().isEmpty()
				&& requestDTO.getRequest().getPageStart() != null
				&& requestDTO.getRequest().getPageStart().trim().isEmpty()) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					"please provide Page size and Page number to be fetched", msg));
			throw new InvalidInputException("please provide Page size and Page number to be Fetched");
		}

		if (requestDTO.getRequest().getPageFetch() != null && requestDTO.getRequest().getPageFetch().trim().isEmpty()
				&& StringUtils.isEmpty(requestDTO.getRequest().getPageStart())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					"please provide Page size and Page number to be fetched", msg));
			throw new InvalidInputException("please provide Page size and Page number to be Fetched");
		}

		validatePageFetchAndPageStartEmptyCheck(requestDTO, msg);
		validatePageFetchAndPageStartFormat(requestDTO, msg);
	}

	private void validatePageFetchAndPageStartEmptyCheck(RequestWrapper<AuthHistoryRequestDTO> requestDTO, String msg) {
		if (StringUtils.isEmpty(requestDTO.getRequest().getPageFetch())
				&& requestDTO.getRequest().getPageStart() != null
				&& requestDTO.getRequest().getPageStart().trim().isEmpty()) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					"please provide Page size and Page number to be fetched", msg));
			throw new InvalidInputException("please provide Page size and Page number to be Fetched");
		}
		if (StringUtils.isEmpty(requestDTO.getRequest().getPageFetch())
				&& StringUtils.isNotEmpty(requestDTO.getRequest().getPageStart())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					"please provide Page size to be fetched", msg));
			throw new InvalidInputException("please provide Page size to be Fetched");
		}

		if (StringUtils.isEmpty(requestDTO.getRequest().getPageStart())
				&& StringUtils.isNotEmpty(requestDTO.getRequest().getPageFetch())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					"please provide Page number to be fetched", msg));
			throw new InvalidInputException("please provide Page number to be Fetched");
		}
	}

	public void validatePageFetchAndPageStartFormat(RequestWrapper<AuthHistoryRequestDTO> requestDTO, String msg) {

		if (!(StringUtils.isEmpty(requestDTO.getRequest().getPageStart())
				|| StringUtils.isEmpty(requestDTO.getRequest().getPageFetch()))) {
			if (!isNumeric(requestDTO.getRequest().getPageStart())) {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "pageStart", msg));
				throw new InvalidInputException("pageStart");
			}
			if (!isNumeric(requestDTO.getRequest().getPageFetch())) {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "pageFetch", msg));
				throw new InvalidInputException("pageFetch");
			}
			if (Integer.parseInt(requestDTO.getRequest().getPageStart()) < 1
					|| Integer.parseInt(requestDTO.getRequest().getPageFetch()) < 1) {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
						"Page Fetch or Page Start must be greater than 0", msg));
				throw new InvalidInputException("Page Fetch or Page Start must be greater than 0");
			}
		}
	}

	public void validateAuthType(List<String> authType, String msg) {
		if (authType == null || authType.isEmpty()) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "authTypes", msg));
			throw new InvalidInputException("authTypes");
		}
		String[] authTypesArray = authTypes.split(",");
		List<String> authTypesAllowed = new ArrayList<>(Arrays.asList(authTypesArray));
		for (String type : authType) {
			if (!authTypesAllowed.contains(type)) {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "authTypes", msg));
				throw new InvalidInputException("authTypes");
			}
		}
	}

	public boolean phoneValidator(String phone) {
		return phone.matches(phoneRegex);
	}

	public boolean emailValidator(String email) {
		return email.matches(emailRegex);
	}
	
	public void emailCharsValidator(String email) {
		if (email.length() > emailCharsLimit) {
			throw new ResidentServiceException(ResidentErrorCode.CHAR_LIMIT_EXCEEDS.getErrorCode(),
					String.format(ResidentErrorCode.CHAR_LIMIT_EXCEEDS.getErrorMessage(), emailCharsLimit, email));
		}
	}
	
	public void phoneCharsValidator(String phoneNo) {
		if (phoneNo.length() > phoneCharsLimit) {
			throw new ResidentServiceException(ResidentErrorCode.CHAR_LIMIT_EXCEEDS.getErrorCode(),
					String.format(ResidentErrorCode.CHAR_LIMIT_EXCEEDS.getErrorMessage(), phoneCharsLimit, phoneNo));
		}
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
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "vidStatus", "Request to revoke VID"));
			throw new InvalidInputException("vidStatus");
		}

		if(requestDto.getRequest() instanceof VidRevokeRequestDTO) {
			VidRevokeRequestDTO vidRevokeRequestDTO = (VidRevokeRequestDTO) requestDto.getRequest();
			if (StringUtils.isEmpty(vidRevokeRequestDTO.getIndividualId())
					|| (!validateIndividualIdvIdWithoutIdType(vidRevokeRequestDTO.getIndividualId()))) {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individualId", "Request to revoke VID"));
				throw new InvalidInputException("individualId");
			}

			if (isOtpValidationRequired && StringUtils.isEmpty(vidRevokeRequestDTO.getOtp())) {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "otp", "Request to revoke VID"));
				throw new InvalidInputException("otp");
			}
		}

		if (StringUtils.isEmpty(requestDto.getRequest().getTransactionID())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "transactionId", "Request to revoke VID"));
			throw new InvalidInputException("transactionId");
		}
	}

	public void validateRevokeVidRequestWrapper(RequestWrapper<?> request,String msg) {

		if (StringUtils.isEmpty(request.getId()) || !request.getId().equalsIgnoreCase(revokeVidId)) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "revokeVidId", msg));
			throw new InvalidInputException("revokeVidId");
		}
		try {
			DateUtils.parseToLocalDateTime(request.getRequesttime());
		} catch (Exception e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, REQUESTTIME, msg));
			throw new InvalidInputException(REQUESTTIME);
		}

		if (StringUtils.isEmpty(request.getVersion()) || !request.getVersion().equalsIgnoreCase(version)) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, VERSION, msg));
			throw new InvalidInputException(VERSION);
		}
		if (request.getRequest() == null) {
			audit.setAuditRequestDto(EventEnum.INPUT_DOESNT_EXISTS);
			throw new InvalidInputException(REQUEST);
		}
	}

	public void validateVidRevokeV2Request(RequestWrapper<? extends BaseVidRevokeRequestDTO> requestDto, boolean isOtpValidationRequired, String individualId) {

		validateRevokeVidV2RequestWrapper(requestDto,"Request to revoke VID");

		if (StringUtils.isEmpty(requestDto.getRequest().getVidStatus())
				|| !requestDto.getRequest().getVidStatus().equalsIgnoreCase("REVOKED")) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "vidStatus", "Request to revoke VID"));
			throw new InvalidInputException("vidStatus");
		}

		if(requestDto.getRequest() instanceof VidRevokeRequestDTO) {
			VidRevokeRequestDTO vidRevokeRequestDTO = (VidRevokeRequestDTO) requestDto.getRequest();
			if (StringUtils.isEmpty(vidRevokeRequestDTO.getIndividualId())
					|| (!validateIndividualIdvIdWithoutIdType(vidRevokeRequestDTO.getIndividualId()))) {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individualId", "Request to revoke VID"));
				throw new InvalidInputException("individualId");
			}

			if (isOtpValidationRequired && StringUtils.isEmpty(vidRevokeRequestDTO.getOtp())) {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "otp", "Request to revoke VID"));
				throw new InvalidInputException("otp");
			}
		}

		if (StringUtils.isEmpty(requestDto.getRequest().getTransactionID())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "transactionId", "Request to revoke VID"));
			throw new InvalidInputException("transactionId");
		}
	}

	public void validateRevokeVidV2RequestWrapper(RequestWrapper<?> requestWrapper,String msg) {

		if (StringUtils.isEmpty(requestWrapper.getId()) || !requestWrapper.getId().equalsIgnoreCase(revokeVidIdNew)) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "revokeVidIdNew", msg));
			throw new InvalidInputException("revokeVidIdNew");
		}
		try {
			DateUtils.parseToLocalDateTime(requestWrapper.getRequesttime());
		} catch (Exception e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, REQUESTTIME, msg));
			throw new InvalidInputException(REQUESTTIME);
		}

		if (StringUtils.isEmpty(requestWrapper.getVersion()) || !requestWrapper.getVersion().equalsIgnoreCase(revokeVidVersion)) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "revokeVidVersion", msg));
			throw new InvalidInputException("revokeVidVersion");
		}
		validateAPIRequestToCheckNull(requestWrapper);
	}

	public boolean validateRequest(RequestWrapper<?> request, RequestIdType requestIdType) {
		if (StringUtils.isEmpty(request.getId()) || !request.getId().equals(map.get(requestIdType)))
			throw new InvalidInputException(ID);
		try {
			DateUtils.parseToLocalDateTime(request.getRequesttime());
		} catch (Exception e) {
			throw new InvalidInputException(REQUESTTIME);
		}
		if (StringUtils.isEmpty(request.getVersion()) || !request.getVersion().equals(version))
			throw new InvalidInputException(VERSION);

		if (request.getRequest() == null) {
			audit.setAuditRequestDto(EventEnum.INPUT_DOESNT_EXISTS);
			throw new InvalidInputException(REQUEST);
		}
		return true;

	}

	public boolean isNumeric(String strNum) {
		return strNum.matches(numericDataRegex);
	}

	public void validateReprintRequest(RequestWrapper<ResidentReprintRequestDto> requestDTO) {
		validateRequest(requestDTO, RequestIdType.RE_PRINT_ID);

		validateIndividualIdType(requestDTO.getRequest().getIndividualIdType(), "Request for print UIN API");

		if (StringUtils.isEmpty(requestDTO.getRequest().getIndividualId())
				|| (!validateIndividualIdvIdWithoutIdType(requestDTO.getRequest().getIndividualId()))) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individualId",
					"Request for print UIN API"));
			throw new InvalidInputException("individualId");
		}

		if (StringUtils.isEmpty(requestDTO.getRequest().getCardType())
				|| (!requestDTO.getRequest().getCardType().equalsIgnoreCase(CardType.UIN.name())
				&& !requestDTO.getRequest().getCardType().equalsIgnoreCase(CardType.MASKED_UIN.name()))) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "cardType", "Request for print UIN API"));
			throw new InvalidInputException("cardType");
		}

		if (StringUtils.isEmpty(requestDTO.getRequest().getOtp())) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "otp", "Request for print UIN API"));
			throw new InvalidInputException("otp");
		}

		if (StringUtils.isEmpty(requestDTO.getRequest().getTransactionID())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "transactionId",
					"Request for print UIN API"));
			throw new InvalidInputException("transactionId");
		}
	}

	public void validateUpdateRequest(RequestWrapper<ResidentUpdateRequestDto> requestDTO, boolean isPatch) {
		if (!isPatch) {
			validateRequest(requestDTO, RequestIdType.RES_UPDATE);
			validateIndividualIdType(requestDTO.getRequest().getIndividualIdType(), "Request for update uin");
			if (StringUtils.isEmpty(requestDTO.getRequest().getIndividualId())
					|| (!validateIndividualIdvIdWithoutIdType(requestDTO.getRequest().getIndividualId()))) {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individualId",
						"Request for update uin"));
				throw new InvalidInputException("individualId");
			}
		} else {
			validateRequestNewApi(requestDTO, RequestIdType.RES_UPDATE);
			validateIndividualIdvIdWithoutIdType(requestDTO.getRequest().getIndividualId());
			validateLanguageCodeInIdentityJson(requestDTO.getRequest().getIdentity());
		}
		if (!isPatch && StringUtils.isEmpty(requestDTO.getRequest().getOtp())) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "otp", "Request for update uin"));
			throw new InvalidInputException("otp");
		}

		if (StringUtils.isEmpty(requestDTO.getRequest().getTransactionID())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "transactionID",
					"Request for update uin"));
			throw new InvalidInputException("transactionID");
		}

		if(!isPatch) {
			if (requestDTO.getRequest().getIdentityJson() == null || requestDTO.getRequest().getIdentityJson().isEmpty()) {
				audit.setAuditRequestDto(
						EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "identityJson", "Request for update uin"));
				throw new InvalidInputException("identityJson");
			}
		}
	}

	private void validateLanguageCodeInIdentityJson(JSONObject identity) {
		if(identity!=null) {
			// Get a set of entries
			for (Map.Entry entry : (Iterable<Map.Entry>) identity.entrySet()) {
				// Retrieve the key and value of each entry
				String key = (String) entry.getKey();
				Object value = entry.getValue();
				if (value instanceof ArrayList<?>) {
					ArrayList<?> valueArray = (ArrayList<?>) value;
					for (Object valueInList : valueArray) {
						if (valueInList instanceof Map) {
							Map <String, String> valueInListMap = (Map <String, String>) valueInList;
							if (valueInListMap.containsKey(ResidentConstants.LANGUAGE)) {
								String languageCode = valueInListMap.get(ResidentConstants.LANGUAGE);
								validateMissingInputParameter(languageCode, ResidentConstants.LANGUAGE, EventEnum.INPUT_INVALID.getName());
								validateLanguageCode(languageCode);
								String valueOfLanguageCode = valueInListMap.get(ResidentConstants.VALUE);
								validateMissingInputParameter(valueOfLanguageCode, key+" "+languageCode+ " "+
										ResidentConstants.LANGUAGE +" "+ ResidentConstants.VALUE, EventEnum.INPUT_INVALID.getName());
							}
						}
					}
				}
			}
		}
	}

	public void validateRidCheckStatusRequestDTO(RequestWrapper<RequestDTO> requestDTO) {
		validateRequest(requestDTO, RequestIdType.CHECK_STATUS);

		if (StringUtils.isEmpty(requestDTO.getRequest().getIndividualIdType())
				|| (!requestDTO.getRequest().getIndividualIdType().equalsIgnoreCase(IdType.RID.name()))) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individual type", "get RID status"));
			throw new InvalidInputException("individualIdType");
		}
		if (StringUtils.isEmpty(requestDTO.getRequest().getIndividualId())) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individual Id", "get RID status"));
			throw new InvalidInputException("individualId");
		}

	}

	private void validateIndividualIdType(String individualIdType, String typeofRequest) {
		if (StringUtils.isEmpty(individualIdType) || (!individualIdType.equalsIgnoreCase(IdType.UIN.name())
				&& !individualIdType.equalsIgnoreCase(IdType.VID.name()))) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individual type", typeofRequest));
			throw new InvalidInputException("individualIdType");
		}
	}

	public void validateAuthUnlockRequest(RequestWrapper<AuthUnLockRequestDTO> requestDTO,
										  AuthTypeStatus authTypeStatus) {
		validateRequest(requestDTO, RequestIdType.AUTH_UNLOCK_ID);

		String individualId = requestDTO.getRequest().getIndividualId();
		if (StringUtils.isEmpty(individualId)
				|| !validateIndividualIdvIdWithoutIdType(individualId)) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individualId",
					"Request auth " + authTypeStatus.toString().toLowerCase() + " API"));
			throw new InvalidInputException("individualId");
		}
		if (StringUtils.isEmpty(requestDTO.getRequest().getOtp())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "otp",
					"Request auth " + authTypeStatus.toString().toLowerCase() + " API"));
			throw new InvalidInputException("otp");
		}

		if (StringUtils.isEmpty(requestDTO.getRequest().getTransactionID())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "transactionId",
					"Request auth " + authTypeStatus.toString().toLowerCase() + " API"));
			throw new InvalidInputException("transactionId");
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
		if (StringUtils.isEmpty(requestDTO.getRequest().getUnlockForSeconds()) || !isNumeric(requestDTO.getRequest().getUnlockForSeconds())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "unlockForSeconds",
					"Request auth " + authTypeStatus.toString().toLowerCase() + " API"));
			throw new InvalidInputException("UnlockForSeconds must be greater than or equal to 0");
		}
		long unlockSeconds = Long.parseLong(requestDTO.getRequest().getUnlockForSeconds());
		validateUnlockForSeconds(unlockSeconds,
				"Request auth " + authTypeStatus.toString().toLowerCase() + " API");

	}

	private void validateUnlockForSeconds(Long unlockForSeconds, String message) {
		if (unlockForSeconds != null) {
			if (unlockForSeconds < 0) {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
						"UnlockForSeconds must be greater than or equal to 0", message));
				throw new InvalidInputException("UnlockForSeconds must be greater than or equal to 0");
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
		validateRequestNewApi(reqDto, RequestIdType.CHECK_STATUS);
		validateTransactionId(reqDto.getRequest().getTransactionId());
		if(reqDto.getRequest().getIndividualId() == null) {
			throw new InvalidInputException("individualId");
		}

	}

	public void validateChannelVerificationStatus(String channel, String individualId) {
		if (StringUtils.isEmpty(channel) || !channel.equalsIgnoreCase(PHONE_CHANNEL)
				&& !channel.equalsIgnoreCase(EMAIL_CHANNEL) ) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "channel", "Request channel verification API"));
			throw new InvalidInputException("channel");
		}
		if (StringUtils.isEmpty(individualId) || !validateIndividualIdvIdWithoutIdType(individualId)) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individualId", "Request channel verification API"));
			throw new InvalidInputException("individualId");
		}
		if (!individualId.matches(idAllowedSpecialCharRegex)) {
			throw new ResidentServiceException(ResidentErrorCode.CONTAINS_SPECIAL_CHAR.getErrorCode(),
					String.format(ResidentErrorCode.CONTAINS_SPECIAL_CHAR.getErrorMessage(),individualId));
		}
		if (individualId.length() > vidLength) {
			throw new ResidentServiceException(ResidentErrorCode.CHAR_LIMIT_EXCEEDS.getErrorCode(),
					String.format(ResidentErrorCode.CHAR_LIMIT_EXCEEDS.getErrorMessage(),vidLength,individualId));
		}
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
	
	public void validateSearchText(String searchText) {
		if (searchText != null) {
			if (searchText.length() > searchTextLength) {
				throw new ResidentServiceException(ResidentErrorCode.CHAR_LIMIT_EXCEEDS.getErrorCode(), String
						.format(ResidentErrorCode.CHAR_LIMIT_EXCEEDS.getErrorMessage(),searchTextLength,searchText));
			}
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
					audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "statusFilter",
							request_service_history_api));
					throw new InvalidInputException("statusFilter");
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
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "sortType",
						requestServiceHistoryApi));
				throw new InvalidInputException("sortType");
			}
		}
	}

	private void validateServiceType(String serviceType, String requestServiceHistoryApi) {
		if(serviceType!=null) {
			List<String> serviceTypes = List.of(serviceType.split(","));
			for (String service : serviceTypes) {
				Optional<ServiceType> serviceOptional = ServiceType.getServiceTypeFromString(service);
				if(serviceOptional.isEmpty()) {
					audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "serviceType",
							requestServiceHistoryApi));
					throw new InvalidInputException("serviceType");
				}
			}
		}
	}

	public void validateSchemaType(String schemaType) {
		Optional<UISchemaTypes> uiSchemaTypeOptional = UISchemaTypes.getUISchemaTypeFromString(schemaType);
		if (uiSchemaTypeOptional.isEmpty()) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					ResidentConstants.SCHEMA_TYPE, "Validating schema type"));
			throw new InvalidInputException(ResidentConstants.SCHEMA_TYPE + ". Valid values are "
					+ UISchemaTypes.getUISchemaTypesList().stream().collect(Collectors.joining(", ")));
		}
	}

	public void validateEventId(String eventId) {
		validateMissingInputParameter(eventId, TemplateVariablesConstants.EVENT_ID, VALIDATE_EVENT_ID);
		if (!isDataValidWithRegex(eventId, eventIdRegex)) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					TemplateVariablesConstants.EVENT_ID, VALIDATE_EVENT_ID));
			throw new InvalidInputException(TemplateVariablesConstants.EVENT_ID);
		}
	}

	private void validateMissingInputParameter(String variableValue, String variableName, String eventEnumName) {
		if (variableValue==null || variableValue.trim().isEmpty()) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					variableName, eventEnumName));
			throw new ResidentServiceException(ResidentErrorCode.MISSING_INPUT_PARAMETER, variableName);
		}
	}
	
	public void validateOtpCharLimit(String otp) {
		if (otp.length() > otpLength) {
			throw new ResidentServiceException(ResidentErrorCode.CHAR_LIMIT_EXCEEDS.getErrorCode(),
					String.format(ResidentErrorCode.CHAR_LIMIT_EXCEEDS.getErrorMessage(),otpLength,otp));
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
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "languageCode", "Request service history API"));
			throw new InvalidInputException("languageCode");
		}
		if(!allowedMandatoryLanguage.contains(languageCode) && !allowedOptionalLanguage.contains(languageCode)) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INVALID_LANGUAGE_CODE, "languageCode", "Request service history API"));
			throw new InvalidInputException("languageCode");
		}
	}

	public void validateId(io.mosip.preregistration.core.common.dto.MainRequestDTO<TransliterationRequestDTO> requestDTO) {
		if (Objects.nonNull(requestDTO.getId())) {
			if (!requestDTO.getId().equals(transliterateId)) {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, ID, "Invalid Transliterate id"));
				throw new InvalidInputException(ID);
			}
		} else {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, ID, "id is null"));
			throw new InvalidInputException(ID);
		}
		if (requestDTO.getRequest().getFromFieldLang().equalsIgnoreCase(requestDTO.getRequest().getToFieldLang())) {
			throw new InvalidInputException("'from' and 'to' languages cannot be same");
		}
	}

	public List<String> validateUserIdAndTransactionId(String userId, String transactionID) {
		validateTransactionId(transactionID);
		List<String> list = new ArrayList<>();
		if (userId == null || userId.isEmpty()) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "userId", "userId is null"));
			throw new InvalidInputException("userId");
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
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					"transactionId", "transactionId must not be null"));
			throw new InvalidInputException("transactionId");
		} else if(!isDataValidWithRegex(transactionID, transactionIdRegex)){
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					"transactionId", "transactionId must be 10 digit containing numbers"));
			throw new InvalidInputException("transactionId");
		}
	}

	private boolean isDataValidWithRegex(String inputData, String regex) {
		return inputData.matches(regex);
	}

	public void validateProxySendOtpRequest(MainRequestDTO<OtpRequestDTOV2> userOtpRequest) throws ApisResourceAccessException {
		validateRequestType(userOtpRequest.getId(), this.environment.getProperty(ResidentConstants.RESIDENT_CONTACT_DETAILS_SEND_OTP_ID), ID);
		validateVersion(userOtpRequest.getVersion());
		validateDate(userOtpRequest.getRequesttime());
		validateUserIdAndTransactionId(userOtpRequest.getRequest().getUserId(), userOtpRequest.getRequest().getTransactionId());
		validateSameUserId(userOtpRequest.getRequest().getUserId());
	}

	private void validateSameUserId(String userId) throws ApisResourceAccessException {
		try {
			IdentityDTO identityDTO = identityService.getIdentity(identityService.getResidentIndvidualIdFromSession());
			if(phoneValidator(userId)){
				String phone = identityDTO.getPhone();
				if(phone!=null && phone.equalsIgnoreCase(userId)) {
					throw new ResidentServiceException(ResidentErrorCode.SAME_PHONE_ERROR,
							ResidentErrorCode.SAME_PHONE_ERROR.getErrorMessage());
				}
			} else {
				String email = identityDTO.getEmail();
				if(email!=null && email.equalsIgnoreCase(userId)){
					throw new ResidentServiceException(ResidentErrorCode.SAME_EMAIL_ERROR,
							ResidentErrorCode.SAME_EMAIL_ERROR.getErrorMessage());
				}
			}
		} catch (ResidentServiceCheckedException e) {
			throw new ResidentServiceException(ResidentErrorCode.CLAIM_NOT_AVAILABLE.getErrorCode(),
					ResidentErrorCode.CLAIM_NOT_AVAILABLE.getErrorMessage(), e);
		}
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
		if(otp==null){
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					"otp", "otp must not be null"));
			throw new InvalidInputException("otp");
		} else if (!isNumeric(otp) || otp.length() != otpLength){
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					"otp", "otp is invalid"));
			throw new InvalidInputException("otp");
		}
	}

	public void validateRequestType(String inputRequestType, String requestTypeStoredInProperty, String type) {
		if(inputRequestType==null){
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					"request "+type, type+" must not be null"));
			throw new InvalidInputException(type);
		} else if(!inputRequestType.equalsIgnoreCase(requestTypeStoredInProperty)){
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					"request "+type, type+" is invalid"));
			throw new InvalidInputException(type);
		}
	}

	public void validateDate(Date requesttime) {
		if(requesttime==null) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, REQUESTTIME, "Request time invalid"));
			throw new InvalidInputException(REQUESTTIME);
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
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individualId",
					"Request service history API"));
			throw new InvalidInputException("individualId");
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

	private void validateAttributeList(List<String> attributes) {
		if(attributes.isEmpty()){
			throw new InvalidInputException(TemplateVariablesConstants.ATTRIBUTES);
		}
	}

	public void validateVersion(String requestVersion) {
		if (StringUtils.isEmpty(requestVersion) || !requestVersion.equals(reqResVersion))
			throw new InvalidInputException(VERSION);
	}

	private void validateEncodedString(String html) {
		try{
			CryptoUtil.decodePlainBase64(html);
		}catch (Exception e){
			audit.setAuditRequestDto(EventEnum.INPUT_INVALID);
			throw new InvalidInputException("html", e);
		}
	}

	private void validateString(String string, String variableName) {
		if(string == null || string.trim().isEmpty()) {
			audit.setAuditRequestDto(EventEnum.INPUT_INVALID);
			throw new InvalidInputException(variableName);
		}
	}

	public void validateDownloadCardVid(String vid) {
		if(!validateVid(vid)){
			audit.setAuditRequestDto(EventEnum.INPUT_INVALID);
			throw new InvalidInputException("VID");
		}
	}

	public boolean validateRequestNewApi(RequestWrapper<?> requestWrapper, RequestIdType requestIdType) {
		if (StringUtils.isEmpty(requestWrapper.getId()) || !requestWrapper.getId().equals(map.get(requestIdType)))
			throw new InvalidInputException(ID);
		try {
			DateUtils.parseToLocalDateTime(requestWrapper.getRequesttime());
		} catch (Exception e) {
			throw new InvalidInputException(REQUESTTIME);
		}
		if (StringUtils.isEmpty(requestWrapper.getVersion()) || !requestWrapper.getVersion().equals(reqResVersion))
			throw new InvalidInputException(VERSION);

		validateAPIRequestToCheckNull(requestWrapper);
		return true;
	}

	private void validateAPIRequestToCheckNull(RequestWrapper<?> requestWrapper) {
		if (requestWrapper.getRequest() == null) {
			audit.setAuditRequestDto(EventEnum.INPUT_DOESNT_EXISTS);
			throw new InvalidInputException(REQUEST);
		}
	}

	public void validateReqCredentialRequest(RequestWrapper<ResidentCredentialRequestDto> requestWrapper) {
		validateAPIRequestToCheckNull(requestWrapper);
		validateDataToCheckNullOrEmpty(requestWrapper.getRequest().getIndividualId(), ResidentConstants.INDIVIDUAL_ID);
		validateDataToCheckNullOrEmpty(requestWrapper.getRequest().getCredentialType(),
				ResidentConstants.CREDENTIAL_TYPE);
		validateDataToCheckNullOrEmpty(requestWrapper.getRequest().getIssuer(), ResidentConstants.ISSUER);
		validateDataToCheckNullOrEmpty(requestWrapper.getRequest().getOtp(), ResidentConstants.OTP);
		validateDataToCheckNullOrEmpty(requestWrapper.getRequest().getTransactionID(),
				ResidentConstants.TRANSACTION_ID_OLD);
	}

	private void validateDataToCheckNullOrEmpty(String variableValue, String variableName) {
		if (StringUtils.isBlank(variableValue)) {
			throw new ResidentServiceException(ResidentErrorCode.INVALID_INPUT.getErrorCode(),
					ResidentErrorCode.INVALID_INPUT.getErrorMessage() + variableName);
		}
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
		String alternateEmail = grievanceRequestDTOMainRequestDTO.getRequest().getAlternateEmailId();
		if(alternateEmail!=null){
			validateEmailId(alternateEmail);
		}
		String alternatePhone = grievanceRequestDTOMainRequestDTO.getRequest().getAlternatePhoneNo();
		if(alternatePhone!=null){
			validatePhoneNumber(alternatePhone);
		}
		validatePhoneNumber(grievanceRequestDTOMainRequestDTO.getRequest().getPhoneNo());
		validateMessage(grievanceRequestDTOMainRequestDTO.getRequest().getMessage());
    }

	private void validateMessage(String message) {
		validateMissingInputParameter(message, MESSAGE_CODE, "Validating message");
		if (message.length() > messageCharsLimit) {
			throw new ResidentServiceException(ResidentErrorCode.CHAR_LIMIT_EXCEEDS.getErrorCode(),
					String.format(ResidentErrorCode.CHAR_LIMIT_EXCEEDS.getErrorMessage(),messageCharsLimit,message));
		}
		if (!message.matches(messageAllowedSpecialCharRegex)) {
			throw new ResidentServiceException(ResidentErrorCode.CONTAINS_SPECIAL_CHAR.getErrorCode(),
					String.format(ResidentErrorCode.CONTAINS_SPECIAL_CHAR.getErrorMessage(),message));
		}
	}

	private void validatePhoneNumber(String phoneNo) {
		if (phoneNo != null) {
			phoneCharsValidator(phoneNo);
			if (!phoneValidator(phoneNo)) {
				throw new InvalidInputException(PHONE_CHANNEL);
			}
		}
	}

	private void validateEmailId(String emailId) {
		if (emailId != null) {
			emailCharsValidator(emailId);
			if (!emailValidator(emailId)) {
				throw new InvalidInputException(EMAIL_CHANNEL, EMAIL_CHANNEL + ResidentConstants.MUST_NOT_BE_EMPTY);
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

	public void validatePurpose(String purpose) {
		validateMissingInputParameter(purpose, TemplateVariablesConstants.PURPOSE, "Validating purpose");
		if (purpose.length() > purposeCharsLimit) {
			throw new ResidentServiceException(ResidentErrorCode.CHAR_LIMIT_EXCEEDS.getErrorCode(),
					ResidentErrorCode.CHAR_LIMIT_EXCEEDS.getErrorMessage());
		}
		if (!purpose.matches(purposeAllowedSpecialCharRegex)) {
			throw new ResidentServiceException(ResidentErrorCode.CONTAINS_SPECIAL_CHAR.getErrorCode(),
					String.format(ResidentErrorCode.CONTAINS_SPECIAL_CHAR.getErrorMessage(), purpose));
		}
	}

	public void validateSharableAttributes(List<SharableAttributesDTO> sharableAttributes) {
		if(sharableAttributes.isEmpty()){
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					TemplateVariablesConstants.ATTRIBUTE_LIST, "Validating sharable attributes"));
			throw new ResidentServiceException(ResidentErrorCode.MISSING_INPUT_PARAMETER, TemplateVariablesConstants.ATTRIBUTE_LIST);
		}
	}

	public void validateName(List<String> name) throws ResidentServiceCheckedException {
		if (name == null || name.isEmpty()) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.INVALID_REG_CENTER_NAME.getErrorCode(),
					ResidentErrorCode.INVALID_REG_CENTER_NAME.getErrorMessage());
		}
	}
}