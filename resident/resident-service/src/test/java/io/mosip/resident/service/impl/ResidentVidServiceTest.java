package io.mosip.resident.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import io.mosip.resident.util.*;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.idrepository.core.dto.VidPolicy;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.TemplateVariablesConstants;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.dto.NotificationRequestDto;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.VidGeneratorResponseDto;
import io.mosip.resident.dto.VidRequestDto;
import io.mosip.resident.dto.VidRequestDtoV2;
import io.mosip.resident.dto.VidResponseDto;
import io.mosip.resident.dto.VidRevokeRequestDTO;
import io.mosip.resident.dto.VidRevokeRequestDTOV2;
import io.mosip.resident.dto.VidRevokeResponseDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.VidCreationException;
import io.mosip.resident.exception.VidRevocationException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;

@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class ResidentVidServiceTest {

    private static final String LOCALE_EN_US = "en-US";

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
	private Utility utility;
    
	private VidRequestDto requestDto;
    
    private VidRevokeRequestDTO vidRevokeRequest;
    
    @Mock
    private IdentityServiceImpl identityServiceImpl;

    @Mock
    private Utilities utilities;

    @Mock
    private UinVidValidator uinVidValidator;

    @InjectMocks
    private ResidentVidServiceImpl residentVidService;
    
    @Mock
	private ResidentTransactionRepository residentTransactionRepository;
    
    private JSONObject identity;

    private ResponseWrapper<List<Map<String,?>>> vidResponse;

    private String vid;

    private List<Map<String, ?>> vidList;

    private Map<String, Object> vidDetails;

    private IdentityDTO identityValue;
    
	@Value("${perpatual.vid-type:PERPETUAL}")
	private String perpatualVidType;

    @Mock
    private IdentityUtil identityUtil;

    @Mock
    private AvailableClaimUtility availableClaimUtility;

    @Mock
    private PerpetualVidUtility perpetualVidUtility;

    @Before
    public void setup() throws IOException, ResidentServiceCheckedException, ApisResourceAccessException {

        requestDto = new VidRequestDto();
        requestDto.setOtp("123");
        requestDto.setTransactionID("12345");
        requestDto.setIndividualId("1234567890");
        requestDto.setVidType("Temporary");
        
        ReflectionTestUtils.setField(residentVidService, "perpatualVidType", "PERPETUAL");

        NotificationResponseDTO notificationResponseDTO = new NotificationResponseDTO();
        notificationResponseDTO.setMessage("Vid successfully generated");
        notificationResponseDTO.setMaskedEmail("demo@gmail.com");
        notificationResponseDTO.setMaskedPhone("9876543210");

        when(notificationService.sendNotification(any(NotificationRequestDto.class), Mockito.nullable(Map.class))).thenReturn(notificationResponseDTO);
        identityValue = new IdentityDTO();
        identityValue.setEmail("aaa@bbb.com");
        identityValue.setPhone("987654321");
        identityValue.setUIN("123");
		when(identityUtil.getIdentity(Mockito.anyString())).thenReturn(identityValue);
        
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
		
		ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
		residentTransactionEntity.setEventId(UUID.randomUUID().toString());

        vidResponse = new ResponseWrapper<>();
        vidDetails = new HashMap<>();
        vidDetails.put("vidType", "perpetual");
        vidList = new ArrayList<>();
        vidDetails.put("vid", "123");
        vidDetails.put("maskedVid", "1******4");
        vidDetails.put("expiryTimeStamp", "1234343434");
        vidDetails.put("expiryTimestamp", "1516239022");
        vidDetails.put("genratedOnTimestamp", "1234343434");
        vidDetails.put("transactionLimit", 2);
        vidDetails.put("transactionCount", 2);
        vidList.add(vidDetails);
        vidResponse.setResponse(vidList);
        vid = "2038096257310540";
        when(mapper.convertValue("1516239022", LocalDateTime.class)).thenReturn(LocalDateTime.now());
        when(mapper.convertValue("1234343434", LocalDateTime.class)).thenReturn(LocalDateTime.now());
        Mockito.when(utilities.getUinByVid(Mockito.anyString())).thenReturn("123");
    }

    @Test(expected = Exception.class)
    public void generateVidSuccessTest() throws OtpValidationFailedException, IOException, ApisResourceAccessException, ResidentServiceCheckedException {

        String vid = "12345";
        VidGeneratorResponseDto vidGeneratorResponseDto = new VidGeneratorResponseDto();
        vidGeneratorResponseDto.setVidStatus("Active");
        vidGeneratorResponseDto.setVID(vid);
        ResponseWrapper<VidGeneratorResponseDto> response = new ResponseWrapper<>();
        response.setResponsetime(DateUtils.getCurrentDateTimeString());
        response.setResponse(vidGeneratorResponseDto);

		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenThrow(new ApisResourceAccessException());
        when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(response);

        ResponseWrapper<VidResponseDto> result = residentVidService.generateVid(requestDto, vid);
        if(result!=null) {
            assertTrue("Expected Vid should be 12345", result.getResponse().getVid().equalsIgnoreCase(vid));
        }
    }

    @Test(expected = OtpValidationFailedException.class)
    public void otpValidationFailedTest() throws ResidentServiceCheckedException, OtpValidationFailedException {
    	
		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(Boolean.FALSE);
        residentVidService.generateVid(requestDto, "12345");
    }

    @Test(expected = Exception.class)
    public void vidAlreadyExistsExceptionTest() throws ResidentServiceCheckedException, OtpValidationFailedException, ApisResourceAccessException {

        String VID_ALREADY_EXISTS_ERROR_CODE = "IDR-VID-003";

        ServiceError serviceError = new ServiceError();
        serviceError.setErrorCode(VID_ALREADY_EXISTS_ERROR_CODE);
        serviceError.setMessage("Vid already present");
        ResponseWrapper<VidGeneratorResponseDto> response = new ResponseWrapper<>();
        response.setResponsetime(DateUtils.getCurrentDateTimeString());
        response.setErrors(Lists.newArrayList(serviceError));

		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);
        residentVidService.generateVid(requestDto, "12345");
    }

    @Test(expected = Exception.class)
    public void vidCreationExceptionTest() throws ResidentServiceCheckedException, OtpValidationFailedException, ApisResourceAccessException {

        String ERROR_CODE = "err";

        ServiceError serviceError = new ServiceError();
        serviceError.setErrorCode(ERROR_CODE);
        serviceError.setMessage("Vid already present");
        ResponseWrapper<VidGeneratorResponseDto> response = new ResponseWrapper<>();
        response.setResponsetime(DateUtils.getCurrentDateTimeString());
        response.setErrors(Lists.newArrayList(serviceError));

		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);
        residentVidService.generateVid(requestDto, "12345");
    }

    @Test(expected = Exception.class)
    public void apiResourceAccessExceptionTest() throws ResidentServiceCheckedException, OtpValidationFailedException, ApisResourceAccessException {
    	
        String ERROR_CODE = "err";

        ServiceError serviceError = new ServiceError();
        serviceError.setErrorCode(ERROR_CODE);
        serviceError.setMessage("Vid already present");
        ResponseWrapper<VidGeneratorResponseDto> response = new ResponseWrapper<>();
        response.setResponsetime(DateUtils.getCurrentDateTimeString());
        response.setErrors(Lists.newArrayList(serviceError));

		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);
        residentVidService.generateVid(requestDto, "12345");
    }
    
    @Test(expected = Exception.class)
	public void revokeVidSuccessTest() throws OtpValidationFailedException, IOException, ApisResourceAccessException,
			ResidentServiceCheckedException {

		String vid = "1234567890";
	
		VidGeneratorResponseDto dto = new VidGeneratorResponseDto();
		dto.setVidStatus("Deactive");

		ResponseWrapper<VidGeneratorResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(dto);
		responseWrapper.setVersion("v1");
		responseWrapper.setResponsetime(DateUtils.getCurrentDateTimeString());

		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenThrow(new ApisResourceAccessException());
		when(residentServiceRestClient.patchApi(any(), any(), any(), any())).thenReturn(responseWrapper);
        when(uinVidValidator.getUinForIndividualId(vid)).thenReturn("1234567890");

		ResponseWrapper<VidRevokeResponseDTO> result2 = residentVidService.revokeVid(vidRevokeRequest,vid, "1234567890");

		assertEquals("Vid successfully generated", result2.getResponse().getMessage().toString());
	}
    
    @Test(expected = OtpValidationFailedException.class)
    public void otpValidationFailedTest1() throws ResidentServiceCheckedException, OtpValidationFailedException, ApisResourceAccessException, IOException {
    	String vid = "2038096257310540";
		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(Boolean.FALSE);

        residentVidService.revokeVid(vidRevokeRequest, vid, "12345");
    }
    
    @Test(expected = Exception.class)
    public void apiResourceAccessExceptionTest2() throws ResidentServiceCheckedException, OtpValidationFailedException, ApisResourceAccessException, IOException {

        String ERROR_CODE = "err";
        String vid = "2038096257310540";
        ServiceError serviceError = new ServiceError();
        serviceError.setErrorCode(ERROR_CODE);
        serviceError.setMessage("No Record Found");
        ResponseWrapper<VidGeneratorResponseDto> response = new ResponseWrapper<>();
        response.setResponsetime(DateUtils.getCurrentDateTimeString());
        response.setErrors(Lists.newArrayList(serviceError));
    	
		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);

        residentVidService.revokeVid(vidRevokeRequest,vid, "12345");
    }
    
    @Test(expected = Exception.class)
    public void idRepoAppExceptionTest() throws ResidentServiceCheckedException, OtpValidationFailedException, ApisResourceAccessException, IOException {

        String ERROR_CODE = "err";
        String vid = "2038096257310540";
        ServiceError serviceError = new ServiceError();
        serviceError.setErrorCode(ERROR_CODE);
        serviceError.setMessage("No Record Found");
        ResponseWrapper<VidGeneratorResponseDto> response = new ResponseWrapper<>();
        response.setResponsetime(DateUtils.getCurrentDateTimeString());
        response.setErrors(Lists.newArrayList(serviceError));
    	
		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);

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

    @Test
    public void testRetrieveVids() throws ResidentServiceCheckedException, ApisResourceAccessException {
        when(residentServiceRestClient.getApi(Mockito.anyString(), Mockito.any())).thenReturn(vidResponse);
        assertEquals(vidResponse.getResponse().size(),
                perpetualVidUtility.retrieveVids(vid, ResidentConstants.UTC_TIMEZONE_OFFSET, LOCALE_EN_US).getResponse().size());
    }

    @Test(expected = ApisResourceAccessException.class)
    public void testRetrieveVidsFailure() throws ResidentServiceCheckedException, ApisResourceAccessException {
        when(residentServiceRestClient.getApi(Mockito.anyString(), Mockito.any())).thenReturn(new ApisResourceAccessException());
        perpetualVidUtility.retrieveVids(vid, ResidentConstants.UTC_TIMEZONE_OFFSET, LOCALE_EN_US);
    }

    @Test
    public void testRetrieveVidsInvalidYear() throws ResidentServiceCheckedException, ApisResourceAccessException {
        when(mapper.convertValue((Object) any(), (Class<Object>) any())).thenReturn(
                LocalDateTime.of(10000, 12, 1, 12, 12, 12));
        when(residentServiceRestClient.getApi(Mockito.anyString(), Mockito.any())).thenReturn(vidResponse);
        assertEquals(vidResponse.getResponse().size(),
                perpetualVidUtility.retrieveVids(vid, ResidentConstants.UTC_TIMEZONE_OFFSET, LOCALE_EN_US).getResponse().size());
    }

    @Test
    public void testRetrieveVidsInvalidExpiryTimeStamp() throws ResidentServiceCheckedException, ApisResourceAccessException {
        vidDetails.remove("expiryTimestamp");
        vidList.add(vidDetails);
        vidResponse.setResponse(vidList);
        when(residentServiceRestClient.getApi(Mockito.anyString(), Mockito.any())).thenReturn(vidResponse);
        assertEquals(vidResponse.getResponse().size(),
                perpetualVidUtility.retrieveVids(vid, ResidentConstants.UTC_TIMEZONE_OFFSET, LOCALE_EN_US).getResponse().size());
    }

    @Test
    public void testRetrieveVidsNegativeTransactionLimit() throws ResidentServiceCheckedException, ApisResourceAccessException {
        vidDetails.put("transactionLimit", -1);
        vidList.add(vidDetails);
        vidResponse.setResponse(vidList);
        when(mapper.convertValue((Object) any(), (Class<Object>) any())).thenReturn(
                LocalDateTime.of(10000, 12, 1, 12, 12, 12));
        when(residentServiceRestClient.getApi(Mockito.anyString(), Mockito.any())).thenReturn(vidResponse);
        assertEquals(0,
                perpetualVidUtility.retrieveVids(vid, ResidentConstants.UTC_TIMEZONE_OFFSET, LOCALE_EN_US).getResponse().size());
    }

    @Test
    public void testRetrieveVidsNullTransactionLimit() throws ResidentServiceCheckedException, ApisResourceAccessException {
        vidDetails.put("transactionLimit", null);
        vidList.add(vidDetails);
        vidResponse.setResponse(vidList);
        when(mapper.convertValue((Object) any(), (Class<Object>) any())).thenReturn(
                LocalDateTime.of(10000, 12, 1, 12, 12, 12));
        when(residentServiceRestClient.getApi(Mockito.anyString(), Mockito.any())).thenReturn(vidResponse);
        assertEquals(vidResponse.getResponse().size(),
                perpetualVidUtility.retrieveVids(vid, ResidentConstants.UTC_TIMEZONE_OFFSET, LOCALE_EN_US).getResponse().size());
    }
    
	@Test
	public void getPerpatualVidTest() throws ResidentServiceCheckedException, ApisResourceAccessException {
		when(residentServiceRestClient.getApi(Mockito.anyString(), Mockito.any())).thenReturn(vidResponse);
		Optional<String> response = perpetualVidUtility.getPerpatualVid("9054257141");
		Optional<String> perpetualVid = Optional.of("123");
		assertEquals(perpetualVid, response);
	}

	@Test
	public void getPerpatualVidEmptyTest() throws ResidentServiceCheckedException, ApisResourceAccessException {
		vidResponse.setResponse(List.of());
		when(residentServiceRestClient.getApi(Mockito.anyString(), Mockito.any())).thenReturn(vidResponse);
		Optional<String> response = perpetualVidUtility.getPerpatualVid("9054257141");
		assertEquals(Optional.empty(), response);
	}

    @Test
    public void testGenerateVidV2() throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException, IOException {
        IdentityServiceTest.getAuthUserDetailsFromAuthentication();
        Mockito.when(utility.createEntity(Mockito.any())).thenReturn(new ResidentTransactionEntity());
        Mockito.when(utility.createEventId()).thenReturn("1236547899874563");
        Mockito.when(uinVidValidator.getIndividualIdType(Mockito.anyString())).thenReturn(IdType.VID);
        when(residentServiceRestClient.getApi(Mockito.anyString(), Mockito.any())).thenReturn(vidResponse);
        String vidPolicyURL = "https://dev.mosip.net";
        ReflectionTestUtils.setField(residentVidService, "vidPolicyUrl", vidPolicyURL);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode policy = objectMapper.readValue(this.getClass().getClassLoader().getResource("vid_policy.json"),
                ObjectNode.class);
        when(mapper.readValue(Mockito.any(URL.class), Mockito.any(Class.class))).thenReturn(policy);
        Map<Object, Object> vidPolicyMap = new HashMap<>();
        List<Map<String, String>> vidList = new ArrayList<>();
        Map<String, String> vids= new HashMap<>();
        vids.put(TemplateVariablesConstants.VID_TYPE,ResidentConstants.PERPETUAL);
        vids.put("vidPolicy", "vidPolicy");
        vidList.add(vids);
        vidPolicyMap.put("vidPolicies",vidList);
        when(mapper.readValue("{\"vidPolicies\":[{\"vidType\":\"Perpetual\",\"vidPolicy\":{\"validForInMinutes\":null,\"transactionsAllowed\":null,\"instancesAllowed\":1,\"autoRestoreAllowed\":true,\"restoreOnAction\":\"REVOKE\"}},{\"vidType\":\"Temporary\",\"vidPolicy\":{\"validForInMinutes\":30,\"transactionsAllowed\":1,\"instancesAllowed\":5,\"autoRestoreAllowed\":false,\"restoreOnAction\":\"REGENERATE\"}}]}",
                Map.class)).thenReturn(vidPolicyMap);
        VidPolicy vidPolicy = new VidPolicy();
        vidPolicy.setAllowedInstances(1);
        vidPolicy.setAutoRestoreAllowed(true);
        vidPolicy.setRestoreOnAction("true");

        when(mapper.convertValue("vidPolicy", VidPolicy.class)).thenReturn(vidPolicy);
        when(env.getProperty(Mockito.anyString())).thenReturn("false");

        String vid = "12345";
        VidGeneratorResponseDto vidGeneratorResponseDto = new VidGeneratorResponseDto();
        vidGeneratorResponseDto.setVidStatus("Active");
        vidGeneratorResponseDto.setVID(vid);
        ResponseWrapper<VidGeneratorResponseDto> response = new ResponseWrapper<>();
        response.setResponsetime(DateUtils.getCurrentDateTimeString());
        response.setResponse(vidGeneratorResponseDto);
        when(mapper.writeValueAsString(Mockito.any())).thenReturn("response");
        when(mapper.readValue("response", VidGeneratorResponseDto.class)).thenReturn(vidGeneratorResponseDto);
        when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(response);
        NotificationResponseDTO notificationResponseDTO = new NotificationResponseDTO();
        notificationResponseDTO.setMaskedEmail("Ka**g.com");
        notificationResponseDTO.setMaskedPhone("88**09");
        notificationResponseDTO.setStatus("SUCCESS");
        notificationResponseDTO.setMessage("SUCCESS");
        when(notificationService.sendNotification(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.nullable(Map.class))).thenReturn(notificationResponseDTO);
        VidRequestDtoV2 vidRequestDtoV2 = new VidRequestDtoV2();
        vidRequestDtoV2.setVidType("PERPETUAL");
        vidRequestDtoV2.setChannels(List.of("EMAIL"));
        vidRequestDtoV2.setTransactionID("3434232323");
        assertEquals("12345",residentVidService.
                generateVid(vidRequestDtoV2, "123232323").getResponse().getVid());
    }

    @Test(expected = VidRevocationException.class)
    public void testRevokeVidV2Failed() throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException, IOException {
        IdentityServiceTest.getAuthUserDetailsFromAuthentication();
        Mockito.when(utility.createEntity(Mockito.any())).thenReturn(new ResidentTransactionEntity());
        Mockito.when(availableClaimUtility.getIDAToken(Mockito.anyString())).thenReturn("123456789");
        Mockito.when(utility.createEventId()).thenReturn("1236547899874563");
        when(residentServiceRestClient.getApi(Mockito.anyString(), Mockito.any())).thenReturn(vidResponse);
        VidRevokeRequestDTOV2 vidRevokeRequestDTOV2 = new VidRevokeRequestDTOV2();
        vidRevokeRequestDTOV2.setTransactionID("1234567896");
        vidRevokeRequestDTOV2.setVidStatus("Active");
        residentVidService.revokeVid(vidRevokeRequestDTOV2, "2076439409167031", "2037293183 ");
    }

    @Test(expected = VidRevocationException.class)
    public void testRevokeVidV2VidRevocationException() throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException, IOException {
        IdentityServiceTest.getAuthUserDetailsFromAuthentication();
        Mockito.when(utility.createEntity(Mockito.any())).thenReturn(new ResidentTransactionEntity());
        Mockito.when(utility.createEventId()).thenReturn("1236547899874563");
        Mockito.when(availableClaimUtility.getIDAToken(Mockito.anyString())).thenReturn("123456789");
        when(residentServiceRestClient.getApi(Mockito.anyString(), Mockito.any())).thenReturn(vidResponse);
        VidRevokeRequestDTOV2 vidRevokeRequestDTOV2 = new VidRevokeRequestDTOV2();
        vidRevokeRequestDTOV2.setTransactionID("1234567896");
        vidRevokeRequestDTOV2.setVidStatus("Active");
        residentVidService.revokeVid(vidRevokeRequestDTOV2, "2076439409167031", "2037293183 ");
    }

    @Test
    public void testRevokeVidV2Success() throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException, IOException {
        IdentityServiceTest.getAuthUserDetailsFromAuthentication();
        ResponseWrapper<VidGeneratorResponseDto> responseWrapper = new ResponseWrapper<>();
        VidGeneratorResponseDto dto = new VidGeneratorResponseDto();
        dto.setVidStatus("Deactive");
        responseWrapper.setResponse(dto);
        responseWrapper.setVersion("v1");
        responseWrapper.setResponsetime(DateUtils.getCurrentDateTimeString());
        when(residentServiceRestClient.patchApi(any(), any(), any(), any())).thenReturn(responseWrapper);
        Mockito.when(utility.createEntity(Mockito.any())).thenReturn(new ResidentTransactionEntity());
        Mockito.when(utility.createEventId()).thenReturn("1236547899874563");
        Mockito.when(availableClaimUtility.getIDAToken(Mockito.anyString())).thenReturn("123456789");
        when(residentServiceRestClient.getApi(Mockito.anyString(), Mockito.any())).thenReturn(vidResponse);
        VidRevokeRequestDTOV2 vidRevokeRequestDTOV2 = new VidRevokeRequestDTOV2();
        vidRevokeRequestDTOV2.setTransactionID("1234567896");
        vidRevokeRequestDTOV2.setVidStatus("Active");
        assertEquals("Vid successfully generated",
                residentVidService.revokeVid(vidRevokeRequestDTOV2, "2076439409167031", "2037293183 ").getResponse().getMessage());
    }

    @Test
    public void testGenerateVidV2NullNotificationResponse() throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException, IOException {
        IdentityServiceTest.getAuthUserDetailsFromAuthentication();
        Mockito.when(utility.createEntity(Mockito.any())).thenReturn(new ResidentTransactionEntity());
        Mockito.when(utility.createEventId()).thenReturn("1236547899874563");
        Mockito.when(uinVidValidator.getIndividualIdType(Mockito.anyString())).thenReturn(IdType.VID);
        when(residentServiceRestClient.getApi(Mockito.anyString(), Mockito.any())).thenReturn(vidResponse);
        String vidPolicyURL = "https://dev.mosip.net";
        ReflectionTestUtils.setField(residentVidService, "vidPolicyUrl", vidPolicyURL);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode policy = objectMapper.readValue(this.getClass().getClassLoader().getResource("vid_policy.json"),
                ObjectNode.class);
        when(mapper.readValue(Mockito.any(URL.class), Mockito.any(Class.class))).thenReturn(policy);
        Map<Object, Object> vidPolicyMap = new HashMap<>();
        List<Map<String, String>> vidList = new ArrayList<>();
        Map<String, String> vids= new HashMap<>();
        vids.put(TemplateVariablesConstants.VID_TYPE,ResidentConstants.PERPETUAL);
        vids.put("vidPolicy", "vidPolicy");
        vidList.add(vids);
        vidPolicyMap.put("vidPolicies",vidList);
        when(mapper.readValue("{\"vidPolicies\":[{\"vidType\":\"Perpetual\",\"vidPolicy\":{\"validForInMinutes\":null,\"transactionsAllowed\":null,\"instancesAllowed\":1,\"autoRestoreAllowed\":true,\"restoreOnAction\":\"REVOKE\"}},{\"vidType\":\"Temporary\",\"vidPolicy\":{\"validForInMinutes\":30,\"transactionsAllowed\":1,\"instancesAllowed\":5,\"autoRestoreAllowed\":false,\"restoreOnAction\":\"REGENERATE\"}}]}",
                Map.class)).thenReturn(vidPolicyMap);
        VidPolicy vidPolicy = new VidPolicy();
        vidPolicy.setAllowedInstances(1);
        vidPolicy.setAutoRestoreAllowed(true);
        vidPolicy.setRestoreOnAction("true");

        when(mapper.convertValue("vidPolicy", VidPolicy.class)).thenReturn(vidPolicy);
        when(env.getProperty(Mockito.anyString())).thenReturn("false");

        String vid = "12345";
        VidGeneratorResponseDto vidGeneratorResponseDto = new VidGeneratorResponseDto();
        vidGeneratorResponseDto.setVidStatus("Active");
        vidGeneratorResponseDto.setVID(vid);
        ResponseWrapper<VidGeneratorResponseDto> response = new ResponseWrapper<>();
        response.setResponsetime(DateUtils.getCurrentDateTimeString());
        response.setResponse(vidGeneratorResponseDto);
        when(mapper.writeValueAsString(Mockito.any())).thenReturn("response");
        when(mapper.readValue("response", VidGeneratorResponseDto.class)).thenReturn(vidGeneratorResponseDto);
        when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(response);
        NotificationResponseDTO notificationResponseDTO = new NotificationResponseDTO();
        notificationResponseDTO.setMaskedEmail(null);
        notificationResponseDTO.setMaskedPhone(null);
        notificationResponseDTO.setStatus("SUCCESS");
        notificationResponseDTO.setMessage("SUCCESS");
        when(notificationService.sendNotification(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.nullable(Map.class))).thenReturn(notificationResponseDTO);
        VidRequestDtoV2 vidRequestDtoV2 = new VidRequestDtoV2();
        vidRequestDtoV2.setVidType("PERPETUAL");
        vidRequestDtoV2.setChannels(List.of("EMAIL"));
        vidRequestDtoV2.setTransactionID("3434232323");
        assertEquals("12345",residentVidService.
                generateVid(vidRequestDtoV2, "123232323").getResponse().getVid());
    }

    @Test(expected = VidCreationException.class)
    public void testGenerateVidV2NullNotificationResponseApiResourceException() throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException, IOException {
        IdentityServiceTest.getAuthUserDetailsFromAuthentication();
        Mockito.when(utility.createEntity(Mockito.any())).thenReturn(new ResidentTransactionEntity());
        Mockito.when(utility.createEventId()).thenReturn("1236547899874563");
        Mockito.when(uinVidValidator.getIndividualIdType(Mockito.anyString())).thenReturn(IdType.VID);
        when(residentServiceRestClient.getApi(Mockito.anyString(), Mockito.any())).thenReturn(vidResponse);
        String vidPolicyURL = "https://dev.mosip.net";
        ReflectionTestUtils.setField(residentVidService, "vidPolicyUrl", vidPolicyURL);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode policy = objectMapper.readValue(this.getClass().getClassLoader().getResource("vid_policy.json"),
                ObjectNode.class);
        when(mapper.readValue(Mockito.any(URL.class), Mockito.any(Class.class))).thenReturn(policy);
        Map<Object, Object> vidPolicyMap = new HashMap<>();
        List<Map<String, String>> vidList = new ArrayList<>();
        Map<String, String> vids= new HashMap<>();
        vids.put(TemplateVariablesConstants.VID_TYPE,ResidentConstants.PERPETUAL);
        vids.put("vidPolicy", "vidPolicy");
        vidList.add(vids);
        vidPolicyMap.put("vidPolicies",vidList);
        when(mapper.readValue("{\"vidPolicies\":[{\"vidType\":\"Perpetual\",\"vidPolicy\":{\"validForInMinutes\":null,\"transactionsAllowed\":null,\"instancesAllowed\":1,\"autoRestoreAllowed\":true,\"restoreOnAction\":\"REVOKE\"}},{\"vidType\":\"Temporary\",\"vidPolicy\":{\"validForInMinutes\":30,\"transactionsAllowed\":1,\"instancesAllowed\":5,\"autoRestoreAllowed\":false,\"restoreOnAction\":\"REGENERATE\"}}]}",
                Map.class)).thenReturn(vidPolicyMap);
        VidPolicy vidPolicy = new VidPolicy();
        vidPolicy.setAllowedInstances(1);
        vidPolicy.setAutoRestoreAllowed(true);
        vidPolicy.setRestoreOnAction("true");

        when(mapper.convertValue("vidPolicy", VidPolicy.class)).thenReturn(vidPolicy);
        when(env.getProperty(Mockito.anyString())).thenReturn("false");

        String vid = "12345";
        VidGeneratorResponseDto vidGeneratorResponseDto = new VidGeneratorResponseDto();
        vidGeneratorResponseDto.setVidStatus("Active");
        vidGeneratorResponseDto.setVID(vid);
        ResponseWrapper<VidGeneratorResponseDto> response = new ResponseWrapper<>();
        response.setResponsetime(DateUtils.getCurrentDateTimeString());
        response.setResponse(vidGeneratorResponseDto);
        when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenThrow(new ApisResourceAccessException());
        NotificationResponseDTO notificationResponseDTO = new NotificationResponseDTO();
        notificationResponseDTO.setMaskedEmail(null);
        notificationResponseDTO.setMaskedPhone(null);
        notificationResponseDTO.setStatus("SUCCESS");
        notificationResponseDTO.setMessage("SUCCESS");
        VidRequestDtoV2 vidRequestDtoV2 = new VidRequestDtoV2();
        vidRequestDtoV2.setVidType("PERPETUAL");
        vidRequestDtoV2.setChannels(List.of("EMAIL"));
        vidRequestDtoV2.setTransactionID("3434232323");
        assertEquals("12345",residentVidService.
                generateVid(vidRequestDtoV2, "123232323").getResponse().getVid());
    }

}
