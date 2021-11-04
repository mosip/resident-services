package io.mosip.resident.validator;

import io.mosip.kernel.core.idvalidator.exception.InvalidIDException;
import io.mosip.kernel.core.idvalidator.spi.RidValidator;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.constant.*;
import io.mosip.resident.dto.*;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.util.*;

@Component
public class RequestValidator {

	@Autowired
	private UinValidator<String> uinValidator;

	@Autowired
	private VidValidator<String> vidValidator;

	@Autowired
	private RidValidator<String> ridValidator;

	@Autowired
	private AuditUtil audit;

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

	public void validateVidCreateRequest(ResidentVidRequestDto requestDto) {

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

		validateVidType(requestDto, "Request to generate VID");

		validateIndividualIdType(requestDto.getRequest().getIndividualIdType(), "Request to generate VID");

		if (!validateIndividualId(requestDto.getRequest().getIndividualId(),
				requestDto.getRequest().getIndividualIdType())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individualId",
					"Request to generate VID"));

			throw new InvalidInputException("individualId");
		}

		if (StringUtils.isEmpty(requestDto.getRequest().getOtp())) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "otp", "Request to generate VID"));

			throw new InvalidInputException("otp");
		}

		if (StringUtils.isEmpty(requestDto.getRequest().getTransactionID())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "transactionId",
					"Request to generate VID"));

			throw new InvalidInputException("transactionId");
		}
	}

	public void validateVidType(ResidentVidRequestDto requestDto, String msg) {
		if (StringUtils.isEmpty(requestDto.getRequest().getVidType())
				|| (!requestDto.getRequest().getVidType().equalsIgnoreCase(VidType.PERPETUAL.name())
						&& !requestDto.getRequest().getVidType().equalsIgnoreCase(VidType.TEMPORARY.name()))) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "vidType", msg));

			throw new InvalidInputException("vidType");
		}
	}

	public void validateAuthLockOrUnlockRequest(RequestWrapper<AuthLockOrUnLockRequestDto> requestDTO,
			AuthTypeStatus authTypeStatus) {

		validateAuthorUnlockId(requestDTO, authTypeStatus);

		if (requestDTO.getRequest() == null) {
			audit.setAuditRequestDto(EventEnum.INPUT_DOESNT_EXISTS);
			throw new InvalidInputException("request");
		}

		validateIndividualIdType(requestDTO.getRequest().getIndividualIdType(),
				"Request auth " + authTypeStatus.toString().toLowerCase() + " API");
		if (StringUtils.isEmpty(requestDTO.getRequest().getIndividualId())
				|| !validateIndividualId(requestDTO.getRequest().getIndividualId(),
						requestDTO.getRequest().getIndividualIdType())) {
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
		validateAuthType(requestDTO.getRequest().getAuthType(),
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
				|| (!validateIndividualId(requestDTO.getRequest().getIndividualId(),
						requestDTO.getRequest().getIndividualIdType()))) {
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
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "authType", msg));
			throw new InvalidInputException("authType");
		}
		String[] authTypesArray = authTypes.split(",");
		List<String> authTypesAllowed = new ArrayList<>(Arrays.asList(authTypesArray));
		for (String type : authType) {
			if (!authTypesAllowed.contains(type)) {
				audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "authType", msg));
				throw new InvalidInputException("authType");
			}
		}
	}

	public boolean phoneValidator(String phone) {
		return phone.matches(phoneRegex);
	}

	public boolean emailValidator(String email) {
		return email.matches(emailRegex);
	}

	private boolean validateIndividualId(String individualId, String individualIdType) {
		boolean validation = false;
		try {
			if (individualIdType.equalsIgnoreCase(IdType.UIN.toString())) {
				validation = uinValidator.validateId(individualId);
			} else if (individualIdType.equalsIgnoreCase(IdType.VID.toString())) {
				validation = vidValidator.validateId(individualId);
			} else if (individualIdType.equalsIgnoreCase(IdType.RID.toString())) {
				validation = ridValidator.validateId(individualId);
			}
		} catch (InvalidIDException e) {
			throw new InvalidInputException("individualId");
		}
		return validation;
	}

	public void validateVidRevokeRequest(RequestWrapper<VidRevokeRequestDTO> requestDto) {

		validateRequestWrapper(requestDto,"Request to revoke VID");

		if (StringUtils.isEmpty(requestDto.getRequest().getVidStatus())
				|| !requestDto.getRequest().getVidStatus().equalsIgnoreCase("REVOKED")) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "vidStatus", "Request to revoke VID"));
			throw new InvalidInputException("vidStatus");
		}

        if (StringUtils.isEmpty(requestDto.getRequest().getIndividualIdType())) {
            audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individualIdType", "Request to revoke VID"));
            throw new InvalidInputException("individualIdType");
        }

		if (StringUtils.isEmpty(requestDto.getRequest().getIndividualId())
				|| (!validateIndividualId(requestDto.getRequest().getIndividualId(),
						requestDto.getRequest().getIndividualIdType()))) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individualId", "Request to revoke VID"));
			throw new InvalidInputException("individualId");
		}

		if (StringUtils.isEmpty(requestDto.getRequest().getOtp())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "otp", "Request to revoke VID"));
			throw new InvalidInputException("otp");
		}

		if (StringUtils.isEmpty(requestDto.getRequest().getTransactionID())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "transactionId", "Request to revoke VID"));
			throw new InvalidInputException("transactionId");
		}
	}

	public void validateRequestWrapper(RequestWrapper<?> request,String msg) {

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

		if (StringUtils.isEmpty(request.getVersion()) || !request.getVersion().equalsIgnoreCase(version)) {
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
		validateRequest(requestDTO, RequestIdType.RE_PRINT_ID);

		if (requestDTO.getRequest() == null) {
			audit.setAuditRequestDto(EventEnum.INPUT_DOESNT_EXISTS);
			throw new InvalidInputException("request");
		}

		validateIndividualIdType(requestDTO.getRequest().getIndividualIdType(), "Request for print UIN API");

		if (StringUtils.isEmpty(requestDTO.getRequest().getIndividualId())
				|| !validateIndividualId(requestDTO.getRequest().getIndividualId(),
						requestDTO.getRequest().getIndividualIdType())) {
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

	public void validateUpdateRequest(RequestWrapper<ResidentUpdateRequestDto> requestDTO) {
		validateRequest(requestDTO, RequestIdType.RES_UPDATE);

		if (requestDTO.getRequest() == null) {
			audit.setAuditRequestDto(EventEnum.INPUT_DOESNT_EXISTS);
			throw new InvalidInputException("request");
		}

		validateIndividualIdType(requestDTO.getRequest().getIndividualIdType(), "Request for update uin");
		if (StringUtils.isEmpty(requestDTO.getRequest().getIndividualId())
				|| !validateIndividualId(requestDTO.getRequest().getIndividualId(),
						requestDTO.getRequest().getIndividualIdType())) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individualId", "Request for update uin"));
			throw new InvalidInputException("individualId");
		}
		if (StringUtils.isEmpty(requestDTO.getRequest().getOtp())) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "otp", "Request for update uin"));
			throw new InvalidInputException("otp");
		}

		if (StringUtils.isEmpty(requestDTO.getRequest().getTransactionID())) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "transactionId",
					"Request for update uin"));
			throw new InvalidInputException("transactionId");
		}

		if (requestDTO.getRequest().getIdentityJson() == null || requestDTO.getRequest().getIdentityJson().isEmpty()) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "identityJson", "Request for update uin"));
			throw new InvalidInputException("identityJson");
		}

	}

	public void validateRequestDTO(RequestWrapper<RequestDTO> requestDTO) {
		validateRequest(requestDTO, RequestIdType.CHECK_STATUS);

		if (requestDTO.getRequest() == null) {
			audit.setAuditRequestDto(EventEnum.INPUT_DOESNT_EXISTS);
			throw new InvalidInputException("request");
		}

		if (StringUtils.isEmpty(requestDTO.getRequest().getIndividualIdType())
				|| (!requestDTO.getRequest().getIndividualIdType().equalsIgnoreCase(IdType.RID.name()))) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individual type", "get RID status"));
			throw new InvalidInputException("individualIdType");
		}
		if (StringUtils.isEmpty(requestDTO.getRequest().getIndividualId())
				|| !validateIndividualId(requestDTO.getRequest().getIndividualId(),
						requestDTO.getRequest().getIndividualIdType())) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individual Id", "get RID status"));
			throw new InvalidInputException("individualId");
		}

	}

	public void validateIndividualIdType(String individualIdType, String typeofRequest) {
		if (StringUtils.isEmpty(individualIdType) || (!individualIdType.equalsIgnoreCase(IdType.UIN.name())
				&& !individualIdType.equalsIgnoreCase(IdType.VID.name()))) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individual type", typeofRequest));
			throw new InvalidInputException("individualIdType");
		}
	}
}
