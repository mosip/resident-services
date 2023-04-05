package io.mosip.resident.service.impl;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.exception.ResidentServiceException;

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
	
	Resource resource;

	@Before
	public void setUp() throws Exception {
		Mockito.when(identityMappingJsonFile.getInputStream())
				.thenReturn(new ByteArrayInputStream("{\"name\":\"identity-mapping\"}".getBytes()));
		ReflectionTestUtils.setField(configServiceImpl, "resourceLoader", resourceLoader);
		ReflectionTestUtils.setField(configServiceImpl, "residentUiSchemaJsonFilePrefix", "classpath:resident-ui");
		resource = Mockito.mock(Resource.class);
		Mockito.when(resourceLoader.getResource(Mockito.anyString())).thenReturn(resource);
		when(resource.exists()).thenReturn(true);
		String uiSchema = "{\"name\":\"ui-schema\"}";
		when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(uiSchema.getBytes()));
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

	@Test
	public void testGetIdentityMapping() throws Exception {
		ResidentConfigServiceImpl testSubject;

		testSubject = createTestSubject();
		String identityMapping = "identity-mapping-json";
		ReflectionTestUtils.setField(testSubject, "identityMapping", identityMapping);
		String result = testSubject.getIdentityMapping();
		assertTrue(result.contains(identityMapping));
	}

	@Test
	public void testGetIdentityMappingTry() throws Exception {
		ResidentConfigServiceImpl testSubject;

		testSubject = createTestSubject();
		String identityMapping = null;
		ReflectionTestUtils.setField(testSubject, "identityMapping", identityMapping);
		identityMapping = "{\"name\":\"identity-mapping\"}";
		String result = testSubject.getIdentityMapping();
		assertTrue(result.contains(identityMapping));
	}

	@Test
	public void testGetUiSchemaFilteredInputAttributes() throws Exception{
		ResidentConfigServiceImpl testSubject;
		List<String> result;
		Map<String, Object> uiSchema = new HashMap<>();
		List<Map<String, Object>> uiSchemaInputAttributes = new ArrayList<>();
		Map<String, Object> uiSchemaInputAttribute = new HashMap<>();
		uiSchemaInputAttribute.put("inputRequired", "firstName");
		uiSchemaInputAttribute.put("controlType", "text");
		uiSchemaInputAttribute.put("id", "1234");
		uiSchemaInputAttributes.add(uiSchemaInputAttribute);
		uiSchema.put("identity", uiSchemaInputAttributes);
		byte[] src = "{\"name\":\"ui-schema\"}".getBytes();
		Mockito.when(objectMapper.readValue(src, Map.class)).thenReturn(uiSchema);
		testSubject = createTestSubject();
		result = testSubject.getUiSchemaFilteredInputAttributes("update-demographics");
		assertNotNull(result);
	}

	@Test
	public void testGetUiSchemaFilteredInputAttributesNotNull() throws Exception{
		ResidentConfigServiceImpl testSubject;
		List<String> result;
		Map<String, Object> uiSchema = new HashMap<>();
		uiSchema.put("identity", null);
		byte[] src = "{\"name\":\"ui-schema\"}".getBytes();
		Mockito.when(objectMapper.readValue(src, Map.class)).thenReturn(uiSchema);
		testSubject = createTestSubject();
		result = testSubject.getUiSchemaFilteredInputAttributes("update-demographics");
		assertNull(result);
	}
}
