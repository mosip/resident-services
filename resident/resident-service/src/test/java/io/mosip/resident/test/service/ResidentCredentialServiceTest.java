package io.mosip.resident.test.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.dto.*;
import io.mosip.resident.exception.*;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ResidentCredentialService;
import io.mosip.resident.service.impl.ResidentCredentialServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utilitiy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class ResidentCredentialServiceTest {

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

    private ResidentCredentialRequestDto residentCredentialRequestDto;

    @InjectMocks
    @Spy
    private ResidentCredentialServiceImpl residentCredentialService = new ResidentCredentialServiceImpl();

    @Before
    public void setup() throws IOException, ResidentServiceCheckedException {
        residentCredentialRequestDto = new ResidentCredentialRequestDto();
        residentCredentialRequestDto.setOtp("123");
        residentCredentialRequestDto.setTransactionID("12345");
        residentCredentialRequestDto.setIndividualId("1234567890");
        residentCredentialRequestDto.setIssuer("mpartner-default-print");
        residentCredentialRequestDto.setCredentialType("euin");
        residentCredentialRequestDto.setEncrypt(true);
        residentCredentialRequestDto.setEncryptionKey("abc123");
    }

    @Test
    public void generateCredentialTest() throws OtpValidationFailedException, ApisResourceAccessException, ResidentServiceCheckedException {
        ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();
        residentCredentialResponseDto.setRequestId("10001100010006920211220064226");
        ResponseWrapper<ResidentCredentialResponseDto> response = new ResponseWrapper<>();
        response.setResponsetime(DateUtils.getCurrentDateTimeString());
        response.setResponse(residentCredentialResponseDto);

        PartnerResponseDto partnerResponseDto = new PartnerResponseDto();
        partnerResponseDto.setOrganizationName("MOSIP");
        ResponseWrapper<PartnerResponseDto> partnerResponseDtoResponseWrapper = new ResponseWrapper<>();
        partnerResponseDtoResponseWrapper.setResponse(partnerResponseDto);

        when(idAuthService.validateOtp(residentCredentialRequestDto.getTransactionID(), residentCredentialRequestDto.getIndividualId(), residentCredentialRequestDto.getOtp())).thenReturn(Boolean.TRUE);

        RequestWrapper<CredentialReqestDto> requestDto = new RequestWrapper<>();
        requestDto.setId("mosip.credential.request.service.id");
        requestDto.setRequest(new CredentialReqestDto());
        requestDto.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
        requestDto.setVersion("1.0");

        String partnerUrl = env.getProperty(ApiName.PARTNER_API_URL.name()) + "/" + residentCredentialRequestDto.getIssuer();
        URI partnerUri = URI.create(partnerUrl);
        when(residentServiceRestClient.getApi(partnerUri, ResponseWrapper.class)).thenReturn(partnerResponseDtoResponseWrapper);
        when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(response);

        ResidentCredentialResponseDto credentialResponseDto = residentCredentialService.reqCredential(residentCredentialRequestDto);
        assertEquals("10001100010006920211220064226", credentialResponseDto.getRequestId());
    }

    @Test(expected = ResidentServiceException.class)
    public void testGenerateCredentialWithOTPFailure() throws OtpValidationFailedException, ResidentServiceCheckedException {
        when(idAuthService.validateOtp(residentCredentialRequestDto.getTransactionID(), residentCredentialRequestDto.getIndividualId(), residentCredentialRequestDto.getOtp())).thenReturn(Boolean.FALSE);

        residentCredentialService.reqCredential(residentCredentialRequestDto);
    }

    @Test(expected = ResidentCredentialServiceException.class)
    public void testGenerateCredentialWithCredentialServiceException() throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException {
        ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();
        residentCredentialResponseDto.setRequestId("10001100010006920211220064226");
        ResponseWrapper<ResidentCredentialResponseDto> response = new ResponseWrapper<>();
        response.setResponsetime(DateUtils.getCurrentDateTimeString());
        response.setResponse(residentCredentialResponseDto);

        PartnerResponseDto partnerResponseDto = new PartnerResponseDto();
        partnerResponseDto.setOrganizationName("MOSIP");
        ResponseWrapper<PartnerResponseDto> partnerResponseDtoResponseWrapper = new ResponseWrapper<>();
        partnerResponseDtoResponseWrapper.setResponse(partnerResponseDto);

        when(idAuthService.validateOtp(residentCredentialRequestDto.getTransactionID(), residentCredentialRequestDto.getIndividualId(), residentCredentialRequestDto.getOtp())).thenReturn(Boolean.TRUE);

        RequestWrapper<CredentialReqestDto> requestDto = new RequestWrapper<>();
        requestDto.setId("mosip.credential.request.service.id");
        requestDto.setRequest(new CredentialReqestDto());
        requestDto.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
        requestDto.setVersion("1.0");

        String partnerUrl = env.getProperty(ApiName.PARTNER_API_URL.name()) + "/" + residentCredentialRequestDto.getIssuer();
        URI partnerUri = URI.create(partnerUrl);
        when(residentServiceRestClient.getApi(partnerUri, ResponseWrapper.class)).thenReturn(partnerResponseDtoResponseWrapper);
        when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenThrow(ApisResourceAccessException.class);

        residentCredentialService.reqCredential(residentCredentialRequestDto);
    }

    @Test(expected = ResidentCredentialServiceException.class)
    public void testGenerateCredentialWithIOException() throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException {
        ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();
        residentCredentialResponseDto.setRequestId("10001100010006920211220064226");
        ResponseWrapper<ResidentCredentialResponseDto> response = new ResponseWrapper<>();
        response.setResponsetime(DateUtils.getCurrentDateTimeString());
        response.setResponse(residentCredentialResponseDto);

        PartnerResponseDto partnerResponseDto = new PartnerResponseDto();
        partnerResponseDto.setOrganizationName("MOSIP");
        ResponseWrapper<PartnerResponseDto> partnerResponseDtoResponseWrapper = new ResponseWrapper<>();
        partnerResponseDtoResponseWrapper.setResponse(partnerResponseDto);

        when(idAuthService.validateOtp(residentCredentialRequestDto.getTransactionID(), residentCredentialRequestDto.getIndividualId(), residentCredentialRequestDto.getOtp())).thenReturn(Boolean.TRUE);

        RequestWrapper<CredentialReqestDto> requestDto = new RequestWrapper<>();
        requestDto.setId("mosip.credential.request.service.id");
        requestDto.setRequest(new CredentialReqestDto());
        requestDto.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
        requestDto.setVersion("1.0");

        String partnerUrl = env.getProperty(ApiName.PARTNER_API_URL.name()) + "/" + residentCredentialRequestDto.getIssuer();
        URI partnerUri = URI.create(partnerUrl);
        when(residentServiceRestClient.getApi(partnerUri, ResponseWrapper.class)).thenReturn(partnerResponseDtoResponseWrapper);
        when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(partnerResponseDtoResponseWrapper);

        residentCredentialService.reqCredential(residentCredentialRequestDto);
    }

    @Test
    public void testGetCredentialTypes() throws ApisResourceAccessException {
        Issuer issuer = new Issuer();
        issuer.setCode("paytm");
        issuer.setName("PayTM");
        List<Issuer> issuerList = new ArrayList<>();
        issuerList.add(issuer);
        Type type = new Type();
        type.setDescription("Secure Digital QR Code");
        type.setId("idtype1");
        type.setName("Secure Digital QR Code");
        type.setIssuers(issuerList);
        List<Type> types = new ArrayList<>();
        types.add(type);
        CredentialTypeResponse credentialTypeResponse = new CredentialTypeResponse();
        credentialTypeResponse.setCredentialTypes(types);

        when(env.getProperty(ApiName.CREDENTIAL_TYPES_URL.name())).thenReturn("https://mosip.net/v1/credentialservice/types");
        when(residentServiceRestClient.getApi(any(), any())).thenReturn(credentialTypeResponse);
        CredentialTypeResponse credentialTypes = residentCredentialService.getCredentialTypes();
        assertEquals(credentialTypes.getCredentialTypes().size(), 1);
        assertEquals(credentialTypes.getCredentialTypes().get(0).getDescription(), "Secure Digital QR Code");
        assertEquals(credentialTypes.getCredentialTypes().get(0).getIssuers().size(), 1);
        assertEquals(credentialTypes.getCredentialTypes().get(0).getIssuers().get(0).getName(), "PayTM");
    }

    @Test(expected = ResidentCredentialServiceException.class)
    public void testGetCredentialTypesWithAPIResourceException() throws ApisResourceAccessException {
        when(env.getProperty(ApiName.CREDENTIAL_TYPES_URL.name())).thenReturn("https://mosip.net/v1/credentialservice/types");
        when(residentServiceRestClient.getApi(any(), any())).thenThrow(ApisResourceAccessException.class);
        residentCredentialService.getCredentialTypes();
    }

    @Test
    public void testGeneratePin() {
        ReflectionTestUtils.setField(residentCredentialService, "random", null);
        ReflectionTestUtils.setField(residentCredentialService, "min", 100000);
        ReflectionTestUtils.setField(residentCredentialService, "max", 999999);
        residentCredentialService.generatePin();
        Mockito.verify(residentCredentialService, times(1)).instantiate();
    }
}
