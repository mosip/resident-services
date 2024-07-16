package io.mosip.resident.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.authcodeflowproxy.api.validator.ValidateTokenUtil;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.openid.bridge.api.constants.AuthErrorCode;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.helper.ObjectStoreHelper;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Kamesh Shekhar Prasad
 */

@Component
public class UserInfoUtility {

    @Value("${mosip.iam.userinfo_endpoint}")
    private String usefInfoEndpointUrl;

    private static final String AUTHORIZATION = "Authorization";

    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    @Qualifier("restClientWithPlainRestTemplate")
    private ResidentServiceRestClient restClientWithPlainRestTemplate;

    @Autowired
    private Environment env;

    @Autowired
    private ValidateTokenUtil tokenValidationHelper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ObjectStoreHelper objectStoreHelper;

    private static final Logger logger = LoggerConfiguration.logConfig(UserInfoUtility.class);


    @Cacheable(value = "userInfoCache", key = "#token")
    public Map<String, Object> getUserInfo(String token) throws ApisResourceAccessException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(usefInfoEndpointUrl);
        UriComponents uriComponent = builder.build(false).encode();

        Map<String, Object> responseMap;
        try {
            MultiValueMap<String, String> headers =
                    new LinkedMultiValueMap<String, String>(Map.of(AUTHORIZATION, List.of(BEARER_PREFIX + token)));
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

    private Map<String, Object> decodeAndDecryptUserInfo(String userInfoResponseStr) throws JsonParseException, JsonMappingException, UnsupportedEncodingException, IOException {
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
        if (Boolean.parseBoolean(this.env.getProperty(ResidentConstants.MOSIP_OIDC_ENCRYPTION_ENABLED))) {
            userInfoStr = decodeString(decryptPayload((String) userInfoStr));
        }
        return objectMapper.readValue(userInfoStr.getBytes(UTF_8), Map.class);
    }

    public String decodeString(String payload) {
        byte[] bytes = java.util.Base64.getUrlDecoder().decode(payload);
        return new String(bytes, UTF_8);
    }

    private String getPayload(DecodedJWT decodedJWT) {
        return decodedJWT.getPayload();
    }

    public String decryptPayload(String payload) {
        return objectStoreHelper.decryptData(payload, this.env.getProperty(ResidentConstants.RESIDENT_APP_ID),
                this.env.getProperty(ResidentConstants.IDP_REFERENCE_ID));
    }
}
