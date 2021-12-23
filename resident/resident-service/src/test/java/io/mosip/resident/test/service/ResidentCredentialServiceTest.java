package io.mosip.resident.test.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.dto.*;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ResidentCredentialService;
import io.mosip.resident.service.impl.ResidentCredentialServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TokenGenerator;
import io.mosip.resident.util.Utilitiy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
    private TokenGenerator tokenGenerator;

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
    private ResidentCredentialService residentCredentialService = new ResidentCredentialServiceImpl();

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
    public void generateCredentialTest() throws OtpValidationFailedException, IOException, ApisResourceAccessException, ResidentServiceCheckedException {
        ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();
        residentCredentialResponseDto.setRequestId("10001100010006920211220064226");
        ResponseWrapper<ResidentCredentialResponseDto> response = new ResponseWrapper<>();
        response.setResponsetime(DateUtils.getCurrentDateTimeString());
        response.setResponse(residentCredentialResponseDto);
        String valueAsString = objectMapper.writeValueAsString(residentCredentialResponseDto);

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
        when(residentServiceRestClient.getApi(partnerUri, ResponseWrapper.class, tokenGenerator.getToken())).thenReturn(partnerResponseDtoResponseWrapper);
        when(residentServiceRestClient.postApi(any(), any(), any(), any(), any())).thenReturn(response);

        ResidentCredentialResponseDto credentialResponseDto = residentCredentialService.reqCredential(residentCredentialRequestDto);
        assertEquals("10001100010006920211220064226", credentialResponseDto.getRequestId());
    }

}
