package io.mosip.resident.test.controller;

import io.mosip.resident.controller.AcknowledgementController;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.AcknowledgementService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ResidentVidServiceImpl;
import io.mosip.resident.test.ResidentTestBootApplication;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.validator.RequestValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Acknowledgment Controller Test
 * Note: This class is used to test the Acknowledgment Controller
 * @author Kamesh Shekhar Prasad
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResidentTestBootApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
public class AcknowledgmentControllerTest {
	

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AcknowledgementController acknowledgementController;

    @MockBean
    private AuditUtil auditUtil;

    @MockBean
    private RequestValidator requestValidator;

    @MockBean
    private AcknowledgementService acknowledgementService;

    @MockBean
    private IdentityServiceImpl identityService;

    @MockBean
    private ObjectStoreHelper objectStore;

    @MockBean
    private ResidentVidServiceImpl residentVidService;

    @MockBean
    @Qualifier("selfTokenRestTemplate")
    private RestTemplate residentRestTemplate;

    @MockBean
    private IdAuthService idAuthService;

    private ResponseEntity<Object> responseEntity;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(acknowledgementController).build();
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
        Mockito.when(acknowledgementController.getAcknowledgement(Mockito.any(),Mockito.any())).thenReturn(responseEntity);
        mockMvc.perform(MockMvcRequestBuilders.get("/ack/download/pdf/event/bf42d76e-b02e-48c8-a17a-6bb842d85ea9/language/eng"))
                .andExpect(status().isOk());
    }

}
