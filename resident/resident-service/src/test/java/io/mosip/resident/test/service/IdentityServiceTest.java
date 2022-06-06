package io.mosip.resident.test.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.biometrics.spi.CbeffUtil;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.handler.service.ResidentConfigService;
import io.mosip.resident.service.IdentityService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utilitiy;

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
	private ResidentServiceRestClient restClientWithSelfTOkenRestTemplate;

	@Mock
	private ResidentConfigService residentConfigService;

	private ResponseWrapper responseWrapper;

	private Map responseMap;

	private List responseList;

	private Map bdbFaceMap;

	@Before
	public void setUp() throws Exception {
		ReflectionTestUtils.setField(identityService, "dateFormat", "yyyy/MM/dd");
		ReflectionTestUtils.setField(identityService, "individualDocs", "individualBiometrics");

		Map identityMap = new LinkedHashMap();
		identityMap.put("UIN", "8251649601");
		identityMap.put("email", "manojvsp12@gmail.com");
		identityMap.put("phone", "9395910872");
		identityMap.put("dateOfBirth", "1970/11/16");

		List<Map> nameList = new ArrayList();
		Map nameMap = new LinkedHashMap();
		nameMap.put("language", "eng");
		nameMap.put("value", "Manoj_eng");
		nameList.add(nameMap);
		identityMap.put("firstName", nameList);

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
	}

	@Test
	public void testGetIdentity() throws Exception {
		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), anyMap(), anyList(), anyList(), any()))
				.thenReturn(responseWrapper);
		responseWrapper.setErrors(null);
		when(residentConfigService.getUiSchemaFilteredInputAttributes())
				.thenReturn(List.of("UIN", "email", "phone", "dateOfBirth", "firstName"));
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		when(utility.getMappingJson()).thenReturn(mappingJson);
		String str = CryptoUtil.encodeToURLSafeBase64("response return".getBytes());
		when(cbeffUtil.getBDBBasedOnType(any(), anyString(), any())).thenReturn(bdbFaceMap);
		IdentityDTO result = identityService.getIdentity("6", "bio", "eng");
		assertNotNull(result);
		assertEquals("8251649601", result.getUIN());
//		assertEquals("Manoj_eng", result.getFullName());
	}

	@Test
	public void testGetIdentityLangCodeNull() throws Exception {
		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), anyMap(), anyList(), anyList(), any()))
				.thenReturn(responseWrapper);
		responseWrapper.setErrors(null);
		when(residentConfigService.getUiSchemaFilteredInputAttributes())
				.thenReturn(List.of("UIN", "email", "phone", "dateOfBirth", "firstName"));
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		when(utility.getMappingJson()).thenReturn(mappingJson);
		String str = CryptoUtil.encodeToURLSafeBase64("response return".getBytes());
		when(cbeffUtil.getBDBBasedOnType(any(), anyString(), any())).thenReturn(bdbFaceMap);
		IdentityDTO result = identityService.getIdentity("6");
		assertNotNull(result);
		assertEquals("8251649601", result.getUIN());
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetIdentityNestedTryCatch() throws Exception {
		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), anyMap(), anyList(), anyList(), any()))
				.thenReturn(responseWrapper);
		responseWrapper.setErrors(null);
		when(residentConfigService.getUiSchemaFilteredInputAttributes())
				.thenReturn(List.of("UIN", "email", "phone", "dateOfBirth", "fullName"));
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		when(utility.getMappingJson()).thenReturn(mappingJson);
		String str = CryptoUtil.encodeToURLSafeBase64("response return".getBytes());
		IdentityDTO result = identityService.getIdentity("6");
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

}
