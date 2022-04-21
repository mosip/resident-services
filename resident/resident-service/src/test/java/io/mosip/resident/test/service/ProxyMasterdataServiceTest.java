package io.mosip.resident.test.service;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.service.impl.ProxyMasterdataServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;

/**
 * Resident proxy masterdata service test class.
 * 
 * @author Ritik Jain
 */
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

	@Test
	public void testGetLocationDetailsByLocCodeAndLangCode()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), any(), any())).thenReturn(responseWrapper);
		ResponseWrapper<?> result = proxyMasterdataService.getLocationDetailsByLocCodeAndLangCode("MOR", "eng");
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetLocationDetailsByLocCodeAndLangCodeIf()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), any(), any())).thenReturn(responseWrapper);
		ServiceError error = new ServiceError();
		error.setErrorCode("101");
		error.setMessage("errors");

		List<ServiceError> errorList = new ArrayList<ServiceError>();
		errorList.add(error);

		responseWrapper.setErrors(errorList);
		proxyMasterdataService.getLocationDetailsByLocCodeAndLangCode("MOR", "eng");
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetLocationDetailsByLocCodeAndLangCodeWithApisResourceAccessException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), any(), any())).thenThrow(new ApisResourceAccessException());
		proxyMasterdataService.getLocationDetailsByLocCodeAndLangCode("MOR", "eng");
	}

	@Test
	public void testGetCoordinateSpecificRegistrationCenters()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), any(), any())).thenReturn(responseWrapper);
		ResponseWrapper<?> result = proxyMasterdataService.getCoordinateSpecificRegistrationCenters("eng", "-6.516428",
				"34.287879", "2000");
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetCoordinateSpecificRegistrationCentersIf()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), any(), any())).thenReturn(responseWrapper);
		ServiceError error = new ServiceError();
		error.setErrorCode("101");
		error.setMessage("errors");

		List<ServiceError> errorList = new ArrayList<ServiceError>();
		errorList.add(error);

		responseWrapper.setErrors(errorList);
		proxyMasterdataService.getCoordinateSpecificRegistrationCenters("eng", "-6.516428", "34.287879", "2000");
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetCoordinateSpecificRegistrationCentersWithApisResourceAccessException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), any(), any())).thenThrow(new ApisResourceAccessException());
		proxyMasterdataService.getCoordinateSpecificRegistrationCenters("eng", "-6.516428", "34.287879", "2000");
	}

	@Test
	public void testGetApplicantValidDocument() throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), (List<String>) any(), any(), any()))
				.thenReturn(responseWrapper);
		ResponseWrapper<?> result = proxyMasterdataService.getApplicantValidDocument("002", "eng");
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetApplicantValidDocumentIf() throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), (List<String>) any(), any(), any()))
				.thenReturn(responseWrapper);
		ServiceError error = new ServiceError();
		error.setErrorCode("101");
		error.setMessage("errors");

		List<ServiceError> errorList = new ArrayList<ServiceError>();
		errorList.add(error);

		responseWrapper.setErrors(errorList);
		proxyMasterdataService.getApplicantValidDocument("002", "eng");
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetApplicantValidDocumentWithApisResourceAccessException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), (List<String>) any(), any(), any()))
				.thenThrow(new ApisResourceAccessException());
		proxyMasterdataService.getApplicantValidDocument("002", "eng");
	}

	@Test
	public void testGetRegistrationCentersByHierarchyLevel()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), (List<String>) any(), any(), any()))
				.thenReturn(responseWrapper);
		ResponseWrapper<?> result = proxyMasterdataService.getRegistrationCentersByHierarchyLevel("ang", "5", "14110");
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetRegistrationCentersByHierarchyLevelIf()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), (List<String>) any(), any(), any()))
				.thenReturn(responseWrapper);
		ServiceError error = new ServiceError();
		error.setErrorCode("101");
		error.setMessage("errors");

		List<ServiceError> errorList = new ArrayList<ServiceError>();
		errorList.add(error);

		responseWrapper.setErrors(errorList);
		proxyMasterdataService.getRegistrationCentersByHierarchyLevel("ang", "5", "14110");
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetRegistrationCentersByHierarchyLevelWithApisResourceAccessException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), (List<String>) any(), any(), any()))
				.thenThrow(new ApisResourceAccessException());
		proxyMasterdataService.getRegistrationCentersByHierarchyLevel("ang", "5", "14110");
	}

	@Test
	public void testGetRegistrationCenterByHierarchyLevelAndTextPaginated()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), (List<String>) any(), any(), any()))
				.thenReturn(responseWrapper);
		ResponseWrapper<?> result = proxyMasterdataService.getRegistrationCenterByHierarchyLevelAndTextPaginated("eng",
				"5", "14110", "0", "10", "desc", "createdDateTime");
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetRegistrationCenterByHierarchyLevelAndTextPaginatedIf()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), (List<String>) any(), any(), any()))
				.thenReturn(responseWrapper);
		ServiceError error = new ServiceError();
		error.setErrorCode("101");
		error.setMessage("errors");

		List<ServiceError> errorList = new ArrayList<ServiceError>();
		errorList.add(error);

		responseWrapper.setErrors(errorList);
		proxyMasterdataService.getRegistrationCenterByHierarchyLevelAndTextPaginated("eng", "5", "14110", "0", "10",
				"desc", "createdDateTime");
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetRegistrationCenterByHierarchyLevelAndTextPaginatedWithApisResourceAccessException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), (List<String>) any(), any(), any()))
				.thenThrow(new ApisResourceAccessException());
		proxyMasterdataService.getRegistrationCenterByHierarchyLevelAndTextPaginated("eng", "5", "14110", "0", "10",
				"desc", "createdDateTime");
	}

}
