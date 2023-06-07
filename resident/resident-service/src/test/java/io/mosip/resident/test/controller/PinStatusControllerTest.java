package io.mosip.resident.test.controller;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.controller.PinStatusController;
import io.mosip.resident.dto.ResponseDTO;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.PinUnpinStatusService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.validator.RequestValidator;

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
    public void pinStatusControllerTest() throws ResidentServiceCheckedException{
        ResponseWrapper<ResponseDTO> responseWrapper = new ResponseWrapper<>();
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setStatus(HttpStatus.OK.toString());
        responseWrapper.setResponse(responseDTO);
        Mockito.when(pinUnpinStatusService.pinStatus(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(responseWrapper);
        ResponseWrapper<ResponseDTO> resultResponseDTO = pinStatusController.pinStatus("eventId");
        assertEquals(resultResponseDTO.getResponse().getStatus(), HttpStatus.OK.toString());
    }

    @Test
    public void unPinStatusControllerTest() throws ResidentServiceCheckedException{
        ResponseWrapper<ResponseDTO> responseWrapper = new ResponseWrapper<>();
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setStatus(HttpStatus.OK.toString());
        responseWrapper.setResponse(responseDTO);
        Mockito.when(pinUnpinStatusService.pinStatus(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(responseWrapper);
        ResponseWrapper<ResponseDTO> responseEntity = pinStatusController.unPinStatus("eventId");
        assertEquals(responseEntity.getResponse().getStatus(), HttpStatus.OK.toString());
    }
}
