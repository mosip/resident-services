package io.mosip.resident.test.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import io.mosip.kernel.core.idvalidator.spi.RidValidator;
import io.mosip.resident.dto.RegistrationStatusDTO;
import io.mosip.resident.dto.RegistrationStatusResponseDTO;
import io.mosip.resident.dto.RequestDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;

@RunWith(MockitoJUnitRunner.class)
public class ResidentServiceGetCredentialRequestTest {

    private static final String DATETIME_PATTERN = "mosip.utc-datetime-pattern";
    private static final String STATUS_CHECK_ID = "mosip.resident.service.status.check.id";
    private static final String STATUS_CHECEK_VERSION = "mosip.resident.service.status.check.version";
    private static final String REGISTRATIONSTATUSSEARCH = "REGISTRATIONSTATUSSEARCH";
    @Mock
    ResidentServiceRestClient residentServiceRestClient;

    @Mock
    Environment env;

    @Mock
    NotificationService notificationService;

    @Mock
    IdentityServiceImpl identityServiceImpl;

    @Mock
    private RidValidator<String> ridValidator;

    @Mock
    private AuditUtil audit;

    @InjectMocks
    ResidentServiceImpl residentService = new ResidentServiceImpl();

    private RequestDTO requestDTO;

    private RegistrationStatusResponseDTO responseWrapper;
    private RegistrationStatusDTO response;

    @Before
    public void setup() throws IOException, ApisResourceAccessException {
        requestDTO = new RequestDTO();
        requestDTO.setIndividualId("10006100435989220191202104224");
        requestDTO.setIndividualIdType("RID");

        Mockito.when(env.getProperty(STATUS_CHECK_ID)).thenReturn("id");
        Mockito.when(env.getProperty(STATUS_CHECEK_VERSION)).thenReturn("version");
        Mockito.when(env.getProperty(DATETIME_PATTERN)).thenReturn("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Mockito.when(env.getProperty(REGISTRATIONSTATUSSEARCH)).thenReturn(REGISTRATIONSTATUSSEARCH);


        responseWrapper = new RegistrationStatusResponseDTO();
        response = new RegistrationStatusDTO();
        response.setRegistrationId("10008100670000320191212101846");
        response.setStatusCode("PROCESSED");
        responseWrapper.setErrors(null);
        responseWrapper.setId("mosip.resident.status");
        List<RegistrationStatusDTO> list = new ArrayList<>();
        list.add(response);
        responseWrapper.setResponse(list);

        Mockito.when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(responseWrapper);
        Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());
    }

    @Test
    public void getCredentialRequestTest() throws ResidentServiceException, ResidentServiceCheckedException {
        Mockito.when(identityServiceImpl.getUinForIndividualId(any())).thenReturn("10006100435989220191202104224");
        String result = residentService.checkAidStatus("10006100435989220191202104224");
        assertEquals("PROCESSED", result);
    }

    @Test
    public void getAidStatusTest() throws ResidentServiceException, ResidentServiceCheckedException, ApisResourceAccessException {
        Mockito.when(identityServiceImpl.getUinForIndividualId(any())).thenReturn("10006100435989220191202104224");
        Mockito.when(identityServiceImpl.getIndividualIdForAid(any())).thenThrow(new ApisResourceAccessException());
        String result = residentService.checkAidStatus("10006100435989220191202104224");
        assertEquals("PROCESSED", result);
    }

}
