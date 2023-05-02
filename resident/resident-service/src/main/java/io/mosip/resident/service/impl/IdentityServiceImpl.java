package io.mosip.resident.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.idrepository.core.util.TokenIDGenerator;
import io.mosip.kernel.authcodeflowproxy.api.validator.ValidateTokenUtil;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.openid.bridge.api.constants.AuthErrorCode;
import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.entity.ResidentSessionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.exception.VidCreationException;
import io.mosip.resident.handler.service.ResidentConfigService;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.repository.ResidentSessionRepository;
import io.mosip.resident.service.IdentityService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.util.Utility;
import io.mosip.resident.validator.RequestValidator;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Resident identity service implementation class.
 * 
 * @author Ritik Jain
 */
@Component
public class IdentityServiceImpl implements IdentityService {

	private static final String UTF_8 = "utf-8";
	private static final String RETRIEVE_IDENTITY_PARAM_TYPE_DEMO = "demo";
	private static final String UIN = "UIN";
	private static final String BEARER_PREFIX = "Bearer ";
	private static final String AUTHORIZATION = "Authorization";
	private static final String INDIVIDUAL_ID = "individual_id";
	private static final String IDENTITY = "identity";
	private static final String VALUE = "value";
	private static final String EMAIL = "email";
	private static final String PHONE = "phone";
	private static final String DATE_OF_BIRTH = "dob";
	private static final String NAME = "name";
	private static final String MAPPING_ATTRIBUTE_SEPARATOR = ",";
    private static final String ATTRIBUTE_VALUE_SEPARATOR = " ";
    private static final String LANGUAGE = "language";
	private static final String IMAGE = "mosip.resident.photo.token.claim-photo";
	private static final String PHOTO_ATTRIB_PROP = "mosip.resident.photo.attribute.name";

	private static final String VID = "VID";
	private static final String AID = "AID";
	private static final String  PERPETUAL_VID = "perpetualVID";

	@Autowired
	@Qualifier("restClientWithSelfTOkenRestTemplate")
	private ResidentServiceRestClient restClientWithSelfTOkenRestTemplate;
	
	@Autowired
	@Qualifier("restClientWithPlainRestTemplate")
	private ResidentServiceRestClient restClientWithPlainRestTemplate;
	
	@Autowired
	private Utility utility;
	
	@Autowired
	private TokenIDGenerator tokenIDGenerator;

	@Autowired
	private ObjectMapper objectMapper;
	
	@Value("${ida.online-verification-partner-id}")
	private String onlineVerificationPartnerId;

	@Autowired
	private ResidentConfigService residentConfigService;
	
	@Autowired
	private Environment env;

	@Autowired
	private RequestValidator requestValidator;
	
	@Value("${mosip.iam.userinfo_endpoint}")
	private String usefInfoEndpointUrl;
	
	@Value("${mosip.resident.identity.claim.individual-id}")
	private String individualIdClaim;
	
	@Value("${mosip.resident.identity.claim.ida-token}")
	private String idaTokenClaim;
	
	@Value("${resident.dateofbirth.pattern}")
	private String dateFormat;

	@Autowired
	private ResidentVidService residentVidService;

	@Value("${resident.flag.use-vid-only:false}")
	private boolean useVidOnly;
	
	@Autowired
	private ObjectStoreHelper objectStoreHelper;
	
	@Autowired
	private ValidateTokenUtil tokenValidationHelper;
	
	@Autowired
    private Utilities  utilities;
	
	@Autowired
	private ResidentSessionRepository  residentSessionRepo;
	
	private static final Logger logger = LoggerConfiguration.logConfig(IdentityServiceImpl.class);
	
	@Override
    public IdentityDTO getIdentity(String id) throws ResidentServiceCheckedException{
    	return getIdentity(id, false, null);
    }

