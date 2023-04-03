package io.mosip.resident.constant;

import java.util.Map;

import org.springframework.core.env.Environment;

import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.util.Utility;

/**
 * The interface for implementing logic to be invoked before updating the status in batch job
 * 
 * @author Loganathan S
 *
 */
public interface PreUpdateInBatchJob {

	/**
	 * Invoked before updating the status in the database and before sending notification
	 * 
	 * @param env
	 * @param utility
	 * @param txn
	 * @param credentialStatus
	 * @param newStatusCode
	 * @throws ResidentServiceCheckedException
	 * @throws ApisResourceAccessException
	 */
	void preUpdateInBatchJob(Environment env, Utility utility, ResidentTransactionEntity txn,
			Map<String, String> credentialStatus, String newStatusCode)
			throws ResidentServiceCheckedException, ApisResourceAccessException;

}