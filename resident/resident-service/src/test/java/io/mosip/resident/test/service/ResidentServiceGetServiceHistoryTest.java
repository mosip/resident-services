package io.mosip.resident.test.service;

import io.mosip.resident.dto.AutnTxnDto;
import io.mosip.resident.dto.ServiceHistoryResponseDto;
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

/**
 * This class is used to test the get service history service
 * @author Kamesh Shekhar Prasad
 */

@RunWith(SpringRunner.class)
public class ResidentServiceGetServiceHistoryTest {

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

    private int pageStart;
    private int pageSize;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private String serviceType;
    private String sortType;
    List<ServiceHistoryResponseDto> serviceHistoryResponseDto;

    @Before
    public void setup() throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
        details = new ArrayList<>();
        pageSize = 10;
        pageStart = 2;
        fromDate = LocalDateTime.now();
        toDate = LocalDateTime.now();
        serviceType = "AUTHENTICATION_REQUEST";
        sortType = "ASC";
        serviceHistoryResponseDto = new ArrayList<>();

        Mockito.when(partnerServiceImpl.getPartnerDetails(Mockito.anyString())).thenReturn(new ArrayList<>());
        Mockito.when(residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, serviceType, sortType)).thenReturn(serviceHistoryResponseDto);
        Mockito.when(partnerServiceImpl.getPartnerDetails(Mockito.anyString())).thenReturn(new ArrayList<>());
        Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());
    }

    @Test
    public void testGetServiceHistorySuccess() throws ResidentServiceCheckedException, ApisResourceAccessException {
        pageStart = 2;
        pageSize = 3;

        Mockito.when(residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, serviceType, sortType)).thenReturn(serviceHistoryResponseDto);
        assertEquals(0, residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, serviceType, sortType).size());
        assertEquals(0, residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, serviceType, "DESC").size());
    }

    @Test
    public void testGetServiceHistoryNullCheck() throws ResidentServiceCheckedException, ApisResourceAccessException {
        Mockito.when(residentServiceImpl.getServiceHistory(null, null, fromDate, toDate, serviceType, sortType)).thenReturn(null);
        assertEquals(0, residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, serviceType, sortType).size());
        assertEquals(0, residentServiceImpl.getServiceHistory(null, pageSize, fromDate, toDate, serviceType, sortType).size());
        assertEquals(0, residentServiceImpl.getServiceHistory(null, null, fromDate, toDate, serviceType, sortType).size());
        assertEquals(0, residentServiceImpl.getServiceHistory(2, null, fromDate, toDate, serviceType, sortType).size());
        assertEquals(0, residentServiceImpl.getServiceHistory(2, null, fromDate, toDate, null, sortType).size());
        assertEquals(0, residentServiceImpl.getServiceHistory(2, null, fromDate, toDate, null, null).size());
    }
}
