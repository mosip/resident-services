package io.mosip.resident.service.impl;

import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.helper.CredentialStatusUpdateHelper;
import io.mosip.resident.repository.ResidentTransactionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.test.context.ContextConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * @author Kamesh Shekhar Prasad
 * Test clss for WebsubCredentialStatusUpdateServiceImpl.
 */
@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class WebSubCredentialStatusUpdateServiceTest {

    @Mock
    private CredentialStatusUpdateHelper credentialStatusUpdateHelper;

    @Mock
    private ResidentTransactionRepository repo;

    @InjectMocks
    private WebSubCredentialStatusUpdateServiceImpl webSubCredentialStatusUpdateService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testUpdateCredentialStatus() throws Exception {
        // Mock data
        Map<String, Object> eventModel = new HashMap<>();
        eventModel.put(ResidentConstants.EVENT, getMockEventMap());

        // Mock repository response
        ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
        when(repo.findOneByCredentialRequestId("12345")).thenReturn(Optional.of(residentTransactionEntity));

        // Invoke the method
        webSubCredentialStatusUpdateService.updateCredentialStatus(eventModel);

        // Verify that the expected methods were called
        verify(credentialStatusUpdateHelper, times(1)).updateStatus(eq(residentTransactionEntity), anyMap());
        verify(repo, times(1)).findOneByCredentialRequestId("12345");
    }

    @Test
    public void testUpdateCredentialStatusWithNullData() throws Exception {
        // Mock data
        Map<String, Object> eventModel = new HashMap<>();
        eventModel.put(ResidentConstants.EVENT, getMockEventMap());

        // Mock repository response
        when(repo.findOneByCredentialRequestId("12345")).thenReturn(Optional.empty());

        // Invoke the method
        webSubCredentialStatusUpdateService.updateCredentialStatus(eventModel);

        // Verify that the expected methods were called
        verify(repo, times(1)).findOneByCredentialRequestId("12345");
    }

    private Map<String, Object> getMockEventMap() {
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put(ResidentConstants.REQUEST_ID, "12345");
        // Add other necessary data
        return eventMap;
    }
}
