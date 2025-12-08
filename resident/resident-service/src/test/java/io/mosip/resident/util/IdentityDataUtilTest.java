package io.mosip.resident.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.resident.dto.NotificationRequestDtoV2;
import io.mosip.resident.exception.IdRepoAppException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ProxyMasterdataService;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for IdentityDataUtil
 * @author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
public class IdentityDataUtilTest {

    private IdentityDataUtil identityDataUtil;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ProxyMasterdataService proxyMasterdataService;

    @Mock
    private AcrMappingUtil acrMappingUtil;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CachedIdentityDataUtil cachedIdentityDataUtil;

    @Mock
    private AccessTokenUtility accessTokenUtility;

    @Before
    public void setUp() {
        identityDataUtil = new IdentityDataUtil();

        ReflectionTestUtils.setField(identityDataUtil, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(identityDataUtil, "proxyMasterdataService", proxyMasterdataService);
        ReflectionTestUtils.setField(identityDataUtil, "acrMappingUtil", acrMappingUtil);
        ReflectionTestUtils.setField(identityDataUtil, "notificationService", notificationService);
        ReflectionTestUtils.setField(identityDataUtil, "cachedIdentityDataUtil", cachedIdentityDataUtil);
        ReflectionTestUtils.setField(identityDataUtil, "getAccessToken", accessTokenUtility);
    }

    // ---------------- sendNotification tests ----------------

    @Test
    public void sendNotificationSuccessInvokesNotificationService() throws Exception {
        // arrange
        String eventId = "E1";
        String individualId = "IND1";

        // call
        identityDataUtil.sendNotification(eventId, individualId, null);

        // verify that notificationService.sendNotification was invoked with a NotificationRequestDtoV2
        ArgumentCaptor<NotificationRequestDtoV2> captor = ArgumentCaptor.forClass(NotificationRequestDtoV2.class);
        verify(notificationService, times(1)).sendNotification(captor.capture(), isNull());
        NotificationRequestDtoV2 dto = captor.getValue();
        assertEquals(eventId, dto.getEventId());
        assertEquals(individualId, dto.getId());
    }

    @Test
    public void sendNotificationWhenNotificationThrowsIsHandled() throws Exception {
        // arrange
        doThrow(new ResidentServiceCheckedException("ERR", "msg"))
                .when(notificationService).sendNotification(any(NotificationRequestDtoV2.class), any());

        // call - should not throw
        identityDataUtil.sendNotification("E2", "IND2", null);

        // verify sendNotification invoked
        verify(notificationService, times(1)).sendNotification(any(NotificationRequestDtoV2.class), any());
        // no exception expected
    }

    @Test
    public void convertIdResponseIdentityObjectToJsonObjectSuccess() throws Exception {
        Map<String,Object> identityObj = Map.of("k","v");
        when(objectMapper.writeValueAsString(identityObj)).thenReturn("{\"k\":\"v\"}");

        JSONObject json = identityDataUtil.convertIdResponseIdentityObjectToJsonObject(identityObj);

        assertNotNull(json);
        assertEquals("v", json.get("k"));
    }

    @Test(expected = IdRepoAppException.class)
    public void convertIdResponseIdentityObjectToJsonObjectInvalidJsonThrowsIdRepoAppException() throws Exception {
        Object bad = new Object();
        // make objectMapper produce an invalid JSON string
        when(objectMapper.writeValueAsString(bad)).thenReturn("not-a-json");

        // method should catch parse exception and throw IdRepoAppException
        identityDataUtil.convertIdResponseIdentityObjectToJsonObject(bad);
    }

    @Test
    public void getAuthTypeCodeFromKeyDelegatesToAcrMappingUtil() throws Exception {
        when(acrMappingUtil.getAmrAcrMapping()).thenReturn(Map.of("REQ","AMR"));
        String value = identityDataUtil.getAuthTypeCodefromkey("REQ");
        assertEquals("AMR", value);
        verify(acrMappingUtil, times(1)).getAmrAcrMapping();
    }
}
