package io.mosip.resident.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.util.*;
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

	@Mock
	private Utility utility;

	@Mock
	private IdentityDataUtil identityDataUtil;

	@InjectMocks
	private ProxyPartnerManagementService proxyPartnerManagementService = new ProxyPartnerManagementServiceImpl();

	private ResponseWrapper responseWrapper;

	@InjectMocks
	private GetPartnersByPartnerType getPartnersByPartnerType;

	@Mock
	private GetPartnersByPartnerTypeCache getPartnersByPartnerTypeCache;

	@Before
	public void setUp() throws Exception {
		Map partnerMap=new HashMap<>();
		partnerMap.put("partnerID", "2345671");
		responseWrapper = new ResponseWrapper<>();
		responseWrapper.setVersion("v1");
		responseWrapper.setId("1");
		responseWrapper.setResponse(Map.of("partners",List.of(partnerMap)));
		when(getPartnersByPartnerTypeCache.getPartnersByPartnerType(any(), any()))
				.thenReturn(responseWrapper);
	}

	@Test
	public void testGetPartnersByPartnerType() throws ApisResourceAccessException, ResidentServiceCheckedException {
		responseWrapper.setErrors(null);
		ResponseWrapper<?> result = proxyPartnerManagementService
				.getPartnersByPartnerType("Device_Provider");
		assertNotNull(result);
	}
	
	@Test
	public void testGetPartnersByPartnerTypeIf() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResponseWrapper<?> result = proxyPartnerManagementService
				.getPartnersByPartnerType("");
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
		when(getPartnersByPartnerTypeCache.getPartnersByPartnerType(any(), any()))
				.thenThrow(new ResidentServiceCheckedException());

		responseWrapper.setErrors(errorList);
		proxyPartnerManagementService.getPartnersByPartnerType("Device_Provider");
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetPartnersByPartnerTypeWithApisResourceAccessException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(getPartnersByPartnerTypeCache.getPartnersByPartnerType(any(), any()))
				.thenThrow(new ResidentServiceCheckedException());
		proxyPartnerManagementService.getPartnersByPartnerType("Device_Provider");
	}
	
	@Test
	public void testGetPartnerDetailFromPartnerId() throws ResidentServiceCheckedException {
		Map<String, ?> result = proxyPartnerManagementService.getPartnerDetailFromPartnerIdAndPartnerType("2345671", "Auth");
		assertEquals("2345671", result.get("partnerID"));
	}

	@Test(expected = ResidentServiceException.class)
	public void testGetPartnerDetailFromPartnerIdException() throws ResidentServiceCheckedException, ApisResourceAccessException {
		when(getPartnersByPartnerTypeCache.getPartnersByPartnerType(any(), any()))
				.thenThrow(new ResidentServiceCheckedException(ResidentErrorCode.PARTNER_SERVICE_EXCEPTION));
		proxyPartnerManagementService.getPartnerDetailFromPartnerIdAndPartnerType("", "Auth");
	}

	@Test
	public void testGetPartnersByPartnerTypeV2() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setErrors(new ArrayList<>());
		responseWrapper.setId("https://example.org/example");
		responseWrapper.setMetadata("Metadata");
		responseWrapper.setResponse("Response");
		responseWrapper.setResponsetime(LocalDateTime.of(1, 1, 1, 1, 1));
		responseWrapper.setVersion("https://example.org/example");
		when(residentServiceRestClient.getApi((ApiName) any(), (List<String>) any(), (List<String>) any(),
				(List<Object>) any(), (Class<Object>) any())).thenReturn(responseWrapper);
		assertSame(responseWrapper,
				getPartnersByPartnerType.getPartnersByPartnerType(Optional.of("42"), ApiName.PARTNER_API_URL));
		verify(residentServiceRestClient).getApi((ApiName) any(), (List<String>) any(), (List<String>) any(),
				(List<Object>) any(), (Class<Object>) any());
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetPartnersByPartnerType3() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResponseWrapper<Object> responseWrapper = (ResponseWrapper<Object>) mock(ResponseWrapper.class);
		when(responseWrapper.getErrors()).thenReturn(List.of(new ServiceError(ResidentErrorCode.PARTNER_SERVICE_EXCEPTION.getErrorCode(),
				ResidentErrorCode.PARTNER_SERVICE_EXCEPTION.getErrorMessage())));
		when(residentServiceRestClient.getApi((ApiName) any(), (List<String>) any(), (List<String>) any(),
				(List<Object>) any(), (Class<Object>) any())).thenReturn(responseWrapper);
		getPartnersByPartnerType.getPartnersByPartnerType(Optional.of("42"), ApiName.PARTNER_API_URL);
		verify(residentServiceRestClient).getApi((ApiName) any(), (List<String>) any(), (List<String>) any(),
				(List<Object>) any(), (Class<Object>) any());
		verify(responseWrapper, atLeast(1)).getErrors();
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetPartnersByPartnerType4() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResponseWrapper<Object> responseWrapper = (ResponseWrapper<Object>) mock(ResponseWrapper.class);
		when(residentServiceRestClient.getApi((ApiName) any(), (List<String>) any(), (List<String>) any(),
				(List<Object>) any(), (Class<Object>) any())).thenThrow(new ApisResourceAccessException());
		getPartnersByPartnerType.getPartnersByPartnerType(Optional.of("42"), ApiName.PARTNER_API_URL);
		verify(residentServiceRestClient).getApi((ApiName) any(), (List<String>) any(), (List<String>) any(),
				(List<Object>) any(), (Class<Object>) any());
		verify(responseWrapper, atLeast(1)).getErrors();
	}

}
