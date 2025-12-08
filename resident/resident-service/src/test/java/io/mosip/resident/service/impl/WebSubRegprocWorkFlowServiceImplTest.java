package io.mosip.resident.service.impl;

import io.mosip.resident.dto.WorkflowCompletedEventDTO;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.util.IdentityDataUtil;
import io.mosip.resident.util.Utility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

/**
 * Unit tests for WebSubRegprocWorkFlowServiceImpl
 * @author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSubRegprocWorkFlowServiceImplTest {

    private WebSubRegprocWorkFlowServiceImpl sut;

    @Mock
    private Environment environment;

    @Mock
    private ResidentTransactionRepository residentTransactionRepository;

    @Mock
    private Utility utility;

    @Mock
    private IdentityDataUtil identityDataUtil;

    @Before
    public void setUp() {
        sut = new WebSubRegprocWorkFlowServiceImpl();
        ReflectionTestUtils.setField(sut, "environment", environment);
        ReflectionTestUtils.setField(sut, "residentTransactionRepository", residentTransactionRepository);
        ReflectionTestUtils.setField(sut, "utility", utility);
        ReflectionTestUtils.setField(sut, "identityDataUtil", identityDataUtil);
    }

    @Test
    public void updateResidentStatusWhenResultCodeIsNullDoesNothing() throws Exception {
        // Arrange
        WorkflowCompletedEventDTO dto = new WorkflowCompletedEventDTO();
        dto.setResultCode(null);
        dto.setInstanceId("ANY");

        // Act
        sut.updateResidentStatus(dto);

        // Assert - verify repository or utility/identityDataUtil not called
        verifyNoInteractions(residentTransactionRepository);
        verifyNoInteractions(utility);
        verifyNoInteractions(identityDataUtil);
    }

    @Test
    public void updateResidentStatusWhenInstanceIdNullDoesNothing() throws Exception {
        // Arrange: result code present but instanceId null
        WorkflowCompletedEventDTO dto = new WorkflowCompletedEventDTO();
        dto.setResultCode("ANY_CODE");
        dto.setInstanceId(null);

        // Act
        sut.updateResidentStatus(dto);

        // Assert - repository should not be called, no update/notification
        verifyNoInteractions(residentTransactionRepository);
        verifyNoInteractions(utility);
        verifyNoInteractions(identityDataUtil);
    }
}
