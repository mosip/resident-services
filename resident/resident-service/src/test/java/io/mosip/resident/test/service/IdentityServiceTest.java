package io.mosip.resident.test.service;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.test.context.ContextConfiguration;

import io.mosip.kernel.biometrics.spi.CbeffUtil;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
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

	private ResponseWrapper<?> responseWrapper;

	private Map<?, ?> map;

	@Before
	public void setUp() throws Exception {
		responseWrapper = new ResponseWrapper<>();
		responseWrapper.setVersion("v1");
		responseWrapper.setId("1");
		map = (Map<?, ?>) responseWrapper.getResponse();
	}

//	@Test
//	public void testGetIdentity() throws Exception {
//		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), anyMap(), anyList(), anyList(), any()))
//				.thenReturn(responseWrapper);
//		
//		when(cbeffUtil.getBDBBasedOnType(any(), anyString(), any())).thenReturn(new HashMap());
//		IdentityDTO result = identityService.getIdentity("6");
//		assertNotNull(result);
//	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetIdentityIf() throws Exception {
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
	public void testGetIdentityNestedCatch() throws Exception {
		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), anyMap(), anyList(), anyList(), any()))
				.thenThrow(new ApisResourceAccessException());
		identityService.getIdentity("6");
	}

}
