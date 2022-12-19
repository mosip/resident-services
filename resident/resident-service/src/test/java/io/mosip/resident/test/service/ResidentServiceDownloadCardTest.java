package io.mosip.resident.test.service;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.dto.BellNotificationDto;
import io.mosip.resident.dto.DigitalCardStatusResponseDto;
import io.mosip.resident.dto.PageDto;
import io.mosip.resident.dto.ServiceHistoryResponseDto;
import io.mosip.resident.dto.UnreadNotificationDto;
import io.mosip.resident.dto.UnreadServiceNotificationDto;
import io.mosip.resident.dto.UserInfoDto;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.entity.ResidentUserEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.EventIdNotPresentException;
import io.mosip.resident.exception.InvalidRequestTypeCodeException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.repository.ResidentUserRepository;
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ResidentCredentialServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.util.Utilitiy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Kamesh Shekhar Prasad
 * Test class to test download card service method.
 */

@RunWith(SpringRunner.class)
public class ResidentServiceDownloadCardTest {

    @InjectMocks
    private ResidentServiceImpl residentServiceImpl;
    @Mock
    private ResidentTransactionRepository residentTransactionRepository;

    @Mock
    private ResidentCredentialServiceImpl residentCredentialServiceImpl;

    @Mock
    private AuditUtil audit;

    @Mock
    private Environment environment;

    @Mock
    private ResidentServiceRestClient residentServiceRestClient;

    @Mock
    private ObjectStoreHelper objectStoreHelper;

    @Mock
    private TemplateUtil templateUtil;

    @Mock
    private IdentityServiceImpl identityServiceImpl;

    @Mock
    private ResidentUserRepository residentUserRepository;

    @Mock
    private ProxyMasterdataService proxyMasterdataService;

    @Mock
    private TemplateManager templateManager;

    @Mock
    private Utilitiy utility;

    private byte[] result;
    private String eventId;
    private String idType;
    private String resultResponse;
    private Optional<ResidentTransactionEntity> residentTransactionEntity;
    private ResponseWrapper<DigitalCardStatusResponseDto> responseDto;
    DigitalCardStatusResponseDto digitalCardStatusResponseDto;

