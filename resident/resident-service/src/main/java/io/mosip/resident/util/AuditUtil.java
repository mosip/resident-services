package io.mosip.resident.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.dto.AuditRequestDTO;
import io.mosip.resident.exception.ValidationException;

@Component
public class AuditUtil {
	

	@Autowired
	@Qualifier("selfTokenRestTemplate")
	RestTemplate restTemplate;
	
	@Value("${mosip.kernel.masterdata.audit-url}")
	private String auditUrl;
	
	@Autowired
	private ObjectMapper objectMapper;
	
  
	/** The Constant UNKNOWN_HOST. */
	private static final String UNKNOWN_HOST = "Unknown Host";

	private String hostIpAddress = null;

	private String hostName = null;
	
	@Autowired
	private TokenGenerator tokenGenerator;

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
		auditRequestDto.setSessionUserId(SecurityContextHolder.getContext().getAuthentication().getName());
		auditRequestDto.setSessionUserName(SecurityContextHolder.getContext().getAuthentication().getName());
		auditRequestDto.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
		auditRequestDto.setActionTimeStamp(DateUtils.getUTCCurrentDateTime());
		auditRequestDto.setDescription(eventEnum.getDescription());
		auditRequestDto.setEventType(eventEnum.getType());
		auditRequestDto.setEventName(eventEnum.getName());
		auditRequestDto.setModuleId(eventEnum.getModuleId());
		auditRequestDto.setModuleName(eventEnum.getModuleName());
		auditRequestDto.setEventId(eventEnum.getEventId());
		auditRequestDto.setId(eventEnum.getId());
		auditRequestDto.setIdType(eventEnum.getIdType());
		callAuditManager(auditRequestDto);
	}
	
	private void callAuditManager(AuditRequestDTO auditRequestDto) {

		RequestWrapper<AuditRequestDTO> auditReuestWrapper = new RequestWrapper<>();
		auditReuestWrapper.setRequest(auditRequestDto);
		HttpEntity<RequestWrapper<AuditRequestDTO>> httpEntity = new HttpEntity<>(auditReuestWrapper);
		ResponseEntity<String> response = null;

		try {
			response = restTemplate.exchange(auditUrl, HttpMethod.POST, httpEntity, String.class);
			String responseBody = response.getBody();

			getAuditDetailsFromResponse(responseBody);
		} catch (Exception ex) {
			ex.printStackTrace();
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

}
