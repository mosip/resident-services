package io.mosip.resident.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.security.NoSuchAlgorithmException;

import io.mosip.resident.util.*;
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

import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.dto.VerificationResponseDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.VerificationService;

import static io.mosip.resident.constant.MappingJsonConstants.EMAIL;
import static io.mosip.resident.constant.MappingJsonConstants.PHONE;

@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class VerificationServiceTest {

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

	private IdentityDTO identityValue;

	@Mock
	private IdentityUtil identityUtil;

	@Mock
	private AvailableClaimUtility availableClaimUtility;

	@Mock
	private MaskDataUtility maskDataUtility;

	@Before
	public void setup() throws Exception {
		when(utility.getIdForResidentTransaction(anyList(), any(), anyString())).thenReturn("hash ref id");
		identityValue = new IdentityDTO();
		identityValue.setEmail("aaa@bbb.com");
		identityValue.setPhone("987654321");
		identityValue.setUIN("123");
		when(identityUtil.getIdentity(Mockito.anyString())).thenReturn(identityValue);
		when(availableClaimUtility.getIDAToken(Mockito.anyString())).thenReturn("123");
	}

	@Test
	public void testCheckChannelVerificationStatus()
			throws ResidentServiceCheckedException, NoSuchAlgorithmException, ApisResourceAccessException {
		when(residentTransactionRepository.existsByRefIdAndStatusCode(anyString(), anyString()))
				.thenReturn(false);
		VerificationResponseDTO verificationResponseDTO1 = verificationService.checkChannelVerificationStatus(EMAIL,
				"8251649601");
		assertEquals(false, verificationResponseDTO1.getResponse().isVerificationStatus());
	}

	@Test
	public void testCheckChannelVerificationStatusIf()
			throws ResidentServiceCheckedException, NoSuchAlgorithmException, ApisResourceAccessException {
		when(residentTransactionRepository.existsByRefIdAndStatusCode(anyString(), anyString()))
				.thenReturn(true);
		VerificationResponseDTO verificationResponseDTO = verificationService.checkChannelVerificationStatus(PHONE,
				"8251649601");
		verificationService.checkChannelVerificationStatus(EMAIL, "8251649601");
		assertEquals(true, verificationResponseDTO.getResponse().isVerificationStatus());
		identityValue.setEmail(null);
		verificationService.checkChannelVerificationStatus(EMAIL, "8251649601");
		identityValue.setPhone(null);
		verificationService.checkChannelVerificationStatus(PHONE, "8251649601");
	}
}