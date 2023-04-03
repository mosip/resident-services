package io.mosip.resident.batch;

import static io.mosip.resident.constant.EventStatusFailure.FAILED;
import static io.mosip.resident.constant.EventStatusInProgress.IN_TRANSIT;
import static io.mosip.resident.constant.EventStatusInProgress.ISSUED;
import static io.mosip.resident.constant.EventStatusInProgress.NEW;
import static io.mosip.resident.constant.EventStatusInProgress.PAYMENT_CONFIRMED;
import static io.mosip.resident.constant.EventStatusInProgress.PRINTING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.controller.ResidentController;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.dto.RegStatusCheckResponseDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.IdentityService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ResidentService;
import io.mosip.resident.util.ResidentServiceRestClient;

/**
 * @author Kamesh Shekhar Prasad
 * This class is used to test batch job.
 */
@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class CredentialStatusUpdateBatchJobTest {

	@InjectMocks
	private CredentialStatusUpdateBatchJob job;

	@Mock
	private ResidentTransactionRepository repo;

	@Mock
	private ResidentServiceRestClient residentServiceRestClient;

	@Mock
	private ResidentController residentController;

	@Autowired
	private Environment env;

	@Mock
	private NotificationService notificationService;

	@Mock
	private IdentityService identityService;

	@Mock
	private ResidentService residentService;

	@Before
	public void init() {
		ReflectionTestUtils.setField(job, "publicUrl", "http://localhost");
		ReflectionTestUtils.setField(job, "requestTypeCodes", "VID_CARD_DOWNLOAD,ORDER_PHYSICAL_CARD");
	}

	@Test
	public void testTrackAndUpdateNewOrIssuedStatusWithoutRID() throws ResidentServiceCheckedException, ApisResourceAccessException {
		ArgumentCaptor<ResidentTransactionEntity> argCaptor = ArgumentCaptor.forClass(ResidentTransactionEntity.class);
		ResidentTransactionEntity txn = new ResidentTransactionEntity();
		txn.setAid("aid");
		txn.setEventId("eventId");
		txn.setRefIdType("UIN");
		txn.setStatusCode(NEW.name());
        txn.setRequestTypeCode(RequestType.DOWNLOAD_PERSONALIZED_CARD.name());
		txn.setCredentialRequestId(UUID.randomUUID().toString());
		ResponseWrapper<Map<String, String>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(Map.of("requestId", "requestId", "id", "id", "statusCode", "statusCode", "url", "url"));
		when(residentServiceRestClient.getApi(any(), anyList(), anyList(), anyList(), any())).thenReturn(responseWrapper);
		when(repo.findByStatusCodeInAndRequestTypeCodeInAndCredentialRequestIdIsNotNullOrderByCrDtimesAsc(anyList(), anyList())).thenReturn(List.of(txn));
		job.scheduleCredentialStatusUpdateJob();
	}

	@Test
	public void testTrackAndUpdateNewOrIssuedStatusWithoutRIDException() throws ResidentServiceCheckedException, ApisResourceAccessException {
		ArgumentCaptor<ResidentTransactionEntity> argCaptor = ArgumentCaptor.forClass(ResidentTransactionEntity.class);
		ResidentTransactionEntity txn = new ResidentTransactionEntity();
		txn.setAid("aid");
		txn.setEventId("eventId");
		txn.setRefIdType("UIN");
		txn.setStatusCode(NEW.name());
		txn.setRequestTypeCode(RequestType.DOWNLOAD_PERSONALIZED_CARD.name());
		txn.setCredentialRequestId(UUID.randomUUID().toString());
		ResponseWrapper<Map<String, String>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(Map.of("requestId", "requestId", "id", "id", "statusCode", "statusCode", "url", "url"));
		when(residentServiceRestClient.getApi(any(), anyList(), anyList(), anyList(), any())).thenThrow(new ApisResourceAccessException());
		when(repo.findByStatusCodeInAndRequestTypeCodeInAndCredentialRequestIdIsNotNullOrderByCrDtimesAsc(anyList(), anyList())).thenReturn(List.of(txn));
		job.scheduleCredentialStatusUpdateJob();
	}

    @Test
    public void testScheduleCredentialStatusUpdateJobVidCardDownload() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResidentTransactionEntity txn = new ResidentTransactionEntity();
		txn.setAid("aid");
		txn.setEventId("eventId");
		txn.setRefIdType("UIN");
		txn.setStatusCode(ISSUED.name());
		txn.setRequestTypeCode(RequestType.VID_CARD_DOWNLOAD.name());
		txn.setCredentialRequestId(UUID.randomUUID().toString());
		ResponseWrapper<Map<String, String>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(Map.of("requestId", "requestId", "id", "id", "statusCode", "statusCode", "url", "url"));
		when(residentServiceRestClient.getApi(any(), anyList(), anyList(), anyList(), any())).thenReturn(responseWrapper);
		when(repo.findByStatusCodeInAndRequestTypeCodeInAndCredentialRequestIdIsNotNullOrderByCrDtimesAsc(anyList(), anyList())).thenReturn(List.of(txn));
		job.scheduleCredentialStatusUpdateJob();
    }

	@Test
	public void testScheduleCredentialStatusUpdateJobVidCardDownloadFailed() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResidentTransactionEntity txn = new ResidentTransactionEntity();
		txn.setAid("aid");
		txn.setEventId("eventId");
		txn.setRefIdType("UIN");
		txn.setStatusCode(ISSUED.name());
		txn.setRequestTypeCode(RequestType.VID_CARD_DOWNLOAD.name());
		txn.setCredentialRequestId(UUID.randomUUID().toString());
		ResponseWrapper<Map<String, String>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setErrors(List.of(new ServiceError(ResidentErrorCode.UNKNOWN_EXCEPTION.getErrorCode(),
				ResidentErrorCode.UNKNOWN_EXCEPTION.getErrorMessage())));
		when(residentServiceRestClient.getApi(any(), anyList(), anyList(), anyList(), any())).thenReturn(responseWrapper);
		when(repo.findByStatusCodeInAndRequestTypeCodeInAndCredentialRequestIdIsNotNullOrderByCrDtimesAsc(anyList(), anyList())).thenReturn(List.of(txn));
		job.scheduleCredentialStatusUpdateJob();
	}

	@Test
	public void testScheduleCredentialStatusUpdateJobVidCardDownloadFailedApiResourceException() throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResidentTransactionEntity txn = new ResidentTransactionEntity();
		txn.setAid("aid");
		txn.setEventId("eventId");
		txn.setRefIdType("UIN");
		txn.setStatusCode(NEW.name());
		txn.setRequestTypeCode(RequestType.VID_CARD_DOWNLOAD.name());
		txn.setCredentialRequestId(UUID.randomUUID().toString());
		ResponseWrapper<Map<String, String>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(Map.of("requestId", "requestId", "id", "id", "statusCode", "statusCode", "url", "url"));
		when(residentServiceRestClient.getApi(any(), anyList(), anyList(), anyList(), any())).thenThrow(new ApisResourceAccessException());
		when(repo.findByStatusCodeInAndRequestTypeCodeInAndCredentialRequestIdIsNotNullOrderByCrDtimesAsc(anyList(), anyList())).thenReturn(List.of(txn));
		job.scheduleCredentialStatusUpdateJob();
	}

	@Test
	public void updateOrderPhysicalCardTxnStatusTest() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResidentTransactionEntity txn = new ResidentTransactionEntity();
		txn.setAid("aid");
		txn.setEventId("eventId");
		txn.setRefIdType("UIN");
		txn.setStatusCode(PAYMENT_CONFIRMED.name());
		txn.setRequestTypeCode(RequestType.ORDER_PHYSICAL_CARD.name());
		txn.setCredentialRequestId(UUID.randomUUID().toString());
		ResponseWrapper<Map<String, String>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(Map.of("requestId", "requestId", "id", "id", "statusCode", "statusCode", "url", "url"));
		when(residentServiceRestClient.getApi(any(), anyList(), anyList(), anyList(), any())).thenReturn(responseWrapper);
		when(repo.findByStatusCodeInAndRequestTypeCodeInAndCredentialRequestIdIsNotNullOrderByCrDtimesAsc(anyList(), anyList())).thenReturn(List.of(txn));
		job.scheduleCredentialStatusUpdateJob();
	}

	@Test
	public void updateOrderPhysicalCardTxnStatusCodePrintingTest() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResidentTransactionEntity txn = new ResidentTransactionEntity();
		txn.setAid("aid");
		txn.setEventId("eventId");
		txn.setRefIdType("UIN");
		txn.setStatusCode(PRINTING.name());
		txn.setRequestTrnId("123");
		txn.setIndividualId("123");
		txn.setRequestTypeCode(RequestType.ORDER_PHYSICAL_CARD.name());
		txn.setCredentialRequestId(UUID.randomUUID().toString());
		ResponseWrapper<Map<String, String>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(Map.of("requestId", "requestId", "id", "id", "statusCode", "statusCode", "url", "url"));
		when(residentServiceRestClient.getApi(any(), anyList(), anyList(), anyList(), any())).thenReturn(responseWrapper);
		when(repo.findByStatusCodeInAndRequestTypeCodeInAndCredentialRequestIdIsNotNullOrderByCrDtimesAsc(anyList(), anyList())).thenReturn(List.of(txn));
		job.scheduleCredentialStatusUpdateJob();
	}

	@Test
	public void updateOrderPhysicalCardTxnStatusCodeFailedTest() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResidentTransactionEntity txn = new ResidentTransactionEntity();
		txn.setAid("aid");
		txn.setEventId("eventId");
		txn.setRefIdType("UIN");
		txn.setStatusCode(FAILED.name());
		txn.setRequestTrnId("123");
		txn.setIndividualId("123");
		txn.setRequestTypeCode(RequestType.ORDER_PHYSICAL_CARD.name());
		txn.setCredentialRequestId(UUID.randomUUID().toString());
		ResponseWrapper<Map<String, String>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(Map.of("requestId", "requestId", "id", "id", "statusCode", FAILED.name(), "url", "url"));
		when(residentServiceRestClient.getApi(any(), anyList(), anyList(), anyList(), any())).thenReturn(responseWrapper);
		when(repo.findByStatusCodeInAndRequestTypeCodeInAndCredentialRequestIdIsNotNullOrderByCrDtimesAsc(anyList(), anyList())).thenReturn(List.of(txn));
		job.scheduleCredentialStatusUpdateJob();
	}

	@Test
	public void updateOrderPhysicalCardTxnStatusCodeFailedTestApiResourceException() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResidentTransactionEntity txn = new ResidentTransactionEntity();
		txn.setAid("aid");
		txn.setEventId("eventId");
		txn.setRefIdType("UIN");
		txn.setStatusCode(FAILED.name());
		txn.setRequestTrnId("123");
		txn.setIndividualId("123");
		txn.setRequestTypeCode(RequestType.ORDER_PHYSICAL_CARD.name());
		txn.setCredentialRequestId(UUID.randomUUID().toString());
		ResponseWrapper<Map<String, String>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(Map.of("requestId", "requestId", "id", "id", "statusCode", FAILED.name(), "url", "url"));
		when(residentServiceRestClient.getApi(any(), anyList(), anyList(), anyList(), any())).thenThrow(new ApisResourceAccessException());
		when(repo.findByStatusCodeInAndRequestTypeCodeInAndCredentialRequestIdIsNotNullOrderByCrDtimesAsc(anyList(), anyList())).thenReturn(List.of(txn));
		job.scheduleCredentialStatusUpdateJob();
	}

	@Test
	public void updateOrderPhysicalCardTxnStatusCodeFailedTest1() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResidentTransactionEntity txn = new ResidentTransactionEntity();
		txn.setAid("aid");
		txn.setEventId("eventId");
		txn.setRefIdType("UIN");
		txn.setStatusCode(IN_TRANSIT.name());
		txn.setRequestTrnId("123");
		txn.setIndividualId("123");
		txn.setRequestTypeCode(RequestType.ORDER_PHYSICAL_CARD.name());
		txn.setCredentialRequestId(UUID.randomUUID().toString());
		ResponseWrapper<Map<String, String>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setErrors(List.of(new ServiceError(ResidentErrorCode.UNKNOWN_EXCEPTION.getErrorCode(),
				ResidentErrorCode.UNKNOWN_EXCEPTION.getErrorMessage())));
		when(residentServiceRestClient.getApi(any(), anyList(), anyList(), anyList(), any())).thenReturn(responseWrapper);
		when(repo.findByStatusCodeInAndRequestTypeCodeInAndCredentialRequestIdIsNotNullOrderByCrDtimesAsc(anyList(), anyList())).thenReturn(List.of(txn));
		job.scheduleCredentialStatusUpdateJob();
	}

	@Test
	public void testUpdateShareCredentialWithPartnerTxnStatus() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResidentTransactionEntity txn = new ResidentTransactionEntity();
		txn.setAid("aid");
		txn.setEventId("eventId");
		txn.setRefIdType("UIN");
		txn.setStatusCode(NEW.name());
		txn.setRequestTrnId("123");
		txn.setIndividualId("123");
		txn.setRequestTypeCode(RequestType.SHARE_CRED_WITH_PARTNER.name());
		txn.setCredentialRequestId(UUID.randomUUID().toString());
		ResponseWrapper<Map<String, String>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(Map.of("requestId", "requestId", "id", "id", "statusCode", "statusCode", "url", "url"));
		when(residentServiceRestClient.getApi(any(), anyList(), anyList(), anyList(), any())).thenReturn(responseWrapper);
		when(repo.findByStatusCodeInAndRequestTypeCodeInAndCredentialRequestIdIsNotNullOrderByCrDtimesAsc(anyList(), anyList())).thenReturn(List.of(txn));
		job.scheduleCredentialStatusUpdateJob();
	}

	@Test
	public void testUpdateShareCredentialWithPartnerTxnStatusFailed() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResidentTransactionEntity txn = new ResidentTransactionEntity();
		txn.setAid("aid");
		txn.setEventId("eventId");
		txn.setRefIdType("UIN");
		txn.setStatusCode(FAILED.name());
		txn.setRequestTrnId("123");
		txn.setIndividualId("123");
		txn.setRequestTypeCode(RequestType.SHARE_CRED_WITH_PARTNER.name());
		txn.setCredentialRequestId(UUID.randomUUID().toString());
		ResponseWrapper<Map<String, String>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(Map.of("requestId", "requestId", "id", "id", "statusCode", FAILED.name(), "url", "url"));
		when(residentServiceRestClient.getApi(any(), anyList(), anyList(), anyList(), any())).thenReturn(responseWrapper);
		when(repo.findByStatusCodeInAndRequestTypeCodeInAndCredentialRequestIdIsNotNullOrderByCrDtimesAsc(anyList(), anyList())).thenReturn(List.of(txn));
		job.scheduleCredentialStatusUpdateJob();
	}

	@Test
	public void testUpdateShareCredentialWithPartnerTxnStatusFailedApiResourceException() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResidentTransactionEntity txn = new ResidentTransactionEntity();
		txn.setAid("aid");
		txn.setEventId("eventId");
		txn.setRefIdType("UIN");
		txn.setStatusCode(FAILED.name());
		txn.setRequestTrnId("123");
		txn.setIndividualId("123");
		txn.setRequestTypeCode(RequestType.SHARE_CRED_WITH_PARTNER.name());
		txn.setCredentialRequestId(UUID.randomUUID().toString());
		ResponseWrapper<Map<String, String>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(Map.of("requestId", "requestId", "id", "id", "statusCode", FAILED.name(), "url", "url"));
		when(residentServiceRestClient.getApi(any(), anyList(), anyList(), anyList(), any())).thenThrow(new ApisResourceAccessException());
		when(repo.findByStatusCodeInAndRequestTypeCodeInAndCredentialRequestIdIsNotNullOrderByCrDtimesAsc(anyList(), anyList())).thenReturn(List.of(txn));
		job.scheduleCredentialStatusUpdateJob();
	}

	@Test
	public void testUpdateUinDemoDataUpdateTxnStatus() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResidentTransactionEntity txn = new ResidentTransactionEntity();
		txn.setAid("aid");
		txn.setEventId("eventId");
		txn.setRefIdType("UIN");
		txn.setStatusCode(NEW.name());
		txn.setRequestTrnId("123");
		txn.setIndividualId("123");
		txn.setAid("123");
		txn.setRequestTypeCode(RequestType.UPDATE_MY_UIN.name());
		txn.setCredentialRequestId(UUID.randomUUID().toString());
		ResponseWrapper<Map<String, String>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(Map.of("requestId", "requestId", "id", "id", "statusCode", "statusCode", "url", "url"));
		when(residentServiceRestClient.getApi(any(), anyList(), anyList(), anyList(), any())).thenReturn(responseWrapper);
		IdentityDTO identityDTO = new IdentityDTO();
		identityDTO.setFullName("kamesh");
		when(identityService.getIdentity(Mockito.anyString())).thenReturn(identityDTO);
		when(repo.findByStatusCodeInAndRequestTypeCodeInAndCredentialRequestIdIsNotNullOrderByCrDtimesAsc(anyList(), anyList())).thenReturn(List.of(txn));
		job.scheduleCredentialStatusUpdateJob();
	}

	@Test
	public void testUpdateUinDemoDataUpdateTxnStatusNullAid() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResidentTransactionEntity txn = new ResidentTransactionEntity();
		txn.setAid("aid");
		txn.setEventId("eventId");
		txn.setRefIdType("UIN");
		txn.setStatusCode(NEW.name());
		txn.setRequestTrnId("123");
		txn.setIndividualId("123");
		txn.setAid("123");
		txn.setRequestTypeCode(RequestType.UPDATE_MY_UIN.name());
		txn.setCredentialRequestId(UUID.randomUUID().toString());
		ResponseWrapper<Map<String, String>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(Map.of("requestId", "requestId", "id", "id", "statusCode", "statusCode", "url", "url"));
		when(residentServiceRestClient.getApi(any(), anyList(), anyList(), anyList(), any())).thenReturn(responseWrapper);
		IdentityDTO identityDTO = new IdentityDTO();
		identityDTO.setFullName("kamesh");
		when(identityService.getIdentity(Mockito.anyString())).thenThrow(new ResidentServiceCheckedException());
		RegStatusCheckResponseDTO regStatusCheckResponseDTO = new RegStatusCheckResponseDTO();
		regStatusCheckResponseDTO.setRidStatus("123");
		when(residentService.getRidStatus(Mockito.anyString())).thenReturn(regStatusCheckResponseDTO);
		when(repo.findByStatusCodeInAndRequestTypeCodeInAndCredentialRequestIdIsNotNullOrderByCrDtimesAsc(anyList(), anyList())).thenReturn(List.of(txn));
		job.scheduleCredentialStatusUpdateJob();
	}
}
