package io.mosip.resident.service.impl;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.MappingJsonConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.util.ResidentServiceRestClient;
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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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

	@Mock
	private ResidentConfigServiceImpl residentConfigService;

	@Mock
	private ObjectMapper mapper;

	@SuppressWarnings("unchecked")
	@Test
	public void testGetRemainingUpdateCountByIndividualId()
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setVersion("v1");
		responseWrapper.setId("1");
		when(identityServiceImpl.getResidentIndvidualIdFromSession()).thenReturn("8251649601");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), (List<String>) any(), any(), any()))
				.thenReturn(responseWrapper);
		ResponseWrapper<?> response1 = service.getRemainingUpdateCountByIndividualId(List.of("name", "gender"));
		assertNotNull(response1);
		responseWrapper.setErrors(null);
		ResponseWrapper<?> response2 = service.getRemainingUpdateCountByIndividualId(List.of("name", "gender"));
		assertNotNull(response2);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetRemainingUpdateCountByIndividualIdException()
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		when(identityServiceImpl.getResidentIndvidualIdFromSession()).thenReturn("8251649601");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), (List<String>) any(), any(), any()))
				.thenThrow(new ApisResourceAccessException());
		service.getRemainingUpdateCountByIndividualId(List.of());
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetRemainingUpdateCountByIndividualIdIf()
			throws ResidentServiceCheckedException, ApisResourceAccessException, JsonParseException, JsonMappingException, IOException {
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		ServiceError error = new ServiceError();
		error.setErrorCode(ResidentErrorCode.NO_RECORDS_FOUND.getErrorCode());
		error.setMessage(ResidentErrorCode.NO_RECORDS_FOUND.getErrorMessage());
		responseWrapper.setErrors(List.of(error));
		when(identityServiceImpl.getResidentIndvidualIdFromSession()).thenReturn("8251649601");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), (List<String>) any(), any(), any()))
				.thenReturn(responseWrapper);
		when(residentConfigService.getIdentityMapping()).thenReturn("{ \"fullName\": \"3\" }");
		when(mapper.readValue("{ \"fullName\": \"3\" }".getBytes(), Map.class)).thenReturn(Map.of(MappingJsonConstants.ATTRIBUTE_UPDATE_COUNT_LIMIT, Map.of("fullName", 3)));
		service.getRemainingUpdateCountByIndividualId(null);
		service.getRemainingUpdateCountByIndividualId(List.of());
		service.getRemainingUpdateCountByIndividualId(List.of("fullName"));
	}
}
