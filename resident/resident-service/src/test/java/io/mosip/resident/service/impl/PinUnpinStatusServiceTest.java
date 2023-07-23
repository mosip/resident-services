package io.mosip.resident.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

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
import io.mosip.resident.entity.ResidentTransactionEntity;
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

    Optional<ResidentTransactionEntity> residentTransactionEntity;
    @Before
    public void setup(){
        residentTransactionEntity = Optional.of(new ResidentTransactionEntity());
        residentTransactionEntity.get().setEventId("eventId");
        residentTransactionEntity.get().setPinnedStatus(true);
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
    }

    @Test
    public void pinStatusSuccessTest() throws ResidentServiceCheckedException{
        ResponseWrapper<ResponseDTO> responseDTO = pinUnpinStatusService.pinStatus("eventId", true);
        assertEquals(responseDTO.getResponse().getStatus(), HttpStatus.OK.toString());
    }

    @Test
    public void pinStatusFailureTest() throws ResidentServiceCheckedException{
        residentTransactionEntity = Optional.empty();
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        ResponseWrapper<ResponseDTO> responseDTO = pinUnpinStatusService.pinStatus("eventId", true);
        assertEquals(responseDTO.getErrors().get(0).getErrorCode(), ResidentErrorCode.EVENT_STATUS_NOT_FOUND.getErrorCode());
    }

    @Test
    public void unPinStatusSuccessTest() throws ResidentServiceCheckedException{
        ResponseWrapper<ResponseDTO> responseDTO = pinUnpinStatusService.pinStatus("eventId", false);
        assertEquals(responseDTO.getResponse().getStatus(), HttpStatus.OK.toString());
    }

    @Test
    public void unPinStatusFailureTest() throws ResidentServiceCheckedException{
        residentTransactionEntity = Optional.empty();
        Mockito.when(residentTransactionRepository.findById(Mockito.anyString())).thenReturn(residentTransactionEntity);
        ResponseWrapper<ResponseDTO> responseDTO = pinUnpinStatusService.pinStatus("eventId", false);
        assertEquals(responseDTO.getErrors().get(0).getErrorCode(), ResidentErrorCode.EVENT_STATUS_NOT_FOUND.getErrorCode());
    }

}
