package io.mosip.resident.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.AuditRequestDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.exception.ValidationException;
import io.mosip.resident.service.impl.IdentityServiceImpl;
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
	private IdentityServiceImpl identityService;

	@Autowired
	private Environment environment;
	
  
	/** The Constant UNKNOWN_HOST. */
	private static final String UNKNOWN_HOST = "Unknown Host";

	private String hostIpAddress = null;

	private String hostName = null;
	
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
	
	public  void setAuditRequestDto(EventEnum eventEnum) {
		AuditRequestDTO auditRequestDto = new AuditRequestDTO();

		auditRequestDto.setHostIp(hostIpAddress);
		auditRequestDto.setHostName(hostName);
		auditRequestDto.setApplicationId(eventEnum.getApplicationId());
		auditRequestDto.setApplicationName(eventEnum.getApplicationName());
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication != null) {
			String name = null;
			try {
				name = identityService.getAvailableclaimValue(
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
		}
		auditRequestDto.setActionTimeStamp(DateUtils.getUTCCurrentDateTime());
		auditRequestDto.setDescription(eventEnum.getDescription());
		auditRequestDto.setEventType(eventEnum.getType());
		auditRequestDto.setEventName(eventEnum.getName());
		auditRequestDto.setModuleId(eventEnum.getModuleId());
		auditRequestDto.setModuleName(eventEnum.getModuleName());
		auditRequestDto.setEventId(eventEnum.getEventId());
		auditRequestDto.setId(getRefIdandType().getT1());
		auditRequestDto.setIdType(getRefIdandType().getT2());
		callAuditManager(auditRequestDto);
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
	
	public Tuple2<String, String> getRefIdandType() {
		String refId = null;
		String refIdType = null;
		try {
			String individualId = identityService.getResidentIndvidualIdFromSession();
			if (individualId == null || individualId.isEmpty()) {
				return Tuples.of(ResidentConstants.NO_ID, ResidentConstants.NO_ID_TYPE);
			}
			refId = HMACUtils2.digestAsPlainText(individualId.getBytes());
			refIdType = identityService.getIndividualIdType(individualId);
		} catch (ApisResourceAccessException | NoSuchAlgorithmException e) {
			throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		return Tuples.of(refId, refIdType);
	}

}
