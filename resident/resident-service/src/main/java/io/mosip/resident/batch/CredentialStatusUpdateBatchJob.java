package io.mosip.resident.batch;

import static io.mosip.resident.constant.EventStatusFailure.FAILED;
import static io.mosip.resident.constant.ResidentConstants.CREDENTIAL_UPDATE_STATUS_UPDATE_INITIAL_DELAY;
import static io.mosip.resident.constant.ResidentConstants.CREDENTIAL_UPDATE_STATUS_UPDATE_INITIAL_DELAY_DEFAULT;
import static io.mosip.resident.constant.ResidentConstants.CREDENTIAL_UPDATE_STATUS_UPDATE_INTERVAL;
import static io.mosip.resident.constant.ResidentConstants.CREDENTIAL_UPDATE_STATUS_UPDATE_INTERVAL_DEFAULT;
import static io.mosip.resident.constant.ResidentConstants.IS_CREDENTIAL_STATUS_UPDATE_JOB_ENABLED;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.IdRepoAppException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.function.RunnableWithException;
import io.mosip.resident.helper.CredentialStatusUpdateHelper;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;

/**
 * @author Manoj SP
 * @author Loganathan S
 *
 */
@Component
@ConditionalOnProperty(name = IS_CREDENTIAL_STATUS_UPDATE_JOB_ENABLED, havingValue = "true", matchIfMissing = true)
public class CredentialStatusUpdateBatchJob {

	private final Logger logger = LoggerConfiguration.logConfig(CredentialStatusUpdateBatchJob.class);
	

	@Autowired
	private ResidentTransactionRepository repo;
	
	@Value("#{'${resident.async.request.types}'.split(',')}")
	private List<String> requestTypeCodesToProcessInBatchJob;
	
	@Autowired
	private CredentialStatusUpdateHelper credentialStatusUpdateHelper;
	
	@Autowired
	private Environment env;
	
	@Autowired
	@Qualifier("restClientWithSelfTOkenRestTemplate")
	private ResidentServiceRestClient residentServiceRestClient;

	private void handleWithTryCatch(RunnableWithException runnableWithException) {
		try {
			runnableWithException.run();
		} catch (ApisResourceAccessException | ResidentServiceCheckedException | ResidentServiceException |
				 IdRepoAppException e) {
			logErrorForBatchJob(e);
		}
	}

	private void logErrorForBatchJob(Exception e) {
		logger.debug(String.format("Error in batch job: %s : %s : %s", e.getClass().getSimpleName(), e.getMessage(),
				(e.getCause() != null ? "rootcause: " + e.getCause().getMessage() : "")));
	}

	@Scheduled(initialDelayString = "${" + CREDENTIAL_UPDATE_STATUS_UPDATE_INITIAL_DELAY + ":"
			+ CREDENTIAL_UPDATE_STATUS_UPDATE_INITIAL_DELAY_DEFAULT + "}", fixedDelayString = "${"
					+ CREDENTIAL_UPDATE_STATUS_UPDATE_INTERVAL + ":" + CREDENTIAL_UPDATE_STATUS_UPDATE_INTERVAL_DEFAULT
					+ "}")
	public void scheduleCredentialStatusUpdateJob() throws ResidentServiceCheckedException {
		List<ResidentTransactionEntity> residentTxnList = repo.findByStatusCodeInAndRequestTypeCodeInAndCredentialRequestIdIsNotNullOrderByCrDtimesAsc(
				getStatusCodesToProcess(), requestTypeCodesToProcessInBatchJob);
		logger.debug("Total records picked from resident_transaction table for processing is " + residentTxnList.size());
		for (ResidentTransactionEntity txn : residentTxnList) {
			logger.debug("Processing event:" + txn.getEventId());
			if (txn.getIndividualId() == null) {
				txn.setStatusCode(FAILED.name());
				txn.setStatusComment("individualId is null");
				credentialStatusUpdateHelper.updateEntity(txn);
				credentialStatusUpdateHelper.saveEntity(txn);
			} else {
				handleWithTryCatch(() -> updateTransactionStatus(txn));
			}
		}
	}
	
	public void updateTransactionStatus(ResidentTransactionEntity txn)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		String requestTypeCode = txn.getRequestTypeCode();
		if (requestTypeCodesToProcessInBatchJob.contains(requestTypeCode)) {
			RequestType requestType = RequestType.getRequestTypeFromString(requestTypeCode);
			// If it is already a success / failed status, do not process it.
			if (!requestType.isSuccessOrFailedStatus(env, txn.getStatusCode())) {
				Map<String, String> credentialStatus = getCredentialStatusForEntity(txn);
				credentialStatusUpdateHelper.updateStatus(txn, credentialStatus);
			}
		}
	}
	
	public List<String> getStatusCodesToProcess() {
		return RequestType.getAllNewOrInprogressStatusList(env);
	}
	
	private Map<String, String> getCredentialEventDetails(String credentialRequestId, ResidentTransactionEntity txn)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		Object object = residentServiceRestClient.getApi(ApiName.CREDENTIAL_STATUS_URL, List.of(credentialRequestId),
				Collections.emptyList(), Collections.emptyList(), ResponseWrapper.class);
		ResponseWrapper<Map<String, String>> responseWrapper = JsonUtil.convertValue(object,
				new TypeReference<ResponseWrapper<Map<String, String>>>() {
				});
		List<ServiceError> errors = responseWrapper.getErrors();
		if (Objects.nonNull(errors) && !errors.isEmpty()) {
			logger.debug("CREDENTIAL_STATUS_URL returned error " + errors);
			throw new ResidentServiceCheckedException(ResidentErrorCode.UNKNOWN_EXCEPTION);
		}
		return responseWrapper.getResponse();
	}

	private Map<String, String> getCredentialStatusForEntity(ResidentTransactionEntity txn)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		if (txn.getCredentialRequestId() != null && !txn.getCredentialRequestId().isEmpty()) {
			Map<String, String> eventDetails = getCredentialEventDetails(txn.getCredentialRequestId(), txn);
			return eventDetails;
		}
		return Map.of();
	}

}
