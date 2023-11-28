package io.mosip.resident.validator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.mosip.resident.dto.IdResponseDTO1;
import io.mosip.resident.util.Utility;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.idvalidator.exception.InvalidIDException;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.CardType;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.LogDescription;
import io.mosip.resident.dto.MachineResponseDto;
import io.mosip.resident.dto.RegistrationCenterResponseDto;
import io.mosip.resident.dto.RegistrationType;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.IdRepoAppException;
import io.mosip.resident.exception.RequestHandlerValidationException;
import io.mosip.resident.exception.VidCreationException;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utilities;

/**
 * The Class RequestHandlerRequestValidator.
 * 
 * @author Rishabh Keshari
 */
@Component
public class RequestHandlerRequestValidator {

	/** The mosip logger. */
	private final Logger logger = LoggerConfiguration.logConfig(RequestHandlerRequestValidator.class);

	/** The Constant ID_FIELD. */
	private static final String ID_FIELD = "id";

	/** The Constant VID. */
	private static final String EMAIL = "Email";

	/** The Constant VID. */
	private static final String PHONE = "Phone";

	/** The Constant REG_PACKET_GENERATOR_SERVICE_ID. */
	private static final String REG_PACKET_GENERATOR_SERVICE_ID = "mosip.registration.processor.registration.packetgenerator.id";

	/** The Constant REG_UINCARD_REPRINT_SERVICE_ID. */
	private static final String REG_UINCARD_REPRINT_SERVICE_ID = "mosip.registration.processor.uincard.reprint.id";

	/** The Constant RES_UPDATE_SERVICE_ID. */
	private static final String RES_UPDATE_SERVICE_ID = "mosip.registration.processor.resident.service.id";

	/** The Constant REG_UINCARD_REPRINT_SERVICE_ID. */
	private static final String REG_LOST_PACKET_SERVICE_ID = "mosip.registration.processor.lost.id";

	/** The env. */
	@Autowired
	private Environment env;

	/** The id. */
	private Map<String, String> id = new HashMap<>();

	/** The rest client service. */
	@Autowired
	private ResidentServiceRestClient restClientService;

	/** The mapper. */
	@Autowired
	private ObjectMapper mapper;

	/** The uin validator impl. */
	@Autowired
	private UinValidator<String> uinValidatorImpl;

	/** The vid validator impl. */
	@Autowired
	private VidValidator<String> vidValidatorImpl;

	/** The utilities. */
	@Autowired
	private Utilities utilities;

	@Autowired
	private Utility utility;

	/**
	 * Validate.
	 *
	 * @param requestId
	 *            the request id
	 * @throws RequestHandlerValidationException
	 *             the packet generator validation exception
	 */
	public void validate(String requestId)
			throws RequestHandlerValidationException {
		id.put("packet_generator", env.getProperty(REG_PACKET_GENERATOR_SERVICE_ID));
		id.put("uincard_reprint_status", env.getProperty(REG_UINCARD_REPRINT_SERVICE_ID));
		id.put("res_update", env.getProperty(RES_UPDATE_SERVICE_ID));
		id.put("lost_id", env.getProperty(REG_LOST_PACKET_SERVICE_ID));
		validateId(requestId);

	}

	/**
	 * Validate id.
	 *
	 * @param id
	 *            the id
	 * @throws RequestHandlerValidationException
	 *             the packet generator validation exception
	 */
	private void validateId(String id) throws RequestHandlerValidationException {
		RequestHandlerValidationException exception = new RequestHandlerValidationException();
		if (Objects.isNull(id)) {
			throw new RequestHandlerValidationException(ResidentErrorCode.INVALID_INPUT.getErrorCode(),
					String.format(ResidentErrorCode.INVALID_INPUT.getErrorMessage(), ID_FIELD),
					exception);

		} else if (!this.id.containsValue(id)) {
			throw new RequestHandlerValidationException(ResidentErrorCode.INVALID_INPUT.getErrorCode(),
					String.format(ResidentErrorCode.INVALID_INPUT.getErrorMessage(), ID_FIELD),
					exception);

		}
	}

