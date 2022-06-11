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
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.OrderEnum;
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
		when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(responseWrapper);
		ResponseWrapper<?> result = proxyMasterdataService.getValidDocumentByLangCode("eng");
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetValidDocumentByLangCodeIf() throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(responseWrapper);

		ServiceError error = new ServiceError();
		error.setErrorCode("101");
		error.setMessage("errors");

		List<ServiceError> errorList = new ArrayList<ServiceError>();
		errorList.add(error);

		responseWrapper.setErrors(errorList);

		proxyMasterdataService.getValidDocumentByLangCode("eng");
	}

	@Test
	public void testGetValidDocumentByLangCodeElse()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(responseWrapper);

		responseWrapper.setErrors(null);

		ResponseWrapper<?> result = proxyMasterdataService.getValidDocumentByLangCode("eng");
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetValidDocumentByLangCodeWithApisResourceAccessException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) (ApiName) any(), any(), any()))
				.thenThrow(new ApisResourceAccessException());
		proxyMasterdataService.getValidDocumentByLangCode("eng");
	}

	@Test
	public void testGetLocationHierarchyLevelByLangCode()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) (ApiName) any(), any(), any())).thenReturn(responseWrapper);
		ResponseWrapper<?> result = proxyMasterdataService.getLocationHierarchyLevelByLangCode("eng");
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetLocationHierarchyLevelByLangCodeIf()
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		when(residentServiceRestClient.getApi((ApiName) (ApiName) any(), any(), any())).thenReturn(responseWrapper);

		ServiceError error = new ServiceError();
		error.setErrorCode("101");
		error.setMessage("errors");

		List<ServiceError> errorList = new ArrayList<ServiceError>();
		errorList.add(error);

		responseWrapper.setErrors(errorList);

		proxyMasterdataService.getLocationHierarchyLevelByLangCode("eng");
	}

	@Test
	public void testGetLocationHierarchyLevelByLangCodeElse()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) (ApiName) any(), any(), any())).thenReturn(responseWrapper);
		responseWrapper.setErrors(null);
		ResponseWrapper<?> result = proxyMasterdataService.getLocationHierarchyLevelByLangCode("eng");
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetLocationHierarchyLevelByLangCodeWithApisResourceAccessException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), any(), any()))
				.thenThrow(new ApisResourceAccessException());
		proxyMasterdataService.getLocationHierarchyLevelByLangCode("eng");
	}

	@Test
	public void testGetImmediateChildrenByLocCodeAndLangCode()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(responseWrapper);
		ResponseWrapper<?> result = proxyMasterdataService.getImmediateChildrenByLocCodeAndLangCode("MOR", "eng");
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetImmediateChildrenByLocCodeAndLangCodeIf()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(responseWrapper);

		ServiceError error = new ServiceError();
		error.setErrorCode("101");
		error.setMessage("errors");

		List<ServiceError> errorList = new ArrayList<ServiceError>();
		errorList.add(error);

		responseWrapper.setErrors(errorList);

		proxyMasterdataService.getImmediateChildrenByLocCodeAndLangCode("MOR", "eng");
	}

	@Test
	public void testGetImmediateChildrenByLocCodeAndLangCodeElse()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(responseWrapper);
		responseWrapper.setErrors(null);
		ResponseWrapper<?> result = proxyMasterdataService.getImmediateChildrenByLocCodeAndLangCode("MOR", "eng");
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetImmediateChildrenByLocCodeAndLangCodeWithApisResourceAccessException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), any(), any()))
				.thenThrow(new ApisResourceAccessException());
		proxyMasterdataService.getImmediateChildrenByLocCodeAndLangCode("MOR", "eng");
	}

	@Test
	public void testGetLocationDetailsByLocCodeAndLangCode()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(responseWrapper);
		ResponseWrapper<?> result = proxyMasterdataService.getLocationDetailsByLocCodeAndLangCode("MOR", "eng");
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetLocationDetailsByLocCodeAndLangCodeIf()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(responseWrapper);
		ServiceError error = new ServiceError();
		error.setErrorCode("101");
		error.setMessage("errors");

		List<ServiceError> errorList = new ArrayList<ServiceError>();
		errorList.add(error);

		responseWrapper.setErrors(errorList);
		proxyMasterdataService.getLocationDetailsByLocCodeAndLangCode("MOR", "eng");
	}

	@Test
	public void testGetLocationDetailsByLocCodeAndLangCodeElse()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(responseWrapper);
		responseWrapper.setErrors(null);
		ResponseWrapper<?> result = proxyMasterdataService.getLocationDetailsByLocCodeAndLangCode("MOR", "eng");
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetLocationDetailsByLocCodeAndLangCodeWithApisResourceAccessException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), any(), any()))
				.thenThrow(new ApisResourceAccessException());
		proxyMasterdataService.getLocationDetailsByLocCodeAndLangCode("MOR", "eng");
	}

	@Test
	public void testGetCoordinateSpecificRegistrationCenters()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(responseWrapper);
		ResponseWrapper<?> result = proxyMasterdataService.getCoordinateSpecificRegistrationCenters("eng", -6.516428,
				34.287879, 2000);
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetCoordinateSpecificRegistrationCentersIf()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(responseWrapper);
		ServiceError error = new ServiceError();
		error.setErrorCode("101");
		error.setMessage("errors");

		List<ServiceError> errorList = new ArrayList<ServiceError>();
		errorList.add(error);

		responseWrapper.setErrors(errorList);
		proxyMasterdataService.getCoordinateSpecificRegistrationCenters("eng", -6.516428, 34.287879, 2000);
	}

	@Test
	public void testGetCoordinateSpecificRegistrationCentersElse()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(responseWrapper);
		responseWrapper.setErrors(null);
		ResponseWrapper<?> result = proxyMasterdataService.getCoordinateSpecificRegistrationCenters("eng", -6.516428,
				34.287879, 2000);
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetCoordinateSpecificRegistrationCentersWithApisResourceAccessException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), any(), any()))
				.thenThrow(new ApisResourceAccessException());
		proxyMasterdataService.getCoordinateSpecificRegistrationCenters("eng", -6.516428, 34.287879, 2000);
	}

	@Test
	public void testGetApplicantValidDocument() throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), (Map<String, String>) any(), (List<String>) any(), any(),
				any())).thenReturn(responseWrapper);
		ResponseWrapper<?> result = proxyMasterdataService.getApplicantValidDocument("002", List.of("eng", "ara"));
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetApplicantValidDocumentIf() throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), (Map<String, String>) any(), (List<String>) any(), any(),
				any())).thenReturn(responseWrapper);
		ServiceError error = new ServiceError();
		error.setErrorCode("101");
		error.setMessage("errors");

		List<ServiceError> errorList = new ArrayList<ServiceError>();
		errorList.add(error);

		responseWrapper.setErrors(errorList);
		proxyMasterdataService.getApplicantValidDocument("002", List.of("eng", "ara"));
	}

	@Test
	public void testGetApplicantValidDocumentElse()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), (Map<String, String>) any(), (List<String>) any(), any(),
				any())).thenReturn(responseWrapper);
		responseWrapper.setErrors(null);
		ResponseWrapper<?> result = proxyMasterdataService.getApplicantValidDocument("002", List.of("eng", "ara"));
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetApplicantValidDocumentWithApisResourceAccessException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), (Map<String, String>) any(), (List<String>) any(), any(),
				any())).thenThrow(new ApisResourceAccessException());
		proxyMasterdataService.getApplicantValidDocument("002", List.of("eng", "ara"));
	}

	@Test
	public void testGetRegistrationCentersByHierarchyLevel()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), (Map<String, String>) any(), (List<String>) any(), any(),
				any())).thenReturn(responseWrapper);
		ResponseWrapper<?> result = proxyMasterdataService.getRegistrationCentersByHierarchyLevel("eng", (short) 5,
				List.of("14110", "14080"));
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetRegistrationCentersByHierarchyLevelIf()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), (Map<String, String>) any(), (List<String>) any(), any(),
				any())).thenReturn(responseWrapper);
		ServiceError error = new ServiceError();
		error.setErrorCode("101");
		error.setMessage("errors");

		List<ServiceError> errorList = new ArrayList<ServiceError>();
		errorList.add(error);

		responseWrapper.setErrors(errorList);
		proxyMasterdataService.getRegistrationCentersByHierarchyLevel("eng", (short) 5, List.of("14110", "14080"));
	}

	@Test
	public void testGetRegistrationCentersByHierarchyLevelElse()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), (Map<String, String>) any(), (List<String>) any(), any(),
				any())).thenReturn(responseWrapper);
		responseWrapper.setErrors(null);
		ResponseWrapper<?> result = proxyMasterdataService.getRegistrationCentersByHierarchyLevel("eng", (short) 5,
				List.of("14110", "14080"));
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetRegistrationCentersByHierarchyLevelWithApisResourceAccessException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), (Map<String, String>) any(), (List<String>) any(), any(),
				any())).thenThrow(new ApisResourceAccessException());
		proxyMasterdataService.getRegistrationCentersByHierarchyLevel("eng", (short) 5, List.of("14110", "14080"));
	}

	@Test
	public void testGetRegistrationCenterByHierarchyLevelAndTextPaginated()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), (Map<String, String>) any(), (List<String>) any(), any(),
				any())).thenReturn(responseWrapper);
		ResponseWrapper<?> result = proxyMasterdataService.getRegistrationCenterByHierarchyLevelAndTextPaginated("eng",
				(short) 5, "14110", 0, 10, OrderEnum.desc, "createdDateTime");
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetRegistrationCenterByHierarchyLevelAndTextPaginatedIf()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), (Map<String, String>) any(), (List<String>) any(), any(),
				any())).thenReturn(responseWrapper);
		ServiceError error = new ServiceError();
		error.setErrorCode("101");
		error.setMessage("errors");

		List<ServiceError> errorList = new ArrayList<ServiceError>();
		errorList.add(error);

		responseWrapper.setErrors(errorList);
		proxyMasterdataService.getRegistrationCenterByHierarchyLevelAndTextPaginated("eng", (short) 5, "14110", 0, 10,
				OrderEnum.desc, "createdDateTime");
	}

	@Test
	public void testGetRegistrationCenterByHierarchyLevelAndTextPaginatedElse()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), (Map<String, String>) any(), (List<String>) any(), any(),
				any())).thenReturn(responseWrapper);
		responseWrapper.setErrors(null);
		ResponseWrapper<?> result = proxyMasterdataService.getRegistrationCenterByHierarchyLevelAndTextPaginated("eng",
				(short) 5, "14110", 0, 10, OrderEnum.desc, "createdDateTime");
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetRegistrationCenterByHierarchyLevelAndTextPaginatedWithApisResourceAccessException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), (Map<String, String>) any(), (List<String>) any(), any(),
				any())).thenThrow(new ApisResourceAccessException());
		proxyMasterdataService.getRegistrationCenterByHierarchyLevelAndTextPaginated("eng", (short) 5, "14110", 0, 10,
				OrderEnum.desc, "createdDateTime");
	}

	@Test
	public void testGetRegistrationCenterWorkingDays()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(responseWrapper);
		ResponseWrapper<?> result = proxyMasterdataService.getRegistrationCenterWorkingDays("10002", "eng");
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetRegistrationCenterWorkingDaysIf()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(responseWrapper);
		ServiceError error = new ServiceError();
		error.setErrorCode("101");
		error.setMessage("errors");

		List<ServiceError> errorList = new ArrayList<ServiceError>();
		errorList.add(error);

		responseWrapper.setErrors(errorList);
		proxyMasterdataService.getRegistrationCenterWorkingDays("10002", "eng");
	}

	@Test
	public void testGetRegistrationCenterWorkingDaysElse()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(responseWrapper);
		responseWrapper.setErrors(null);
		ResponseWrapper<?> result = proxyMasterdataService.getRegistrationCenterWorkingDays("10002", "eng");
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetRegistrationCenterWorkingDaysWithApisResourceAccessException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), any(), any()))
				.thenThrow(new ApisResourceAccessException());
		proxyMasterdataService.getRegistrationCenterWorkingDays("10002", "eng");
	}

	@Test
	public void testGetLatestIdSchema() throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), (List<String>) any(), (List<String>) any(), any(),
				any())).thenReturn(responseWrapper);
		ResponseWrapper<?> result = proxyMasterdataService.getLatestIdSchema(0, "domain", "type");
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetLatestIdSchemaIf() throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), (List<String>) any(), (List<String>) any(), any(),
				any())).thenReturn(responseWrapper);
		ServiceError error = new ServiceError();
		error.setErrorCode("101");
		error.setMessage("errors");

		List<ServiceError> errorList = new ArrayList<ServiceError>();
		errorList.add(error);

		responseWrapper.setErrors(errorList);
		proxyMasterdataService.getLatestIdSchema(0, "domain", "type");
	}

	@Test
	public void testGetLatestIdSchemaElse() throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), (List<String>) any(), (List<String>) any(), any(),
				any())).thenReturn(responseWrapper);
		responseWrapper.setErrors(null);
		ResponseWrapper<?> result = proxyMasterdataService.getLatestIdSchema(0, "domain", "type");
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetLatestIdSchemaWithApisResourceAccessException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi((ApiName) any(), (List<String>) any(), (List<String>) any(), any(),
				any())).thenThrow(new ApisResourceAccessException());
		proxyMasterdataService.getLatestIdSchema(0, "domain", "type");
	}

}
