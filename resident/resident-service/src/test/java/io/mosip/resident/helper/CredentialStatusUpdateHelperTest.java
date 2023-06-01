package io.mosip.resident.helper;

import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.util.Utility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
public class CredentialStatusUpdateHelperTest {

    @Mock
    private Environment env;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ResidentTransactionRepository repo;

    @Mock
    private Utility utility;

    @InjectMocks
    private CredentialStatusUpdateHelper helper;

    private ResidentTransactionEntity txn;

    @Before
    public void setup() {
        txn = new ResidentTransactionEntity();
        txn.setEventId("event123");
        txn.setStatusCode("INITIAL");
    }

    @Test
    public void testUpdateStatus_NewStatusAndReferenceLink() throws ResidentServiceCheckedException, ApisResourceAccessException {
        Map<String, String> credentialStatus = new HashMap<>();
        credentialStatus.put("status", "SUCCESS");
        credentialStatus.put("url", "https://example.com");
        when(env.getProperty(any(String.class))).thenReturn("SUCCESS");
        helper.updateStatus(txn, credentialStatus);
    }

    @Test
    public void testUpdateStatus_NoNewStatus() throws ResidentServiceCheckedException, ApisResourceAccessException {
        Map<String, String> credentialStatus = new HashMap<>();
        credentialStatus.put("status", "INITIAL");
        helper.updateStatus(txn, credentialStatus);
    }

    @Test
    public void testUpdateStatus_NoNotificationSent() throws ResidentServiceCheckedException, ApisResourceAccessException {
        Map<String, String> credentialStatus = new HashMap<>();
        credentialStatus.put("status", "IN_PROGRESS");
        when(env.getProperty(any(String.class))).thenReturn(null);
        helper.updateStatus(txn, credentialStatus);
    }
}

