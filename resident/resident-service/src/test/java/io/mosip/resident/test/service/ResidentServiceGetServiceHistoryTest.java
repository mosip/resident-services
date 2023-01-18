package io.mosip.resident.test.service;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.EventStatus;
import io.mosip.resident.constant.EventStatusSuccess;
import io.mosip.resident.constant.ServiceType;
import io.mosip.resident.dto.AidStatusRequestDTO;
import io.mosip.resident.dto.AutnTxnDto;
import io.mosip.resident.dto.PageDto;
import io.mosip.resident.dto.ServiceHistoryResponseDto;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.ProxyIdRepoService;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;
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
	
    @MockBean
    private ProxyIdRepoService proxyIdRepoService;

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
    private EntityManager entityManager;

    @Mock
    private PartnerServiceImpl partnerServiceImpl;

    @Mock
    private IdAuthService idAuthServiceImpl;
    List<AutnTxnDto> details = null;

    private int pageStart;
    private int pageSize;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String serviceType;
    private String sortType;
    List<ServiceHistoryResponseDto> serviceHistoryResponseDto;
    private ArrayList<String> partnerIds;
    List<ResidentTransactionEntity> residentTransactionEntityList;
    ResidentTransactionEntity residentTransactionEntity;
    private String statusFilter;
    private String searchText;

    private ResponseWrapper<PageDto<ServiceHistoryResponseDto>> responseWrapper;
    private Query query;

    @Before
    public void setup() throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
        statusFilter = EventStatus.SUCCESS.toString();
        searchText = "1";
        details = new ArrayList<>();
        pageSize = 10;
        pageStart = 2;
        serviceType = "AUTHENTICATION_REQUEST";
        sortType = "ASC";
        serviceHistoryResponseDto = new ArrayList<>();
        partnerIds = new ArrayList<>();
        residentTransactionEntityList = new ArrayList<>();
        residentTransactionEntity = new ResidentTransactionEntity();
        residentTransactionEntity.setEventId("eventId");

        responseWrapper = new ResponseWrapper<>();
        query = Mockito.mock(Query.class);

        residentTransactionEntity.setRequestTrnId("12345");
        residentTransactionEntity.setStatusCode(ServiceType.AUTHENTICATION_REQUEST.name());
        residentTransactionEntity.setStatusComment("Success");
        residentTransactionEntity.setCrDtimes(LocalDateTime.now());
        residentTransactionEntity.setStatusCode(EventStatusSuccess.AUTHENTICATION_SUCCESSFUL.toString());

        residentTransactionEntityList.add(residentTransactionEntity);

        partnerIds.add("m-partner-default-auth");
        partnerIds.add("MOVP");



        Mockito.when(residentTransactionRepository.findByTokenAndTransactionType(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString())).thenReturn(residentTransactionEntityList);

        Mockito.when(residentTransactionRepository.findByTokenWithoutDate(Mockito.anyString(), Mockito.any(), Mockito.any(),Mockito.any(), Mockito.anyString())).thenReturn(residentTransactionEntityList);
        Mockito.when(identityServiceImpl.getResidentIndvidualId()).thenReturn("8251649601");
        Mockito.when(identityServiceImpl.getIDAToken(Mockito.anyString(), Mockito.anyString())).thenReturn("346697314566835424394775924659202696");
        Mockito.when(partnerServiceImpl.getPartnerDetails(Mockito.anyString())).thenReturn(partnerIds);
        Mockito.when(entityManager.createNativeQuery(Mockito.anyString(), (Class) Mockito.any())).thenReturn(query);
        Mockito.when(entityManager.createNativeQuery(Mockito.anyString())).thenReturn(query);
        Mockito.when(query.getSingleResult()).thenReturn(BigInteger.valueOf(1));
        Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());
    }

    @Test
    public void testGetServiceHistorySuccess() throws ResidentServiceCheckedException, ApisResourceAccessException {
        pageStart = 2;
        pageSize = 3;
        fromDate = LocalDate.now();
        toDate = LocalDate.now();
        assertEquals(3, residentServiceImpl.getServiceHistory(pageStart, pageSize, LocalDate.now(), LocalDate.now(), serviceType, sortType, statusFilter, searchText, "eng", 0).getResponse().getPageSize());
        assertEquals(3, residentServiceImpl.getServiceHistory(pageStart, pageSize, LocalDate.now(), LocalDate.now(), serviceType, "DESC", statusFilter, searchText, "eng", 0).getResponse().getPageSize());
    }

    @Test
    public void testGetServiceHistoryDateNullCheck() throws ResidentServiceCheckedException, ApisResourceAccessException {
        pageStart = 2;
        pageSize = 3;

        assertEquals(3, residentServiceImpl.getServiceHistory(pageStart, pageSize, null, null, serviceType, sortType, statusFilter, searchText, "eng", 0).getResponse().getPageSize());
        assertEquals(3, residentServiceImpl.getServiceHistory(pageStart, pageSize, null, null, serviceType, "DESC", statusFilter, searchText, "eng", 0).getResponse().getPageSize());
        assertEquals(3, residentServiceImpl.getServiceHistory(pageStart, pageSize, null, null, serviceType, "DESC", statusFilter, searchText, "eng", 0).getResponse().getPageSize());

    }

    @Test
    public void testGetServiceHistoryNullCheck() throws ResidentServiceCheckedException, ApisResourceAccessException {
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, serviceType, sortType, statusFilter, searchText, "eng", 0).getResponse().getPageSize());
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetServiceHistoryCheckedException() throws ResidentServiceCheckedException, ApisResourceAccessException {
        Integer pageStart = 1;
        Integer pageSize = 1;
        Mockito.when(residentServiceImpl.getServiceHistory( -1, pageSize, fromDate, toDate, serviceType, sortType, statusFilter, searchText, "eng", 0)).thenThrow(ResidentServiceCheckedException.class);
        Mockito.when(residentServiceImpl.getServiceHistory( pageStart, -1, fromDate, toDate, serviceType, sortType, statusFilter, searchText, "eng", 0)).thenThrow(ResidentServiceCheckedException.class);
        Mockito.when(residentServiceImpl.getServiceHistory( pageStart, 1, fromDate, toDate, serviceType, sortType, statusFilter, searchText, "eng", 0)).thenThrow(ResidentServiceCheckedException.class);
        assertEquals(0, residentServiceImpl.getServiceHistory( -1, pageSize, fromDate, toDate, serviceType, sortType, statusFilter, searchText, "eng", 0).getResponse().getPageSize());
        assertEquals(0, residentServiceImpl.getServiceHistory( pageStart, -1, fromDate, toDate, serviceType, sortType, statusFilter, searchText, "eng", 0).getResponse().getPageSize());
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetServiceHistoryNegativeResidentServiceCheckedException() throws ResidentServiceCheckedException, ApisResourceAccessException {
        Integer pageStart = 1;
        Mockito.when(residentServiceImpl.getServiceHistory( pageStart, -1, fromDate, toDate, serviceType, sortType, statusFilter, searchText, "eng", 0)).thenThrow(ResidentServiceCheckedException.class);
        assertEquals(0, residentServiceImpl.getServiceHistory( pageStart, -1, fromDate, toDate, serviceType, sortType, statusFilter, searchText, "eng", 0).getResponse().getPageSize());
    }

    @Test
    public void testPageSizeCheck() throws ResidentServiceCheckedException, ApisResourceAccessException {
        pageSize = 10;
        pageStart = 1;
        assertEquals(10, residentServiceImpl.getServiceHistory(null, null, fromDate, toDate, serviceType, sortType, statusFilter, searchText, "eng", 0).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(null, pageSize, fromDate, toDate, serviceType, sortType, statusFilter, searchText, "eng", 0).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, null, fromDate, toDate, serviceType, sortType, statusFilter, searchText, "eng", 0).getResponse().getPageSize());
    }

    @Test
    public void testSortTypeNullCheck() throws ResidentServiceCheckedException, ApisResourceAccessException {
        pageStart = 1;
        pageSize = 10;
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, serviceType,
                null, statusFilter, searchText, "eng", 0).getResponse().getPageSize());
    }

    @Test
    public void testServiceHistoryWithDifferentParameters() throws ResidentServiceCheckedException, ApisResourceAccessException {
        pageStart = 1;
        pageSize = 10;
        fromDate = LocalDate.MAX;
        toDate = LocalDate.MIN;
        serviceType = ServiceType.AUTHENTICATION_REQUEST.toString();
        sortType = "ASC";
        statusFilter = "SUCCESS";
        searchText = "a";
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, serviceType, sortType,
                statusFilter, searchText, "eng", 0).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, "ALL", sortType,
                statusFilter, searchText, "eng", 0).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, serviceType, sortType,
                statusFilter, null, "eng", 0).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, "ALL", sortType,
                statusFilter, null, "eng", 0).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, serviceType, sortType,
                null, "a", "eng", 0).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, "ALL", sortType,
                null, "a", "eng", 0).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, null, sortType,
                statusFilter, "a", "eng", 0).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, "ALL", sortType,
                null, "a", "eng", 0).getResponse().getPageSize());

    }

    @Test
    public void testGetAidStatus() throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException {
        AidStatusRequestDTO aidStatusRequestDTO = new AidStatusRequestDTO();
        aidStatusRequestDTO.setIndividualId("10087100401001420220929210144");
        aidStatusRequestDTO.setOtp("111111");
        aidStatusRequestDTO.setTransactionId("1234567890");
        Mockito.when(idAuthServiceImpl.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(identityServiceImpl.getIndividualIdForAid(Mockito.anyString())).thenReturn("2476302389");
        assertEquals("PROCESSED", residentServiceImpl.getAidStatus(aidStatusRequestDTO).getAidStatus());
    }

    @Test
    public void testGetAidStatusOtpValidationFalse() throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException {
        AidStatusRequestDTO aidStatusRequestDTO = new AidStatusRequestDTO();
        aidStatusRequestDTO.setIndividualId("10087100401001420220929210144");
        aidStatusRequestDTO.setOtp("111111");
        aidStatusRequestDTO.setTransactionId("1234567890");
        Mockito.when(idAuthServiceImpl.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(identityServiceImpl.getIndividualIdForAid(Mockito.anyString())).thenReturn("2476302389");
        assertEquals("PROCESSED", residentServiceImpl.getAidStatus(aidStatusRequestDTO, false).getAidStatus());
    }


}
