package io.mosip.resident.constant;

import java.util.Map;

import org.springframework.core.env.Environment;

import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.util.Utility;

public interface PreUpdateInBatchJob {

	void preUpdateInBatchJob(Environment env, Utility utility, ResidentTransactionEntity txn,
			Map<String, String> credentialStatus, String newStatusCode)
			throws ResidentServiceCheckedException, ApisResourceAccessException;

}