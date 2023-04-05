package io.mosip.resident.util;

import java.io.IOException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.kernel.core.util.TokenHandlerUtil;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.dto.ClientIdSecretKeyRequestDto;
import io.mosip.resident.dto.TokenRequestDto;
import io.mosip.resident.exception.TokenGenerationFailedException;

@Component
public class TokenGenerator {

    private static Logger logger = LoggerConfiguration.logConfig(TokenGenerator.class);

    @Autowired
    Environment environment;

	private static final String AUTHORIZATION = "Authorization=";


    /**
     * This method gets the token for the user details present in config server.
     *
     * @return
     * @throws IOException
     */
    public String getToken() throws IOException {
        return generateToken(setRequestDto());
    }

    private String generateToken(ClientIdSecretKeyRequestDto dto) throws IOException {
        // TokenRequestDTO<PasswordRequest> tokenRequest = new
        // TokenRequestDTO<PasswordRequest>();
		String token = System.getProperty("token");
		boolean isValid = false;

		if (StringUtils.isNotEmpty(token)) {

			isValid = TokenHandlerUtil.isValidBearerToken(token, environment.getProperty("token.request.issuerUrl"),
					environment.getProperty("resident.clientId"));

		}
		if (!isValid) {
        TokenRequestDto tokenRequest = new TokenRequestDto();
        tokenRequest.setId(environment.getProperty("token.request.id"));

        tokenRequest.setRequesttime(DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime()));
        // tokenRequest.setRequest(setPasswordRequestDTO());
        tokenRequest.setRequest(dto);
        tokenRequest.setVersion(environment.getProperty("token.request.version"));

        Gson gson = new Gson();
        HttpClient httpClient = HttpClientBuilder.create().build();
        // HttpPost post = new
        // HttpPost(environment.getProperty("PASSWORDBASEDTOKENAPI"));
        HttpPost post = new HttpPost(environment.getProperty("KERNELAUTHMANAGER"));
        try {
            StringEntity postingString = new StringEntity(gson.toJson(tokenRequest));
            post.setEntity(postingString);
            post.setHeader("Content-type", "application/json");
            HttpResponse response = httpClient.execute(post);
            org.apache.http.HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity, "UTF-8");
            logger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                    LoggerFileConstant.APPLICATIONID.toString(), "Resonse body=> " + responseBody);
            Header[] cookie = response.getHeaders("Set-Cookie");
            if (cookie.length == 0)
                throw new TokenGenerationFailedException();
				token = response.getHeaders("Set-Cookie")[0].getValue();
            logger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                    LoggerFileConstant.APPLICATIONID.toString(), "Cookie => " + cookie[0]);
				System.setProperty("token", token.substring(14, token.indexOf(';')));
            return token.substring(0, token.indexOf(';'));
        } catch (IOException e) {
            logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                    LoggerFileConstant.APPLICATIONID.toString(), e.getMessage() + ExceptionUtils.getStackTrace(e));
            throw e;
        }
		}
		return AUTHORIZATION + token;
    }

    private ClientIdSecretKeyRequestDto setRequestDto() {
        ClientIdSecretKeyRequestDto request = new ClientIdSecretKeyRequestDto();
        request.setAppId(environment.getProperty("resident.appid"));
        request.setClientId(environment.getProperty("resident.clientId"));
        request.setSecretKey(environment.getProperty("resident.secretKey"));
        return request;
    }

}
