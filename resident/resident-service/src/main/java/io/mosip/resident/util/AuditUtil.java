package io.mosip.resident.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.AuditRequestDTO;
import io.mosip.resident.dto.AuditResponseDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.exception.ValidationException;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Component
public class AuditUtil {

	private static final Logger logger = LoggerConfiguration.logConfig(AuditUtil.class);

	@Autowired
	@Qualifier("selfTokenRestTemplate")
	RestTemplate restTemplate;
	
	@Value("${mosip.kernel.masterdata.audit-url}")
	private String auditUrl;
	
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UinVidValidator uinVidValidator;

	@Autowired
	private Environment environment;

	@Autowired
	private Utility utility;
	
	@Autowired
	private AsyncUtil asyncUtil;

	/** The Constant UNKNOWN_HOST. */
	private static final String UNKNOWN_HOST = "Unknown Host";

	private String hostIpAddress = null;

	private String hostName = null;

	@Autowired
	private AvailableClaimUtility availableClaimUtility;

	public String getServerIp() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return UNKNOWN_HOST;
		}
	}
	
	public String getServerName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return UNKNOWN_HOST;
		}
	}
	
	@PostConstruct
	public void getHostDetails() {
		hostIpAddress = getServerIp();
		hostName = getServerName();
	}

	public void setAuditRequestDto(AuditEvent auditEvent) {
		asyncUtil.asyncRun(() -> {
			AuditRequestDTO auditRequestDto = new AuditRequestDTO();

			auditRequestDto.setHostIp(hostIpAddress);
			auditRequestDto.setHostName(hostName);
			auditRequestDto.setApplicationId(auditEvent.getApplicationId());
			auditRequestDto.setApplicationName(auditEvent.getApplicationName());
			if(Utility.isSecureSession()) {
				String name = null;
				try {
					name = availableClaimUtility.getAvailableClaimValue(
							this.environment.getProperty(ResidentConstants.NAME_FROM_PROFILE));
				} catch (ApisResourceAccessException e) {
					throw new RuntimeException(e);
				}
				if (name == null || name.trim().isEmpty()) {
					auditRequestDto.setSessionUserId("UnknownSessionId");
					auditRequestDto.setSessionUserName("UnknownSessionName");
					auditRequestDto.setCreatedBy("Unknown");
				} else {
					auditRequestDto.setSessionUserId(name);
					auditRequestDto.setSessionUserName(name);
					auditRequestDto.setCreatedBy(name);
				}
			} else {
				auditRequestDto.setSessionUserId("UnknownSessionId");
				auditRequestDto.setSessionUserName("UnknownSessionName");
				auditRequestDto.setCreatedBy("Unknown");
			}
			auditRequestDto.setActionTimeStamp(DateUtils.getUTCCurrentDateTime());
			auditRequestDto.setDescription(auditEvent.getDescription());
			auditRequestDto.setEventType(auditEvent.getType());
			auditRequestDto.setEventName(auditEvent.getName());
			auditRequestDto.setModuleId(auditEvent.getModuleId());
			auditRequestDto.setModuleName(auditEvent.getModuleName());
			auditRequestDto.setEventId(auditEvent.getEventId());
			Tuple2<String, String> refIdHashAndType = getRefIdHashAndType();
			auditRequestDto.setId(refIdHashAndType.getT1());
			auditRequestDto.setIdType(refIdHashAndType.getT2());
			callAuditManager(auditRequestDto);
		});
	}
	
	public void callAuditManager(AuditRequestDTO auditRequestDto) {

		RequestWrapper<AuditRequestDTO> auditReuestWrapper = new RequestWrapper<>();
		auditReuestWrapper.setRequest(auditRequestDto);
		HttpEntity<RequestWrapper<AuditRequestDTO>> httpEntity = new HttpEntity<>(auditReuestWrapper);
		ResponseEntity<String> response = null;

		try {
			response = restTemplate.exchange(auditUrl, HttpMethod.POST, httpEntity, String.class);
			String responseBody = response.getBody();

			getAuditDetailsFromResponse(responseBody);
		} catch (Exception ex) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), ex.getMessage() + org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(ex));
		}
		

	}
	
	private AuditResponseDto getAuditDetailsFromResponse(String responseBody) throws Exception {

		List<ServiceError> validationErrorsList = null;
		validationErrorsList = ExceptionUtils.getServiceErrorList(responseBody);
		AuditResponseDto auditResponseDto = null;
		if (!validationErrorsList.isEmpty()) {
			throw new ValidationException(validationErrorsList);
		}
		ResponseWrapper<AuditResponseDto> responseObject = null;
		try {

			responseObject = objectMapper.readValue(responseBody,
					new TypeReference<ResponseWrapper<AuditResponseDto>>() {
					});
			auditResponseDto = responseObject.getResponse();
		} catch (IOException | NullPointerException exception) {
			throw exception;
		}

		return auditResponseDto;
	}
	
	public Tuple2<String, String> getRefIdHashAndType() {
		try {
			if (Utility.isSecureSession()) {
				String individualId = availableClaimUtility.getResidentIndvidualIdFromSession();
				if (individualId != null && !individualId.isEmpty()) {
					return getRefIdHashAndTypeFromIndividualId(individualId);
				}
			}
			return Tuples.of(ResidentConstants.NO_ID, ResidentConstants.NO_ID_TYPE);

		} catch (ApisResourceAccessException | NoSuchAlgorithmException e) {
			throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
	}

	public Tuple2<String, String> getRefIdHashAndTypeFromIndividualId(String individualId) throws NoSuchAlgorithmException {
		String refIdHash = utility.getRefIdHash(individualId);
		String idType = uinVidValidator.getIndividualIdType(individualId).name();
		return Tuples.of(refIdHash, idType);
	}

}
