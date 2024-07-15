package io.mosip.resident.util;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import io.mosip.resident.service.impl.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.EventStatus;
import io.mosip.resident.constant.EventStatusFailure;
import io.mosip.resident.constant.EventStatusInProgress;
import io.mosip.resident.constant.EventStatusSuccess;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ServiceType;
import io.mosip.resident.constant.TemplateType;
import io.mosip.resident.constant.TemplateVariablesConstants;
import io.mosip.resident.dto.NotificationTemplateVariableDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.handler.service.ResidentConfigService;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.ProxyMasterdataService;
import reactor.util.function.Tuples;

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
    private Utility utility;
    
    @Mock
    private ProxyPartnerManagementServiceImpl proxyPartnerManagementServiceImpl;

    @Mock
    private Environment environment;

    @Mock
    private TemplateValueFromTemplateTypeCodeAndLangCode templateValueFromTemplateTypeCodeAndLangCode;

    @Mock
    private ResidentServiceImpl residentService;

    @Mock
    private ResidentConfigService residentConfigService;

    @Mock
    private EventStatusCode eventStatusCode;

    @Mock
    private ProxyMasterdataService proxyMasterdataService;

    @Mock
    private AvailableClaimUtility availableClaimUtility;

    @Mock
    private UinVidValidator uinVidValidator;

    @InjectMocks
    private SummaryForLangCode summaryForLangCodeMock = new SummaryForLangCode();

    @Mock
    private EventStatusBasedOnLangCode eventStatusBasedOnLangCode;

    @InjectMocks
    private SmsTemplateTypeCode smsTemplateTypeCode = new SmsTemplateTypeCode();

    @InjectMocks
    private PurposeTemplateTypeCode purposeTemplateTypeCode = new PurposeTemplateTypeCode();

    @Mock
    private SummaryForLangCode summaryForLangCode;

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

	private Map<String, Object> mailingAttributes = Map.of();

    @Mock
    private DescriptionTemplateVariables descriptionTemplateVariables;

    @Mock
    private AttributeBasedOnLangCode attributeBasedOnLangCode;

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
        Mockito.when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn(eventId);
        Mockito.when(uinVidValidator.getIndividualIdType(Mockito.anyString())).thenReturn(IdType.UIN);
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
        Mockito.when(proxyMasterdataService.getTemplateValueFromTemplateTypeCodeAndLangCode(Mockito.anyString(), Mockito.anyString())).thenReturn(
                "otp");
        Mockito.when(eventStatusCode.getEventStatusCode(Mockito.anyString(), Mockito.anyString())).thenReturn(Tuples.of(EventStatus.SUCCESS.name(), "Success"));
        Mockito.when(eventStatusBasedOnLangCode.getEventStatusBasedOnLangcode(Mockito.any(), Mockito.anyString())).thenReturn("test");
        Mockito.when(eventStatusBasedOnLangCode.getTemplateTypeCode(Mockito.anyString())).thenReturn("test");
        Mockito.when(templateValueFromTemplateTypeCodeAndLangCode.getTemplateValueFromTemplateTypeCodeAndLangCode(
                Mockito.anyString(), Mockito.anyString())).thenReturn("test");
    }

    @Test
    public void getAckTemplateVariablesForAuthenticationRequest() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForAuthenticationRequest(residentTransactionEntity, "eng", 0, LOCALE_EN_US).getT1();
        assertEquals("Success",ackTemplateVariables.get(TemplateVariablesConstants.EVENT_STATUS));
    }

    @Test
    public void getAckTemplateVariablesForCredentialShare() {
    	getUISchemaData();
    	residentTransactionEntity.setAttributeList("fullName:fullName");
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForShareCredentialWithPartner(residentTransactionEntity, "eng", 0, LOCALE_EN_US).getT1();
        assertEquals("test",ackTemplateVariables.get(TemplateVariablesConstants.AUTHENTICATION_MODE));
    }

    @Test
    public void getAckTemplateVariablesForDownloadPersonalizedCard() {
    	getUISchemaData();
    	residentTransactionEntity.setAttributeList("fullName:fullName");
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForDownloadPersonalizedCard(residentTransactionEntity, "eng", 0, LOCALE_EN_US).getT1();
        assertEquals("test",ackTemplateVariables.get(TemplateVariablesConstants.AUTHENTICATION_MODE));
    }

	private void getUISchemaData() {
		Map<String, Object> attrData = new HashMap<>();
    	attrData.put(ResidentConstants.LABEL, "Name");
    	attrData.put(ResidentConstants.FORMAT_OPTION, Map.of("fullName", "Full Name"));
    	Map<String, Map<String, Object>> schemaData = new HashMap<>();
    	schemaData.put("fullName", attrData);
    	Map<String, Map<String, Map<String, Object>>> schemaLangData = new HashMap<>();
    	schemaLangData.put("eng", schemaData);
    	Mockito.when(residentConfigService.getUISchemaCacheableData(anyString())).thenReturn(schemaLangData);
	}

    @Test
    public void getAckTemplateVariablesForOrderPhysicalCard() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForOrderPhysicalCard(residentTransactionEntity, "eng", 0, LOCALE_EN_US).getT1();
        assertEquals("test",ackTemplateVariables.get(TemplateVariablesConstants.AUTHENTICATION_MODE));
    }

    @Test
    public void getAckTemplateVariablesForOrderPhysicalCardPaymentFailed() {
        residentTransactionEntity.setStatusCode(EventStatusFailure.PAYMENT_FAILED.name());
        Mockito.when(residentTransactionRepository.findById(eventId)).thenReturn(java.util.Optional.ofNullable(residentTransactionEntity));
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForOrderPhysicalCard(residentTransactionEntity, "eng", 0, LOCALE_EN_US).getT1();
        assertEquals("test",ackTemplateVariables.get(TemplateVariablesConstants.AUTHENTICATION_MODE));
    }

    @Test
    public void getAckTemplateVariablesForGetMyId() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForGetMyId(residentTransactionEntity, "eng", 0, LOCALE_EN_US).getT1();
        assertEquals(eventId,ackTemplateVariables.get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getAckTemplateVariablesForUpdateMyUin() {
    	getUISchemaData();
    	residentTransactionEntity.setAttributeList("fullName");
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForUpdateMyUin(residentTransactionEntity, "eng", 0, LOCALE_EN_US).getT1();
        assertEquals("test",ackTemplateVariables.get(TemplateVariablesConstants.AUTHENTICATION_MODE));
    }

    @Test
    public void getAckTemplateVariablesForUpdateMyUinDownloadLinkNotAvailable() {
    	getUISchemaData();
    	residentTransactionEntity.setAttributeList("fullName");
    	residentTransactionEntity.setStatusCode(EventStatusSuccess.CARD_DOWNLOADED.name());
        Map<String, String> ackTemplateVariables1 = templateUtil.getAckTemplateVariablesForUpdateMyUin(residentTransactionEntity, "eng", 0, LOCALE_EN_US).getT1();
        assertEquals("test",ackTemplateVariables1.get(TemplateVariablesConstants.AUTHENTICATION_MODE));
        residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
        Map<String, String> ackTemplateVariables2 = templateUtil.getAckTemplateVariablesForUpdateMyUin(residentTransactionEntity, "eng", 0, LOCALE_EN_US).getT1();
        assertEquals("test",ackTemplateVariables2.get(TemplateVariablesConstants.AUTHENTICATION_MODE));
    }

    @Test
    public void getAckTemplateVariablesForGenerateVid() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForGenerateVid(residentTransactionEntity, "eng", 0, LOCALE_EN_US).getT1();
        assertEquals("test",ackTemplateVariables.get(TemplateVariablesConstants.AUTHENTICATION_MODE));
    }

    @Test
    public void getAckTemplateVariablesForRevokeVid() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForRevokeVid(residentTransactionEntity, "eng", 0, LOCALE_EN_US).getT1();
        assertEquals("test",ackTemplateVariables.get(TemplateVariablesConstants.AUTHENTICATION_MODE));
    }

    @Test
    public void getAckTemplateVariablesForAuthLock() {
        Map<String, String> ackTemplateVariables = templateUtil.getAckTemplateVariablesForAuthTypeLockUnlock(residentTransactionEntity, "eng", 0, LOCALE_EN_US).getT1();
        assertEquals("test",ackTemplateVariables.get(TemplateVariablesConstants.AUTHENTICATION_MODE));
    }

    @Test
    public void getCommonTemplateVariablesTestFailedEventStatus() {
        residentTransactionEntity.setStatusCode(EventStatusFailure.AUTHENTICATION_FAILED.name());
        residentTransactionEntity.setAuthTypeCode("");
        Mockito.when(eventStatusCode.getEventStatusCode(Mockito.anyString(), Mockito.anyString())).thenReturn(Tuples.of(EventStatus.FAILED.name(), "Failed"));
        assertEquals("Failed",templateUtil.getCommonTemplateVariables(residentTransactionEntity, RequestType.AUTHENTICATION_REQUEST, "eng", 0, LOCALE_EN_US).get(
                TemplateVariablesConstants.EVENT_STATUS
        ));
    }

    @Test
    public void getCommonTemplateVariablesTestInProgressEventStatus() {
        residentTransactionEntity.setStatusCode(EventStatusInProgress.OTP_REQUESTED.name());
        residentTransactionEntity.setAuthTypeCode(null);
        Mockito.when(eventStatusCode.getEventStatusCode(Mockito.anyString(), Mockito.anyString())).thenReturn(Tuples.of(EventStatus.IN_PROGRESS.name(), "In Progress"));
        assertEquals("In Progress",templateUtil.getCommonTemplateVariables(residentTransactionEntity, RequestType.AUTHENTICATION_REQUEST, "eng", 0, LOCALE_EN_US).get(
                TemplateVariablesConstants.EVENT_STATUS
        ));
    }

    @Test
    public void getAckTemplateVariablesForVidCardDownloadTest() {
        assertEquals(2,templateUtil.getAckTemplateVariablesForVidCardDownload(residentTransactionEntity, "eng", 0, LOCALE_EN_US).size());
    }

    @Test
    public void getAckTemplateVariablesForVidCardDownloadLinkNotAvailable() {
    	residentTransactionEntity.setStatusCode(EventStatusSuccess.CARD_DOWNLOADED.name());
        assertEquals(2,templateUtil.getAckTemplateVariablesForVidCardDownload(residentTransactionEntity, "eng", 0, LOCALE_EN_US).size());
        residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
        assertEquals(2,templateUtil.getAckTemplateVariablesForVidCardDownload(residentTransactionEntity, "eng", 0, LOCALE_EN_US).size());
    }

    @Test
    public void getAckTemplateVariablesForSendOtpTest() {
        assertEquals(eventId,templateUtil.getAckTemplateVariablesForSendOtp(residentTransactionEntity, "eng", 0, LOCALE_EN_US).getT1().get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getAckTemplateVariablesForValidateOtpTest() {
        assertEquals(eventId,templateUtil.getAckTemplateVariablesForValidateOtp(residentTransactionEntity, "eng", 0, LOCALE_EN_US).getT1().get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationCommonTemplateVariablesTest() {
        assertEquals(eventId,templateUtil.getNotificationCommonTemplateVariables(dto, mailingAttributes).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationCommonTemplateVariablesTestFailed() {
        dto = new NotificationTemplateVariableDTO(eventId, RequestType.AUTHENTICATION_REQUEST, TemplateType.FAILURE, "eng", "111111");
        assertEquals(eventId,templateUtil.getNotificationCommonTemplateVariables(dto, mailingAttributes).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationSendOtpVariablesTest() {
        assertEquals(eventId,templateUtil.getNotificationSendOtpVariables(dto, mailingAttributes).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationCommonTemplateVariablesTestException() throws ResidentServiceCheckedException, IOException {
        Mockito.when(utility.getMappingValue(Mockito.anyMap(), Mockito.anyString(), Mockito.anyString())).thenThrow(new ResidentServiceCheckedException());
        dto = new NotificationTemplateVariableDTO(eventId, RequestType.AUTHENTICATION_REQUEST, TemplateType.FAILURE, "eng", "111111");
        assertEquals(eventId,templateUtil.getNotificationCommonTemplateVariables(dto, mailingAttributes).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationTemplateVariablesForGenerateOrRevokeVidTest() {
        assertEquals(eventId,templateUtil.getNotificationTemplateVariablesForGenerateOrRevokeVid(dto, mailingAttributes).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationTemplateVariablesForAuthTypeLockUnlockTest() {
        assertEquals(eventId,templateUtil.getNotificationTemplateVariablesForAuthTypeLockUnlock(dto, mailingAttributes).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationTemplateVariablesForUpdateMyUinTest() {
        assertEquals(eventId,templateUtil.getNotificationTemplateVariablesForUpdateMyUin(dto, mailingAttributes).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationTemplateVariablesForVerifyPhoneEmailTest() {
        assertEquals(eventId,templateUtil.getNotificationTemplateVariablesForVerifyPhoneEmail(dto, mailingAttributes).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationTemplateVariablesForGetMyIdTest() {
        assertEquals(eventId,templateUtil.getNotificationTemplateVariablesForGetMyId(dto, mailingAttributes).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationTemplateVariablesForDownloadPersonalizedCardTest() {
        assertEquals(eventId,templateUtil.getNotificationTemplateVariablesForDownloadPersonalizedCard(dto, mailingAttributes).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationTemplateVariablesForOrderPhysicalCardTest() {
        assertEquals(eventId,templateUtil.getNotificationTemplateVariablesForOrderPhysicalCard(dto, mailingAttributes).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationTemplateVariablesForShareCredentialWithPartnerTest() {
        assertEquals(eventId,templateUtil.getNotificationTemplateVariablesForShareCredentialWithPartner(dto, mailingAttributes).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getNotificationTemplateVariablesForVidCardDownloadTest() {
        assertEquals(eventId,templateUtil.getNotificationTemplateVariablesForVidCardDownload(dto, mailingAttributes).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void getEmailSubjectTemplateTypeCodeTest() {
        assertEquals("test",
                templateUtil.getEmailSubjectTemplateTypeCode(RequestType.AUTHENTICATION_REQUEST, TemplateType.SUCCESS));
    }

    @Test
    public void getEmailContentTemplateTypeCodeTest() {
        assertEquals("test",
                templateUtil.getEmailContentTemplateTypeCode(RequestType.AUTHENTICATION_REQUEST, TemplateType.SUCCESS));
    }

    @Test
    public void getSmsTemplateTypeCodeTest() {
        assertEquals("test",
                smsTemplateTypeCode.getSmsTemplateTypeCode(RequestType.AUTHENTICATION_REQUEST, TemplateType.SUCCESS));
    }

    @Test
    public void getPurposeTemplateTypeCodeTest() {
        assertEquals("test",
                purposeTemplateTypeCode.getPurposeTemplateTypeCode(RequestType.AUTHENTICATION_REQUEST, TemplateType.SUCCESS));
    }

    @Test
    public void getSummaryTemplateTypeCodeTest() {
        assertEquals("test",
                summaryForLangCodeMock.getSummaryTemplateTypeCode(RequestType.AUTHENTICATION_REQUEST, TemplateType.SUCCESS));
    }

    @Test
    public void testGetDescriptionTemplateVariablesForDownloadPersonalizedCard(){
        assertEquals("VID", descriptionTemplateVariables.
                getDescriptionTemplateVariablesForDownloadPersonalizedCard(residentTransactionEntity, "VID", "eng"));
    }

    @Test
    public void testGetDescriptionTemplateVariablesForDownloadPersonalizedCardNullFileText(){
        descriptionTemplateVariables.
                getDescriptionTemplateVariablesForDownloadPersonalizedCard(residentTransactionEntity, null, "eng");
    }

    @Test
    public void testGetDescriptionTemplateVariablesForDownloadPersonalizedCardSuccess(){
        descriptionTemplateVariables.
                getDescriptionTemplateVariablesForDownloadPersonalizedCard(residentTransactionEntity, ResidentConstants.ATTRIBUTES.toString(), "eng");
    }

    @Test
    public void testGetDescriptionTemplateVariablesForDownloadPersonalizedCardFailure(){
        residentTransactionEntity.setAttributeList(null);
        residentTransactionEntity.setPurpose(null);
        Mockito.when(residentTransactionRepository.findById(eventId)).thenReturn(java.util.Optional.ofNullable(residentTransactionEntity));
        descriptionTemplateVariables.
                getDescriptionTemplateVariablesForDownloadPersonalizedCard(residentTransactionEntity, ResidentConstants.ATTRIBUTES.toString(), "eng");
    }

    @Test
    public void getCommonTemplateVariablesTestForRequestTypeNotPresentInServiceType() throws ResidentServiceCheckedException {
        residentTransactionEntity.setStatusCode(EventStatusInProgress.OTP_REQUESTED.name());
        residentTransactionEntity.setRequestTypeCode(RequestType.SEND_OTP.name());
        Mockito.when(eventStatusCode.getEventStatusCode(Mockito.anyString(), Mockito.anyString())).thenReturn(Tuples.of(EventStatus.IN_PROGRESS.name(), "In Progress"));
        Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("template-type-code").thenReturn(null).thenReturn("template-type-code");
        assertEquals("In Progress",templateUtil.getCommonTemplateVariables(residentTransactionEntity, RequestType.SEND_OTP, "eng", 0, LOCALE_EN_US).get(
                TemplateVariablesConstants.EVENT_STATUS
        ));
    }

    @Test
    public void getCommonTemplateVariablesTestApiResourceException() throws ResidentServiceCheckedException, ApisResourceAccessException {
        residentTransactionEntity.setStatusCode(EventStatusInProgress.OTP_REQUESTED.name());
        residentTransactionEntity.setRequestTypeCode(RequestType.SEND_OTP.name());
        Mockito.when(eventStatusCode.getEventStatusCode(Mockito.anyString(), Mockito.anyString())).thenReturn(Tuples.of(EventStatus.IN_PROGRESS.name(), "In Progress"));
        Mockito.when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenThrow(new ApisResourceAccessException());
        assertEquals("In Progress",templateUtil.getCommonTemplateVariables(residentTransactionEntity, RequestType.SEND_OTP, "eng", 0, LOCALE_EN_US).get(
                TemplateVariablesConstants.EVENT_STATUS
        ));
    }

    @Test
    public void getDescriptionTemplateVariablesForAuthenticationRequestTest() {
    	Mockito.when(proxyMasterdataService.getTemplateValueFromTemplateTypeCodeAndLangCode(Mockito.anyString(), Mockito.anyString())).thenReturn(
                "OTP Authentication Success");
        assertEquals("test",
                descriptionTemplateVariables.getDescriptionTemplateVariablesForAuthenticationRequest
                        (residentTransactionEntity, null, "eng"));
    }

    @Test
    public void getDescriptionTemplateVariablesForAuthenticationRequestTestNullTempTypeCode() {
    	Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn(null).thenReturn("template-type-code");
    	Mockito.when(proxyMasterdataService.getTemplateValueFromTemplateTypeCodeAndLangCode(Mockito.anyString(), Mockito.anyString())).thenReturn(
                "OTP Authentication Success");
        assertEquals("test",
                descriptionTemplateVariables.getDescriptionTemplateVariablesForAuthenticationRequest
                        (residentTransactionEntity, null, "eng"));
    }

    @Test
    public void getDescriptionTemplateVariablesForShareCredentialTest(){
        descriptionTemplateVariables.getDescriptionTemplateVariablesForShareCredentialWithPartner(residentTransactionEntity, null, null);
    }

    @Test
    public void getDescriptionTemplateVariablesForOrderPhysicalCardTest(){
        assertEquals("OTP", descriptionTemplateVariables.getDescriptionTemplateVariablesForOrderPhysicalCard(
                residentTransactionEntity, "OTP", "eng"));
    }

    @Test
    public void getDescriptionTemplateVariablesForGetMyIdTest(){
        assertEquals("OTP", descriptionTemplateVariables.getDescriptionTemplateVariablesForGetMyId(
                residentTransactionEntity, "OTP", "eng"));
    }

    @Test
    public void getDescriptionTemplateVariablesForUpdateMyUinTest(){
        descriptionTemplateVariables.getDescriptionTemplateVariablesForUpdateMyUin(residentTransactionEntity, "OTP", "eng");
    }

    @Test
    public void getDescriptionTemplateVariablesForManageMyVidTest(){
        assertEquals("OTP", descriptionTemplateVariables.getDescriptionTemplateVariablesForManageMyVid(
                residentTransactionEntity, "OTP", "eng"));
    }

    @Test
    public void getDescriptionTemplateVariablesForManageMyVidGenerateVidTest(){
        residentTransactionEntity.setRequestTypeCode(RequestType.GENERATE_VID.name());
        assertEquals("OTP", descriptionTemplateVariables.getDescriptionTemplateVariablesForManageMyVid(
                residentTransactionEntity, "OTP", "eng"));
    }

    @Test
    public void getDescriptionTemplateVariablesForManageMyVidRevokeVidTest(){
        residentTransactionEntity.setRequestTypeCode(RequestType.REVOKE_VID.name());
        assertEquals("OTP", descriptionTemplateVariables.getDescriptionTemplateVariablesForManageMyVid(
                residentTransactionEntity, "OTP", "eng"));
    }

    @Test
    public void getDescriptionTemplateVariablesForVidCardDownloadTest(){
        assertEquals("OTP", descriptionTemplateVariables.getDescriptionTemplateVariablesForVidCardDownload(residentTransactionEntity, "OTP",
                "eng"));
    }

    @Test
    public void getDescriptionTemplateVariablesForValidateOtpTest(){
        assertEquals("OTP", descriptionTemplateVariables.getDescriptionTemplateVariablesForValidateOtp(residentTransactionEntity, "OTP",
                "eng"));
    }

    @Test
    public void getDescriptionTemplateVariablesForValidateOtpNullChannelTest(){
        residentTransactionEntity.setPurpose(null);
        assertEquals("OTP", descriptionTemplateVariables.getDescriptionTemplateVariablesForValidateOtp(residentTransactionEntity, "OTP",
                "eng"));
    }

    @Test
    public void getDescriptionTemplateVariablesForValidateOtpEmptyChannelTest(){
        residentTransactionEntity.setPurpose("");
        assertEquals("OTP", descriptionTemplateVariables.getDescriptionTemplateVariablesForValidateOtp(residentTransactionEntity, "OTP",
                "eng"));
    }

    @Test
    public void getDescriptionTemplateVariablesForSecureMyIdTest(){
        residentTransactionEntity.setAttributeList("fullName,dateOfBirth,UIN,perpetualVID,phone,email");
        assertEquals("OTP, OTP, OTP, OTP, OTP, OTP", descriptionTemplateVariables.getDescriptionTemplateVariablesForSecureMyId(
                residentTransactionEntity, "OTP", "eng"));
    }

    @Test
    public void getDescriptionTemplateVariablesForSecureMyIdUnlockedTest(){
    	residentTransactionEntity.setAttributeList(null);
        residentTransactionEntity.setPurpose("UNLOCKED,dateOfBirth,UIN,perpetualVID,phone,email");
        assertEquals("OTP, OTP, OTP, OTP, OTP, OTP", descriptionTemplateVariables.getDescriptionTemplateVariablesForSecureMyId(
                residentTransactionEntity, "OTP", "eng"));
    }

    @Test
    public void getDescriptionTemplateVariablesForSecureMyIdUnlockedTestNullDescription(){
    	residentTransactionEntity.setAttributeList("");
        residentTransactionEntity.setPurpose(null);
        assertEquals("OTP", descriptionTemplateVariables.getDescriptionTemplateVariablesForSecureMyId(
                residentTransactionEntity, "OTP", "eng"));
    }

    @Test
    public void getDefaultTemplateVariablesTest(){
        templateUtil.getAckTemplateVariablesForDefault(residentTransactionEntity, "eng", 0, LOCALE_EN_US);
    }

    @Test
    public void getSummaryFromResidentTransactionEntityLangCodeTest() throws ResidentServiceCheckedException {
        Mockito.when(summaryForLangCode.getSummaryForLangCode(Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenThrow(new ResidentServiceCheckedException());
        assertEquals("AUTHENTICATION_REQUEST",templateUtil.getSummaryFromResidentTransactionEntityLangCode(
                residentTransactionEntity, "eng", "SUCCESS",
                RequestType.AUTHENTICATION_REQUEST));
    }

    @Test
    public void getNotificationCommonTemplateVariablesSecureSessionTest() {
        IdentityServiceTest.getAuthUserDetailsFromAuthentication();
        assertEquals(eventId,templateUtil.getNotificationCommonTemplateVariables(dto, mailingAttributes).get(TemplateVariablesConstants.EVENT_ID));
    }

    @Test
    public void testGetEventTypeBasedOnLangcodeIf() {
    	Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn(null).thenReturn("template-type-code");
    	assertEquals("test", templateUtil.getEventTypeBasedOnLangcode(RequestType.AUTHENTICATION_REQUEST, "eng"));
    }

    @Test
    public void testGetServiceTypeBasedOnLangcode() {
    	Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("template-type-code");
    	assertEquals("test", templateUtil.getServiceTypeBasedOnLangcode(ServiceType.AUTHENTICATION_REQUEST, "eng"));
    	Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn(null).thenReturn("template-type-code");
    	assertEquals("test", templateUtil.getServiceTypeBasedOnLangcode(ServiceType.AUTHENTICATION_REQUEST, "eng"));
    }

    @Test
    public void testGetEventStatusBasedOnLangcode() {
    	Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("template-type-code");
    	assertEquals("test", eventStatusBasedOnLangCode.getEventStatusBasedOnLangcode(EventStatus.SUCCESS, "eng"));
    	Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn(null).thenReturn("template-type-code");
    	assertEquals("test", eventStatusBasedOnLangCode.getEventStatusBasedOnLangcode(EventStatus.SUCCESS, "eng"));
    }

    @Test
    public void testGetAttributeBasedOnLangcodeIf() {
    	Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn(null).thenReturn("template-type-code");
    	assertEquals("test", attributeBasedOnLangCode.getAttributeBasedOnLangcode("fullName", "eng"));
    }
}
