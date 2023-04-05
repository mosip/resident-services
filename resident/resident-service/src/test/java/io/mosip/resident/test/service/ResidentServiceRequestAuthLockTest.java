/**
 * 
 */
package io.mosip.resident.test.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

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

import io.mosip.kernel.core.idvalidator.spi.RidValidator;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDto;
import io.mosip.resident.dto.AuthUnLockRequestDTO;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.dto.ResponseDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ResidentService;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.UINCardDownloadService;

/**
 * @author M1022006
 *
 */
@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class ResidentServiceRequestAuthLockTest {

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

	@InjectMocks
	private ResidentService residentService = new ResidentServiceImpl();

	NotificationResponseDTO notificationResponseDTO;
	
	AuthLockOrUnLockRequestDto authLockRequestDto;

	@Before
	public void setup() throws Exception {

		notificationResponseDTO = new NotificationResponseDTO();
		notificationResponseDTO.setStatus("Notification success");
		Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());
		ReflectionTestUtils.setField(residentService, "authTypes", "otp,bio-FIR,bio-IIR,bio-FACE");
		authLockRequestDto = new AuthLockOrUnLockRequestDto();
		authLockRequestDto.setIndividualId("1234567889");
		authLockRequestDto.setOtp("1234");
		authLockRequestDto.setTransactionID("1234567898");
		List<String> authTypesList=new ArrayList<String>();
		authTypesList.add("otp");
		authTypesList.add("bio-FIR");
		authLockRequestDto.setAuthType(authTypesList);
		Mockito.when(idAuthService.authTypeStatusUpdate(authLockRequestDto.getIndividualId(),
				authTypesList, AuthTypeStatus.LOCK, null)).thenReturn(true);
	}

	@Test
	public void testReqAauthLockSuccess()
			throws ApisResourceAccessException, ResidentServiceCheckedException, OtpValidationFailedException {

		Mockito.when(idAuthService.validateOtp(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
		ResponseDTO response = new ResponseDTO();
		response.setMessage("Notification success");

		Mockito.when(notificationService.sendNotification(Mockito.any())).thenReturn(notificationResponseDTO);
		ResponseDTO authLockResponse = residentService.reqAauthTypeStatusUpdate(authLockRequestDto,
				AuthTypeStatus.LOCK);
		assertEquals(authLockResponse.getMessage(), authLockResponse.getMessage());

	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAauthLockOTPFailed()
			throws ApisResourceAccessException, ResidentServiceCheckedException, OtpValidationFailedException {

		Mockito.when(idAuthService.validateOtp(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
		
		residentService.reqAauthTypeStatusUpdate(authLockRequestDto, AuthTypeStatus.LOCK);

	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAauthLockFailed()
			throws ApisResourceAccessException, ResidentServiceCheckedException, OtpValidationFailedException {

		Mockito.when(idAuthService.validateOtp(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);

		Mockito.when(idAuthService.authTypeStatusUpdate(authLockRequestDto.getIndividualId(),
				authLockRequestDto.getAuthType(), AuthTypeStatus.LOCK, null)).thenReturn(false);
		residentService.reqAauthTypeStatusUpdate(authLockRequestDto, AuthTypeStatus.LOCK);

	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testReqAauthLockNotificationFailed()
			throws ApisResourceAccessException, ResidentServiceCheckedException, OtpValidationFailedException {

		Mockito.when(idAuthService.validateOtp(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);

		Mockito.when(notificationService.sendNotification(Mockito.any()))
				.thenThrow(new ResidentServiceCheckedException());
		residentService.reqAauthTypeStatusUpdate(authLockRequestDto, AuthTypeStatus.LOCK);

	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAauthLockException()
			throws ApisResourceAccessException, ResidentServiceCheckedException, OtpValidationFailedException {

		Mockito.when(idAuthService.validateOtp(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);

		Mockito.when(idAuthService.authTypeStatusUpdate(authLockRequestDto.getIndividualId(),
				authLockRequestDto.getAuthType(), AuthTypeStatus.LOCK, null))
				.thenThrow(new ApisResourceAccessException());

		residentService.reqAauthTypeStatusUpdate(authLockRequestDto, AuthTypeStatus.LOCK);

	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAuthUnLockException() throws OtpValidationFailedException, ApisResourceAccessException, ResidentServiceCheckedException {
		Mockito.when(idAuthService.validateOtp(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);

		AuthUnLockRequestDTO authUnLockRequestDTO = new AuthUnLockRequestDTO();
		authUnLockRequestDTO.setIndividualId("12344567");
		authUnLockRequestDTO.setOtp("12345");
		authUnLockRequestDTO.setTransactionID("12345");
		authUnLockRequestDTO.setUnlockForSeconds(String.valueOf(-1L));

		Mockito.lenient().when(idAuthService.authTypeStatusUpdate(authUnLockRequestDTO.getIndividualId(),
				authUnLockRequestDTO.getAuthType(), AuthTypeStatus.UNLOCK, null))
				.thenThrow(new ApisResourceAccessException());
		residentService.reqAauthTypeStatusUpdate(authUnLockRequestDTO, AuthTypeStatus.UNLOCK);
	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAauthLockOTPFailedException()
			throws ApisResourceAccessException, ResidentServiceCheckedException, OtpValidationFailedException {

		Mockito.when(idAuthService.validateOtp(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenThrow(new OtpValidationFailedException());
		
		residentService.reqAauthTypeStatusUpdate(authLockRequestDto, AuthTypeStatus.LOCK);

	}
}
