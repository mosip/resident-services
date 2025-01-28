package io.mosip.resident.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.mosip.resident.constant.ApiName;
import io.mosip.resident.util.Utilities;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.dto.PacketGeneratorResDto;
import io.mosip.resident.dto.RegProcCommonResponseDto;
import io.mosip.resident.dto.ResidentReprintRequestDto;
import io.mosip.resident.dto.ResidentReprintResponseDto;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.MachineDto;
import io.mosip.resident.dto.MachineSearchResponseDTO;
import io.mosip.resident.dto.PacketSignPublicKeyResponseDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.handler.service.UinCardRePrintService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.util.ResidentServiceRestClient;

@RunWith(SpringRunner.class)
public class ResidentServiceReqReprintTest {
	
    @MockBean
    private ProxyIdRepoService proxyIdRepoService;
    
	@InjectMocks
	ResidentServiceImpl residentServiceImpl;

	@Mock
	private UinCardRePrintService rePrintService;

	@Mock
	private ResidentServiceRestClient residentServiceRestClient;

	@Mock
	private IdAuthService idAuthService;

	@Mock
	private UinValidator<String> uinValidator;

	@Mock
	Environment env;

	@Mock
    NotificationService notificationService;

	@Mock
	private Utilities utilities;
	
	private ResidentReprintRequestDto residentReqDto;

	@Before
	public void setUp() throws IOException, BaseCheckedException {
		Mockito.when(idAuthService.validateOtp(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString())).thenReturn(true);
		Mockito.when(uinValidator.validateId(Mockito.anyString())).thenReturn(true);
		residentReqDto = new ResidentReprintRequestDto();
		residentReqDto.setIndividualId("3527812406");
		residentReqDto.setIndividualIdType(IdType.UIN.name());
		residentReqDto.setOtp("689745");
		residentReqDto.setTransactionID("0987654321");
		PacketGeneratorResDto resDto = new PacketGeneratorResDto();
		resDto.setRegistrationId("10008200070004620191203115734");
		Mockito.when(rePrintService.createPacket(any())).thenReturn(resDto);

	}

	@Test
	public void reqPrintUinTest() throws ResidentServiceCheckedException, ApisResourceAccessException {

		RegProcCommonResponseDto reprintResp = new RegProcCommonResponseDto();
		reprintResp.setMessage("sent to packet receiver");
		reprintResp.setRegistrationId("10008200070004620191203115734");
		reprintResp.setStatus("success");
		NotificationResponseDTO notificationResponse = new NotificationResponseDTO();
		notificationResponse.setMessage("Notification sent to registered contact details");
		notificationResponse.setStatus("success");

		String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuGXPqbFOIZhB_N_fbTXOMIsRgq_LMdL9DJ5kWYAneCj_LPw3OEm2ncLVIRyJsF2DcSQwvzt_Njdvg1Cr54nD1uHBu3Vt9G1sy3p6uwbeK1l5mJSMNe5oGe11fmehtsR2QcB_45_us_IiiiUzzHJrySexmDfdOiPdy-dID4DYRDAf-HXlMIEf4Di_8NV3wVrA3jq1tuNkXX3qKtM4NhZOihp0HmB9E7RHttSV9VJNh00BrC57qdMfa5xqsHok3qftU5SAan4BGuPklN2fzOVcsa-V-B8JbwxRfPdwMkq-jW7Eu1LcNhNVQYJGEWDLAQDGKY_fOB_YwBzn8xvYRjqSfQIDAQAB";
		List<MachineDto> machineDtos = new ArrayList<>();
		MachineDto machineDto = new MachineDto();
		machineDto.setMachineSpecId("1001");
		machineDto.setIsActive(false);
		machineDto.setId("10147");
		machineDto.setName("resident_machine_1640777004542");
		machineDto.setValidityDateTime("2024-12-29T11:23:24.541Z");
		machineDto.setPublicKey(publicKey);
		machineDto.setSignPublicKey(publicKey);
		machineDtos.add(machineDto);
		MachineSearchResponseDTO.MachineSearchDto response = MachineSearchResponseDTO.MachineSearchDto.builder()
				.fromRecord(0).toRecord(0).toRecord(0).data(machineDtos).build();
		MachineSearchResponseDTO machineSearchResponseDTO = new MachineSearchResponseDTO();
		machineSearchResponseDTO.setId("null");
		machineSearchResponseDTO.setVersion("1.0");
		machineSearchResponseDTO.setResponsetime("2022-01-28T06:25:23.958Z");
		machineSearchResponseDTO.setResponse(response);

		PacketSignPublicKeyResponseDTO responseDto = new PacketSignPublicKeyResponseDTO();
		PacketSignPublicKeyResponseDTO.PacketSignPublicKeyResponse publicKeyResponse = new PacketSignPublicKeyResponseDTO.PacketSignPublicKeyResponse();
		publicKeyResponse.setPublicKey(publicKey);
		responseDto.setId(null);
		responseDto.setVersion(null);
		responseDto.setResponsetime("2022-01-28T06:51:30.286Z");
		responseDto.setResponse(publicKeyResponse);
		responseDto.setErrors(new ArrayList<>());

		Mockito.when(env.getProperty(ApiName.PACKETSIGNPUBLICKEY.name())).thenReturn("PACKETSIGNPUBLICKEY");
		Mockito.when(env.getProperty(ApiName.MACHINESEARCH.name())).thenReturn("MACHINESEARCH");
		Mockito.when(env.getProperty(ApiName.MACHINESTATUSUPDATE.name())).thenReturn("http://localhost");

		Mockito.when(residentServiceRestClient.postApi(eq("PACKETSIGNPUBLICKEY"), any(MediaType.class),
				any(HttpEntity.class), eq(PacketSignPublicKeyResponseDTO.class))).thenReturn(responseDto);
		Mockito.when(residentServiceRestClient.postApi(eq("MACHINESEARCH"), any(MediaType.class), any(HttpEntity.class),
				eq(MachineSearchResponseDTO.class))).thenReturn(machineSearchResponseDTO);
		when(residentServiceRestClient.patchApi(any(), any(), any(), any())).thenReturn(new io.mosip.kernel.core.http.ResponseWrapper<>());

		when(utilities.getLanguageCode()).thenReturn("eng");

		Mockito.when(notificationService.sendNotification(any(), Mockito.nullable(Map.class))).thenReturn(notificationResponse);
		ResidentReprintResponseDto residentResponse = residentServiceImpl.reqPrintUin(residentReqDto);
		assertEquals("10008200070004620191203115734", residentResponse.getRegistrationId());

	}

