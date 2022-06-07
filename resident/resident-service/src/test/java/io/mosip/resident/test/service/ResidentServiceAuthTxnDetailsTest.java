package io.mosip.resident.test.service;

import io.mosip.resident.dto.AutnTxnDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.impl.PartnerServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.validator.RequestValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class ResidentServiceAuthTxnDetailsTest {

    @Mock
    private AuditUtil audit;

    @InjectMocks
    private ResidentServiceImpl residentServiceImpl;

    @Mock
    private RequestValidator validator;

    @Mock
    private Utilities utilities;

    @Mock
    private PartnerServiceImpl partnerServiceImpl;
    List<AutnTxnDto> details = null;

    @Before
    public void setup() throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
        details = new ArrayList<>();
        Mockito.when(partnerServiceImpl.getPartnerDetails(Mockito.anyString())).thenReturn(new ArrayList<>());
        Mockito.when(residentServiceImpl.getAuthTxnDetails("8251649601",null, null, "UIN", LocalDateTime.now(), LocalDateTime.now())).thenReturn(details);
        Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());
    }

    @Test
    public void testGetTxnDetails() throws ResidentServiceCheckedException {
        String individualId = "8251649601";
        Integer pageStart = 1;
        Integer pageSize = 1;

        assertEquals(0, residentServiceImpl.getAuthTxnDetails(individualId, pageStart, pageSize, "UIN",
                LocalDateTime.now(), LocalDateTime.now()).size());
    }

    @Test
    public void testGetTxnDetailsNullCheck() throws ResidentServiceCheckedException {
        String individualId = "8251649601";
        Integer pageSize = 1;

        assertEquals(0, residentServiceImpl.getAuthTxnDetails(individualId, null, pageSize, "UIN",
                LocalDateTime.now(), LocalDateTime.now()).size());
        assertEquals(0, residentServiceImpl.getAuthTxnDetails(individualId, null, null, "UIN",
                LocalDateTime.now(), LocalDateTime.now()).size());
        assertEquals(0, residentServiceImpl.getAuthTxnDetails(individualId, 1, null, "UIN",
                LocalDateTime.now(), LocalDateTime.now()).size());
        assertEquals(0, residentServiceImpl.getAuthTxnDetails("5936486578", 1, null, "VID",
                LocalDateTime.now(), LocalDateTime.now()).size());
        assertEquals(0, residentServiceImpl.getAuthTxnDetails("5936416578", 1, null, "VID",
                LocalDateTime.now(), LocalDateTime.now()).size());
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetTxnDetailsResidentServiceCheckedException() throws ResidentServiceCheckedException {
        String individualId = "8251649601";
        Integer pageStart = 1;
        Integer pageSize = 1;

        Mockito.when(residentServiceImpl.getAuthTxnDetails(individualId, pageStart, pageSize, "UIN",
                LocalDateTime.now(), LocalDateTime.now())).thenThrow(ResidentServiceCheckedException.class);
        Mockito.when(residentServiceImpl.getAuthTxnDetails(individualId, -1, pageSize, "UIN",
                LocalDateTime.now(), LocalDateTime.now())).thenThrow(ResidentServiceCheckedException.class);
        Mockito.when(residentServiceImpl.getAuthTxnDetails(individualId, pageStart, -1, "UIN",
                LocalDateTime.now(), LocalDateTime.now())).thenThrow(ResidentServiceCheckedException.class);
        Mockito.when(residentServiceImpl.getAuthTxnDetails(individualId, pageStart, -1, null,
                LocalDateTime.now(), LocalDateTime.now())).thenThrow(ResidentServiceCheckedException.class);
        Mockito.when(residentServiceImpl.getAuthTxnDetails(individualId, pageStart, 1, "invalid",
                LocalDateTime.now(), LocalDateTime.now())).thenThrow(ResidentServiceCheckedException.class);
        Mockito.when(residentServiceImpl.getAuthTxnDetails("8251649601", pageStart, 1, "VID",
                LocalDateTime.now(), LocalDateTime.now())).thenThrow(ResidentServiceCheckedException.class);
        assertEquals(0, residentServiceImpl.getAuthTxnDetails(individualId, pageStart, pageSize, "UIN",
                LocalDateTime.now(), LocalDateTime.now()).size());
    }
}
