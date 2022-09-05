package io.mosip.resident.test.controller;

import io.mosip.resident.controller.PinStatusController;
import io.mosip.resident.service.PinUnpinStatusService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.validator.RequestValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;

/**
 * This class is used to test pin or unpin status api based on event id.
 * @Author Kamesh Shekhar Prasad
 */

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration
public class PinStatusControllerTest {

    @InjectMocks
    PinStatusController pinStatusController;

    @Mock
    private AuditUtil audit;

    @Mock
    private RequestValidator requestValidator;

    @Mock
    private PinUnpinStatusService pinUnpinStatusService;

    @Test
    public void pinStatusControllerTest(){
        Mockito.when(pinUnpinStatusService.pinStatus(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        ResponseEntity<?> responseEntity = pinStatusController.pinStatus("eventId");
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void unPinStatusControllerTest(){
        Mockito.when(pinUnpinStatusService.pinStatus(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        ResponseEntity<?> responseEntity = pinStatusController.unPinStatus("eventId");
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    }
}
