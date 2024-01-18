package io.mosip.resident.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.EventStatusSuccess;
import io.mosip.resident.dto.CredentialCancelRequestResponseDto;
import io.mosip.resident.dto.CredentialReqestDto;
import io.mosip.resident.dto.CredentialRequestStatusDto;
import io.mosip.resident.dto.CredentialRequestStatusResponseDto;
import io.mosip.resident.dto.CredentialTypeResponse;
import io.mosip.resident.dto.CryptomanagerRequestDto;
import io.mosip.resident.dto.CryptomanagerResponseDto;
import io.mosip.resident.dto.EncryptResponseDto;
import io.mosip.resident.dto.Issuer;
import io.mosip.resident.dto.PartnerCredentialTypePolicyDto;
import io.mosip.resident.dto.PartnerResponseDto;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentCredentialRequestDto;
import io.mosip.resident.dto.ResidentCredentialResponseDto;
import io.mosip.resident.dto.ResidentCredentialResponseDtoV2;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.SharableAttributesDTO;
import io.mosip.resident.dto.Type;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentCredentialServiceException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ResidentCredentialService;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utility;
import reactor.util.function.Tuple2;

/**
 * This class is used to create service class test for
 * ResidentCredentialServiceImpl.
 * 
 * @Author Ritik Jain
 */
