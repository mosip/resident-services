package io.mosip.resident.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.OrderEnum;
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
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	public ResponseWrapper<?> getValidDocumentByLangCode(String langCode) throws ResidentServiceCheckedException;

	/**
	 * Get location hierarchy levels by language code.
	 * 
	 * @param langCode
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	public ResponseWrapper<?> getLocationHierarchyLevelByLangCode(String langCode)
			throws ResidentServiceCheckedException;

	/**
	 * Get immediate children by location code and language code.
	 * 
	 * @param locationCode
	 * @param langCode
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	public ResponseWrapper<?> getImmediateChildrenByLocCodeAndLangCode(String locationCode, String langCode)
			throws ResidentServiceCheckedException;

	/**
	 * Get location details by location code and language code.
	 * 
	 * @param locationCode
	 * @param langCode
	 * @return ResponseWrapper object
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
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	public ResponseWrapper<?> getCoordinateSpecificRegistrationCenters(String langCode, double longitude,
			double latitude, int proximityDistance) throws ResidentServiceCheckedException;

	/**
	 * Get applicant valid document.
	 * 
	 * @param applicantId
	 * @param languages
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	public ResponseWrapper<?> getApplicantValidDocument(String applicantId, List<String> languages)
			throws ResidentServiceCheckedException;

	/**
	 * Get registration centers by hierarchy level.
	 * 
	 * @param langCode
	 * @param hierarchyLevel
	 * @param name
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	public ResponseWrapper<?> getRegistrationCentersByHierarchyLevel(String langCode, Short hierarchyLevel,
			List<String> name) throws ResidentServiceCheckedException;

	/**
	 * Get registration centers by hierarchy level and text-paginated.
	 * 
	 * @param langCode
	 * @param hierarchyLevel
	 * @param name
	 * @param pageNumber
	 * @param pageSize
	 * @param orderBy
	 * @param sortBy
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	public ResponseWrapper<?> getRegistrationCenterByHierarchyLevelAndTextPaginated(String langCode,
			Short hierarchyLevel, String name, int pageNumber, int pageSize, OrderEnum orderBy, String sortBy)
			throws ResidentServiceCheckedException;

	/**
	 * Get registration center working days by registration center ID.
	 * 
	 * @param registrationCenterID
	 * @param langCode
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	public ResponseWrapper<?> getRegistrationCenterWorkingDays(String registrationCenterID, String langCode)
			throws ResidentServiceCheckedException;

	/**
	 * Get latest ID schema.
	 * 
	 * @param schemaVersion
	 * @param domain
	 * @param type
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	public ResponseWrapper<?> getLatestIdSchema(double schemaVersion, String domain, String type)
			throws ResidentServiceCheckedException;
	
	/**
	 * Get templates by language code and template type code.
	 * 
	 * @param langCode
	 * @param templateTypeCode
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	public ResponseWrapper<?> getAllTemplateBylangCodeAndTemplateTypeCode(String langCode, String templateTypeCode)
			throws ResidentServiceCheckedException;
	
	/**
	 * Get gender types by language code.
	 * 
	 * @param langCode
	 * @return ResponseWrapper object
	 * @throws ResidentServiceCheckedException
	 */
	public ResponseWrapper<?> getGenderTypesByLangCode(String langCode) throws ResidentServiceCheckedException;

	public ResponseWrapper<?> getDocumentTypesByDocumentCategoryAndLangCode(String documentcategorycode, String langCode) throws ResidentServiceCheckedException;
	
}