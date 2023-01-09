package io.mosip.resident.service;

import java.io.IOException;
import java.util.List;
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
	 * Get identity data by id, fetchFace and langCode.
	 * 
	 * @param id
	 * @param fetchFace
	 * @param langCode
	 * @return IdentityDTO object
	 * @throws ResidentServiceCheckedException
	 */
	public IdentityDTO getIdentity(String id, boolean fetchFace, String langCode) throws ResidentServiceCheckedException;

	public String getIDAToken(String uin);

	public String getIDAToken(String uin, String olvPartnerId);

	/**
	 * Get ID-Repo api data by id.
	 *
	 * @param id
	 * @return Map
	 * @throws ResidentServiceCheckedException
	 * @throws IOException
	 */
	Map<String, ?> getIdentityAttributes(String id,String schemaType) throws ResidentServiceCheckedException, IOException;

	/**
	 * Get ID-Repo api data by id, type and includeUin.
	 * 
	 * @param id
	 * @param includeUin
	 * @return Map
	 * @throws ResidentServiceCheckedException
	 */
	Map<String, ?> getIdentityAttributes(String id, boolean includeUin,String schemaType)
			throws ResidentServiceCheckedException;
	
	Map<String, Object> getIdentityAttributes(String id, boolean includeUin, String schemaType, boolean includePhoto)
			throws ResidentServiceCheckedException;
			
	public String getResidentIndvidualId() throws ApisResourceAccessException;

	Map<String, Object> getIdentityAttributes(String id, boolean includeUin, String schemaType, boolean includePhoto,
											  List<String> additionalAttributes) throws ResidentServiceCheckedException;

	String getUinForIndividualId(String idvid) throws ResidentServiceCheckedException;

	String getIDATokenForIndividualId(String idvid) throws ResidentServiceCheckedException;


    String getIndividualIdType(String s);
}
