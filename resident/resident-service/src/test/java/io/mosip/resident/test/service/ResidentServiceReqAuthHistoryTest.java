package io.mosip.resident.test.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.resident.dto.AuthHistoryRequestDTO;
import io.mosip.resident.dto.AuthTxnDetailsDTO;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.AuditUtil;
@RunWith(SpringRunner.class)
public class ResidentServiceReqAuthHistoryTest {
	@InjectMocks
	ResidentServiceImpl residentServiceImpl;

	@Mock
	private IdAuthService idAuthService;

	@Mock
	private AuditUtil audit;
	
	@Mock
	NotificationService notificationService;
	List<AuthTxnDetailsDTO> details=null;
	@Before
	public void setup() throws ApisResourceAccessException, ResidentServiceCheckedException, OtpValidationFailedException {
		AuthTxnDetailsDTO dto=new AuthTxnDetailsDTO();
		dto.setAuthModality("OTP_AUTH");
		details=new ArrayList<>();
		details.add(dto);
		Mockito.when(idAuthService.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(true);
		Mockito.when(idAuthService.getAuthHistoryDetails(Mockito.anyString(), Mockito.any(), Mockito.any()))
				.thenReturn(details);
		Mockito.when(notificationService.sendNotification(Mockito.any())).thenReturn(mock(NotificationResponseDTO.class));
		Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());
	}
	@Test
	public void testReqAuthHistorySuccess() throws  ResidentServiceCheckedException {
		AuthHistoryRequestDTO dto=new AuthHistoryRequestDTO();
		dto.setOtp("1235");
		dto.setTransactionID("1234567890");
		dto.setIndividualId("123456789");
		
		assertEquals("OTP_AUTH", residentServiceImpl.reqAuthHistory(dto).getAuthHistory().get(0).getAuthModality());
	}
	@Test
	public void testReqAuthHistorywithVID() throws  ResidentServiceCheckedException {
		AuthHistoryRequestDTO dto=new AuthHistoryRequestDTO();
		dto.setOtp("1235");
		dto.setTransactionID("1234567890");
		dto.setIndividualId("123456789");
		
		assertEquals("OTP_AUTH", residentServiceImpl.reqAuthHistory(dto).getAuthHistory().get(0).getAuthModality());
	}
	@Test(expected=ResidentServiceException.class)
	public void testReqAuthHistoryNull() throws  ApisResourceAccessException, ResidentServiceCheckedException {
		Mockito.when(idAuthService.getAuthHistoryDetails(Mockito.anyString(), Mockito.any(), Mockito.any()))
				.thenReturn(null);
		AuthHistoryRequestDTO dto=new AuthHistoryRequestDTO();
		dto.setOtp("1235");
		dto.setTransactionID("1234567890");
		dto.setIndividualId("123456789");
		
		residentServiceImpl.reqAuthHistory(dto);
	}
	@Test(expected=ResidentServiceException.class)
	public void testReqAuthHistoryDetailsFetchFailed() throws  ApisResourceAccessException, ResidentServiceCheckedException {
		Mockito.when(idAuthService.getAuthHistoryDetails(Mockito.anyString(), Mockito.any(), Mockito.any()))
				.thenThrow(new ApisResourceAccessException());
		AuthHistoryRequestDTO dto=new AuthHistoryRequestDTO();
		dto.setOtp("1235");
		dto.setTransactionID("1234567890");
		dto.setIndividualId("123456789");
		residentServiceImpl.reqAuthHistory(dto);
	}
	@Test(expected=ResidentServiceException.class)
	public void testReqAuthHistorySendNotificationFailed() throws  ApisResourceAccessException, ResidentServiceCheckedException {
		Mockito.when(notificationService.sendNotification(Mockito.any())).thenThrow(new ResidentServiceCheckedException());
		AuthHistoryRequestDTO dto=new AuthHistoryRequestDTO();
		dto.setOtp("1235");
		dto.setTransactionID("1234567890");
		dto.setIndividualId("123456789");
		residentServiceImpl.reqAuthHistory(dto);
	}
	
	@Test(expected=ResidentServiceException.class)
	public void testReqAuthHistoryinotpvalidationfailed() throws  ApisResourceAccessException, ResidentServiceCheckedException, OtpValidationFailedException {
		Mockito.when(idAuthService.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(false);
		AuthHistoryRequestDTO dto=new AuthHistoryRequestDTO();
		dto.setOtp("1235");
		dto.setTransactionID("1234567890");
		dto.setIndividualId("123456789");
		residentServiceImpl.reqAuthHistory(dto);
	}
	@Test(expected=ResidentServiceException.class)
	public void testReqAuthHistoryinotpvalidationException() throws OtpValidationFailedException, ApisResourceAccessException, ResidentServiceCheckedException {
		Mockito.when(idAuthService.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenThrow(new OtpValidationFailedException());
		AuthHistoryRequestDTO dto=new AuthHistoryRequestDTO();
		dto.setOtp("1235");
		dto.setTransactionID("1234567890");
		dto.setIndividualId("123456789");
		residentServiceImpl.reqAuthHistory(dto);
	}
}
