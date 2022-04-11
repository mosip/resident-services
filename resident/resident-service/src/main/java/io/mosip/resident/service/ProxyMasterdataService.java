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

}
