package io.mosip.resident.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.dto.AuthTxnDetailsDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import reactor.util.function.Tuple2;

@Service
public interface IdAuthService {

	public boolean validateOtp(String transactionID, String individualId, String otp)
			throws OtpValidationFailedException, ResidentServiceCheckedException;
	
	public Tuple2<Boolean, String> validateOtpV1(String transactionId, String individualId, String otp)
			throws OtpValidationFailedException, ResidentServiceCheckedException;
	
	public Tuple2<Boolean, ResidentTransactionEntity> validateOtpV2(String transactionId, String individualId, String otp, RequestType requestType)
			throws OtpValidationFailedException;

	public boolean authTypeStatusUpdate(String individualId, List<String> authType,
			AuthTypeStatus authTypeStatus, Long unlockForSeconds) throws ApisResourceAccessException;
	
	public List<AuthTxnDetailsDTO> getAuthHistoryDetails(String individualId,
			String pageStart, String pageFetch) throws ApisResourceAccessException;
	
	public String authTypeStatusUpdate(String individualId, Map<String, AuthTypeStatus> authTypeStatusMap, Map<String, Long> unlockForSecondsMap)
			throws ApisResourceAccessException;

    String authTypeStatusUpdateForRequestId(String individualId, Map<String, AuthTypeStatus> authTypeStatusMap, Map<String, Long> unlockForSecondsMap) throws ApisResourceAccessException;
}
