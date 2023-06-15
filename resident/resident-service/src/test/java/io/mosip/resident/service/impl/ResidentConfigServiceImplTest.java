package io.mosip.resident.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.MappingJsonConstants;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.dto.SharableAttributesDTO;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.util.AuditUtil;

@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class ResidentConfigServiceImplTest {

	@InjectMocks
	private ResidentConfigServiceImpl configServiceImpl;

	@Mock
	private Environment env;

	@Mock
	private ResourceLoader resourceLoader;

	@Mock
	private Resource residentUiSchemaJsonFile;

	@Mock
	private Resource identityMappingJsonFile;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private AuditUtil auditUtil;

	Resource resource;
	private String identityMapping;
	private String uiSchema;
	private Map<String, Object> uiSchemaMap;

	@Before
	public void setUp() throws Exception {
		identityMapping = "{\"name\":\"identity-mapping\"}";
		Mockito.when(identityMappingJsonFile.getInputStream())
				.thenReturn(new ByteArrayInputStream(identityMapping.getBytes()));
		ReflectionTestUtils.setField(configServiceImpl, "resourceLoader", resourceLoader);
		ReflectionTestUtils.setField(configServiceImpl, "residentUiSchemaJsonFilePrefix", "classpath:resident-ui");
		resource = Mockito.mock(Resource.class);
		Mockito.when(resourceLoader.getResource(Mockito.anyString())).thenReturn(resource);
		when(resource.exists()).thenReturn(true);

		// getUISchemaData()
		uiSchema = "{\"name\":\"ui-schema\"}";
		when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(uiSchema.getBytes()));
		uiSchemaMap = new HashMap<>();
		List<Map<String, Object>> uiSchemaInputAttributes = new ArrayList<>();
		Map<String, String> map1 = new HashMap<>();
		map1.put(ResidentConstants.LABEL, "Full Name");
		map1.put(ResidentConstants.VALUE, "fullName");
		List<Map<String, String>> list = new ArrayList<>();
		list.add(map1);
		Map<String, Object> mapFormat = new HashMap<>();
		mapFormat.put("eng", list);
		Map<String, Object> uiSchemaInputAttribute1 = new HashMap<>();
		uiSchemaInputAttribute1.put(ResidentConstants.ATTRIBUTE_NAME, "fullName");
		uiSchemaInputAttribute1.put(ResidentConstants.MASK_REQUIRED, false);
		uiSchemaInputAttribute1.put(ResidentConstants.LABEL, Map.of("eng", "Name"));
		uiSchemaInputAttribute1.put(ResidentConstants.FORMAT_REQUIRED, true);
		uiSchemaInputAttribute1.put(ResidentConstants.FORMAT_OPTION, mapFormat);
		uiSchemaInputAttributes.add(uiSchemaInputAttribute1);
		Map<String, Object> uiSchemaInputAttribute2 = new HashMap<>();
		uiSchemaInputAttribute2.put(ResidentConstants.ATTRIBUTE_NAME, "UIN");
		uiSchemaInputAttribute2.put(ResidentConstants.MASK_REQUIRED, true);
		uiSchemaInputAttribute2.put(ResidentConstants.MASK_ATTRIBUTE_NAME, "masked_UIN");
		uiSchemaInputAttribute2.put(ResidentConstants.LABEL, Map.of("eng", "UIN"));
		uiSchemaInputAttribute2.put(ResidentConstants.FORMAT_REQUIRED, false);
		uiSchemaInputAttributes.add(uiSchemaInputAttribute2);
		uiSchemaMap.put(MappingJsonConstants.IDENTITY, uiSchemaInputAttributes);
		Mockito.when(objectMapper.readValue(uiSchema.getBytes(), Map.class)).thenReturn(uiSchemaMap);
	}

	private ResidentConfigServiceImpl createTestSubject() {
		return configServiceImpl;
	}

	@Test
	public void testGetUIProperties_emptyPropArray() throws Exception {
		ResidentConfigServiceImpl testSubject;
		ResponseWrapper<?> result;

		// default test
		testSubject = createTestSubject();
		ReflectionTestUtils.setField(testSubject, "propKeys", new String[0]);
		ReflectionTestUtils.setField(testSubject, "env", env);
		result = testSubject.getUIProperties();
		Set resultProps = ((Map) result.getResponse()).keySet();
		assertTrue(resultProps.size() == 0);
	}

	@Test
	public void testGetUIProperties_nonEmptyPropArray() throws Exception {
		ResidentConfigServiceImpl testSubject;
		ResponseWrapper<?> result;

		// default test
		testSubject = createTestSubject();
		String[] propKeys = new String[] { "aaa.key", "bbb.key", "ccc.key" };
		ReflectionTestUtils.setField(testSubject, "propKeys", propKeys);
		when(env.getProperty("aaa.key", Object.class)).thenReturn("aaa");
		when(env.getProperty("bbb.key", Object.class)).thenReturn("bbb");
		ReflectionTestUtils.setField(testSubject, "env", env);
		result = testSubject.getUIProperties();
		Set resultProps = ((Map) result.getResponse()).keySet();
		assertTrue(resultProps.size() == 2);
		assertTrue(resultProps.contains("aaa.key"));
		assertTrue(resultProps.contains("bbb.key"));

	}

	@Test(expected = ResidentServiceException.class)
	public void testGetUISchemaElse() throws Exception {
		ResidentConfigServiceImpl testSubject = createTestSubject();
		when(resource.exists()).thenReturn(false);
		testSubject.getUISchema("update-demographics");
	}

	private void getIdentityMappingMap(ResidentConfigServiceImpl testSubject)
			throws IOException, JsonParseException, JsonMappingException {
		ReflectionTestUtils.setField(testSubject, "identityMapping", identityMapping);
		Map<String, Object> identityDataMap = new HashMap<>();
		Map<String, Object> identityAttributeMap1 = new HashMap<>();
		identityAttributeMap1.put(MappingJsonConstants.VALUE, "fullName");
		identityDataMap.put("name", identityAttributeMap1);
		Map<String, Object> identityAttributeMap2 = new HashMap<>();
		identityAttributeMap2.put(MappingJsonConstants.VALUE, "UIN");
		identityDataMap.put("uin", identityAttributeMap2);
		Map<String, Object> identityMappingMap = new HashMap<>();
		identityMappingMap.put(MappingJsonConstants.IDENTITY, identityDataMap);
		Mockito.when(objectMapper.readValue(identityMapping.getBytes(), Map.class)).thenReturn(identityMappingMap);
	}

	@Test
	public void testGetIdentityMappingTry() throws Exception {
		ResidentConfigServiceImpl testSubject;
		testSubject = createTestSubject();
		identityMapping = null;
		ReflectionTestUtils.setField(testSubject, "identityMapping", identityMapping);
		identityMapping = "{\"name\":\"identity-mapping\"}";
		String result = testSubject.getIdentityMapping();
		assertTrue(result.contains(identityMapping));
	}

	@Test
	public void testGetUiSchemaFilteredInputAttributes() throws Exception {
		ResidentConfigServiceImpl testSubject;
		List<String> result;
		testSubject = createTestSubject();
		result = testSubject.getUiSchemaFilteredInputAttributes("update-demographics");
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceException.class)
	public void testGetUiSchemaFilteredInputAttributesNotNull() throws Exception {
		ResidentConfigServiceImpl testSubject;
		uiSchemaMap.put(MappingJsonConstants.IDENTITY, null);
		Mockito.when(objectMapper.readValue(uiSchema.getBytes(), Map.class)).thenReturn(uiSchemaMap);
		testSubject = createTestSubject();
		testSubject.getUiSchemaFilteredInputAttributes("update-demographics");
	}

	@Test(expected = ResidentServiceException.class)
	public void testGetUiSchemaFilteredInputAttributesWithException() throws Exception {
		ResidentConfigServiceImpl testSubject;
		Mockito.when(objectMapper.readValue(uiSchema.getBytes(), Map.class)).thenThrow(new IOException());
		testSubject = createTestSubject();
		testSubject.getUiSchemaFilteredInputAttributes("update-demographics");
	}

	@Test
	public void testGetSharableAttributesList() throws Exception {
		ResidentConfigServiceImpl testSubject;
		testSubject = createTestSubject();
		getIdentityMappingMap(testSubject);
		Mockito.when(env.getProperty(Mockito.anyString())).thenReturn(ResidentConstants.ATTRIBUTE_NAME);
		SharableAttributesDTO sharableAttributesDTO1 = new SharableAttributesDTO();
		sharableAttributesDTO1.setAttributeName("name");
		sharableAttributesDTO1.setFormat("fullName");
		SharableAttributesDTO sharableAttributesDTO2 = new SharableAttributesDTO();
		sharableAttributesDTO2.setAttributeName("uin");
		SharableAttributesDTO sharableAttributesDTO3 = new SharableAttributesDTO();
		sharableAttributesDTO3.setAttributeName("dob");
		sharableAttributesDTO3.setFormat("");
		List<SharableAttributesDTO> sharableAttrList = new ArrayList<>();
		sharableAttrList.add(sharableAttributesDTO1);
		sharableAttrList.add(sharableAttributesDTO2);
		sharableAttrList.add(sharableAttributesDTO3);
		testSubject.getSharableAttributesList(sharableAttrList, "update-demographics");
	}

	@Test
	public void testGetUISchemaCacheableData() throws Exception {
		ResidentConfigServiceImpl testSubject;
		testSubject = createTestSubject();
		testSubject.getUISchemaCacheableData("update-demographics");
	}
}