    @Before
    public void setup() throws Exception {
        result = "data".getBytes();
        eventId = "123";
        idType = "RID";
        resultResponse = "[B@3a7e365";

        residentTransactionEntity = Optional.of(new ResidentTransactionEntity());
        residentTransactionEntity.get().setEventId(eventId);
        residentTransactionEntity.get().setRequestTypeCode(RequestType.UPDATE_MY_UIN.toString());
        residentTransactionEntity.get().setAid(eventId);
        digitalCardStatusResponseDto = new DigitalCardStatusResponseDto();
        responseDto = new ResponseWrapper<>();
        digitalCardStatusResponseDto.setId(eventId);
        digitalCardStatusResponseDto.setStatusCode(HttpStatus.OK.toString());
        digitalCardStatusResponseDto.setUrl("http://datashare.datashare/123");
        responseDto.setResponse(digitalCardStatusResponseDto);
        responseDto.setVersion("v1");
        responseDto.setId("io.mosip.digital.card");
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        Mockito.when(residentCredentialServiceImpl.getCard(Mockito.anyString())).thenReturn(result);
        Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn(ApiName.DIGITAL_CARD_STATUS_URL.toString());
        Mockito.when(residentServiceRestClient.getApi((URI)any(), any(Class.class))).thenReturn(responseDto);
        Mockito.when(objectStoreHelper.decryptData(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn("ZGF0YQ==");
    }
    @Test(expected = ResidentServiceException.class)
    public void testUpdateMyUinException() throws ResidentServiceCheckedException{
        residentTransactionEntity = Optional.of(new ResidentTransactionEntity());
        residentTransactionEntity.get().setEventId(eventId);
        residentTransactionEntity.get().setRequestTypeCode(RequestType.UPDATE_MY_UIN.name());
        residentTransactionEntity.get().setAid(eventId);
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        byte[] response = residentServiceImpl.downloadCard(eventId, idType);
        assertEquals(response, result);
    }

    @Test
    public void testUpdateMyUinSuccess() throws Exception {
        residentTransactionEntity = Optional.of(new ResidentTransactionEntity());
        residentTransactionEntity.get().setEventId(eventId);
        residentTransactionEntity.get().setRequestTypeCode(RequestType.UPDATE_MY_UIN.name());
        residentTransactionEntity.get().setAid(eventId);
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        digitalCardStatusResponseDto.setStatusCode("AVAILABLE");
        String digitalCardStatusUri= "http://datashare.datashare/123";
        digitalCardStatusResponseDto.setUrl(digitalCardStatusUri);
        responseDto.setResponse(digitalCardStatusResponseDto);
        responseDto.setVersion("v1");
        responseDto.setId("io.mosip.digital.card");
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        Mockito.when(residentCredentialServiceImpl.getCard(Mockito.anyString())).thenReturn(result);
        Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn(ApiName.DIGITAL_CARD_STATUS_URL.toString());
        when(residentServiceRestClient.getApi(URI.create(ApiName.DIGITAL_CARD_STATUS_URL.name()+eventId),ResponseWrapper.class)).thenReturn(responseDto);
        when(residentServiceRestClient.getApi(URI.create(digitalCardStatusUri), byte[].class))
                .thenReturn("data".getBytes());
        byte[] response = residentServiceImpl.downloadCard(eventId, idType);
        assertNotNull(response);
    }

    @Test(expected = EventIdNotPresentException.class)
    public void testEventIdNotPresentException() throws ResidentServiceCheckedException {
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());
        byte[] response = residentServiceImpl.downloadCard(eventId, idType);
        assertEquals(response, result);
    }

    @Test(expected = InvalidRequestTypeCodeException.class)
    public void testInvalidRequestTypeCodeException() throws ResidentServiceCheckedException {
        residentTransactionEntity = Optional.of(new ResidentTransactionEntity());
        residentTransactionEntity.get().setEventId(eventId);
        residentTransactionEntity.get().setRequestTypeCode(RequestType.REVOKE_VID.name());
        residentTransactionEntity.get().setAid(eventId);
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        byte[] response = residentServiceImpl.downloadCard(eventId, idType);
        assertEquals(response, result);
    }

    @Test
    public void testGetUserInfo() throws ApisResourceAccessException{
        Mockito.when(identityServiceImpl.getClaimFromIdToken(Mockito.anyString())).thenReturn("claim");
        ResidentUserEntity residentUserEntity = new ResidentUserEntity();
        residentUserEntity.setHost("localhost");
        residentUserEntity.setIdaToken("123");
        residentUserEntity.setIpAddress("http");
        Optional<ResidentUserEntity> response = Optional.of(residentUserEntity);
        Mockito.when(residentUserRepository.findById(Mockito.anyString())).thenReturn(response);
        ResponseWrapper<UserInfoDto> responseWrapper = residentServiceImpl.getUserinfo("123");
        assertEquals(responseWrapper.getResponse().getFullName(), responseWrapper.getResponse().getFullName());
    }

    @Test(expected = ResidentServiceException.class)
    public void testGetUserInfoFailed() throws ApisResourceAccessException {
        Mockito.when(identityServiceImpl.getClaimFromIdToken(Mockito.anyString())).thenReturn("claim");
        Optional<ResidentUserEntity> response = Optional.empty();
        Mockito.when(residentUserRepository.findById(Mockito.anyString())).thenReturn(response);
        ResponseWrapper<UserInfoDto> responseWrapper = residentServiceImpl.getUserinfo("123");
        assertEquals(responseWrapper.getResponse().getFullName(), responseWrapper.getResponse().getFullName());
    }

    @Test
    public void testDownloadServiceHistory() throws ResidentServiceCheckedException, IOException {
        ResponseWrapper<PageDto<ServiceHistoryResponseDto>> responseWrapper = new ResponseWrapper<>();
        ServiceHistoryResponseDto serviceHistoryResponseDto = new ServiceHistoryResponseDto();
        serviceHistoryResponseDto.setEventId("123");
        PageDto<ServiceHistoryResponseDto> responseDtoPageDto= new PageDto<>();
        responseDtoPageDto.setData(List.of(serviceHistoryResponseDto));
        responseWrapper.setResponse(responseDtoPageDto);
        ResponseWrapper responseWrapper1 = new ResponseWrapper<>();
        Map<String, Object> templateResponse = new LinkedHashMap<>();
        templateResponse.put("fileText", "test");
        responseWrapper1.setResponse(templateResponse);
        Mockito.when(proxyMasterdataService.getAllTemplateBylangCodeAndTemplateTypeCode(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(responseWrapper1);
        Mockito.when(templateManager.merge(any(), any())).thenReturn(new ByteArrayInputStream("pdf".getBytes()));
        Mockito.when(utility.signPdf(any(), any())).thenReturn("pdf".getBytes(StandardCharsets.UTF_8));
        byte[] pdfDocument = residentServiceImpl.downLoadServiceHistory(responseWrapper, "eng",
                LocalDateTime.now(), LocalDate.now(), LocalDate.now(),
                String.valueOf(RequestType.DOWNLOAD_PERSONALIZED_CARD), "SUCCESS");
        assertNotNull(pdfDocument);
    }

    @Test
    public void testGetUnreadNotifyList(){
        ResponseWrapper<List<UnreadServiceNotificationDto>> responseWrapper = new ResponseWrapper<>();
        UnreadServiceNotificationDto unreadServiceNotificationDto = new UnreadServiceNotificationDto();
        unreadServiceNotificationDto.setEventId("123");
        unreadServiceNotificationDto.setRequestType("SERVICE_HISTORY");
        responseWrapper.setResponse(List.of(unreadServiceNotificationDto));
        ResidentTransactionEntity residentTransactionEntity1 = new ResidentTransactionEntity();
        residentTransactionEntity1.setStatusComment("notification send");
        residentTransactionEntity1.setEventId("123");
        residentTransactionEntity1.setRequestSummary("notification");
        residentTransactionEntity1.setStatusCode("200");
        residentTransactionEntity1.setRequestDtimes(LocalDateTime.now());
        residentTransactionEntity1.setRequestTypeCode("SERVICE_HISTORY");
        Mockito.when(residentTransactionRepository.findByIdandStatus(Mockito.anyString())).thenReturn(List.of(residentTransactionEntity1));
        assertEquals("123", residentServiceImpl.getUnreadnotifylist("123").getResponse().get(0).getEventId());
    }

    @Test
    public void testUpdatebellClickdttimes(){
        ResidentUserEntity residentUserEntity = new ResidentUserEntity();
        residentUserEntity.setHost("localhost");
        residentUserEntity.setIdaToken("123");
        residentUserEntity.setIpAddress("http");
        Optional<ResidentUserEntity> response = Optional.of(residentUserEntity);
        Mockito.when(residentUserRepository.findById(Mockito.anyString())).thenReturn(response);
        Mockito.when(residentUserRepository.updateByIdandTime(any(), any())).thenReturn(2);
        assertEquals(2, residentServiceImpl.updatebellClickdttimes("123"));
    }

    @Test
    public void testUpdatebellClickdttimesNewRecord(){
        Optional<ResidentUserEntity> response = Optional.empty();
        Mockito.when(residentUserRepository.findById(Mockito.anyString())).thenReturn(response);
        Mockito.when(residentUserRepository.insertRecordByIdAndNotificationClickTime(any(), any())).thenReturn(1);
        assertEquals(1, residentServiceImpl.updatebellClickdttimes("123"));
    }

    @Test
    public void testGetbellClickdttimes(){
        ResidentUserEntity residentUserEntity = new ResidentUserEntity();
        residentUserEntity.setHost("localhost");
        residentUserEntity.setIdaToken("123");
        residentUserEntity.setIpAddress("http");
        residentUserEntity.setLastbellnotifDtimes(LocalDateTime.of(2015, 12, 3, 4, 4, 4));
        Optional<ResidentUserEntity> response = Optional.of(residentUserEntity);
        Mockito.when(residentUserRepository.findById(Mockito.anyString())).thenReturn(response);
        Mockito.when(residentUserRepository.insertRecordByIdAndNotificationClickTime(any(), any())).thenReturn(1);
        ResponseWrapper<BellNotificationDto> responseWrapper = new ResponseWrapper<>();
        BellNotificationDto bellNotificationDto = new BellNotificationDto();
        bellNotificationDto.setLastbellnotifclicktime(LocalDateTime.now());
        responseWrapper.setResponse(bellNotificationDto);
        assertEquals(LocalDateTime.of(2015, 12, 3, 4, 4, 4),
                residentServiceImpl.getbellClickdttimes("123").getResponse().getLastbellnotifclicktime());
    }

    @Test
    public void testGetnotificationCount(){
        ResidentUserEntity residentUserEntity = new ResidentUserEntity();
        residentUserEntity.setHost("localhost");
        residentUserEntity.setIdaToken("123");
        residentUserEntity.setIpAddress("http");
        residentUserEntity.setLastloginDtime(LocalDateTime.of(2015, 12, 3, 4, 4, 4));
        Optional<ResidentUserEntity> response = Optional.of(residentUserEntity);
        ResponseWrapper<UnreadNotificationDto> responseWrapper = new ResponseWrapper<>();
        UnreadNotificationDto unreadServiceNotificationDto = new UnreadNotificationDto();
        unreadServiceNotificationDto.setUnreadCount(4L);
        responseWrapper.setResponse(unreadServiceNotificationDto);
        Mockito.when(residentTransactionRepository.findByIdandcount(Mockito.anyString())).thenReturn(4L);
        assertEquals(Optional. of(4L), Optional.ofNullable(residentServiceImpl.
                getnotificationCount("123").getResponse().getUnreadCount()));
    }

    @Test
    public void testGetnotificationCountLastLoginTime(){
        ResponseWrapper<UnreadNotificationDto> responseWrapper = new ResponseWrapper<>();
        UnreadNotificationDto unreadServiceNotificationDto = new UnreadNotificationDto();
        unreadServiceNotificationDto.setUnreadCount(4L);
        responseWrapper.setResponse(unreadServiceNotificationDto);
        ResidentUserEntity residentUserEntity = new ResidentUserEntity();
        residentUserEntity.setHost("localhost");
        residentUserEntity.setIdaToken("123");
        residentUserEntity.setIpAddress("http");
        residentUserEntity.setLastbellnotifDtimes(LocalDateTime.of(2015, 12, 3, 4, 4, 4));
        Optional<ResidentUserEntity> response = Optional.of(residentUserEntity);
        Mockito.when(residentUserRepository.findById(Mockito.anyString())).thenReturn(response);
        Mockito.when(residentTransactionRepository.findByIdandcount(Mockito.anyString())).thenReturn(4L);
        assertEquals(Optional. of(0L), Optional.ofNullable(residentServiceImpl.
                getnotificationCount("123").getResponse().getUnreadCount()));
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetCheckAidStatusFailed() throws ResidentServiceCheckedException {
        Mockito.when(identityServiceImpl.getUinForIndividualId(Mockito.anyString())).thenReturn(null);
        residentServiceImpl.checkAidStatus("123");
    }
}
