package io.mosip.resident.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;

/**
 * Resident identity service class.
 * 
 * @author Ritik Jain
 */
@Service
public interface IdentityService {

	/**
	 * Get identity data by id.
	 * 
	 * @param id
	 * @return IdentityDTO object
	 * @throws ResidentServiceCheckedException
	 */
	public IdentityDTO getIdentity(String id) throws ResidentServiceCheckedException;

	/**
	 * Get identity data by id, type & langCode.
	 * 
	 * @param id
	 * @param type
	 * @param langCode
	 * @return IdentityDTO object
	 * @throws ResidentServiceCheckedException
	 */
	public IdentityDTO getIdentity(String id, boolean fetchFace, String langCode) throws ResidentServiceCheckedException;

	public String getIDAToken(String uin);

	public String getIDAToken(String uin, String olvPartnerId);

	Map<String, ?> getIdentityAttributes(String id, boolean includeUin) throws ResidentServiceCheckedException;

	/**
	 * Get ID-Repo api data by id.
	 * 
	 * @param id
	 * @return Map
	 * @throws ResidentServiceCheckedException
	 */
	Map<String, ?> getIdentityAttributes(String id) throws ResidentServiceCheckedException;

	/**
	 * Get ID-Repo api data by id, type & includeUin.
	 * 
	 * @param id
	 * @param type
	 * @param includeUin
	 * @return Map
	 * @throws ResidentServiceCheckedException
	 */
	Map<String, ?> getIdentityAttributes(String id, String type, boolean includeUin)
			throws ResidentServiceCheckedException;
			
	public String getResidentIndvidualId() throws ApisResourceAccessException;

}
