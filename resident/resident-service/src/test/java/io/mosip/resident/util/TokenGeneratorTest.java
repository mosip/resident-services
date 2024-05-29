package io.mosip.resident.util;

import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.IOException;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.env.Environment;

import io.mosip.kernel.core.util.TokenHandlerUtil;
import io.mosip.resident.exception.TokenGenerationFailedException;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PrepareForTest({ HttpClientBuilder.class, TokenHandlerUtil.class })
@Ignore
public class TokenGeneratorTest {

    @Mock
    private HttpClientBuilder httpClientBuilder;

    @Mock
    private CloseableHttpClient httpClient;

    @Mock
    private CloseableHttpResponse response;

    @Mock
    Environment environment;

    /*@Mock
    org.apache.http.HttpEntity entity;*/

    @InjectMocks
    TokenGenerator tokenGenerator;

    @Before
    public void setup() throws IOException {
        when(environment.getProperty("token.request.id")).thenReturn("RequestId");
        when(environment.getProperty("token.request.version")).thenReturn("RequestVersion");
        when(environment.getProperty("KERNELAUTHMANAGER")).thenReturn("http://localhost:8080");
        when(environment.getProperty("token.request.id")).thenReturn("RequestId");
        when(environment.getProperty("token.request.id")).thenReturn("RequestId");
		when(environment.getProperty("token.request.issuerUrl")).thenReturn("http://keycloak");
		when(environment.getProperty("resident.clientId")).thenReturn("resident");
        mockStatic(HttpClientBuilder.class);
        when(HttpClientBuilder.create()).thenReturn(httpClientBuilder);
        when(httpClientBuilder.build()).thenReturn(httpClient);
        when(httpClient.execute(any())).thenReturn(response);
    }

    @Test
    public void getTokenTest() throws IOException {
		String token = "Authorizationtoken";
        HttpEntity entity = new StringEntity(token);
		BasicHeader header = new BasicHeader("token", "Authorizationtoken;");
        BasicHeader[] headers = new BasicHeader[1];
        headers[0] = header;

        when(response.getEntity()).thenReturn(entity);
        when(response.getHeaders("Set-Cookie")).thenReturn(headers);

        String result = tokenGenerator.getToken();

        Assert.assertTrue("Expected token", result.equals(token));
    }

    @Test(expected = TokenGenerationFailedException.class)
    public void tokenGenerationFailedTest() throws IOException {
        String token = "token";
        HttpEntity entity = new StringEntity(token);
        BasicHeader[] headers = new BasicHeader[0];

        when(response.getEntity()).thenReturn(entity);
        when(response.getHeaders("Set-Cookie")).thenReturn(headers);

        tokenGenerator.getToken();
    }

    @Test(expected = IOException.class)
    public void ioExceptionTest() throws IOException {

        when(httpClient.execute(any())).thenThrow(new IOException("IO exception occured"));

        tokenGenerator.getToken();
    }

	@Test
	public void getExistingTokenTest() throws Exception {
		PowerMockito.mockStatic(TokenHandlerUtil.class);
		PowerMockito.when(TokenHandlerUtil.class, "isValidBearerToken", "Authorizationtoken", "", "").thenReturn(true);
		String token = "Authorizationtoken";
		HttpEntity entity = new StringEntity(token);
		BasicHeader header = new BasicHeader("token", "Authorizationtoken;");
		BasicHeader[] headers = new BasicHeader[1];
		headers[0] = header;

		when(response.getEntity()).thenReturn(entity);
		when(response.getHeaders("Set-Cookie")).thenReturn(headers);
		System.setProperty("token", "token");
		String result = tokenGenerator.getToken();

		Assert.assertTrue("Expected token", result.equals(token));
	}
}
