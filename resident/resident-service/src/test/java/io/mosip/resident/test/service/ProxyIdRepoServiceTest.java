package io.mosip.resident.test.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.dto.UpdateCountDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ProxyIdRepoServiceImpl;
import io.mosip.resident.util.ResidentServiceRestClient;

@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
@ActiveProfiles("test")
public class ProxyIdRepoServiceTest {

	@InjectMocks
	private ProxyIdRepoServiceImpl service;

	@Mock
	private ResidentServiceRestClient residentServiceRestClient;
	
	@Mock
	private IdentityServiceImpl identityServiceImpl;

	@SuppressWarnings("unchecked")
	@Test
	public void testGetRemainingUpdateCountByIndividualId()
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<List<UpdateCountDto>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(List.of());
		when(identityServiceImpl.getResidentIndvidualId()).thenReturn("8251649601");
		when(residentServiceRestClient.getApi(any(), any(List.class), any(String.class), any(String.class), any()))
				.thenReturn(responseWrapper);
		ResponseWrapper<List<UpdateCountDto>> response = service.getRemainingUpdateCountByIndividualId("",
				List.of());
		assertEquals(List.of(), response.getResponse());
	}

	@SuppressWarnings("unchecked")
	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetRemainingUpdateCountByIndividualIdException()
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		when(identityServiceImpl.getResidentIndvidualId()).thenReturn("8251649601");
		when(residentServiceRestClient.getApi(any(), any(List.class), any(String.class), any(String.class), any()))
				.thenThrow(new ApisResourceAccessException());
		service.getRemainingUpdateCountByIndividualId("",
				List.of());
	}
}
