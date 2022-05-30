package io.mosip.resident.test.service;

import io.mosip.kernel.core.idvalidator.spi.RidValidator;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDto;
import io.mosip.resident.dto.AuthUnLockRequestDTO;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.dto.ResponseDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.PartnerService;
import io.mosip.resident.service.ResidentService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.test.context.ContextConfiguration;

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

	@InjectMocks
	private ResidentService residentService = new ResidentServiceImpl();

	NotificationResponseDTO notificationResponseDTO;

	@Before
	public void setup() {

		notificationResponseDTO = new NotificationResponseDTO();
		notificationResponseDTO.setStatus("Notification success");
		Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());

	}

	@Test
	public void testReqAuthTypeStatusUpdateSuccess()
			throws ApisResourceAccessException, ResidentServiceCheckedException {

		ResponseDTO response = new ResponseDTO();
		response.setMessage("Notification success");
		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		authLockRequestDto.setIndividualId("1234567889");
		authLockRequestDto.setOtp("1234");
		authLockRequestDto.setTransactionID("1234567898");

		Mockito.when(idAuthService.authTypeStatusUpdate(authLockRequestDto.getIndividualId(),
				authLockRequestDto.getAuthType(), AuthTypeStatus.LOCK, null)).thenReturn(true);
		Mockito.when(notificationService.sendNotification(Mockito.any())).thenReturn(notificationResponseDTO);
		ResponseDTO authLockResponse = residentService.reqAauthTypeStatusUpdateV2(authLockRequestDto,
				AuthTypeStatus.LOCK);
		assertEquals(authLockResponse.getMessage(), authLockResponse.getMessage());

	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAuthTypeLockFailed()
			throws ApisResourceAccessException, ResidentServiceCheckedException {

		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		authLockRequestDto.setIndividualId("1234567889");
		authLockRequestDto.setOtp("1234");
		authLockRequestDto.setTransactionID("1234567898");
		Mockito.when(idAuthService.authTypeStatusUpdate(authLockRequestDto.getIndividualId(),
				authLockRequestDto.getAuthType(), AuthTypeStatus.LOCK, null)).thenReturn(false);
		residentService.reqAauthTypeStatusUpdateV2(authLockRequestDto, AuthTypeStatus.LOCK);

	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testReqAuthTypeLockNotificationFailed()
			throws ApisResourceAccessException, ResidentServiceCheckedException {

		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		authLockRequestDto.setIndividualId("1234567889");
		authLockRequestDto.setOtp("1234");
		authLockRequestDto.setTransactionID("1234567898");
		Mockito.when(idAuthService.authTypeStatusUpdate(authLockRequestDto.getIndividualId(),
				authLockRequestDto.getAuthType(), AuthTypeStatus.LOCK, null)).thenReturn(true);
		Mockito.when(notificationService.sendNotification(Mockito.any()))
				.thenThrow(new ResidentServiceCheckedException());
		residentService.reqAauthTypeStatusUpdateV2(authLockRequestDto, AuthTypeStatus.LOCK);

	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAuthTypeLockException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {

		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		authLockRequestDto.setIndividualId("1234567889");
		authLockRequestDto.setOtp("1234");
		authLockRequestDto.setTransactionID("1234567898");
		Mockito.when(idAuthService.authTypeStatusUpdate(authLockRequestDto.getIndividualId(),
				authLockRequestDto.getAuthType(), AuthTypeStatus.LOCK, null))
				.thenThrow(new ApisResourceAccessException());

		residentService.reqAauthTypeStatusUpdateV2(authLockRequestDto, AuthTypeStatus.LOCK);

	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAuthTypeUnLockException() throws ApisResourceAccessException, ResidentServiceCheckedException {
		AuthUnLockRequestDTO authUnLockRequestDTO = new AuthUnLockRequestDTO();
		authUnLockRequestDTO.setIndividualId("12344567");
		authUnLockRequestDTO.setOtp("12345");
		authUnLockRequestDTO.setTransactionID("12345");
		authUnLockRequestDTO.setUnlockForSeconds(String.valueOf(-1L));

		Mockito.lenient().when(idAuthService.authTypeStatusUpdate(authUnLockRequestDTO.getIndividualId(),
				authUnLockRequestDTO.getAuthType(), AuthTypeStatus.UNLOCK, null))
				.thenThrow(new ApisResourceAccessException());
		residentService.reqAauthTypeStatusUpdateV2(authUnLockRequestDTO, AuthTypeStatus.UNLOCK);
	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAuthTypeLockOTPFailedException()
			throws ResidentServiceCheckedException {

		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		authLockRequestDto.setIndividualId("1234567889");
		authLockRequestDto.setOtp("1234");
		authLockRequestDto.setTransactionID("1234567898");
		residentService.reqAauthTypeStatusUpdateV2(authLockRequestDto, AuthTypeStatus.LOCK);

	}
}