@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class ResidentCredentialServiceTest {

	@Mock
	private Environment env;

	@Mock
	private ResidentServiceRestClient residentServiceRestClient;

	@Mock
	private NotificationService notificationService;

	@Mock
	private IdAuthService idAuthService;

	@Mock
	private ObjectMapper mapper;

	@Mock
	private Utility utility;

	@Mock
	private ResidentTransactionRepository residentTransactionRepository;

	@Mock
	private IdentityServiceImpl identityServiceImpl;

	@Mock
	private ProxyPartnerManagementServiceImpl proxyPartnerManagementServiceImpl;

	private ResidentCredentialRequestDto residentCredentialRequestDto;

	private ResidentTransactionEntity residentTransactionEntity;

	@InjectMocks
	private ResidentCredentialService residentCredentialService = new ResidentCredentialServiceImpl();

	@Before
	public void setup() throws IOException, ResidentServiceCheckedException, ApisResourceAccessException {
		ReflectionTestUtils.setField(residentCredentialService, "max", 982608);
		ReflectionTestUtils.setField(residentCredentialService, "min", 120078);
		ReflectionTestUtils.setField(residentCredentialService, "ridSuffix", "-pdf");
		List<String> attributeList = new ArrayList<>();
		attributeList.add("name");
		attributeList.add("gender");
		residentCredentialRequestDto = new ResidentCredentialRequestDto();
		residentCredentialRequestDto.setOtp("123");
		residentCredentialRequestDto.setTransactionID("12345");
		residentCredentialRequestDto.setIndividualId("1234567890");
		residentCredentialRequestDto.setIssuer("mpartner-default-print");
		residentCredentialRequestDto.setCredentialType("euin");
		residentCredentialRequestDto.setEncrypt(true);
		residentCredentialRequestDto.setEncryptionKey("abc123");
		residentCredentialRequestDto.setSharableAttributes(attributeList);
		residentCredentialRequestDto.setConsent("Accepted");
		residentTransactionEntity = new ResidentTransactionEntity();
		residentTransactionEntity.setEventId("e65c86f5-8929-4547-a156-9b349c29ab8b");
		when(utility.createEntity(Mockito.any())).thenReturn(residentTransactionEntity);
		when(identityServiceImpl.getResidentIndvidualIdFromSession()).thenReturn("1234567890");
		when(utility.createEventId()).thenReturn("1111111111111111");
	}

	@Test
	public void generateCredentialTest() throws OtpValidationFailedException, IOException, ApisResourceAccessException,
			ResidentServiceCheckedException {
		ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();
		residentCredentialResponseDto.setRequestId("10001100010006920211220064226");
		ResponseWrapper<ResidentCredentialResponseDto> response = new ResponseWrapper<>();
		response.setResponsetime(DateUtils.getCurrentDateTimeString());
		response.setResponse(residentCredentialResponseDto);

		PartnerResponseDto partnerResponseDto = new PartnerResponseDto();
		partnerResponseDto.setOrganizationName("MOSIP");
		ResponseWrapper<PartnerResponseDto> partnerResponseDtoResponseWrapper = new ResponseWrapper<>();
		partnerResponseDtoResponseWrapper.setResponse(partnerResponseDto);

		when(idAuthService.validateOtp(residentCredentialRequestDto.getTransactionID(),
				residentCredentialRequestDto.getIndividualId(), residentCredentialRequestDto.getOtp()))
						.thenReturn(Boolean.TRUE);

		RequestWrapper<CredentialReqestDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.credential.request.service.id");
		requestDto.setRequest(new CredentialReqestDto());
		requestDto.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestDto.setVersion("1.0");

		String partnerUrl = env.getProperty(ApiName.PARTNER_API_URL.name()) + "/"
				+ residentCredentialRequestDto.getIssuer();
		URI partnerUri = URI.create(partnerUrl);
		when(residentServiceRestClient.getApi(partnerUri, ResponseWrapper.class))
				.thenReturn(partnerResponseDtoResponseWrapper);
		when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(response);

		ResidentCredentialResponseDto credentialResponseDto = residentCredentialService
				.reqCredential(residentCredentialRequestDto);
		assertEquals("10001100010006920211220064226", credentialResponseDto.getRequestId());
	}

	@Test(expected = ResidentServiceException.class)
	public void testGenerateCredentialWithIndividualIdNull() throws ResidentServiceCheckedException, OtpValidationFailedException {
		residentCredentialRequestDto.setIndividualId(null);

		residentCredentialService.reqCredential(residentCredentialRequestDto);
	}

	@Test(expected = ResidentServiceException.class)
	public void testGenerateCredentialWithOTPFailure()
			throws OtpValidationFailedException, ResidentServiceCheckedException {
		when(idAuthService.validateOtp(residentCredentialRequestDto.getTransactionID(),
				residentCredentialRequestDto.getIndividualId(), residentCredentialRequestDto.getOtp()))
						.thenReturn(Boolean.FALSE);

		residentCredentialService.reqCredential(residentCredentialRequestDto);
	}

	@Test(expected = Exception.class)
	public void testGenerateCredentialWithOtpValidationFailedException()
			throws OtpValidationFailedException, ResidentServiceCheckedException {
		when(idAuthService.validateOtp(residentCredentialRequestDto.getTransactionID(),
				residentCredentialRequestDto.getIndividualId(), residentCredentialRequestDto.getOtp()))
						.thenThrow(new OtpValidationFailedException());

		residentCredentialService.reqCredential(residentCredentialRequestDto);
	}

	@Test(expected = Exception.class)
	public void testGenerateCredentialWithOtpValidationNotificationFailed()
			throws OtpValidationFailedException, ResidentServiceCheckedException {
		when(idAuthService.validateOtp(residentCredentialRequestDto.getTransactionID(),
				residentCredentialRequestDto.getIndividualId(), residentCredentialRequestDto.getOtp()))
						.thenThrow(new OtpValidationFailedException());
		when(notificationService.sendNotification(any(), Mockito.nullable(Map.class))).thenThrow(new ResidentServiceCheckedException());

		residentCredentialService.reqCredential(residentCredentialRequestDto);
	}

	@Test(expected = ResidentCredentialServiceException.class)
	public void testReqCredentialWithApisResourceAccessException() throws Exception {
		residentCredentialRequestDto.setEncryptionKey(null);
		ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();
		residentCredentialResponseDto.setRequestId("10001100010006920211220064226");
		ResponseWrapper<ResidentCredentialResponseDto> response = new ResponseWrapper<>();
		response.setResponsetime(DateUtils.getCurrentDateTimeString());
		response.setResponse(residentCredentialResponseDto);

		PartnerResponseDto partnerResponseDto = new PartnerResponseDto();
		partnerResponseDto.setOrganizationName("MOSIP");
		ResponseWrapper<PartnerResponseDto> partnerResponseDtoResponseWrapper = new ResponseWrapper<>();
		partnerResponseDtoResponseWrapper.setResponse(partnerResponseDto);

		when(idAuthService.validateOtp(residentCredentialRequestDto.getTransactionID(),
				residentCredentialRequestDto.getIndividualId(), residentCredentialRequestDto.getOtp()))
						.thenReturn(Boolean.TRUE);

		RequestWrapper<CredentialReqestDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.credential.request.service.id");
		requestDto.setRequest(new CredentialReqestDto());
		requestDto.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestDto.setVersion("1.0");

		String partnerUrl = env.getProperty(ApiName.PARTNER_API_URL.name()) + "/"
				+ residentCredentialRequestDto.getIssuer();
		URI partnerUri = URI.create(partnerUrl);
		when(residentServiceRestClient.getApi(partnerUri, ResponseWrapper.class))
				.thenReturn(partnerResponseDtoResponseWrapper);
		when(residentServiceRestClient.postApi(any(), any(), any(), any()))
				.thenThrow(ApisResourceAccessException.class);
		residentCredentialService.reqCredential(residentCredentialRequestDto);
	}

	@Test(expected = ResidentCredentialServiceException.class)
	public void testGenerateCredentialWithIOException()
			throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException {
		residentCredentialRequestDto.setEncryptionKey("");
		ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();
		residentCredentialResponseDto.setRequestId("10001100010006920211220064226");
		ResponseWrapper<ResidentCredentialResponseDto> response = new ResponseWrapper<>();
		response.setResponsetime(DateUtils.getCurrentDateTimeString());
		response.setResponse(residentCredentialResponseDto);

		PartnerResponseDto partnerResponseDto = new PartnerResponseDto();
		partnerResponseDto.setOrganizationName("MOSIP");
		ResponseWrapper<PartnerResponseDto> partnerResponseDtoResponseWrapper = new ResponseWrapper<>();
		partnerResponseDtoResponseWrapper.setResponse(partnerResponseDto);

		when(idAuthService.validateOtp(residentCredentialRequestDto.getTransactionID(),
				residentCredentialRequestDto.getIndividualId(), residentCredentialRequestDto.getOtp()))
						.thenReturn(Boolean.TRUE);

		RequestWrapper<CredentialReqestDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.credential.request.service.id");
		requestDto.setRequest(new CredentialReqestDto());
		requestDto.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestDto.setVersion("1.0");

		String partnerUrl = env.getProperty(ApiName.PARTNER_API_URL.name()) + "/"
				+ residentCredentialRequestDto.getIssuer();
		URI partnerUri = URI.create(partnerUrl);
		when(residentServiceRestClient.getApi(partnerUri, ResponseWrapper.class))
				.thenReturn(partnerResponseDtoResponseWrapper);
		when(residentServiceRestClient.postApi(any(), any(), any(), any()))
				.thenReturn(partnerResponseDtoResponseWrapper);

		residentCredentialService.reqCredential(residentCredentialRequestDto);
	}

	@Test
	public void testShareCredentialPurpose()
			throws IOException, ApisResourceAccessException, ResidentServiceCheckedException {
		ReflectionTestUtils.setField(residentCredentialService, "credentialType", "euin");
		ReflectionTestUtils.setField(residentCredentialService, "encryptionKey", "abc1234");
		IdentityServiceTest.getAuthUserDetailsFromAuthentication();
		SharableAttributesDTO sharableAttribute1 = new SharableAttributesDTO();
		sharableAttribute1.setAttributeName("name");
		sharableAttribute1.setFormat("firstName,secondName");
		SharableAttributesDTO sharableAttribute2 = new SharableAttributesDTO();
		sharableAttribute2.setAttributeName("gender");
		SharableAttributesDTO sharableAttribute3 = new SharableAttributesDTO();
		sharableAttribute3.setAttributeName("phone");
		sharableAttribute3.setFormat("");
		ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();
		residentCredentialResponseDto.setRequestId("10001100010006920211220064226");
		ResponseWrapper<ResidentCredentialResponseDto> response = new ResponseWrapper<>();
		response.setResponsetime(DateUtils.getCurrentDateTimeString());
		response.setResponse(residentCredentialResponseDto);

		RequestWrapper<CredentialReqestDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.credential.request.service.id");
		requestDto.setRequest(new CredentialReqestDto());
		requestDto.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestDto.setVersion("1.0");

		when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(response);

		Tuple2<ResidentCredentialResponseDtoV2, String> credentialResponseDto = residentCredentialService
				.shareCredential(residentCredentialRequestDto, "Banking",
						List.of(sharableAttribute1, sharableAttribute2, sharableAttribute3));
		assertNotNull(credentialResponseDto.getT1().getStatus());
	}

	@Test(expected = ResidentServiceException.class)
	public void testShareCredentialConsentNull()
			throws IOException, ApisResourceAccessException, ResidentServiceCheckedException {
		residentCredentialRequestDto.setConsent(null);
		residentCredentialService.shareCredential(residentCredentialRequestDto, "Banking", List.of());
	}

	@Test(expected = ResidentCredentialServiceException.class)
	public void testShareCredentialWithApisResourceAccessException() throws Exception {
		ReflectionTestUtils.setField(residentCredentialService, "credentialType", "euin");
		ReflectionTestUtils.setField(residentCredentialService, "encryptionKey", "abc1234");
		IdentityServiceTest.getAuthUserDetailsFromAuthentication();
		residentCredentialRequestDto.setSharableAttributes(null);
		ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();
		residentCredentialResponseDto.setRequestId("10001100010006920211220064226");
		ResponseWrapper<ResidentCredentialResponseDto> response = new ResponseWrapper<>();
		response.setResponsetime(DateUtils.getCurrentDateTimeString());
		response.setResponse(residentCredentialResponseDto);

		RequestWrapper<CredentialReqestDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.credential.request.service.id");
		requestDto.setRequest(new CredentialReqestDto());
		requestDto.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestDto.setVersion("1.0");

		when(residentServiceRestClient.postApi(any(), any(), any(), any()))
				.thenThrow(ApisResourceAccessException.class);
		residentCredentialService.shareCredential(residentCredentialRequestDto, null, null);
	}

	@Test(expected = ResidentCredentialServiceException.class)
	public void testShareCredentialWithIOException()
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		ReflectionTestUtils.setField(residentCredentialService, "credentialType", "euin");
		ReflectionTestUtils.setField(residentCredentialService, "encryptionKey", "abc1234");
		IdentityServiceTest.getAuthUserDetailsFromAuthentication();
		residentCredentialRequestDto.setSharableAttributes(List.of());
		PartnerResponseDto partnerResponseDto = new PartnerResponseDto();
		ResponseWrapper<PartnerResponseDto> response = new ResponseWrapper<>();
		response.setResponse(partnerResponseDto);

		RequestWrapper<CredentialReqestDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.credential.request.service.id");
		requestDto.setRequest(new CredentialReqestDto());
		requestDto.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestDto.setVersion("1.0");

		when(residentServiceRestClient.postApi(any(), any(), any(), any()))
				.thenReturn(response);
		residentCredentialService.shareCredential(residentCredentialRequestDto, null, List.of());
	}

	@Test
	public void testGetCredentialTypes() throws ApisResourceAccessException {
		Issuer issuer = new Issuer();
		issuer.setCode("paytm");
		issuer.setName("PayTM");
		List<Issuer> issuerList = new ArrayList<Issuer>();
		issuerList.add(issuer);
		Type type = new Type();
		type.setDescription("Secure Digital QR Code");
		type.setId("idtype1");
		type.setName("Secure Digital QR Code");
		type.setIssuers(issuerList);
		List<Type> types = new ArrayList<>();
		types.add(type);
		CredentialTypeResponse credentialTypeResponse = new CredentialTypeResponse();
		credentialTypeResponse.setCredentialTypes(types);

		when(env.getProperty(ApiName.CREDENTIAL_TYPES_URL.name()))
				.thenReturn("https://mosip.net/v1/credentialservice/types");
		when(residentServiceRestClient.getApi((URI) any(), any())).thenReturn(credentialTypeResponse);
		CredentialTypeResponse credentialTypes = residentCredentialService.getCredentialTypes();
		assertEquals(credentialTypes.getCredentialTypes().size(), 1);
		assertEquals(credentialTypes.getCredentialTypes().get(0).getDescription(), "Secure Digital QR Code");
		assertEquals(credentialTypes.getCredentialTypes().get(0).getIssuers().size(), 1);
		assertEquals(credentialTypes.getCredentialTypes().get(0).getIssuers().get(0).getName(), "PayTM");
	}

	@Test(expected = ResidentCredentialServiceException.class)
	public void testGetCredentialTypesWithAPIResourceException() throws ApisResourceAccessException {
		when(env.getProperty(ApiName.CREDENTIAL_TYPES_URL.name()))
				.thenReturn("https://mosip.net/v1/credentialservice/types");
		when(residentServiceRestClient.getApi((URI) any(), any())).thenThrow(ApisResourceAccessException.class);
		residentCredentialService.getCredentialTypes();
	}

	@Test
	public void testGetPolicyByCredentialType() throws Exception {

		PartnerCredentialTypePolicyDto credentialTypePolicyDto = new PartnerCredentialTypePolicyDto();
		credentialTypePolicyDto.setPartnerId("1");
		credentialTypePolicyDto.setCredentialType("credentialType");
		credentialTypePolicyDto.setPolicyId("policyId");
		credentialTypePolicyDto.setPolicyName("policyName");
		credentialTypePolicyDto.setPolicyDesc("policyDesc");
		credentialTypePolicyDto.setPolicyType("policyType");
		credentialTypePolicyDto.setPublishDate("publishDate");
		credentialTypePolicyDto.setValidTill("validTill");
		credentialTypePolicyDto.setStatus("status");
		credentialTypePolicyDto.setVersion("version1");
		credentialTypePolicyDto.setSchema("schema");
		credentialTypePolicyDto.setIs_Active(true);
		credentialTypePolicyDto.setCr_by("crBy");
		credentialTypePolicyDto.setCr_dtimes("crDtimes");
		credentialTypePolicyDto.setUpd_dtimes("uddDtimes");
		credentialTypePolicyDto.setPolicies(new JSONObject());

		ResponseWrapper<PartnerCredentialTypePolicyDto> responseWrapper = new ResponseWrapper<PartnerCredentialTypePolicyDto>();
		responseWrapper.setId("1");
		responseWrapper.setVersion("T version");
		responseWrapper.setResponsetime("responseTime");
		responseWrapper.setResponse(credentialTypePolicyDto);
		// responseWrapper.setErrors(null);

		when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(responseWrapper);

		ResponseWrapper<PartnerCredentialTypePolicyDto> response = residentCredentialService
				.getPolicyByCredentialType("1", "credentialType");
		assertEquals(response.getResponse(), credentialTypePolicyDto);
		assertEquals(response.getId(), "1");
		assertEquals(response.getVersion(), "T version");
		assertEquals(response, responseWrapper);

	}

	@Test(expected = ResidentCredentialServiceException.class)
	public void testGetPolicyByCredentialTypeWithAPIResourceException() throws ApisResourceAccessException {
		when(residentServiceRestClient.getApi((ApiName) any(), any(), any()))
				.thenThrow(ApisResourceAccessException.class);
		residentCredentialService.getPolicyByCredentialType("2", "credential-type");
	}

	@Test
	public void testGetStatus() throws ApisResourceAccessException, IOException, ResidentServiceCheckedException {
		CredentialRequestStatusDto credentialRequestStatusDto = new CredentialRequestStatusDto();
		credentialRequestStatusDto.setId("id-1");
		credentialRequestStatusDto.setRequestId("effc56cd-cf3b-4042-ad48-7277cf90f763");
		credentialRequestStatusDto.setStatusCode("code-101");
		credentialRequestStatusDto.setUrl("https://url");
		ResponseWrapper<CredentialRequestStatusDto> responseWrapper = new ResponseWrapper<CredentialRequestStatusDto>();
		responseWrapper.setId("id-1");
		responseWrapper.setVersion("T version");
		responseWrapper.setResponsetime("responseTime");
		responseWrapper.setResponse(credentialRequestStatusDto);

		when(env.getProperty(any())).thenReturn("https://credentialUrl");
		when(residentServiceRestClient.getApi((URI) any(), any())).thenReturn(responseWrapper);

		CredentialRequestStatusResponseDto response = residentCredentialService
				.getStatus("effc56cd-cf3b-4042-ad48-7277cf90f763");
		assertEquals(response.getId(), responseWrapper.getId());
		assertEquals(response.getStatusCode(), responseWrapper.getResponse().getStatusCode());
		assertEquals(response.getRequestId(), "effc56cd-cf3b-4042-ad48-7277cf90f763");
	}

	@Test(expected = ResidentCredentialServiceException.class)
	public void testGetStatusWithApisResourceAccessException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(env.getProperty(any())).thenReturn("https://credentialUrl");
		when(residentServiceRestClient.getApi((URI) any(), any())).thenThrow(ApisResourceAccessException.class);
		residentCredentialService.getStatus("effc56cd-cf3b-4042-ad48-7277cf90f763");
	}

	@Test(expected = ResidentCredentialServiceException.class)
	public void testGetStatusWithIOException()
			throws ApisResourceAccessException, IOException, ResidentServiceCheckedException {
		ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setId("id-1");
		responseWrapper.setVersion("T version");
		responseWrapper.setResponsetime("responseTime");
		responseWrapper.setResponse("throw io exception");

		when(env.getProperty(any())).thenReturn("https://credentialUrl");
		when(residentServiceRestClient.getApi((URI) any(), any())).thenReturn(responseWrapper);

		residentCredentialService.getStatus("effc56cd-cf3b-4042-ad48-7277cf90f763");
	}

	@Test(expected = ResidentCredentialServiceException.class)
	public void testGetStatusWithIllegalArgumentException()
			throws IllegalArgumentException, ApisResourceAccessException, ResidentServiceCheckedException {
		when(env.getProperty(any())).thenReturn("https://credentialUrl");
		when(residentServiceRestClient.getApi((URI) any(), any())).thenThrow(IllegalArgumentException.class);
		residentCredentialService.getStatus("effc56cd-cf3b-4042-ad48-7277cf90f763");
	}

	@Test(expected = ResidentCredentialServiceException.class)
	public void testGetStatusWithResidentServiceCheckedException()
			throws ApisResourceAccessException, IOException, ResidentServiceCheckedException {
		CredentialRequestStatusDto credentialRequestStatusDto = new CredentialRequestStatusDto();
		credentialRequestStatusDto.setId("id-1");
		credentialRequestStatusDto.setRequestId("effc56cd-cf3b-4042-ad48-7277cf90f763");
		credentialRequestStatusDto.setStatusCode("code-101");
		credentialRequestStatusDto.setUrl("https://url");
		ResponseWrapper<CredentialRequestStatusDto> responseWrapper = new ResponseWrapper<CredentialRequestStatusDto>();
		responseWrapper.setId("id-1");
		responseWrapper.setVersion("T version");
		responseWrapper.setResponsetime("responseTime");
		responseWrapper.setResponse(credentialRequestStatusDto);

		when(env.getProperty(any())).thenReturn("https://credentialUrl");
		when(residentServiceRestClient.getApi((URI) any(), any())).thenReturn(responseWrapper);
		when(notificationService.sendNotification(any(), Mockito.nullable(Map.class))).thenThrow(new ResidentServiceCheckedException());

		residentCredentialService.getStatus("effc56cd-cf3b-4042-ad48-7277cf90f763");
	}

	@Test
	public void testGetCard() throws Exception {
		ReflectionTestUtils.setField(residentCredentialService, "applicationId", "resident");
		ReflectionTestUtils.setField(residentCredentialService, "partnerReferenceId", "mosip_partner");

		ResponseWrapper<CredentialRequestStatusDto> responseWrapper = new ResponseWrapper<CredentialRequestStatusDto>();
		CredentialRequestStatusDto credentialRequestStatusDto = new CredentialRequestStatusDto();
		credentialRequestStatusDto.setId("id-1");
		credentialRequestStatusDto.setRequestId("effc56cd-cf3b-4042-ad48-7277cf90f763");
		credentialRequestStatusDto.setStatusCode(EventStatusSuccess.STORED.name());
		credentialRequestStatusDto.setUrl("https://url");

		responseWrapper.setId("id-1");
		responseWrapper.setVersion("T version");
		responseWrapper.setResponsetime("responseTime");
		responseWrapper.setResponse(credentialRequestStatusDto);

		when(env.getProperty(any())).thenReturn("https://credentialUrl");
		URI credentailStatusUri = URI.create("https://credentialUrleffc56cd-cf3b-4042-ad48-7277cf90f763");
		when(residentServiceRestClient.getApi(credentailStatusUri, ResponseWrapper.class)).thenReturn(responseWrapper);

		URI dataShareUri = URI.create(credentialRequestStatusDto.getUrl());
		when(residentServiceRestClient.getApi(dataShareUri, String.class)).thenReturn("str");
		RequestWrapper<CryptomanagerRequestDto> request = new RequestWrapper<>();
		CryptomanagerRequestDto cryptomanagerRequestDto = new CryptomanagerRequestDto();
		cryptomanagerRequestDto.setApplicationId("resident");
		cryptomanagerRequestDto.setData("str");
		cryptomanagerRequestDto.setReferenceId("mosip_partner");
		cryptomanagerRequestDto.setPrependThumbprint(true);
		cryptomanagerRequestDto.setTimeStamp(LocalDateTime.now());
		request.setRequesttime("responseTime");
		request.setRequest(cryptomanagerRequestDto);

		String str = CryptoUtil.encodeToURLSafeBase64("response return".getBytes());
		when(residentServiceRestClient.postApi(anyString(), any(), any(), any())).thenReturn(str);
		CryptomanagerResponseDto responseObject = new CryptomanagerResponseDto();
		responseObject.setResponse(new EncryptResponseDto(str));
		when(mapper.readValue(str, CryptomanagerResponseDto.class)).thenReturn(responseObject);

		byte[] card = residentCredentialService.getCard("effc56cd-cf3b-4042-ad48-7277cf90f763");
		assertNotNull(card);
	}

	@Test
	public void testGetCardWithAppIdNull() throws Exception {
		ResponseWrapper<CredentialRequestStatusDto> responseWrapper = new ResponseWrapper<CredentialRequestStatusDto>();
		CredentialRequestStatusDto credentialRequestStatusDto = new CredentialRequestStatusDto();
		credentialRequestStatusDto.setId("id-1");
		credentialRequestStatusDto.setRequestId("effc56cd-cf3b-4042-ad48-7277cf90f763-pdf");
		credentialRequestStatusDto.setStatusCode(EventStatusSuccess.STORED.name());
		credentialRequestStatusDto.setUrl("https://url");

		responseWrapper.setId("id-1");
		responseWrapper.setVersion("T version");
		responseWrapper.setResponsetime("responseTime");
		responseWrapper.setResponse(credentialRequestStatusDto);

		when(env.getProperty(any())).thenReturn("https://credentialUrl");
		URI credentailStatusUri = URI.create("https://credentialUrleffc56cd-cf3b-4042-ad48-7277cf90f763-pdf");
		when(residentServiceRestClient.getApi(credentailStatusUri, ResponseWrapper.class)).thenReturn(responseWrapper);

		URI dataShareUri = URI.create(credentialRequestStatusDto.getUrl());
		when(residentServiceRestClient.getApi(dataShareUri, byte[].class)).thenReturn("card data".getBytes());

		byte[] card = residentCredentialService.getCard("effc56cd-cf3b-4042-ad48-7277cf90f763-pdf");
		assertNotNull(card);
	}

	@Test(expected = ResidentCredentialServiceException.class)
	public void testGetCardWithDataShareUrlNull() throws Exception {
		ResponseWrapper<CredentialRequestStatusDto> responseWrapper = new ResponseWrapper<CredentialRequestStatusDto>();
		CredentialRequestStatusDto credentialRequestStatusDto = new CredentialRequestStatusDto();
		credentialRequestStatusDto.setId("id-1");
		credentialRequestStatusDto.setRequestId("effc56cd-cf3b-4042-ad48-7277cf90f763");
		credentialRequestStatusDto.setStatusCode("code-101");
		credentialRequestStatusDto.setUrl(null);

		responseWrapper.setId("id-1");
		responseWrapper.setVersion("T version");
		responseWrapper.setResponsetime("responseTime");
		responseWrapper.setResponse(credentialRequestStatusDto);

		when(env.getProperty(any())).thenReturn("https://credentialUrl");
		URI credentailStatusUri = URI.create("https://credentialUrleffc56cd-cf3b-4042-ad48-7277cf90f763");
		when(residentServiceRestClient.getApi(credentailStatusUri, ResponseWrapper.class)).thenReturn(responseWrapper);

		residentCredentialService.getCard("effc56cd-cf3b-4042-ad48-7277cf90f763");
	}

	@Test(expected = ResidentCredentialServiceException.class)
	public void testGetCardWithIOException() throws Exception {
		ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setId("id-1");
		responseWrapper.setVersion("T version");
		responseWrapper.setResponsetime("responseTime");
		responseWrapper.setResponse("throw io exception");

		when(env.getProperty(any())).thenReturn("https://credentialUrl");
		URI credentailStatusUri = URI.create("https://credentialUrleffc56cd-cf3b-4042-ad48-7277cf90f763");
		when(residentServiceRestClient.getApi(credentailStatusUri, ResponseWrapper.class)).thenReturn(responseWrapper);

		residentCredentialService.getCard("effc56cd-cf3b-4042-ad48-7277cf90f763");
	}

	@Test(expected = ResidentCredentialServiceException.class)
	public void testGetCardWithApisResourceAccessException() throws Exception {
		when(env.getProperty(any())).thenReturn("https://credentialUrl");
		URI credentailStatusUri = URI.create("https://credentialUrleffc56cd-cf3b-4042-ad48-7277cf90f763");

		when(residentServiceRestClient.getApi(credentailStatusUri, ResponseWrapper.class))
				.thenThrow(ApisResourceAccessException.class);
		residentCredentialService.getCard("effc56cd-cf3b-4042-ad48-7277cf90f763");
	}

	@Test(expected = ResidentCredentialServiceException.class)
	public void testGetCardWithIllegalArgumentException() throws Exception {
		when(env.getProperty(any())).thenReturn("https://credentialUrl");
		URI credentailStatusUri = URI.create("https://credentialUrleffc56cd-cf3b-4042-ad48-7277cf90f763");

		when(residentServiceRestClient.getApi(credentailStatusUri, ResponseWrapper.class))
				.thenThrow(IllegalArgumentException.class);
		residentCredentialService.getCard("effc56cd-cf3b-4042-ad48-7277cf90f763");
	}

	@Test
	public void testCancelCredentialRequest() throws ResidentCredentialServiceException, ApisResourceAccessException {
		CredentialCancelRequestResponseDto credentialCancelRequestResponseDto = new CredentialCancelRequestResponseDto();
		credentialCancelRequestResponseDto.setId("ID-1");
		credentialCancelRequestResponseDto.setRequestId("effc56cd-cf3b-4042-ad48-7277cf90f763");

		ResponseWrapper<CredentialCancelRequestResponseDto> response = new ResponseWrapper<CredentialCancelRequestResponseDto>();
		response.setId("ID-1");
		response.setVersion("T version");
		response.setResponsetime("responseTime");
		response.setResponse(credentialCancelRequestResponseDto);

		when(env.getProperty(any())).thenReturn("https://credentialCancelReqUrl");
		when(residentServiceRestClient.getApi((URI) any(), any())).thenReturn(response);

		CredentialCancelRequestResponseDto responseDto = residentCredentialService
				.cancelCredentialRequest("effc56cd-cf3b-4042-ad48-7277cf90f763");
		assertEquals("ID-1", responseDto.getId());
		assertEquals("effc56cd-cf3b-4042-ad48-7277cf90f763", responseDto.getRequestId());
	}

	@Test(expected = ResidentCredentialServiceException.class)
	public void testCancelCredentialRequestWithResidentCredentialServiceException() throws Exception {
		CredentialCancelRequestResponseDto credentialCancelRequestResponseDto = new CredentialCancelRequestResponseDto();
		credentialCancelRequestResponseDto.setId("ID-1");
		credentialCancelRequestResponseDto.setRequestId("effc56cd-cf3b-4042-ad48-7277cf90f763");

		ResponseWrapper<CredentialCancelRequestResponseDto> response = new ResponseWrapper<CredentialCancelRequestResponseDto>();
		response.setId("ID-1");
		response.setVersion("T version");
		response.setResponsetime("responseTime");
		response.setResponse(credentialCancelRequestResponseDto);

		when(env.getProperty(any())).thenReturn("https://credentialCancelReqUrl");
		when(residentServiceRestClient.getApi((URI) any(), any())).thenReturn(response);

		ServiceError error = new ServiceError();
		error.setErrorCode("invalid-101");
		error.setMessage("invalid center");
		List<ServiceError> errorList = new ArrayList<ServiceError>();
		errorList.add(error);
		response.setErrors(errorList);

		residentCredentialService.cancelCredentialRequest("effc56cd-cf3b-4042-ad48-7277cf90f763");
	}

	@Test(expected = ResidentCredentialServiceException.class)
	public void testCancelCredentialRequestWithApisResourceAccessException() throws Exception {
		when(env.getProperty(any())).thenReturn("https://credentialCancelReqUrl");
		when(residentServiceRestClient.getApi((URI) any(), any())).thenThrow(ApisResourceAccessException.class);

		residentCredentialService.cancelCredentialRequest("effc56cd-cf3b-4042-ad48-7277cf90f763");
	}

	@Test(expected = ResidentCredentialServiceException.class)
	public void testCancelCredentialRequestWithIllegalArgumentException() throws Exception {
		when(env.getProperty(any())).thenReturn("https://credentialCancelReqUrl");
		when(residentServiceRestClient.getApi((URI) any(), any())).thenThrow(IllegalArgumentException.class);

		residentCredentialService.cancelCredentialRequest("effc56cd-cf3b-4042-ad48-7277cf90f763");
	}

	@Test(expected = ResidentCredentialServiceException.class)
	public void testCancelCredentialRequestWithIOException()
			throws ResidentCredentialServiceException, ApisResourceAccessException {
		ResponseWrapper<Object> response = new ResponseWrapper<>();
		response.setId("ID-1");
		response.setVersion("T version");
		response.setResponsetime("responseTime");
		response.setResponse("throw io exception");

		when(env.getProperty(any())).thenReturn("https://credentialCancelReqUrl");
		when(residentServiceRestClient.getApi((URI) any(), any())).thenReturn(response);

		residentCredentialService.cancelCredentialRequest("effc56cd-cf3b-4042-ad48-7277cf90f763");
	}

	@Test(expected = ResidentCredentialServiceException.class)
	public void testCancelCredentialRequestWithResidentServiceCheckedException()
			throws ResidentCredentialServiceException, ApisResourceAccessException, ResidentServiceCheckedException {
		CredentialCancelRequestResponseDto credentialCancelRequestResponseDto = new CredentialCancelRequestResponseDto();
		credentialCancelRequestResponseDto.setId("ID-1");
		credentialCancelRequestResponseDto.setRequestId("effc56cd-cf3b-4042-ad48-7277cf90f763");

		ResponseWrapper<CredentialCancelRequestResponseDto> response = new ResponseWrapper<CredentialCancelRequestResponseDto>();
		response.setId("ID-1");
		response.setVersion("T version");
		response.setResponsetime("responseTime");
		response.setResponse(credentialCancelRequestResponseDto);
		response.setErrors(null);
		when(env.getProperty(any())).thenReturn("https://credentialCancelReqUrl");
		when(residentServiceRestClient.getApi((URI) any(), any())).thenReturn(response);
		when(notificationService.sendNotification(any(), Mockito.nullable(Map.class))).thenThrow(new ResidentServiceCheckedException());

		residentCredentialService.cancelCredentialRequest("effc56cd-cf3b-4042-ad48-7277cf90f763");
	}
}
