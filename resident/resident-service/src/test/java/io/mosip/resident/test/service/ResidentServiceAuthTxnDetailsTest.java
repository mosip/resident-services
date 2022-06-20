package io.mosip.resident.test.service;

import io.mosip.resident.dto.AutnTxnDto;
import io.mosip.resident.dto.ResidentTransactionType;
import io.mosip.resident.entity.AutnTxn;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.AutnTxnRepository;
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
    private IdentityServiceImpl identityServiceImpl;

    @Mock
    private AutnTxnRepository autnTxnRepository;

    @Mock
    private PartnerServiceImpl partnerServiceImpl;
    List<AutnTxnDto> details = null;
    ArrayList<String> partnerIds = null;
    List<AutnTxn> autnTxnList;
    AutnTxn autnTxn;

    @Before
    public void setup() throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
        details = new ArrayList<>();
        partnerIds = new ArrayList<>();
        autnTxnList = new ArrayList<>();
        autnTxn = new AutnTxn();

        partnerIds.add("m-partner-default-auth");
        partnerIds.add("MOVP");

        autnTxn.setId("12345");
        autnTxn.setAuthTypeCode(ResidentTransactionType.SERVICE_REQUEST.toString());
        autnTxn.setEntityName("RESIDENT");
        autnTxn.setRequestDTtimes(LocalDateTime.now());
        autnTxn.setStatusCode("NEW");
        autnTxn.setRequestTrnId("12345");
        autnTxn.setStatusComment("New Request");

        autnTxnList.add(autnTxn);

        Mockito.when(partnerServiceImpl.getPartnerDetails(Mockito.anyString())).thenReturn(partnerIds);
        Mockito.when(identityServiceImpl.getIDAToken(Mockito.anyString(), Mockito.anyString())).thenReturn("346697314566835424394775924659202696");
        Mockito.when(autnTxnRepository.findByToken(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(autnTxnList);
        Mockito.when(residentServiceImpl.getAuthTxnDetails("8251649601",null, null, "UIN", LocalDateTime.now(), LocalDateTime.now())).thenReturn(details);
        Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());
    }

    @Test
    public void testGetTxnDetails() throws ResidentServiceCheckedException {
        String individualId = "8251649601";
        Integer pageStart = 1;
        Integer pageSize = 1;

        assertEquals(2, residentServiceImpl.getAuthTxnDetails(individualId, pageStart, pageSize, "UIN",
                LocalDateTime.now(), LocalDateTime.now()).size());
    }

    @Test
    public void testGetTxnDetailsNullCheck() throws ResidentServiceCheckedException {
        String individualId = "8251649601";
        Integer pageSize = 1;

        assertEquals(2, residentServiceImpl.getAuthTxnDetails(individualId, null, pageSize, "UIN",
                LocalDateTime.now(), LocalDateTime.now()).size());
        assertEquals(2, residentServiceImpl.getAuthTxnDetails(individualId, null, null, "UIN",
                LocalDateTime.now(), LocalDateTime.now()).size());
        assertEquals(2, residentServiceImpl.getAuthTxnDetails(individualId, 1, null, "UIN",
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

        Mockito.when(residentServiceImpl.getAuthTxnDetails(individualId, -1, pageSize, "UIN",
                LocalDateTime.now(), LocalDateTime.now())).thenThrow(ResidentServiceCheckedException.class);

        Mockito.when(residentServiceImpl.getAuthTxnDetails(individualId, pageStart, -1, null,
                LocalDateTime.now(), LocalDateTime.now())).thenThrow(ResidentServiceCheckedException.class);

        Mockito.when(residentServiceImpl.getAuthTxnDetails("8251649601", pageStart, 1, "VID",
                LocalDateTime.now(), LocalDateTime.now())).thenThrow(ResidentServiceCheckedException.class);
        assertEquals(0, residentServiceImpl.getAuthTxnDetails(individualId, pageStart, pageSize, "UIN",
                LocalDateTime.now(), LocalDateTime.now()).size());
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetTxnDetailsApisResourceAccessException() throws ApisResourceAccessException, ResidentServiceCheckedException, IOException {
        String individualId = "8251649601";
        Integer pageStart = 1;

        Mockito.when(utilities.getUinByVid(individualId)).thenThrow(new ApisResourceAccessException());

        assertEquals(0, residentServiceImpl.getAuthTxnDetails(individualId, pageStart, 1, "VID",
                LocalDateTime.now(), LocalDateTime.now()).size());
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetTxnDetailsPageFetchNegativeResidentServiceCheckedException() throws ResidentServiceCheckedException {
        String individualId = "8251649601";
        Integer pageStart = 1;

        Mockito.when(residentServiceImpl.getAuthTxnDetails(individualId, pageStart, -1, "UIN",
                LocalDateTime.now(), LocalDateTime.now())).thenThrow(ResidentServiceCheckedException.class);

        assertEquals(0, residentServiceImpl.getAuthTxnDetails(individualId, pageStart, -1, "UIN",
                LocalDateTime.now(), LocalDateTime.now()).size());
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetTxnDetailsIdTypeInvalidResidentServiceCheckedException() throws ResidentServiceCheckedException {
        String individualId = "8251649601";
        Integer pageStart = 1;

        Mockito.when(residentServiceImpl.getAuthTxnDetails(individualId, pageStart, 3, "invalid",
                LocalDateTime.now(), LocalDateTime.now())).thenThrow(ResidentServiceCheckedException.class);

        assertEquals(0, residentServiceImpl.getAuthTxnDetails(individualId, pageStart, -1, "invalid",
                LocalDateTime.now(), LocalDateTime.now()).size());
    }

}
