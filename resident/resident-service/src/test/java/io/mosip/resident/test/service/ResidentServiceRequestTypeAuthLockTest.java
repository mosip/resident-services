package io.mosip.resident.test.service;

import io.mosip.kernel.core.idvalidator.spi.RidValidator;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.dto.*;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


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
	}

	@Test
	public void testReqAuthTypeStatusUpdateSuccess()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		AuthTypeStatusDto authTypeStatusDto = new AuthTypeStatusDto();
		authTypeStatusDto.setAuthType("OTP");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(10L);
		List<AuthTypeStatusDto> authTypeStatusDtoList = new java.util.ArrayList<>();
		authTypeStatusDtoList.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthType(authTypeStatusDtoList);
		ArrayList<String> partnerIds = new ArrayList<>();
		partnerIds.add("m-partner-default-auth");
		Mockito.when(partnerService.getPartnerDetails(Mockito.anyString())).thenReturn(partnerIds);
		for (AuthTypeStatusDto authTypeStatusDto1 : authLockOrUnLockRequestDtoV2.getAuthType()) {
			 idAuthService.authTypeStatusUpdate(individualId,
					List.of(authTypeStatusDto.getAuthType().split(",")),
					authTypeStatusDto.getLocked() ? AuthTypeStatus.LOCK : AuthTypeStatus.UNLOCK, authTypeStatusDto.getUnlockForSeconds());
			ResponseDTO response = new ResponseDTO();
			response.setMessage("Notification success");
			Mockito.when(idAuthService.authTypeStatusUpdate(individualId,List.of(authTypeStatusDto.getAuthType().split(",")), AuthTypeStatus.LOCK,
					authTypeStatusDto.getUnlockForSeconds())).thenReturn(true);
			Mockito.when(notificationService.sendNotification(Mockito.any())).thenReturn(notificationResponseDTO);
			ResponseDTO authLockResponse = residentService.reqAauthTypeStatusUpdateV2(authLockOrUnLockRequestDtoV2);
			assertEquals(authLockResponse.getMessage(), authLockResponse.getMessage());
		}



	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAuthTypeLockFailed()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		AuthTypeStatusDto authTypeStatusDto = new AuthTypeStatusDto();
		authTypeStatusDto.setAuthType("OTP");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(10L);
		List<AuthTypeStatusDto> authTypeStatusDtoList = new java.util.ArrayList<>();
		authTypeStatusDtoList.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthType(authTypeStatusDtoList);
		residentService.reqAauthTypeStatusUpdateV2(authLockOrUnLockRequestDtoV2);

	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testReqAuthTypeLockNotificationFailed()
			throws ApisResourceAccessException, ResidentServiceCheckedException {

		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		AuthTypeStatusDto authTypeStatusDto = new AuthTypeStatusDto();
		authTypeStatusDto.setAuthType("OTP");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(10L);
		List<AuthTypeStatusDto> authTypeStatusDtoList = new java.util.ArrayList<>();
		authTypeStatusDtoList.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthType(authTypeStatusDtoList);
		Mockito.when(idAuthService.authTypeStatusUpdate(individualId,List.of(authTypeStatusDto.getAuthType().split(",")), AuthTypeStatus.LOCK,
				authTypeStatusDto.getUnlockForSeconds())).thenReturn(true);
		Mockito.when(notificationService.sendNotification(Mockito.any()))
				.thenThrow(new ResidentServiceCheckedException());
		residentService.reqAauthTypeStatusUpdateV2(authLockOrUnLockRequestDtoV2);

	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAuthTypeLockException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {

		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		AuthTypeStatusDto authTypeStatusDto = new AuthTypeStatusDto();
		authTypeStatusDto.setAuthType("OTP");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(10L);
		List<AuthTypeStatusDto> authTypeStatusDtoList = new java.util.ArrayList<>();
		authTypeStatusDtoList.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthType(authTypeStatusDtoList);
		Mockito.when(idAuthService.authTypeStatusUpdate(individualId,List.of(authTypeStatusDto.getAuthType().split(",")), AuthTypeStatus.LOCK,
						authTypeStatusDto.getUnlockForSeconds()))
				.thenThrow(new ApisResourceAccessException());

		residentService.reqAauthTypeStatusUpdateV2(authLockOrUnLockRequestDtoV2);

	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAuthTypeUnLockException() throws ApisResourceAccessException, ResidentServiceCheckedException {
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		AuthTypeStatusDto authTypeStatusDto = new AuthTypeStatusDto();
		authTypeStatusDto.setAuthType("OTP");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(10L);
		List<AuthTypeStatusDto> authTypeStatusDtoList = new java.util.ArrayList<>();
		authTypeStatusDtoList.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthType(authTypeStatusDtoList);

		Mockito.lenient().when(idAuthService.authTypeStatusUpdate(individualId,List.of(authTypeStatusDto.getAuthType().split(",")), AuthTypeStatus.LOCK,
				authTypeStatusDto.getUnlockForSeconds()))
				.thenThrow(new ApisResourceAccessException());
		residentService.reqAauthTypeStatusUpdateV2(authLockOrUnLockRequestDtoV2);
	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAuthTypeLockOTPFailedException()
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		AuthTypeStatusDto authTypeStatusDto = new AuthTypeStatusDto();
		authTypeStatusDto.setAuthType("OTP");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(10L);
		List<AuthTypeStatusDto> authTypeStatusDtoList = new java.util.ArrayList<>();
		authTypeStatusDtoList.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthType(authTypeStatusDtoList);
		residentService.reqAauthTypeStatusUpdateV2(authLockOrUnLockRequestDtoV2);

	}
}
