package io.mosip.resident.test.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.assertj.core.util.Lists;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.dto.NotificationRequestDto;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.VidGeneratorResponseDto;
import io.mosip.resident.dto.VidRequestDto;
import io.mosip.resident.dto.VidResponseDto;
import io.mosip.resident.dto.VidRevokeRequestDTO;
import io.mosip.resident.dto.VidRevokeResponseDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.VidAlreadyPresentException;
import io.mosip.resident.exception.VidCreationException;
import io.mosip.resident.exception.VidRevocationException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ResidentVidServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utilitiy;

@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class ResidentVidServiceTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Environment env;

    @Mock
    private ResidentServiceRestClient residentServiceRestClient;

    @Mock
    private NotificationService notificationService;

    @Mock
    private IdAuthService idAuthService;

    @Mock
    private ObjectMapper mapper;
    
    @Mock
	private Utilitiy utilitiy;
    
	@Mock
	private AuditUtil audit;

    private VidRequestDto requestDto;
    
    private VidRevokeRequestDTO vidRevokeRequest;
    
    @Mock
    private IdentityServiceImpl identityServiceImpl;

    @InjectMocks
    private ResidentVidService residentVidService=new ResidentVidServiceImpl();
    
    @Mock
	private ResidentTransactionRepository residentTransactionRepository;
    
    private JSONObject identity;
    
    @Before
    public void setup() throws IOException, ResidentServiceCheckedException, ApisResourceAccessException {

        requestDto = new VidRequestDto();
        requestDto.setOtp("123");
        requestDto.setTransactionID("12345");
        requestDto.setIndividualId("1234567890");
        requestDto.setVidType("Temporary");

        NotificationResponseDTO notificationResponseDTO = new NotificationResponseDTO();
        notificationResponseDTO.setMessage("Vid successfully generated");

        when(notificationService.sendNotification(any(NotificationRequestDto.class))).thenReturn(notificationResponseDTO);
        IdentityDTO identityValue = new IdentityDTO();
        identityValue.setEmail("aaa@bbb.com");
        identityValue.setPhone("987654321");
        identityValue.setUIN("1234567890");
		when(identityServiceImpl.getIdentity(Mockito.anyString())).thenReturn(identityValue);
        
        ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("ID.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String idJsonString = IOUtils.toString(is, "UTF-8");
		identity = JsonUtil.readValue(idJsonString, JSONObject.class);
		
		vidRevokeRequest = new VidRevokeRequestDTO();

		vidRevokeRequest.setIndividualId("2038096257310540");
		vidRevokeRequest.setOtp("974436");
		vidRevokeRequest.setTransactionID("1111122222");
		vidRevokeRequest.setVidStatus("REVOKE");
		
		NotificationRequestDto notificationRequestDto = new NotificationRequestDto();
		notificationRequestDto.setId("1234567");
		Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());
		
		ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
		residentTransactionEntity.setEventId(UUID.randomUUID().toString());
		Mockito.lenient().when(utilitiy.createEntity()).thenReturn(residentTransactionEntity);
		Mockito.lenient().when(identityServiceImpl.getResidentIdaToken()).thenReturn("idaToken");
    }

    @Test
    public void generateVidSuccessTest() throws OtpValidationFailedException, IOException, ApisResourceAccessException, ResidentServiceCheckedException {

        String vid = "12345";
        VidGeneratorResponseDto vidGeneratorResponseDto = new VidGeneratorResponseDto();
        vidGeneratorResponseDto.setVidStatus("Active");
        vidGeneratorResponseDto.setVID(vid);
        ResponseWrapper<VidGeneratorResponseDto> response = new ResponseWrapper<>();
        response.setResponsetime(DateUtils.getCurrentDateTimeString());
        response.setResponse(vidGeneratorResponseDto);

        doReturn(objectMapper.writeValueAsString(vidGeneratorResponseDto)).when(mapper).writeValueAsString(any());
        doReturn(vidGeneratorResponseDto).when(mapper).readValue(anyString(), any(Class.class));
		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);
        when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(response);

        ResponseWrapper<VidResponseDto> result = residentVidService.generateVid(requestDto, vid);

        assertTrue("Expected Vid should be 12345", result.getResponse().getVid().equalsIgnoreCase(vid));
    }

    @Test(expected = OtpValidationFailedException.class)
    public void otpValidationFailedTest() throws ResidentServiceCheckedException, OtpValidationFailedException {
    	
		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(Boolean.FALSE);
        residentVidService.generateVid(requestDto, "12345");
    }

    @Test(expected = VidAlreadyPresentException.class)
    public void vidAlreadyExistsExceptionTest() throws ResidentServiceCheckedException, OtpValidationFailedException, ApisResourceAccessException {

        String VID_ALREADY_EXISTS_ERROR_CODE = "IDR-VID-003";

        ServiceError serviceError = new ServiceError();
        serviceError.setErrorCode(VID_ALREADY_EXISTS_ERROR_CODE);
        serviceError.setMessage("Vid already present");
        ResponseWrapper<VidGeneratorResponseDto> response = new ResponseWrapper<>();
        response.setResponsetime(DateUtils.getCurrentDateTimeString());
        response.setErrors(Lists.newArrayList(serviceError));

		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);
		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);

        when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(response);

        residentVidService.generateVid(requestDto, "12345");
    }

    @Test(expected = VidCreationException.class)
    public void vidCreationExceptionTest() throws ResidentServiceCheckedException, OtpValidationFailedException, ApisResourceAccessException {

        String ERROR_CODE = "err";

        ServiceError serviceError = new ServiceError();
        serviceError.setErrorCode(ERROR_CODE);
        serviceError.setMessage("Vid already present");
        ResponseWrapper<VidGeneratorResponseDto> response = new ResponseWrapper<>();
        response.setResponsetime(DateUtils.getCurrentDateTimeString());
        response.setErrors(Lists.newArrayList(serviceError));

		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);
		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);

        when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(response);

        residentVidService.generateVid(requestDto, "12345");
    }

    @Test(expected = VidCreationException.class)
    public void apiResourceAccessExceptionTest() throws ResidentServiceCheckedException, OtpValidationFailedException, ApisResourceAccessException {

        String ERROR_CODE = "err";

        ServiceError serviceError = new ServiceError();
        serviceError.setErrorCode(ERROR_CODE);
        serviceError.setMessage("Vid already present");
        ResponseWrapper<VidGeneratorResponseDto> response = new ResponseWrapper<>();
        response.setResponsetime(DateUtils.getCurrentDateTimeString());
        response.setErrors(Lists.newArrayList(serviceError));

		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);
		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);

        when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenThrow(new ApisResourceAccessException());

        residentVidService.generateVid(requestDto, "12345");
    }
    
    @Test
	public void revokeVidSuccessTest() throws OtpValidationFailedException, IOException, ApisResourceAccessException,
			ResidentServiceCheckedException {

		String vid = "2038096257310540";
	
		VidGeneratorResponseDto dto = new VidGeneratorResponseDto();
		dto.setVidStatus("Deactive");

		ResponseWrapper<VidGeneratorResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(dto);
		responseWrapper.setVersion("v1");
		responseWrapper.setResponsetime(DateUtils.getCurrentDateTimeString());
		
		doReturn(dto).when(mapper).convertValue(any(), any(Class.class));
		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);
		when(residentServiceRestClient.patchApi(any(), any(), any(), any())).thenReturn(responseWrapper);
        when(identityServiceImpl.getUinForIndividualId(vid)).thenReturn("1234567890");

		ResponseWrapper<VidRevokeResponseDTO> result2 = residentVidService.revokeVid(vidRevokeRequest,vid, "12345");

		assertEquals("Vid successfully generated", result2.getResponse().getMessage().toString());
	}
    
    @Test(expected = OtpValidationFailedException.class)
    public void otpValidationFailedTest1() throws ResidentServiceCheckedException, OtpValidationFailedException, ApisResourceAccessException {
    	String vid = "2038096257310540";
		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(Boolean.FALSE);

        residentVidService.revokeVid(vidRevokeRequest, vid, "12345");
    }
    
    @Test(expected = VidRevocationException.class)
    public void apiResourceAccessExceptionTest2() throws ResidentServiceCheckedException, OtpValidationFailedException, ApisResourceAccessException {

        String ERROR_CODE = "err";
        String vid = "2038096257310540";
        ServiceError serviceError = new ServiceError();
        serviceError.setErrorCode(ERROR_CODE);
        serviceError.setMessage("No Record Found");
        ResponseWrapper<VidGeneratorResponseDto> response = new ResponseWrapper<>();
        response.setResponsetime(DateUtils.getCurrentDateTimeString());
        response.setErrors(Lists.newArrayList(serviceError));
    	
		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);
		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);

        when(residentServiceRestClient.patchApi(any(), any(), any(), any())).thenThrow(new ApisResourceAccessException());

        when(identityServiceImpl.getUinForIndividualId(vid)).thenReturn("1234567890");
        residentVidService.revokeVid(vidRevokeRequest,vid, "12345");
    }
    
    @Test(expected = VidRevocationException.class)
    public void idRepoAppExceptionTest() throws ResidentServiceCheckedException, OtpValidationFailedException, ApisResourceAccessException {

        String ERROR_CODE = "err";
        String vid = "2038096257310540";
        ServiceError serviceError = new ServiceError();
        serviceError.setErrorCode(ERROR_CODE);
        serviceError.setMessage("No Record Found");
        ResponseWrapper<VidGeneratorResponseDto> response = new ResponseWrapper<>();
        response.setResponsetime(DateUtils.getCurrentDateTimeString());
        response.setErrors(Lists.newArrayList(serviceError));
    	
		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);
		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);
        when(identityServiceImpl.getUinForIndividualId(vid)).thenReturn("1234567890");

        residentVidService.revokeVid(vidRevokeRequest,vid, "12345");
    }
    
    @Test
    public void testGetVidPolicy() throws ResidentServiceCheckedException, JsonParseException, JsonMappingException, IOException {
    	ReflectionTestUtils.setField(residentVidService, "vidPolicyUrl", "https://dev.mosip.net");
    	ObjectMapper objectMapper = new ObjectMapper();
    	ObjectNode policy = objectMapper.readValue(this.getClass().getClassLoader().getResource("vid_policy.json"),
				ObjectNode.class);
    	when(mapper.readValue(Mockito.any(URL.class), Mockito.any(Class.class))).thenReturn(policy);
    	assertEquals(policy.toString(), residentVidService.getVidPolicy());
    }
    
    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetVidPolicyFailed() throws ResidentServiceCheckedException, JsonParseException, JsonMappingException, IOException {
    	ReflectionTestUtils.setField(residentVidService, "vidPolicyUrl", "https://dev.mosip.net");
    	ObjectMapper objectMapper = new ObjectMapper();
    	ObjectNode policy = objectMapper.readValue(this.getClass().getClassLoader().getResource("vid_policy.json"),
				ObjectNode.class);
    	when(mapper.readValue(Mockito.any(URL.class), Mockito.any(Class.class))).thenThrow(new IOException());
    	residentVidService.getVidPolicy();
    }
}
