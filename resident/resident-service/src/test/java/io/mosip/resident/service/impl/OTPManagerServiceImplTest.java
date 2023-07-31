package io.mosip.resident.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mosip.resident.dto.IdResponseDTO1;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.dto.ResidentUpdateResponseDTO;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.dto.OtpRequestDTOV2;
import io.mosip.resident.entity.OtpTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.repository.OtpTransactionRepository;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ResidentService;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.validator.RequestValidator;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * This class is used to test Otp Manger service impl class.
 * @author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class OTPManagerServiceImplTest {

    @Mock
    private ResidentServiceRestClient residentServiceRestClient;

    @Mock
    Environment env;

    @InjectMocks
    private OtpManagerServiceImpl otpManagerService;

    @Mock
    private IdentityServiceImpl identityServiceImpl;

    @Mock
    @Qualifier("selfTokenRestTemplate")
    private RestTemplate restTemplate;

    @Mock
    private ResidentTransactionRepository residentTransactionRepository;

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    private NotificationResponseDTO notificationResponseDTO;

    private MainRequestDTO<OtpRequestDTOV2> requestDTO;

    private OtpRequestDTOV2 otpRequestDTOV2;


    @Mock
    private OtpTransactionRepository otpTransactionRepository;

    private ResponseWrapper<Map<String, String>> response;

    @Mock
    private Environment environment;

    private ResponseEntity<ResponseWrapper> response1;

    @Mock
    private TemplateUtil templateUtil;

    @Mock
    private RequestValidator requestValidator;

    @Mock
    private ResidentService residentService;

    @Mock
    private Utilities utilities;
    private IdentityDTO identityDTO;

    @Before
    public void setup() throws ApisResourceAccessException, ResidentServiceCheckedException, IOException {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(otpManagerService).build();
        response = new ResponseWrapper<>();
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("otp", "111111");
        responseMap.put("status", "PASSED");
        response.setResponse(responseMap);
        otpRequestDTOV2 = new OtpRequestDTOV2();
        requestDTO = new MainRequestDTO<>();
        otpRequestDTOV2.setTransactionId("1234567891");
        otpRequestDTOV2.setUserId("kamesh@gmail.com");
        requestDTO.setRequest(otpRequestDTOV2);
        Mockito.when(identityServiceImpl.getResidentIndvidualIdFromSession()).thenReturn("2123456");
        when(otpTransactionRepository.checkotpsent(any(), any(), any(), any())).thenReturn(0);
        ResponseWrapper<Map<String, String>> responseMap1=new ResponseWrapper<>();
        responseMap1.setResponse(responseMap);
        response1 = new ResponseEntity<>(responseMap1, HttpStatus.ACCEPTED);
        Mockito.when(environment.getProperty(Mockito.any())).thenReturn("https://dev.mosip.net/v1/otpmanager/otp/generate");
        Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(HttpMethod.class),
                        ArgumentMatchers.any(),
                        Mockito.eq(ResponseWrapper.class)))
                .thenReturn(response1);
        Mockito.when(environment.getProperty(any())).thenReturn("http://localhost:8099");
        Mockito.when(environment.getProperty("otp.request.flooding.duration", Long.class)).thenReturn(45L);
        Mockito.when(environment.getProperty("mosip.kernel.otp.expiry-time", Long.class)).thenReturn(45L);
        Mockito.when(environment.getProperty("otp.request.flooding.max-count", Integer.class)).thenReturn(8);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("UIN", "1234567898");
        String schemaJson = "schema";
        Tuple3<JSONObject, String, IdResponseDTO1> idRepoJsonSchemaJsonAndIdResponseDtoTuple = Tuples.of(jsonObject, schemaJson, new IdResponseDTO1());
        Mockito.when(utilities.
                getIdRepoJsonSchemaJsonAndIdResponseDtoFromIndividualId(Mockito.anyString())).thenReturn(idRepoJsonSchemaJsonAndIdResponseDtoTuple);

    }

    @Test
    public void testSendOtpSuccess() throws ResidentServiceCheckedException, IOException, ApisResourceAccessException {
        assertTrue(otpManagerService.sendOtp(requestDTO, "EMAIL", "eng", identityDTO));
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testSendOtpAlreadyOtpSendError() throws ResidentServiceCheckedException, IOException, ApisResourceAccessException {
        when(otpTransactionRepository.checkotpsent(any(), any(), any(), any())).thenReturn(9);
        assertTrue(otpManagerService.sendOtp(requestDTO, "EMAIL", "eng", identityDTO));
    }

    @Test
    public void testSendOtpOtpSendWithinLessTime() throws ResidentServiceCheckedException, IOException, ApisResourceAccessException {
        OtpTransactionEntity otpTransactionEntity = new OtpTransactionEntity();
        assertTrue(otpManagerService.sendOtp(requestDTO, "EMAIL", "eng", identityDTO));
    }

    @Test
    public void testSendOtpPhoneSuccess() throws ResidentServiceCheckedException, IOException, ApisResourceAccessException {
        assertTrue(otpManagerService.sendOtp(requestDTO, "PHONE", "eng", identityDTO));
    }

    @Test
    public void testValidateOtpSuccess() throws ResidentServiceCheckedException, ApisResourceAccessException {
        when(otpTransactionRepository.existsByOtpHashAndStatusCode(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        OtpTransactionEntity otpTransactionEntity = new OtpTransactionEntity();
        otpTransactionEntity.setExpiryDtimes(DateUtils.getUTCCurrentDateTime().plusSeconds(120));
        when(otpTransactionRepository.findTopByOtpHashAndStatusCode(Mockito.anyString(), Mockito.anyString())).thenReturn(otpTransactionEntity);
        assertTrue(otpManagerService.validateOtp("111111", "kamesh@gmail.com", "1234565656"));
    }

    @Test(expected = ResidentServiceException.class)
    public void testValidateOtpFailure() throws ResidentServiceCheckedException, ApisResourceAccessException {
        when(otpTransactionRepository.existsByOtpHashAndStatusCode(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        OtpTransactionEntity otpTransactionEntity = new OtpTransactionEntity();
        otpTransactionEntity.setExpiryDtimes(DateUtils.getUTCCurrentDateTime());
        when(otpTransactionRepository.findTopByOtpHashAndStatusCode(Mockito.anyString(), Mockito.anyString())).thenReturn(otpTransactionEntity);
        assertTrue(otpManagerService.validateOtp("111111", "kamesh@gmail.com", "1234565656"));
    }

    @Test(expected = ResidentServiceException.class)
    public void testSendOtpPhoneBlockedOtp() throws ResidentServiceCheckedException, IOException, ApisResourceAccessException {
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("otp", "111111");
        responseMap.put("status", "USER_BLOCKED");
        response.setResponse(responseMap);
        ResponseWrapper<Map<String, String>> responseMap1=new ResponseWrapper<>();
        responseMap1.setResponse(responseMap);
        response1 = new ResponseEntity<>(responseMap1, HttpStatus.ACCEPTED);
        Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(HttpMethod.class),
                        ArgumentMatchers.any(),
                        Mockito.eq(ResponseWrapper.class)))
                .thenReturn(response1);
        assertTrue(otpManagerService.sendOtp(requestDTO, "PHONE", "eng", identityDTO));
    }

    @Test(expected = ResidentServiceException.class)
    public void testSendOtpPhoneServerError() throws ResidentServiceCheckedException, IOException, ApisResourceAccessException {
        Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(HttpMethod.class),
                        ArgumentMatchers.any(),
                        Mockito.eq(ResponseWrapper.class)))
                .thenThrow(new RestClientException("error"));
        assertTrue(otpManagerService.sendOtp(requestDTO, "PHONE", "eng", identityDTO));
    }

    @Test
    public void testValidateOtpOtpHashExists() throws ResidentServiceCheckedException, ApisResourceAccessException {
        when(otpTransactionRepository.existsByOtpHashAndStatusCode(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
        assertFalse(otpManagerService.validateOtp("111111", "kamesh@gmail.com", "1234565656"));
    }

    @Test
    public void testUpdateUserIdWithEmail() throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("IDSchemaVersion", "0.1");
        when(requestValidator.validateUserIdAndTransactionId(Mockito.anyString(), Mockito.anyString())).thenReturn(List.of("EMAIL"));
        Mockito.when(residentService.reqUinUpdate(Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Tuples.of(new ResidentUpdateResponseDTO(), "passed"));
        assertEquals("passed",otpManagerService.updateUserId("kam@g.com", "1232323232").getT2());
    }

    @Test
    public void testUpdateUserIdWithPhone() throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("IDSchemaVersion", "0.1");
        when(requestValidator.validateUserIdAndTransactionId(Mockito.anyString(), Mockito.anyString())).thenReturn(List.of("PHONE"));
        Mockito.when(residentService.reqUinUpdate(Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Tuples.of(new ResidentUpdateResponseDTO(), "passed"));
        assertEquals("passed",otpManagerService.updateUserId("kam@g.com", "1232323232").getT2());
    }
}