	@Override
	public IdentityDTO getIdentity(String id, boolean fetchFace, String langCode) throws ResidentServiceCheckedException {
		logger.debug("IdentityServiceImpl::getIdentity()::entry");
		IdentityDTO identityDTO = new IdentityDTO();
		try {
			Map<String, Object> identity =	getIdentityAttributes(id, null);
			/**
			 * It is assumed that in the UI schema the UIN is added.
			 */
			identityDTO.setUIN(getMappingValue(identity, UIN));
			identityDTO.setEmail(getMappingValue(identity, EMAIL));
			identityDTO.setPhone(getMappingValue(identity, PHONE));
			String dateOfBirth = getMappingValue(identity, DATE_OF_BIRTH);
			if(dateOfBirth != null && !dateOfBirth.isEmpty()) {
				identityDTO.setDateOfBirth(dateOfBirth);
				DateTimeFormatter formatter=DateTimeFormatter.ofPattern(dateFormat);
				LocalDate localDate=LocalDate.parse(dateOfBirth, formatter);
				identityDTO.setYearOfBirth(Integer.toString(localDate.getYear()));
			}
			identityDTO.setFullName(getMappingValue(identity, NAME, langCode));

			if(fetchFace) {
				identity.put(env.getProperty(ResidentConstants.PHOTO_ATTRIBUTE_NAME), getClaimValue(env.getProperty(IMAGE)));
				identity.remove("individualBiometrics");
			}

		} catch (IOException e) {
			logger.error("Error occured in accessing identity data %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		} catch (ApisResourceAccessException e) {
			logger.error("Error occured in accessing identity data %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("IdentityServiceImpl::getIdentity()::exit");
		return identityDTO;
	}
	
	@Override
	public Map<String, Object> getIdentityAttributes(String id, String schemaType) throws ResidentServiceCheckedException, IOException {
		return getIdentityAttributes(id, schemaType, List.of(
				Objects.requireNonNull(env.getProperty(ResidentConstants.ADDITIONAL_ATTRIBUTE_TO_FETCH))
				.split(ResidentConstants.COMMA)));
	}

	@Override
	public Map<String, Object> getIdentityAttributes(String id, String schemaType,
			List<String> additionalAttributes) throws ResidentServiceCheckedException {
		logger.debug("IdentityServiceImpl::getIdentityAttributes()::entry");
		Map<String, String> pathsegments = new HashMap<String, String>();
		pathsegments.put("id", id);
		
		List<String> queryParamName = new ArrayList<String>();
		queryParamName.add("type");
		
		List<Object> queryParamValue = new ArrayList<>();
		queryParamValue.add(RETRIEVE_IDENTITY_PARAM_TYPE_DEMO);
		
		try {
			ResponseWrapper<?> responseWrapper = restClientWithSelfTOkenRestTemplate.getApi(ApiName.IDREPO_IDENTITY_URL,
					pathsegments, queryParamName, queryParamValue, ResponseWrapper.class);
			if(responseWrapper.getErrors() != null && responseWrapper.getErrors().size() > 0) {
				throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
						responseWrapper.getErrors().get(0).getErrorCode() + " --> " + responseWrapper.getErrors().get(0).getMessage());
			}
			Map<String, Object> identityResponse = new LinkedHashMap<>((Map<String, Object>) responseWrapper.getResponse());
			Map<String,Object> identity = (Map<String, Object>) identityResponse.get(IDENTITY);
			List<String> finalFilter = new ArrayList<>();
			if(schemaType != null) {
				List<String> filterBySchema = residentConfigService.getUiSchemaFilteredInputAttributes(schemaType);
				finalFilter.addAll(filterBySchema);
			}
			if(additionalAttributes != null && additionalAttributes.size()>0){
				finalFilter.addAll(additionalAttributes);
			}
			Map<String, Object> response = finalFilter.stream()
					.peek(a -> {
						if(a.equals(PERPETUAL_VID) || a.equals(ResidentConstants.MASK_PERPETUAL_VID)) {
							Optional<String> perpVid= Optional.empty();
							try {
								perpVid = residentVidService.getPerpatualVid((String) identity.get(UIN));
							} catch (ResidentServiceCheckedException | ApisResourceAccessException e) {
								throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
										ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
							}
							if(perpVid.isPresent()) {
								String vid = perpVid.get();
								identity.put(PERPETUAL_VID, vid);
							}
						}
					})
					.peek(a -> {
						if(a.equals(env.getProperty(PHOTO_ATTRIB_PROP))) {
							String photo;
							try {
								if (Utility.isSecureSession()) {
									photo = this.getAvailableclaimValue(env.getProperty(IMAGE));
								} else {
									photo = null;
								}
							} catch (ApisResourceAccessException e) {
								logger.error("Error occured in accessing picture from claims %s", e.getMessage());
								throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
										ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
							}
							if(photo != null) {
								identity.put(env.getProperty(PHOTO_ATTRIB_PROP), photo);
							}
						}
					})
					.peek(attr -> {
						if(attr.contains(ResidentConstants.MASK_PREFIX)) {
							String attributeName = attr.replace(ResidentConstants.MASK_PREFIX, "");
							if(identity.containsKey(attributeName)) {
								identity.put(attr, utility.convertToMaskDataFormat((String) identity.get(attributeName)));
							}
						}
					})
					.filter(attrib -> identity.containsKey(attrib))
					.collect(Collectors.toMap(Function.identity(), identity::get,(m1, m2) -> m1, () -> new LinkedHashMap<String, Object>()));
			logger.debug("IdentityServiceImpl::getIdentityAttributes()::exit");

			return response;
		} catch (ApisResourceAccessException | IOException e) {
			logger.error("Error occured in accessing identity data %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
	}

	public String getNameForNotification(Map<?, ?> identity, String langCode) throws ResidentServiceCheckedException, IOException {
		return getMappingValue(identity, NAME, langCode);
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
                    } else if(attributeValue instanceof Map) {
                    	return ((String)((Map) attributeValue).get(VALUE));
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining(ATTRIBUTE_VALUE_SEPARATOR));
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
	
	@Override
	public String getUinForIndividualId(String idvid) throws ResidentServiceCheckedException {
	
		try {
			if(getIndividualIdType(idvid).equalsIgnoreCase(UIN)){
				return idvid;
			}
			return utilities.getUinByVid(idvid);
		} catch (VidCreationException e) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.VID_CREATION_EXCEPTION.getErrorCode(),
					ResidentErrorCode.VID_CREATION_EXCEPTION.getErrorMessage());
		} catch (ApisResourceAccessException e) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage());
		} catch (IOException e) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage());
		}
		
	}
	
	@Override
	public String getIDATokenForIndividualId(String idvid) throws ResidentServiceCheckedException {
		return getIDAToken(getUinForIndividualId(idvid));
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

	public Map<String, String> getClaims(String... claims) throws ApisResourceAccessException {
		return getClaims(Set.of(claims));
	}
	
	private Map<String, String> getClaims(Set<String> claims) throws ApisResourceAccessException {
		AuthUserDetails authUserDetails = getAuthUserDetails();
		if (authUserDetails != null) {
			String token = authUserDetails.getToken();
				return getClaimsFromToken(claims, token);
		}
		return Map.of();
	}
	
	private Map<String, String> getClaimsFromToken(Set<String> claims, String token) throws ApisResourceAccessException {
		Map<String, Object> userInfo = getUserInfo(token);
		return claims.stream().map(claim -> new SimpleEntry<>(claim, getClaimFromUserInfo(userInfo, claim)))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
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
			String responseStr = restClientWithPlainRestTemplate.getApi(uriComponent.toUri(), String.class, headers);
			responseMap = (Map<String, Object>) decodeAndDecryptUserInfo(responseStr);
		} catch (ApisResourceAccessException e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "NA",
					"IdAuthServiceImp::lencryptRSA():: ENCRYPTIONSERVICE GET service call"
							+ ExceptionUtils.getStackTrace(e));
			throw new ApisResourceAccessException("Could not fetch public key from kernel keymanager", e);
		}
		return responseMap;
	}

	private Map<String, Object> decodeAndDecryptUserInfo(String userInfoResponseStr) throws JsonParseException, JsonMappingException, UnsupportedEncodingException, IOException  {
		String userInfoStr;
		if (Boolean.parseBoolean(this.env.getProperty(ResidentConstants.MOSIP_OIDC_JWT_SIGNED))) {
			DecodedJWT decodedJWT = JWT.decode(userInfoResponseStr);
			if (Boolean.parseBoolean(this.env.getProperty(ResidentConstants.MOSIP_OIDC_JWT_VERIFY_ENABLED))) {
				ImmutablePair<Boolean, AuthErrorCode> verifySignagure = tokenValidationHelper
						.verifyJWTSignagure(decodedJWT);
				if (verifySignagure.left) {
					userInfoStr = decodeString(getPayload(decodedJWT));
				} else {
					throw new ResidentServiceException(ResidentErrorCode.CLAIM_NOT_AVAILABLE,
							String.format(ResidentErrorCode.CLAIM_NOT_AVAILABLE.getErrorMessage(),
									String.format("User info signature validation failed. Error: %s: %s",
											verifySignagure.getRight().getErrorCode(),
											verifySignagure.getRight().getErrorMessage())));
				}
			} else {
				userInfoStr = decodeString(getPayload(decodedJWT));
			}
		} else {
			userInfoStr = userInfoResponseStr;
		}
		if(Boolean.parseBoolean(this.env.getProperty(ResidentConstants.MOSIP_OIDC_ENCRYPTION_ENABLED))){
			userInfoStr = decodeString(decryptPayload((String) userInfoStr));
		}
		return objectMapper.readValue(userInfoStr.getBytes(UTF_8), Map.class);
	}


	private String getPayload(DecodedJWT decodedJWT) {
		return decodedJWT.getPayload();
	}

	public String getResidentIndvidualIdFromSession() throws ApisResourceAccessException {
		return  getClaimValue(INDIVIDUAL_ID);
	}

	public String getClaimValue(String claim) throws ApisResourceAccessException {
		return getClaims(claim).get(claim);
	}
	public String getAvailableclaimValue(String claim) throws ApisResourceAccessException {
		String claimValue;
		try {
			claimValue = getClaims(claim).get(claim);
		} catch (ResidentServiceException e) {
			logger.error(e.getMessage());
			claimValue = null;
		}
		return claimValue;
	}

	public String getResidentIdaToken() throws ApisResourceAccessException, ResidentServiceCheckedException {
		return getIDATokenForIndividualId(getResidentIndvidualIdFromSession());
	}

	public String getResidentIdaTokenFromAccessToken(String accessToken) throws ApisResourceAccessException, ResidentServiceCheckedException {
		String claimName = env.getProperty(ResidentConstants.INDIVIDUALID_CLAIM_NAME);
		Map<String, ?> claims = getClaimsFromToken(Set.of(claimName), accessToken);
		String individualId = (String) claims.get(claimName);
		if(individualId==null){
			throw new ResidentServiceException(ResidentErrorCode.CLAIM_NOT_AVAILABLE, String.format(ResidentErrorCode.CLAIM_NOT_AVAILABLE.getErrorMessage(), claimName));
		}
		return getIDATokenForIndividualId(individualId);
	}
	
	public String createSessionId(){
		return utility.createEventId();
	}
	
	public String getSessionId() throws ApisResourceAccessException, ResidentServiceCheckedException {
		String residentIdaToken = getResidentIdaToken();
		return residentSessionRepo.findFirstByIdaTokenOrderByLoginDtimesDesc(residentIdaToken)
				.map(ResidentSessionEntity::getSessionId)
				.orElseGet(this::createSessionId);
	}

	/**
     * @param individualId - it can be UIN, VID or AID.
     * @return UIN or VID based on the flag "useVidOnly"
     */
	public String getIndividualIdForAid(String aid)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
			IdentityDTO identity = getIdentity(aid);
			String uin = identity.getUIN();
			String individualId;
			if(useVidOnly) {
				Optional<String> perpVid = residentVidService.getPerpatualVid(uin);
				if(perpVid.isPresent()) {
					individualId = perpVid.get();
				} else {
					throw new ResidentServiceCheckedException(ResidentErrorCode.PERPETUAL_VID_NOT_AVALIABLE);
				}
			} else {
				individualId = uin;
			}
			return individualId;
	}
	
	public String getResidentAuthenticationMode() throws ApisResourceAccessException, ResidentServiceCheckedException {
		String authenticationMode = getClaimFromIdToken(
				this.env.getProperty(ResidentConstants.AUTHENTICATION_MODE_CLAIM_NAME));
		String authTypeCode = utility.getAuthTypeCodefromkey(authenticationMode);
		return authTypeCode;
	}
	
	public String getClaimFromAccessToken(String claim) {
		AuthUserDetails authUserDetails = getAuthUserDetails();
		String accessToken = authUserDetails.getToken();
		return getClaimValueFromJwtToken(accessToken, claim);
	}

	public String getClaimFromIdToken(String claim) {
		AuthUserDetails authUserDetails= getAuthUserDetails();
		String idToken = authUserDetails.getIdToken();
		return getClaimValueFromJwtToken(idToken, claim);
	}

	public String getClaimValueFromJwtToken(String jwtToken, String claim) {
		String claimValue = "";
		String payLoad = "";
		if(jwtToken!=null){
			if(jwtToken.contains(".")){
				String[] parts = jwtToken.split("\\.", 0);
				payLoad = decodeString(parts[1]);
			} else{
				payLoad = decodeString(jwtToken);
			}
			Map payLoadMap;
			try {
				payLoadMap = objectMapper.readValue(payLoad, Map.class);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
			if(claim!=null){
				claimValue = (String) payLoadMap.get(claim);
			}
		}
		return claimValue;
	}

	public String decodeString(String payload)
	{
		byte[] bytes = Base64.getUrlDecoder().decode(payload);
		return new String(bytes, StandardCharsets.UTF_8);
	}

	public String decryptPayload(String payload) {
		return objectStoreHelper.decryptData(payload, this.env.getProperty(ResidentConstants.RESIDENT_APP_ID), this.env.getProperty(ResidentConstants.IDP_REFERENCE_ID));
	}

	public String getIndividualIdType(String individualId){
		if(requestValidator.validateUin(individualId)){
			return UIN;
		} else if(requestValidator.validateVid(individualId)){
			return VID;
		} else if(requestValidator.validateRid(individualId)){
			return AID;
		} else {
			throw new InvalidInputException(ResidentConstants.INDIVIDUAL_ID);
		}
	}
}
