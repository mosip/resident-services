package io.mosip.resident.test.service;

import io.mosip.kernel.core.websub.model.Event;
import io.mosip.kernel.core.websub.model.EventModel;
import io.mosip.kernel.core.websub.spi.PublisherClient;
import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;
import io.mosip.kernel.websub.api.model.UnsubscriptionRequest;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.WebSubUpdateAuthTypeServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class WebSubUpdateAuthTypeServiceTest {

    @Mock
    private ResidentServiceRestClient residentServiceRestClient;

    @Mock
    Environment env;

    @Mock
    private AuditUtil audit;

    @InjectMocks
    private WebSubUpdateAuthTypeServiceImpl webSubUpdateAuthTypeService;

    @Mock
    private IdentityServiceImpl identityServiceImpl;

    @Mock
    private ResidentTransactionRepository residentTransactionRepository;

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private PublisherClient<String, Object, HttpHeaders> publisher;

    @Mock
    SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse> subscribe;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(webSubUpdateAuthTypeService).build();
    }

    @Test
    public void testWebSubUpdateAuthTypeService() throws ResidentServiceCheckedException {

        EventModel eventModel = new EventModel();
        EventModel e1=new EventModel();
        Event event=new Event();
        event.setTransactionId("1234");

        e1.setEvent(event);
        e1.setTopic("AUTH_TYPE_STATUS_UPDATE_ACK");
        e1.setPublishedOn(String.valueOf(LocalDateTime.now()));
        e1.setPublisher("AUTH_TYPE_STATUS_UPDATE_ACK");

        webSubUpdateAuthTypeService.updateAuthTypeStatus(eventModel);
    }
}