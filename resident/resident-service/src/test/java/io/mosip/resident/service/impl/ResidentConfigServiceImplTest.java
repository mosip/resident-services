package io.mosip.resident.service.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.core.http.ResponseWrapper;

public class ResidentConfigServiceImplTest {

	@Before
	public void setUp() throws Exception {

	}

	private ResidentConfigServiceImpl createTestSubject() {
		return new ResidentConfigServiceImpl();
	}

	@Test
	public void testGetUIProperties_emptyPropArray() throws Exception {
		ResidentConfigServiceImpl testSubject;
		ResponseWrapper<?> result;

		// default test
		testSubject = createTestSubject();
		ReflectionTestUtils.setField(testSubject, "propKeys", new String[0]);
		Environment mockEnv = Mockito.mock(Environment.class);
		when(mockEnv.getProperty("aaa.key", String.class)).thenReturn("aaa");
		when(mockEnv.getProperty("bbb.key", String.class)).thenReturn("bbb");
		ReflectionTestUtils.setField(testSubject, "env", mockEnv);
		result = testSubject.getUIProperties();
		Set resultProps  = ((Map)result.getResponse()).keySet();
		assertTrue(resultProps.size() == 0);
	}
	
	@Test
	public void testGetUIProperties_nonEmptyPropArray() throws Exception {
		ResidentConfigServiceImpl testSubject;
		ResponseWrapper<?> result;

		// default test
		testSubject = createTestSubject();
		String[] propKeys = new String[] {"aaa.key","bbb.key", "ccc.key"};
		ReflectionTestUtils.setField(testSubject, "propKeys", propKeys);
		Environment mockEnv = Mockito.mock(Environment.class);
		when(mockEnv.getProperty("aaa.key", Object.class)).thenReturn("aaa");
		when(mockEnv.getProperty("bbb.key", Object.class)).thenReturn("bbb");
		ReflectionTestUtils.setField(testSubject, "env", mockEnv);
		result = testSubject.getUIProperties();
		Set resultProps  = ((Map)result.getResponse()).keySet();
		assertTrue(resultProps.size() == 2);
		assertTrue(resultProps.contains("aaa.key"));
		assertTrue(resultProps.contains("bbb.key"));
		
	}
}