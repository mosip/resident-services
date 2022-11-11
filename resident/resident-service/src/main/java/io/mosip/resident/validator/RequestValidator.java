package io.mosip.resident.validator;

import io.mosip.kernel.core.idvalidator.exception.InvalidIDException;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.preregistration.application.dto.TransliterationRequestDTO;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.constant.CardType;
import io.mosip.resident.constant.EventStatus;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.RequestIdType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ServiceType;
import io.mosip.resident.dto.AidStatusRequestDTO;
import io.mosip.resident.dto.AuthHistoryRequestDTO;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDto;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDtoV2;
import io.mosip.resident.dto.AuthTypeStatusDtoV2;
import io.mosip.resident.dto.AuthUnLockRequestDTO;
import io.mosip.resident.dto.BaseVidRequestDto;
import io.mosip.resident.dto.BaseVidRevokeRequestDTO;
import io.mosip.resident.dto.DownloadCardRequestDTO;
import io.mosip.resident.dto.DownloadHtml2PdfRequestDTO;
import io.mosip.resident.dto.EuinRequestDTO;
import io.mosip.resident.dto.IVidRequestDto;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.dto.OtpRequestDTOV3;
import io.mosip.resident.dto.RequestDTO;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentReprintRequestDto;
import io.mosip.resident.dto.ResidentUpdateRequestDto;
import io.mosip.resident.dto.SortType;
import io.mosip.resident.dto.VidRequestDto;
import io.mosip.resident.dto.VidRevokeRequestDTO;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.mosip.resident.service.impl.ResidentOtpServiceImpl.EMAIL_CHANNEL;
import static io.mosip.resident.service.impl.ResidentOtpServiceImpl.PHONE_CHANNEL;

@Component
public class RequestValidator {

	@Autowired
	private UinValidator<String> uinValidator;

	@Autowired
	private VidValidator<String> vidValidator;

	@Autowired
	private AuditUtil audit;

	@Autowired
	private Environment environment;

	@Autowired
	private IdentityServiceImpl identityService;

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

	@Value("${resident.revokevid.id}")
	private String revokeVidId;

	@Value("${resident.vid.version}")
	private String version;
	
