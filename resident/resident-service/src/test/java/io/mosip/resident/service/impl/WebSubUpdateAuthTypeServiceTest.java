package io.mosip.resident.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.dto.NotificationRequestDtoV2;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.util.Utility;

@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class WebSubUpdateAuthTypeServiceTest {

	@InjectMocks
	private WebSubUpdateAuthTypeServiceImpl webSubUpdateAuthTypeService;

	@Mock
	private ResidentTransactionRepository residentTransactionRepository;

	@Mock
	private NotificationService notificationService;

	@Mock
	private Utility utility;

	private NotificationResponseDTO notificationResponseDTO;

	private String partnerId;

	@Before
	public void setup() throws ApisResourceAccessException, ResidentServiceCheckedException {
		MockitoAnnotations.initMocks(this);
		notificationResponseDTO = new NotificationResponseDTO();
		notificationResponseDTO.setStatus("Notification success");
		partnerId = "mpartner-default-auth";
		ReflectionTestUtils.setField(webSubUpdateAuthTypeService, "onlineVerificationPartnerId", partnerId);
	}

	@Test
	public void testUpdateAuthTypeStatus_Success() throws Exception {
		// Mock data
		Map<String, Object> eventModel = new HashMap<>();
		eventModel.put(ResidentConstants.EVENT, getMockEventMap());

		// Mock repository response
		ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
		residentTransactionEntity.setOlvPartnerId(partnerId);
		residentTransactionEntity.setEventId("12454578458478547");
		residentTransactionEntity.setIndividualId("4515452565");
		when(residentTransactionRepository.findByRequestTrnId("12345")).thenReturn(List.of(residentTransactionEntity));

		// Mock utility response
		when(utility.getSessionUserName()).thenReturn("testUser");

		// Invoke the method
		webSubUpdateAuthTypeService.updateAuthTypeStatus(eventModel);

		// Verify that the expected methods were called
		verify(residentTransactionRepository, times(1)).findByRequestTrnId("12345");
		verify(notificationService, times(1)).sendNotification(any(NotificationRequestDtoV2.class), Mockito.nullable(Map.class));
	}

	@Test
	public void testUpdateAuthTypeStatus_Failure() throws Exception {
		// Mock data
		Map<String, Object> eventModel = new HashMap<>();
		eventModel.put(ResidentConstants.EVENT, getMockEventMap());

		// Mock repository response
		when(residentTransactionRepository.findByRequestTrnId("12345")).thenThrow(new RuntimeException());

		// Invoke the method
		webSubUpdateAuthTypeService.updateAuthTypeStatus(eventModel);
	}

	private Map<String, Object> getMockEventMap() {
		Map<String, Object> eventMap = new HashMap<>();
		Map<String, Object> dataMap = new HashMap<>();
		List<Map<String, Object>> authTypesList = new ArrayList<>();
		Map<String, Object> authTypeStatus = new HashMap<>();
		authTypeStatus.put(ResidentConstants.REQUEST_ID, "12345");
		authTypesList.add(authTypeStatus);
		dataMap.put(ResidentConstants.AUTH_TYPES, authTypesList);
		eventMap.put(ResidentConstants.DATA, dataMap);
		return eventMap;
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testUpdateAuthTypeStatusWithException() throws Exception {
		// Mock data
		Map<String, Object> eventModel = new HashMap<>();
		eventModel.put(ResidentConstants.EVENT, getMockEventMap());

		// Mock repository response
		ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
		residentTransactionEntity.setOlvPartnerId(partnerId);
		residentTransactionEntity.setEventId("12454578458478547");
		residentTransactionEntity.setIndividualId("4515452565");
		when(residentTransactionRepository.findByRequestTrnId("12345")).thenReturn(List.of(residentTransactionEntity));

		// Mock utility response
		when(utility.getSessionUserName()).thenReturn("testUser");

		when(notificationService.sendNotification(any(), Mockito.nullable(Map.class))).thenThrow(new ResidentServiceCheckedException());
		// Invoke the method
		webSubUpdateAuthTypeService.updateAuthTypeStatus(eventModel);
	}

	@Test
	public void testUpdateAuthTypeStatusWithEmptyEntity() throws Exception {
		// Mock data
		Map<String, Object> eventModel = new HashMap<>();
		eventModel.put(ResidentConstants.EVENT, getMockEventMap());

		// Mock repository response
		when(residentTransactionRepository.findByRequestTrnId("12345")).thenReturn(List.of());

		// Invoke the method
		webSubUpdateAuthTypeService.updateAuthTypeStatus(eventModel);
	}
}