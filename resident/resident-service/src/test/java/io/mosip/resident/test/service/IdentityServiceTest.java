package io.mosip.resident.test.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.idrepository.core.util.TokenIDGenerator;
import io.mosip.kernel.biometrics.spi.CbeffUtil;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.handler.service.ResidentConfigService;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utilitiy;
import io.mosip.resident.validator.RequestValidator;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.FileInputStream;
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
	private io.mosip.resident.service.IdentityService identityService = new IdentityServiceImpl();

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

	private ResponseWrapper responseWrapper;

	private Map responseMap;

	private List responseList;

	private Map bdbFaceMap;

	private ObjectMapper objectMapper = new ObjectMapper();
	
	
	@Before
	public void setUp() throws Exception {
		ReflectionTestUtils.setField(identityService, "dateFormat", "yyyy/MM/dd");
		ReflectionTestUtils.setField(identityService, "individualDocs", "individualBiometrics");
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
		assertEquals("Rahul Singh Kumar", result.getFullName());
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
		String result = identityService.getUinForIndividualId(id);
		assertEquals("8251649601", result);
	}

	@Test
	public void testGetIDATokenForIndividualId() throws Exception{
		String id = "123456789";
		String token = "1234";
		ReflectionTestUtils.setField(identityService, "onlineVerificationPartnerId", "m-partner-default-auth");
		when(tokenIDGenerator.generateTokenID(anyString(), anyString())).thenReturn(token);
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
}
