package io.mosip.resident.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.idrepository.core.util.TokenIDGenerator;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.IdResponseDTO1;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.exception.VidCreationException;
import io.mosip.resident.handler.service.ResidentConfigService;
import io.mosip.resident.service.IdentityService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.util.Utility;
import io.mosip.resident.validator.RequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.mosip.resident.util.Utility.IDENTITY;

/**
 * Resident identity service implementation class.
 * 
 * @author Ritik Jain
 */
@Component
public class IdentityServiceImpl implements IdentityService {

	private static final String UIN = "UIN";
	private static final String INDIVIDUAL_ID = "individual_id";
	private static final String EMAIL = "email";
	private static final String PHONE = "phone";
	private static final String DATE_OF_BIRTH = "dob";
	private static final String NAME = "name";
	private static final String IMAGE = "mosip.resident.photo.token.claim-photo";
	private static final String PHOTO_ATTRIB_PROP = "mosip.resident.photo.attribute.name";

	private static final String VID = "VID";
	private static final String AID = "AID";
	private static final String  PERPETUAL_VID = "perpetualVID";

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
	
	@Value("${resident.dateofbirth.pattern}")
	private String dateFormat;

	@Autowired
	private ResidentVidService residentVidService;

	@Value("${resident.flag.use-vid-only:false}")
	private boolean useVidOnly;
	
	@Autowired
    private Utilities  utilities;

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
			identityDTO.setUIN(utility.getMappingValue(identity, UIN));
			identityDTO.setEmail(utility.getMappingValue(identity, EMAIL));
			identityDTO.setPhone(utility.getMappingValue(identity, PHONE));
			String dateOfBirth = utility.getMappingValue(identity, DATE_OF_BIRTH);
			if(dateOfBirth != null && !dateOfBirth.isEmpty()) {
				identityDTO.setDateOfBirth(dateOfBirth);
				DateTimeFormatter formatter=DateTimeFormatter.ofPattern(dateFormat);
				LocalDate localDate=LocalDate.parse(dateOfBirth, formatter);
				identityDTO.setYearOfBirth(Integer.toString(localDate.getYear()));
			}
			String name = utility.getMappingValue(identity, NAME, langCode);
			identityDTO.setFullName(name);
			identityDTO.putAll((Map<? extends String, ? extends Object>) identity.get(IDENTITY));

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
		try {
			IdResponseDTO1 idResponseDTO1;
			if(Utility.isSecureSession()){
				idResponseDTO1 = (IdResponseDTO1)utility.getCachedIdentityData(id, getAccessToken(), IdResponseDTO1.class);
			} else {
				idResponseDTO1 = (IdResponseDTO1)utility.getIdentityData(id, IdResponseDTO1.class);
			}
			if(idResponseDTO1.getErrors() != null && idResponseDTO1.getErrors().size() > 0) {
				throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
						idResponseDTO1.getErrors().get(0).getErrorCode() + " --> " + idResponseDTO1.getErrors().get(0).getMessage());
			}
			Map<String,Object> identity = (Map<String, Object>) idResponseDTO1.getResponse().getIdentity();
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
						if(a.equals(PERPETUAL_VID) || a.equals(ResidentConstants.MASK_PERPETUAL_VID) && !identity.containsKey(PERPETUAL_VID)) {
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
								identity.put(attr, utility.convertToMaskData((String) identity.get(attributeName)));
							}
						}
					})
					.filter(attrib -> identity.containsKey(attrib))
					.collect(Collectors.toMap(Function.identity(), identity::get,(m1, m2) -> m1, () -> new LinkedHashMap<String, Object>()));
			response.put(IDENTITY, identity);
			logger.debug("IdentityServiceImpl::getIdentityAttributes()::exit");
			return response;
		} catch (ApisResourceAccessException e) {
			logger.error("Error occured in accessing identity data %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
	}


	@Override
	public String getUinForIndividualId(String idvid) throws ResidentServiceCheckedException {
	
		try {
			if(getIndividualIdType(idvid).equalsIgnoreCase(UIN)){
				return idvid;
			}
			return getIdentity(idvid).getUIN();
		} catch (VidCreationException e) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.VID_CREATION_EXCEPTION.getErrorCode(),
					ResidentErrorCode.VID_CREATION_EXCEPTION.getErrorMessage());
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
		String accessToken = getAccessToken();
		if (!Objects.equals(accessToken, "")) {
			return getClaimsFromToken(claims, accessToken);
		}
		return Map.of();
	}

	public String getAccessToken(){
		AuthUserDetails authUserDetails = getAuthUserDetails();
		if(authUserDetails != null){
			return authUserDetails.getToken();
		}
		return "";
	}
	
	private Map<String, String> getClaimsFromToken(Set<String> claims, String token) throws ApisResourceAccessException {
		Map<String, Object> userInfo = utility.getUserInfo(token);
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

	public String getResidentIndvidualIdFromSession() throws ApisResourceAccessException {
		return  getClaimValue(INDIVIDUAL_ID);
	}

	public String getClaimValue(String claim) throws ApisResourceAccessException {
		return getClaims(claim).get(claim);
	}
	public String getAvailableclaimValue(String claim) throws ApisResourceAccessException {
		logger.debug("IdentityServiceImpl::getAvailableclaimValue()::entry");
		String claimValue;
		try {
			claimValue = getClaims(claim).get(claim);
		} catch (ResidentServiceException e) {
			logger.error(e.getMessage());
			claimValue = null;
		}
		logger.debug("IdentityServiceImpl::getAvailableclaimValue()::exit");
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

	/**
     * @param aid - it can be UIN, VID or AID.
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
	
	public String getResidentAuthenticationMode() throws ResidentServiceCheckedException {
		String authenticationMode = getClaimFromIdToken(
				this.env.getProperty(ResidentConstants.AUTHENTICATION_MODE_CLAIM_NAME));
		String authTypeCode = utility.getAuthTypeCodefromkey(authenticationMode);
		if(authTypeCode == null) {
			logger.warn("Mapping is missing for %s in AMR to ACR mapping file", authenticationMode);
			return authenticationMode;
		}
		return authTypeCode;
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
