package io.mosip.resident.test.util;

import io.mosip.resident.constant.TemplateVariablesEnum;
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
        assertEquals("otp",ackTemplateVariables.get(TemplateVariablesEnum.AUTHENTICATION_MODE));
    }

    @Test
    public void getAckTemplateVariablesForCredentialShare() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForCredentialShare( eventId);
        assertEquals("otp",ackTemplateVariables.get(TemplateVariablesEnum.AUTHENTICATION_MODE));
    }

    @Test
    public void getAckTemplateVariablesForDownloadPersonalizedCard() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForDownloadPersonalizedCard( eventId);
        assertEquals("otp",ackTemplateVariables.get(TemplateVariablesEnum.AUTHENTICATION_MODE));
    }

    @Test
    public void getAckTemplateVariablesForOrderPhysicalCard() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForOrderPhysicalCard( eventId);
        assertEquals("otp",ackTemplateVariables.get(TemplateVariablesEnum.AUTHENTICATION_MODE));
    }

    @Test
    public void getAckTemplateVariablesForGetMyId() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForGetMyId( eventId);
        assertEquals(eventId,ackTemplateVariables.get(TemplateVariablesEnum.EVENT_ID));
    }

    @Test
    public void getAckTemplateVariablesForBookAnAppointment() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForBookAnAppointment( eventId);
        assertEquals(Collections.emptyMap(),ackTemplateVariables);
    }

    @Test
    public void getAckTemplateVariablesForUpdateMyUin() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForUpdateMyUin( eventId);
        assertEquals("otp",ackTemplateVariables.get(TemplateVariablesEnum.AUTHENTICATION_MODE));
    }

    @Test
    public void getAckTemplateVariablesForGenerateVid() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForGenerateVid( eventId);
        assertEquals("otp",ackTemplateVariables.get(TemplateVariablesEnum.AUTHENTICATION_MODE));
    }

    @Test
    public void getAckTemplateVariablesForRevokeVid() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForRevokeVid( eventId);
        assertEquals("otp",ackTemplateVariables.get(TemplateVariablesEnum.AUTHENTICATION_MODE));
    }

    @Test
    public void getAckTemplateVariablesForVerifyPhoneOrEmail() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForVerifyPhoneEmail( eventId);
        assertEquals(eventId,ackTemplateVariables.get(TemplateVariablesEnum.EVENT_ID));
    }

    @Test
    public void getAckTemplateVariablesForAuthLock() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForAuthTypeLockUnlock( eventId);
        assertEquals("otp",ackTemplateVariables.get(TemplateVariablesEnum.AUTHENTICATION_MODE));
    }
}
