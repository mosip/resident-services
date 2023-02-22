package io.mosip.resident.test.service;

import io.mosip.kernel.core.authmanager.model.AuthNResponse;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.preregistration.application.constant.PreRegLoginConstant;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.dto.MainResponseDTO;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.dto.OtpRequestDTOV2;
import io.mosip.resident.dto.OtpRequestDTOV3;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.repository.OtpTransactionRepository;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.OtpManager;
import io.mosip.resident.service.ResidentService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ProxyOtpServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.util.Utility;
import io.mosip.resident.validator.RequestValidator;
import reactor.util.function.Tuples;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

/**
 * This class is used to test proxy otp service impl class.
 * @author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class ProxyOtpServiceImpllTest {

    @Mock
    private ResidentServiceRestClient residentServiceRestClient;

    @Mock
    Environment env;

    @Mock
    private AuditUtil audit;

    @InjectMocks
    private ProxyOtpServiceImpl proxyOtpService;

    @Mock
    private IdentityServiceImpl identityServiceImpl;

    @Mock
    private Utility utility;

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
    private OtpManager otpManager;

    private ResponseEntity<MainResponseDTO<AuthNResponse>> responseEntity;

    @Before
    public void setup() throws ApisResourceAccessException, ResidentServiceCheckedException, IOException {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(proxyOtpService).build();
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
        ResponseWrapper<Map<String, String>> responseMap1 = new ResponseWrapper<>();
        responseMap1.setResponse(responseMap);
        response1 = new ResponseEntity<>(responseMap1, HttpStatus.ACCEPTED);
        Mockito.when(requestValidator.validateUserIdAndTransactionId(Mockito.anyString(), Mockito.anyString())).thenReturn(List.of("EMAIL"));
        ReflectionTestUtils.setField(proxyOtpService, "mandatoryLanguage", "eng");
        Mockito.when(otpManager.sendOtp(any(), any(), any())).thenReturn(true);
        AuthNResponse authNResponse = new AuthNResponse(PreRegLoginConstant.EMAIL_SUCCESS, PreRegLoginConstant.SUCCESS);
        MainResponseDTO<AuthNResponse> response = new MainResponseDTO<>();
        response.setResponse(authNResponse);
        responseEntity = new ResponseEntity<>(HttpStatus.OK);
    }

    @Test
    public void testSendOtpEmailSuccess() {
        assertEquals(responseEntity.getStatusCode(), proxyOtpService.sendOtp(requestDTO).getStatusCode());
    }

    @Test
    public void testSendOtpPhoneSuccess() {
        otpRequestDTOV2.setUserId("8809463737");
        requestDTO.setRequest(otpRequestDTOV2);
        Mockito.when(requestValidator.validateUserIdAndTransactionId(Mockito.anyString(),
                Mockito.anyString())).thenReturn(List.of("PHONE"));
        assertEquals(responseEntity.getStatusCode(), proxyOtpService.sendOtp(requestDTO).getStatusCode());
    }

    @Test
    public void testSendOtpFailure() throws ResidentServiceCheckedException, IOException, ApisResourceAccessException {
        Mockito.when(otpManager.sendOtp(any(), any(), any())).thenReturn(false);
        assertEquals(responseEntity.getStatusCode(), proxyOtpService.sendOtp(requestDTO).getStatusCode());
    }

    @Test(expected = ResidentServiceException.class)
    public void testHttpServerErrorException() throws ResidentServiceCheckedException, IOException, ApisResourceAccessException {
        Mockito.when(otpManager.sendOtp(any(), any(), any())).thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));
        assertEquals(responseEntity.getStatusCode(), proxyOtpService.sendOtp(requestDTO).getStatusCode());
    }

    @Test(expected = ResidentServiceException.class)
    public void testHttpClientErrorException() throws ResidentServiceCheckedException, IOException, ApisResourceAccessException {
        Mockito.when(otpManager.sendOtp(any(), any(), any())).thenThrow(new HttpClientErrorException(HttpStatus.BAD_GATEWAY));
        assertEquals(responseEntity.getStatusCode(), proxyOtpService.sendOtp(requestDTO).getStatusCode());
    }

    @Test(expected = ResidentServiceException.class)
    public void testResidentServiceException() throws ResidentServiceCheckedException, IOException, ApisResourceAccessException {
        Mockito.when(otpManager.sendOtp(any(), any(), any()))
                .thenThrow(new ResidentServiceException(ResidentErrorCode.SEND_OTP_FAILED.getErrorCode(),
                        ResidentErrorCode.SEND_OTP_FAILED.getErrorMessage()));
        assertEquals(responseEntity.getStatusCode(), proxyOtpService.sendOtp(requestDTO).getStatusCode());
    }

    @Test(expected = ResidentServiceException.class)
    public void testValidateOtpFailure() {
        MainRequestDTO<OtpRequestDTOV3> requestDTO1 = new MainRequestDTO<>();
        OtpRequestDTOV3 otpRequestDTOV3 = new OtpRequestDTOV3();
        otpRequestDTOV3.setOtp("11111");
        otpRequestDTOV3.setUserId("ka@gm.com");
        otpRequestDTOV3.setTransactionId("122222222");
        requestDTO1.setRequest(otpRequestDTOV3);
        assertEquals("12345", proxyOtpService.validateWithUserIdOtp(requestDTO1).getT2());
    }

    @Test
    public void testValidateOtpSuccess() throws ResidentServiceCheckedException, ApisResourceAccessException {
        MainRequestDTO<OtpRequestDTOV3> requestDTO1 = new MainRequestDTO<>();
        OtpRequestDTOV3 otpRequestDTOV3 = new OtpRequestDTOV3();
        otpRequestDTOV3.setOtp("11111");
        otpRequestDTOV3.setUserId("ka@gm.com");
        otpRequestDTOV3.setTransactionId("122222222");
        requestDTO1.setRequest(otpRequestDTOV3);
        Mockito.when(otpManager.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(otpManager.updateUserId(Mockito.anyString(), Mockito.anyString())).thenReturn(Tuples.of(new Object(), "12345"));
        assertEquals("12345", proxyOtpService.validateWithUserIdOtp(requestDTO1).getT2());
    }

    @Test(expected = ResidentServiceException.class)
    public void testValidateOtpFailureResidentServiceException() throws ResidentServiceCheckedException, ApisResourceAccessException {
        MainRequestDTO<OtpRequestDTOV3> requestDTO1 = new MainRequestDTO<>();
        OtpRequestDTOV3 otpRequestDTOV3 = new OtpRequestDTOV3();
        otpRequestDTOV3.setOtp("11111");
        otpRequestDTOV3.setUserId("ka@gm.com");
        otpRequestDTOV3.setTransactionId("122222222");
        requestDTO1.setRequest(otpRequestDTOV3);
        Mockito.when(otpManager.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).
                thenThrow(new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
                        ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage()));
        assertEquals("12345", proxyOtpService.validateWithUserIdOtp(requestDTO1).getT2());
    }

    @Test(expected = ResidentServiceException.class)
    public void testValidateOtpFailureRuntimeException() throws ResidentServiceCheckedException, ApisResourceAccessException {
        MainRequestDTO<OtpRequestDTOV3> requestDTO1 = new MainRequestDTO<>();
        OtpRequestDTOV3 otpRequestDTOV3 = new OtpRequestDTOV3();
        otpRequestDTOV3.setOtp("11111");
        otpRequestDTOV3.setUserId("ka@gm.com");
        otpRequestDTOV3.setTransactionId("122222222");
        requestDTO1.setRequest(otpRequestDTOV3);
        Mockito.when(otpManager.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).
                thenThrow(new RuntimeException());
        assertEquals("12345", proxyOtpService.validateWithUserIdOtp(requestDTO1).getT2());
    }

    @Test(expected = ResidentServiceException.class)
    public void testValidateOtpFailureResidentServiceCheckedException() throws ResidentServiceCheckedException, ApisResourceAccessException {
        MainRequestDTO<OtpRequestDTOV3> requestDTO1 = new MainRequestDTO<>();
        OtpRequestDTOV3 otpRequestDTOV3 = new OtpRequestDTOV3();
        otpRequestDTOV3.setOtp("11111");
        otpRequestDTOV3.setUserId("ka@gm.com");
        otpRequestDTOV3.setTransactionId("122222222");
        requestDTO1.setRequest(otpRequestDTOV3);
        Mockito.when(otpManager.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).
                thenThrow(new ResidentServiceCheckedException());
        assertEquals("12345", proxyOtpService.validateWithUserIdOtp(requestDTO1).getT2());
    }

    @Test(expected = ResidentServiceException.class)
    public void testValidateOtpFailureApiResourceException() throws ResidentServiceCheckedException, ApisResourceAccessException {
        MainRequestDTO<OtpRequestDTOV3> requestDTO1 = new MainRequestDTO<>();
        OtpRequestDTOV3 otpRequestDTOV3 = new OtpRequestDTOV3();
        otpRequestDTOV3.setOtp("11111");
        otpRequestDTOV3.setUserId("ka@gm.com");
        otpRequestDTOV3.setTransactionId("122222222");
        requestDTO1.setRequest(otpRequestDTOV3);
        Mockito.when(otpManager.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).
                thenThrow(new ApisResourceAccessException());
        assertEquals("12345", proxyOtpService.validateWithUserIdOtp(requestDTO1).getT2());
    }

}