	@Test(expected = ResidentServiceException.class)
	public void reqPrintUinTestFailure() throws ResidentServiceCheckedException, ApisResourceAccessException {

		RegProcCommonResponseDto reprintResp = new RegProcCommonResponseDto();
		reprintResp.setMessage("sent to packet receiver");
		reprintResp.setRegistrationId("10008200070004620191203115734");
		reprintResp.setStatus("success");
		NotificationResponseDTO notificationResponse = new NotificationResponseDTO();
		notificationResponse.setMessage("Notification sent to registered contact details");
		notificationResponse.setStatus("success");

		String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuGXPqbFOIZhB_N_fbTXOMIsRgq_LMdL9DJ5kWYAneCj_LPw3OEm2ncLVIRyJsF2DcSQwvzt_Njdvg1Cr54nD1uHBu3Vt9G1sy3p6uwbeK1l5mJSMNe5oGe11fmehtsR2QcB_45_us_IiiiUzzHJrySexmDfdOiPdy-dID4DYRDAf-HXlMIEf4Di_8NV3wVrA3jq1tuNkXX3qKtM4NhZOihp0HmB9E7RHttSV9VJNh00BrC57qdMfa5xqsHok3qftU5SAan4BGuPklN2fzOVcsa-V-B8JbwxRfPdwMkq-jW7Eu1LcNhNVQYJGEWDLAQDGKY_fOB_YwBzn8xvYRjqSfQIDAQAB";
		List<MachineDto> machineDtos = new ArrayList<>();
		MachineDto machineDto = new MachineDto();
		machineDto.setMachineSpecId("1001");
		machineDto.setIsActive(false);
		machineDto.setId("10147");
		machineDto.setName("resident_machine_1640777004542");
		machineDto.setValidityDateTime("2024-12-29T11:23:24.541Z");
		machineDto.setPublicKey(publicKey);
		machineDto.setSignPublicKey(publicKey);
		machineDtos.add(machineDto);
		MachineSearchResponseDTO.MachineSearchDto response = MachineSearchResponseDTO.MachineSearchDto.builder()
				.fromRecord(0).toRecord(0).toRecord(0).data(machineDtos).build();
		MachineSearchResponseDTO machineSearchResponseDTO = new MachineSearchResponseDTO();
		machineSearchResponseDTO.setId("null");
		machineSearchResponseDTO.setVersion("1.0");
		machineSearchResponseDTO.setResponsetime("2022-01-28T06:25:23.958Z");
		machineSearchResponseDTO.setResponse(response);

		PacketSignPublicKeyResponseDTO responseDto = new PacketSignPublicKeyResponseDTO();
		PacketSignPublicKeyResponseDTO.PacketSignPublicKeyResponse publicKeyResponse = new PacketSignPublicKeyResponseDTO.PacketSignPublicKeyResponse();
		publicKeyResponse.setPublicKey(publicKey);
		responseDto.setId(null);
		responseDto.setVersion(null);
		responseDto.setResponsetime("2022-01-28T06:51:30.286Z");
		responseDto.setResponse(publicKeyResponse);
		responseDto.setErrors(new ArrayList<>());

		Mockito.when(env.getProperty(ApiName.PACKETSIGNPUBLICKEY.name())).thenReturn("PACKETSIGNPUBLICKEY");
		Mockito.when(env.getProperty(ApiName.MACHINESEARCH.name())).thenReturn("MACHINESEARCH");
		Mockito.when(env.getProperty(ApiName.MACHINESTATUSUPDATE.name())).thenReturn("http://localhost");

		Mockito.when(residentServiceRestClient.postApi(eq("PACKETSIGNPUBLICKEY"), any(MediaType.class),
				any(HttpEntity.class), eq(PacketSignPublicKeyResponseDTO.class))).thenReturn(responseDto);
		Mockito.when(residentServiceRestClient.postApi(eq("MACHINESEARCH"), any(MediaType.class), any(HttpEntity.class),
				eq(MachineSearchResponseDTO.class))).thenReturn(machineSearchResponseDTO);
		when(residentServiceRestClient.patchApi(any(), any(), any(), any())).thenThrow(new ApisResourceAccessException());

		when(utilities.getLanguageCode()).thenReturn("eng");

		Mockito.when(notificationService.sendNotification(any(), Mockito.nullable(Map.class))).thenReturn(notificationResponse);
		ResidentReprintResponseDto residentResponse = residentServiceImpl.reqPrintUin(residentReqDto);
		assertEquals("10008200070004620191203115734", residentResponse.getRegistrationId());

	}

