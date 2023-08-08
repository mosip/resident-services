package io.mosip.resident.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import io.mosip.resident.dto.IdentityDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;

import io.mosip.resident.dto.VerificationResponseDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.VerificationService;
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

	@InjectMocks
	private VerificationService verificationService = new VerificationServiceImpl();

	@Mock
	private IdentityServiceImpl identityService;

	@Mock
	private Utility utility;

	@Mock
	private ResidentTransactionRepository residentTransactionRepository;

	private ResidentTransactionEntity residentTransactionEntity;

	private IdentityDTO identityValue;

	@Before
	public void setup() throws Exception {
		residentTransactionEntity = new ResidentTransactionEntity();
		residentTransactionEntity.setEventId(UUID.randomUUID().toString());
		when(utility.getIdForResidentTransaction(anyList(), any(), anyString())).thenReturn("hash ref id");
		identityValue = new IdentityDTO();
		identityValue.setEmail("aaa@bbb.com");
		identityValue.setPhone("987654321");
		identityValue.setUIN("123");
		when(identityService.getIdentity(Mockito.anyString())).thenReturn(identityValue);
		when(identityService.getIDAToken(Mockito.anyString())).thenReturn("123");
	}

	@Test
	public void testCheckChannelVerificationStatus()
			throws ResidentServiceCheckedException, NoSuchAlgorithmException, ApisResourceAccessException {
		when(residentTransactionRepository.findTopByRefIdAndStatusCodeOrderByCrDtimesDesc(anyString(), anyString()))
				.thenReturn(null);
		VerificationResponseDTO verificationResponseDTO1 = verificationService.checkChannelVerificationStatus("email",
				"8251649601");
		assertEquals(false, verificationResponseDTO1.getResponse().isVerificationStatus());
	}

	@Test
	public void testCheckChannelVerificationStatusIf()
			throws ResidentServiceCheckedException, NoSuchAlgorithmException, ApisResourceAccessException {
		when(residentTransactionRepository.findTopByRefIdAndStatusCodeOrderByCrDtimesDesc(anyString(), anyString()))
				.thenReturn(residentTransactionEntity);
		VerificationResponseDTO verificationResponseDTO = verificationService.checkChannelVerificationStatus("email",
				"8251649601");
		assertEquals(true, verificationResponseDTO.getResponse().isVerificationStatus());
	}
}