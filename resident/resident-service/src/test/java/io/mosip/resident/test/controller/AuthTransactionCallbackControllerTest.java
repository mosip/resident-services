package io.mosip.resident.test.controller;

import io.mosip.kernel.core.websub.model.Event;
import io.mosip.kernel.core.websub.model.EventModel;
import io.mosip.resident.controller.AuthTransactionCallbackController;
import io.mosip.resident.controller.VerificationController;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.*;
import io.mosip.resident.service.impl.VerificationServiceImpl;
import io.mosip.resident.test.ResidentTestBootApplication;
import io.mosip.resident.util.AuditUtil;
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

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Web-Sub Update Controller Test
 * Note: This class is used to test the Auth transaction callback controller
 * @author Kamesh Shekhar Prasad
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResidentTestBootApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
public class AuthTransactionCallbackControllerTest {

    @MockBean
    @Qualifier("selfTokenRestTemplate")
    private RestTemplate residentRestTemplate;

    @Mock
    private AuditUtil audit;

    @InjectMocks
    AuthTransactionCallbackController authTransactionCallbackController;

    @MockBean
    private AuthTransactionCallBackService authTransactionCallBackService;

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

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(authTransactionCallbackController).build();
    }

    @Test
    public void testCreateRequestGenerationSuccess() throws Exception {

        EventModel eventModel=new EventModel();
        Event event=new Event();
        event.setTransactionId("1234");
        Map<String, Object> partnerIdMap = new java.util.HashMap<>();
        partnerIdMap.put("olv_partner_id", "mpartner-default-auth");
        event.setData(partnerIdMap);

        eventModel.setEvent(event);
        eventModel.setTopic("AUTH_TYPE_STATUS_UPDATE_ACK");
        eventModel.setPublishedOn(String.valueOf(LocalDateTime.now()));
        eventModel.setPublisher("AUTH_TYPE_STATUS_UPDATE_ACK");
        authTransactionCallbackController.authTypeCallback(eventModel);

        mockMvc.perform((MockMvcRequestBuilders.post("/callback/authTransaction"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(eventModel.toString()))
                .andReturn();
    }

}
