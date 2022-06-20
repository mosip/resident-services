package io.mosip.resident.test.service;

import io.mosip.kernel.core.websub.model.Event;
import io.mosip.kernel.core.websub.model.EventModel;
import io.mosip.kernel.core.websub.spi.PublisherClient;
import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;
import io.mosip.kernel.websub.api.model.UnsubscriptionRequest;
import io.mosip.resident.dto.ResidentTransactionType;
import io.mosip.resident.entity.AutnTxn;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.AutnTxnRepository;
import io.mosip.resident.service.impl.AuthTransactionCallBackServiceImpl;
import io.mosip.resident.service.impl.IdentityServiceImpl;
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

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.mock;

@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class AuthTransactionCallbackServiceTest {

    @Mock
    private ResidentServiceRestClient residentServiceRestClient;

    @Mock
    Environment env;

    @Mock
    private AuditUtil audit;

    @InjectMocks
    private AuthTransactionCallBackServiceImpl authTransactionCallBackService;

    @Mock
    private IdentityServiceImpl identityServiceImpl;

    @Mock
    private AutnTxnRepository autnTxnRepository;

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private PublisherClient<String, Object, HttpHeaders> publisher;

    @Mock
    SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse> subscribe;

    EventModel eventModel;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(authTransactionCallBackService).build();
        eventModel=new EventModel();
        Event event=new Event();
        event.setTransactionId("1234");
        event.setId("io.mosip.idauthentication");
        Map<String, Object> partnerIdMap = new java.util.HashMap<>();
        partnerIdMap.put("olv_partner_id", "mpartner-default-auth");
        event.setData(partnerIdMap);

        eventModel.setEvent(event);
        eventModel.setTopic("AUTH_TYPE_STATUS_UPDATE_ACK");
        eventModel.setPublishedOn(String.valueOf(LocalDateTime.now()));
        eventModel.setPublisher("AUTH_TYPE_STATUS_UPDATE_ACK");
    }

    @Test
    public void testWebSubUpdateAuthTypeService() throws ResidentServiceCheckedException, ApisResourceAccessException, NoSuchAlgorithmException {
        authTransactionCallBackService.updateAuthTransactionCallBackService(eventModel);
        authTransactionCallBackService = mock(AuthTransactionCallBackServiceImpl.class);
        Mockito.lenient().doNothing().when(authTransactionCallBackService).updateAuthTransactionCallBackService(Mockito.any());
    }

    @Test
    public void testWebSubUpdateAuthTypeServiceUpdate() throws ResidentServiceCheckedException, ApisResourceAccessException, NoSuchAlgorithmException {
        AutnTxn autnTxn = new AutnTxn();
        autnTxn.setResponseDTimes(LocalDateTime.now());
        autnTxn.setStatusCode("NEW");
        autnTxn.setStatusComment("NEW");
        autnTxn.setUpdBy("RESIDENT");
        autnTxn.setUpdDTimes(LocalDateTime.now());
        autnTxn.setOlvPartnerId("mpartner-default-auth");
        autnTxn.setToken("1234");
        autnTxn.setAuthTypeCode(ResidentTransactionType.SERVICE_REQUEST.toString());

        Mockito.when(autnTxnRepository.findById(Mockito.anyString())).thenReturn(autnTxn);
        authTransactionCallBackService.updateAuthTransactionCallBackService(eventModel);
        authTransactionCallBackService = mock(AuthTransactionCallBackServiceImpl.class);
        Mockito.lenient().doNothing().when(authTransactionCallBackService).updateAuthTransactionCallBackService(Mockito.any());
    }

    @Test
    public void testWebSubUpdateAuthTypeServiceException() throws ResidentServiceCheckedException, ApisResourceAccessException, NoSuchAlgorithmException {
        authTransactionCallBackService.updateAuthTransactionCallBackService(eventModel);
        authTransactionCallBackService = mock(AuthTransactionCallBackServiceImpl.class);
        Mockito.lenient().doThrow(ResidentServiceCheckedException.class).when(authTransactionCallBackService).updateAuthTransactionCallBackService(Mockito.any());
    }

}