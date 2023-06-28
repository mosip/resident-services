package io.mosip.resident.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.websub.model.Event;
import io.mosip.kernel.core.websub.model.EventModel;
import io.mosip.kernel.core.websub.spi.PublisherClient;
import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;
import io.mosip.kernel.websub.api.model.UnsubscriptionRequest;
import io.mosip.resident.dto.NotificationRequestDtoV2;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utility;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
    private ResidentTransactionRepository residentTransactionRepository;

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private PublisherClient<String, Object, HttpHeaders> publisher;

    @Mock
    SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse> subscribe;

    @Mock
    private AuditUtil auditUtil;

    @Mock
    private NotificationService notificationService;

    @Mock
    private Utility utility;
    
    private ObjectMapper objectMapper = new ObjectMapper();

    private NotificationResponseDTO notificationResponseDTO;

    private EventModel eventModel;
    private Event event;
    private String partnerId;

    @Before
    public void setup() throws ApisResourceAccessException, ResidentServiceCheckedException {
        eventModel=new EventModel();
        event=new Event();
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(webSubUpdateAuthTypeService).build();
        notificationResponseDTO = new NotificationResponseDTO();
		notificationResponseDTO.setStatus("Notification success");
        partnerId = "mpartner-default-auth";
        ReflectionTestUtils.setField(webSubUpdateAuthTypeService, "onlineVerificationPartnerId", partnerId);
    }

    @Test
    public void testWebSubUpdateAuthTypeService() throws ResidentServiceCheckedException, ApisResourceAccessException {
        event.setTransactionId("1234");
        event.setId("1234");

        eventModel.setEvent(event);
        eventModel.setTopic("AUTH_TYPE_STATUS_UPDATE_ACK");
        eventModel.setPublishedOn(String.valueOf(LocalDateTime.now()));
        eventModel.setPublisher("AUTH_TYPE_STATUS_UPDATE_ACK");

        webSubUpdateAuthTypeService.updateAuthTypeStatus(objectMapper.convertValue(eventModel, Map.class));
        webSubUpdateAuthTypeService = mock(WebSubUpdateAuthTypeServiceImpl.class);
        Mockito.lenient().doNothing().when(webSubUpdateAuthTypeService).updateAuthTypeStatus(any());
    }

    @Test
    public void testWebSubUpdateAuthPassed() throws ResidentServiceCheckedException, ApisResourceAccessException {
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> authTypeList = new ArrayList<>();
        Map<String, Object> authTypeMap = new HashMap<>();
        authTypeMap.put("bio-FIR", "Locked");
        authTypeList.add(authTypeMap);
        data.put("authTypes", authTypeList);
        data.put("requestId", "0839c2bf-5be5-4359-b860-6f9bda908378");
        event.setData(data);
        eventModel.setEvent(event);
        webSubUpdateAuthTypeService.updateAuthTypeStatus(objectMapper.convertValue(eventModel, Map.class));
    }


    @Test
    public void testUpdateAuthTypeStatus_Success() throws Exception {
        // Mock data
        Map<String, Object> eventModel = new HashMap<>();
        eventModel.put("event", getMockEventMap());

        // Mock repository response
        ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
        residentTransactionEntity.setOlvPartnerId(partnerId);
        residentTransactionEntity.setEventId("12454578458478547");
        residentTransactionEntity.setIndividualId("4515452565");
        when(residentTransactionRepository.findByRequestTrnId("12345")).thenReturn(List.of(residentTransactionEntity));

        // Mock utility response
        when(utility.getSessionUserName()).thenReturn("testUser");

        // Invoke the method
        webSubUpdateAuthTypeService.updateAuthTypeStatus(eventModel);

        // Verify that the expected methods were called
        verify(auditUtil, times(1)).setAuditRequestDto(EventEnum.UPDATE_AUTH_TYPE_STATUS);
        verify(residentTransactionRepository, times(1)).findByRequestTrnId("12345");
        verify(notificationService, times(1)).sendNotification(any(NotificationRequestDtoV2.class));
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testUpdateAuthTypeStatus_Failure() throws Exception {
        // Mock data
        Map<String, Object> eventModel = new HashMap<>();
        eventModel.put("event", getMockEventMap());

        // Mock repository response
        when(residentTransactionRepository.findByRequestTrnId("12345")).thenThrow(new RuntimeException());

        // Invoke the method
        webSubUpdateAuthTypeService.updateAuthTypeStatus(eventModel);
    }

    private Map<String, Object> getMockEventMap() {
        Map<String, Object> eventMap = new HashMap<>();
        Map<String, Object> dataMap = new HashMap<>();
        List<Map<String, Object>> authTypesList = new ArrayList<>();
        Map<String, Object> authTypeStatus = new HashMap<>();
        authTypeStatus.put("requestId", "12345");
        authTypesList.add(authTypeStatus);
        dataMap.put("authTypes", authTypesList);
        eventMap.put("data", dataMap);
        return eventMap;
    }
}