package io.mosip.resident.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.IdentityService;
import io.mosip.resident.util.*;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Resident identity service implementation class.
 * 
 * @author Ritik Jain
 */
@Component
public class IdentityServiceImpl implements IdentityService {

	@Autowired
	private Utility utility;

	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private Environment env;

	@Autowired
	private AuthUserDetailsUtil authUserDetailsUtil;

	@Value("${resident.flag.use-vid-only:false}")
	private boolean useVidOnly;

	@Autowired
	private IdentityDataUtil identityDataUtil;

	private static final Logger logger = LoggerConfiguration.logConfig(IdentityServiceImpl.class);

    @Autowired
	private AvailableClaimUtility availableClaimUtility;

	@Autowired
	private UinVidValidator uinVidValidator;

	@Autowired
	private IdentityUtil identityUtil;

	@Autowired
	private AvailableClaimValueUtility availableClaimValueUtility;

	@Autowired
	private PerpetualVidUtil perpetualVidUtil;

	public String getResidentIdaTokenFromAccessToken(String accessToken) throws ApisResourceAccessException, ResidentServiceCheckedException {
		String claimName = env.getProperty(ResidentConstants.INDIVIDUALID_CLAIM_NAME);
		Map<String, ?> claims = availableClaimValueUtility.getClaimsFromToken(Set.of(claimName), accessToken);
		String individualId = (String) claims.get(claimName);
		return availableClaimUtility.getIDATokenForIndividualId(individualId);
	}
	
	public String createSessionId(){
		return utility.createEventId();
	}

	/**
     * @param individualId - it can be UIN, VID or AID.
     * @return UIN or VID based on the flag "useVidOnly"
     */
	public Tuple2<String, IdType> getIdAndTypeForIndividualId(String individualId)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		String id;
		IdType idType = uinVidValidator.getIndividualIdType(individualId);
		if(idType.equals(IdType.AID)) {
			IdentityDTO identity = identityUtil.getIdentity(individualId);
			String uin = identity.getUIN();
			if(useVidOnly) {
				Optional<String> perpVid = perpetualVidUtil.getPerpatualVid(uin);
				if(perpVid.isPresent()) {
					id = perpVid.get();
					idType = IdType.VID;
				} else {
					throw new ResidentServiceCheckedException(ResidentErrorCode.PERPETUAL_VID_NOT_AVALIABLE);
				}
			} else {
				id = uin;
				idType = IdType.UIN;
			}
		} else {
			id = individualId;
		}
		return Tuples.of(id, idType);
	}
	
	public String getResidentAuthenticationMode() throws ResidentServiceCheckedException {
		String authenticationMode = getClaimFromIdToken(
				this.env.getProperty(ResidentConstants.AUTHENTICATION_MODE_CLAIM_NAME));
		String authTypeCode = identityDataUtil.getAuthTypeCodefromkey(authenticationMode);
		if(authTypeCode == null) {
			logger.warn("Mapping is missing for %s in AMR to ACR mapping file", authenticationMode);
			return authenticationMode;
		}
		return authTypeCode;
	}

	public String getClaimFromIdToken(String claim) {
		AuthUserDetails authUserDetails= authUserDetailsUtil.getAuthUserDetails();
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

}
