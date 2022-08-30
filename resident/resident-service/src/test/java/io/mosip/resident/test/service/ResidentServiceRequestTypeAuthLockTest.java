package io.mosip.resident.test.service;

import io.mosip.kernel.core.idvalidator.spi.RidValidator;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.dto.*;
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
import static org.mockito.ArgumentMatchers.anyMap;
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
	public void setup() throws ApisResourceAccessException {

		notificationResponseDTO = new NotificationResponseDTO();
		notificationResponseDTO.setStatus("Notification success");
		Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());
		individualId = identityServiceImpl.getResidentIndvidualId();
		
		List<ResidentTransactionEntity> residentTransactionEntities=new ArrayList<>();
		ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
		residentTransactionEntity.setEventId(UUID.randomUUID().toString());
		when(utility.createEntity()).thenReturn(residentTransactionEntity);
		residentTransactionEntities.add(residentTransactionEntity);
		ReflectionTestUtils.invokeMethod(residentService, "createResidentTransactionEntity", "individualId", "partnerId");
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
		ArrayList<String> partnerIds = new ArrayList<>();
		partnerIds.add("m-partner-default-auth");
		for (AuthTypeStatusDto authTypeStatusDto1 : authLockOrUnLockRequestDtoV2.getAuthTypes()) {
			Mockito.when(idAuthService.authTypeStatusUpdate(any(), any(), any())).thenReturn(true);
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
		Mockito.when(idAuthService.authTypeStatusUpdate(any(), any(), any())).thenThrow(new ApisResourceAccessException());

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
}
