package io.mosip.resident.service.impl;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import io.mosip.resident.dto.IdentityDTO;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.keygenerator.bouncycastle.KeyGenerator;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.AuthError;
import io.mosip.resident.dto.AuthResponseDTO;
import io.mosip.resident.dto.AuthTypeStatusResponseDto;
import io.mosip.resident.dto.AutnTxnDto;
import io.mosip.resident.dto.AutnTxnResponseDto;
import io.mosip.resident.dto.ErrorDTO;
import io.mosip.resident.dto.IdAuthResponseDto;
import io.mosip.resident.dto.PublicKeyResponseDto;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.CertificateException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utility;

@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class IdAuthServiceTest {
	
    @MockBean
    private ProxyIdRepoService proxyIdRepoService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ObjectMapper mapper;

    @Mock
    private SecretKey secretKey;

    @Mock
    private KeyGenerator keyGenerator;

    @Mock
    private Environment environment;

    @Mock
    private ResidentServiceRestClient restClient;

    @Mock
    private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> encryptor;

    @InjectMocks
    private IdAuthService idAuthService = new IdAuthServiceImpl();

    @Mock
    private ResidentTransactionRepository residentTransactionRepository;

    @Mock
    private IdentityServiceImpl identityService;

    @Mock
    private NotificationService notificationService;
    
    @Mock
    private Utility utility;

    @Before
    public void setup() throws ResidentServiceCheckedException {
        IdentityDTO identityDTO1 = new IdentityDTO();
        identityDTO1.setUIN("234");
        Mockito.when(identityService.getIdentity(Mockito.anyString())).thenReturn(identityDTO1);
    }

    @Test
    public void testAuthTypeStatusUpdateSuccess() throws ApisResourceAccessException, ResidentServiceCheckedException {
        AuthTypeStatusResponseDto authTypeStatusResponseDto = new AuthTypeStatusResponseDto();
        when(restClient.postApi(any(), any(), any(), any())).thenReturn(authTypeStatusResponseDto);
        List<String> authTypes = new ArrayList<>();
        authTypes.add("bio");
        Map<String, AuthTypeStatus> authTypeStatusMap=authTypes.stream().distinct().collect(Collectors.toMap(Function.identity(), str -> AuthTypeStatus.LOCK));
        Map<String, Long> unlockForSecondsMap=authTypes.stream().distinct().collect(Collectors.toMap(Function.identity(), str -> 10L));
        String requestId = idAuthService.authTypeStatusUpdate("1234567891", authTypeStatusMap, unlockForSecondsMap);
        assertTrue(requestId != null && !requestId.isEmpty());
    }

    @Test(expected = ApisResourceAccessException.class)
    public void testAuthTypeStatusUpdateFailure() throws ApisResourceAccessException, ResidentServiceCheckedException {

        when(restClient.postApi(any(), any(), any(), any())).thenThrow(new ApisResourceAccessException());
        List<String> authTypes = new ArrayList<>();
        authTypes.add("bio-FIR");
        Map<String, AuthTypeStatus> authTypeStatusMap=authTypes.stream().distinct().collect(Collectors.toMap(Function.identity(), str -> AuthTypeStatus.LOCK));
        Map<String, Long> unlockForSecondsMap=authTypes.stream().distinct().collect(Collectors.toMap(Function.identity(), str -> 10L));
        String requestId = idAuthService.authTypeStatusUpdate("1234567891", authTypeStatusMap, unlockForSecondsMap);
        assertTrue(requestId != null && !requestId.isEmpty());
    }

    @Test(expected = CertificateException.class)
    public void validateOtpSuccessThrowsAPIsResourceAccessExceptionTest() throws IOException, ApisResourceAccessException, OtpValidationFailedException, ResidentServiceCheckedException {
        String transactionID = "12345";
        String individualId = "individual";
        String individualIdType = IdType.UIN.name();
        String otp = "12345";

        String request = "request";

        IdAuthResponseDto authResponse = new IdAuthResponseDto();
        authResponse.setAuthStatus(true);
        AuthResponseDTO response = new AuthResponseDTO();
        response.setResponse(authResponse);

        PublicKeyResponseDto responseDto = new PublicKeyResponseDto();
        responseDto.setCertificate("Certificate String");
        responseDto.setPublicKey(
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApGh1E3bppaeL8pznuRFx-diebah_ZIcIqs_uCJFvK-x2FkWi0F73tzTYYXE6R-peMmfgjMz8OVIcILEFylVpeQEPHy9ChNEhdSI861zSDbhW_aPPUMWgUOsMzD3b_b5IPLKODUWsGoeY2U8uwjLeVQjje89RK5z080C8SmhX0NRNPkfgX4K71kpqcP6ROKQMhHZ5m8ezdVb_AogndFx8Jw8A1CgIOPfFMY7z-l5UbH8afOydrtH2nShb5HAal5vX4tGOyv0KsZIrBR3YquNfw9vEzmHfrvt_0xrYubasbh3_Fnal57LY-GdQ7XKf9OPXJGDL4B85Z_gkbvefYhFflwIDAQAB");
        ResponseWrapper<PublicKeyResponseDto> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponse(responseDto);

        when(keyGenerator.getSymmetricKey()).thenReturn(secretKey);
        when(encryptor.symmetricEncrypt(any(), any(), any())).thenReturn(request.getBytes());
        when(restClient.getApi((URI)any(), any(Class.class))).thenReturn(responseWrapper);
        when(environment.getProperty(anyString())).thenReturn("dummy url");

        doReturn(objectMapper.writeValueAsString(responseDto)).when(mapper).writeValueAsString(any());
        doReturn(responseDto).when(mapper).readValue(anyString(), any(Class.class));

        idAuthService.validateOtp(transactionID, individualId, otp);
    }

    @Test
    public void validateOtpSuccessTest() throws IOException, ApisResourceAccessException, OtpValidationFailedException, ResidentServiceCheckedException {
        String transactionID = "12345";
        String individualId = "individual";
        String individualIdType = IdType.UIN.name();
        String otp = "12345";

        String request = "request";
        
        ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
        residentTransactionEntity.setEventId("12345");

        IdAuthResponseDto authResponse = new IdAuthResponseDto();
        authResponse.setAuthStatus(true);
        AuthResponseDTO response = new AuthResponseDTO();
        response.setResponse(authResponse);

        String certificate = "-----BEGIN CERTIFICATE-----\n" +
                "MIIDrTCCApWgAwIBAgIIrpI6A2r3eqswDQYJKoZIhvcNAQELBQAwdjELMAkGA1UE\n" +
                "BhMCSU4xCzAJBgNVBAgMAktBMRIwEAYDVQQHDAlCQU5HQUxPUkUxDTALBgNVBAoM\n" +
                "BElJVEIxIDAeBgNVBAsMF01PU0lQLVRFQ0gtQ0VOVEVSIChJREEpMRUwEwYDVQQD\n" +
                "DAx3d3cubW9zaXAuaW8wHhcNMjExMjIyMTUxMjA0WhcNMjMxMjIyMTUxMjA0WjB2\n" +
                "MQswCQYDVQQGEwJJTjELMAkGA1UECAwCS0ExEjAQBgNVBAcMCUJBTkdBTE9SRTEN\n" +
                "MAsGA1UECgwESUlUQjEgMB4GA1UECwwXTU9TSVAtVEVDSC1DRU5URVIgKElEQSkx\n" +
                "FTATBgNVBAMMDElEQS1JTlRFUk5BTDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCC\n" +
                "AQoCggEBAJzaknlQZ6P/a8SI7GvrXa3ZnLqYwAC4c6kR7+rwPZRRlPHbGPCl9rLm\n" +
                "Wgv2JNJLWK8OMa/U9TgWyogU3sXt+r5hXpBLc2dwqLEq6zPinjLoIG7l1Lu/vJCb\n" +
                "0xXwllhjNqYFmsOb3ic3DoSycdXSj63oX+oHk3ghvRtcWzvGJPprsYOzyqSQmfqZ\n" +
                "/TQJ7/JRajsY7oEcbMYa0uelWgrkvMZwEMhESQHzMdjQUzplAkk5hfFhCagIGoz8\n" +
                "xF9JiGAtYvI98p2ZOyBqPC8zhVPlPa2R/zs2cZuPf2Pct46jMoMQ0KRhWUFJ89hz\n" +
                "YKV7XtDJk0hBaVkLeuDJSQxZ+voDTW8CAwEAAaM/MD0wDAYDVR0TAQH/BAIwADAd\n" +
                "BgNVHQ4EFgQU7+vHTy+sk1PhZcHuza7JFymIY0IwDgYDVR0PAQH/BAQDAgUgMA0G\n" +
                "CSqGSIb3DQEBCwUAA4IBAQA9ovMUDmV6ffQR9ZXUpWcPTMZc7ypMTia7Vtpxp1A3\n" +
                "T0P9d5lN9RjRYM6WWZ7URN12vBm2O58BiD/RqBz3gu6nADQ8gdj3WDtTlb8UBOZF\n" +
                "Z84qso9qmd4T4Xr/y3lRRcxLLjytRhfnivS1VWaiOD5nQhz3HEHT1t1/Ldg786k3\n" +
                "ZK3y6vmLZpbh1Ot0zT/gS5J+WmjYjefE4WG6PlhyypyTi+cWeWV+LRIAEYNRA9+G\n" +
                "4VObWa+kqa/YvTDRcJ+PcjK/W2N+7Dg/FTui0eqavoSlIEy2KEbNTSDd1HK3qPUf\n" +
                "RcvjiQLoyaZFsyWDXXucWu/kwUg++xvceSWdAa2gXFlW\n" +
                "-----END CERTIFICATE-----";
        PublicKeyResponseDto responseDto = new PublicKeyResponseDto();
        responseDto.setCertificate(certificate);
        responseDto.setPublicKey(
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApGh1E3bppaeL8pznuRFx-diebah_ZIcIqs_uCJFvK-x2FkWi0F73tzTYYXE6R-peMmfgjMz8OVIcILEFylVpeQEPHy9ChNEhdSI861zSDbhW_aPPUMWgUOsMzD3b_b5IPLKODUWsGoeY2U8uwjLeVQjje89RK5z080C8SmhX0NRNPkfgX4K71kpqcP6ROKQMhHZ5m8ezdVb_AogndFx8Jw8A1CgIOPfFMY7z-l5UbH8afOydrtH2nShb5HAal5vX4tGOyv0KsZIrBR3YquNfw9vEzmHfrvt_0xrYubasbh3_Fnal57LY-GdQ7XKf9OPXJGDL4B85Z_gkbvefYhFflwIDAQAB");
        ResponseWrapper<PublicKeyResponseDto> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponse(responseDto);

        when(keyGenerator.getSymmetricKey()).thenReturn(secretKey);
        when(encryptor.symmetricEncrypt(any(), any(), any())).thenReturn(request.getBytes());
        when(restClient.getApi((URI)any(), any(Class.class))).thenReturn(responseWrapper);
        when(environment.getProperty(anyString())).thenReturn("dummy url");

        doReturn(objectMapper.writeValueAsString(responseDto)).when(mapper).writeValueAsString(any());
        doReturn(responseDto).when(mapper).readValue(anyString(), any(Class.class));

        when(encryptor.asymmetricEncrypt(any(), any())).thenReturn(request.getBytes());

        when(restClient.postApi(any(), any(), any(), any(Class.class))).thenReturn(response);
		when(residentTransactionRepository.findTopByRequestTrnIdAndTokenIdAndStatusCodeInOrderByCrDtimesDesc(anyString(),
				anyString(), anyList())).thenReturn(residentTransactionEntity);

        boolean result = idAuthService.validateOtp(transactionID, individualId, otp);

        assertThat("Expected otp validation successful", result, is(true));
    }

    @Test(expected = OtpValidationFailedException.class)
    public void otpValidationFailedTest()
            throws IOException, ApisResourceAccessException, OtpValidationFailedException, ResidentServiceCheckedException {
        String transactionID = "12345";
        String individualId = "individual";
        String otp = "12345";

        String request = "request";

        String certificate = "-----BEGIN CERTIFICATE-----\n" +
                "MIIDrTCCApWgAwIBAgIIrpI6A2r3eqswDQYJKoZIhvcNAQELBQAwdjELMAkGA1UE\n" +
                "BhMCSU4xCzAJBgNVBAgMAktBMRIwEAYDVQQHDAlCQU5HQUxPUkUxDTALBgNVBAoM\n" +
                "BElJVEIxIDAeBgNVBAsMF01PU0lQLVRFQ0gtQ0VOVEVSIChJREEpMRUwEwYDVQQD\n" +
                "DAx3d3cubW9zaXAuaW8wHhcNMjExMjIyMTUxMjA0WhcNMjMxMjIyMTUxMjA0WjB2\n" +
                "MQswCQYDVQQGEwJJTjELMAkGA1UECAwCS0ExEjAQBgNVBAcMCUJBTkdBTE9SRTEN\n" +
                "MAsGA1UECgwESUlUQjEgMB4GA1UECwwXTU9TSVAtVEVDSC1DRU5URVIgKElEQSkx\n" +
                "FTATBgNVBAMMDElEQS1JTlRFUk5BTDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCC\n" +
                "AQoCggEBAJzaknlQZ6P/a8SI7GvrXa3ZnLqYwAC4c6kR7+rwPZRRlPHbGPCl9rLm\n" +
                "Wgv2JNJLWK8OMa/U9TgWyogU3sXt+r5hXpBLc2dwqLEq6zPinjLoIG7l1Lu/vJCb\n" +
                "0xXwllhjNqYFmsOb3ic3DoSycdXSj63oX+oHk3ghvRtcWzvGJPprsYOzyqSQmfqZ\n" +
                "/TQJ7/JRajsY7oEcbMYa0uelWgrkvMZwEMhESQHzMdjQUzplAkk5hfFhCagIGoz8\n" +
                "xF9JiGAtYvI98p2ZOyBqPC8zhVPlPa2R/zs2cZuPf2Pct46jMoMQ0KRhWUFJ89hz\n" +
                "YKV7XtDJk0hBaVkLeuDJSQxZ+voDTW8CAwEAAaM/MD0wDAYDVR0TAQH/BAIwADAd\n" +
                "BgNVHQ4EFgQU7+vHTy+sk1PhZcHuza7JFymIY0IwDgYDVR0PAQH/BAQDAgUgMA0G\n" +
                "CSqGSIb3DQEBCwUAA4IBAQA9ovMUDmV6ffQR9ZXUpWcPTMZc7ypMTia7Vtpxp1A3\n" +
                "T0P9d5lN9RjRYM6WWZ7URN12vBm2O58BiD/RqBz3gu6nADQ8gdj3WDtTlb8UBOZF\n" +
                "Z84qso9qmd4T4Xr/y3lRRcxLLjytRhfnivS1VWaiOD5nQhz3HEHT1t1/Ldg786k3\n" +
                "ZK3y6vmLZpbh1Ot0zT/gS5J+WmjYjefE4WG6PlhyypyTi+cWeWV+LRIAEYNRA9+G\n" +
                "4VObWa+kqa/YvTDRcJ+PcjK/W2N+7Dg/FTui0eqavoSlIEy2KEbNTSDd1HK3qPUf\n" +
                "RcvjiQLoyaZFsyWDXXucWu/kwUg++xvceSWdAa2gXFlW\n" +
                "-----END CERTIFICATE-----";
        PublicKeyResponseDto responseDto = new PublicKeyResponseDto();
        responseDto.setCertificate(certificate);
        responseDto.setPublicKey(
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApGh1E3bppaeL8pznuRFx-diebah_ZIcIqs_uCJFvK-x2FkWi0F73tzTYYXE6R-peMmfgjMz8OVIcILEFylVpeQEPHy9ChNEhdSI861zSDbhW_aPPUMWgUOsMzD3b_b5IPLKODUWsGoeY2U8uwjLeVQjje89RK5z080C8SmhX0NRNPkfgX4K71kpqcP6ROKQMhHZ5m8ezdVb_AogndFx8Jw8A1CgIOPfFMY7z-l5UbH8afOydrtH2nShb5HAal5vX4tGOyv0KsZIrBR3YquNfw9vEzmHfrvt_0xrYubasbh3_Fnal57LY-GdQ7XKf9OPXJGDL4B85Z_gkbvefYhFflwIDAQAB");
        ResponseWrapper<PublicKeyResponseDto> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponse(responseDto);

        ErrorDTO error = new ErrorDTO(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(), ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage());
        List<ErrorDTO> errorResponse = new ArrayList<>();
        errorResponse.add(error);

        IdAuthResponseDto authResponse = new IdAuthResponseDto();
        authResponse.setAuthStatus(true);
        AuthResponseDTO response = new AuthResponseDTO();
        response.setResponse(authResponse);
        response.setErrors(errorResponse);

        when(keyGenerator.getSymmetricKey()).thenReturn(secretKey);
        when(encryptor.symmetricEncrypt(any(), any(), any())).thenReturn(request.getBytes());
        when(restClient.getApi((URI)any(), any(Class.class))).thenReturn(responseWrapper);
        when(environment.getProperty(anyString())).thenReturn("dummy url");

        doReturn(objectMapper.writeValueAsString(responseDto)).when(mapper).writeValueAsString(any());
        doReturn(responseDto).when(mapper).readValue(anyString(), any(Class.class));
        when(restClient.postApi(any(), any(), any(), any(Class.class))).thenReturn(response);

        idAuthService.validateOtp(transactionID, individualId, otp);
    }

    @Test(expected = Exception.class)
    public void idAuthErrorsTest() throws IOException, ApisResourceAccessException, OtpValidationFailedException, ResidentServiceCheckedException {
        String transactionID = "12345";
        String individualId = "individual";
        String individualIdType = IdType.UIN.name();
        String otp = "12345";

        String request = "request";

        ErrorDTO errorDTO = new ErrorDTO("errorId", "errorMessage");
        AuthResponseDTO response = new AuthResponseDTO();
        response.setErrors(Lists.newArrayList(errorDTO));

        PublicKeyResponseDto responseDto = new PublicKeyResponseDto();
        responseDto.setPublicKey(
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApGh1E3bppaeL8pznuRFx-diebah_ZIcIqs_uCJFvK-x2FkWi0F73tzTYYXE6R-peMmfgjMz8OVIcILEFylVpeQEPHy9ChNEhdSI861zSDbhW_aPPUMWgUOsMzD3b_b5IPLKODUWsGoeY2U8uwjLeVQjje89RK5z080C8SmhX0NRNPkfgX4K71kpqcP6ROKQMhHZ5m8ezdVb_AogndFx8Jw8A1CgIOPfFMY7z-l5UbH8afOydrtH2nShb5HAal5vX4tGOyv0KsZIrBR3YquNfw9vEzmHfrvt_0xrYubasbh3_Fnal57LY-GdQ7XKf9OPXJGDL4B85Z_gkbvefYhFflwIDAQAB");
        ResponseWrapper<PublicKeyResponseDto> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponse(responseDto);

        when(keyGenerator.getSymmetricKey()).thenReturn(secretKey);
        when(encryptor.symmetricEncrypt(any(), any(), any())).thenReturn(request.getBytes());

        doReturn(objectMapper.writeValueAsString(responseDto)).when(mapper).writeValueAsString(any());

        idAuthService.validateOtp(transactionID, individualId, otp);
    }

    @Test
    public void testGetAuthHistoryDetailsSuccess() throws ApisResourceAccessException {
        AutnTxnResponseDto response = new AutnTxnResponseDto();
        AutnTxnDto autnTxnDto = new AutnTxnDto();
        autnTxnDto.setAuthtypeCode("OTP-AUTH");
        autnTxnDto.setEntityName("ida_app_user");
        autnTxnDto.setReferenceIdType("UIN");
        autnTxnDto.setRequestdatetime(DateUtils.getUTCCurrentDateTime());
        autnTxnDto.setStatusCode("N");
        autnTxnDto.setStatusComment("OTP Authentication Failed");
        autnTxnDto.setTransactionID("1111122222");
        Map<String, List<AutnTxnDto>> responsemap = new HashMap<>();
        responsemap.put("authTransactions", Arrays.asList(autnTxnDto));
        response.setResponse(responsemap);
        when(restClient.getApi(any(), any(), anyString(), any(), any(Class.class))).thenReturn(response);
        assertEquals("OTP-AUTH", idAuthService.getAuthHistoryDetails("1234", "1", "1").get(0).getAuthModality());
    }

    @Test
    public void testGetAuthHistoryDetailsServiceErrors() throws ApisResourceAccessException {
        AutnTxnResponseDto response = new AutnTxnResponseDto();
        AuthError error = new AuthError("e", "e");
        response.setErrors(Arrays.asList(error));
        when(restClient.getApi(any(), any(), anyString(), any(), any(Class.class))).thenReturn(response);
        assertEquals(null, idAuthService.getAuthHistoryDetails("1234", "1", "1"));
    }

    @Test(expected = ApisResourceAccessException.class)
    public void testGetAuthHistoryDetailsFetchFailure() throws ApisResourceAccessException {
        when(restClient.getApi(any(), any(), anyString(), any(), any(Class.class))).thenThrow(new ApisResourceAccessException());
        idAuthService.getAuthHistoryDetails("1234", "1", "10");
    }

    @Test
    public void testAuthTypeStatusUpdateUnlockSuccess()
            throws ApisResourceAccessException, ResidentServiceCheckedException {
        AuthTypeStatusResponseDto authTypeStatusResponseDto = new AuthTypeStatusResponseDto();
        
        ErrorDTO error = new ErrorDTO();
		error.setErrorCode("101");
		error.setErrorMessage("errors");

		List<ErrorDTO> errorList = new ArrayList<ErrorDTO>();
		errorList.add(error);
		authTypeStatusResponseDto.setErrors(errorList);
        when(restClient.postApi(any(), any(), any(), any())).thenReturn(authTypeStatusResponseDto);
        List<String> authTypes = new ArrayList<>();
        authTypes.add("bio-FIR");
        Map<String, AuthTypeStatus> authTypeStatusMap=authTypes.stream().distinct().collect(Collectors.toMap(Function.identity(), str -> AuthTypeStatus.LOCK));
        Map<String, Long> unlockForSecondsMap=authTypes.stream().distinct().collect(Collectors.toMap(Function.identity(), str -> 10L));
        idAuthService.authTypeStatusUpdate("1234567891", authTypeStatusMap, unlockForSecondsMap);
    }

    @Test
    public void testAuthTypeStatusUpdateUnlockSuccessWithUnlockForSeconds()
            throws ApisResourceAccessException, ResidentServiceCheckedException {
        AuthTypeStatusResponseDto authTypeStatusResponseDto = new AuthTypeStatusResponseDto();
        when(restClient.postApi(any(), any(), any(), any())).thenReturn(authTypeStatusResponseDto);
        List<String> authTypes = new ArrayList<>();
        authTypes.add("bio-FIR");
        boolean isUpdated = idAuthService.authTypeStatusUpdate("1234567891", authTypes, AuthTypeStatus.UNLOCK,
                2L);
        assertTrue(isUpdated);
    }
}
