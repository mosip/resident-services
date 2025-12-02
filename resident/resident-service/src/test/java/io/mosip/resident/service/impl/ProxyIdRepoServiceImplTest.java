package io.mosip.resident.service.impl;

import io.mosip.idrepository.core.dto.IdResponseDTO;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.util.IdentityDataUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProxyIdRepoServiceImpl.discardDraft(...)
 */
@RunWith(MockitoJUnitRunner.class)
public class ProxyIdRepoServiceImplTest {

    private ProxyIdRepoServiceImpl service;

    @Mock
    private ResidentServiceRestClient residentServiceRestClient;

    @Mock
    private ResidentTransactionRepository residentTransactionRepository;

    @Mock
    private Utility utility;

    @Mock
    private IdentityDataUtil identityDataUtil;

    @Before
    public void setUp() {
        service = new ProxyIdRepoServiceImpl();
        ReflectionTestUtils.setField(service, "residentServiceRestClient", residentServiceRestClient);
        ReflectionTestUtils.setField(service, "residentTransactionRepository", residentTransactionRepository);
        ReflectionTestUtils.setField(service, "utility", utility);
        ReflectionTestUtils.setField(service, "identityDataUtil", identityDataUtil);
    }

    @Test
    public void discardDraft_whenResponseHasNoRecordsError_throwsNoRecordsFound() throws Exception {
        // Arrange
        String eid = "EID-404";
        ResidentTransactionEntity entity = new ResidentTransactionEntity();
        entity.setAid("AID-2");
        when(residentTransactionRepository.findById(eq(eid))).thenReturn(Optional.of(entity));

        IdResponseDTO idResp = mock(IdResponseDTO.class);
        ServiceError err = new ServiceError(ProxyIdRepoServiceImpl.NO_RECORDS_FOUND_ID_REPO_ERROR_CODE, "no record");
        when(idResp.getErrors()).thenReturn(List.of(err));

        when(residentServiceRestClient.deleteApi(eq(ApiName.IDREPO_IDENTITY_DISCARD_DRAFT),
                anyList(), anyString(), anyString(), eq(IdResponseDTO.class))).thenReturn(idResp);

        // Act / Assert
        try {
            service.discardDraft(eid);
            fail("Expected ResidentServiceCheckedException");
        } catch (ResidentServiceCheckedException ex) {
            assertEquals(io.mosip.resident.constant.ResidentErrorCode.NO_RECORDS_FOUND.getErrorCode(), ex.getErrorCode());
        }

        verify(utility, never()).updateEntity(anyString(), anyString(), anyBoolean(), anyString(), any());
        verify(identityDataUtil, never()).sendNotification(anyString(), anyString(), any());
    }

    @Test
    public void discardDraft_whenResponseHasOtherError_throwsUnknownException() throws Exception {
        // Arrange
        String eid = "EID-500";
        ResidentTransactionEntity entity = new ResidentTransactionEntity();
        entity.setAid("AID-3");
        when(residentTransactionRepository.findById(eq(eid))).thenReturn(Optional.of(entity));

        IdResponseDTO idResp = mock(IdResponseDTO.class);
        ServiceError err = new ServiceError("SOME_OTHER_CODE", "some error");
        when(idResp.getErrors()).thenReturn(List.of(err));

        when(residentServiceRestClient.deleteApi(eq(ApiName.IDREPO_IDENTITY_DISCARD_DRAFT),
                anyList(), anyString(), anyString(), eq(IdResponseDTO.class))).thenReturn(idResp);

        // Act / Assert
        try {
            service.discardDraft(eid);
            fail("Expected ResidentServiceCheckedException");
        } catch (ResidentServiceCheckedException ex) {
            assertEquals(io.mosip.resident.constant.ResidentErrorCode.UNKNOWN_EXCEPTION.getErrorCode(), ex.getErrorCode());
        }

        verify(utility, never()).updateEntity(anyString(), anyString(), anyBoolean(), anyString(), any());
        verify(identityDataUtil, never()).sendNotification(anyString(), anyString(), any());
    }

    @Test
    public void discardDraft_whenApiThrowsApisResourceAccessException_wrapsAndRethrows() throws Exception {
        // Arrange
        String eid = "EID-EX";
        ResidentTransactionEntity entity = new ResidentTransactionEntity();
        entity.setAid("AID-EX");
        when(residentTransactionRepository.findById(eq(eid))).thenReturn(Optional.of(entity));

        when(residentServiceRestClient.deleteApi(eq(ApiName.IDREPO_IDENTITY_DISCARD_DRAFT),
                anyList(), anyString(), anyString(), eq(IdResponseDTO.class)))
                .thenThrow(new ApisResourceAccessException("down"));

        // Act / Assert
        try {
            service.discardDraft(eid);
            fail("Expected ResidentServiceCheckedException");
        } catch (ResidentServiceCheckedException ex) {
            // API_RESOURCE_ACCESS_EXCEPTION is mapped to ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION
            assertEquals(io.mosip.resident.constant.ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(), ex.getErrorCode());
        }

        verify(utility, never()).updateEntity(anyString(), anyString(), anyBoolean(), anyString(), any());
        verify(identityDataUtil, never()).sendNotification(anyString(), anyString(), any());
    }
}
