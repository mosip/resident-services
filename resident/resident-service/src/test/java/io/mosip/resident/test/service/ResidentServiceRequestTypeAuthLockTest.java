package io.mosip.resident.test.service;

import io.mosip.kernel.core.idvalidator.spi.RidValidator;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDtoV2;
import io.mosip.resident.dto.AuthTypeStatusDto;
import io.mosip.resident.dto.AuthTypeStatusDtoV2;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.dto.ResponseDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.PartnerService;
import io.mosip.resident.service.ResidentService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.PartnerServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.UINCardDownloadService;
import io.mosip.resident.util.Utilitiy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class ResidentServiceRequestTypeAuthLockTest {

	@Mock
	private VidValidator<String> vidValidator;

	@Mock
	private UinValidator<String> uinValidator;

	@Mock
	private RidValidator<String> ridValidator;

	@Mock
	private UINCardDownloadService uinCardDownloadService;

	@Mock
	private IdAuthService idAuthService;

	@Mock
	NotificationService notificationService;

	@Mock
	private AuditUtil audit;

	@Mock
	PartnerServiceImpl partnerServiceImpl;

	@Mock
	PartnerService partnerService;

	@Mock
	IdentityServiceImpl identityServiceImpl;
	
	@Mock
	private Utilitiy utility;

	@Mock
	ResidentTransactionRepository residentTransactionRepository;

	@InjectMocks
	private ResidentService residentService = new ResidentServiceImpl();

	NotificationResponseDTO notificationResponseDTO;

	AuthTypeStatusDto authTypeStatusDto;

	private String individualId;

	@Before
	public void setup() throws ApisResourceAccessException, ResidentServiceCheckedException {

		notificationResponseDTO = new NotificationResponseDTO();
		notificationResponseDTO.setStatus("Notification success");
		Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());
		individualId = identityServiceImpl.getResidentIndvidualId();
		
		List<ResidentTransactionEntity> residentTransactionEntities=new ArrayList<>();
		ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
		residentTransactionEntity.setEventId(UUID.randomUUID().toString());
		when(utility.createEntity()).thenReturn(residentTransactionEntity);
		residentTransactionEntities.add(residentTransactionEntity);
		ArrayList<String> partnerIds = new ArrayList<>();
		partnerIds.add("m-partner-default-auth");
		when(partnerService.getPartnerDetails(Mockito.anyString())).thenReturn(partnerIds);
		ReflectionTestUtils.invokeMethod(residentService, "createResidentTransactionEntity", "2157245364", "partnerId");
		ReflectionTestUtils.setField(residentService, "authTypes", "otp,bio-FIR,bio-IIR,bio-FACE");
	}

	@Test
	public void testReqAuthTypeStatusUpdateSuccess()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
		authTypeStatusDto.setAuthType("OTP");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(10L);
		List<AuthTypeStatusDtoV2> authTypeStatusDtoList = new java.util.ArrayList<>();
		authTypeStatusDtoList.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthTypes(authTypeStatusDtoList);
		for (AuthTypeStatusDto authTypeStatusDto1 : authLockOrUnLockRequestDtoV2.getAuthTypes()) {
			Mockito.when(idAuthService.authTypeStatusUpdateForRequestId(any(), any(), any())).thenReturn("123");
			ResponseDTO response = new ResponseDTO();
			response.setMessage("Notification success");
			Mockito.when(notificationService.sendNotification(Mockito.any())).thenReturn(notificationResponseDTO);
			ResponseDTO authLockResponse = residentService.reqAauthTypeStatusUpdateV2(authLockOrUnLockRequestDtoV2);
			assertEquals(authLockResponse.getMessage(), authLockResponse.getMessage());
		}

	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAuthTypeLockFailed()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
		authTypeStatusDto.setAuthType("OTP");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(10L);
		List<AuthTypeStatusDtoV2> authTypeStatusDtoList = new java.util.ArrayList<>();
		authTypeStatusDtoList.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthTypes(authTypeStatusDtoList);
		residentService.reqAauthTypeStatusUpdateV2(authLockOrUnLockRequestDtoV2);

	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testReqAuthTypeLockNotificationFailed()
			throws ApisResourceAccessException, ResidentServiceCheckedException {

		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
		authTypeStatusDto.setAuthType("OTP");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(10L);
		List<AuthTypeStatusDtoV2> authTypeStatusDtoList = new java.util.ArrayList<>();
		authTypeStatusDtoList.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthTypes(authTypeStatusDtoList);
		Mockito.when(notificationService.sendNotification(Mockito.any()))
				.thenThrow(new ResidentServiceCheckedException());
		residentService.reqAauthTypeStatusUpdateV2(authLockOrUnLockRequestDtoV2);

	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAuthTypeLockException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {

		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
		authTypeStatusDto.setAuthType("OTP");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(10L);
		List<AuthTypeStatusDtoV2> authTypeStatusDtoList = new java.util.ArrayList<>();
		authTypeStatusDtoList.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthTypes(authTypeStatusDtoList);
		residentService.reqAauthTypeStatusUpdateV2(authLockOrUnLockRequestDtoV2);
	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAuthTypeLockOTPFailedException()
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
		authTypeStatusDto.setAuthType("OTP");
		authTypeStatusDto.setLocked(false);
		authTypeStatusDto.setUnlockForSeconds(10L);
		List<AuthTypeStatusDtoV2> authTypeStatusDtoList = new java.util.ArrayList<>();
		authTypeStatusDtoList.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthTypes(authTypeStatusDtoList);
		residentService.reqAauthTypeStatusUpdateV2(authLockOrUnLockRequestDtoV2);

	}

	@Test
	public void testGetAuthTypeBasedOnConfigV2(){
		AuthTypeStatusDtoV2 authTypeStatus = new AuthTypeStatusDtoV2();
		authTypeStatus.setAuthType("demo");
		assertEquals("demo",ResidentServiceImpl.getAuthTypeBasedOnConfigV2(authTypeStatus));
	}

	@Test
	public void testGetAuthTypeBasedOnConfigV2WithSubType(){
		AuthTypeStatusDtoV2 authTypeStatus = new AuthTypeStatusDtoV2();
		authTypeStatus.setAuthType("demo");
		authTypeStatus.setAuthSubType("demo");
		assertEquals(null,ResidentServiceImpl.getAuthTypeBasedOnConfigV2(authTypeStatus));
	}

	@Test
	public void testGetAuthTypeBasedOnConfigV2WithoutSubType(){
		AuthTypeStatusDtoV2 authTypeStatus = new AuthTypeStatusDtoV2();
		authTypeStatus.setAuthType("bio");
		authTypeStatus.setAuthSubType("FIR");
		assertEquals("bio-FIR",ResidentServiceImpl.getAuthTypeBasedOnConfigV2(authTypeStatus));
	}

	@Test
	public void testTrySendNotificationFailure() throws ResidentServiceCheckedException {
		Mockito.when(notificationService.sendNotification(any())).thenThrow(new ResidentServiceCheckedException());
		ReflectionTestUtils.invokeMethod(residentService,
				"trySendNotification", "123", null, null);
	}
}
