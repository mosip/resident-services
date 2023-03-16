package io.mosip.resident.test.controller;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
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
import org.springframework.web.client.RestTemplate;

import io.mosip.resident.constant.RequestType;
import io.mosip.resident.controller.AcknowledgementController;
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
        Mockito.when(templateUtil.getFeatureName(Mockito.anyString())).thenReturn(RequestType.AUTHENTICATION_REQUEST.toString());
        Mockito.when(acknowledgementService.getAcknowledgementPDF(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn("test".getBytes());
        ResponseEntity<Object> response = acknowledgementController.getAcknowledgement("bf42d76e-b02e-48c8-a17a-6bb842d85ea9", "eng", 0);
        assertEquals(response.getStatusCode(), responseEntity.getStatusCode());
    }

}
