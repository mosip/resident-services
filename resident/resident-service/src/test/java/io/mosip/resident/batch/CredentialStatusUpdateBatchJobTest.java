//package io.mosip.resident.batch;
//
//import static io.mosip.resident.constant.CredentialUpdateStatus.NEW;
//import static org.junit.Assert.assertEquals;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyList;
//import static org.mockito.Mockito.when;
//
//import java.util.List;
//import java.util.Map;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.core.env.Environment;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.TestContext;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.util.ReflectionTestUtils;
//import org.springframework.web.context.WebApplicationContext;
//
//import io.mosip.kernel.core.http.ResponseWrapper;
//import io.mosip.resident.CredentialStatusUpdateBatchJob;
//import io.mosip.resident.controller.ResidentController;
//import io.mosip.resident.entity.ResidentTransactionEntity;
//import io.mosip.resident.exception.ApisResourceAccessException;
//import io.mosip.resident.exception.ResidentServiceCheckedException;
//import io.mosip.resident.repository.ResidentTransactionRepository;
//import io.mosip.resident.service.NotificationService;
//import io.mosip.resident.util.ResidentServiceRestClient;
//
//@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
//@RunWith(SpringRunner.class)
//@WebMvcTest
//public class CredentialStatusUpdateBatchJobTest {
//	
//	@InjectMocks
//	private CredentialStatusUpdateBatchJob job;
//
//	@Mock
//	private ResidentTransactionRepository repo;
//
//	@Mock
//	private ResidentServiceRestClient residentServiceRestClient;
//
//	@Mock
//	private ResidentController residentController;
//
//	@Autowired
//	private Environment env;
//
//	@Mock
//	private NotificationService notificationService;
//	
//	@Before
//	public void init() {
//		ReflectionTestUtils.setField(job, "env", env);
//	}
//	
//	@Test
//	public void testTrackAndUpdateNewOrIssuedStatusWithoutRID() throws ResidentServiceCheckedException, ApisResourceAccessException {
//		ArgumentCaptor<ResidentTransactionEntity> argCaptor = ArgumentCaptor.forClass(ResidentTransactionEntity.class);
//		ResidentTransactionEntity txn = new ResidentTransactionEntity();
//		txn.setAid("aid");
//		txn.setEventId("eventId");
//		txn.setRefIdType("UIN");
//		txn.setStatusCode(NEW);
//		ResponseWrapper<Map<String, String>> responseWrapper = new ResponseWrapper<>();
//		responseWrapper.setResponse(Map.of("requestId", "requestId", "id", "id", "statusCode", "statusCode", "url", "url"));
//		when(residentServiceRestClient.getApi(any(), anyList(), anyList(), anyList(), any())).thenReturn(responseWrapper);
//		when(repo.findByStatusCodeIn(anyList())).thenReturn(List.of(txn));
//		job.scheduleCredentialStatusUpdateJob();
//		ResidentTransactionEntity capturedResponse = argCaptor.capture();
//		assertEquals("statusCode", capturedResponse.getStatusCode());
//		assertEquals(false, capturedResponse.isReadStatus());
//		assertEquals("RESIDENT", capturedResponse.getUpdBy());
//	}
//}
