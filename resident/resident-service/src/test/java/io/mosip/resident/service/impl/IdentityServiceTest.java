package io.mosip.resident.service.impl;

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
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.exception.VidCreationException;
import io.mosip.resident.handler.service.ResidentConfigService;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.IdentityService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.util.Utility;
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
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
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

@RunWith(MockitoJUnitRunner.Silent.class)
@RefreshScope
@ContextConfiguration
public class IdentityServiceTest {
	
	@InjectMocks
	private IdentityService identityService = new IdentityServiceImpl();

	@Mock
	private AuditUtil auditUtil;

	@Mock
	private Utility utility;

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
	private ObjectStoreHelper objectStoreHelper;
	
	@Mock
	private Utilities utilities;

	@Mock
	private ValidateTokenUtil tokenValidationHelper;

	private ResponseWrapper responseWrapper;

	private Map responseMap;

	private Map bdbFaceMap;

	private ObjectMapper objectMapper = new ObjectMapper();

	private static String token;
	
	
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
		responseWrapper.setErrors(null);
		
		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), anyMap(), anyList(), anyList(), any()))
		.thenReturn(responseWrapper);
		when(residentConfigService.getUiSchemaFilteredInputAttributes(anyString()))
		.thenReturn(List.of("UIN", "email", "phone", "dateOfBirth", "firstName", "middleName", "lastName", "perpetualVID"));
		
		Mockito.when(requestValidator.validateUin(Mockito.anyString())).thenReturn(true);
		Mockito.when(requestValidator.validateRid(Mockito.anyString())).thenReturn(true);
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
		when(env.getProperty("mosip.resident.photo.attribute.name")).thenReturn("photo");
		when(env.getProperty("resident.additional.identity.attribute.to.fetch")).thenReturn("UIN,email,phone,dateOfBirth,fullName");
		when(env.getProperty("mosip.resident.photo.token.claim-photo")).thenReturn("picture");
	}

	private void fileLoadMethod() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		when(utility.getMappingJson()).thenReturn(mappingJson);
	}

	@Test
	public void testGetIdentityLangCodeNull() throws Exception {
		getAuthUserDetailsFromAuthentication();
		Tuple3<URI, MultiValueMap<String, String>, Map<String, Object>> tuple3 = loadUserInfoMethod();
		tuple3.getT3().put("photo", "NGFjNzk1OTYyYWRkIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJ");
		when(restClientWithPlainRestTemplate.getApi(tuple3.getT1(), String.class, tuple3.getT2()))
				.thenReturn(objectMapper.writeValueAsString(tuple3.getT3()));
		fileLoadMethod();
		IdentityDTO result = identityService.getIdentity("6");
		assertNotNull(result);
		assertEquals("8251649601", result.getUIN());
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetIdentityAttributesIf() throws Exception {
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
		Tuple3<URI, MultiValueMap<String, String>, Map<String, Object>> tuple3 = loadUserInfoMethod();
		tuple3.getT3().put("photo", "NGFjNzk1OTYyYWRkIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJ");
		when(restClientWithPlainRestTemplate.getApi(tuple3.getT1(), String.class, tuple3.getT2()))
				.thenReturn(objectMapper.writeValueAsString(tuple3.getT3()));
		identityService.getIdentity("6");
	}

	@Test
	public void testGetUinForIndividualId() throws Exception{
		String id = "123456789";
		fileLoadMethod();
		String result = identityService.getUinForIndividualId(id);
		assertEquals("123456789", result);
	}

	@Test
	public void testGetIDATokenForIndividualId() throws Exception{
		String id = "123456789";
		String token = "1234";
		ReflectionTestUtils.setField(identityService, "onlineVerificationPartnerId", "m-partner-default-auth");
		when(tokenIDGenerator.generateTokenID(anyString(), anyString())).thenReturn(token);
		fileLoadMethod();
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
	
	private Tuple3<URI, MultiValueMap<String, String>, Map<String, Object>> loadUserInfoMethod() throws Exception {
		ReflectionTestUtils.setField(identityService, "usefInfoEndpointUrl", "http://localhost:8080/userinfo");
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("claim", "value");
		URI uri = URI.create("http://localhost:8080/userinfo");
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", "Bearer " + token);
		return Tuples.of(uri, headers, userInfo);
	}

	@Test
	public void testGetUserInfoSuccess() throws Exception {
		Tuple3<URI, MultiValueMap<String, String>, Map<String, Object>> tuple3 = loadUserInfoMethod();
		when(restClientWithPlainRestTemplate.getApi(tuple3.getT1(), String.class, tuple3.getT2()))
				.thenReturn(objectMapper.writeValueAsString(tuple3.getT3()));
		Map<String, Object> result = ReflectionTestUtils.invokeMethod(identityService, "getUserInfo", token);
		assertEquals("value", result.get("claim"));
	}

	@Test
	public void testGetIndividualIdForAid() throws Exception{
		Tuple3<URI, MultiValueMap<String, String>, Map<String, Object>> tuple3 = loadUserInfoMethod();
		tuple3.getT3().put("photo", "NGFjNzk1OTYyYWRkIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJ");
		when(restClientWithPlainRestTemplate.getApi(tuple3.getT1(), String.class, tuple3.getT2()))
				.thenReturn(objectMapper.writeValueAsString(tuple3.getT3()));
		String aid = "123456789";
		fileLoadMethod();
		String result = ReflectionTestUtils.invokeMethod(identityService, "getIndividualIdForAid", aid);
		assertEquals("8251649601", result);
	}

	@Test
	public void testGetIndividualIdForAidUseVidOnlyTrue() throws Exception{
		Tuple3<URI, MultiValueMap<String, String>, Map<String, Object>> tuple3 = loadUserInfoMethod();
		tuple3.getT3().put("photo", "NGFjNzk1OTYyYWRkIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJ");
		when(restClientWithPlainRestTemplate.getApi(tuple3.getT1(), String.class, tuple3.getT2()))
				.thenReturn(objectMapper.writeValueAsString(tuple3.getT3()));
		String aid = "123456789";
		Optional<String> perpVid = Optional.of("8251649601");
		when(residentVidService.getPerpatualVid(anyString())).thenReturn(perpVid);
		ReflectionTestUtils.setField(identityService,"useVidOnly", true);
		fileLoadMethod();
		String result = ReflectionTestUtils.invokeMethod(identityService, "getIndividualIdForAid", aid);
		assertEquals("8251649601", result);
	}

	@Test
	public void testGetIndividualIdTypeUin(){
		assertEquals(IdType.UIN.toString(), identityService.getIndividualIdType("2476302389"));
	}

	@Test
	public void testGetIndividualIdTypeVid(){
		assertEquals(IdType.UIN.toString(), identityService.getIndividualIdType("2476302389"));
	}

	@Test
	public void testDecryptPayload(){
		Mockito.when(env.getProperty(Mockito.anyString())).thenReturn("RESIDENT");
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
		assertEquals("2476302389",identityService.getUinForIndividualId("2476302389"));
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetMappingValueNullIoException() throws Exception {
		Tuple3<URI, MultiValueMap<String, String>, Map<String, Object>> tuple3 = loadUserInfoMethod();
		tuple3.getT3().put("photo", "NGFjNzk1OTYyYWRkIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJ");
		when(restClientWithPlainRestTemplate.getApi(tuple3.getT1(), String.class, tuple3.getT2()))
				.thenReturn(objectMapper.writeValueAsString(tuple3.getT3()));
		String mappingJson = "mappingJson";
		when(utility.getMappingJson()).thenReturn(mappingJson);
		IdentityDTO result = identityService.getIdentity("6", false, "eng");
		assertNotNull(result);
		assertEquals("6", result.getUIN());
	}

	@Test
	public void testGetMappingValueInvalidPerpetualVid() throws Exception {
		Tuple3<URI, MultiValueMap<String, String>, Map<String, Object>> tuple3 = loadUserInfoMethod();
		tuple3.getT3().put("photo", "NGFjNzk1OTYyYWRkIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJ");
		when(restClientWithPlainRestTemplate.getApi(tuple3.getT1(), String.class, tuple3.getT2()))
				.thenReturn(objectMapper.writeValueAsString(tuple3.getT3()));
		fileLoadMethod();
		IdentityDTO result = identityService.getIdentity("6", false, "eng");
		assertNotNull(result);
		assertEquals("8251649601", result.getUIN());
	}

	@Test
	public void testGetMappingValueValidPerpetualVid() throws Exception {
		Tuple3<URI, MultiValueMap<String, String>, Map<String, Object>> tuple3 = loadUserInfoMethod();
		tuple3.getT3().put("photo", "NGFjNzk1OTYyYWRkIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJ");
		when(restClientWithPlainRestTemplate.getApi(tuple3.getT1(), String.class, tuple3.getT2()))
				.thenReturn(objectMapper.writeValueAsString(tuple3.getT3()));
		when(residentVidService.getPerpatualVid(Mockito.anyString())).thenReturn(Optional.of("4069341201794732"));
		fileLoadMethod();
		String str = CryptoUtil.encodeToURLSafeBase64("response return".getBytes());
		IdentityDTO result = identityService.getIdentity("6", false, "eng");
		assertNotNull(result);
		assertEquals("8251649601", result.getUIN());
	}

	@Test(expected = ResidentServiceException.class)
	public void testGetMappingValueValidPerpetualVidResidentServiceCheckedException() throws Exception {
		fileLoadMethod();
		when(env.getProperty("resident.additional.identity.attribute.to.fetch")).thenReturn("UIN,email,phone,dateOfBirth,fullName,perpetualVID");
		when(residentVidService.getPerpatualVid(Mockito.anyString())).thenThrow(new ResidentServiceCheckedException());
		IdentityDTO result = identityService.getIdentity("6", false, "eng");
		assertNotNull(result);
		assertEquals("6", result.getUIN());
	}

	@Test(expected = ResidentServiceException.class)
	public void testGetMappingValueValidPerpetualVidApisResourceAccessException() throws Exception {
		when(residentVidService.getPerpatualVid(Mockito.anyString())).thenThrow(new ApisResourceAccessException());
		when(env.getProperty("resident.additional.identity.attribute.to.fetch")).thenReturn("UIN,email,phone,dateOfBirth,fullName,perpetualVID");
		fileLoadMethod();
		IdentityDTO result = identityService.getIdentity("6", false, "eng");
		assertNotNull(result);
		assertEquals("6", result.getUIN());
	}

	@Test
	public void testGetNameForNotification() throws Exception {
		fileLoadMethod();
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
	
	public static void getAuthUserDetailsFromAuthentication() {
		Authentication authentication= Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setToken(token);
		// test the case where the principal is an AuthUserDetails object
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, token);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
	}

	@Test
	public void testGetAuthUserDetails() {
		getAuthUserDetailsFromAuthentication();
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
	public void testGetResidentIndividualIdInvalidToken() throws Exception {
		Tuple3<URI, MultiValueMap<String, String>, Map<String, Object>> tuple3 = loadUserInfoMethod();

		when(restClientWithPlainRestTemplate.getApi(tuple3.getT1(), String.class, tuple3.getT2()))
				.thenReturn(objectMapper.writeValueAsString(tuple3.getT3()));
		getAuthUserDetailsFromAuthentication();
		assertEquals("3956038419",identityService.getResidentIndvidualIdFromSession());
	}

	@Test(expected = Exception.class)
	public void testGetResidentIndividualIdValidToken() throws Exception {
		ImmutablePair<Boolean, AuthErrorCode> verifySignagure = new ImmutablePair<>(true, AuthErrorCode.UNAUTHORIZED);
		//Mockito.when(tokenValidationHelper.verifyJWTSignagure(Mockito.any())).thenReturn(verifySignagure);
		//Mockito.when(objectStoreHelper.decryptData(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn("Value");
		Tuple3<URI, MultiValueMap<String, String>, Map<String, Object>> tuple3 = loadUserInfoMethod();

		when(restClientWithPlainRestTemplate.getApi(tuple3.getT1(), String.class, tuple3.getT2()))
				.thenReturn(token);
		getAuthUserDetailsFromAuthentication();
		assertEquals("3956038419",identityService.getResidentIndvidualIdFromSession());
	}

	@Test(expected = Exception.class)
	public void testGetResidentIndividualIdValidTokenVerifySignatureFalse() throws Exception {
		ImmutablePair<Boolean, AuthErrorCode> verifySignagure = new ImmutablePair<>(false, AuthErrorCode.UNAUTHORIZED);
		//Mockito.when(tokenValidationHelper.verifyJWTSignagure(Mockito.any())).thenReturn(verifySignagure);
		Tuple3<URI, MultiValueMap<String, String>, Map<String, Object>> tuple3 = loadUserInfoMethod();

		when(restClientWithPlainRestTemplate.getApi(tuple3.getT1(), String.class, tuple3.getT2()))
				.thenReturn(token);
		getAuthUserDetailsFromAuthentication();
		assertEquals("3956038419",identityService.getResidentIndvidualIdFromSession());
	}

	@Test
	public void testGetResidentIndividualIdValidTokenSucess() throws Exception {
		Tuple3<URI, MultiValueMap<String, String>, Map<String, Object>> tuple3 = loadUserInfoMethod();
		tuple3.getT3().put("individual_id", "3956038419");
		when(restClientWithPlainRestTemplate.getApi(tuple3.getT1(), String.class, tuple3.getT2()))
				.thenReturn(objectMapper.writeValueAsString(tuple3.getT3()));
		getAuthUserDetailsFromAuthentication();
		assertEquals("3956038419",identityService.getResidentIndvidualIdFromSession());
	}

	@Test
	public void testGetResidentAuthenticationMode() throws Exception {
		Tuple3<URI, MultiValueMap<String, String>, Map<String, Object>> tuple3 = loadUserInfoMethod();
		String authTypeCode = "OTP";
		Authentication authentication= Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		
		when(utility.getAuthTypeCodefromkey(Mockito.any())).thenReturn(authTypeCode);
		tuple3.getT3().put("individual_id", "3956038419");
		getAuthUserDetailsFromAuthentication();
		assertEquals("OTP",ReflectionTestUtils.invokeMethod(identityService,
				"getResidentAuthenticationMode"));
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
		ReflectionTestUtils.setField(identityService, "useVidOnly", true);
		fileLoadMethod();
		String result = ReflectionTestUtils.invokeMethod(identityService, "getIndividualIdForAid", aid);
		assertEquals("123456789", result);
	}

	@Test
	public void testGetMappingValueFetchFaceTrue() throws Exception {
		Tuple3<URI, MultiValueMap<String, String>, Map<String, Object>> tuple3 = loadUserInfoMethod();
		tuple3.getT3().put("picture", "3956038419");
		when(restClientWithPlainRestTemplate.getApi(tuple3.getT1(), String.class, tuple3.getT2()))
				.thenReturn(objectMapper.writeValueAsString(tuple3.getT3()));
		getAuthUserDetailsFromAuthentication();
		tuple3.getT3().put("photo", "NGFjNzk1OTYyYWRkIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJ");
		when(restClientWithPlainRestTemplate.getApi(tuple3.getT1(), String.class, tuple3.getT2()))
				.thenReturn(objectMapper.writeValueAsString(tuple3.getT3()));
		when(residentVidService.getPerpatualVid(Mockito.anyString())).thenReturn(Optional.of("4069341201794732"));
		fileLoadMethod();
		IdentityDTO result = identityService.getIdentity("6", true, "eng");
		assertNotNull(result);
		assertEquals("8251649601", result.getUIN());
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetMappingValueFetchFaceTrueFailed() throws Exception {
		Tuple3<URI, MultiValueMap<String, String>, Map<String, Object>> tuple3 = loadUserInfoMethod();
		tuple3.getT3().put("picture", "3956038419");
		when(restClientWithPlainRestTemplate.getApi(tuple3.getT1(), String.class, tuple3.getT2()))
				.thenReturn(objectMapper.writeValueAsString(tuple3.getT3()));
		getAuthUserDetailsFromAuthentication();
		tuple3.getT3().put("photo", "NGFjNzk1OTYyYWRkIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJ");
		when(restClientWithPlainRestTemplate.getApi(tuple3.getT1(), String.class, tuple3.getT2()))
				.thenThrow(new ApisResourceAccessException());
		when(residentVidService.getPerpatualVid(Mockito.anyString())).thenReturn(Optional.of("4069341201794732"));
		fileLoadMethod();
		IdentityDTO result = identityService.getIdentity("6", true, "eng");
		assertNotNull(result);
		assertEquals("8251649601", result.getUIN());
	}

	@Test
	public void testGetIdentityAttributes() throws Exception {
		Tuple3<URI, MultiValueMap<String, String>, Map<String, Object>> tuple3 = loadUserInfoMethod();
		tuple3.getT3().put("picture", "NGFjNzk1OTYyYWRkIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJ");
		when(restClientWithPlainRestTemplate.getApi(tuple3.getT1(), String.class, tuple3.getT2()))
				.thenReturn(objectMapper.writeValueAsString(tuple3.getT3()));
		Mockito.when(residentVidService.getPerpatualVid(Mockito.anyString())).thenReturn(Optional.of("1212121212"));
		assertEquals("8251649601",
				identityService.getIdentityAttributes("4578987854", "personalized-card", List.of("Name")).get("UIN"));
	}

	@Test(expected = Exception.class)
	public void testGetIdentityAttributesWithSecureSessionFailed() throws Exception {
		when(residentConfigService.getUiSchemaFilteredInputAttributes(anyString()))
				.thenReturn(List.of("UIN", "email", "phone", "dateOfBirth", "firstName", "middleName", "lastName", "perpetualVID", "photo", ResidentConstants.MASK_PREFIX+"UIN"));
		getAuthUserDetailsFromAuthentication();
		Tuple3<URI, MultiValueMap<String, String>, Map<String, Object>> tuple3 = loadUserInfoMethod();
		tuple3.getT3().put("photo", "NGFjNzk1OTYyYWRkIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJ");
		Mockito.when(residentVidService.getPerpatualVid(Mockito.anyString())).thenReturn(Optional.of("1212121212"));
		assertEquals("8251649601",
				identityService.getIdentityAttributes("4578987854", "personalized-card", List.of("Name")).get("UIN"));
	}

	@Test
	public void testGetIdentityAttributesWithSecureSession() throws Exception {
		when(residentConfigService.getUiSchemaFilteredInputAttributes(anyString()))
				.thenReturn(List.of("UIN", "email", "phone", "dateOfBirth", "firstName", "middleName", "lastName", "perpetualVID", "photo", ResidentConstants.MASK_PREFIX+"UIN"));
		getAuthUserDetailsFromAuthentication();
		Tuple3<URI, MultiValueMap<String, String>, Map<String, Object>> tuple3 = loadUserInfoMethod();
		tuple3.getT3().put("picture", "NGFjNzk1OTYyYWRkIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJ");
		Mockito.when(residentVidService.getPerpatualVid(Mockito.anyString())).thenReturn(Optional.of("1212121212"));
		when(restClientWithPlainRestTemplate.getApi(tuple3.getT1(), String.class, tuple3.getT2()))
				.thenReturn(objectMapper.writeValueAsString(tuple3.getT3()));
		Mockito.when(utility.convertToMaskData(Mockito.anyString())).thenReturn("81***23");
		assertEquals("8251649601",
				identityService.getIdentityAttributes("4578987854", "personalized-card", List.of("Name")).get("UIN"));
	}

	@Test(expected = InvalidInputException.class)
	public void testGetIndividualIdType(){
		Mockito.when(requestValidator.validateUin(Mockito.anyString())).thenReturn(false);
		Mockito.when(requestValidator.validateRid(Mockito.anyString())).thenReturn(false);
		identityService.getIndividualIdType("3434343343");
	}

	@Test
	public void testCreateSessionId(){
		Mockito.when(utility.createEventId()).thenReturn("123");
		assertEquals("123", identityService.createSessionId());
	}

	@Test
	public void testGetResidentIdaTokenFromAccessToken() throws Exception {
		when(env.getProperty(Mockito.anyString())).thenReturn("individual_id");
		ReflectionTestUtils.setField(identityService, "onlineVerificationPartnerId", "m-partner-default-auth");
		Tuple3<URI, MultiValueMap<String, String>, Map<String, Object>> tuple3 = loadUserInfoMethod();
		tuple3.getT3().put("individual_id", "4343434343");
		when(restClientWithPlainRestTemplate.getApi(tuple3.getT1(), String.class, tuple3.getT2()))
				.thenReturn(objectMapper.writeValueAsString(tuple3.getT3()));
		when(tokenIDGenerator.generateTokenID(anyString(), anyString())).thenReturn(token);
		assertEquals(token, identityService.getResidentIdaTokenFromAccessToken(token));
	}

	@Test(expected = ResidentServiceException.class)
	public void testGetResidentIdaTokenFromAccessTokenNullIndividualId() throws Exception {
		when(env.getProperty(Mockito.anyString())).thenReturn("individual_id");
		ReflectionTestUtils.setField(identityService, "onlineVerificationPartnerId", "m-partner-default-auth");
		Tuple3<URI, MultiValueMap<String, String>, Map<String, Object>> tuple3 = loadUserInfoMethod();
		tuple3.getT3().put("individual_id", null);
		when(restClientWithPlainRestTemplate.getApi(tuple3.getT1(), String.class, tuple3.getT2()))
				.thenReturn(objectMapper.writeValueAsString(tuple3.getT3()));
		when(tokenIDGenerator.generateTokenID(anyString(), anyString())).thenReturn(token);
		assertEquals(token, identityService.getResidentIdaTokenFromAccessToken(token));
	}

	@Test
	public void testDecodeAndDecryptUserInfo(){
		Mockito.when(env.getProperty(ResidentConstants.MOSIP_OIDC_JWT_SIGNED)).thenReturn(String.valueOf(true));
		Mockito.when(env.getProperty(ResidentConstants.MOSIP_OIDC_JWT_VERIFY_ENABLED)).thenReturn(String.valueOf(true));
		AuthErrorCode authErrorCode = null;
		ImmutablePair<Boolean, AuthErrorCode> verifySignature = new ImmutablePair<>(true, authErrorCode);
		Mockito.when(tokenValidationHelper
				.verifyJWTSignagure(Mockito.any())).thenReturn(verifySignature);
		ReflectionTestUtils.invokeMethod(identityService, "decodeAndDecryptUserInfo", token);
	}

	@Test
	public void testDecodeAndDecryptUserInfoOidcJwtDisabled(){
		Mockito.when(env.getProperty(ResidentConstants.MOSIP_OIDC_JWT_SIGNED)).thenReturn(String.valueOf(true));
		Mockito.when(env.getProperty(ResidentConstants.MOSIP_OIDC_JWT_VERIFY_ENABLED)).thenReturn(String.valueOf(false));
		AuthErrorCode authErrorCode = null;
		ImmutablePair<Boolean, AuthErrorCode> verifySignature = new ImmutablePair<>(true, authErrorCode);
		Mockito.when(tokenValidationHelper
				.verifyJWTSignagure(Mockito.any())).thenReturn(verifySignature);
		ReflectionTestUtils.invokeMethod(identityService, "decodeAndDecryptUserInfo", token);
	}

	@Test(expected = ResidentServiceException.class)
	public void testDecodeAndDecryptUserInfoOidcJwtDisabledFailure(){
		Mockito.when(env.getProperty(ResidentConstants.MOSIP_OIDC_JWT_SIGNED)).thenReturn(String.valueOf(true));
		Mockito.when(env.getProperty(ResidentConstants.MOSIP_OIDC_JWT_VERIFY_ENABLED)).thenReturn(String.valueOf(true));
		AuthErrorCode authErrorCode = AuthErrorCode.FORBIDDEN;
		ImmutablePair<Boolean, AuthErrorCode> verifySignature = new ImmutablePair<>(false, authErrorCode);
		Mockito.when(tokenValidationHelper
				.verifyJWTSignagure(Mockito.any())).thenReturn(verifySignature);
		ReflectionTestUtils.invokeMethod(identityService, "decodeAndDecryptUserInfo", token);
	}

	@Test(expected = Exception.class)
	public void testDecodeAndDecryptUserInfoOidcEncryptionEnabled(){
		Mockito.when(env.getProperty(ResidentConstants.MOSIP_OIDC_JWT_SIGNED)).thenReturn(String.valueOf(false));
		Mockito.when(env.getProperty(ResidentConstants.MOSIP_OIDC_JWT_VERIFY_ENABLED)).thenReturn(String.valueOf(false));
		Mockito.when(env.getProperty(ResidentConstants.MOSIP_OIDC_ENCRYPTION_ENABLED)).thenReturn(String.valueOf(true));
		Mockito.when(objectStoreHelper.decryptData(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(Arrays.toString(Base64.getEncoder().encode("payload".getBytes())));
		AuthErrorCode authErrorCode = null;
		ImmutablePair<Boolean, AuthErrorCode> verifySignature = new ImmutablePair<>(true, authErrorCode);
		Mockito.when(tokenValidationHelper
				.verifyJWTSignagure(Mockito.any())).thenReturn(verifySignature);
		ReflectionTestUtils.invokeMethod(identityService, "decodeAndDecryptUserInfo", token);
	}

}
