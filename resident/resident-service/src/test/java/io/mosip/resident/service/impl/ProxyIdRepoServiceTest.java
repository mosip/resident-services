package io.mosip.resident.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.EventStatusInProgress;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.DraftResponseDto;
import io.mosip.resident.dto.DraftUinResponseDto;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.util.AvailableClaimUtility;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.UinVidValidator;
import io.mosip.resident.util.Utility;
import io.mosip.resident.validator.RequestValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
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
	private RequestValidator requestValidator;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private Environment environment;

	@Mock
	private ResidentTransactionRepository residentTransactionRepository;

	@Mock
	private Utility utility;

	@Mock
	private ResidentServiceImpl residentService;

	@Mock
	private UinVidValidator uinVidValidator;

	@Mock
	private AvailableClaimUtility availableClaimUtility;

	@SuppressWarnings("unchecked")
	@Test
	public void testGetRemainingUpdateCountByIndividualId()
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setVersion("v1");
		responseWrapper.setId("1");
		when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("8251649601");
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
		when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("8251649601");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), (List<String>) any(), any(), any()))
				.thenThrow(new ApisResourceAccessException());
		service.getRemainingUpdateCountByIndividualId(List.of());
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetRemainingUpdateCountByIndividualIdIfIf()
			throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		ServiceError error = new ServiceError();
		error.setErrorCode("IDR-IDC-007");
		error.setMessage(ResidentErrorCode.NO_RECORDS_FOUND.getErrorMessage());
		responseWrapper.setErrors(List.of(error));
		when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("8251649601");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), (List<String>) any(), any(), any()))
				.thenReturn(responseWrapper);
		service.getRemainingUpdateCountByIndividualId(null);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetRemainingUpdateCountByIndividualIdIfElse()
			throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		ServiceError error = new ServiceError();
		error.setErrorCode(ResidentErrorCode.NO_RECORDS_FOUND.getErrorMessage());
		error.setMessage(ResidentErrorCode.NO_RECORDS_FOUND.getErrorMessage());
		responseWrapper.setErrors(List.of(error));
		when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("8251649601");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), (List<String>) any(), any(), any()))
				.thenReturn(responseWrapper);
		service.getRemainingUpdateCountByIndividualId(List.of("fullName"));
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetRemainingUpdateCountByIndividualIdErrorCodeEmpty()
			throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		ServiceError error = new ServiceError();
		error.setErrorCode("");
		responseWrapper.setErrors(List.of(error));
		when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("8251649601");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), (List<String>) any(), any(), any()))
				.thenReturn(responseWrapper);
		service.getRemainingUpdateCountByIndividualId(List.of("fullName"));
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetRemainingUpdateCountByIndividualIdErrorCodeNull()
			throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		ServiceError error = new ServiceError();
		error.setErrorCode(null);
		responseWrapper.setErrors(List.of(error));
		when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("8251649601");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), (List<String>) any(), any(), any()))
				.thenReturn(responseWrapper);
		service.getRemainingUpdateCountByIndividualId(List.of("fullName"));
	}

	@Test
	public void testGetPendingDraftsSuccess() throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<DraftResponseDto> responseWrapper = new ResponseWrapper<>();
		DraftResponseDto draftResponseDto = new DraftResponseDto();
		responseWrapper.setResponse(draftResponseDto);

		when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("123");
		when(uinVidValidator.getUinForIndividualId(Mockito.anyString())).thenReturn("123");
		when(environment.getProperty(Mockito.anyString())).thenReturn("id");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), any())).thenReturn(responseWrapper);
		when(objectMapper.convertValue((Object) any(), (Class<Object>) any())).thenReturn(draftResponseDto);
		assertNotNull(service.getPendingDrafts("eng"));
	}

	@Test(expected = InvalidInputException.class)
	public void testGetPendingDraftsFailure() throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<DraftResponseDto> responseWrapper = new ResponseWrapper<>();
		DraftResponseDto draftResponseDto = new DraftResponseDto();
		responseWrapper.setErrors(List.of(new ServiceError("IDR-IDC-002", "No Record found")));

		when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("123");
		when(uinVidValidator.validateUin(Mockito.anyString())).thenReturn(true);
		when(environment.getProperty(Mockito.anyString())).thenReturn("id");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), any())).thenReturn(responseWrapper);
		when(objectMapper.convertValue((Object) any(), (Class<Object>) any())).thenReturn(draftResponseDto);
		service.getPendingDrafts("eng");
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetPendingDraftsFailureUnknownException() throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<DraftResponseDto> responseWrapper = new ResponseWrapper<>();
		DraftResponseDto draftResponseDto = new DraftResponseDto();
		responseWrapper.setErrors(List.of(new ServiceError("IDR-IDC-003", "No Record found")));

		when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("123");
		when(uinVidValidator.validateUin(Mockito.anyString())).thenReturn(true);
		when(environment.getProperty(Mockito.anyString())).thenReturn("id");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), any())).thenReturn(responseWrapper);
		when(objectMapper.convertValue((Object) any(), (Class<Object>) any())).thenReturn(draftResponseDto);
		service.getPendingDrafts("eng");
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetPendingDraftsFailureApiResourceException() throws ResidentServiceCheckedException, ApisResourceAccessException {
		when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("123");
		when(uinVidValidator.validateUin(Mockito.anyString())).thenReturn(true);
		when(environment.getProperty(Mockito.anyString())).thenReturn("id");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), any())).thenThrow(new ApisResourceAccessException());
		service.getPendingDrafts("eng");
	}

	@Test
	public void testGetPendingDraftsSuccessWithPendingDraft() throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<DraftResponseDto> responseWrapper = new ResponseWrapper<>();
		DraftResponseDto draftResponseDto = new DraftResponseDto();
		DraftUinResponseDto draftUinResponseDto = new DraftUinResponseDto();
		draftUinResponseDto.setAttributes(List.of("PHONE"));
		draftUinResponseDto.setRid("123");
		draftUinResponseDto.setCreatedDTimes(LocalDateTime.now().toString());
		draftResponseDto.setDrafts(List.of(draftUinResponseDto));
		responseWrapper.setResponse(draftResponseDto);
		ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
		residentTransactionEntity.setStatusCode(EventStatusInProgress.NEW.name());
		residentTransactionEntity.setRequestTypeCode(RequestType.UPDATE_MY_UIN.name());
		residentTransactionEntity.setEventId("123");

		ResidentTransactionEntity residentTransactionEntity1 = new ResidentTransactionEntity();
		residentTransactionEntity1.setStatusCode(EventStatusInProgress.NEW.name());
		residentTransactionEntity1.setRequestTypeCode(RequestType.UPDATE_MY_UIN.name());
		residentTransactionEntity1.setEventId("1234");

		when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("123");
		when(uinVidValidator.getUinForIndividualId(Mockito.anyString())).thenReturn("123");
		when(environment.getProperty(Mockito.anyString())).thenReturn("id");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), any())).thenReturn(responseWrapper);
		when(objectMapper.convertValue((Object) any(), (Class<Object>) any())).thenReturn(draftResponseDto);
		when(residentTransactionRepository.findTopByAidOrderByCrDtimesDesc(Mockito.anyString())).thenReturn(residentTransactionEntity);
		when(residentTransactionRepository.findByTokenIdAndRequestTypeCodeAndStatusCode(Mockito.anyString()
		, Mockito.anyString(), Mockito.anyString())).thenReturn(List.of(residentTransactionEntity, residentTransactionEntity1));
		when(utility.createEntity(any())).thenReturn(residentTransactionEntity);
		when(availableClaimUtility.getResidentIdaToken()).thenReturn("123");
		when(residentService.getEventStatusCode(Mockito.anyString(), Mockito.anyString())).thenReturn(Tuples.of(EventStatusInProgress.NEW.name(),
				"eng"));
		assertEquals("123", service.getPendingDrafts("eng").getResponse().getDrafts().get(0).getEid());
	}

}
