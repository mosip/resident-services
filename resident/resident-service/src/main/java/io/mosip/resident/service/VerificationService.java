package io.mosip.resident.service;

import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Service;

import io.mosip.resident.dto.VerificationResponseDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;

@Service
public interface VerificationService {
	VerificationResponseDTO checkChannelVerificationStatus(String channel, String individualId)
			throws ResidentServiceCheckedException, NoSuchAlgorithmException, ApisResourceAccessException;
}
