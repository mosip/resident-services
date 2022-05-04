package io.mosip.resident.service;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.dto.VerificationResponseDTO;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;

@Service
public interface VerificationService {
    VerificationResponseDTO checkChannelVerificationStatus(String channel, String individualId) throws ResidentServiceCheckedException, NoSuchAlgorithmException;
}
