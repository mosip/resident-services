package io.mosip.resident.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.idrepository.core.util.TokenIDGenerator;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.handler.service.ResidentConfigService;
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

	private static final String UIN = "UIN";
	private static final String BEARER_PREFIX = "Bearer ";
	private static final String AUTHORIZATION = "Authorization";
	private static final String IDA_TOKEN = "ida_token";
	private static final String INDIVIDUAL_ID = "individual_id";
	private static final String IDENTITY = "identity";
	private static final String VALUE = "value";
	private static final String EMAIL = "email";
	private static final String PHONE = "phone";
	private static final String DATE_OF_BIRTH = "dob";
	private static final String NAME = "name";
	private static final String PHOTO = "individualBiometrics";
	private static final String MAPPING_ATTRIBUTE_SEPARATOR = ",";
    private static final String ATTRIBUTE_VALUE_SEPARATOR = " ";
    private static final String LANGUAGE = "language";

	@Autowired
	@Qualifier("restClientWithSelfTOkenRestTemplate")
	private ResidentServiceRestClient restClientWithSelfTOkenRestTemplate;
	
	@Autowired
	@Qualifier("restClientWithPlainRestTemplate")
	private ResidentServiceRestClient restClientWithPlainRestTemplate;
	
	@Autowired
	private AuditUtil auditUtil;

	@Autowired
	private Utilitiy utility;
	
	@Autowired
	private TokenIDGenerator tokenIDGenerator;
	
	@Value("${ida.online-verification-partner-id}")
	private String onlineVerificationPartnerId;

	@Autowired
	private ResidentConfigService residentConfigService;
	
	@Value("${mosip.iam.userinfo_endpoint}")
	private String usefInfoEndpointUrl;
	
	@Value("${mosip.resident.identity.claim.individual-id}")
	private String individualIdClaim;
	
	@Value("${mosip.resident.identity.claim.ida-token}")
	private String idaTokenClaim;
	
	@Value("${resident.dateofbirth.pattern}")
	private String dateFormat;

	private static final Logger logger = LoggerConfiguration.logConfig(IdentityServiceImpl.class);
	
	@Override
    public IdentityDTO getIdentity(String id) throws ResidentServiceCheckedException{
    	return getIdentity(id,null);
    }

	@Override
	public IdentityDTO getIdentity(String id,String langCode) throws ResidentServiceCheckedException {
		logger.debug("IdentityServiceImpl::getIdentity()::entry");
		IdentityDTO identityDTO = new IdentityDTO();
		try {
			Map<?, ?> identity = (Map<?, ?>) getIdentityAttributes(id, true);
			identityDTO.setUIN(getMappingValue(identity, UIN));
			identityDTO.setEmail(getMappingValue(identity, EMAIL));
			identityDTO.setPhone(getMappingValue(identity, PHONE));
			String dateOfBirth = getMappingValue(identity, DATE_OF_BIRTH);
			DateTimeFormatter formatter=DateTimeFormatter.ofPattern(dateFormat);
			LocalDate localDate=LocalDate.parse(dateOfBirth, formatter);
			identityDTO.setYearOfBirth(Integer.toString(localDate.getYear()));
			identityDTO.setFullName(getMappingValue(identity, NAME, langCode));

		} catch (IOException e) {
			logger.error("Error occured in accessing identity data %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("IdentityServiceImpl::getIdentity()::exit");
		return identityDTO;
	}
	
	@Override
	public Map<String, ?> getIdentityAttributes(String id) throws ResidentServiceCheckedException {
		return getIdentityAttributes(id, false);
	}

	@Override
	public Map<String, ?> getIdentityAttributes(String id, boolean includeUin) throws ResidentServiceCheckedException {
		logger.debug("IdentityServiceImpl::getIdentityAttributes()::entry");
		Map<String, String> pathsegments = new HashMap<String, String>();
		pathsegments.put("id", id);
		try {
			ResponseWrapper<?> responseWrapper = restClientWithSelfTOkenRestTemplate.getApi(ApiName.IDREPO_IDENTITY_URL,
					pathsegments, ResponseWrapper.class);
			if(responseWrapper.getErrors() != null && responseWrapper.getErrors().size() > 0) {
				throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
						responseWrapper.getErrors().get(0).getErrorCode() + " --> " + responseWrapper.getErrors().get(0).getMessage());
			}
			Map<String, ?> identityResponse = new LinkedHashMap<>((Map<String, Object>) responseWrapper.getResponse());
			Map<String, ?> identity = (Map<String, ?>) identityResponse.get(IDENTITY);

			Map<String, Object> response = residentConfigService.getUiSchemaFilteredInputAttributes().stream()
					.filter(attrib -> identity.containsKey(attrib))
					.collect(Collectors.toMap(Function.identity(), identity::get,(m1, m2) -> m1, () -> new LinkedHashMap<String, Object>()));
			logger.debug("IdentityServiceImpl::getIdentityAttributes()::exit");
			if(includeUin) {
				response.put(UIN, identity.get(UIN));
			}
			return response;
		} catch (ApisResourceAccessException | IOException e) {
			logger.error("Error occured in accessing identity data %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
	}
	
	private String getMappingValue(Map<?, ?> identity, String mappingName)
            throws ResidentServiceCheckedException, IOException {
        return getMappingValue(identity, mappingName, null);
    }

	private String getMappingValue(Map<?, ?> identity, String mappingName, String langCode)
			throws ResidentServiceCheckedException, IOException {
		String mappingJson = utility.getMappingJson();
		if (mappingJson == null || mappingJson.trim().isEmpty()) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.JSON_PROCESSING_EXCEPTION.getErrorCode(),
					ResidentErrorCode.JSON_PROCESSING_EXCEPTION.getErrorMessage());
		}
		JSONObject mappingJsonObject = JsonUtil.readValue(mappingJson, JSONObject.class);
		JSONObject identityMappingJsonObject = JsonUtil.getJSONObject(mappingJsonObject, IDENTITY);
		String mappingAttributes = getMappingAttribute(identityMappingJsonObject, mappingName);
		return Stream.of(mappingAttributes.split(MAPPING_ATTRIBUTE_SEPARATOR))
                .map(mappingAttribute -> identity.get(mappingAttribute))
                .map(attributeValue -> {
                    if(attributeValue instanceof String) {
                        return (String) attributeValue;
                    } else if(attributeValue instanceof List){
                        if(langCode == null) {
                            return null;
                        } else {
                            return getValueForLang((List<Map<String,Object>>)attributeValue, langCode);
                        }
                    }
                    return null;
                }).collect(Collectors.joining(ATTRIBUTE_VALUE_SEPARATOR));
	}
	
	private String getValueForLang(List<Map<String, Object>> attributeValue, String langCode) {
        return attributeValue.stream()
                    .filter(map -> map.get(LANGUAGE) instanceof String && ((String)map.get(LANGUAGE)).equalsIgnoreCase(langCode))
                    .map(map -> (String)map.get(VALUE))
                    .findAny()
                    .orElse(null);
    }

	private String getMappingAttribute(JSONObject identityJson, String name) {
		JSONObject docJson = JsonUtil.getJSONObject(identityJson, name);
		if(docJson != null) {
			return JsonUtil.getJSONValue(docJson, VALUE);
		}
		return name;
	}
	
	public String getIDAToken(String uin) {
		return getIDAToken(uin, onlineVerificationPartnerId);
	}
	
	public String getIDAToken(String uin, String olvPartnerId) {
		return tokenIDGenerator.generateTokenID(uin, olvPartnerId);
	}

	public AuthUserDetails getAuthUserDetails() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(principal instanceof AuthUserDetails) {
			return (AuthUserDetails) principal;
		}
		return null;
	}

	public Map<String, String> getResidentIdentity() throws ApisResourceAccessException {
		return getClaims(Set.of(INDIVIDUAL_ID, IDA_TOKEN));
	}
	
	private Map<String, String> getClaims(String... claims) throws ApisResourceAccessException {
		return getClaims(Set.of(claims));
	}

	private Map<String, String> getClaims(Set<String> claims) throws ApisResourceAccessException {
		AuthUserDetails authUserDetails = getAuthUserDetails();
		if (authUserDetails != null) {
			String token = authUserDetails.getToken();
				Map<String, Object> userInfo = getUserInfo(token);
				return claims.stream().map(claim -> new SimpleEntry<>(claim, getClaimFromUserInfo(userInfo, claim)))
						.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

		}
		return Map.of();
	}

	private String getClaimFromUserInfo(Map<String, Object> userInfo, String claim) {
		Object claimValue = userInfo.get(claim);
		if(claimValue == null) {
			throw new ResidentServiceException(ResidentErrorCode.CLAIM_NOT_AVAILABLE, claim);
		}
		
		return String.valueOf(claimValue);
	}


	private Map<String, Object> getUserInfo(String token) throws ApisResourceAccessException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(usefInfoEndpointUrl);
		UriComponents uriComponent = builder.build(false).encode();

		Map<String, Object> responseMap;
		try {
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(Map.of(AUTHORIZATION, List.of(BEARER_PREFIX + token)));
			responseMap = (Map<String, Object>) restClientWithPlainRestTemplate.getApi(uriComponent.toUri(), Map.class, headers);
		} catch (Exception e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "NA",
					"IdAuthServiceImp::lencryptRSA():: ENCRYPTIONSERVICE GET service call"
							+ ExceptionUtils.getStackTrace(e));
			throw new ApisResourceAccessException("Could not fetch public key from kernel keymanager", e);
		}
		return responseMap;
	}

	public String getResidentIndvidualId() throws ApisResourceAccessException {
		return  getClaimValue(INDIVIDUAL_ID);
	}

	private String getClaimValue(String claim) throws ApisResourceAccessException {
		return getClaims(claim).get(claim);
	}
	
	public String getResidentIdaToken() throws ApisResourceAccessException {
		return  getClaimValue(IDA_TOKEN);
	}

}
