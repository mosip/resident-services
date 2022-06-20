package io.mosip.resident.test.service;

import io.mosip.resident.dto.CredentialRequestStatusResponseDto;
import io.mosip.resident.dto.ResidentServiceHistoryResponseDto;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.PartnerServiceImpl;
import io.mosip.resident.service.impl.ResidentCredentialServiceImpl;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class ResidentServiceGetServiceRequestUpdateTest {

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
    private ResidentTransactionRepository residentTransactionRepository;

    @Mock
    private ResidentCredentialServiceImpl residentCredentialServiceImpl;

    @Mock
    private PartnerServiceImpl partnerServiceImpl;
    List<ResidentServiceHistoryResponseDto> details = null;
    List<ResidentTransactionEntity> residentTransactionEntityList;
    ResidentTransactionEntity residentTransactionEntity;
    CredentialRequestStatusResponseDto credentialRequestStatusResponseDto;
    @Before
    public void setup() throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
        details = new ArrayList<>();
        residentTransactionEntity = new ResidentTransactionEntity();
        residentTransactionEntityList = new ArrayList<>();
        credentialRequestStatusResponseDto = new CredentialRequestStatusResponseDto();

        residentTransactionEntity.setAid("c758c9e9-1882-49a4-b07b-db98a1942538");
        residentTransactionEntityList.add(residentTransactionEntity);

        credentialRequestStatusResponseDto.setRequestId("c758c9e9-1882-49a4-b07b-db98a1942538");
        credentialRequestStatusResponseDto.setStatusCode("NEW");
        credentialRequestStatusResponseDto.setId("8067104584");

        Mockito.when(partnerServiceImpl.getPartnerDetails(Mockito.anyString())).thenReturn(new ArrayList<>());
        Mockito.when(identityServiceImpl.getResidentIdaToken()).thenReturn("346697314566835424394775924659202696");
        Mockito.when(residentTransactionRepository.findRequestIdByToken(Mockito.anyString(), Mockito.anyString()
        , Mockito.any())).thenReturn(residentTransactionEntityList);
        Mockito.when(residentCredentialServiceImpl.getStatus(Mockito.anyString())).thenReturn(credentialRequestStatusResponseDto);
        Mockito.when(residentServiceImpl.getServiceRequestUpdate(null, null)).thenReturn(details);
        Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());
    }

    @Test
    public void testGetServiceRequestUpdate() throws ResidentServiceCheckedException {
        Integer pageStart = 1;
        Integer pageSize = 1;

        assertEquals(1, residentServiceImpl.getServiceRequestUpdate(pageStart, pageSize).size());
    }

    @Test
    public void testGetServiceRequestUpdateNullCheck() throws ResidentServiceCheckedException {
        Integer pageSize = 1;

        assertEquals(1, residentServiceImpl.getServiceRequestUpdate( null, pageSize).size());
        assertEquals(1, residentServiceImpl.getServiceRequestUpdate( null, null).size());
        assertEquals(1, residentServiceImpl.getServiceRequestUpdate( 1, null).size());
        assertEquals(1, residentServiceImpl.getServiceRequestUpdate( 1, null).size());
        assertEquals(1, residentServiceImpl.getServiceRequestUpdate( 1, null).size());
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetServiceRequestUpdateCheckedException() throws ResidentServiceCheckedException {
        Integer pageStart = 1;
        Integer pageSize = 1;
        Mockito.when(residentServiceImpl.getServiceRequestUpdate( -1, pageSize )).thenThrow(ResidentServiceCheckedException.class);
        Mockito.when(residentServiceImpl.getServiceRequestUpdate( pageStart, -1)).thenThrow(ResidentServiceCheckedException.class);
        Mockito.when(residentServiceImpl.getServiceRequestUpdate( pageStart, 1)).thenThrow(ResidentServiceCheckedException.class);
        assertEquals(0, residentServiceImpl.getServiceRequestUpdate( pageStart, pageSize).size());
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetServiceRequestUpdateApisResourceAccessException() throws ApisResourceAccessException, ResidentServiceCheckedException, IOException {
        Integer pageStart = 1;
        Mockito.when(identityServiceImpl.getResidentIdaToken()).thenThrow(new ApisResourceAccessException());
        assertEquals(0, residentServiceImpl.getServiceRequestUpdate(pageStart, 1).size());
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetServiceRequestUpdateNegativeResidentServiceCheckedException() throws ResidentServiceCheckedException {
        Integer pageStart = 1;
        Mockito.when(residentServiceImpl.getServiceRequestUpdate( pageStart, -1)).thenThrow(ResidentServiceCheckedException.class);
        assertEquals(0, residentServiceImpl.getServiceRequestUpdate( pageStart, -1).size());
    }

}
