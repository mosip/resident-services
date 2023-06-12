package io.mosip.resident.test.controller;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.websub.model.Event;
import io.mosip.kernel.core.websub.model.EventModel;
import io.mosip.resident.controller.VerificationController;
import io.mosip.resident.controller.WebSubCredentialStatusUpdateController;
import io.mosip.resident.controller.WebSubUpdateAuthTypeController;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.DocumentService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.service.VerificationService;
import io.mosip.resident.service.WebSubCredentialStatusUpdateService;
import io.mosip.resident.service.WebSubUpdateAuthTypeService;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.service.impl.VerificationServiceImpl;
import io.mosip.resident.test.ResidentTestBootApplication;
import io.mosip.resident.util.AuditUtil;

/**
 * Web-Sub Credential Status Update Controller Test
 * Note: This class is used to test the Web-Sub Credential Status Update Controller
 * @author Ritik Jain
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResidentTestBootApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
public class WebSubCredentialStatusUpdateControllerTest {
	
    @MockBean
    private ProxyIdRepoService proxyIdRepoService;

    @MockBean
    @Qualifier("selfTokenRestTemplate")
    private RestTemplate residentRestTemplate;

    @Mock
    private AuditUtil audit;

    @InjectMocks
    WebSubCredentialStatusUpdateController webSubCredentialStatusUpdateController;

    @MockBean
    WebSubCredentialStatusUpdateService webSubCredentialStatusUpdateService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VerificationService verificationService;

    @MockBean
    private IdAuthService idAuthService;

    @MockBean
    private ResidentVidService vidService;

    @MockBean
    private DocumentService docService;

    @MockBean
    private ObjectStoreHelper objectStore;

    @MockBean
    private VerificationServiceImpl verificationServiceImpl;

    @InjectMocks
    VerificationController verificationController;
    
    @MockBean
    private ResidentServiceImpl residentService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(webSubCredentialStatusUpdateController).build();
    }

    @Test
    public void testCredentialStatusUpdateCallback() throws Exception {

        EventModel eventModel=new EventModel();
        Event event=new Event();
        event.setTransactionId("1234");
        event.setId("8251649601");
        Map<String, Object> partnerIdMap = new java.util.HashMap<>();
        partnerIdMap.put("olv_partner_id", "mpartner-default-auth");
        event.setData(partnerIdMap);

        eventModel.setEvent(event);
        eventModel.setTopic("CREDENTIAL_STATUS_UPDATE_CALL_BACK");
        eventModel.setPublishedOn(String.valueOf(LocalDateTime.now()));
        eventModel.setPublisher("CREDENTIAL_STATUS_UPDATE_CALL_BACK");
        webSubCredentialStatusUpdateController.credentialStatusUpdateCallback(objectMapper.convertValue(eventModel, Map.class));

        mockMvc.perform((MockMvcRequestBuilders.post("/callback/credentialStatusUpdate"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(eventModel.toString()))
                .andReturn();
    }

    @Test(expected = ResidentServiceException.class)
    public void testCredentialStatusUpdateCallbackWithException() throws Exception {
    	EventModel eventModel=new EventModel();
        doThrow(new ResidentServiceCheckedException()).when(webSubCredentialStatusUpdateService).updateCredentialStatus(anyMap());
        webSubCredentialStatusUpdateController.credentialStatusUpdateCallback(objectMapper.convertValue(eventModel, Map.class));
    }
}
