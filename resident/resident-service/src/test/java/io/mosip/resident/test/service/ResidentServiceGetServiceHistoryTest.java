package io.mosip.resident.test.service;

import io.mosip.resident.dto.AutnTxnDto;
import io.mosip.resident.dto.ResidentTransactionType;
import io.mosip.resident.dto.ServiceHistoryResponseDto;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.impl.IdentityServiceImpl;
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
    private IdentityServiceImpl identityServiceImpl;

    @Mock
    private ResidentTransactionRepository residentTransactionRepository;

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
    private ArrayList<String> partnerIds;
    List<ResidentTransactionEntity> residentTransactionEntityList;
    ResidentTransactionEntity residentTransactionEntity;

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
        partnerIds = new ArrayList<>();
        residentTransactionEntityList = new ArrayList<>();
        residentTransactionEntity = new ResidentTransactionEntity();

        residentTransactionEntity.setRequestTrnId("12345");
        residentTransactionEntity.setStatusCode(ResidentTransactionType.AUTHENTICATION_REQUEST.toString());
        residentTransactionEntity.setStatusComment("Success");
        residentTransactionEntity.setCrDtimes(LocalDateTime.now());

        residentTransactionEntityList.add(residentTransactionEntity);

        partnerIds.add("m-partner-default-auth");
        partnerIds.add("MOVP");

        Mockito.when(residentTransactionRepository.findByToken(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(residentTransactionEntityList);
        Mockito.when(residentTransactionRepository.findByTokenWithoutDate(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(residentTransactionEntityList);
        Mockito.when(identityServiceImpl.getResidentIndvidualId()).thenReturn("8251649601");
        Mockito.when(identityServiceImpl.getIDAToken(Mockito.anyString(), Mockito.anyString())).thenReturn("346697314566835424394775924659202696");
        Mockito.when(partnerServiceImpl.getPartnerDetails(Mockito.anyString())).thenReturn(partnerIds);
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
        assertEquals(0, residentServiceImpl.getServiceHistory(pageStart, pageSize, null, null, serviceType, "DESC").size());
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

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetServiceHistoryCheckedException() throws ResidentServiceCheckedException, ApisResourceAccessException {
        Integer pageStart = 1;
        Integer pageSize = 1;
        Mockito.when(residentServiceImpl.getServiceHistory( -1, pageSize, fromDate, toDate, serviceType, sortType )).thenThrow(ResidentServiceCheckedException.class);
        Mockito.when(residentServiceImpl.getServiceHistory( pageStart, -1, fromDate, toDate, serviceType, sortType)).thenThrow(ResidentServiceCheckedException.class);
        Mockito.when(residentServiceImpl.getServiceHistory( pageStart, 1, fromDate, toDate, serviceType, sortType)).thenThrow(ResidentServiceCheckedException.class);
        assertEquals(0, residentServiceImpl.getServiceHistory( -1, pageSize, fromDate, toDate, serviceType, sortType).size());
        assertEquals(0, residentServiceImpl.getServiceHistory( pageStart, -1, fromDate, toDate, serviceType, sortType).size());
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetServiceHistoryNegativeResidentServiceCheckedException() throws ResidentServiceCheckedException, ApisResourceAccessException {
        Integer pageStart = 1;
        Mockito.when(residentServiceImpl.getServiceHistory( pageStart, -1, fromDate, toDate, serviceType, sortType)).thenThrow(ResidentServiceCheckedException.class);
        assertEquals(0, residentServiceImpl.getServiceHistory( pageStart, -1, fromDate, toDate, serviceType, sortType).size());
    }
}
