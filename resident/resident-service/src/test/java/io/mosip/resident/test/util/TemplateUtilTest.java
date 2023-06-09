package io.mosip.resident.test.util;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.EventStatus;
import io.mosip.resident.constant.EventStatusFailure;
import io.mosip.resident.constant.EventStatusInProgress;
import io.mosip.resident.constant.EventStatusSuccess;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.TemplateType;
import io.mosip.resident.constant.TemplateVariablesConstants;
import io.mosip.resident.dto.NotificationTemplateVariableDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.handler.service.ResidentConfigService;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ProxyPartnerManagementServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.test.service.IdentityServiceTest;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.util.Utility;
import io.mosip.resident.validator.RequestValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
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

    @Mock
    private RequestValidator validator;
    
    @Mock
    private Utility utility;
    
    @Mock
    private ProxyPartnerManagementServiceImpl proxyPartnerManagementServiceImpl;

    @Mock
    private Environment environment;

    @Mock
    private ResidentServiceImpl residentService;

    @Mock
    private ResidentConfigService residentConfigService;

    @Mock
    private ProxyMasterdataService proxyMasterdataService;

    private String eventId;
    private ResidentTransactionEntity residentTransactionEntity;

    private NotificationTemplateVariableDTO dto;

    private static final String OTP = "otp";

    private static final String PROPERTY = "YYYY-MM-DD HH:MM:SS";

	private static final String LOCALE_EN_US = "en-US";

    private Map<String, Object> templateResponse;
    private ResponseWrapper responseWrapper;
    private Map<String, String> templateVariables;
    private Map<String, Object> values;

    @Before
    public void setUp() throws ApisResourceAccessException, ResidentServiceCheckedException {
        eventId = "12345";
        residentTransactionEntity = new ResidentTransactionEntity();
        residentTransactionEntity.setEventId(eventId);
        residentTransactionEntity.setRequestTypeCode(RequestType.AUTHENTICATION_REQUEST.name());
        residentTransactionEntity.setPurpose("Test");
        residentTransactionEntity.setStatusCode(EventStatusSuccess.AUTHENTICATION_SUCCESSFULL.name());
        residentTransactionEntity.setRequestSummary("Test");
        residentTransactionEntity.setAuthTypeCode("otp");
        residentTransactionEntity.setAttributeList("YYYY-MM-DD HH:MM:SS");
        residentTransactionEntity.setCrDtimes(LocalDateTime.now());
        Mockito.when(residentTransactionRepository.findById(eventId)).thenReturn(java.util.Optional.ofNullable(residentTransactionEntity));
        Mockito.when(identityServiceImpl.getResidentIndvidualIdFromSession()).thenReturn(eventId);
        Mockito.when(validator.validateUin(Mockito.anyString())).thenReturn(true);
        ReflectionTestUtils.setField(templateUtil, "templateDatePattern", "dd-MM-yyyy");
        ReflectionTestUtils.setField(templateUtil, "templateTimePattern", "HH:mm:ss");
        Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn(PROPERTY);
        dto = new NotificationTemplateVariableDTO(eventId, RequestType.AUTHENTICATION_REQUEST, TemplateType.SUCCESS, "eng", "111111");
        templateResponse = new LinkedHashMap<>();
        templateVariables = new LinkedHashMap<>();
        values = new LinkedHashMap<>();
        values.put("test", String.class);
        templateVariables.put("eventId", eventId);
        responseWrapper = new ResponseWrapper<>();
        templateResponse.put(ResidentConstants.FILE_TEXT, "otp");
        responseWrapper.setResponse(templateResponse);
        Mockito.when(proxyMasterdataService.getAllTemplateBylangCodeAndTemplateTypeCode(Mockito.anyString(), Mockito.anyString())).thenReturn(
                responseWrapper);
        Mockito.when(residentService.getEventStatusCode(Mockito.anyString())).thenReturn(EventStatus.SUCCESS.getStatus());
    }

    @Test
    public void getAckTemplateVariablesForAuthenticationRequest() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForAuthenticationRequest(eventId, "eng", 0, LOCALE_EN_US).getT1();
        assertEquals(EventStatus.SUCCESS.getStatus(),ackTemplateVariables.get(TemplateVariablesConstants.EVENT_STATUS));
    }

    @Test
    public void getAckTemplateVariablesForCredentialShare() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForCredentialShare( eventId, "eng", 0, LOCALE_EN_US).getT1();
        assertEquals(OTP,ackTemplateVariables.get(TemplateVariablesConstants.AUTHENTICATION_MODE));
    }

    @Test
    public void getAckTemplateVariablesForDownloadPersonalizedCard() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForDownloadPersonalizedCard( eventId, "eng", 0, LOCALE_EN_US).getT1();
        assertEquals(OTP,ackTemplateVariables.get(TemplateVariablesConstants.AUTHENTICATION_MODE));
    }

    @Test
    public void getAckTemplateVariablesForOrderPhysicalCard() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForOrderPhysicalCard( eventId, "eng", 0, LOCALE_EN_US).getT1();
        assertEquals(OTP,ackTemplateVariables.get(TemplateVariablesConstants.AUTHENTICATION_MODE));
    }

    @Test
    public void getAckTemplateVariablesForOrderPhysicalCardPaymentFailed() {
        residentTransactionEntity.setStatusCode(EventStatusFailure.PAYMENT_FAILED.name());
        Mockito.when(residentTransactionRepository.findById(eventId)).thenReturn(java.util.Optional.ofNullable(residentTransactionEntity));
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForOrderPhysicalCard( eventId, "eng", 0, LOCALE_EN_US).getT1();
        assertEquals(OTP,ackTemplateVariables.get(TemplateVariablesConstants.AUTHENTICATION_MODE));
    }

    @Test
    public void getAckTemplateVariablesForGetMyId() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForGetMyId( eventId, "eng", 0, LOCALE_EN_US).getT1();
        assertEquals(eventId,ackTemplateVariables.get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getAckTemplateVariablesForBookAnAppointment() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForBookAnAppointment( eventId, "eng", 0, LOCALE_EN_US).getT1();
        assertEquals(Collections.emptyMap(),ackTemplateVariables);
    }

    @Test
    public void getAckTemplateVariablesForUpdateMyUin() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForUpdateMyUin( eventId, "eng", 0, LOCALE_EN_US).getT1();
        assertEquals(OTP,ackTemplateVariables.get(TemplateVariablesConstants.AUTHENTICATION_MODE));
    }

    @Test
    public void getAckTemplateVariablesForGenerateVid() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForGenerateVid( eventId, "eng", 0, LOCALE_EN_US).getT1();
        assertEquals(OTP,ackTemplateVariables.get(TemplateVariablesConstants.AUTHENTICATION_MODE));
    }

    @Test
    public void getAckTemplateVariablesForRevokeVid() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForRevokeVid( eventId, "eng", 0, LOCALE_EN_US).getT1();
        assertEquals(OTP,ackTemplateVariables.get(TemplateVariablesConstants.AUTHENTICATION_MODE));
    }

    @Test
    public void getAckTemplateVariablesForVerifyPhoneOrEmail() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForVerifyPhoneEmail( eventId, 0, LOCALE_EN_US);
        assertEquals(eventId,ackTemplateVariables.get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getAckTemplateVariablesForAuthLock() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForAuthTypeLockUnlock( eventId, "eng", 0, LOCALE_EN_US).getT1();
        assertEquals(OTP,ackTemplateVariables.get(TemplateVariablesConstants.AUTHENTICATION_MODE));
    }

    @Test(expected = ResidentServiceException.class)
    public void getCommonTemplateVariablesTestBadEventId() {
        Mockito.when(residentTransactionRepository.findById(eventId)).thenReturn(java.util.Optional.empty());
        templateUtil.getCommonTemplateVariables(eventId, "", 0, LOCALE_EN_US);
    }

    @Test
    public void getCommonTemplateVariablesTestFailedEventStatus() {
        residentTransactionEntity.setStatusCode(EventStatusFailure.AUTHENTICATION_FAILED.name());
        Mockito.when(residentService.getEventStatusCode(Mockito.anyString())).thenReturn(EventStatus.FAILED.getStatus());
        assertEquals(EventStatus.FAILED.getStatus(),templateUtil.getCommonTemplateVariables(eventId, "eng", 0, LOCALE_EN_US).getT1().get(
                TemplateVariablesConstants.EVENT_STATUS
        ));
    }

    @Test
    public void getCommonTemplateVariablesTestInProgressEventStatus() {
        residentTransactionEntity.setStatusCode(EventStatusInProgress.OTP_REQUESTED.name());
        Mockito.when(residentService.getEventStatusCode(Mockito.anyString())).thenReturn(EventStatus.IN_PROGRESS.getStatus());
        assertEquals(EventStatus.IN_PROGRESS.getStatus(),templateUtil.getCommonTemplateVariables(eventId, "eng", 0, LOCALE_EN_US).getT1().get(
                TemplateVariablesConstants.EVENT_STATUS
        ));
    }

    @Test
    public void getAckTemplateVariablesForVidCardDownloadTest() {
        assertEquals(2,templateUtil.getAckTemplateVariablesForVidCardDownload(eventId, "eng", 0, LOCALE_EN_US).size());
    }

    @Test
    public void getAckTemplateVariablesForSendOtpTest() {
        assertEquals(eventId,templateUtil.getAckTemplateVariablesForSendOtp(eventId, "eng", 0, LOCALE_EN_US).getT1().get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getAckTemplateVariablesForValidateOtpTest() {
        assertEquals(eventId,templateUtil.getAckTemplateVariablesForValidateOtp(eventId, "eng", 0, LOCALE_EN_US).getT1().get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationCommonTemplateVariablesTest() {
        assertEquals(eventId,templateUtil.getNotificationCommonTemplateVariables(dto).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationCommonTemplateVariablesTestFailed() {
        dto = new NotificationTemplateVariableDTO(eventId, RequestType.AUTHENTICATION_REQUEST, TemplateType.FAILURE, "eng", "111111");
        assertEquals(eventId,templateUtil.getNotificationCommonTemplateVariables(dto).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationSendOtpVariablesTest() {
        assertEquals(eventId,templateUtil.getNotificationSendOtpVariables(dto).get(TemplateVariablesConstants.EVENT_ID));
    }

    public void getNotificationCommonTemplateVariablesTestFailedApiResourceException() throws ApisResourceAccessException {
        Mockito.when(identityServiceImpl.getResidentIndvidualIdFromSession()).thenThrow(new ApisResourceAccessException());
        dto = new NotificationTemplateVariableDTO(eventId, RequestType.AUTHENTICATION_REQUEST, TemplateType.FAILURE, "eng", "111111");
        assertEquals(eventId,templateUtil.getNotificationCommonTemplateVariables(dto).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationTemplateVariablesForGenerateOrRevokeVidTest() {
        assertEquals(eventId,templateUtil.getNotificationTemplateVariablesForGenerateOrRevokeVid(dto).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationTemplateVariablesForAuthTypeLockUnlockTest() {
        assertEquals(eventId,templateUtil.getNotificationTemplateVariablesForAuthTypeLockUnlock(dto).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationTemplateVariablesForUpdateMyUinTest() {
        assertEquals(eventId,templateUtil.getNotificationTemplateVariablesForUpdateMyUin(dto).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationTemplateVariablesForVerifyPhoneEmailTest() {
        assertEquals(eventId,templateUtil.getNotificationTemplateVariablesForVerifyPhoneEmail(dto).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationTemplateVariablesForGetMyIdTest() {
        assertEquals(eventId,templateUtil.getNotificationTemplateVariablesForGetMyId(dto).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationTemplateVariablesForDownloadPersonalizedCardTest() {
        assertEquals(eventId,templateUtil.getNotificationTemplateVariablesForDownloadPersonalizedCard(dto).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationTemplateVariablesForOrderPhysicalCardTest() {
        assertEquals(eventId,templateUtil.getNotificationTemplateVariablesForOrderPhysicalCard(dto).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationTemplateVariablesForShareCredentialWithPartnerTest() {
        assertEquals(eventId,templateUtil.getNotificationTemplateVariablesForShareCredentialWithPartner(dto).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationTemplateVariablesForVidCardDownloadTest() {
        assertEquals(eventId,templateUtil.getNotificationTemplateVariablesForVidCardDownload(dto).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getEmailSubjectTemplateTypeCodeTest() {
        assertEquals(PROPERTY,
                templateUtil.getEmailSubjectTemplateTypeCode(RequestType.AUTHENTICATION_REQUEST, TemplateType.SUCCESS));
    }

    @Test
    public void getEmailContentTemplateTypeCodeTest() {
        assertEquals(PROPERTY,
                templateUtil.getEmailContentTemplateTypeCode(RequestType.AUTHENTICATION_REQUEST, TemplateType.SUCCESS));
    }

    @Test
    public void getSmsTemplateTypeCodeTest() {
        assertEquals(PROPERTY,
                templateUtil.getSmsTemplateTypeCode(RequestType.AUTHENTICATION_REQUEST, TemplateType.SUCCESS));
    }

    @Test
    public void getBellIconTemplateTypeCodeTest() {
        assertEquals(PROPERTY,
                templateUtil.getBellIconTemplateTypeCode(RequestType.AUTHENTICATION_REQUEST, TemplateType.SUCCESS));
    }

    @Test
    public void getPurposeTemplateTypeCodeTest() {
        assertEquals(PROPERTY,
                templateUtil.getPurposeTemplateTypeCode(RequestType.AUTHENTICATION_REQUEST, TemplateType.SUCCESS));
    }

    @Test
    public void getSummaryTemplateTypeCodeTest() {
        assertEquals(PROPERTY,
                templateUtil.getSummaryTemplateTypeCode(RequestType.AUTHENTICATION_REQUEST, TemplateType.SUCCESS));
    }

    @Test
    public void testGetDescriptionTemplateVariablesForDownloadPersonalizedCard(){
        assertEquals("VID", templateUtil.
                getDescriptionTemplateVariablesForDownloadPersonalizedCard(residentTransactionEntity, "VID", "eng"));
    }

    @Test
    public void testGetDescriptionTemplateVariablesForDownloadPersonalizedCardNullFileText(){
        templateUtil.
                getDescriptionTemplateVariablesForDownloadPersonalizedCard(residentTransactionEntity, null, "eng");
    }

    @Test
    public void testGetDescriptionTemplateVariablesForDownloadPersonalizedCardSuccess(){
        templateUtil.
                getDescriptionTemplateVariablesForDownloadPersonalizedCard(residentTransactionEntity, ResidentConstants.ATTRIBUTES.toString(), "eng");
    }

    @Test
    public void testGetDescriptionTemplateVariablesForDownloadPersonalizedCardFailure(){
        residentTransactionEntity.setAttributeList(null);
        residentTransactionEntity.setPurpose(null);
        Mockito.when(residentTransactionRepository.findById(eventId)).thenReturn(java.util.Optional.ofNullable(residentTransactionEntity));
        templateUtil.
                getDescriptionTemplateVariablesForDownloadPersonalizedCard(residentTransactionEntity, ResidentConstants.ATTRIBUTES.toString(), "eng");
    }

    @Test(expected = ResidentServiceException.class)
    public void testGetTemplateValueFromTemplateTypeCodeAndLangCode() throws ResidentServiceCheckedException {
        Mockito.when(proxyMasterdataService.getAllTemplateBylangCodeAndTemplateTypeCode(Mockito.anyString(), Mockito.anyString()))
                        .thenThrow(new ResidentServiceCheckedException());
        assertEquals(PROPERTY,
                templateUtil.getTemplateValueFromTemplateTypeCodeAndLangCode("eng", "ack"));

    }

    @Test
    public void getCommonTemplateVariablesTestForRequestTypeNotPresentInServiceType() throws ResidentServiceCheckedException {
        templateResponse.put(ResidentConstants.FILE_TEXT, "Unknown");
        responseWrapper.setResponse(templateResponse);
        Mockito.when(proxyMasterdataService.getAllTemplateBylangCodeAndTemplateTypeCode(Mockito.anyString(), Mockito.anyString())).thenReturn(
                responseWrapper);
        residentTransactionEntity.setStatusCode(EventStatusInProgress.OTP_REQUESTED.name());
        residentTransactionEntity.setRequestTypeCode("requestType");
        Mockito.when(residentService.getEventStatusCode(Mockito.anyString())).thenReturn(EventStatus.IN_PROGRESS.getStatus());
        assertEquals(EventStatus.IN_PROGRESS.getStatus(),templateUtil.getCommonTemplateVariables(eventId, "eng", 0).getT1().get(
                TemplateVariablesConstants.EVENT_STATUS
        ));
    }

    @Test
    public void getCommonTemplateVariablesTestApiResourceException() throws ResidentServiceCheckedException, ApisResourceAccessException {
        templateResponse.put(ResidentConstants.FILE_TEXT, "Unknown");
        responseWrapper.setResponse(templateResponse);
        Mockito.when(proxyMasterdataService.getAllTemplateBylangCodeAndTemplateTypeCode(Mockito.anyString(), Mockito.anyString())).thenReturn(
                responseWrapper);
        residentTransactionEntity.setStatusCode(EventStatusInProgress.OTP_REQUESTED.name());
        residentTransactionEntity.setRequestTypeCode("requestType");
        Mockito.when(residentService.getEventStatusCode(Mockito.anyString())).thenReturn(EventStatus.IN_PROGRESS.getStatus());
        Mockito.when(identityServiceImpl.getResidentIndvidualIdFromSession()).thenThrow(new ApisResourceAccessException());
        assertEquals(EventStatus.IN_PROGRESS.getStatus(),templateUtil.getCommonTemplateVariables(eventId, "eng", 0).getT1().get(
                TemplateVariablesConstants.EVENT_STATUS
        ));
    }

    @Test
    public void getDescriptionTemplateVariablesForAuthenticationRequestTest() {
        residentTransactionEntity.setStatusComment("SUCCESS");
        assertEquals("SUCCESS",
                templateUtil.getDescriptionTemplateVariablesForAuthenticationRequest
                        (residentTransactionEntity, null, null));
    }

    @Test
    public void getDescriptionTemplateVariablesForShareCredentialTest(){
        templateUtil.getDescriptionTemplateVariablesForShareCredential(residentTransactionEntity, null, null);
    }

    @Test
    public void getDescriptionTemplateVariablesForOrderPhysicalCardTest(){
        assertEquals("OTP", templateUtil.getDescriptionTemplateVariablesForOrderPhysicalCard(
                residentTransactionEntity, "OTP", "eng"));
    }

    @Test
    public void getDescriptionTemplateVariablesForGetMyIdTest(){
        assertEquals("OTP", templateUtil.getDescriptionTemplateVariablesForGetMyId(
                residentTransactionEntity, "OTP", "eng"));
    }

    @Test
    public void getDescriptionTemplateVariablesForUpdateMyUinTest(){
        templateUtil.getDescriptionTemplateVariablesForUpdateMyUin(residentTransactionEntity, "OTP", "eng");
    }

    @Test
    public void getDescriptionTemplateVariablesForManageMyVidTest(){
        assertEquals("OTP", templateUtil.getDescriptionTemplateVariablesForManageMyVid(
                residentTransactionEntity, "OTP", "eng"));
    }

    @Test
    public void getDescriptionTemplateVariablesForManageMyVidGenerateVidTest(){
        residentTransactionEntity.setRequestTypeCode(RequestType.GENERATE_VID.name());
        assertEquals("OTP", templateUtil.getDescriptionTemplateVariablesForManageMyVid(
                residentTransactionEntity, "OTP", "eng"));
    }

    @Test
    public void getDescriptionTemplateVariablesForManageMyVidRevokeVidTest(){
        residentTransactionEntity.setRequestTypeCode(RequestType.REVOKE_VID.name());
        assertEquals("OTP", templateUtil.getDescriptionTemplateVariablesForManageMyVid(
                residentTransactionEntity, "OTP", "eng"));
    }

    @Test
    public void getDescriptionTemplateVariablesForVidCardDownloadTest(){
        assertEquals("OTP", templateUtil.getDescriptionTemplateVariablesForVidCardDownload(residentTransactionEntity, "OTP",
                "eng"));
    }

    @Test
    public void getDescriptionTemplateVariablesForValidateOtpTest(){
        assertEquals("OTP", templateUtil.getDescriptionTemplateVariablesForValidateOtp(residentTransactionEntity, "OTP",
                "eng"));
    }

    @Test
    public void getDescriptionTemplateVariablesForValidateOtpNullChannelTest(){
        residentTransactionEntity.setPurpose(null);
        assertEquals("OTP", templateUtil.getDescriptionTemplateVariablesForValidateOtp(residentTransactionEntity, "OTP",
                "eng"));
    }

    @Test
    public void getDescriptionTemplateVariablesForValidateOtpEmptyChannelTest(){
        residentTransactionEntity.setPurpose("");
        assertEquals("OTP", templateUtil.getDescriptionTemplateVariablesForValidateOtp(residentTransactionEntity, "OTP",
                "eng"));
    }

    @Test
    public void getDescriptionTemplateVariablesForSecureMyIdTest(){
        residentTransactionEntity.setPurpose("fullName,dateOfBirth,UIN,perpetualVID,phone,email");
        assertEquals("OTP, OTP, OTP, OTP, OTP, OTP", templateUtil.getDescriptionTemplateVariablesForSecureMyId(
                residentTransactionEntity,
                "OTP", "eng"));
    }

    @Test
    public void getDescriptionTemplateVariablesForSecureMyIdUnlockedTest(){
        residentTransactionEntity.setPurpose("UNLOCKED,dateOfBirth,UIN,perpetualVID,phone,email");
        assertEquals("OTP, OTP, OTP, OTP, OTP, OTP", templateUtil.getDescriptionTemplateVariablesForSecureMyId(
                residentTransactionEntity,
                "OTP", "eng"));
    }

    @Test
    public void getDefaultTemplateVariablesTest(){
        templateUtil.getDefaultTemplateVariables(eventId, "eng", 0);
    }

    @Test
    public void getFeatureNameTest(){
        templateUtil.getFeatureName(eventId);
    }

    @Test
    public void getPurposeFromResidentTransactionEntityLangCodeTest() throws ResidentServiceCheckedException {
        Mockito.when(residentService.getDescriptionForLangCode(Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenThrow(new ResidentServiceCheckedException());
        assertEquals("",templateUtil.getPurposeFromResidentTransactionEntityLangCode(residentTransactionEntity, "eng"));
    }

    @Test
    public void getSummaryFromResidentTransactionEntityLangCodeTest() throws ResidentServiceCheckedException {
        Mockito.when(residentService.getSummaryForLangCode(Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenThrow(new ResidentServiceCheckedException());
        assertEquals("AUTHENTICATION_REQUEST",templateUtil.getSummaryFromResidentTransactionEntityLangCode(
                residentTransactionEntity, "eng", "SUCCESS",
                RequestType.AUTHENTICATION_REQUEST));
    }

    @Test
    public void getNotificationCommonTemplateVariablesSecureSessionTest() {
        IdentityServiceTest.getAuthUserDetailsFromAuthentication();
        assertEquals(eventId,templateUtil.getNotificationCommonTemplateVariables(dto).get(TemplateVariablesConstants.EVENT_ID));
    }
}
