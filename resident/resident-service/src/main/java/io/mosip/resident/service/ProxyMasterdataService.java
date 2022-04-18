package io.mosip.resident.service;

import org.springframework.stereotype.Service;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.exception.ResidentServiceCheckedException;

/**
 * Resident proxy masterdata service class.
 * 
 * @author Ritik Jain
 */
@Service
public interface ProxyMasterdataService {

	/**
	 * Get valid documents by language code.
	 * 
	 * @param langCode
	 * @return ResponseWrapper<?> object
	 * @throws ResidentServiceCheckedException
	 */
	public ResponseWrapper<?> getValidDocumentByLangCode(String langCode) throws ResidentServiceCheckedException;

	/**
	 * Get location hierarchy levels by language code.
	 * 
	 * @param langCode
	 * @return ResponseWrapper<?> object
	 * @throws ResidentServiceCheckedException
	 */
	public ResponseWrapper<?> getLocationHierarchyLevelByLangCode(String langCode)
			throws ResidentServiceCheckedException;

	/**
	 * Get immediate children by location code and language code.
	 * 
	 * @param locationCode
	 * @param langCode
	 * @return ResponseWrapper<?> object
	 * @throws ResidentServiceCheckedException
	 */
	public ResponseWrapper<?> getImmediateChildrenByLocCodeAndLangCode(String locationCode, String langCode)
			throws ResidentServiceCheckedException;

	/**
	 * Get location details by location code and language code.
	 * 
	 * @param locationCode
	 * @param langCode
	 * @return ResponseWrapper<?> object
	 * @throws ResidentServiceCheckedException
	 */
	public ResponseWrapper<?> getLocationDetailsByLocCodeAndLangCode(String locationCode, String langCode)
			throws ResidentServiceCheckedException;

	/**
	 * Get coordinate specific registration centers
	 * 
	 * @param langCode
	 * @param longitude
	 * @param latitude
	 * @param proximityDistance
	 * @return ResponseWrapper<?> object
	 * @throws ResidentServiceCheckedException
	 */
	public ResponseWrapper<?> getCoordinateSpecificRegistrationCenters(String langCode, String longitude,
			String latitude, String proximityDistance) throws ResidentServiceCheckedException;

	/**
	 * Get applicant valid document.
	 * 
	 * @param applicantId
	 * @param languages
	 * @return ResponseWrapper<?> object
	 * @throws ResidentServiceCheckedException
	 */
	public ResponseWrapper<?> getApplicantValidDocument(String applicantId, String languages)
			throws ResidentServiceCheckedException;

	/**
	 * Get registration centers by hierarchy level.
	 * 
	 * @param langCode
	 * @param hierarchyLevel
	 * @param name
	 * @return ResponseWrapper<?> object
	 * @throws ResidentServiceCheckedException
	 */
	public ResponseWrapper<?> getRegistrationCentersByHierarchyLevel(String langCode, String hierarchyLevel,
			String name) throws ResidentServiceCheckedException;

}