	@Test(expected = ResidentServiceException.class)
	public void validateOtpException()
			throws OtpValidationFailedException, IOException, ResidentServiceCheckedException {
		Mockito.when(idAuthService.validateOtp(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString())).thenReturn(false);
		residentServiceImpl.reqPrintUin(residentReqDto);

	}

	@Test(expected = ResidentServiceException.class)
	public void reprintResidentServiceException() throws BaseCheckedException, IOException {
		ResponseWrapper<RegProcCommonResponseDto> response = new ResponseWrapper<>();
		RegProcCommonResponseDto reprintResp = new RegProcCommonResponseDto();
		reprintResp.setMessage("sent to packet receiver");
		reprintResp.setRegistrationId("10008200070004620191203115734");
		reprintResp.setStatus("success");
		response.setResponse(reprintResp);
		List<ServiceError> errorList = new ArrayList<>();
		ServiceError error = new ServiceError();
		error.setErrorCode("RES_SER-001");
		error.setMessage("Runtime exception");
		errorList.add(error);
		response.setErrors(errorList);
		Mockito.when(rePrintService.createPacket(any())).thenThrow(new IOException("IO exception"));
		residentServiceImpl.reqPrintUin(residentReqDto);

	}

	@Test(expected = ResidentServiceException.class)
	public void testOtpValidationException() throws OtpValidationFailedException, ResidentServiceCheckedException {
		Mockito.when(idAuthService.validateOtp(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString())).thenThrow(new OtpValidationFailedException("OTP validation failed"));
		residentServiceImpl.reqPrintUin(residentReqDto);
	}

	@Test(expected = ResidentServiceException.class)
	public void testApiResourceAccessException() throws BaseCheckedException, IOException {
		HttpClientErrorException exp = new HttpClientErrorException(HttpStatus.BAD_GATEWAY);
		Mockito.when(rePrintService.createPacket(any())).thenThrow(new ApisResourceAccessException("badgateway", exp));
		residentServiceImpl.reqPrintUin(residentReqDto);
	}

	@Test(expected = ResidentServiceException.class)
	public void testApiResourceAccessExceptionServer() throws BaseCheckedException, IOException {
		HttpServerErrorException exp = new HttpServerErrorException(HttpStatus.BAD_GATEWAY);
		Mockito.when(rePrintService.createPacket(any())).thenThrow(new ApisResourceAccessException("badgateway", exp));
		residentServiceImpl.reqPrintUin(residentReqDto);
	}

	@Test(expected = ResidentServiceException.class)
	public void testApiResourceAccessExceptionUnknown() throws BaseCheckedException, IOException {
		Mockito.when(rePrintService.createPacket(any())).thenThrow(new ApisResourceAccessException("badgateway", new RuntimeException()));
		residentServiceImpl.reqPrintUin(residentReqDto);
	}

	@Test(expected = ResidentServiceException.class)
	public void testBaseCheckedException() throws BaseCheckedException, IOException {
		Mockito.when(rePrintService.createPacket(any())).thenThrow(new BaseCheckedException("erorcode", "badgateway", new RuntimeException()));
		residentServiceImpl.reqPrintUin(residentReqDto);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void notificationServiceException() throws ApisResourceAccessException, OtpValidationFailedException,
			IOException, ResidentServiceCheckedException {
		ResponseWrapper<RegProcCommonResponseDto> response = new ResponseWrapper<>();
		RegProcCommonResponseDto reprintResp = new RegProcCommonResponseDto();
		reprintResp.setMessage("sent to packet receiver");
		reprintResp.setRegistrationId("10008200070004620191203115734");
		reprintResp.setStatus("success");
		response.setResponse(reprintResp);
		Mockito.when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(response);
		NotificationResponseDTO notificationResponse = new NotificationResponseDTO();
		notificationResponse.setMessage("Notification sent to registered contact details");
		notificationResponse.setStatus("success");
		Mockito.when(notificationService.sendNotification(any(), Mockito.nullable(Map.class)))
				.thenThrow(new ResidentServiceCheckedException());
		residentServiceImpl.reqPrintUin(residentReqDto);

	}

}
