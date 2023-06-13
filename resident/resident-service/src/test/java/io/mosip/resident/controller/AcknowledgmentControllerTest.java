package io.mosip.resident.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.AcknowledgementService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ResidentVidServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.util.Utility;
import io.mosip.resident.validator.RequestValidator;

/**
 * Acknowledgment Controller Test
 * Note: This class is used to test the Acknowledgment Controller
 * @author Kamesh Shekhar Prasad
 */

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration
public class AcknowledgmentControllerTest {
    private static final String LOCALE_EN_US = "en-US";

	@InjectMocks
    private AcknowledgementController acknowledgementController;

    @Mock
    private AuditUtil auditUtil;

    @Mock
    private RequestValidator requestValidator;

    @Mock
    private AcknowledgementService acknowledgementService;

    @Mock
    private IdentityServiceImpl identityService;

    @Mock
    private ObjectStoreHelper objectStore;

    @Mock
    private ResidentVidServiceImpl residentVidService;

    @Mock
    private TemplateUtil templateUtil;

    @MockBean
    @Qualifier("selfTokenRestTemplate")
    private RestTemplate residentRestTemplate;

    @Mock
    private IdAuthService idAuthService;

    @Mock
    private Utility utility;

    private ResponseEntity<Object> responseEntity;

    @Before
    public void setup() throws Exception {
        String eventId = "bf42d76e-b02e-48c8-a17a-6bb842d85ea9";
        byte[] pdfBytes = "test".getBytes(StandardCharsets.UTF_8);
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(pdfBytes));
        responseEntity = ResponseEntity.ok().contentType(MediaType.parseMediaType("application/pdf"))
                .header("Content-Disposition", "attachment; filename=\"" +
                        eventId + ".pdf\"")
                .body(resource);
    }

    @Test
    public void testCreateRequestGenerationSuccess() throws Exception {
        when(templateUtil.getFeatureName(anyString(), anyString())).thenReturn(RequestType.AUTHENTICATION_REQUEST.toString());
        when(acknowledgementService.getAcknowledgementPDF(anyString(), anyString(), Mockito.anyInt(), anyString())).thenReturn("test".getBytes());
        ResponseEntity<Object> response = acknowledgementController.getAcknowledgement("bf42d76e-b02e-48c8-a17a-6bb842d85ea9", "eng", 0, LOCALE_EN_US);
        assertEquals(response.getStatusCode(), responseEntity.getStatusCode());
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetAcknowledgementFailure() throws ResidentServiceCheckedException, IOException {
        ReflectionTestUtils.setField(acknowledgementController, "ackDownloadId", "ack.id");
        when(acknowledgementService.getAcknowledgementPDF(anyString(), anyString(), Mockito.anyInt(), anyString()))
                .thenThrow(new ResidentServiceCheckedException(ResidentErrorCode.EVENT_STATUS_NOT_FOUND));
        ResponseEntity<Object> response = acknowledgementController.getAcknowledgement("bf42d76e-b02e-48c8-a17a-6bb842d85ea9", "eng", 0, LOCALE_EN_US);
        assertEquals(response.getStatusCode(), responseEntity.getStatusCode());
    }

    @Test(expected = ResidentServiceException.class)
    public void testGetAcknowledgementFailureResidentServiceException() throws ResidentServiceCheckedException, IOException {
        doThrow(new ResidentServiceException("error", "Error message")).
                when(requestValidator).validateEventIdLanguageCode(any(), any());
        ReflectionTestUtils.setField(acknowledgementController, "ackDownloadId", "ack.id");
        ResponseEntity<Object> response = acknowledgementController.getAcknowledgement("bf42d76e-b02e-48c8-a17a-6bb842d85ea9", "eng", 0, LOCALE_EN_US);
        assertEquals(response.getStatusCode(), responseEntity.getStatusCode());
    }

    @Test(expected = ResidentServiceException.class)
    public void testGetAcknowledgementFailureInvalidInputException() throws ResidentServiceCheckedException, IOException {
        doThrow(new InvalidInputException()).
                when(requestValidator).validateEventIdLanguageCode(any(), any());
        ReflectionTestUtils.setField(acknowledgementController, "ackDownloadId", "ack.id");
        ResponseEntity<Object> response = acknowledgementController.getAcknowledgement("bf42d76e-b02e-48c8-a17a-6bb842d85ea9", "eng", 0, LOCALE_EN_US);
        assertEquals(response.getStatusCode(), responseEntity.getStatusCode());
    }

}