	@Value("${resident.revokevid.version}")
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

	}

	public void validateVidCreateRequest(IVidRequestDto<? extends BaseVidRequestDto> requestDto, boolean otpValidationRequired, String individualId) {

		try {
			DateUtils.parseToLocalDateTime(requestDto.getRequesttime());
		} catch (Exception e) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "requesttime", "Request to generate VID"));

			throw new InvalidInputException("requesttime");
		}

		if (StringUtils.isEmpty(requestDto.getId()) || !requestDto.getId().equalsIgnoreCase(id)) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "id", "Request to generate VID"));

			throw new InvalidInputException("id");
		}
		
		if (StringUtils.isEmpty(requestDto.getVersion()) || !requestDto.getVersion().equalsIgnoreCase(version)) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "version", "Request to generate VID"));

			throw new InvalidInputException("version");
		}

		if (requestDto.getRequest() == null) {
			audit.setAuditRequestDto(EventEnum.INPUT_DOESNT_EXISTS);
			throw new InvalidInputException("request");
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
		if (StringUtils.isEmpty(requestDto.getId()) || !requestDto.getId().equalsIgnoreCase(authLockStatusUpdateV2Id)) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "id", "request to auth lock or unlock"));
			throw new InvalidInputException("id");
		}
		if (requestDto.getRequest() == null) {
			audit.setAuditRequestDto(EventEnum.INPUT_DOESNT_EXISTS);
			throw new InvalidInputException("request");
		}
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

		if (requestDTO.getRequest() == null) {
			audit.setAuditRequestDto(EventEnum.INPUT_DOESNT_EXISTS);
			throw new InvalidInputException("request");
		}

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

		if (requestDTO.getRequest() == null) {
			audit.setAuditRequestDto(EventEnum.INPUT_DOESNT_EXISTS);
			throw new InvalidInputException("request");
		}

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

		if (requestDTO.getRequest() == null) {
			audit.setAuditRequestDto(EventEnum.INPUT_DOESNT_EXISTS);
			throw new InvalidInputException("request");
		}

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
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "id", msg));
			throw new InvalidInputException("id");
		}
		try {
			DateUtils.parseToLocalDateTime(request.getRequesttime());
		} catch (Exception e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "requesttime", msg));
			throw new InvalidInputException("requesttime");
		}

		if (StringUtils.isEmpty(request.getVersion()) || !request.getVersion().equalsIgnoreCase(revokeVidVersion)) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "version", msg));
			throw new InvalidInputException("version");
		}
		if (request.getRequest() == null) {
			audit.setAuditRequestDto(EventEnum.INPUT_DOESNT_EXISTS);
			throw new InvalidInputException("request");
		}
	}

	public boolean validateRequest(RequestWrapper<?> request, RequestIdType requestIdType) {
		if (StringUtils.isEmpty(request.getId()) || !request.getId().equals(map.get(requestIdType)))
			throw new InvalidInputException("id");
		try {
			DateUtils.parseToLocalDateTime(request.getRequesttime());
		} catch (Exception e) {
			throw new InvalidInputException("requesttime");
		}
		if (StringUtils.isEmpty(request.getVersion()) || !request.getVersion().equals(version))
			throw new InvalidInputException("version");
		return true;

	}

	public static boolean isNumeric(String strNum) {
		try {
			Integer.parseInt(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public void validateReprintRequest(RequestWrapper<ResidentReprintRequestDto> requestDTO) {
		if (requestDTO.getRequest() == null) {
			audit.setAuditRequestDto(EventEnum.INPUT_DOESNT_EXISTS);
			throw new InvalidInputException("request");
		}

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
		if (requestDTO.getRequest() == null) {
			audit.setAuditRequestDto(EventEnum.INPUT_DOESNT_EXISTS);
			throw new InvalidInputException("request");
		}

		validateRequest(requestDTO, RequestIdType.RES_UPDATE);

		if (!isPatch) {
			validateIndividualIdType(requestDTO.getRequest().getIndividualIdType(), "Request for update uin");
			if (StringUtils.isEmpty(requestDTO.getRequest().getIndividualId())
					|| (!validateIndividualIdvIdWithoutIdType(requestDTO.getRequest().getIndividualId()))) {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individualId",
						"Request for update uin"));
				throw new InvalidInputException("individualId");
			}
		} else {
			validateIndividualIdvIdWithoutIdType(requestDTO.getRequest().getIndividualId());
		}
		if (!isPatch && StringUtils.isEmpty(requestDTO.getRequest().getOtp())) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "otp", "Request for update uin"));
			throw new InvalidInputException("otp");
		}

		if (StringUtils.isEmpty(requestDTO.getRequest().getTransactionID())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "transactionId",
					"Request for update uin"));
			throw new InvalidInputException("transactionId");
		}

		if(!isPatch) {
			if (requestDTO.getRequest().getIdentityJson() == null || requestDTO.getRequest().getIdentityJson().isEmpty()) {
				audit.setAuditRequestDto(
						EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "identityJson", "Request for update uin"));
				throw new InvalidInputException("identityJson");
			}
		}
	}

	public void validateRidCheckStatusRequestDTO(RequestWrapper<RequestDTO> requestDTO) {
		if (requestDTO.getRequest() == null) {
			audit.setAuditRequestDto(EventEnum.INPUT_DOESNT_EXISTS);
			throw new InvalidInputException("request");
		}

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

		if (requestDTO.getRequest() == null) {
			audit.setAuditRequestDto(EventEnum.INPUT_DOESNT_EXISTS);
			throw new InvalidInputException("request");
		}

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
			return this.validateUin(individualId) || this.validateVid(individualId);
		} catch (InvalidIDException e) {
			return false;
		}
	}

	public void validateAidStatusRequestDto(RequestWrapper<AidStatusRequestDTO> reqDto) throws ResidentServiceCheckedException {
		validateRequest(reqDto, RequestIdType.CHECK_STATUS);
		
		if(reqDto.getRequest() == null) {
			throw new InvalidInputException("request");
		}
		if(reqDto.getRequest().getAid() == null) {
			throw new InvalidInputException("aid");
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
    }

    public void validateServiceHistoryRequest(LocalDateTime fromDateTime, LocalDateTime toDateTime, String sortType, String serviceType, String statusFilter) {
		validateServiceType(serviceType, "Request service history API");
		validateSortType(sortType, "Request service history API");
		validateStatusFilter(statusFilter, "Request service history API");
		validateFromDateTimeToDateTime(fromDateTime, toDateTime, "Request service history API");
		if(!isValidDate(fromDateTime) || !isValidDate(toDateTime)) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "fromDateTime", "Request service history API"));
			throw new InvalidInputException("DateTime");
		}
	}

	public void validateFromDateTimeToDateTime(LocalDateTime fromDateTime, LocalDateTime toDateTime, String request_service_history_api) {
		if(fromDateTime != null && toDateTime != null) {
			if(fromDateTime.isAfter(toDateTime)) {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "fromDateTime", request_service_history_api));
				throw new InvalidInputException("fromDateTime");
			}
		}
		if(fromDateTime == null && toDateTime != null) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "fromDateTime", request_service_history_api));
			throw new InvalidInputException("fromDateTime");
		} else if(fromDateTime != null && toDateTime == null) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "toDateTime", request_service_history_api));
			throw new InvalidInputException("toDateTime");
		}
	}

	private void validateStatusFilter(String statusFilter, String request_service_history_api) {
		if(statusFilter != null) {
			List<String> statusFilterList = Arrays.asList(statusFilter.split(","));
			for (String status : statusFilterList) {
				if (!status.equalsIgnoreCase(EventStatus.FAILED.toString()) && !status.equalsIgnoreCase(EventStatus.IN_PROGRESS.toString())
						&& !status.equalsIgnoreCase(EventStatus.SUCCESS.toString())) {
					audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "statusFilter",
							request_service_history_api));
					throw new InvalidInputException("statusFilter");
				}
			}
		}
	}

	private boolean isValidDate(LocalDateTime localDateTime) {
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

	public void validateIndividualId(String eventId) {
		if (eventId==null ||StringUtils.isEmpty(eventId)) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "eventId", "Request service history API"));
			throw new InvalidInputException("eventId");
		}
	}

	public void validateEventIdLanguageCode(String eventId, String languageCode) {
		validateIndividualId(eventId);
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
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "id", "Invalid Transliterate id"));
				throw new InvalidInputException("id");
			}
		} else {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "id", "id is null"));
			throw new InvalidInputException("id");
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
		if(transactionID.isEmpty()){
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					"transactionID", "transactionID must not be null"));
			throw new InvalidInputException("transactionID");
		} else if(!isNumeric(transactionID) || transactionID.length()!=10){
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					"transactionID", "transactionID must be 10 digit containing numbers"));
			throw new InvalidInputException("transactionID");
		}
	}

	public void validateUpdateDataRequest(MainRequestDTO<OtpRequestDTOV3> userIdOtpRequest) {
		String inputRequestId = userIdOtpRequest.getId();
		String requestIdStoredInProperty = this.environment.getProperty(ResidentConstants.RESIDENT_CONTACT_DETAILS_UPDATE_ID);
		validateRequestId(inputRequestId, requestIdStoredInProperty);
		validateDate(userIdOtpRequest.getRequesttime());
		validateUserIdAndTransactionId(userIdOtpRequest.getRequest().getUserId(), userIdOtpRequest.getRequest().getTransactionID());
		validateOTP(userIdOtpRequest.getRequest().getOtp());
	}

	public void validateOTP(String otp) {
		if(otp==null){
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					"otp", "otp must not be null"));
			throw new InvalidInputException("otp");
		} else if(!isNumeric(otp)){
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					"otp", "otp is invalid"));
			throw new InvalidInputException("otp");
		}
	}

	public void validateRequestId(String inputRequestId, String requestIdStoredInProperty) {
		if(inputRequestId==null){
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					"request id", "requestId must not be null"));
			throw new InvalidInputException("requestId");
		} else if(!inputRequestId.equalsIgnoreCase(requestIdStoredInProperty)){
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID,
					"request id", "requestId is invalid"));
			throw new InvalidInputException("requestId");
		}
	}

	public void validateDate(Date requesttime) {
		if(requesttime==null) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "requesttime", "Request time invalid"));
			throw new InvalidInputException("requesttime");
		}
	}

	public void validateDownloadCardRequest(MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO) {
		validateRequestId(downloadCardRequestDTOMainRequestDTO.getId(), this.environment.getProperty(ResidentConstants.DOWNLOAD_UIN_CARD_ID));
		validateDate(downloadCardRequestDTOMainRequestDTO.getRequesttime());
		validateTransactionId(downloadCardRequestDTOMainRequestDTO.getRequest().getTransactionId());
		validateOTP(downloadCardRequestDTOMainRequestDTO.getRequest().getOtp());
		validateIndividualIdV2(downloadCardRequestDTOMainRequestDTO.getRequest().getIndividualId());
	}

	private void validateIndividualIdV2(String individualId) {
		if (individualId == null || StringUtils.isEmpty(individualId)) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individualId",
					"Request service history API"));
			throw new InvalidInputException("individualId");
		}
	}

    public void validateDownloadHtml2pdfRequest(MainRequestDTO<DownloadHtml2PdfRequestDTO> downloadHtml2PdfRequestDTOMainRequestDTO) {
		validateRequestId(downloadHtml2PdfRequestDTOMainRequestDTO.getId(), this.environment.getProperty(ResidentConstants.DOWNLOAD_HTML_2_PDF_ID));
		validateDate(downloadHtml2PdfRequestDTOMainRequestDTO.getRequesttime());
		validateString(downloadHtml2PdfRequestDTOMainRequestDTO.getRequest().getHtml(), "html");
		validateEncodedString(downloadHtml2PdfRequestDTOMainRequestDTO.getRequest().getHtml());
		validateString(downloadHtml2PdfRequestDTOMainRequestDTO.getRequest().getSchemaType(), "schemaType");
    }

	private void validateEncodedString(String html) {
		try{
			identityService.decodeString(html);
		}catch (Exception e){
			audit.setAuditRequestDto(EventEnum.INPUT_INVALID);
			throw new InvalidInputException("html", e);
		}
	}

	private void validateString(String string, String variableName) {
		if(string == null){
			audit.setAuditRequestDto(EventEnum.INPUT_INVALID);
			throw new InvalidInputException(variableName);
		}
	}
}
