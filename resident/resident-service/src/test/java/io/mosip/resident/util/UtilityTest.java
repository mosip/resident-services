package io.mosip.resident.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.authcodeflowproxy.api.validator.ValidateTokenUtil;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.pdfgenerator.spi.PDFGenerator;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.kernel.openid.bridge.api.constants.AuthErrorCode;
import io.mosip.kernel.signature.dto.SignatureResponseDto;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.IdRepoResponseDto;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.dto.RegistrationCenterDto;
import io.mosip.resident.dto.RegistrationCenterResponseDto;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.IdRepoAppException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.service.ProxyPartnerManagementService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.mosip.resident.constant.RegistrationConstants.DATETIME_PATTERN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PrepareForTest({ JsonUtil.class })
public class UtilityTest {
	private static final String LOCALE = "en-US";

	@Mock
	private ResidentServiceRestClient residentServiceRestClient;

	@InjectMocks
	private Utility utility;

	private JSONObject identity;

	@Mock
	private Environment env;

	@Mock
	private IdentityServiceImpl identityService;
	
	@Mock
	private HttpServletRequest request;

	@Mock
	private PDFGenerator pdfGenerator;

	@Mock
	private ResidentTransactionRepository residentTransactionRepository;
	
	@Mock
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate residentRestTemplate;
	
	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private Utilities utilities;

	@Mock
	private ProxyMasterdataService proxyMasterdataService;

	@Mock
	private ValidateTokenUtil tokenValidationHelper;

	@Mock
	private ProxyPartnerManagementService proxyPartnerManagementService;

	private ObjectMapper mapper = new ObjectMapper();

	private String replaceSplChars = "{\" \": \"_\", \",\" : \"\", \":\" : \".\"}";
	private static String token;
	@Mock
	private ObjectStoreHelper objectStoreHelper;
	private String idaToken;


