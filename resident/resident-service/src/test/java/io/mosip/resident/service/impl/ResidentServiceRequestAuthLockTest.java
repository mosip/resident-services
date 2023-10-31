/**
 * 
 */
package io.mosip.resident.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.idvalidator.spi.RidValidator;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDto;
import io.mosip.resident.dto.AuthLockStatusResponseDtoV2;
import io.mosip.resident.dto.AuthLockTypeStatusDtoV2;
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
import io.mosip.resident.util.ResidentServiceRestClient;
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
	private ResidentServiceRestClient residentServiceRestClient;

	@InjectMocks
	private ResidentService residentService = new ResidentServiceImpl();

	NotificationResponseDTO notificationResponseDTO;
	
	AuthLockOrUnLockRequestDto authLockRequestDto;

	@Before
	public void setup() throws Exception {

		notificationResponseDTO = new NotificationResponseDTO();
		notificationResponseDTO.setStatus("Notification success");
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

		Mockito.when(idAuthService.validateOtp(any(), any(), any())).thenReturn(true);
		ResponseDTO response = new ResponseDTO();
		response.setMessage("Notification success");

		Mockito.when(notificationService.sendNotification(any(), Mockito.nullable(Map.class))).thenReturn(notificationResponseDTO);
		ResponseDTO authLockResponse = residentService.reqAauthTypeStatusUpdate(authLockRequestDto,
				AuthTypeStatus.LOCK);
		assertEquals(authLockResponse.getMessage(), authLockResponse.getMessage());

	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAauthLockOTPFailed()
			throws ApisResourceAccessException, ResidentServiceCheckedException, OtpValidationFailedException {

		Mockito.when(idAuthService.validateOtp(any(), any(), any())).thenReturn(false);
		
		residentService.reqAauthTypeStatusUpdate(authLockRequestDto, AuthTypeStatus.LOCK);

	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAauthLockFailed()
			throws ApisResourceAccessException, ResidentServiceCheckedException, OtpValidationFailedException {

		Mockito.when(idAuthService.validateOtp(any(), any(), any())).thenReturn(true);

		Mockito.when(idAuthService.authTypeStatusUpdate(authLockRequestDto.getIndividualId(),
				authLockRequestDto.getAuthType(), AuthTypeStatus.LOCK, null)).thenReturn(false);
		residentService.reqAauthTypeStatusUpdate(authLockRequestDto, AuthTypeStatus.LOCK);

	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testReqAauthLockNotificationFailed()
			throws ApisResourceAccessException, ResidentServiceCheckedException, OtpValidationFailedException {

		Mockito.when(idAuthService.validateOtp(any(), any(), any())).thenReturn(true);

		Mockito.when(notificationService.sendNotification(any(), Mockito.nullable(Map.class)))
				.thenThrow(new ResidentServiceCheckedException());
		residentService.reqAauthTypeStatusUpdate(authLockRequestDto, AuthTypeStatus.LOCK);

	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAauthLockException()
			throws ApisResourceAccessException, ResidentServiceCheckedException, OtpValidationFailedException {

		Mockito.when(idAuthService.validateOtp(any(), any(), any())).thenReturn(true);

		Mockito.when(idAuthService.authTypeStatusUpdate(authLockRequestDto.getIndividualId(),
				authLockRequestDto.getAuthType(), AuthTypeStatus.LOCK, null))
				.thenThrow(new ApisResourceAccessException());

		residentService.reqAauthTypeStatusUpdate(authLockRequestDto, AuthTypeStatus.LOCK);

	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAuthUnLockException() throws OtpValidationFailedException, ApisResourceAccessException, ResidentServiceCheckedException {
		Mockito.when(idAuthService.validateOtp(any(), any(), any())).thenReturn(true);

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

		Mockito.when(idAuthService.validateOtp(any(), any(), any()))
				.thenThrow(new OtpValidationFailedException());
		
		residentService.reqAauthTypeStatusUpdate(authLockRequestDto, AuthTypeStatus.LOCK);

	}

	@Test
	public void testGetAuthLockStatus() throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<AuthLockStatusResponseDtoV2> responseWrapper = new ResponseWrapper<>();
		AuthLockTypeStatusDtoV2 authLockTypeStatusDtoV2 = new AuthLockTypeStatusDtoV2();
		authLockTypeStatusDtoV2.setLocked(true);
		authLockTypeStatusDtoV2.setAuthType("OTP");
		authLockTypeStatusDtoV2.setRequestId("1234");
		AuthLockTypeStatusDtoV2 authLockTypeStatusDtoV21 = new AuthLockTypeStatusDtoV2();
		authLockTypeStatusDtoV21.setAuthType("Phone");
		authLockTypeStatusDtoV21.setLocked(true);
		AuthLockStatusResponseDtoV2 authLockStatusResponseDtoV2 = new AuthLockStatusResponseDtoV2();
		authLockStatusResponseDtoV2.setAuthTypes(List.of(authLockTypeStatusDtoV2, authLockTypeStatusDtoV21));
		responseWrapper.setResponse(authLockStatusResponseDtoV2);
		Mockito.when(residentServiceRestClient.getApi((ApiName) any(), (List<String>) any(), (List<String>) any(), any(), any()))
						.thenReturn(responseWrapper);
		residentService.getAuthLockStatus("7947240763");
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetAuthLockFailed() throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<AuthLockStatusResponseDtoV2> responseWrapper = new ResponseWrapper<>();
		AuthLockTypeStatusDtoV2 authLockTypeStatusDtoV2 = new AuthLockTypeStatusDtoV2();
		authLockTypeStatusDtoV2.setLocked(true);
		authLockTypeStatusDtoV2.setAuthType("OTP");
		authLockTypeStatusDtoV2.setRequestId("1234");
		AuthLockStatusResponseDtoV2 authLockStatusResponseDtoV2 = new AuthLockStatusResponseDtoV2();
		authLockStatusResponseDtoV2.setAuthTypes(List.of(authLockTypeStatusDtoV2));
		responseWrapper.setResponse(authLockStatusResponseDtoV2);
		responseWrapper.setErrors(List.of(new ServiceError(ResidentErrorCode.AUTH_LOCK_STATUS_FAILED.getErrorCode(),
				ResidentErrorCode.AUTH_LOCK_STATUS_FAILED.getErrorMessage())));
		Mockito.when(residentServiceRestClient.getApi((ApiName) any(), (List<String>) any(), (List<String>) any(), any(), any()))
				.thenReturn(responseWrapper);
		residentService.getAuthLockStatus("7947240763");
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetAuthLockResidentServiceCheckedException() throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<AuthLockStatusResponseDtoV2> responseWrapper = new ResponseWrapper<>();
		AuthLockTypeStatusDtoV2 authLockTypeStatusDtoV2 = new AuthLockTypeStatusDtoV2();
		authLockTypeStatusDtoV2.setLocked(true);
		authLockTypeStatusDtoV2.setAuthType("OTP");
		authLockTypeStatusDtoV2.setRequestId("1234");
		AuthLockStatusResponseDtoV2 authLockStatusResponseDtoV2 = new AuthLockStatusResponseDtoV2();
		authLockStatusResponseDtoV2.setAuthTypes(List.of(authLockTypeStatusDtoV2));
		responseWrapper.setResponse(authLockStatusResponseDtoV2);
		responseWrapper.setErrors(List.of(new ServiceError(ResidentErrorCode.AUTH_LOCK_STATUS_FAILED.getErrorCode(),
				ResidentErrorCode.AUTH_LOCK_STATUS_FAILED.getErrorMessage())));
		Mockito.when(residentServiceRestClient.getApi((ApiName) any(), (List<String>) any(), (List<String>) any(), any(), any()))
				.thenThrow(new ApisResourceAccessException());
		residentService.getAuthLockStatus("7947240763");
	}

	@Test
	public void testGetAuthLockStatusSuccess() throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<AuthLockStatusResponseDtoV2> responseWrapper = new ResponseWrapper<>();
		AuthLockTypeStatusDtoV2 authLockTypeStatusDtoV2 = new AuthLockTypeStatusDtoV2();
		authLockTypeStatusDtoV2.setLocked(true);
		authLockTypeStatusDtoV2.setAuthType("OTP");
		authLockTypeStatusDtoV2.setRequestId("1234");
		AuthLockTypeStatusDtoV2 authLockTypeStatusDtoV21 = new AuthLockTypeStatusDtoV2();
		authLockTypeStatusDtoV21.setAuthType("Phone");
		authLockTypeStatusDtoV21.setLocked(true);
		AuthLockStatusResponseDtoV2 authLockStatusResponseDtoV2 = new AuthLockStatusResponseDtoV2();
		authLockStatusResponseDtoV2.setAuthTypes(List.of());
		responseWrapper.setResponse(authLockStatusResponseDtoV2);
		Mockito.when(residentServiceRestClient.getApi((ApiName) any(), (List<String>) any(), (List<String>) any(), any(), any()))
				.thenReturn(responseWrapper);
		residentService.getAuthLockStatus("7947240763");
	}
}
