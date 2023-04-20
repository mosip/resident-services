package io.mosip.resident.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;

/**
 * Service to update the resident transaction status from the credential status
 * update in the websub event.
 * 
 * @author Loganathan S
 *
 */
@Service
public interface WebSubCredentialStatusUpdateService {
    public void updateCredentialStatus(Map<String, Object> eventModel) throws ResidentServiceCheckedException, ApisResourceAccessException;
}
