package io.mosip.resident.test.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.resident.dto.PacketGeneratorResDto;
import io.mosip.resident.handler.service.ResidentUpdateService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
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

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.dto.RegProcCommonResponseDto;
import io.mosip.resident.dto.ResidentDocuments;
import io.mosip.resident.dto.ResidentUpdateRequestDto;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TokenGenerator;
import io.mosip.resident.util.Utilitiy;

import static org.mockito.ArgumentMatchers.any;

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
	private TokenGenerator tokenGenerator;

	@Mock
    NotificationService notificationService;
	@Mock
	private Utilitiy utility;

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

		Mockito.when(idAuthService.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString())).thenReturn(true);
		ResponseWrapper<RegProcCommonResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setId("mosip.resident.uin");
		RegProcCommonResponseDto response = new RegProcCommonResponseDto();
		response.setMessage("packet received");
		response.setRegistrationId("10008100670001720191120095702");
		response.setStatus("success");
		responseWrapper.setResponse(response);
		Mockito.when(residentServiceRestClient.postApi(any(), any(), any(), any(),
				any())).thenReturn(responseWrapper);
		NotificationResponseDTO notificationResponse = new NotificationResponseDTO();
		notificationResponse.setMessage("Notification sent");
		notificationResponse.setStatus("success");
		Mockito.when(notificationService.sendNotification(any())).thenReturn(notificationResponse);

		PacketGeneratorResDto updateDto = new PacketGeneratorResDto();
		updateDto.setRegistrationId("10008100670001720191120095702");
		Mockito.when(residentUpdateService.createPacket(any())).thenReturn(updateDto);
	}

	@Test
	public void reqUinUpdateSuccessTest() throws ResidentServiceCheckedException {
		residentServiceImpl.reqUinUpdate(dto);
	}

	@Test(expected = ResidentServiceException.class)
	public void validateOtpException()
			throws OtpValidationFailedException, IOException, ResidentServiceCheckedException {
		Mockito.when(idAuthService.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
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
		Mockito.when(idAuthService.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString())).thenThrow(new OtpValidationFailedException());
		residentServiceImpl.reqUinUpdate(dto);

	}
}