	/**
	 * Checks if is valid center.
	 *
	 * @param centerId
	 *            the center id
	 * @return true, if is valid center
	 * @throws BaseCheckedException
	 *             the reg base checked exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public boolean isValidCenter(String centerId) throws BaseCheckedException, IOException {
		String langCode = utilities.getLanguageCode();
		boolean isValidCenter = false;
		RegistrationCenterResponseDto rcpdto;
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		try {
			if (centerId != null && !centerId.isEmpty()) {
				logger.debug(LoggerFileConstant.SESSIONID.toString(),
						LoggerFileConstant.REGISTRATIONID.toString(), "",
						"PacketGeneratorServiceImpl::isValidCenter():: Centerdetails Api call started");
				responseWrapper = (ResponseWrapper<?>) utility.getCenterDetails(centerId, langCode);
				rcpdto = mapper.readValue(mapper.writeValueAsString(responseWrapper.getResponse()),
						RegistrationCenterResponseDto.class);
				logger.debug(LoggerFileConstant.SESSIONID.toString(),
						LoggerFileConstant.REGISTRATIONID.toString(), "",
						"\"PacketGeneratorServiceImpl::isValidCenter():: Centerdetails Api call  ended with response data : "
								+ JsonUtil.objectMapperObjectToJson(rcpdto));
				if (CollectionUtils.isEmpty(responseWrapper.getErrors()) && !rcpdto.getRegistrationCenters().isEmpty()) {
					isValidCenter = true;
				} else {
					List<ServiceError> error = responseWrapper.getErrors();
					throw new BaseCheckedException(ResidentErrorCode.INVALID_INPUT.getErrorCode(), ResidentErrorCode.INVALID_INPUT.getErrorMessage()+" "+error.get(0).getMessage());
				}
			} else {
				throw new BaseCheckedException(ResidentErrorCode.INVALID_INPUT.getErrorCode(), ResidentErrorCode.INVALID_INPUT.getErrorMessage()+" CenterId is Mandatory");

			}
		} catch (ApisResourceAccessException e) {
			if (e.getCause() instanceof HttpClientErrorException) {
				List<ServiceError> error = responseWrapper.getErrors();
				throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(),
						error.get(0).getMessage(), e);

			}

		}
		return isValidCenter;
	}

	/**
	 * Checks if is valid machine.
	 *
	 * @param machine
	 *            the machine
	 * @return true, if is valid machine
	 * @throws BaseCheckedException
	 *             the reg base checked exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public boolean isValidMachine(String machine) throws BaseCheckedException, IOException {
		boolean isValidMachine = false;
		List<String> pathsegments = new ArrayList<>();
		pathsegments.add(machine);
		MachineResponseDto machinedto;
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		try {

			if (machine != null && !machine.isEmpty()) {
				logger.debug(LoggerFileConstant.SESSIONID.toString(),
						LoggerFileConstant.REGISTRATIONID.toString(), "",
						"PacketGeneratorServiceImpl::isValidMachine():: MachineDetails Api call started");
				responseWrapper = (ResponseWrapper<?>) restClientService.getApi(ApiName.MACHINEDETAILS, pathsegments,
						"", "", ResponseWrapper.class);
				machinedto = mapper.readValue(mapper.writeValueAsString(responseWrapper.getResponse()),
						MachineResponseDto.class);
				logger.debug(LoggerFileConstant.SESSIONID.toString(),
						LoggerFileConstant.REGISTRATIONID.toString(), "",
						"\"PacketGeneratorServiceImpl::isValidMachine():: MachienDetails Api call  ended with response data : "
								+ JsonUtil.objectMapperObjectToJson(machinedto));
				if (CollectionUtils.isEmpty(responseWrapper.getErrors()) && !machinedto.getMachines().isEmpty()) {
					isValidMachine = true;
				} else {
					List<ServiceError> error = responseWrapper.getErrors();
					throw new BaseCheckedException(ResidentErrorCode.INVALID_REQUEST_EXCEPTION.getErrorCode(), ResidentErrorCode.INVALID_REQUEST_EXCEPTION.getErrorMessage()+" "+error.get(0).getMessage());
				}
			} else {
				throw new BaseCheckedException(ResidentErrorCode.INVALID_REQUEST_EXCEPTION.getErrorCode(), ResidentErrorCode.INVALID_REQUEST_EXCEPTION.getErrorMessage()+" MachineId is Mandatory");

			}

		} catch (ApisResourceAccessException e) {
			if (e.getCause() instanceof HttpClientErrorException) {
				List<ServiceError> error = responseWrapper.getErrors();
				throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(),
						error.get(0).getMessage(), e);

			}

		}
		return isValidMachine;

	}

	/**
	 * Checks if is valid uin.
	 *
	 * @param uin
	 *            the uin
	 * @return true, if is valid uin
	 * @throws BaseCheckedException
	 *             the reg base checked exception
	 */
	public boolean isValidUin(String uin) throws BaseCheckedException {
		boolean isValidUIN = false;
		try {
			isValidUIN = uinValidatorImpl.validateId(uin);
			JSONObject jsonObject = utilities.retrieveIdrepoJson(uin);
			if (isValidUIN && jsonObject != null) {
				isValidUIN = true;
			} else {
				throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(), "UIN is not valid",
						new Throwable());

			}
		} catch (InvalidIDException ex) {
			throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(), ex.getErrorText(), ex);

		} catch (IdRepoAppException e) {
			throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(), e.getErrorText(), e);
		} catch (NumberFormatException e) {
			throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(), ResidentErrorCode.BASE_EXCEPTION.getErrorMessage(), e);
		} catch (ApisResourceAccessException e) {
			throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(), e.getErrorText(), e);
		} catch (IOException e) {
			throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(), ResidentErrorCode.BASE_EXCEPTION.getErrorMessage(), e);
		}
		return isValidUIN;
	}

	/**
	 * Checks if is valid re print registration type.
	 *
	 * @param registrationType
	 *            the registration type
	 * @return true, if is valid re print registration type
	 * @throws BaseCheckedException
	 *             the reg base checked exception
	 */
	public boolean isValidRePrintRegistrationType(String registrationType) throws BaseCheckedException {
		if (registrationType != null && (registrationType.equalsIgnoreCase(RegistrationType.RES_REPRINT.toString()))) {
			return true;
		} else {
			throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(),
					"Invalid RegistrationType:Enter RES_REPRINT", new Throwable());
		}

	}

	/**
	 * Checks if is valid registration type and uin.
	 *
	 * @param registrationType the registration type
	 * @param uin              the uin
	 * @param idResponseDto
	 * @return true, if is valid registration type and uin
	 * @throws BaseCheckedException the reg base checked exception
	 * @throws IOException          Signals that an I/O exception has occurred.
	 */
	public boolean isValidRegistrationTypeAndUin(String registrationType, String uin, IdResponseDTO1 idResponseDto)
			throws BaseCheckedException, IOException {
		try {
			if (registrationType != null
					&& (registrationType.equalsIgnoreCase(RegistrationType.ACTIVATED.toString())
							|| registrationType.equalsIgnoreCase(RegistrationType.DEACTIVATED.toString()))
					|| registrationType != null && registrationType.equals(RegistrationType.RES_UPDATE.toString())) {
				boolean isValidUin = uinValidatorImpl.validateId(uin);
				String status = null;
				if(idResponseDto == null){
					status = utilities.retrieveIdrepoJsonStatus(uin);
				} else {
					status = idResponseDto.getResponse().getStatus();
				}
				if (isValidUin) {
					if(registrationType.equals(RegistrationType.RES_UPDATE.toString())) {
						return validateUINForResUpdate(status);
					}
					if (!status.equalsIgnoreCase(registrationType)) {
						return true;
					} else {
						throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(),
								"Uin is already " + status, new Throwable());
					}
				} else {
					throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(),
							"UIN is not valid", new Throwable());
				}
			} else {
				throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(),
						"Invalid RegistrationType:Enter ACTIVATED or DEACTIVATED", new Throwable());
			}
		} catch (InvalidIDException ex) {
			throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(), ex.getErrorText(), ex);
		} catch (NumberFormatException | IdRepoAppException | ApisResourceAccessException e) {
			throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(), ResidentErrorCode.BASE_EXCEPTION.getErrorMessage(), e);
		}
	}

	private boolean validateUINForResUpdate(String status)
			throws BaseCheckedException {
		if(status.equals(RegistrationType.ACTIVATED.name()))
			return true;
		 else
			throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(),
					"UIN is not valid", new Throwable());
	}

	public boolean isValidVid(String vid) throws BaseCheckedException, IOException {
		return isValidVid(vid, null);
	}
	/**
	 * Checks if is valid vid.
	 *
	 * @param vid
	 *            the vid
	 * @return true, if is valid vid
	 * @throws BaseCheckedException
	 *             the reg base checked exception
	 */
	public boolean isValidVid(String vid, String sessionUin) throws BaseCheckedException, IOException {
		boolean isValidVID = false;
		try {
			isValidVID = vidValidatorImpl.validateId(vid);
			String result;
			if(sessionUin!=null){
				result = sessionUin;
			} else {
				result = utilities.getUinByVid(vid);
			}

			if (isValidVID && result != null) {
				isValidVID = true;
			} else {
				throw new BaseCheckedException(ResidentErrorCode.INVALID_VID.getErrorCode(), "VID is not valid",
						new Throwable());

			}
		} catch (InvalidIDException ex) {
			throw new BaseCheckedException(ResidentErrorCode.INVALID_INPUT.getErrorCode(), ex.getErrorText(), ex);

		} catch (IdRepoAppException e) {
			throw new BaseCheckedException(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(), e.getErrorText(), e);
		} catch (NumberFormatException e) {
			throw new BaseCheckedException(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(), ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorMessage(), e);
		} catch (ApisResourceAccessException e) {
			throw new BaseCheckedException(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(), e.getErrorText(), e);
		} catch (VidCreationException e) {
			throw new BaseCheckedException(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(), e.getErrorText(), e);
		}
		return isValidVID;
	}

	/**
	 * Checks if is valid id type.
	 *
	 * @param idType
	 *            the id type
	 * @return true, if is valid id type
	 * @throws BaseCheckedException
	 *             the reg base checked exception
	 */
	public boolean isValidIdType(String idType) throws BaseCheckedException {
		if (idType != null && (idType.equalsIgnoreCase(IdType.UIN.name()) || idType.equalsIgnoreCase(IdType.VID.name())))
			return true;
		else
			throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(),
					"Invalid IdType : Enter UIN or VID", new Throwable());
	}

	/**
	 * Checks if is valid card type.
	 *
	 * @param cardType
	 *            the card type
	 * @return true, if is valid card type
	 * @throws BaseCheckedException
	 *             the reg base checked exception
	 */
	public boolean isValidCardType(String cardType) throws BaseCheckedException {
		if (cardType != null && !cardType.isEmpty() && (cardType.equalsIgnoreCase(CardType.UIN.toString())
				|| cardType.equalsIgnoreCase(CardType.MASKED_UIN.toString()))) {
			return true;
		} else {
			throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(),
					"Invalid CardType : Enter UIN or MASKED_UIN", new Throwable());
		}

	}

	/**
	 * Checks if is valid contact type.
	 *
	 * @param contactType
	 *            the contact type
	 * @return true, if is valid contact type
	 * @throws BaseCheckedException
	 *             the reg base checked exception
	 */
	public boolean isValidContactType(String contactType, LogDescription description) throws BaseCheckedException {
		if (contactType != null && (contactType.equalsIgnoreCase(EMAIL) || contactType.equalsIgnoreCase(PHONE))) {
			return true;
		} else {
			description.setMessage(ResidentErrorCode.INVALID_INPUT.getErrorMessage());
			description.setCode(ResidentErrorCode.INVALID_INPUT.getErrorCode());
			throw new BaseCheckedException(ResidentErrorCode.INVALID_INPUT.getErrorCode(),
					ResidentErrorCode.INVALID_INPUT.getErrorMessage(), new Throwable());
		}

	}

}
