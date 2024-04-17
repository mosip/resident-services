package io.mosip.resident.service.impl;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.NotificationTemplateCode;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.TemplateType;
import io.mosip.resident.dto.NotificationRequestDto;
import io.mosip.resident.dto.NotificationRequestDtoV2;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.IdentityService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.util.Utility;
import io.mosip.resident.validator.RequestValidator;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class NotificationServiceTest {

	@MockBean
	private ProxyIdRepoService proxyIdRepoService;

	@InjectMocks
	private NotificationService notificationService;

	@Mock
	private Utility utility;

	@Mock
	private Utilities utilities;

	@Mock
	private Environment env;

	@Mock
	private ResidentServiceRestClient restClient;

	@Mock
	private TemplateManager templateManager;

	@Mock
	private AuditUtil audit;

	@Mock
	private TemplateUtil templateUtil;

	@Mock
	private RequestValidator requestValidator;

	@Mock
	private IdentityService identityService;
	private Map<String, Object> mailingAttributes;
	private NotificationRequestDto reqDto;
	private NotificationRequestDtoV2 notificationRequestDtoV2;
	private ResponseWrapper<NotificationResponseDTO> notificationResponseWrapper;

	private static final String SMS_EMAIL_SUCCESS = "Notification has been sent to the provided contact detail(s)";
	private static final String SMS_SUCCESS = "Notification has been sent to the provided contact phone number";
	private static final String EMAIL_SUCCESS = "Notification has been sent to the provided email ";

	@Before
	public void setUp() throws Exception {
		Mockito.when(utility.getDataCapturedLanguages(Mockito.nullable(Map.class), Mockito.nullable(Map.class)))
				.thenReturn(Set.of("eng", "ara"));
		mailingAttributes = new HashMap<String, Object>();
		mailingAttributes.put("fullName_eng", "Test");
		mailingAttributes.put("fullName_ara", "Test");
		mailingAttributes.put("phoneNumber", "9876543210");
		mailingAttributes.put("email", "test@test.com");
		Mockito.when(utility.getMailingAttributes(Mockito.any(), Mockito.any(), Mockito.nullable(Map.class),
				Mockito.nullable(Map.class))).thenReturn(mailingAttributes);
		ReflectionTestUtils.setField(notificationService, "notificationType", "SMS|EMAIL");
		ReflectionTestUtils.setField(notificationService, "notificationEmails", "test@test.com|test1@test1.com");
		Mockito.when(utilities.getPhoneAttribute()).thenReturn("phoneNumber");
		Mockito.when(utilities.getEmailAttribute()).thenReturn("email");
		Mockito.when(env.getProperty(ApiName.EMAILNOTIFIER.name())).thenReturn("https://int.mosip.io/template/email");
		Mockito.when(requestValidator.emailValidator(Mockito.anyString())).thenReturn(true);
		Mockito.when(requestValidator.phoneValidator(Mockito.anyString())).thenReturn(true);
		Map<String, Object> additionalAttributes = new HashMap<>();
		additionalAttributes.put(IdType.RID.name(), "10008200070004420191203104356");
		Mockito.lenient().when(utility.getMappingJsonObject()).thenReturn(Mockito.mock(JSONObject.class));
		reqDto = new NotificationRequestDto();
		reqDto.setId("3527812406");
		reqDto.setTemplateTypeCode(NotificationTemplateCode.RS_UIN_RPR_SUCCESS);
		reqDto.setAdditionalAttributes(additionalAttributes);
		notificationRequestDtoV2 = new NotificationRequestDtoV2();
		notificationRequestDtoV2.setId("3527812406");
		notificationRequestDtoV2.setEventId("1122334455667788");
		notificationRequestDtoV2.setRequestType(RequestType.GENERATE_VID);
		notificationRequestDtoV2.setTemplateType(TemplateType.SUCCESS);
		Mockito.when(templateUtil.getSmsTemplateTypeCode(Mockito.any(), Mockito.any()))
				.thenReturn("sms-template-type-code");
		Mockito.when(templateUtil.getEmailSubjectTemplateTypeCode(Mockito.any(), Mockito.any()))
				.thenReturn("email-subject-template-type-code");
		Mockito.when(templateUtil.getEmailContentTemplateTypeCode(Mockito.any(), Mockito.any()))
				.thenReturn("email-content-template-type-code");

		Mockito.when(templateUtil.getTemplateValueFromTemplateTypeCodeAndLangCode(Mockito.anyString(), Mockito.anyString())).
				thenReturn("Hi $name_eng,Your request for \"Reprint Of UIN\" has been successfully placed. Your RID (Req Number) is $RID.<br>");

		String primaryTemplatetext = "Hi Test,Your request for \"Reprint Of UIN\" has been successfully placed. Your RID (Req Number) is 10008200070004420191203104356.<br>";
		InputStream primaryIs = new ByteArrayInputStream(primaryTemplatetext.getBytes(StandardCharsets.UTF_8));
		Mockito.when(templateManager.merge(Mockito.any(), Mockito.any())).thenReturn(primaryIs);
		notificationResponseWrapper = new ResponseWrapper<>();
		NotificationResponseDTO notificationResp = new NotificationResponseDTO();
		notificationResp.setMessage("Notification has been sent to provided contact details");
		notificationResp.setStatus("success");
		notificationResponseWrapper.setResponse(notificationResp);
		Mockito.when(restClient.postApi(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
				.thenReturn(notificationResponseWrapper);
		Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());
	}

	@Test
	public void sendNotificationTest() throws ResidentServiceCheckedException {
		Mockito.when(utility.getPreferredLanguage(Mockito.nullable(Map.class))).thenReturn(Set.of("eng"));
		NotificationResponseDTO response = notificationService.sendNotification(reqDto, null);
		assertEquals(SMS_EMAIL_SUCCESS, response.getMessage());
	}

	@Test
	public void smsFailedAndEmailSuccessTestPhoneNull() throws ResidentServiceCheckedException {
		mailingAttributes.put("phoneNumber", null);
		NotificationResponseDTO response = notificationService.sendNotification(reqDto, null);
		assertEquals(EMAIL_SUCCESS, response.getMessage());
	}

	@Test
	public void smsFailedAndEmailSuccessTest() throws ResidentServiceCheckedException {
		Mockito.when(requestValidator.phoneValidator(Mockito.anyString())).thenReturn(false);
		NotificationResponseDTO response = notificationService.sendNotification(reqDto, null);
		assertEquals(EMAIL_SUCCESS, response.getMessage());
	}

	@Test
	public void emailFailedAndSMSSuccessTest() throws ResidentServiceCheckedException {
		Mockito.when(requestValidator.emailValidator(Mockito.anyString())).thenReturn(false);
		NotificationResponseDTO response = notificationService.sendNotification(reqDto, null);
		assertEquals(SMS_SUCCESS, response.getMessage());
	}

	@Test
	public void testEmailSuccessV2WithChannelsNull() throws ResidentServiceCheckedException {
		ReflectionTestUtils.setField(notificationService, "notificationType", "EMAIL");
		NotificationResponseDTO response = notificationService.sendNotification(notificationRequestDtoV2, null,
				"ka@gm.com", "8897878787", null);
		assertEquals(EMAIL_SUCCESS, response.getMessage());
	}

	@Test
	public void testSmsSuccessV2WithChannelsNull() throws ResidentServiceCheckedException {
		ReflectionTestUtils.setField(notificationService, "notificationType", "SMS");
		NotificationResponseDTO response = notificationService.sendNotification(notificationRequestDtoV2, null,
				"ka@gm.com", "8897878787", null);
		assertEquals(SMS_SUCCESS, response.getMessage());
	}

	@Test
	public void testNotificationFailure() throws Exception {
		NotificationResponseDTO notificationResp = new NotificationResponseDTO();
		notificationResp.setMessage("Notification failure");
		notificationResp.setStatus("failed");
		notificationResponseWrapper.setResponse(notificationResp);
		Mockito.when(restClient.postApi(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
				.thenReturn(notificationResponseWrapper);
		notificationService.sendNotification(reqDto, null);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testTemplateMergeWithIOException() throws IOException, ResidentServiceCheckedException {
		Mockito.when(templateManager.merge(Mockito.any(), Mockito.any())).thenThrow(new IOException());
		notificationService.sendNotification(reqDto, null);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void sendSMSClientException() throws ApisResourceAccessException, ResidentServiceCheckedException {
		HttpClientErrorException clientExp = new HttpClientErrorException(HttpStatus.BAD_GATEWAY);
		ApisResourceAccessException apiResourceAccessExp = new ApisResourceAccessException("BadGateway", clientExp);
		Mockito.when(restClient.postApi(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
				.thenThrow(apiResourceAccessExp);
		notificationService.sendNotification(reqDto, null);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void sendSMSServerException() throws ApisResourceAccessException, ResidentServiceCheckedException {
		HttpServerErrorException serverExp = new HttpServerErrorException(HttpStatus.BAD_GATEWAY);
		ApisResourceAccessException apiResourceAccessExp = new ApisResourceAccessException("BadGateway", serverExp);
		Mockito.when(restClient.postApi(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
				.thenThrow(apiResourceAccessExp);
		notificationService.sendNotification(reqDto, null);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void sendSMSUnknownException() throws ApisResourceAccessException, ResidentServiceCheckedException {
		RuntimeException runTimeExp = new RuntimeException();
		ApisResourceAccessException apiResourceAccessExp = new ApisResourceAccessException("runtime exp", runTimeExp);
		Mockito.when(restClient.postApi(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
				.thenThrow(apiResourceAccessExp);
		notificationService.sendNotification(reqDto, null);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testsendSMSNotificationWithIOException()
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<Object> notificationResponseWrapper = new ResponseWrapper<>();
		notificationResponseWrapper.setResponse("throw io exception");
		Mockito.when(restClient.postApi(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
				.thenReturn(notificationResponseWrapper);
		notificationService.sendNotification(notificationRequestDtoV2, List.of("PHONE"), "ka@gm.com", "8897878787",
				null);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void sendEmailClientException() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ReflectionTestUtils.setField(notificationService, "notificationType", "EMAIL");
		HttpClientErrorException clientExp = new HttpClientErrorException(HttpStatus.BAD_GATEWAY);
		ApisResourceAccessException apiResourceAccessExp = new ApisResourceAccessException("BadGateway", clientExp);
		Mockito.when(restClient.postApi(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
				.thenThrow(apiResourceAccessExp);
		notificationService.sendNotification(reqDto, null);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void sendEmailServerException() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ReflectionTestUtils.setField(notificationService, "notificationType", "EMAIL");
		HttpServerErrorException serverExp = new HttpServerErrorException(HttpStatus.BAD_GATEWAY);
		ApisResourceAccessException apiResourceAccessExp = new ApisResourceAccessException("BadGateway", serverExp);
		Mockito.when(restClient.postApi(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
				.thenThrow(apiResourceAccessExp);
		notificationService.sendNotification(reqDto, null);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void sendEmailUnknownException() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ReflectionTestUtils.setField(notificationService, "notificationType", "EMAIL");
		RuntimeException runTimeExp = new RuntimeException();
		ApisResourceAccessException apiResourceAccessExp = new ApisResourceAccessException("runtime exp", runTimeExp);
		Mockito.when(restClient.postApi(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
				.thenThrow(apiResourceAccessExp);
		notificationService.sendNotification(reqDto, null);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testsendEmailNotificationWithIOException()
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<Object> notificationResponseWrapper = new ResponseWrapper<>();
		notificationResponseWrapper.setResponse("throw io exception");
		Mockito.when(restClient.postApi(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
				.thenReturn(notificationResponseWrapper);
		notificationService.sendNotification(notificationRequestDtoV2, List.of("EMAIL"), "ka@gm.com", "8897878787",
				null);
	}

	@Test
	public void testSmsAndEmailSuccessV2() throws ResidentServiceCheckedException {
		Mockito.when(utility.getDefaultTemplateLanguages()).thenReturn(List.of("eng"));
		NotificationResponseDTO response = notificationService.sendNotification(notificationRequestDtoV2,
				List.of("PHONE", "EMAIL"), "ka@gm.com", "8897878787", null);
		assertEquals(SMS_EMAIL_SUCCESS, response.getMessage());
	}

	@Test
	public void testSmsSuccessV2() throws ResidentServiceCheckedException {
		notificationRequestDtoV2.setOtp("111111");
		NotificationResponseDTO response = notificationService.sendNotification(notificationRequestDtoV2,
				List.of("PHONE"), null, null, null);
		assertEquals(SMS_SUCCESS, response.getMessage());
	}

	@Test
	public void testEmailSuccessV2() throws ResidentServiceCheckedException {
		notificationRequestDtoV2.setOtp("111111");
		NotificationResponseDTO response = notificationService.sendNotification(notificationRequestDtoV2,
				List.of("EMAIL"), "ka@gm.com", null, null);
		assertEquals(EMAIL_SUCCESS, response.getMessage());
	}

	@Test(expected = ResidentServiceException.class)
	public void testSmsErrorResponse() throws ResidentServiceCheckedException {
		ServiceError error = new ServiceError("res-ser", "error response from API");
		notificationResponseWrapper.setErrors(List.of(error));
		notificationService.sendNotification(notificationRequestDtoV2, List.of("PHONE"), null, "8897878787", null);
	}

	@Test(expected = ResidentServiceException.class)
	public void testEmailErrorResponse() throws ResidentServiceCheckedException {
		ServiceError error = new ServiceError("res-ser", "error response from API");
		notificationResponseWrapper.setErrors(List.of(error));
		notificationService.sendNotification(notificationRequestDtoV2, List.of("EMAIL"), "ka@gm.com", null, null);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testSendNotificationWithReflectiveOperationException()
			throws ResidentServiceCheckedException, ReflectiveOperationException {
		Mockito.when(utility.getDataCapturedLanguages(Mockito.nullable(Map.class), Mockito.nullable(Map.class)))
				.thenThrow(ReflectiveOperationException.class);
		notificationService.sendNotification(reqDto, null);
	}
}