package io.mosip.resident.test.service;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.*;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.handler.service.ResidentUpdateService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utilitiy;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ResidentServiceResUpdateTest {
	@InjectMocks
	ResidentServiceImpl residentServiceImpl;

	@Mock
	private ResidentUpdateService residentUpdateService;

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
	private Utilitiy utility;

	@Mock
	private AuditUtil audit;

	ResidentUpdateRequestDto dto;

	@Before
	public void setUp() throws BaseCheckedException, IOException {

		dto = new ResidentUpdateRequestDto();
		ResidentDocuments document = new ResidentDocuments();
		document.setName("POA_Certificate of residence");
		document.setValue(
				"_9j_4AAQSkZJRgABAQAAAQABAAD_2wCEAAkGBxMTEhUSExIVFRUVFRUVFRUWFxUVFRcVFRYWFhUVFRYYHiggGBolHhcVITEhJSkrLi4uFx8zODMtNygtLisBCgoKDg0OGhAQGy0dHR0tLS0tLS0tLS0tLS0tLS0tKy0tKy0tLS0tLS0tKy0tLS0tKystLS0tLS0tLS0tLSsrK__AABEIALMBGgMBIgACEQEDEQH_xAAbAAABBQEBAAAAAAAAAAAAAAADAAIEBQYBB__EA");
		List<ResidentDocuments> documents = new ArrayList<>();
		documents.add(document);
		dto.setDocuments(documents);
		dto.setIdentityJson(
				"ew0KICAiaWRlbnRpdHkiIDogew0KICAgICJkYXRlT2ZCaXJ0aCIgOiAiMTk5NS8wOC8wOCIsDQogICAgImFnZSIgOiAyNywNCiAgICAicGhvbmUiIDogIjk3ODY1NDMyMTAiLA0KICAgICJlbWFpbCIgOiAiZ2lyaXNoLnlhcnJ1QG1pbmR0cmVlLmNvbSIsDQogICAgInByb29mT2ZBZGRyZXNzIiA6IHsNCiAgICAgICJ2YWx1ZSIgOiAiUE9BX0NlcnRpZmljYXRlIG9mIHJlc2lkZW5jZSIsDQogICAgICAidHlwZSIgOiAiQ09SIiwNCiAgICAgICJmb3JtYXQiIDogImpwZyINCiAgICB9LA0KCSJVSU4iOiAzNTI3ODEyNDA2LA0KICAgICJJRFNjaGVtYVZlcnNpb24iIDogMS4wDQogIH0NCn0");
		dto.setIndividualId("3527812406");
		dto.setIndividualIdType(IdType.UIN.name());
		dto.setTransactionID("12345");
		dto.setOtp("12345");
		ReflectionTestUtils.setField(residentServiceImpl, "centerId", "10008");
		ReflectionTestUtils.setField(residentServiceImpl, "machineId", "10008");

		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		Mockito.when(utility.getMappingJson()).thenReturn(mappingJson);

		Mockito.when(idAuthService.validateOtp(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString())).thenReturn(true);

		NotificationResponseDTO notificationResponse = new NotificationResponseDTO();
		notificationResponse.setMessage("Notification sent");
		notificationResponse.setStatus("success");
		Mockito.when(notificationService.sendNotification(any())).thenReturn(notificationResponse);

		PacketGeneratorResDto updateDto = new PacketGeneratorResDto();
		updateDto.setRegistrationId("10008100670001720191120095702");
		Mockito.when(residentUpdateService.createPacket(any())).thenReturn(updateDto);
		Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());
	}

    @Test(expected = ResidentServiceException.class)
    public void reqUinUpdatePublicKeyManagerThrowsApiResourceExceptionTest() throws ResidentServiceCheckedException, ApisResourceAccessException {
        String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuGXPqbFOIZhB_N_fbTXOMIsRgq_LMdL9DJ5kWYAneCj_LPw3OEm2ncLVIRyJsF2DcSQwvzt_Njdvg1Cr54nD1uHBu3Vt9G1sy3p6uwbeK1l5mJSMNe5oGe11fmehtsR2QcB_45_us_IiiiUzzHJrySexmDfdOiPdy-dID4DYRDAf-HXlMIEf4Di_8NV3wVrA3jq1tuNkXX3qKtM4NhZOihp0HmB9E7RHttSV9VJNh00BrC57qdMfa5xqsHok3qftU5SAan4BGuPklN2fzOVcsa-V-B8JbwxRfPdwMkq-jW7Eu1LcNhNVQYJGEWDLAQDGKY_fOB_YwBzn8xvYRjqSfQIDAQAB";
        PacketSignPublicKeyResponseDTO responseDto = new PacketSignPublicKeyResponseDTO();
        PacketSignPublicKeyResponseDTO.PacketSignPublicKeyResponse publicKeyResponse = new PacketSignPublicKeyResponseDTO.PacketSignPublicKeyResponse();
        publicKeyResponse.setPublicKey(publicKey);
        responseDto.setId(null);
        responseDto.setVersion(null);
        responseDto.setResponsetime("2022-01-28T06:51:30.286Z");
        responseDto.setResponse(publicKeyResponse);
        responseDto.setErrors(new ArrayList<>());
        when(residentServiceRestClient.postApi(anyString(), any(), any(), any(Class.class))).thenReturn(responseDto);
        residentServiceImpl.reqUinUpdate(dto);
    }

    @Test(expected = ResidentServiceException.class)
    public void reqUinUpdateThrowsResidentServiceTPMSignKeyExceptionTest() throws ApisResourceAccessException, ResidentServiceCheckedException {
        PacketSignPublicKeyResponseDTO responseDto = new PacketSignPublicKeyResponseDTO();
        List<PacketSignPublicKeyErrorDTO> errorDTOS = new ArrayList<>();
        PacketSignPublicKeyErrorDTO errorDTO = new PacketSignPublicKeyErrorDTO();
        errorDTO.setErrorCode(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode());
        errorDTO.setMessage(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage());
        errorDTOS.add(errorDTO);
        responseDto.setId(null);
        responseDto.setVersion(null);
        responseDto.setResponsetime("2022-01-28T06:51:30.286Z");
        responseDto.setResponse(null);
        responseDto.setErrors(errorDTOS);
        when(residentServiceRestClient.postApi(any(), any(), any(), any(Class.class))).thenReturn(responseDto);
        residentServiceImpl.reqUinUpdate(dto);
    }

    @Test(expected = ResidentServiceException.class)
    public void reqUinUpdateThrowsResidentServiceTPMSignKeyExceptionWithNullResponseTest() throws ApisResourceAccessException, ResidentServiceCheckedException {
        PacketSignPublicKeyResponseDTO responseDto = new PacketSignPublicKeyResponseDTO();
        responseDto.setId(null);
        responseDto.setVersion(null);
        responseDto.setResponsetime("2022-01-28T06:51:30.286Z");
        responseDto.setResponse(null);
        responseDto.setErrors(new ArrayList<>());
        when(residentServiceRestClient.postApi(any(), any(), any(), any(Class.class))).thenReturn(responseDto);
        residentServiceImpl.reqUinUpdate(dto);
    }

	@Test(expected = ResidentServiceException.class)
	public void validateOtpException()
			throws OtpValidationFailedException, IOException, ResidentServiceCheckedException {
		Mockito.when(idAuthService.validateOtp(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString())).thenReturn(false);
		residentServiceImpl.reqUinUpdate(dto);

	}

	@Test(expected = ResidentServiceException.class)
	public void JsonParsingException() throws ResidentServiceCheckedException {
		Mockito.when(utility.getMappingJson()).thenReturn(null);
		residentServiceImpl.reqUinUpdate(dto);

	}

	@Test(expected = ResidentServiceException.class)
	public void testIOException() throws BaseCheckedException, IOException {
		HttpClientErrorException exp = new HttpClientErrorException(HttpStatus.BAD_GATEWAY);
		Mockito.when(residentUpdateService.createPacket(any())).thenThrow(new IOException("badgateway", exp));
		residentServiceImpl.reqUinUpdate(dto);
	}

	@Test(expected = ResidentServiceException.class)
	public void testApiResourceAccessExceptionServer() throws BaseCheckedException, IOException {
		HttpServerErrorException exp = new HttpServerErrorException(HttpStatus.BAD_GATEWAY);
		Mockito.when(residentUpdateService.createPacket(any())).thenThrow(new ApisResourceAccessException("badgateway", exp));
		residentServiceImpl.reqUinUpdate(dto);
	}

	@Test(expected = ResidentServiceException.class)
	public void testBaseCheckedException() throws BaseCheckedException, IOException {
		Mockito.when(residentUpdateService.createPacket(any())).thenThrow(new BaseCheckedException("erorcode", "badgateway", new RuntimeException()));
		residentServiceImpl.reqUinUpdate(dto);
	}

	@Test(expected = ResidentServiceException.class)
	public void otpValidationFailedException() throws OtpValidationFailedException, ResidentServiceCheckedException {
		Mockito.when(idAuthService.validateOtp(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString())).thenThrow(new OtpValidationFailedException());
		residentServiceImpl.reqUinUpdate(dto);

	}
}
