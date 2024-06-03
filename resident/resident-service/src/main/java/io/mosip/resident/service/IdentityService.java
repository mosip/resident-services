package io.mosip.resident.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;

/**
 * Resident identity service class.
 * 
 * @author Ritik Jain
 */
@Service
public interface IdentityService {

	String createSessionId();

	public String getResidentIdaTokenFromAccessToken(String accessToken) throws ApisResourceAccessException, ResidentServiceCheckedException;
}
