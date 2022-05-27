package io.mosip.resident.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.exception.ResidentServiceCheckedException;

/**
 * Resident identity service class.
 * 
 * @author Ritik Jain
 */
@Service
public interface IdentityService {

	/**
	 * Get ID-Repo identity data by ID.
	 * 
	 * @param id
	 * @return IdentityDTO object
	 * @throws ResidentServiceCheckedException
	 */
	public IdentityDTO getIdentity(String id) throws ResidentServiceCheckedException;
	
	public IdentityDTO getIdentity(String id, String langCode) throws ResidentServiceCheckedException;

	public String getIDAToken(String uin);
	
	public String getIDAToken(String uin, String olvPartnerId);

	Map<?, ?> getIdentityAttributes(String id) throws ResidentServiceCheckedException;

	Map<String, ?> getIdentityAttributes(String id, boolean includeUin) throws ResidentServiceCheckedException;
}
