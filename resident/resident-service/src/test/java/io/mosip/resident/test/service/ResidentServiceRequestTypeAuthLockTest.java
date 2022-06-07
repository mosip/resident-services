package io.mosip.resident.test.service;

import io.mosip.kernel.core.idvalidator.spi.RidValidator;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.dto.*;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
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
		AuthTypeStatusDto authTypeStatusDto = new AuthTypeStatusDto();
		authTypeStatusDto.setAuthType("OTP");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds("10");
		authTypeStatusDto.setUin("123456789012345");
		individualId = identityServiceImpl.getResidentIndvidualId();
	}

	@Test
	public void testReqAuthTypeStatusUpdateSuccess()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		AuthTypeStatusDto authTypeStatusDto = new AuthTypeStatusDto();
		authTypeStatusDto.setAuthType("OTP");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds("10");
		authTypeStatusDto.setUin("123456789012345");
		idAuthService.authTypeStatusUpdate(individualId,
				List.of(authTypeStatusDto.getAuthType().split(",")), AuthTypeStatus.LOCK,
				Long.parseLong(authTypeStatusDto.getUnlockForSeconds()));
		ResponseDTO response = new ResponseDTO();
		response.setMessage("Notification success");
		Mockito.when(idAuthService.authTypeStatusUpdate(individualId,List.of(authTypeStatusDto.getAuthType().split(",")), AuthTypeStatus.LOCK,
				Long.parseLong(authTypeStatusDto.getUnlockForSeconds()))).thenReturn(true);
		Mockito.when(notificationService.sendNotification(Mockito.any())).thenReturn(notificationResponseDTO);
		ResponseDTO authLockResponse = residentService.reqAauthTypeStatusUpdateV2(authTypeStatusDto);
		assertEquals(authLockResponse.getMessage(), authLockResponse.getMessage());

	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAuthTypeLockFailed()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		AuthTypeStatusDto authTypeStatusDto = new AuthTypeStatusDto();
		authTypeStatusDto.setAuthType("OTP");
		authTypeStatusDto.setLocked(false);
		authTypeStatusDto.setUnlockForSeconds("10");
		authTypeStatusDto.setUin("123456789012345");
		residentService.reqAauthTypeStatusUpdateV2(authTypeStatusDto);

	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testReqAuthTypeLockNotificationFailed()
			throws ApisResourceAccessException, ResidentServiceCheckedException {

		AuthTypeStatusDto authTypeStatusDto = new AuthTypeStatusDto();
		authTypeStatusDto.setAuthType("OTP");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds("10");
		authTypeStatusDto.setUin("123456789012345");
		Mockito.when(idAuthService.authTypeStatusUpdate(individualId,List.of(authTypeStatusDto.getAuthType().split(",")), AuthTypeStatus.LOCK,
				Long.parseLong(authTypeStatusDto.getUnlockForSeconds()))).thenReturn(true);
		Mockito.when(notificationService.sendNotification(Mockito.any()))
				.thenThrow(new ResidentServiceCheckedException());
		residentService.reqAauthTypeStatusUpdateV2(authTypeStatusDto);

	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAuthTypeLockException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {

		AuthTypeStatusDto authTypeStatusDto = new AuthTypeStatusDto();
		authTypeStatusDto.setAuthType("OTP");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds("10");
		authTypeStatusDto.setUin("123456789012345");
		Mockito.when(idAuthService.authTypeStatusUpdate(individualId,List.of(authTypeStatusDto.getAuthType().split(",")), AuthTypeStatus.LOCK,
						Long.parseLong(authTypeStatusDto.getUnlockForSeconds())))
				.thenThrow(new ApisResourceAccessException());

		residentService.reqAauthTypeStatusUpdateV2(authTypeStatusDto);

	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAuthTypeUnLockException() throws ApisResourceAccessException, ResidentServiceCheckedException {
		AuthTypeStatusDto authTypeStatusDto = new AuthTypeStatusDto();
		authTypeStatusDto.setAuthType("OTP");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds("10");
		authTypeStatusDto.setUin("123456789012345");

		Mockito.lenient().when(idAuthService.authTypeStatusUpdate(individualId,List.of(authTypeStatusDto.getAuthType().split(",")), AuthTypeStatus.LOCK,
				Long.parseLong(authTypeStatusDto.getUnlockForSeconds())))
				.thenThrow(new ApisResourceAccessException());
		residentService.reqAauthTypeStatusUpdateV2(authTypeStatusDto);
	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAuthTypeLockOTPFailedException()
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		AuthTypeStatusDto authTypeStatusDto = new AuthTypeStatusDto();
		authTypeStatusDto.setAuthType("OTP");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds("10");
		authTypeStatusDto.setUin("123456789012345");
		residentService.reqAauthTypeStatusUpdateV2(authTypeStatusDto);

	}
}
