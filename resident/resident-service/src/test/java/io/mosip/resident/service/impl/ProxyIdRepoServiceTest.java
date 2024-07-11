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
import io.mosip.resident.util.*;
import io.mosip.resident.validator.RequestValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Mock
	private UinForIndividualId uinForIndividualId;

	@Mock
	private GetDescriptionForLangCode getDescriptionForLangCode;

	@InjectMocks
	private GetRemainingUpdateCountByIndividualId getRemainingUpdateCountByIndividualId;

	@InjectMocks
	private GetPendingDrafts getPendingDrafts;

	@Mock
	private GetEventStatusCode getEventStatusCode;

	@SuppressWarnings("unchecked")
	@Test
	public void testgetRemainingUpdateCountByIndividualId()
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setVersion("v1");
		responseWrapper.setId("1");
		when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("8251649601");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), (List<String>) any(), any(), any()))
				.thenReturn(responseWrapper);
		ResponseWrapper<?> response1 = getRemainingUpdateCountByIndividualId.getRemainingUpdateCountByIndividualId(List.of("name", "gender"));
		assertNotNull(response1);
		responseWrapper.setErrors(null);
		ResponseWrapper<?> response2 = getRemainingUpdateCountByIndividualId.getRemainingUpdateCountByIndividualId(List.of("name", "gender"));
		assertNotNull(response2);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = ResidentServiceCheckedException.class)
	public void testgetRemainingUpdateCountByIndividualIdException()
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("8251649601");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), (List<String>) any(), any(), any()))
				.thenThrow(new ApisResourceAccessException());
		getRemainingUpdateCountByIndividualId.getRemainingUpdateCountByIndividualId(List.of());
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testgetRemainingUpdateCountByIndividualIdIfIf()
			throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		ServiceError error = new ServiceError();
		error.setErrorCode("IDR-IDC-007");
		error.setMessage(ResidentErrorCode.NO_RECORDS_FOUND.getErrorMessage());
		responseWrapper.setErrors(List.of(error));
		when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("8251649601");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), (List<String>) any(), any(), any()))
				.thenReturn(responseWrapper);
		getRemainingUpdateCountByIndividualId.getRemainingUpdateCountByIndividualId(null);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testgetRemainingUpdateCountByIndividualIdIfElse()
			throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		ServiceError error = new ServiceError();
		error.setErrorCode(ResidentErrorCode.NO_RECORDS_FOUND.getErrorMessage());
		error.setMessage(ResidentErrorCode.NO_RECORDS_FOUND.getErrorMessage());
		responseWrapper.setErrors(List.of(error));
		when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("8251649601");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), (List<String>) any(), any(), any()))
				.thenReturn(responseWrapper);
		getRemainingUpdateCountByIndividualId.getRemainingUpdateCountByIndividualId(List.of("fullName"));
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testgetRemainingUpdateCountByIndividualIdErrorCodeEmpty()
			throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		ServiceError error = new ServiceError();
		error.setErrorCode("");
		responseWrapper.setErrors(List.of(error));
		when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("8251649601");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), (List<String>) any(), any(), any()))
				.thenReturn(responseWrapper);
		getRemainingUpdateCountByIndividualId.getRemainingUpdateCountByIndividualId(List.of("fullName"));
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testgetRemainingUpdateCountByIndividualIdErrorCodeNull()
			throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		ServiceError error = new ServiceError();
		error.setErrorCode(null);
		responseWrapper.setErrors(List.of(error));
		when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("8251649601");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), (List<String>) any(), any(), any()))
				.thenReturn(responseWrapper);
		getRemainingUpdateCountByIndividualId.getRemainingUpdateCountByIndividualId(List.of("fullName"));
	}

	@Test
	public void testgetPendingDraftsSuccess() throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<DraftResponseDto> responseWrapper = new ResponseWrapper<>();
		DraftResponseDto draftResponseDto = new DraftResponseDto();
		responseWrapper.setResponse(draftResponseDto);

		when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("123");
		when(uinForIndividualId.getUinForIndividualId(Mockito.anyString())).thenReturn("123");
		when(environment.getProperty(Mockito.anyString())).thenReturn("id");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), any())).thenReturn(responseWrapper);
		when(objectMapper.convertValue((Object) any(), (Class<Object>) any())).thenReturn(draftResponseDto);
		assertNotNull(getPendingDrafts.getPendingDrafts("eng"));
	}

	@Test(expected = InvalidInputException.class)
	public void testgetPendingDraftsFailure() throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<DraftResponseDto> responseWrapper = new ResponseWrapper<>();
		DraftResponseDto draftResponseDto = new DraftResponseDto();
		responseWrapper.setErrors(List.of(new ServiceError("IDR-IDC-002", "No Record found")));

		when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("123");
		when(uinVidValidator.validateUin(Mockito.anyString())).thenReturn(true);
		when(environment.getProperty(Mockito.anyString())).thenReturn("id");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), any())).thenReturn(responseWrapper);
		when(objectMapper.convertValue((Object) any(), (Class<Object>) any())).thenReturn(draftResponseDto);
		getPendingDrafts.getPendingDrafts("eng");
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testgetPendingDraftsFailureUnknownException() throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<DraftResponseDto> responseWrapper = new ResponseWrapper<>();
		DraftResponseDto draftResponseDto = new DraftResponseDto();
		responseWrapper.setErrors(List.of(new ServiceError("IDR-IDC-003", "No Record found")));

		when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("123");
		when(uinVidValidator.validateUin(Mockito.anyString())).thenReturn(true);
		when(environment.getProperty(Mockito.anyString())).thenReturn("id");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), any())).thenReturn(responseWrapper);
		when(objectMapper.convertValue((Object) any(), (Class<Object>) any())).thenReturn(draftResponseDto);
		getPendingDrafts.getPendingDrafts("eng");
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testgetPendingDraftsFailureApiResourceException() throws ResidentServiceCheckedException, ApisResourceAccessException {
		when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("123");
		when(uinVidValidator.validateUin(Mockito.anyString())).thenReturn(true);
		when(environment.getProperty(Mockito.anyString())).thenReturn("id");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), any())).thenThrow(new ApisResourceAccessException());
		getPendingDrafts.getPendingDrafts("eng");
	}

	@Test
	public void testgetPendingDraftsSuccessWithPendingDraft() throws ResidentServiceCheckedException, ApisResourceAccessException {
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
		when(uinForIndividualId.getUinForIndividualId(Mockito.anyString())).thenReturn("123");
		when(environment.getProperty(Mockito.anyString())).thenReturn("id");
		when(residentServiceRestClient.getApi(any(), (Map<String, String>) any(), any())).thenReturn(responseWrapper);
		when(objectMapper.convertValue((Object) any(), (Class<Object>) any())).thenReturn(draftResponseDto);
		when(residentTransactionRepository.findTopByAidOrderByCrDtimesDesc(Mockito.anyString())).thenReturn(residentTransactionEntity);
		when(residentTransactionRepository.findByTokenIdAndRequestTypeCodeAndStatusCode(Mockito.anyString()
		, Mockito.anyString(), Mockito.anyString())).thenReturn(List.of(residentTransactionEntity, residentTransactionEntity1));
		when(utility.createEntity(any())).thenReturn(residentTransactionEntity);
		when(availableClaimUtility.getResidentIdaToken()).thenReturn("123");
		when(getEventStatusCode.getEventStatusCode(Mockito.anyString(), Mockito.anyString())).thenReturn(Tuples.of(EventStatusInProgress.NEW.name(),
				"eng"));
		assertEquals("123", getPendingDrafts.getPendingDrafts("eng").getResponse().getDrafts().get(0).getEid());
	}

}
