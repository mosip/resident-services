package io.mosip.resident.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.idrepository.core.util.TokenIDGenerator;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.IdentityService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utilitiy;

/**
 * Resident identity service implementation class.
 * 
 * @author Ritik Jain
 */
@Component
public class IdentityServiceImpl implements IdentityService {

	private static final String IDENTITY = "identity";
	private static final String UIN = "uin";
	private static final String VALUE = "value";
	private static final String EMAIL = "email";
	private static final String PHONE = "phone";
	private static final String DATE_OF_BIRTH = "dob";
	private static final String NAME = "name";
	private static final String PHOTO = "individualBiometrics";

	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;

	@Autowired
	private AuditUtil auditUtil;

	@Autowired
	private Utilitiy utility;
	
	@Autowired
	private TokenIDGenerator tokenIDGenerator;
	
	@Value("${ida.online-verification-partner-id}")
	private String onlineVerificationPartnerId;

	private static final Logger logger = LoggerConfiguration.logConfig(IdentityServiceImpl.class);

	@Override
	public IdentityDTO getIdentity(String id) throws ResidentServiceCheckedException {
		logger.debug("IdentityServiceImpl::getIdentity()::entry");
		IdentityDTO identityDTO = new IdentityDTO();
		Map<String, String> pathsegments = new HashMap<String, String>();
		pathsegments.put("id", id);
		try {
			ResponseWrapper<?> responseWrapper = residentServiceRestClient.getApi(ApiName.IDREPO_IDENTITY_URL,
					pathsegments, ResponseWrapper.class);
			Map<?, ?> response = (Map<?, ?>) responseWrapper.getResponse();
			Map<?, ?> identity = (Map<?, ?>) response.get(IDENTITY);
			identityDTO.setUIN(getMappingValue(identity, UIN));
			identityDTO.setEmail(getMappingValue(identity, EMAIL));
			identityDTO.setPhone(getMappingValue(identity, PHONE));

		} catch (ApisResourceAccessException e) {
			logger.error("Error occured in accessing identity data %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		} catch (IOException e) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("IdentityServiceImpl::getIdentity()::exit");
		return identityDTO;
	}

	private String getMappingValue(Map<?, ?> identity, String mappingName)
			throws ResidentServiceCheckedException, IOException {
		String mappingJson = utility.getMappingJson();
		if (mappingJson == null || mappingJson.trim().isEmpty()) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.JSON_PROCESSING_EXCEPTION.getErrorCode(),
					ResidentErrorCode.JSON_PROCESSING_EXCEPTION.getErrorMessage());
		}
		JSONObject mappingJsonObject = JsonUtil.readValue(mappingJson, JSONObject.class);
		JSONObject identityMappingJsonObject = JsonUtil.getJSONObject(mappingJsonObject, IDENTITY);
		String mappingAttribute = getMappingAttribute(identityMappingJsonObject, mappingName);
		return (String) identity.get(mappingAttribute);
	}

	private String getMappingAttribute(JSONObject identityJson, String name) {
		JSONObject docJson = JsonUtil.getJSONObject(identityJson, name);
		return JsonUtil.getJSONValue(docJson, VALUE);
	}
	
	public String getIDAToken(String uin) {
		return getIDAToken(uin, onlineVerificationPartnerId);
	}
	
	public String getIDAToken(String uin, String olvPartnerId) {
		return tokenIDGenerator.generateTokenID(uin, olvPartnerId);
	}

}
