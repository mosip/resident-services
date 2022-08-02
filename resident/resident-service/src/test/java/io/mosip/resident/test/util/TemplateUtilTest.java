package io.mosip.resident.test.util;

import io.mosip.resident.util.TemplateUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

/**
 * This class is used to test the TemplateUtil class
 * @author Kamesh Shekhar Prasad
 */

@RunWith(PowerMockRunner.class)
public class TemplateUtilTest {

    @InjectMocks
    private TemplateUtil templateUtil = new TemplateUtil();

    private String eventId;

    @Before
    public void setUp() {
        eventId = "12345";
    }

    @Test
    public void getAckTemplateVariablesForAuthenticationRequest() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForAuthenticationRequest(eventId);
        assertEquals("AUTHENTICATION_REQUEST",ackTemplateVariables.get("eventType"));
    }

    @Test
    public void getAckTemplateVariablesForCredentialShare() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForCredentialShare( eventId);
        assertEquals("credential-share",ackTemplateVariables.get("purpose"));
    }

    @Test
    public void getAckTemplateVariablesForDownloadPersonalizedCard() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForDownloadPersonalizedCard( eventId);
        assertEquals("DOWNLOAD_PERSONALIZED_CARD",ackTemplateVariables.get("eventType"));
    }

    @Test
    public void getAckTemplateVariablesForOrderPhysicalCard() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForOrderPhysicalCard( eventId);
        assertEquals("ORDER_PHYSICAL_CARD",ackTemplateVariables.get("eventType"));
    }

    @Test
    public void getAckTemplateVariablesForGetMyId() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForGetMyId( eventId);
        assertEquals("GET_MY_ID",ackTemplateVariables.get("eventType"));
    }

    @Test
    public void getAckTemplateVariablesForBookAnAppointment() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForBookAnAppointment( eventId);
        assertEquals(Collections.emptyMap(),ackTemplateVariables);
    }

    @Test
    public void getAckTemplateVariablesForUpdateMyUin() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForUpdateMyUin( eventId);
        assertEquals("UPDATE_MY_UIN",ackTemplateVariables.get("eventType"));
    }

    @Test
    public void getAckTemplateVariablesForGenerateVid() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForGenerateVid( eventId);
        assertEquals("GENERATE_VID",ackTemplateVariables.get("eventType"));
    }

    @Test
    public void getAckTemplateVariablesForRevokeVid() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForRevokeVid( eventId);
        assertEquals("REVOKE_VID",ackTemplateVariables.get("eventType"));
    }

    @Test
    public void getAckTemplateVariablesForVerifyPhoneOrEmail() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForVerifyPhoneEmail( eventId);
        assertEquals("VERIFY_PHONE_EMAIL",ackTemplateVariables.get("eventType"));
    }

    @Test
    public void getAckTemplateVariablesForAuthLock() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForAuthTypeLockUnlock( eventId);
        assertEquals("AUTH_TYPE_LOCK_UNLOCK",ackTemplateVariables.get("eventType"));
    }
}