	@Before
	public void setUp() throws IOException, ApisResourceAccessException {
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("ID.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String idJsonString = IOUtils.toString(is, "UTF-8");
		identity = JsonUtil.readValue(idJsonString, JSONObject.class);
		
		ReflectionTestUtils.setField(utility, "mapper", mapper);
		ReflectionTestUtils.setField(utility, "configServerFileStorageURL", "url");
		ReflectionTestUtils.setField(utility, "residentIdentityJson", "json");
		ReflectionTestUtils.setField(utility, "formattingStyle", FormatStyle.MEDIUM.name());
		ReflectionTestUtils.setField(utility, "specialCharsReplacementMap", mapper.readValue(replaceSplChars, Map.class));
        when(env.getProperty("resident.ui.datetime.pattern.default")).thenReturn("yyyy-MM-dd");
        when(env.getProperty("resident.filename.datetime.pattern.default")).thenReturn("yyyy-MM-dd hh:mm:ss a");
		request = Mockito.mock(HttpServletRequest.class);
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
		idaToken = "2186705746";
	}

	@Test
	public void retrieveIdrepoJsonSuccessTest() throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<IdRepoResponseDto> response = new ResponseWrapper<>();
		IdRepoResponseDto idRepoResponseDto = new IdRepoResponseDto();
		idRepoResponseDto.setStatus("Activated");
		idRepoResponseDto.setIdentity(JsonUtil.getJSONObject(identity, "identity"));
		response.setResponse(idRepoResponseDto);
		Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
				any(), any(Class.class))).thenReturn(response);
		// UIN
		JSONObject identityJsonObj = utility.retrieveIdrepoJson("3527812406");
		assertEquals(identityJsonObj.get("UIN"), JsonUtil.getJSONObject(identity, "identity").get("UIN"));
		// RID
		JSONObject jsonUsingRID = utility.retrieveIdrepoJson("10008200070004420191203104356");
		assertEquals(jsonUsingRID.get("UIN"), JsonUtil.getJSONObject(identity, "identity").get("UIN"));

	}

	@Test
	public void testRetrieveVidSuccess() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResponseWrapper<IdRepoResponseDto> response = new ResponseWrapper<>();
		IdRepoResponseDto idRepoResponseDto = new IdRepoResponseDto();
		idRepoResponseDto.setStatus("Activated");
		idRepoResponseDto.setIdentity(JsonUtil.getJSONObject(identity, "identity"));
		response.setResponse(idRepoResponseDto);

		Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
				any(), any(Class.class))).thenReturn(response);
		JSONObject jsonUsingVID = utility.retrieveIdrepoJson("5628965106742572");
		assertEquals(jsonUsingVID.get("UIN"), JsonUtil.getJSONObject(identity, "identity").get("UIN"));
	}

	@Test(expected = IdRepoAppException.class)
	public void testRetrieveIdrepoJsonError() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResponseWrapper<IdRepoResponseDto> response = new ResponseWrapper<>();
		response.setErrors(List.of(new ServiceError("error code", "error msg")));

		Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
				any(), any(Class.class))).thenReturn(response);
		utility.retrieveIdrepoJson("5628965106742572");
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void retrieveIdrepoJsonClientError() throws ApisResourceAccessException, ResidentServiceCheckedException {
		HttpClientErrorException clientExp = new HttpClientErrorException(HttpStatus.BAD_GATEWAY);
		ApisResourceAccessException apiResourceAccessExp = new ApisResourceAccessException("BadGateway", clientExp);
        Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
                any(), any(Class.class))).thenThrow(apiResourceAccessExp);
		utility.retrieveIdrepoJson("3527812406");

	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void retrieveIdrepoJsonServerError() throws ApisResourceAccessException, ResidentServiceCheckedException {
		HttpServerErrorException serverExp = new HttpServerErrorException(HttpStatus.BAD_GATEWAY);
		ApisResourceAccessException apiResourceAccessExp = new ApisResourceAccessException("BadGateway", serverExp);
        Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
                any(), any(Class.class))).thenThrow(apiResourceAccessExp);
		utility.retrieveIdrepoJson("3527812406");

	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void retrieveIdrepoJsonUnknownException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		ApisResourceAccessException apiResourceAccessExp = new ApisResourceAccessException("BadGateway",
				new RuntimeException());
        Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
                any(), any(Class.class))).thenThrow(apiResourceAccessExp);
		utility.retrieveIdrepoJson("3527812406");

	}

	@Test(expected = IdRepoAppException.class)
	public void testIdRepoAppException() throws ApisResourceAccessException, ResidentServiceCheckedException {
        Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
                any(), any(Class.class))).thenReturn(null);
		utility.retrieveIdrepoJson("3527812406");

	}

	@Test(expected = IdRepoAppException.class)
	public void vidResponseNull() throws ApisResourceAccessException, ResidentServiceCheckedException {
		List<String> pathsegments = new ArrayList<>();
		pathsegments.add("5628965106742572");
        Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
                any(), any(Class.class))).thenReturn(null);
		utility.retrieveIdrepoJson("5628965106742572");

	}

	@Test
	public void testGetMailingAttributes() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		Utility utilitySpy = Mockito.spy(utility);
		Mockito.doReturn(mappingJson).when(utilitySpy).getMappingJson();
		JSONObject mapperJson = JsonUtil.readValue(mappingJson, JSONObject.class);
		Map mapperIdentity = (Map) mapperJson.get("identity");

		ResponseWrapper<IdRepoResponseDto> response = new ResponseWrapper<>();
		IdRepoResponseDto idRepoResponseDto = new IdRepoResponseDto();
		idRepoResponseDto.setStatus("Activated");
		JSONObject identityJson = JsonUtil.getJSONObject(identity, "identity");
		idRepoResponseDto.setIdentity(identityJson);
		response.setResponse(idRepoResponseDto);
		Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
				any(), any(Class.class))).thenReturn(response);

		Map<String, Object> attributes = utilitySpy.getMailingAttributes("3527812406", new HashSet<String>(), identityJson, mapperIdentity);
		assertEquals("user@mail.com", attributes.get("email"));
		Map<String, Object> attributes1 = utilitySpy.getMailingAttributes("3527812406", new HashSet<String>(), identityJson, mapperIdentity);
		assertEquals("user@mail.com", attributes1.get("email"));

	}

	@Test(expected = ResidentServiceException.class)
	public void testGetMailingAttributesIdNull() throws Exception {
		utility.getMailingAttributes(null, new HashSet<String>(), Map.of(), Map.of());
	}
	
	@Test(expected = ResidentServiceException.class)
	public void testGetMailingAttributesIdEmpty() throws Exception {
		utility.getMailingAttributes("", new HashSet<String>(), Map.of(), Map.of());
	}

	@Test
	public void testGetMappingJsonEmpty() throws Exception {
		ReflectionTestUtils.setField(utility, "regProcessorIdentityJson", "");
		utility.getMappingJson();
	}

	@Test
	public void testGetPreferredLanguage() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		Utility utilitySpy = Mockito.spy(utility);
		Mockito.doReturn(mappingJson).when(utilitySpy).getMappingJson();
		JSONObject mapperJson = JsonUtil.readValue(mappingJson, JSONObject.class);
		Map mapperIdentity = (Map) mapperJson.get("identity");

		ResponseWrapper<IdRepoResponseDto> response = new ResponseWrapper<>();
		IdRepoResponseDto idRepoResponseDto = new IdRepoResponseDto();
		idRepoResponseDto.setStatus("Activated");
		JSONObject identityJson = JsonUtil.getJSONObject(identity, "identity");
		idRepoResponseDto.setIdentity(identityJson);
		response.setResponse(idRepoResponseDto);
		Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
				any(), any(Class.class))).thenReturn(response);

		Mockito.doReturn("preferredLang").when(env).getProperty("mosip.default.user-preferred-language-attribute");
		Map<String, Object> attributes = utilitySpy.getMailingAttributes("3527812406", new HashSet<String>(), identityJson, mapperIdentity);
		assertEquals("eng", attributes.get("preferredLang"));
	}

	@Test
	public void testGetDefaultTemplateLanguages() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		Utility utilitySpy = Mockito.spy(utility);
		Mockito.doReturn(mappingJson).when(utilitySpy).getMappingJson();
		JSONObject mapperJson = JsonUtil.readValue(mappingJson, JSONObject.class);
		Map mapperIdentity = (Map) mapperJson.get("identity");

		ResponseWrapper<IdRepoResponseDto> response = new ResponseWrapper<>();
		IdRepoResponseDto idRepoResponseDto = new IdRepoResponseDto();
		idRepoResponseDto.setStatus("Activated");
		JSONObject identityJson = JsonUtil.getJSONObject(identity, "identity");
		idRepoResponseDto.setIdentity(identityJson);
		response.setResponse(idRepoResponseDto);
		response.setResponse(idRepoResponseDto);
		Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
				any(), any(Class.class))).thenReturn(response);

		Mockito.doReturn("preferredLang").when(env).getProperty("mosip.default.template-languages");
		Map<String, Object> attributes = utilitySpy.getMailingAttributes("3527812406", new HashSet<String>(), identityJson, mapperIdentity);
		assertEquals("eng", attributes.get("preferredLang"));
	}

	@Test
	public void testGetDataCapturedLanguages() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		Utility utilitySpy = Mockito.spy(utility);
		Mockito.doReturn(mappingJson).when(utilitySpy).getMappingJson();
		JSONObject mapperJson = JsonUtil.readValue(mappingJson, JSONObject.class);
		Map mapperIdentity = (Map) mapperJson.get("identity");

		ResponseWrapper<IdRepoResponseDto> response = new ResponseWrapper<>();
		IdRepoResponseDto idRepoResponseDto = new IdRepoResponseDto();
		idRepoResponseDto.setStatus("Activated");
		JSONObject identityJson = JsonUtil.getJSONObject(identity, "identity");
		idRepoResponseDto.setIdentity(identityJson);
		response.setResponse(idRepoResponseDto);
		Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
				any(), any(Class.class))).thenReturn(response);

		Mockito.doReturn(null).when(env).getProperty("mosip.default.template-languages");
		Map<String, Object> attributes = utilitySpy.getMailingAttributes("3527812406", new HashSet<String>(), identityJson, mapperIdentity);
		assertEquals("eng", attributes.get("preferredLang"));
	}

	@Test
	public void testGetMappingJson() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		ReflectionTestUtils.setField(utility, "regProcessorIdentityJson", mappingJson);
		JSONObject mapperJson = JsonUtil.readValue(mappingJson, JSONObject.class);
		Map mapperIdentity = (Map) mapperJson.get("identity");

		ResponseWrapper<IdRepoResponseDto> response = new ResponseWrapper<>();
		IdRepoResponseDto idRepoResponseDto = new IdRepoResponseDto();
		idRepoResponseDto.setStatus("Activated");
		JSONObject identityJson = JsonUtil.getJSONObject(identity, "identity");
		idRepoResponseDto.setIdentity(identityJson);		
		response.setResponse(idRepoResponseDto);
		Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
				any(), any(Class.class))).thenReturn(response);

		Map<String, Object> attributes = utility.getMailingAttributes("3527812406", new HashSet<String>(), identityJson, mapperIdentity);
		assertEquals("eng", attributes.get("preferredLang"));
		verify(residentRestTemplate, never()).getForObject(anyString(), any(Class.class));
	}

	@Test
	public void testGetFileNameAsPerFeatureNameShareCredWithPartner(){
		assertEquals("SHARE_CRED_WITH_PARTNER", utility.getFileName("123", "SHARE_CRED_WITH_PARTNER", 0, LOCALE));
		assertEquals("GENERATE_VID", utility.getFileName("123", "GENERATE_VID", 0, LOCALE));
		assertEquals("REVOKE_VID", utility.getFileName("123", "REVOKE_VID", 0, LOCALE));
		assertEquals("ORDER_PHYSICAL_CARD", utility.getFileName("123", "ORDER_PHYSICAL_CARD", 0, LOCALE));
		assertEquals("DOWNLOAD_PERSONALIZED_CARD", utility.getFileName("123", "DOWNLOAD_PERSONALIZED_CARD", 0, LOCALE));
		assertEquals("UPDATE_MY_UIN", utility.getFileName("123", "UPDATE_MY_UIN", 0, LOCALE));
		assertEquals("AUTH_TYPE_LOCK_UNLOCK", utility.getFileName("123", "AUTH_TYPE_LOCK_UNLOCK", 0, LOCALE));
		assertEquals("Generic", utility.getFileName("123", "Generic", 0, LOCALE));
	}

	@Test
	public void testGetFileNameAsPerFeatureNameGenerateVid(){
		Mockito.when(env.getProperty(ResidentConstants.ACK_MANAGE_MY_VID_NAMING_CONVENTION_PROPERTY))
				.thenReturn("Ack_Manage_my_VID_{eventId}_{timestamp}.pdf");
		Mockito.when(env.getProperty("resident.datetime.pattern"))
				.thenReturn("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		assertNotNull(utility.getFileName("123", "Ack_Manage_my_VID_{eventId}_{timestamp}.pdf", 0, LOCALE));
	}

	@Test
	public void testGetFileNameNullEventId(){
		Mockito.when(env.getProperty(ResidentConstants.ACK_MANAGE_MY_VID_NAMING_CONVENTION_PROPERTY))
				.thenReturn("Ack_Manage_my_VID_{eventId}_{timestamp}.pdf");
		Mockito.when(env.getProperty("resident.datetime.pattern"))
				.thenReturn("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		assertNotNull(utility.getFileName(null, "Ack_Manage_my_VID_{eventId}_{timestamp}.pdf", 0, LOCALE));
	}

	@Test
	public void testGetIdForResidentTransactionEmail() throws ResidentServiceCheckedException, NoSuchAlgorithmException {
		IdentityDTO identityDTO = new IdentityDTO();
		identityDTO.setUIN("2186705746");
		identityDTO.setEmail("kameshprasad1338@gmail.com");
		identityDTO.setPhone("8809989898");
		Mockito.when(identityService.getIdentity(Mockito.anyString())).thenReturn(identityDTO);
		Mockito.when(identityService.getIDAToken(Mockito.anyString())).thenReturn("2186705746");
		assertEquals(HMACUtils2.digestAsPlainText(("kameshprasad1338@gmail.com"+"2186705746").getBytes()),
				utility.getIdForResidentTransaction(List.of("EMAIL"), identityDTO, idaToken));
	}

	@Test
	public void testGetIdForResidentTransactionPhone() throws ResidentServiceCheckedException, NoSuchAlgorithmException {
		IdentityDTO identityDTO = new IdentityDTO();
		identityDTO.setUIN("2186705746");
		identityDTO.setEmail("kameshprasad1338@gmail.com");
		identityDTO.setPhone("8809989898");
		Mockito.when(identityService.getIdentity(Mockito.anyString())).thenReturn(identityDTO);
		Mockito.when(identityService.getIDAToken(Mockito.anyString())).thenReturn("2186705746");
		assertEquals(HMACUtils2.digestAsPlainText(("8809989898"+"2186705746").getBytes()),
				utility.getIdForResidentTransaction(List.of("PHONE"), identityDTO, idaToken));
	}

	@Test
	public void testGetIdForResidentTransactionPhoneEmail() throws ResidentServiceCheckedException, NoSuchAlgorithmException {
		IdentityDTO identityDTO = new IdentityDTO();
		identityDTO.setUIN("2186705746");
		identityDTO.setEmail("kameshprasad1338@gmail.com");
		identityDTO.setPhone("8809989898");
		Mockito.when(identityService.getIdentity(Mockito.anyString())).thenReturn(identityDTO);
		Mockito.when(identityService.getIDAToken(Mockito.anyString())).thenReturn("2186705746");
		assertEquals(HMACUtils2.digestAsPlainText(("kameshprasad1338@gmail.com"+"8809989898"+"2186705746").getBytes()),
				utility.getIdForResidentTransaction(List.of("PHONE","EMAIL"), identityDTO, idaToken));
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetIdForResidentTransactionPhoneEmailFailure() throws ResidentServiceCheckedException, NoSuchAlgorithmException {
		IdentityDTO identityDTO = new IdentityDTO();
		identityDTO.setUIN("2186705746");
		identityDTO.setEmail("kameshprasad1338@gmail.com");
		identityDTO.setPhone("8809989898");
		Mockito.when(identityService.getIdentity(Mockito.anyString())).thenReturn(identityDTO);
		Mockito.when(identityService.getIDAToken(Mockito.anyString())).thenReturn("2186705746");
		assertEquals(HMACUtils2.digestAsPlainText(("kameshprasad1338@gmail.com"+"8809989898"+"2186705746").getBytes()),
				utility.getIdForResidentTransaction(List.of("PH"), identityDTO, idaToken));
	}

	@Test
	public void testSignPdf() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] array = "pdf".getBytes();
		out.write(array);
		Mockito.when(pdfGenerator.generate((InputStream) any())).thenReturn(out);
		utility.signPdf(new ByteArrayInputStream("pdf".getBytes()), null);
	}

	@Test
	public void testCreateDownloadLinkFailure(){
		assertEquals("NA", utility.createDownloadCardLinkFromEventId(new ResidentTransactionEntity()));
	}

	@Test
	public void testCreateDownloadLinkSuccess(){
		ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
		ReflectionTestUtils.setField(utility, "downloadCardUrl", "http://mosip/event/{eventId}");
		residentTransactionEntity.setReferenceLink("http://mosip");
		residentTransactionEntity.setEventId("123455678");
		assertEquals("http://mosip/event/123455678", utility.createDownloadCardLinkFromEventId(residentTransactionEntity));
	}

	@Test
	public void testCreateTrackServiceRequestLink(){
		ReflectionTestUtils.setField(utility, "trackServiceUrl", "http://mosip");
		assertEquals(("http://mosip"+"2186705746111111"), utility.createTrackServiceRequestLink("2186705746111111"));
	}

	@Test
	public void testCreateEventId(){
		ReflectionTestUtils.setField(utility, "trackServiceUrl", "http://mosip");
		Mockito.when(utilities.getSecureRandom()).thenReturn(new SecureRandom());
		assertEquals(16,utility.createEventId().length());
	}

	@Test
	public void testCreateEntity(){
		assertEquals("Unknown",utility.createEntity(RequestType.SHARE_CRED_WITH_PARTNER).getCrBy());
	}

	@Test
	public void testGetFileNameAsPerFeatureName(){
		Mockito.when(env.getProperty(Mockito.anyString()))
				.thenReturn("AckFileName");
		assertEquals("AckFileName", utility.getFileNameAsPerFeatureName("123", RequestType.SHARE_CRED_WITH_PARTNER, 0, LOCALE));
		assertEquals("AckFileName", utility.getFileNameAsPerFeatureName("123", RequestType.GENERATE_VID, 0, LOCALE));
		assertEquals("AckFileName", utility.getFileNameAsPerFeatureName("123", RequestType.REVOKE_VID, 0, LOCALE));
		assertEquals("AckFileName", utility.getFileNameAsPerFeatureName("123", RequestType.ORDER_PHYSICAL_CARD, 0, LOCALE));
		assertEquals("AckFileName", utility.getFileNameAsPerFeatureName("123", RequestType.DOWNLOAD_PERSONALIZED_CARD, 0, LOCALE));
		assertEquals("AckFileName", utility.getFileNameAsPerFeatureName("123", RequestType.UPDATE_MY_UIN, 0, LOCALE));
		assertEquals("AckFileName", utility.getFileNameAsPerFeatureName("123", RequestType.AUTH_TYPE_LOCK_UNLOCK, 0, LOCALE));
		assertEquals("AckFileName", utility.getFileNameAsPerFeatureName("123", RequestType.DEFAULT, 0, LOCALE));
	}

	@Test
	public void testGetClientIp() {
		Mockito.when(request.getHeader(Mockito.anyString())).thenReturn("1.2.3,1.3");
		String ipAddress = utility.getClientIp(request);
		assertEquals("1.2.3", ipAddress);
	}

	@Test
	public void testGetClientIpEmpty() {
		Mockito.when(request.getHeader(Mockito.anyString())).thenReturn("");
		Mockito.when(request.getRemoteAddr()).thenReturn("1.1.5");
		String ipAddress = utility.getClientIp(request);
		assertEquals("1.1.5", ipAddress);
	}

	@Test
	public void testGetClientIpNull() {
		Mockito.when(request.getHeader(Mockito.anyString())).thenReturn(null);
		Mockito.when(request.getRemoteAddr()).thenReturn("1.5.5");
		String ipAddress = utility.getClientIp(request);
		assertEquals("1.5.5", ipAddress);
	}
	
	@Test
	public void test_formatWithOffsetForFileName_en_US() {
		LocalDateTime localDateTime = LocalDateTime.of(1993, 8, 14, 16, 54);
		String formatWithOffsetForFileName = utility.formatWithOffsetForFileName(0, "en-US", localDateTime);
		assertEquals("Aug_14_1993_4.54.00_PM", formatWithOffsetForFileName);
	}
	
	@Test
	public void test_formatWithOffsetForFileName_en_IN() {
		LocalDateTime localDateTime = LocalDateTime.of(1993, 8, 14, 16, 54);
		String formatWithOffsetForFileName = utility.formatWithOffsetForFileName(-330, "en-IN", localDateTime);
		assertEquals("14-Aug-1993_10.24.00_PM", formatWithOffsetForFileName);
	}
	
	@Test
	public void test_formatWithOffsetForFileName_null_locale() {
		LocalDateTime localDateTime = LocalDateTime.of(1993, 8, 14, 16, 54);
		String formatWithOffsetForFileName = utility.formatWithOffsetForFileName(0, null, localDateTime);
		assertEquals("1993-08-14_04.54.00_PM", formatWithOffsetForFileName);
	}
	

	
	@Test
	public void test_formatWithOffsetForUI_en_US() {
		LocalDateTime localDateTime = LocalDateTime.of(1993, 8, 14, 16, 54);
		String formatWithOffsetForFileName = utility.formatWithOffsetForUI(0, "en-US", localDateTime);
		assertEquals("Aug 14, 1993, 4:54:00 PM", formatWithOffsetForFileName);
	}
	
	@Test
	public void test_formatWithOffsetForUI_en_IN() {
		LocalDateTime localDateTime = LocalDateTime.of(1993, 8, 14, 16, 54);
		String formatWithOffsetForFileName = utility.formatWithOffsetForUI(-330, "en-IN", localDateTime);
		assertEquals("14-Aug-1993, 10:24:00 PM", formatWithOffsetForFileName);
	}
	
	@Test
	public void test_formatWithOffsetForUI_null_locale() {
		LocalDateTime localDateTime = LocalDateTime.of(1993, 8, 14, 16, 54);
		String formatWithOffsetForFileName = utility.formatWithOffsetForUI(0, null, localDateTime);
		assertEquals("1993-08-14", formatWithOffsetForFileName);
	}

	@Test(expected = RuntimeException.class)
	public void testGetSessionUserName() throws ApisResourceAccessException {
		Mockito.when(env.getProperty(Mockito.anyString())).thenReturn("name");
		Mockito.when(identityService.getAvailableclaimValue(Mockito.anyString())).thenThrow(new ApisResourceAccessException());
		utility.getSessionUserName();
	}

	@Test
	public void test_formatWithOffsetForUI_locale_length_1() {
		LocalDateTime localDateTime = LocalDateTime.of(1993, 8, 14, 16, 54);
		String formatWithOffsetForFileName = utility.formatWithOffsetForUI(0, "en", localDateTime);
		assertEquals("Aug 14, 1993, 4:54:00 PM", formatWithOffsetForFileName);
	}

	@Test
	public void test_formatWithOffsetForUI_local_null(){
		ReflectionTestUtils.invokeMethod(utility, "formatToLocaleDateTime", null, null, LocalDateTime.now());
	}

	@Test
	public void testGetRefIdHash() throws NoSuchAlgorithmException {
		assertEquals("B9CCBC594A8572018BC9DC97AB7A4BB175ABFC2F9FE3197D891D542C02C7ECE7",
				utility.getRefIdHash("4936295739034704"));
	}

	@Test
	public void testGetFileNameAck(){
		utility.getFileNameAck(RequestType.GET_MY_ID.getName(), "4936295739034704",
				"Ack_{featureName}_{eventId}_{timestamp}", 0, "en-IN");
	}

	@Test
	public void testReplaceSpecialChars(){
		ReflectionTestUtils.setField(utility, "specialCharsReplacementMap", Map.of());
		assertEquals("Get_My_Id",
				ReflectionTestUtils.invokeMethod(utility, "replaceSpecialChars", "Get_My_Id"));
	}

	@Test
	public void testGetFileNameForId(){
		utility.getFileNameForId("Get_My_Id", "UIN_{id}_{timestamp}", 0, "en-IN");
	}

	@Test
	public void testSignPdfSuccess() throws Exception {
		// Mocking environment properties
		when(env.getProperty(ResidentConstants.LOWER_LEFT_X)).thenReturn("10");
		when(env.getProperty(ResidentConstants.LOWER_LEFT_Y)).thenReturn("20");
		when(env.getProperty(ResidentConstants.UPPER_RIGHT_X)).thenReturn("100");
		when(env.getProperty(ResidentConstants.UPPER_RIGHT_Y)).thenReturn("200");
		when(env.getProperty(ResidentConstants.REASON)).thenReturn("Test Reason");
		when(env.getProperty(ResidentConstants.SIGN_PDF_APPLICATION_ID)).thenReturn("AppId");
		when(env.getProperty(ResidentConstants.SIGN_PDF_REFERENCE_ID)).thenReturn("RefId");
		when(env.getProperty(DATETIME_PATTERN)).thenReturn("yyyy-MM-dd HH:mm:ss");
		when(env.getProperty(ApiName.PDFSIGN.name())).thenReturn("http://dev.mosip.net");

		// Mocking PDF generator
		byte[] pdfContent = "Sample PDF Content".getBytes();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		// Write the byte array to the output stream
		outputStream.write(pdfContent);

		try (FileOutputStream fileOutputStream = new FileOutputStream("output.txt")) {
			outputStream.writeTo(fileOutputStream);
		}

		when(pdfGenerator.generate(any(InputStream.class))).thenReturn(outputStream);
		when(utilities.getTotalNumberOfPageInPdf(Mockito.any())).thenReturn(1);

		// Mocking response from the REST client
		ResponseWrapper<SignatureResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(new SignatureResponseDto());
		when(residentServiceRestClient.postApi(
				any(),
				any(),
				any(),
				any()
		)).thenReturn(responseWrapper);

		when(objectMapper.writeValueAsString(any())).thenReturn("mock");
		SignatureResponseDto signatureResponseDto= new SignatureResponseDto();
		signatureResponseDto.setData("ZGF0YQ==");
		when(objectMapper.readValue(anyString(), (Class<Object>) any())).thenReturn(signatureResponseDto);

		// Call the method to be tested
		InputStream pdfInputStream = new ByteArrayInputStream(pdfContent);
		byte[] signaturedPdf = utility.signPdf(pdfInputStream, "password");

		// Assertions
		assertNotNull(signaturedPdf);

		outputStream.close();
	}

	@Test
	public void testSignPdfFailed() throws Exception {
		// Mocking environment properties
		when(env.getProperty(ResidentConstants.LOWER_LEFT_X)).thenReturn("10");
		when(env.getProperty(ResidentConstants.LOWER_LEFT_Y)).thenReturn("20");
		when(env.getProperty(ResidentConstants.UPPER_RIGHT_X)).thenReturn("100");
		when(env.getProperty(ResidentConstants.UPPER_RIGHT_Y)).thenReturn("200");
		when(env.getProperty(ResidentConstants.REASON)).thenReturn("Test Reason");
		when(env.getProperty(ResidentConstants.SIGN_PDF_APPLICATION_ID)).thenReturn("AppId");
		when(env.getProperty(ResidentConstants.SIGN_PDF_REFERENCE_ID)).thenReturn("RefId");
		when(env.getProperty(DATETIME_PATTERN)).thenReturn("yyyy-MM-dd HH:mm:ss");
		when(env.getProperty(ApiName.PDFSIGN.name())).thenReturn("http://dev.mosip.net");

		// Mocking PDF generator
		byte[] pdfContent = "Sample PDF Content".getBytes();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		// Write the byte array to the output stream
		outputStream.write(pdfContent);

		try (FileOutputStream fileOutputStream = new FileOutputStream("output.txt")) {
			outputStream.writeTo(fileOutputStream);
		}

		when(pdfGenerator.generate(any(InputStream.class))).thenReturn(outputStream);
		when(utilities.getTotalNumberOfPageInPdf(Mockito.any())).thenReturn(1);

		// Mocking response from the REST client
		ResponseWrapper<SignatureResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(new SignatureResponseDto());
		responseWrapper.setErrors(List.of(new ServiceError(ResidentErrorCode.UNKNOWN_EXCEPTION.getErrorCode(),
				ResidentErrorCode.UNKNOWN_EXCEPTION.getErrorMessage())));
		when(residentServiceRestClient.postApi(
				any(),
				any(),
				any(),
				any()
		)).thenReturn(responseWrapper);

		when(objectMapper.writeValueAsString(any())).thenReturn("mock");
		SignatureResponseDto signatureResponseDto= new SignatureResponseDto();
		signatureResponseDto.setData("ZGF0YQ==");
		when(objectMapper.readValue(anyString(), (Class<Object>) any())).thenReturn(signatureResponseDto);

		// Call the method to be tested
		InputStream pdfInputStream = new ByteArrayInputStream(pdfContent);
		byte[] signaturedPdf = utility.signPdf(pdfInputStream, "password");

		// Assertions
		assertNull(signaturedPdf);

		outputStream.close();
	}

	@Test
	public void testDecodeAndDecryptUserInfoOidcJwtDisabled(){
		Mockito.when(env.getProperty(ResidentConstants.MOSIP_OIDC_JWT_SIGNED)).thenReturn(String.valueOf(true));
		Mockito.when(env.getProperty(ResidentConstants.MOSIP_OIDC_JWT_VERIFY_ENABLED)).thenReturn(String.valueOf(false));
		AuthErrorCode authErrorCode = null;
		ImmutablePair<Boolean, AuthErrorCode> verifySignature = new ImmutablePair<>(true, authErrorCode);
		Mockito.when(tokenValidationHelper
				.verifyJWTSignagure(Mockito.any())).thenReturn(verifySignature);
		ReflectionTestUtils.invokeMethod(utility, "decodeAndDecryptUserInfo", token);
	}

	@Test
	public void testDecodeAndDecryptUserInfo(){
		Mockito.when(env.getProperty(ResidentConstants.MOSIP_OIDC_JWT_SIGNED)).thenReturn(String.valueOf(true));
		Mockito.when(env.getProperty(ResidentConstants.MOSIP_OIDC_JWT_VERIFY_ENABLED)).thenReturn(String.valueOf(true));
		AuthErrorCode authErrorCode = null;
		ImmutablePair<Boolean, AuthErrorCode> verifySignature = new ImmutablePair<>(true, authErrorCode);
		Mockito.when(tokenValidationHelper
				.verifyJWTSignagure(Mockito.any())).thenReturn(verifySignature);
		ReflectionTestUtils.invokeMethod(utility, "decodeAndDecryptUserInfo", token);
	}

	@Test(expected = ResidentServiceException.class)
	public void testDecodeAndDecryptUserInfoOidcJwtDisabledFailure(){
		Mockito.when(env.getProperty(ResidentConstants.MOSIP_OIDC_JWT_SIGNED)).thenReturn(String.valueOf(true));
		Mockito.when(env.getProperty(ResidentConstants.MOSIP_OIDC_JWT_VERIFY_ENABLED)).thenReturn(String.valueOf(true));
		AuthErrorCode authErrorCode = AuthErrorCode.FORBIDDEN;
		ImmutablePair<Boolean, AuthErrorCode> verifySignature = new ImmutablePair<>(false, authErrorCode);
		Mockito.when(tokenValidationHelper
				.verifyJWTSignagure(Mockito.any())).thenReturn(verifySignature);
		ReflectionTestUtils.invokeMethod(utility, "decodeAndDecryptUserInfo", token);
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
		ReflectionTestUtils.invokeMethod(utility, "decodeAndDecryptUserInfo", token);
	}

	@Test
	public void testDecryptPayload(){
		Mockito.when(env.getProperty(Mockito.anyString())).thenReturn("RESIDENT");
		Mockito.when(objectStoreHelper.decryptData(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn("payload");
		assertEquals("payload", ReflectionTestUtils.invokeMethod(utility, "decryptPayload", "payload"));
	}

	private Tuple3<URI, MultiValueMap<String, String>, Map<String, Object>> loadUserInfoMethod() throws Exception {

		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("claim", "value");
		URI uri = URI.create("http://localhost:8080/userinfo");
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", "Bearer " + token);
		return Tuples.of(uri, headers, userInfo);
	}

	@Test
	public void testGetPreferredLanguageSingleLanguage() {
		Map<String, Object> demographicIdentity = new HashMap<>();
		demographicIdentity.put("languageAttribute", "English");

		when(env.getProperty("mosip.default.user-preferred-language-attribute"))
				.thenReturn("languageAttribute");

		Set<String> preferredLanguages = utility.getPreferredLanguage(demographicIdentity);
		assertEquals(1, preferredLanguages.size());
		assertEquals("English", preferredLanguages.iterator().next());
	}

	@Test
	public void testGetPreferredLanguageMultipleLanguages() {
		Map<String, Object> demographicIdentity = new HashMap<>();
		demographicIdentity.put("languageAttribute", "English,Hindi,French");

		when(env.getProperty("mosip.default.user-preferred-language-attribute"))
				.thenReturn("languageAttribute");

		Set<String> preferredLanguages = utility.getPreferredLanguage(demographicIdentity);
		assertEquals(3, preferredLanguages.size());
		assertEquals(Set.of("English", "Hindi", "French"), preferredLanguages);
	}

	@Test
	public void testGetPreferredLanguageAttributeNotSet() {
		Map<String, Object> demographicIdentity = new HashMap<>();
		demographicIdentity.put("languageAttribute", "English");

		when(env.getProperty("mosip.default.user-preferred-language-attribute"))
				.thenReturn(null);

		Set<String> preferredLanguages = utility.getPreferredLanguage(demographicIdentity);
		assertEquals(0, preferredLanguages.size());
	}

	@Test
	public void testGetDefaultTemplateLanguagesV2() {
		when(env.getProperty("mosip.default.template-languages")).thenReturn("en,fr,es");

		List<String> result = utility.getDefaultTemplateLanguages();

		assertEquals(3, result.size());
		assertEquals(Arrays.asList("en", "fr", "es"), result);
	}

	@Test
	public void testGetDefaultTemplateLanguagesEmpty() {
		when(env.getProperty("mosip.default.template-languages")).thenReturn(null);

		List<String> result = utility.getDefaultTemplateLanguages();

		assertEquals(0, result.size());
	}

	@Test
	public void testGetCenterDetails() throws ApisResourceAccessException {
		String centerId = "center123";
		String langCode = "en";

		ResponseWrapper<RegistrationCenterResponseDto> expectedResponse = new ResponseWrapper<>();
		RegistrationCenterResponseDto registrationCenterResponseDto = new RegistrationCenterResponseDto();
		RegistrationCenterDto registrationCenterDto = new RegistrationCenterDto();
		registrationCenterDto.setId(centerId);
		registrationCenterDto.setLangCode(langCode);
		registrationCenterResponseDto.setRegistrationCenters(List.of(registrationCenterDto));
		expectedResponse.setResponse(registrationCenterResponseDto);

		List<String> pathSegments = Arrays.asList(centerId, langCode);
		when(residentServiceRestClient.getApi(ApiName.CENTERDETAILS, pathSegments, "", "", ResponseWrapper.class))
				.thenReturn(expectedResponse);

		ResponseWrapper<?> result = utility.getCenterDetails(centerId, langCode);

		assertEquals(expectedResponse, result);
	}

	@Test
	public void testGetValidDocumentByLangCode() throws ResidentServiceCheckedException {
		String langCode = "en";

		ResponseWrapper responseWrapper = new ResponseWrapper<>();
		when(proxyMasterdataService.getValidDocumentByLangCode(langCode)).thenReturn(responseWrapper);

		ResponseWrapper<?> result = utility.getValidDocumentByLangCode(langCode);

		assertEquals(responseWrapper, result);
	}

	@Test
	public void testGetPartnersByPartnerType_WithPartnerType() throws ResidentServiceCheckedException {
		String partnerType = "partner";
		ApiName apiUrl = ApiName.PARTNER_API_URL;

		ResponseWrapper expectedResponse = new ResponseWrapper<>();
		when(proxyPartnerManagementService.getPartnersByPartnerType(
				Optional.of(partnerType), apiUrl))
				.thenReturn(expectedResponse);

		ResponseWrapper<?> result = utility.getPartnersByPartnerType(partnerType, apiUrl);

		assertEquals(expectedResponse, result);
	}

	@Test
	public void testGetPartnersByPartnerType_WithoutPartnerType() throws ResidentServiceCheckedException {
		ApiName apiUrl = ApiName.PARTNER_API_URL;

		ResponseWrapper expectedResponse = new ResponseWrapper<>();
		when(proxyPartnerManagementService.getPartnersByPartnerType(
				Optional.empty(), apiUrl))
				.thenReturn(expectedResponse);

		ResponseWrapper<?> result = utility.getPartnersByPartnerType(null, apiUrl);

		assertEquals(expectedResponse, result);
	}

	@Test
	public void testClearIdentityMapCache() {
		utility.clearIdentityMapCache(token);
	}
}
