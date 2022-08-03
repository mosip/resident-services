package io.mosip.resident.test.util;

import io.mosip.resident.constant.EventStatusSuccess;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.TemplateVariablesEnum;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.util.TemplateUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

/**
 * This class is used to test the TemplateUtil class
 * @author Kamesh Shekhar Prasad
 */

@RunWith(SpringRunner.class)
public class TemplateUtilTest {

    @InjectMocks
    private TemplateUtil templateUtil = new TemplateUtil();

    @Mock
    private ResidentTransactionRepository residentTransactionRepository;

    @Mock
    private IdentityServiceImpl identityServiceImpl;

    private String eventId;
    private ResidentTransactionEntity residentTransactionEntity;

    @Before
    public void setUp() throws ApisResourceAccessException {
        eventId = "12345";
        residentTransactionEntity = new ResidentTransactionEntity();
        residentTransactionEntity.setEventId(eventId);
        residentTransactionEntity.setRequestTypeCode(RequestType.AUTHENTICATION_REQUEST.name());
        residentTransactionEntity.setPurpose("Test");
        residentTransactionEntity.setStatusCode(EventStatusSuccess.AUTHENTICATION_SUCCESSFUL.name());
        residentTransactionEntity.setRequestSummary("Test");
        residentTransactionEntity.setCrDtimes(LocalDateTime.now());
        Mockito.when(residentTransactionRepository.findById(eventId)).thenReturn(java.util.Optional.ofNullable(residentTransactionEntity));
        Mockito.when(identityServiceImpl.getResidentIndvidualId()).thenReturn(eventId);
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
