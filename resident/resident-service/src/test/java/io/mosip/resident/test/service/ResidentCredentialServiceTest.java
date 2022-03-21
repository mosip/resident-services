package io.mosip.resident.test.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.dto.*;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentCredentialServiceException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ResidentCredentialService;
import io.mosip.resident.service.impl.ResidentCredentialServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TokenGenerator;
import io.mosip.resident.util.Utilitiy;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class ResidentCredentialServiceTest {

    private ObjectMapper objectMapper = new ObjectMapper();

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
    private Utilitiy utilitiy;

    @Mock
    private AuditUtil audit;

    private ResidentCredentialRequestDto residentCredentialRequestDto;

    @InjectMocks
    private ResidentCredentialService residentCredentialService = new ResidentCredentialServiceImpl();

    @Before
    public void setup() throws IOException, ResidentServiceCheckedException {
        residentCredentialRequestDto = new ResidentCredentialRequestDto();
        residentCredentialRequestDto.setOtp("123");
        residentCredentialRequestDto.setTransactionID("12345");
        residentCredentialRequestDto.setIndividualId("1234567890");
        residentCredentialRequestDto.setIssuer("mpartner-default-print");
        residentCredentialRequestDto.setCredentialType("euin");
        residentCredentialRequestDto.setEncrypt(true);
        residentCredentialRequestDto.setEncryptionKey("abc123");
    }

    @Test
    public void generateCredentialTest() throws OtpValidationFailedException, IOException, ApisResourceAccessException, ResidentServiceCheckedException {
        ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();
        residentCredentialResponseDto.setRequestId("10001100010006920211220064226");
        ResponseWrapper<ResidentCredentialResponseDto> response = new ResponseWrapper<>();
        response.setResponsetime(DateUtils.getCurrentDateTimeString());
        response.setResponse(residentCredentialResponseDto);
        String valueAsString = objectMapper.writeValueAsString(residentCredentialResponseDto);

        PartnerResponseDto partnerResponseDto = new PartnerResponseDto();
        partnerResponseDto.setOrganizationName("MOSIP");
        ResponseWrapper<PartnerResponseDto> partnerResponseDtoResponseWrapper = new ResponseWrapper<>();
        partnerResponseDtoResponseWrapper.setResponse(partnerResponseDto);

        when(idAuthService.validateOtp(residentCredentialRequestDto.getTransactionID(), residentCredentialRequestDto.getIndividualId(), residentCredentialRequestDto.getOtp())).thenReturn(Boolean.TRUE);

        RequestWrapper<CredentialReqestDto> requestDto = new RequestWrapper<>();
        requestDto.setId("mosip.credential.request.service.id");
        requestDto.setRequest(new CredentialReqestDto());
        requestDto.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
        requestDto.setVersion("1.0");

        String partnerUrl = env.getProperty(ApiName.PARTNER_API_URL.name()) + "/" + residentCredentialRequestDto.getIssuer();
        URI partnerUri = URI.create(partnerUrl);
        when(residentServiceRestClient.getApi(partnerUri, ResponseWrapper.class)).thenReturn(partnerResponseDtoResponseWrapper);
        when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(response);

        ResidentCredentialResponseDto credentialResponseDto = residentCredentialService.reqCredential(residentCredentialRequestDto);
        assertEquals("10001100010006920211220064226", credentialResponseDto.getRequestId());
    }
    
    @Test(expected = ResidentServiceException.class)
    public void testGenerateCredentialWithOTPFailure() throws OtpValidationFailedException, ResidentServiceCheckedException {
        when(idAuthService.validateOtp(residentCredentialRequestDto.getTransactionID(), residentCredentialRequestDto.getIndividualId(), residentCredentialRequestDto.getOtp())).thenReturn(Boolean.FALSE);

        residentCredentialService.reqCredential(residentCredentialRequestDto);
    }
    
    @Test(expected = ResidentCredentialServiceException.class)
    public void testReqCredentialWithApisResourceAccessException() throws Exception{
    	ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();
        residentCredentialResponseDto.setRequestId("10001100010006920211220064226");
        ResponseWrapper<ResidentCredentialResponseDto> response = new ResponseWrapper<>();
        response.setResponsetime(DateUtils.getCurrentDateTimeString());
        response.setResponse(residentCredentialResponseDto);
        String valueAsString = objectMapper.writeValueAsString(residentCredentialResponseDto);

        PartnerResponseDto partnerResponseDto = new PartnerResponseDto();
        partnerResponseDto.setOrganizationName("MOSIP");
        ResponseWrapper<PartnerResponseDto> partnerResponseDtoResponseWrapper = new ResponseWrapper<>();
        partnerResponseDtoResponseWrapper.setResponse(partnerResponseDto);

        when(idAuthService.validateOtp(residentCredentialRequestDto.getTransactionID(), residentCredentialRequestDto.getIndividualId(), residentCredentialRequestDto.getOtp())).thenReturn(Boolean.TRUE);

        RequestWrapper<CredentialReqestDto> requestDto = new RequestWrapper<>();
        requestDto.setId("mosip.credential.request.service.id");
        requestDto.setRequest(new CredentialReqestDto());
        requestDto.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
        requestDto.setVersion("1.0");

        String partnerUrl = env.getProperty(ApiName.PARTNER_API_URL.name()) + "/" + residentCredentialRequestDto.getIssuer();
        URI partnerUri = URI.create(partnerUrl);
        when(residentServiceRestClient.getApi(partnerUri, ResponseWrapper.class)).thenReturn(partnerResponseDtoResponseWrapper);
        when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenThrow(ApisResourceAccessException.class);
        residentCredentialService.reqCredential(residentCredentialRequestDto);
    }
    
    @Test(expected = ResidentCredentialServiceException.class)
    public void testGenerateCredentialWithIOException() throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException {
        ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();
        residentCredentialResponseDto.setRequestId("10001100010006920211220064226");
        ResponseWrapper<ResidentCredentialResponseDto> response = new ResponseWrapper<>();
        response.setResponsetime(DateUtils.getCurrentDateTimeString());
        response.setResponse(residentCredentialResponseDto);

        PartnerResponseDto partnerResponseDto = new PartnerResponseDto();
        partnerResponseDto.setOrganizationName("MOSIP");
        ResponseWrapper<PartnerResponseDto> partnerResponseDtoResponseWrapper = new ResponseWrapper<>();
        partnerResponseDtoResponseWrapper.setResponse(partnerResponseDto);

        when(idAuthService.validateOtp(residentCredentialRequestDto.getTransactionID(), residentCredentialRequestDto.getIndividualId(), residentCredentialRequestDto.getOtp())).thenReturn(Boolean.TRUE);

        RequestWrapper<CredentialReqestDto> requestDto = new RequestWrapper<>();
        requestDto.setId("mosip.credential.request.service.id");
        requestDto.setRequest(new CredentialReqestDto());
        requestDto.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
        requestDto.setVersion("1.0");

        String partnerUrl = env.getProperty(ApiName.PARTNER_API_URL.name()) + "/" + residentCredentialRequestDto.getIssuer();
        URI partnerUri = URI.create(partnerUrl);
        when(residentServiceRestClient.getApi(partnerUri, ResponseWrapper.class)).thenReturn(partnerResponseDtoResponseWrapper);
        when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(partnerResponseDtoResponseWrapper);

        residentCredentialService.reqCredential(residentCredentialRequestDto);
    }
    
    @Test
    public void testGetCredentialTypes() throws ApisResourceAccessException {
        Issuer issuer = new Issuer();
        issuer.setCode("paytm");
        issuer.setName("PayTM");
        List<Issuer> issuerList = new ArrayList();
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

        when(env.getProperty(ApiName.CREDENTIAL_TYPES_URL.name())).thenReturn("https://mosip.net/v1/credentialservice/types");
        when(residentServiceRestClient.getApi(any(), any())).thenReturn(credentialTypeResponse);
        CredentialTypeResponse credentialTypes = residentCredentialService.getCredentialTypes();
        assertEquals(credentialTypes.getCredentialTypes().size(), 1);
        assertEquals(credentialTypes.getCredentialTypes().get(0).getDescription(), "Secure Digital QR Code");
        assertEquals(credentialTypes.getCredentialTypes().get(0).getIssuers().size(), 1);
        assertEquals(credentialTypes.getCredentialTypes().get(0).getIssuers().get(0).getName(), "PayTM");
    }

    @Test(expected = ResidentCredentialServiceException.class)
    public void testGetCredentialTypesWithAPIResourceException() throws ApisResourceAccessException {
        when(env.getProperty(ApiName.CREDENTIAL_TYPES_URL.name())).thenReturn("https://mosip.net/v1/credentialservice/types");
        when(residentServiceRestClient.getApi(any(), any())).thenThrow(ApisResourceAccessException.class);
        residentCredentialService.getCredentialTypes();
    }
    
    @Test
    public void testGetPolicyByCredentialType() throws Exception{
    	
    	PartnerCredentialTypePolicyDto credentialTypePolicyDto=new PartnerCredentialTypePolicyDto();
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
    	
    	ResponseWrapper<PartnerCredentialTypePolicyDto> responseWrapper=new ResponseWrapper<PartnerCredentialTypePolicyDto>();
    	responseWrapper.setId("1");
    	responseWrapper.setVersion("T version");
    	responseWrapper.setResponsetime("responseTime");
    	responseWrapper.setResponse(credentialTypePolicyDto);
    	//responseWrapper.setErrors(null);
		
		when(residentServiceRestClient.getApi(any(), any(),any())).thenReturn(responseWrapper);
		
		ResponseWrapper<PartnerCredentialTypePolicyDto> response=residentCredentialService.getPolicyByCredentialType("1", "credentialType");
		assertEquals(response.getResponse(),credentialTypePolicyDto);
		assertEquals(response.getId(),"1");
		assertEquals(response.getVersion(),"T version");
		assertEquals(response,responseWrapper);
		
    }
    
    @Test(expected = ResidentCredentialServiceException.class)
    public void testGetPolicyByCredentialTypeWithAPIResourceException() throws Exception {
        when(residentServiceRestClient.getApi(any(), any(),any())).thenThrow(Exception.class);
        residentCredentialService.getPolicyByCredentialType("2", "credential-type");
    }
    
    @Test
    public void testGetStatus() throws ApisResourceAccessException, IOException, ResidentServiceCheckedException{
    	CredentialRequestStatusDto credentialRequestStatusDto=new CredentialRequestStatusDto();
    	credentialRequestStatusDto.setId("id-1");
    	credentialRequestStatusDto.setRequestId("effc56cd-cf3b-4042-ad48-7277cf90f763");
    	credentialRequestStatusDto.setStatusCode("code-101");
    	credentialRequestStatusDto.setUrl("https://url");
    	ResponseWrapper<CredentialRequestStatusDto> responseWrapper=new ResponseWrapper<CredentialRequestStatusDto>();
    	responseWrapper.setId("id-1");
    	responseWrapper.setVersion("T version");
    	responseWrapper.setResponsetime("responseTime");
    	responseWrapper.setResponse(credentialRequestStatusDto);
    	
    	when(env.getProperty(any())).thenReturn("https://credentialUrl");
    	when(residentServiceRestClient.getApi(any(), any())).thenReturn(responseWrapper);
    	
    	
    	CredentialRequestStatusResponseDto response=residentCredentialService.getStatus("effc56cd-cf3b-4042-ad48-7277cf90f763");
    	assertEquals(response.getId(),responseWrapper.getId());
		assertEquals(response.getStatusCode(),responseWrapper.getResponse().getStatusCode());
		assertEquals(response.getRequestId(),"effc56cd-cf3b-4042-ad48-7277cf90f763");
    }
    
    @Test(expected = ResidentCredentialServiceException.class)
    public void testGetStatusWithApisResourceAccessException() throws ApisResourceAccessException, ResidentServiceCheckedException {
    	when(env.getProperty(any())).thenReturn("https://credentialUrl");
    	when(residentServiceRestClient.getApi(any(), any())).thenThrow(ApisResourceAccessException.class);
    	residentCredentialService.getStatus("effc56cd-cf3b-4042-ad48-7277cf90f763");
    }
    
    @Test(expected = IOException.class)
    public void testGetStatusWithIOException() throws IOException, ApisResourceAccessException, ResidentServiceCheckedException {
    	when(env.getProperty(any())).thenReturn("https://credentialUrl");
    	when(residentServiceRestClient.getApi(any(), any())).thenReturn(new ResponseWrapper<CredentialRequestStatusDto>());
//    	doThrow(new IOException()).when(JsonUtil.readValue(anyString(), any()));
    	residentCredentialService.getStatus("effc56cd-cf3b-4042-ad48-7277cf90f763");
    }
    
    @Test(expected = ResidentCredentialServiceException.class)
    public void testGetStatusWithIllegalArgumentException() throws IllegalArgumentException, ApisResourceAccessException, ResidentServiceCheckedException {
    	when(env.getProperty(any())).thenReturn("https://credentialUrl");
    	when(residentServiceRestClient.getApi(any(), any())).thenThrow(IllegalArgumentException.class);
    	residentCredentialService.getStatus("effc56cd-cf3b-4042-ad48-7277cf90f763");
    }
    
    @Test(expected = ResidentCredentialServiceException.class)
    public void testGetStatusWithResidentServiceCheckedException() throws ResidentServiceCheckedException, ApisResourceAccessException {
    	when(env.getProperty(any())).thenReturn("https://credentialUrl");
    	when(residentServiceRestClient.getApi(any(), any())).thenThrow(ResidentServiceCheckedException.class);
    	residentCredentialService.getStatus("effc56cd-cf3b-4042-ad48-7277cf90f763");
    }
    
    @Test
    public void testGetCard() throws Exception{
    	CredentialRequestStatusDto credentialRequestStatusDto=new CredentialRequestStatusDto();
    	credentialRequestStatusDto.setId("id-1");
    	credentialRequestStatusDto.setRequestId("effc56cd-cf3b-4042-ad48-7277cf90f763");
    	credentialRequestStatusDto.setStatusCode("code-101");
    	credentialRequestStatusDto.setUrl("https://url");
    	
    	ResponseWrapper<CredentialRequestStatusDto> responseWrapper=new ResponseWrapper<CredentialRequestStatusDto>();
    	responseWrapper.setId("id-1");
    	responseWrapper.setVersion("T version");
    	responseWrapper.setResponsetime("responseTime");
    	responseWrapper.setResponse(credentialRequestStatusDto);
    	String str="response return";
    	
    	when(env.getProperty(any())).thenReturn("https://credentialUrl");
    	URI credentailStatusUri = URI.create("https://credentialUrleffc56cd-cf3b-4042-ad48-7277cf90f763");
    	
    	when(residentServiceRestClient.getApi(credentailStatusUri, ResponseWrapper.class)).thenReturn(responseWrapper);
    	URI dataShareUri = URI.create(credentialRequestStatusDto.getUrl());
		
    	when(residentServiceRestClient.getApi(dataShareUri, String.class)).thenReturn(str);
    	
    	RequestWrapper<CryptomanagerRequestDto> request = new RequestWrapper<>();
		CryptomanagerRequestDto cryptomanagerRequestDto = new CryptomanagerRequestDto();
		cryptomanagerRequestDto.setApplicationId("APPLICATION_Id");
		cryptomanagerRequestDto.setData(str);
		cryptomanagerRequestDto.setReferenceId("PARTNER_REFERENCE_Id");
		cryptomanagerRequestDto.setPrependThumbprint(true);
		LocalDateTime localdatetime = LocalDateTime.now();
		request.setRequesttime(localdatetime.toString());
		cryptomanagerRequestDto.setTimeStamp(localdatetime);
		request.setRequest(cryptomanagerRequestDto);
		
    	when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(str);
    	CryptomanagerResponseDto responseObject=new CryptomanagerResponseDto();
    	responseObject.setResponse(new EncryptResponseDto(str));
    	
    	when(mapper.readValue(str, CryptomanagerResponseDto.class)).thenReturn(responseObject);
//    	byte b[] = "H4sICPsdulsCAHJlYWRtZS50eHQAC0".getBytes();
//    	when(CryptoUtil.decodeURLSafeBase64(str)).thenReturn(b);
    	byte[] card=residentCredentialService.getCard("effc56cd-cf3b-4042-ad48-7277cf90f763");
    	assertNotNull(card);
    }
    
    @Test(expected = ResidentCredentialServiceException.class)
    public void testGetCardWithApisResourceAccessException() throws Exception{
    	when(env.getProperty(any())).thenReturn("https://credentialUrl");
    	URI credentailStatusUri = URI.create("https://credentialUrleffc56cd-cf3b-4042-ad48-7277cf90f763");
    	
    	when(residentServiceRestClient.getApi(credentailStatusUri, ResponseWrapper.class)).thenThrow(ApisResourceAccessException.class);
//    	when(residentServiceRestClient.getApi(any(), String.class)).thenThrow(ApisResourceAccessException.class);
    	residentCredentialService.getCard("effc56cd-cf3b-4042-ad48-7277cf90f763");
    }
    
//    @Test(expected = ResidentCredentialServiceException.class)
//    public void testGetCardWithIOException() throws Exception{
//    	CredentialRequestStatusDto credentialRequestStatusDto=new CredentialRequestStatusDto();
//    	credentialRequestStatusDto.setId("id-1");
//    	credentialRequestStatusDto.setRequestId("effc56cd-cf3b-4042-ad48-7277cf90f763");
//    	credentialRequestStatusDto.setStatusCode("code-101");
//    	credentialRequestStatusDto.setUrl("https://url");
//    	
//    	ResponseWrapper<CredentialRequestStatusDto> responseWrapper=new ResponseWrapper<CredentialRequestStatusDto>();
//    	responseWrapper.setId("id-1");
//    	responseWrapper.setVersion("T version");
//    	responseWrapper.setResponsetime("responseTime");
//    	responseWrapper.setResponse(credentialRequestStatusDto);
//    	String str="response return";
//    	
//    	when(env.getProperty(any())).thenReturn("https://credentialUrl");
//    	URI credentailStatusUri = URI.create("https://credentialUrleffc56cd-cf3b-4042-ad48-7277cf90f763");
//    	
//    	when(residentServiceRestClient.getApi(credentailStatusUri, ResponseWrapper.class)).thenReturn(responseWrapper);
//    	URI dataShareUri = URI.create(credentialRequestStatusDto.getUrl());
//    	when(residentServiceRestClient.getApi(dataShareUri, String.class)).thenReturn(responseWrapper);
//    	when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(responseWrapper);
//    	residentCredentialService.getCard("effc56cd-cf3b-4042-ad48-7277cf90f763");
//    }
    
    @Test
    public void testCancelCredentialRequest() throws ResidentCredentialServiceException, ApisResourceAccessException{
    	CredentialCancelRequestResponseDto credentialCancelRequestResponseDto=new CredentialCancelRequestResponseDto();
    	credentialCancelRequestResponseDto.setId("ID-1");
    	credentialCancelRequestResponseDto.setRequestId("effc56cd-cf3b-4042-ad48-7277cf90f763");
    	
    	ResponseWrapper<CredentialCancelRequestResponseDto> response = new ResponseWrapper<CredentialCancelRequestResponseDto>();
    	response.setId("ID-1");
    	response.setVersion("T version");
    	response.setResponsetime("responseTime");
    	response.setResponse(credentialCancelRequestResponseDto);
    	
    	when(env.getProperty(any())).thenReturn("https://credentialCancelReqUrl");
    	when(residentServiceRestClient.getApi(any(), any())).thenReturn(response);
    	
    	CredentialCancelRequestResponseDto responseDto=residentCredentialService.cancelCredentialRequest("effc56cd-cf3b-4042-ad48-7277cf90f763");
    	assertEquals("ID-1", responseDto.getId());
    	assertEquals("effc56cd-cf3b-4042-ad48-7277cf90f763", responseDto.getRequestId());
    }
    
    @Test(expected = ResidentCredentialServiceException.class)
    public void testCancelCredentialRequestWithResidentCredentialServiceException() throws Exception{
    	CredentialCancelRequestResponseDto credentialCancelRequestResponseDto=new CredentialCancelRequestResponseDto();
    	credentialCancelRequestResponseDto.setId("ID-1");
    	credentialCancelRequestResponseDto.setRequestId("effc56cd-cf3b-4042-ad48-7277cf90f763");
    	
    	ResponseWrapper<CredentialCancelRequestResponseDto> response = new ResponseWrapper<CredentialCancelRequestResponseDto>();
    	response.setId("ID-1");
    	response.setVersion("T version");
    	response.setResponsetime("responseTime");
    	response.setResponse(credentialCancelRequestResponseDto);
    	
    	when(env.getProperty(any())).thenReturn("https://credentialCancelReqUrl");
    	when(residentServiceRestClient.getApi(any(), any())).thenReturn(response);
    	
    	ServiceError error=new ServiceError();
    	error.setErrorCode("invalid-101");
    	error.setMessage("invalid center");
    	List<ServiceError> errorList=new ArrayList<ServiceError>();
    	errorList.add(error);
    	response.setErrors(errorList);
    	
    	residentCredentialService.cancelCredentialRequest("effc56cd-cf3b-4042-ad48-7277cf90f763");
    }
    
    @Test(expected = ResidentCredentialServiceException.class)
    public void testCancelCredentialRequestWithApisResourceAccessException() throws Exception{
    	when(env.getProperty(any())).thenReturn("https://credentialCancelReqUrl");
    	when(residentServiceRestClient.getApi(any(), any())).thenThrow(ApisResourceAccessException.class);
    	
    	residentCredentialService.cancelCredentialRequest("effc56cd-cf3b-4042-ad48-7277cf90f763");
    }
    
    @Test(expected = ResidentCredentialServiceException.class)
    public void testCancelCredentialRequestWithIllegalArgumentException() throws Exception{
    	when(env.getProperty(any())).thenReturn("https://credentialCancelReqUrl");
    	when(residentServiceRestClient.getApi(any(), any())).thenThrow(IllegalArgumentException.class);
    	
    	residentCredentialService.cancelCredentialRequest("effc56cd-cf3b-4042-ad48-7277cf90f763");
    }
    
}
