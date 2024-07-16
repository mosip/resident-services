package io.mosip.resident.service.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.ResponseDTO;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.PinUnpinStatusService;

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
    ResidentTransactionRepository residentTransactionRepository;

    @Before
    public void setup(){
        Mockito.when(residentTransactionRepository.existsById(Mockito.anyString())).thenReturn(true);
    }

    @Test
    public void pinStatusSuccessTest() throws ResidentServiceCheckedException{
        ResponseWrapper<ResponseDTO> responseDTO = pinUnpinStatusService.pinStatus("eventId", true);
        assertEquals(String.valueOf(HttpStatus.OK.value()), responseDTO.getResponse().getStatus());
    }

    @Test
    public void pinStatusFailureTest() throws ResidentServiceCheckedException{
        Mockito.when(residentTransactionRepository.existsById(Mockito.anyString())).thenReturn(false);
        ResponseWrapper<ResponseDTO> responseDTO = pinUnpinStatusService.pinStatus("eventId", true);
        assertEquals(responseDTO.getErrors().get(0).getErrorCode(), ResidentErrorCode.EVENT_STATUS_NOT_FOUND.getErrorCode());
    }

    @Test
    public void unPinStatusSuccessTest() throws ResidentServiceCheckedException{
        ResponseWrapper<ResponseDTO> responseDTO = pinUnpinStatusService.pinStatus("eventId", false);
        assertEquals(String.valueOf(HttpStatus.OK.value()), responseDTO.getResponse().getStatus());
    }

    @Test
    public void unPinStatusFailureTest() throws ResidentServiceCheckedException{
        Mockito.when(residentTransactionRepository.existsById(Mockito.anyString())).thenReturn(false);
        ResponseWrapper<ResponseDTO> responseDTO = pinUnpinStatusService.pinStatus("eventId", false);
        assertEquals(responseDTO.getErrors().get(0).getErrorCode(), ResidentErrorCode.EVENT_STATUS_NOT_FOUND.getErrorCode());
    }

}
