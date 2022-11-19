package io.mosip.resident.test.service;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.dto.DigitalCardStatusResponseDto;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.EventIdNotPresentException;
import io.mosip.resident.exception.InvalidRequestTypeCodeException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.impl.ResidentCredentialServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
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
}
