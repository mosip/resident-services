package io.mosip.resident.validator;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.idvalidator.exception.InvalidIDException;
import io.mosip.kernel.core.idvalidator.spi.RidValidator;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.constant.CardType;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.RequestIdType;
import io.mosip.resident.constant.VidType;
import io.mosip.resident.dto.AuthHistoryRequestDTO;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDto;
import io.mosip.resident.dto.EuinRequestDTO;
import io.mosip.resident.dto.RequestDTO;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentReprintRequestDto;
import io.mosip.resident.dto.ResidentUpdateRequestDto;
import io.mosip.resident.dto.ResidentVidRequestDto;
import io.mosip.resident.dto.VidRevokeRequestDTO;
import io.mosip.resident.exception.InvalidInputException;

@Component
public class RequestValidator {

	@Autowired
	private UinValidator<String> uinValidator;

	@Autowired
	private VidValidator<String> vidValidator;

	@Autowired
	private RidValidator<String> ridValidator;

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
			throw new InvalidInputException("requesttime");
		}

		if (StringUtils.isEmpty(requestDto.getId()) || !requestDto.getId().equalsIgnoreCase(id))
			throw new InvalidInputException("id");

		if (StringUtils.isEmpty(requestDto.getVersion()) || !requestDto.getVersion().equalsIgnoreCase(version))
			throw new InvalidInputException("version");

		if (requestDto.getRequest() == null)
			throw new InvalidInputException("request");

		validateVidType(requestDto);

		validateIndividualIdType(requestDto.getRequest().getIndividualIdType());

		if (!validateIndividualId(requestDto.getRequest().getIndividualId(),
				requestDto.getRequest().getIndividualIdType())) {
			throw new InvalidInputException("individualId");
		}

		if (StringUtils.isEmpty(requestDto.getRequest().getOtp()))
			throw new InvalidInputException("otp");

		if (StringUtils.isEmpty(requestDto.getRequest().getTransactionID()))
			throw new InvalidInputException("transactionId");
	}

	public void validateVidType(ResidentVidRequestDto requestDto) {
		if (StringUtils.isEmpty(requestDto.getRequest().getVidType())
				|| (!requestDto.getRequest().getVidType().equalsIgnoreCase(VidType.PERPETUAL.name())
						&& !requestDto.getRequest().getVidType().equalsIgnoreCase(VidType.TEMPORARY.name())))
			throw new InvalidInputException("vidType");
	}

	public void validateAuthLockOrUnlockRequest(RequestWrapper<AuthLockOrUnLockRequestDto> requestDTO,
			AuthTypeStatus authTypeStatus) {

		validateAuthorUnlockId(requestDTO, authTypeStatus);

		if (requestDTO.getRequest() == null)
			throw new InvalidInputException("request");


		validateIndividualIdType(requestDTO.getRequest().getIndividualIdType());
		if (StringUtils.isEmpty(requestDTO.getRequest().getIndividualId())
				|| !validateIndividualId(requestDTO.getRequest().getIndividualId(),
						requestDTO.getRequest().getIndividualIdType())) {
			throw new InvalidInputException("individualId");
		}
		if (StringUtils.isEmpty(requestDTO.getRequest().getOtp()))
			throw new InvalidInputException("otp");

		if (StringUtils.isEmpty(requestDTO.getRequest().getTransactionID()))
			throw new InvalidInputException("transactionId");
		validateAuthType(requestDTO.getRequest().getAuthType());

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

		if (requestDTO.getRequest() == null)
			throw new InvalidInputException("request");


		validateIndividualIdType(requestDTO.getRequest().getIndividualIdType());

		if (StringUtils.isEmpty(requestDTO.getRequest().getIndividualId())
				|| (!validateIndividualId(requestDTO.getRequest().getIndividualId(),
						requestDTO.getRequest().getIndividualIdType()))) {
			throw new InvalidInputException("individualId");
		}

		if (StringUtils.isEmpty(requestDTO.getRequest().getCardType())
				|| (!requestDTO.getRequest().getCardType().equalsIgnoreCase(CardType.UIN.name())
						&& !requestDTO.getRequest().getCardType().equalsIgnoreCase(CardType.MASKED_UIN.name())))
			throw new InvalidInputException("cardType");

		if (StringUtils.isEmpty(requestDTO.getRequest().getOtp()))
			throw new InvalidInputException("otp");

		if (StringUtils.isEmpty(requestDTO.getRequest().getTransactionID()))
			throw new InvalidInputException("transactionId");
	}

	public void validateAuthHistoryRequest(@Valid RequestWrapper<AuthHistoryRequestDTO> requestDTO) {
		validateRequest(requestDTO, RequestIdType.AUTH_HISTORY_ID);

		if (requestDTO.getRequest() == null)
			throw new InvalidInputException("request");

		if (StringUtils.isEmpty(requestDTO.getRequest().getOtp()))
			throw new InvalidInputException("otp");

		if (StringUtils.isEmpty(requestDTO.getRequest().getTransactionID()))
			throw new InvalidInputException("transactionId");
		validatePagefetchAndPageStart(requestDTO);
	}

	public void validatePagefetchAndPageStart(RequestWrapper<AuthHistoryRequestDTO> requestDTO) {
		if (requestDTO.getRequest().getPageFetch() != null && requestDTO.getRequest().getPageFetch().trim().isEmpty()
				&& requestDTO.getRequest().getPageStart() != null
				&& requestDTO.getRequest().getPageStart().trim().isEmpty())
			throw new InvalidInputException("please provide Page size and Page number to be Fetched");

		if (requestDTO.getRequest().getPageFetch() != null && requestDTO.getRequest().getPageFetch().trim().isEmpty()
				&& StringUtils.isEmpty(requestDTO.getRequest().getPageStart()))
			throw new InvalidInputException("please provide Page size and Page number to be Fetched");

		validatePageFetchAndPageStartEmptyCheck(requestDTO);
		validatePageFetchAndPageStartFormat(requestDTO);
	}

	private void validatePageFetchAndPageStartEmptyCheck(RequestWrapper<AuthHistoryRequestDTO> requestDTO) {
		if (StringUtils.isEmpty(requestDTO.getRequest().getPageFetch()) && requestDTO.getRequest().getPageStart() != null
				&& requestDTO.getRequest().getPageStart().trim().isEmpty())
			throw new InvalidInputException("please provide Page size and Page number to be Fetched");
		if (StringUtils.isEmpty(requestDTO.getRequest().getPageFetch())
				&& StringUtils.isNotEmpty(requestDTO.getRequest().getPageStart()))
			throw new InvalidInputException("please provide Page size to be Fetched");

		if (StringUtils.isEmpty(requestDTO.getRequest().getPageStart())
				&& StringUtils.isNotEmpty(requestDTO.getRequest().getPageFetch()))
			throw new InvalidInputException("please provide Page number to be Fetched");
	}

	public void validatePageFetchAndPageStartFormat(RequestWrapper<AuthHistoryRequestDTO> requestDTO) {

		if (!(StringUtils.isEmpty(requestDTO.getRequest().getPageStart())
				|| StringUtils.isEmpty(requestDTO.getRequest().getPageFetch()))) {
			if (!isNumeric(requestDTO.getRequest().getPageStart())) {
				throw new InvalidInputException("pageStart");
			}
			if (!isNumeric(requestDTO.getRequest().getPageFetch())) {
				throw new InvalidInputException("pageFetch");
			}
			if (Integer.parseInt(requestDTO.getRequest().getPageStart()) < 1
					|| Integer.parseInt(requestDTO.getRequest().getPageFetch()) < 1) {
				throw new InvalidInputException("Page Fetch or Page Start must be greater than 0");
			}
		}
	}

	public void validateAuthType(List<String> authType) {
		if (authType == null || authType.isEmpty()) {
			throw new InvalidInputException("authType");
		}
		String[] authTypesArray = authTypes.split(",");
		List<String> authTypesAllowed = new ArrayList<>(Arrays.asList(authTypesArray));
		for (String type : authType) {
			if (!authTypesAllowed.contains(type))
				throw new InvalidInputException("authType");
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

		validateRequestWrapper(requestDto);

		if (StringUtils.isEmpty(requestDto.getRequest().getVidStatus())
				|| !requestDto.getRequest().getVidStatus().equalsIgnoreCase("REVOKED"))
			throw new InvalidInputException("vidStatus");
		if (StringUtils.isEmpty(requestDto.getRequest().getIndividualIdType())
				|| (!requestDto.getRequest().getIndividualIdType().equalsIgnoreCase(IdType.VID.name())))
			throw new InvalidInputException("individualIdType");

		if (StringUtils.isEmpty(requestDto.getRequest().getIndividualId())
				|| (!validateIndividualId(requestDto.getRequest().getIndividualId(),
						requestDto.getRequest().getIndividualIdType())))
			throw new InvalidInputException("individualId");

		if (StringUtils.isEmpty(requestDto.getRequest().getOtp()))
			throw new InvalidInputException("otp");

		if (StringUtils.isEmpty(requestDto.getRequest().getTransactionID()))
			throw new InvalidInputException("transactionId");
	}


	public void validateRequestWrapper(RequestWrapper<?> request) {

		if (StringUtils.isEmpty(request.getId()) || !request.getId().equalsIgnoreCase(revokeVidId))
			throw new InvalidInputException("id");

		try {
			DateUtils.parseToLocalDateTime(request.getRequesttime());
		} catch (Exception e) {
			throw new InvalidInputException("requesttime");
		}

		if (StringUtils.isEmpty(request.getVersion()) || !request.getVersion().equalsIgnoreCase(version))
			throw new InvalidInputException("version");

		if (request.getRequest() == null)
			throw new InvalidInputException("request");
	}

	public boolean validateRequest(RequestWrapper<?> request, RequestIdType requestIdType) {
		if (StringUtils.isEmpty(request.getId()))
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

		if (requestDTO.getRequest() == null)
			throw new InvalidInputException("request");


		validateIndividualIdType(requestDTO.getRequest().getIndividualIdType());

		if (StringUtils.isEmpty(requestDTO.getRequest().getIndividualId())
				|| !validateIndividualId(requestDTO.getRequest().getIndividualId(),
						requestDTO.getRequest().getIndividualIdType())) {
			throw new InvalidInputException("individualId");
		}

		if (StringUtils.isEmpty(requestDTO.getRequest().getCardType())
				|| (!requestDTO.getRequest().getCardType().equalsIgnoreCase(CardType.UIN.name())
						&& !requestDTO.getRequest().getCardType().equalsIgnoreCase(CardType.MASKED_UIN.name())))
			throw new InvalidInputException("cardType");

		if (StringUtils.isEmpty(requestDTO.getRequest().getOtp()))
			throw new InvalidInputException("otp");

		if (StringUtils.isEmpty(requestDTO.getRequest().getTransactionID()))
			throw new InvalidInputException("transactionId");
	}

	public void validateUpdateRequest(RequestWrapper<ResidentUpdateRequestDto> requestDTO) {
		validateRequest(requestDTO, RequestIdType.RES_UPDATE);

		if (requestDTO.getRequest() == null)
			throw new InvalidInputException("request");


		validateIndividualIdType(requestDTO.getRequest().getIndividualIdType());
		if (StringUtils.isEmpty(requestDTO.getRequest().getIndividualId())
				|| !validateIndividualId(requestDTO.getRequest().getIndividualId(),
						requestDTO.getRequest().getIndividualIdType())) {
			throw new InvalidInputException("individualId");
		}
		if (StringUtils.isEmpty(requestDTO.getRequest().getOtp()))
			throw new InvalidInputException("otp");

		if (StringUtils.isEmpty(requestDTO.getRequest().getTransactionID()))
			throw new InvalidInputException("transactionId");

		if (requestDTO.getRequest().getIdentityJson() == null || requestDTO.getRequest().getIdentityJson().isEmpty())
			throw new InvalidInputException("identityJson");

	}

	public void validateRequestDTO(RequestWrapper<RequestDTO> requestDTO) {
		validateRequest(requestDTO, RequestIdType.CHECK_STATUS);

		if (requestDTO.getRequest() == null)
			throw new InvalidInputException("request");

		if (StringUtils.isEmpty(requestDTO.getRequest().getIndividualIdType())
				|| (!requestDTO.getRequest().getIndividualIdType().equalsIgnoreCase(IdType.RID.name())))
			throw new InvalidInputException("individualIdType");

		if (StringUtils.isEmpty(requestDTO.getRequest().getIndividualId())
				|| !validateIndividualId(requestDTO.getRequest().getIndividualId(),
						requestDTO.getRequest().getIndividualIdType())) {
			throw new InvalidInputException("individualId");
		}

	}

	public void validateIndividualIdType(String individualIdType) {
		if (StringUtils.isEmpty(individualIdType) || (!individualIdType.equalsIgnoreCase(IdType.UIN.name())
				&& !individualIdType.equalsIgnoreCase(IdType.VID.name())))
			throw new InvalidInputException("individualIdType");
	}
}
