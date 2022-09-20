package io.mosip.resident.test.service;

import io.mosip.kernel.core.websub.model.Event;
import io.mosip.kernel.core.websub.model.EventModel;
import io.mosip.kernel.core.websub.spi.PublisherClient;
import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;
import io.mosip.kernel.websub.api.model.UnsubscriptionRequest;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.WebSubUpdateAuthTypeServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

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
    
    @Mock
	private NotificationService notificationService;

    private NotificationResponseDTO notificationResponseDTO;

    @Before
    public void setup() throws ApisResourceAccessException, ResidentServiceCheckedException {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(webSubUpdateAuthTypeService).build();
        Mockito.when(identityServiceImpl.getResidentIndvidualId()).thenReturn("8251649601");
        notificationResponseDTO = new NotificationResponseDTO();
		notificationResponseDTO.setStatus("Notification success");
		when(notificationService.sendNotification(Mockito.any())).thenReturn(notificationResponseDTO);
    }

    @Test
    public void testWebSubUpdateAuthTypeService() throws ResidentServiceCheckedException, ApisResourceAccessException {
        EventModel eventModel=new EventModel();
        Event event=new Event();
        event.setTransactionId("1234");
        event.setId("1234");

        eventModel.setEvent(event);
        eventModel.setTopic("AUTH_TYPE_STATUS_UPDATE_ACK");
        eventModel.setPublishedOn(String.valueOf(LocalDateTime.now()));
        eventModel.setPublisher("AUTH_TYPE_STATUS_UPDATE_ACK");

        webSubUpdateAuthTypeService.updateAuthTypeStatus(eventModel);
        webSubUpdateAuthTypeService = mock(WebSubUpdateAuthTypeServiceImpl.class);
        Mockito.lenient().doNothing().when(webSubUpdateAuthTypeService).updateAuthTypeStatus(Mockito.any());
    }
}