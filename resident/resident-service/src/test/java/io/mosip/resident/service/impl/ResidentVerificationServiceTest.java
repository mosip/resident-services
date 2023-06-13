package io.mosip.resident.service.impl;

import static org.junit.Assert.assertEquals;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;

import io.mosip.resident.dto.VerificationResponseDTO;
import io.mosip.resident.dto.VerificationStatusDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.VerificationService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utility;

@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class ResidentVerificationServiceTest {

    @Mock
    private ResidentServiceRestClient residentServiceRestClient;

    @Mock
    Environment env;

    @Mock
    private AuditUtil audit;

    @InjectMocks
    private VerificationService verificationService = new VerificationServiceImpl();

    @Mock
    private IdentityServiceImpl identityServiceImpl;
    
    @Mock
	private Utility utility;

    @Mock
    private ResidentTransactionRepository residentTransactionRepository;

    @Before
    public void setup() throws Exception {
    	ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
		residentTransactionEntity.setEventId(UUID.randomUUID().toString());
    }

    @Test
    public void testCheckChannelVerificationStatus() throws ResidentServiceCheckedException, NoSuchAlgorithmException, ApisResourceAccessException {

        boolean verificationStatus = false;
        VerificationStatusDTO verificationStatusDTO = new VerificationStatusDTO();
        VerificationResponseDTO verificationResponseDTO = new VerificationResponseDTO();
        verificationStatusDTO.setVerificationStatus(verificationStatus);
        verificationResponseDTO.setResponse(verificationStatusDTO);
        verificationResponseDTO.setId("mosip.resident.channel.verification.status");
        verificationResponseDTO.setVersion("v1");
        verificationResponseDTO.setResponseTime(DateTime.now().toString());

        VerificationResponseDTO verificationResponseDTO1 =verificationService.checkChannelVerificationStatus("email", "8251649601");
        assertEquals(verificationResponseDTO.getResponse().isVerificationStatus(), verificationResponseDTO1.getResponse().isVerificationStatus());
    }
}