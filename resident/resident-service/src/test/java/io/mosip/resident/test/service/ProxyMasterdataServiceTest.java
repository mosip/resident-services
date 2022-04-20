package io.mosip.resident.test.service;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.service.impl.ProxyMasterdataServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;

@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class ProxyMasterdataServiceTest {

	@Mock
	private Environment env;

	@Mock
	private AuditUtil auditUtil;

	@Mock
	private ResidentServiceRestClient residentServiceRestClient;

	@InjectMocks
	private ProxyMasterdataService proxyMasterdataService = new ProxyMasterdataServiceImpl();

	private ResponseWrapper<?> responseWrapper;

	@Before
	public void setup() {
		responseWrapper = new ResponseWrapper<>();
		responseWrapper.setVersion("v1");
		responseWrapper.setId("1");
	}

	@Test
	public void testGetValidDocumentByLangCode() throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), any(), any())).thenReturn(responseWrapper);
		ResponseWrapper<?> result = proxyMasterdataService.getValidDocumentByLangCode("eng");
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetValidDocumentByLangCodeIf() throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), any(), any())).thenReturn(responseWrapper);

		ServiceError error = new ServiceError();
		error.setErrorCode("101");
		error.setMessage("errors");

		List<ServiceError> errorList = new ArrayList<ServiceError>();
		errorList.add(error);

		responseWrapper.setErrors(errorList);

		proxyMasterdataService.getValidDocumentByLangCode("eng");
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetValidDocumentByLangCodeWithApisResourceAccessException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), any(), any())).thenThrow(new ApisResourceAccessException());
		proxyMasterdataService.getValidDocumentByLangCode("eng");
	}

	@Test
	public void testGetLocationHierarchyLevelByLangCode()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), any(), any())).thenReturn(responseWrapper);
		ResponseWrapper<?> result = proxyMasterdataService.getLocationHierarchyLevelByLangCode("eng");
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetLocationHierarchyLevelByLangCodeIf()
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		when(residentServiceRestClient.getApi(any(), any(), any())).thenReturn(responseWrapper);

		ServiceError error = new ServiceError();
		error.setErrorCode("101");
		error.setMessage("errors");

		List<ServiceError> errorList = new ArrayList<ServiceError>();
		errorList.add(error);

		responseWrapper.setErrors(errorList);

		proxyMasterdataService.getLocationHierarchyLevelByLangCode("eng");
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetLocationHierarchyLevelByLangCodeWithApisResourceAccessException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), any(), any())).thenThrow(new ApisResourceAccessException());
		proxyMasterdataService.getLocationHierarchyLevelByLangCode("eng");
	}

	@Test
	public void testGetImmediateChildrenByLocCodeAndLangCode()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), any(), any())).thenReturn(responseWrapper);
		ResponseWrapper<?> result = proxyMasterdataService.getImmediateChildrenByLocCodeAndLangCode("MOR", "eng");
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetImmediateChildrenByLocCodeAndLangCodeIf()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), any(), any())).thenReturn(responseWrapper);

		ServiceError error = new ServiceError();
		error.setErrorCode("101");
		error.setMessage("errors");

		List<ServiceError> errorList = new ArrayList<ServiceError>();
		errorList.add(error);

		responseWrapper.setErrors(errorList);

		proxyMasterdataService.getImmediateChildrenByLocCodeAndLangCode("MOR", "eng");
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetImmediateChildrenByLocCodeAndLangCodeWithApisResourceAccessException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), any(), any())).thenThrow(new ApisResourceAccessException());
		proxyMasterdataService.getImmediateChildrenByLocCodeAndLangCode("MOR", "eng");
	}

}
