package io.mosip.resident.test.service;

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
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.OtpManagerServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.validator.RequestValidator;
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
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.when;

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

    @Mock
    private AuditUtil audit;

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

    @Before
    public void setup() throws ApisResourceAccessException, ResidentServiceCheckedException {
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
        Mockito.when(identityServiceImpl.getResidentIndvidualId()).thenReturn("2123456");
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
}

    @Test
    public void testSendOtpSuccess() throws ResidentServiceCheckedException, IOException, ApisResourceAccessException {
        assertEquals(true, otpManagerService.sendOtp(requestDTO, "EMAIL", "eng"));
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testSendOtpAlreadyOtpSendError() throws ResidentServiceCheckedException, IOException, ApisResourceAccessException {
        when(otpTransactionRepository.checkotpsent(any(), any(), any(), any())).thenReturn(9);
        assertEquals(true, otpManagerService.sendOtp(requestDTO, "EMAIL", "eng"));
    }

    @Test
    public void testSendOtpOtpSendWithinLessTime() throws ResidentServiceCheckedException, IOException, ApisResourceAccessException {
        OtpTransactionEntity otpTransactionEntity = new OtpTransactionEntity();
        assertEquals(true, otpManagerService.sendOtp(requestDTO, "EMAIL", "eng"));
    }

    @Test
    public void testSendOtpPhoneSuccess() throws ResidentServiceCheckedException, IOException, ApisResourceAccessException {
        assertEquals(true, otpManagerService.sendOtp(requestDTO, "PHONE", "eng"));
    }

    @Test
    public void testValidateOtpSuccess() throws ResidentServiceCheckedException, ApisResourceAccessException {
        when(otpTransactionRepository.existsByOtpHashAndStatusCode(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        OtpTransactionEntity otpTransactionEntity = new OtpTransactionEntity();
        otpTransactionEntity.setExpiryDtimes(DateUtils.getUTCCurrentDateTime().plusSeconds(120));
        when(otpTransactionRepository.findTopByOtpHashAndStatusCode(Mockito.anyString(), Mockito.anyString())).thenReturn(otpTransactionEntity);
        assertEquals(true, otpManagerService.validateOtp("111111", "kamesh@gmail.com", "1234565656"));
    }

    @Test(expected = ResidentServiceException.class)
    public void testValidateOtpFailure() throws ResidentServiceCheckedException, ApisResourceAccessException {
        when(otpTransactionRepository.existsByOtpHashAndStatusCode(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        OtpTransactionEntity otpTransactionEntity = new OtpTransactionEntity();
        otpTransactionEntity.setExpiryDtimes(DateUtils.getUTCCurrentDateTime());
        when(otpTransactionRepository.findTopByOtpHashAndStatusCode(Mockito.anyString(), Mockito.anyString())).thenReturn(otpTransactionEntity);
        assertEquals(true, otpManagerService.validateOtp("111111", "kamesh@gmail.com", "1234565656"));
    }

}