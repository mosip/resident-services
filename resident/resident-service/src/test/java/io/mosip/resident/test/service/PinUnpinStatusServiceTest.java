package io.mosip.resident.test.service;

import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.PinUnpinStatusService;
import io.mosip.resident.service.impl.PinUnpinStatusServiceImpl;
import io.mosip.resident.util.AuditUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * This class is used to test service class of pin or unpin status api based on event id.
 * @Author Kamesh Shekhar Prasad
 */

@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class PinUnpinStatusServiceTest {

    @InjectMocks
    PinUnpinStatusService pinUnpinStatusService = new PinUnpinStatusServiceImpl();

    @Mock
    AuditUtil auditUtil;

    @Mock
    ResidentTransactionRepository residentTransactionRepository;

    Optional<ResidentTransactionEntity> residentTransactionEntity;
    @Before
    public void setup(){
        residentTransactionEntity = Optional.of(new ResidentTransactionEntity());
        residentTransactionEntity.get().setEventId("eventId");
        residentTransactionEntity.get().setPinnedStatus(true);
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
    }

    @Test
    public void pinStatusSuccessTest(){
        ResponseEntity<?> responseEntity = pinUnpinStatusService.pinStatus("eventId", true);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void pinStatusFailureTest(){
        residentTransactionEntity = Optional.empty();
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        ResponseEntity<?> responseEntity = pinUnpinStatusService.pinStatus("eventId", true);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void unPinStatusSuccessTest(){
        ResponseEntity<?> responseEntity = pinUnpinStatusService.pinStatus("eventId", false);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void unPinStatusFailureTest(){
        residentTransactionEntity = Optional.empty();
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        ResponseEntity<?> responseEntity = pinUnpinStatusService.pinStatus("eventId", false);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

}
