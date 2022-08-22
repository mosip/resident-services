package io.mosip.resident.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.dto.AuthTxnDetailsDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;

@Service
public interface IdAuthService {

	public boolean validateOtp(String transactionID, String individualId, String otp)
			throws OtpValidationFailedException;

	public boolean authTypeStatusUpdate(String individualId, List<String> authType,
			AuthTypeStatus authTypeStatus, Long unlockForSeconds) throws ApisResourceAccessException;
	
	public List<AuthTxnDetailsDTO> getAuthHistoryDetails(String individualId,
			String pageStart, String pageFetch) throws ApisResourceAccessException;
	
	public boolean authTypeStatusUpdate(String individualId, Map<String, AuthTypeStatus> authTypeStatusMap, Map<String, Long> unlockForSecondsMap)
			throws ApisResourceAccessException;
}
