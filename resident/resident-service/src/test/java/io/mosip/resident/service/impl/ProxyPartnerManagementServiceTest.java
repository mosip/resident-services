package io.mosip.resident.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.test.context.ContextConfiguration;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.ProxyPartnerManagementService;
import io.mosip.resident.util.ResidentServiceRestClient;

/**
 * Resident proxy partner management service test class.
 * 
 * @author Ritik Jain
 */
@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class ProxyPartnerManagementServiceTest {

	@Mock
	private ResidentServiceRestClient residentServiceRestClient;

	@InjectMocks
	private ProxyPartnerManagementService proxyPartnerManagementService = new ProxyPartnerManagementServiceImpl();

	private ResponseWrapper responseWrapper;

	@Before
	public void setUp() throws Exception {
		Map partnerMap=new HashMap<>();
		partnerMap.put("partnerID", "2345671");
		responseWrapper = new ResponseWrapper<>();
		responseWrapper.setVersion("v1");
		responseWrapper.setId("1");
		responseWrapper.setResponse(Map.of("partners",List.of(partnerMap)));
		when(residentServiceRestClient.getApi(any(), (List<String>) any(), (List<String>) any(), any(), any()))
		.thenReturn(responseWrapper);
	}

	@Test
	public void testGetPartnersByPartnerType() throws ApisResourceAccessException, ResidentServiceCheckedException {
		responseWrapper.setErrors(null);
		ResponseWrapper<?> result = proxyPartnerManagementService
				.getPartnersByPartnerType(Optional.of("Device_Provider"));
		assertNotNull(result);
	}
	
	@Test
	public void testGetPartnersByPartnerTypeIf() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResponseWrapper<?> result = proxyPartnerManagementService
				.getPartnersByPartnerType(Optional.empty());
		assertNotNull(result);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetPartnersByPartnerTypeNestedIf()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		ServiceError error = new ServiceError();
		error.setErrorCode("101");
		error.setMessage("errors");

		List<ServiceError> errorList = new ArrayList<ServiceError>();
		errorList.add(error);

		responseWrapper.setErrors(errorList);
		proxyPartnerManagementService.getPartnersByPartnerType(Optional.of("Device_Provider"));
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetPartnersByPartnerTypeWithApisResourceAccessException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentServiceRestClient.getApi(any(), (List<String>) any(), (List<String>) any(), any(), any()))
				.thenThrow(new ApisResourceAccessException());
		proxyPartnerManagementService.getPartnersByPartnerType(Optional.of("Device_Provider"));
	}
	
	@Test
	public void testGetPartnerDetailFromPartnerId() throws ResidentServiceCheckedException {
		Map<String, ?> result = proxyPartnerManagementService.getPartnerDetailFromPartnerId("2345671");
		assertEquals("2345671", result.get("partnerID"));
	}

	@Test(expected = ResidentServiceException.class)
	public void testGetPartnerDetailFromPartnerIdException() throws ResidentServiceCheckedException, ApisResourceAccessException {
		when(residentServiceRestClient.getApi(any(), (List<String>) any(), (List<String>) any(), any(), any()))
		.thenThrow(new ApisResourceAccessException());
		proxyPartnerManagementService.getPartnerDetailFromPartnerId("");
	}

}
