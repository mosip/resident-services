package io.mosip.resident.test.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.idrepository.core.util.TokenIDGenerator;
import io.mosip.kernel.authcodeflowproxy.api.validator.ValidateTokenUtil;
import io.mosip.kernel.biometrics.spi.CbeffUtil;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.openid.bridge.api.constants.AuthErrorCode;
import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.kernel.openid.bridge.model.MosipUserDto;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.exception.VidCreationException;
import io.mosip.resident.handler.service.ResidentConfigService;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.IdentityService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.util.Utilitiy;
import io.mosip.resident.validator.RequestValidator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class IdentityServiceTest {

	@InjectMocks
	private IdentityService identityService = new IdentityServiceImpl();

	@Mock
	private AuditUtil auditUtil;

	@Mock
	private Utilitiy utility;

	@Mock
	private CbeffUtil cbeffUtil;

	@Mock
	private TokenIDGenerator tokenIDGenerator;

	@Mock
	private ResidentVidService residentVidService;

	@Mock
	private ResidentServiceRestClient restClientWithSelfTOkenRestTemplate;

	@Mock
	private ResidentServiceRestClient restClientWithPlainRestTemplate;

	@Mock
	private ResidentConfigService residentConfigService;
	
	@Mock
	private Environment env;

	@Mock
	private RequestValidator requestValidator;

	@Mock
	private Environment environment;

	@Mock
	private ObjectStoreHelper objectStoreHelper;
	
	@Mock
	private Utilities utilities;

	@Mock
	private ValidateTokenUtil tokenValidationHelper;

	private ResponseWrapper responseWrapper;

	private Map responseMap;

	private List responseList;

	private Map bdbFaceMap;

	private ObjectMapper objectMapper = new ObjectMapper();

	private String token;
	
	
	@Before
	public void setUp() throws Exception {
		ReflectionTestUtils.setField(identityService, "dateFormat", "yyyy/MM/dd");
		ReflectionTestUtils.setField(identityService, "objectMapper", objectMapper);

		Map identityMap = new LinkedHashMap();
		identityMap.put("UIN", "8251649601");
		identityMap.put("email", "manojvsp12@gmail.com");
		identityMap.put("phone", "9395910872");
		identityMap.put("dateOfBirth", "1970/11/16");

		List<Map> fNameList = new ArrayList();
		Map fNameMap = new LinkedHashMap();
		fNameMap.put("language", "eng");
		fNameMap.put("value", "Rahul");
		fNameList.add(fNameMap);
		identityMap.put("firstName", fNameList);

		List<Map> mNameList = new ArrayList();
		Map mNameMap = new LinkedHashMap();
		mNameMap.put("language", "eng");
		mNameMap.put("value", "Kumar");
		mNameList.add(mNameMap);
		identityMap.put("middleName", mNameList);

		List<Map> lNameList = new ArrayList();
		Map lNameMap = new LinkedHashMap();
		lNameMap.put("language", "eng");
		lNameMap.put("value", "Singh");
		lNameList.add(lNameMap);
		identityMap.put("lastName", lNameList);

		responseMap = new LinkedHashMap();
		responseMap.put("identity", identityMap);

		List<Map> docList = new ArrayList();
		Map docMap = new LinkedHashMap();
		docMap.put("category", "individualBiometrics");
		docMap.put("value", "encodedValue");
		docList.add(docMap);
		responseMap.put("documents", docList);

		bdbFaceMap = new HashMap();
		bdbFaceMap.put("face", "this is a face biometric key");

		responseWrapper = new ResponseWrapper<>();
		responseWrapper.setVersion("v1");
		responseWrapper.setId("1");
		responseWrapper.setResponse(responseMap);
		
		when(env.getProperty(anyString())).thenReturn("property");
		Mockito.when(requestValidator.validateUin(Mockito.anyString())).thenReturn(true);
		token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJubEpTaUExM2tPUWhZQ0JxMEVKSkRlWnFTOGsybDB3MExUbmQ1WFBCZ20wIn0." +
				"eyJleHAiOjE2NzIxMjU0NjEsImlhdCI6MTY3MjAzOTA2MSwianRpIjoiODc5YTdmYTItZWZhYy00YTQwLTkxODQtNzZiM2FhMWJiODg0IiwiaXNzIjoiaHR0c" +
				"HM6Ly9pYW0uZGV2Lm1vc2lwLm5ldC9hdXRoL3JlYWxtcy9tb3NpcCIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiJiNTc3NjkzYi0xOWI1LTRlYTktYWEzNy1kMT" +
				"EzMjdkOGRkNzkiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJtb3NpcC1yZXNpZGVudC1jbGllbnQiLCJzZXNzaW9uX3N0YXRlIjoiNWNmZWIzNTgtNGY1Ni00NjM" +
				"0LTg3NmQtNGFjNzk1OTYyYWRkIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9u" +
				"IiwiZGVmYXVsdC1yb2xlcy1tb3NpcCJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYW" +
				"Njb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoid2FsbGV0X2JpbmRpbmcgYXV0aC5oaXN0b3J5LnJlYWRvbmx5IG1pY3JvcHJvZmlsZS1q" +
				"d3QgaWRlbnRpdHkucmVhZG9ubHkgaWRhX3Rva2VuIG9mZmxpbmVfYWNjZXNzIGFkZHJlc3MgdXBkYXRlX29pZGNfY2xpZW50IGNyZWRlbnRpYWwubWFuYWdlIH" +
				"ZpZC5tYW5hZ2UgZ2V0X2NlcnRpZmljYXRlIGFkZF9vaWRjX2NsaWVudCB2aWQucmVhZG9ubHkgaWRlbnRpdHkudXBkYXRlIG5vdGlmaWNhdGlvbnMubWFuYWdl" +
				"IGVtYWlsIHVwbG9hZF9jZXJ0aWZpY2F0ZSBhdXRoLnJlYWRvbmx5IGF1dGgubWV0aG9kLm1hbmFnZSBub3RpZmljYXRpb25zLnJlYWRvbmx5IGluZGl2aWR1YWxf" +
				"aWQgYXV0aC5oaXN0b3J5Lm1hbmFnZSB0ZXN0IHByb2ZpbGUgY2FyZC5tYW5hZ2Ugc2VuZF9iaW5kaW5nX290cCIsInNpZCI6IjVjZmViMzU4LTRmNTYtNDYzNC0" +
				"4NzZkLTRhYzc5NTk2MmFkZCIsInVwbiI6ImthbWVzaCIsImFkZHJlc3MiOnt9LCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsIm5hbWUiOiJLYW1lc2ggU2hla2hh" +
				"ciIsImdyb3VwcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIiwiZGVmYXVsdC1yb2xlcy1tb3NpcCJdLCJwcmVmZXJyZWRfdXNlcm5hb" +
				"WUiOiJrYW1lc2giLCJnaXZlbl9uYW1lIjoiS2FtZXNoIiwiZmFtaWx5X25hbWUiOiJTaGVraGFyIiwicGljdHVyZSI6ImlWQk9SdzBLR2dvQUFBQU5TVWhFVW" +
				"dBQUFBb0FBQUFLQ0FJQUFBQUNVRmpxQUFBQUFYTlNSMElBcnM0YzZRQUFBQVJuUVUxQkFBQ3hqd3Y4WVFVQUFBQUpjRWhaY3dBQUZpVUFBQllsQVVsU0pQQUF" +
				"BQUJDU1VSQlZDaFRiWXRCRWdBZ0NBTDcvNmVOaEJ5MDlxRGk2Z3BqWFpTeFVVOG8vanJmcERtY21ZMVFBT1doZ1Rzd3Y2c1NtOHpWaFVMbGdzdCsrOFQ1MUlq" +
				"WU5VSGRJKzRYWkhvQUFBQUFTVVZPUks1Q1lJST0iLCJlbWFpbCI6ImthbWVzaHNyMTMzOEBnbWFpbC5jb20ifQ.YLddWNd7ldiMvPhDK0HhXaKjEmeOE0T6wS" +
				"CjfN3mlwxDxHm2DzMHnwbKR5orEm1NRyCnUfGGm5IMVTdDnXz1iUAsU7zeKA2XOdH3zQgMUu-vqJpgRWRG-XJHakSyblfAFIVAILRi7rwJQjL7X1lhm1ZAqUX" +
				"Soh6kZBoOeYd_29RQQzFQNzpn_Ahk4GxQu_TLyvoWeNXpfx94om7TqrZYghtTg5_svku2P0NuFxzbWysPMjaHrEff0idKY94sKJ6eNpLXRXbJCPkAHtfVY0U3" +
				"YDQqWUpYjE3hQCZz0u_L8sieJIN3mYtjd12rfOrjEKu2fFGu5UbJRVqkmOw0egVGHw";
	}

	@Test
	public void testGetIdentity() throws Exception {
		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), anyMap(), anyList(), anyList(), any()))
				.thenReturn(responseWrapper);
		responseWrapper.setErrors(null);
		when(residentConfigService.getUiSchemaFilteredInputAttributes(anyString()))
				.thenReturn(List.of("UIN", "email", "phone", "dateOfBirth", "firstName", "middleName", "lastName"));
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		when(utility.getMappingJson()).thenReturn(mappingJson);
		IdentityDTO result = identityService.getIdentity("6", false, "eng");
		assertNotNull(result);
		assertEquals("8251649601", result.getUIN());
		
	}
	
	@Test
	public void testGetMappingValueNull() throws Exception {
		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), anyMap(), anyList(), anyList(), any()))
				.thenReturn(responseWrapper);
		responseWrapper.setErrors(null);
		when(residentConfigService.getUiSchemaFilteredInputAttributes(anyString()))
				.thenReturn(List.of("UIN", "email", "phone", "dateOfBirth"));
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		when(utility.getMappingJson()).thenReturn(mappingJson);
		String str = CryptoUtil.encodeToURLSafeBase64("response return".getBytes());
		IdentityDTO result = identityService.getIdentity("6", false, "eng");
		assertNotNull(result);
		assertEquals("8251649601", result.getUIN());
	}

	@Test
	public void testGetIdentityLangCodeNull() throws Exception {
		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), anyMap(), anyList(), anyList(), any()))
				.thenReturn(responseWrapper);
		responseWrapper.setErrors(null);
		when(residentConfigService.getUiSchemaFilteredInputAttributes(anyString()))
				.thenReturn(List.of("UIN", "email", "phone", "dateOfBirth", "firstName", "middleName", "lastName"));
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		when(utility.getMappingJson()).thenReturn(mappingJson);
		String str = CryptoUtil.encodeToURLSafeBase64("response return".getBytes());
		IdentityDTO result = identityService.getIdentity("6");
		assertNotNull(result);
		assertEquals("8251649601", result.getUIN());
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetIdentityAttributesIf() throws Exception {
		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), anyMap(), anyList(), anyList(), any()))
				.thenReturn(responseWrapper);
		ServiceError error = new ServiceError();
		error.setErrorCode("101");
		error.setMessage("errors");

		List<ServiceError> errorList = new ArrayList<ServiceError>();
		errorList.add(error);

		responseWrapper.setErrors(errorList);
		identityService.getIdentity("6");
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetIdentityAttributesWithApisResourceAccessException() throws Exception {
		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), anyMap(), anyList(), anyList(), any()))
				.thenThrow(new ApisResourceAccessException());
		identityService.getIdentity("6");
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetMappingValueIf() throws Exception {
		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), anyMap(), anyList(), anyList(), any()))
				.thenReturn(responseWrapper);
		identityService.getIdentity("6");
	}

	@Test
	public void testGetUinForIndividualId() throws Exception{
		String id = "123456789";
		responseWrapper.setErrors(null);
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		String result = identityService.getUinForIndividualId(id);
		assertEquals("123456789", result);
	}

	@Test
	public void testGetIDATokenForIndividualId() throws Exception{
		String id = "123456789";
		String token = "1234";
		ReflectionTestUtils.setField(identityService, "onlineVerificationPartnerId", "m-partner-default-auth");
		when(tokenIDGenerator.generateTokenID(anyString(), anyString())).thenReturn(token);
		responseWrapper.setErrors(null);
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		String result = identityService.getIDATokenForIndividualId(id);
		assertEquals(token, result);
	}

	@Test(expected = ResidentServiceException.class)
	public void testGetClaimFromUserInfoFailure(){
		Map<String, Object> userInfo = new HashMap<>();
		ReflectionTestUtils.invokeMethod(identityService, "getClaimFromUserInfo", userInfo, "claim");
	}

	@Test
	public void testGetClaimFromUserInfoSuccess() {
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("claim", "value");
		String result = ReflectionTestUtils.invokeMethod(identityService, "getClaimFromUserInfo", userInfo, "claim");
		assertEquals("value", result);
	}

	@Test
	public void testGetUserInfoSuccess() throws ApisResourceAccessException, JsonProcessingException {
		String token = "1234";
		ReflectionTestUtils.setField(identityService, "usefInfoEndpointUrl", "http://localhost:8080/userinfo");
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("claim", "value");
		URI uri = URI.create("http://localhost:8080/userinfo");
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", "Bearer " + token);

		when(restClientWithPlainRestTemplate.getApi(uri, String.class, headers))
				.thenReturn(objectMapper.writeValueAsString(userInfo));
		Map<String, Object> result = ReflectionTestUtils.invokeMethod(identityService, "getUserInfo", token);
		assertEquals("value", result.get("claim"));
	}

	@Test
	public void testGetIndividualIdForAid() throws Exception{
		String aid = "123456789";
		ReflectionTestUtils.setField(identityService, "dateFormat", "yyyy/MM/dd");
		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), anyMap(), anyList(), anyList(), any()))
				.thenReturn(responseWrapper);
		responseWrapper.setErrors(null);
		when(residentConfigService.getUiSchemaFilteredInputAttributes(anyString()))
				.thenReturn(List.of("UIN", "email", "phone", "dateOfBirth", "firstName", "middleName", "lastName"));
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		when(utility.getMappingJson()).thenReturn(mappingJson);
		String result = ReflectionTestUtils.invokeMethod(identityService, "getIndividualIdForAid", aid);
		assertEquals("8251649601", result);
	}

	@Test
	public void testGetIndividualIdForAidUseVidOnlyTrue() throws Exception{
		String aid = "123456789";
		Optional<String> perpVid = Optional.of("8251649601");
		when(residentVidService.getPerpatualVid(anyString())).thenReturn(perpVid);
		ReflectionTestUtils.setField(identityService, "dateFormat", "yyyy/MM/dd");
		ReflectionTestUtils.setField(identityService,"useVidOnly", true);
		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), anyMap(), anyList(), anyList(), any()))
				.thenReturn(responseWrapper);
		responseWrapper.setErrors(null);
		when(residentConfigService.getUiSchemaFilteredInputAttributes(anyString()))
				.thenReturn(List.of("UIN", "email", "phone", "dateOfBirth", "firstName", "middleName", "lastName"));
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		when(utility.getMappingJson()).thenReturn(mappingJson);
		String result = ReflectionTestUtils.invokeMethod(identityService, "getIndividualIdForAid", aid);
		assertEquals("8251649601", result);
	}

	@Test
	public void testGetIndividualIdTypeUin(){
		Mockito.when(requestValidator.validateUin(Mockito.anyString())).thenReturn(true);
		assertEquals(IdType.UIN.toString(), identityService.getIndividualIdType("2476302389"));
	}

	@Test
	public void testGetIndividualIdTypeVid(){
		assertEquals(IdType.UIN.toString(), identityService.getIndividualIdType("2476302389"));
	}

	@Test
	public void testDecryptPayload(){
		Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("RESIDENT");
		Mockito.when(objectStoreHelper.decryptData(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn("payload");
		assertEquals("payload", ReflectionTestUtils.invokeMethod(identityService, "decryptPayload", "payload"));
	}

	@Test
	public void testDecodeString(){
		String encodedString = "c3RyaW5n";
		assertEquals("string", ReflectionTestUtils.invokeMethod(identityService, "decodeString", encodedString));
	}

	@Test
	public void testGetClaimValueFromJwtToken(){
		assertEquals("account", ReflectionTestUtils.invokeMethod(identityService, "getClaimValueFromJwtToken",
				token, "aud"));
	}

	@Test
	public void testGetClaimValueFromJwtTokenNullToken(){
		token = null;
		assertEquals("", ReflectionTestUtils.invokeMethod(identityService, "getClaimValueFromJwtToken",
				token, "aud"));
	}

	@Test(expected = RuntimeException.class)
	public void testGetClaimValueFromJwtTokenDecryptedTokenFailed(){
		token = "c3RyaW5n";
		assertEquals("", ReflectionTestUtils.invokeMethod(identityService, "getClaimValueFromJwtToken",
				token, "aud"));
	}

	@Test(expected = RuntimeException.class)
	public void testGetClaimValueFromJwtTokenDecryptedToken(){
		token = "YLddWNd7ldiMvPhDK0HhXaKjEmeOE0T6wSCjfN3mlwxDxHm2DzMHnwbKR5orEm1NRyCnUfGGm5IMVTdDnXz1iUAsU7zeKA2XOdH3zQgMUu" +
				"-vqJpgRWRG-XJHakSyblfAFIVAILRi7rwJQjL7X1lhm1ZAqUXSoh6kZBoOeYd_29RQQzFQNzpn_Ahk4GxQu_TLyvoWeNXpfx94om7TqrZYghtTg" +
				"5_svku2P0NuFxzbWysPMjaHrEff0idKY94sKJ6eNpLXRXbJCPkAHtfVY0U3YDQqWUpYjE3hQCZz0u_L8sieJIN3mYtjd12rfOrjEKu2fFGu5UbJRV" +
				"qkmOw0egVGHw";
		assertEquals("", ReflectionTestUtils.invokeMethod(identityService, "getClaimValueFromJwtToken",
				token, "aud"));
	}

	@Test
	public void testGetClaimValueFromJwtTokenNullClaim() throws ResidentServiceCheckedException {
		Mockito.when(requestValidator.validateUin(Mockito.anyString())).thenReturn(true);
		assertEquals("2476302389",identityService.getUinForIndividualId("2476302389"));
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetMappingValueNullIoException() throws Exception {
		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), anyMap(), anyList(), anyList(), any()))
				.thenReturn(responseWrapper);
		responseWrapper.setErrors(null);
		when(residentConfigService.getUiSchemaFilteredInputAttributes(anyString()))
				.thenReturn(List.of("UIN", "email", "phone", "dateOfBirth"));
		String mappingJson = "mappingJson";
		when(utility.getMappingJson()).thenReturn(mappingJson);
		IdentityDTO result = identityService.getIdentity("6", false, "eng");
		assertNotNull(result);
		assertEquals("6", result.getUIN());
	}

	@Test
	public void testGetMappingValueInvalidPerpetualVid() throws Exception {
		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), anyMap(), anyList(), anyList(), any()))
				.thenReturn(responseWrapper);
		responseWrapper.setErrors(null);
		when(residentConfigService.getUiSchemaFilteredInputAttributes(anyString()))
				.thenReturn(List.of("UIN", "email", "phone", "dateOfBirth", "perpetualVID"));
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		when(utility.getMappingJson()).thenReturn(mappingJson);
		IdentityDTO result = identityService.getIdentity("6", false, "eng");
		assertNotNull(result);
		assertEquals("8251649601", result.getUIN());
	}

	@Test
	public void testGetMappingValueValidPerpetualVid() throws Exception {
		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), anyMap(), anyList(), anyList(), any()))
				.thenReturn(responseWrapper);
		when(residentVidService.getPerpatualVid(Mockito.anyString())).thenReturn(Optional.of("4069341201794732"));
		responseWrapper.setErrors(null);
		when(residentConfigService.getUiSchemaFilteredInputAttributes(anyString()))
				.thenReturn(List.of("UIN", "email", "phone", "dateOfBirth", "perpetualVID"));
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		when(utility.getMappingJson()).thenReturn(mappingJson);
		String str = CryptoUtil.encodeToURLSafeBase64("response return".getBytes());
		IdentityDTO result = identityService.getIdentity("6", false, "eng");
		assertNotNull(result);
		assertEquals("8251649601", result.getUIN());
	}

	@Test(expected = ResidentServiceException.class)
	public void testGetMappingValueValidPerpetualVidResidentServiceCheckedException() throws Exception {
		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), anyMap(), anyList(), anyList(), any()))
				.thenReturn(responseWrapper);
		when(residentVidService.getPerpatualVid(Mockito.anyString())).thenThrow(new ResidentServiceCheckedException());
		responseWrapper.setErrors(null);
		when(residentConfigService.getUiSchemaFilteredInputAttributes(anyString()))
				.thenReturn(List.of("UIN", "email", "phone", "dateOfBirth", "perpetualVID"));
		IdentityDTO result = identityService.getIdentity("6", false, "eng");
		assertNotNull(result);
		assertEquals("6", result.getUIN());
	}

	@Test(expected = ResidentServiceException.class)
	public void testGetMappingValueValidPerpetualVidApisResourceAccessException() throws Exception {
		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), anyMap(), anyList(), anyList(), any()))
				.thenReturn(responseWrapper);
		when(residentVidService.getPerpatualVid(Mockito.anyString())).thenThrow(new ApisResourceAccessException());
		responseWrapper.setErrors(null);
		when(residentConfigService.getUiSchemaFilteredInputAttributes(anyString()))
				.thenReturn(List.of("UIN", "email", "phone", "dateOfBirth", "perpetualVID"));
		IdentityDTO result = identityService.getIdentity("6", false, "eng");
		assertNotNull(result);
		assertEquals("6", result.getUIN());
	}

	@Test
	public void testGetNameForNotification() throws IOException, ApisResourceAccessException {
		responseWrapper.setErrors(null);
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		when(utility.getMappingJson()).thenReturn(mappingJson);
		Map<String, String> identity = new HashMap<>();
		identity.put("name", "Kamesh");
		ReflectionTestUtils.invokeMethod(identityService, "getNameForNotification",
				identity, "eng");
	}

	@Test
	public void testGetUinForIndividualIdVId() throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
		Mockito.when(requestValidator.validateUin(Mockito.anyString())).thenReturn(false);
		Mockito.when(utilities.getUinByVid(Mockito.anyString())).thenReturn("2476302389");
		assertEquals("2476302389",identityService.getUinForIndividualId("2476302389"));
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetUinForIndividualIdVIdCreationException() throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
		Mockito.when(requestValidator.validateUin(Mockito.anyString())).thenReturn(false);
		Mockito.when(utilities.getUinByVid(Mockito.anyString())).thenThrow(new VidCreationException());
		assertEquals("2476302389",identityService.getUinForIndividualId("2476302389"));
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetUinForIndividualIdApisResourceAccessException() throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
		Mockito.when(requestValidator.validateUin(Mockito.anyString())).thenReturn(false);
		Mockito.when(utilities.getUinByVid(Mockito.anyString())).thenThrow(new ApisResourceAccessException());
		assertEquals("2476302389",identityService.getUinForIndividualId("2476302389"));
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetUinForIndividualIdIOException() throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
		Mockito.when(requestValidator.validateUin(Mockito.anyString())).thenReturn(false);
		Mockito.when(utilities.getUinByVid(Mockito.anyString())).thenThrow(new IOException());
		assertEquals("2476302389",identityService.getUinForIndividualId("2476302389"));
	}

	@Test
	public void testGetAuthUserDetails() {
		Authentication authentication= Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setToken(token);
		// test the case where the principal is an AuthUserDetails object
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, token);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		assertNotNull(ReflectionTestUtils.invokeMethod(identityService, "getAuthUserDetails"));
	}

	@Test
	public void testGetAuthUserDetailsPrincipalNull() {
		Authentication authentication= Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setToken(token);
		// test the case where the principal is an AuthUserDetails object
		assertNull(ReflectionTestUtils.invokeMethod(identityService, "getAuthUserDetails"));
	}

	@Test(expected = Exception.class)
	public void testGetResidentIndividualIdInvalidToken() throws ApisResourceAccessException, JsonProcessingException {
		String token = "1234";
		ReflectionTestUtils.setField(identityService, "usefInfoEndpointUrl", "http://localhost:8080/userinfo");
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("claim", "value");
		URI uri = URI.create("http://localhost:8080/userinfo");
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", "Bearer " + token);

		when(restClientWithPlainRestTemplate.getApi(uri, String.class, headers))
				.thenReturn(objectMapper.writeValueAsString(userInfo));

		when(restClientWithPlainRestTemplate.getApi(uri, String.class, headers))
				.thenReturn(objectMapper.writeValueAsString(userInfo));
		Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("true");
		Authentication authentication= Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setToken(token);
		// test the case where the principal is an AuthUserDetails object
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, token);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		assertEquals("3956038419",identityService.getResidentIndvidualId());
	}

	@Test(expected = Exception.class)
	public void testGetResidentIndividualIdValidToken() throws ApisResourceAccessException, JsonProcessingException {
		ImmutablePair<Boolean, AuthErrorCode> verifySignagure = new ImmutablePair<>(true, AuthErrorCode.UNAUTHORIZED);
		Mockito.when(tokenValidationHelper.verifyJWTSignagure(Mockito.any())).thenReturn(verifySignagure);
		ReflectionTestUtils.setField(identityService, "usefInfoEndpointUrl", "http://localhost:8080/userinfo");
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("claim", "value");
		Mockito.when(objectStoreHelper.decryptData(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn("Value");
		URI uri = URI.create("http://localhost:8080/userinfo");
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", "Bearer " + token);

		when(restClientWithPlainRestTemplate.getApi(uri, String.class, headers))
				.thenReturn(token);
		Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("true");
		Authentication authentication= Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setToken(token);
		// test the case where the principal is an AuthUserDetails object
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, token);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		assertEquals("3956038419",identityService.getResidentIndvidualId());
	}

	@Test(expected = Exception.class)
	public void testGetResidentIndividualIdValidTokenVerifySignatureFalse() throws ApisResourceAccessException, JsonProcessingException {
		ImmutablePair<Boolean, AuthErrorCode> verifySignagure = new ImmutablePair<>(false, AuthErrorCode.UNAUTHORIZED);
		Mockito.when(tokenValidationHelper.verifyJWTSignagure(Mockito.any())).thenReturn(verifySignagure);
		ReflectionTestUtils.setField(identityService, "usefInfoEndpointUrl", "http://localhost:8080/userinfo");
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("claim", "value");
		URI uri = URI.create("http://localhost:8080/userinfo");
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", "Bearer " + token);

		when(restClientWithPlainRestTemplate.getApi(uri, String.class, headers))
				.thenReturn(token);
		Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("true");
		Authentication authentication= Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setToken(token);
		// test the case where the principal is an AuthUserDetails object
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, token);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		assertEquals("3956038419",identityService.getResidentIndvidualId());
	}

	@Test
	public void testGetResidentIndividualIdValidTokenSucess() throws ApisResourceAccessException, JsonProcessingException {
		ReflectionTestUtils.setField(identityService, "usefInfoEndpointUrl", "http://localhost:8080/userinfo");
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("claim", "value");
		userInfo.put("individual_id", "3956038419");
		URI uri = URI.create("http://localhost:8080/userinfo");
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", "Bearer " + token);
		when(restClientWithPlainRestTemplate.getApi(uri, String.class, headers))
				.thenReturn(objectMapper.writeValueAsString(userInfo));
		Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("false");
		Authentication authentication= Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setToken(token);
		// test the case where the principal is an AuthUserDetails object
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, token);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		assertEquals("3956038419",identityService.getResidentIndvidualId());
	}

	@Test
	public void testGetResidentAuthenticationMode() throws ApisResourceAccessException, JsonProcessingException {
		ReflectionTestUtils.setField(identityService, "usefInfoEndpointUrl", "http://localhost:8080/userinfo");
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("claim", "value");
		userInfo.put("individual_id", "3956038419");
		URI uri = URI.create("http://localhost:8080/userinfo");
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", "Bearer " + token);
		Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("false");
		Authentication authentication= Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setToken(token);
		// test the case where the principal is an AuthUserDetails object
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, token);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		assertEquals("",ReflectionTestUtils.invokeMethod(identityService,
				"getResidentAuthenticationMode"));
	}

	@Test
	public void testGetClaimFromAccessToken() throws ApisResourceAccessException, JsonProcessingException {
		ReflectionTestUtils.setField(identityService, "usefInfoEndpointUrl", "http://localhost:8080/userinfo");
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("claim", "value");
		userInfo.put("individual_id", "3956038419");
		URI uri = URI.create("http://localhost:8080/userinfo");
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", "Bearer " + token);
		Authentication authentication= Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setToken(token);
		// test the case where the principal is an AuthUserDetails object
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, token);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		ReflectionTestUtils.invokeMethod(identityService,
				"getClaimFromAccessToken", "value");
	}

	@Test
	public void testGetIndividualIdTypeVidPassed(){
		Mockito.when(requestValidator.validateUin(Mockito.anyString())).thenReturn(false);
		Mockito.when(requestValidator.validateVid(Mockito.anyString())).thenReturn(true);
		assertEquals(IdType.VID.toString(), identityService.getIndividualIdType("2476302389"));
	}

	@Test
	public void testGetClaimValueFromJwtTokenFailed(){
		String claim = null;
		assertEquals("", ReflectionTestUtils.invokeMethod(identityService, "getClaimValueFromJwtToken",
				token, claim));
	}

	@Test(expected = Exception.class)
	public void testGetIndividualIdForAidFailed() throws Exception{
		String aid = "123456789";
		Mockito.when(residentVidService.getPerpatualVid(Mockito.anyString())).thenReturn(Optional.empty());
		ReflectionTestUtils.setField(identityService, "dateFormat", "yyyy/MM/dd");
		ReflectionTestUtils.setField(identityService, "useVidOnly", true);
		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), anyMap(), anyList(), anyList(), any()))
				.thenReturn(responseWrapper);
		responseWrapper.setErrors(null);
		when(residentConfigService.getUiSchemaFilteredInputAttributes(anyString()))
				.thenReturn(List.of("UIN", "email", "phone", "dateOfBirth", "firstName", "middleName", "lastName"));
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		when(utility.getMappingJson()).thenReturn(mappingJson);
		String result = ReflectionTestUtils.invokeMethod(identityService, "getIndividualIdForAid", aid);
		assertEquals("123456789", result);
	}


}
