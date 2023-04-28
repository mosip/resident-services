package io.mosip.resident.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.stereotype.Service;

import io.mosip.resident.exception.ResidentServiceCheckedException;

/**
 * 
 * @author M1063027 Rama Devi
 *
 */
@Service
public interface DownLoadMasterDataService {

	/**
	 * 
	 * @param langCode
	 * @param hierarchyLevel
	 * @param name
	 * @return
	 * @throws ResidentServiceCheckedException
	 */
	public InputStream downloadRegistrationCentersByHierarchyLevel(String langCode, Short hierarchyLevel,
			String name) throws ResidentServiceCheckedException, IOException, Exception;
	
	/**
	 * 
	 * @param langCode
	 * @return
	 * @throws ResidentServiceCheckedException
	 * @throws IOException
	 * @throws Exception
	 */
	public InputStream downloadSupportingDocsByLanguage(String langCode) throws ResidentServiceCheckedException, IOException, Exception; 
	
	/**
	 * 
	 * @param langCode
	 * @param longitude
	 * @param latitude
	 * @param proximityDistance
	 * @return
	 * @throws ResidentServiceCheckedException
	 */
	public InputStream getNearestRegistrationcenters(String langCode, double longitude, double latitude,
			int proximityDistance) throws ResidentServiceCheckedException, IOException, Exception;

}
