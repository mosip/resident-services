package io.mosip.resident.batch;

import static io.mosip.resident.constant.EventStatusFailure.FAILED;
import static io.mosip.resident.constant.ResidentConstants.CREDENTIAL_UPDATE_STATUS_UPDATE_INITIAL_DELAY;
import static io.mosip.resident.constant.ResidentConstants.CREDENTIAL_UPDATE_STATUS_UPDATE_INITIAL_DELAY_DEFAULT;
import static io.mosip.resident.constant.ResidentConstants.CREDENTIAL_UPDATE_STATUS_UPDATE_INTERVAL;
import static io.mosip.resident.constant.ResidentConstants.CREDENTIAL_UPDATE_STATUS_UPDATE_INTERVAL_DEFAULT;
import static io.mosip.resident.constant.ResidentConstants.IS_CREDENTIAL_STATUS_UPDATE_JOB_ENABLED;
import static io.mosip.resident.constant.ResidentConstants.NOTIFICATION_DATE_PATTERN;
import static io.mosip.resident.constant.ResidentConstants.NOTIFICATION_TIME_PATTERN;
import static io.mosip.resident.constant.ResidentConstants.NOTIFICATION_ZONE;
import static io.mosip.resident.constant.ResidentConstants.PUBLIC_URL;
import static io.mosip.resident.constant.ResidentConstants.RESIDENT;
import static io.mosip.resident.constant.ResidentConstants.STATUS_CODE;
import static io.mosip.resident.constant.ResidentConstants.URL;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.constant.TemplateType;
import io.mosip.resident.dto.NotificationRequestDtoV2;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.IdRepoAppException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.function.RunnableWithException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utility;

/**
 * @author Manoj SP
 * @author Loganathan S
 *
 */
@Component
@ConditionalOnProperty(name = IS_CREDENTIAL_STATUS_UPDATE_JOB_ENABLED, havingValue = "true", matchIfMissing = true)
public class CredentialStatusUpdateBatchJob {

	private static final String DEFAULT_NOTIF_TIME_PATTERN = "HH:mm:ss";

	private static final String DEFAULT_NOTIF_DATE_PATTERN = "dd-MM-yyyy";

	@Value("${" + PUBLIC_URL + "}")
	private String publicUrl;

	@Value("${" + NOTIFICATION_ZONE + "}")
	private String notificationZone;

	@Value("${" + NOTIFICATION_DATE_PATTERN + ":" + DEFAULT_NOTIF_DATE_PATTERN + "}")
	private String notificationDatePattern;

	@Value("${" + NOTIFICATION_TIME_PATTERN + ":" + DEFAULT_NOTIF_TIME_PATTERN + "}")
	private String notificationTimePattern;

	private final Logger logger = LoggerConfiguration.logConfig(CredentialStatusUpdateBatchJob.class);

	@Autowired
	private ResidentTransactionRepository repo;

	@Autowired
	@Qualifier("restClientWithSelfTOkenRestTemplate")
	private ResidentServiceRestClient residentServiceRestClient;

	@Autowired
	private NotificationService notificationService;

	@Value("#{'${resident.async.request.types}'.split(',')}") 
	private List<String> requestTypeCodesToProcessInBatchJob;
	
	@Autowired
	private Environment env;
	
	@Autowired
	private Utility utility;

	private void handleWithTryCatch(RunnableWithException runnableWithException) {
		try {
			runnableWithException.run();
		} catch (ApisResourceAccessException e) {
			logErrorForBatchJob(e);
		} catch (ResidentServiceCheckedException e) {
			logErrorForBatchJob(e);
		} catch (ResidentServiceException e) {
			logErrorForBatchJob(e);
		} catch (IdRepoAppException e) {
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
				updateEntity(txn);
				saveEntity(txn);
			} else {
				handleWithTryCatch(() -> updateTransactionStatus(txn));
			}
		}
	}

	private List<String> getStatusCodesToProcess() {
		return RequestType.getAllNewOrInprogressStatusList(env);
	}

	private void updateTransactionStatus(ResidentTransactionEntity txn) throws ResidentServiceCheckedException, ApisResourceAccessException {
		String requestTypeCode = txn.getRequestTypeCode();
		if(requestTypeCodesToProcessInBatchJob.contains(requestTypeCode)) {
			RequestType requestType = RequestType.getRequestTypeFromString(requestTypeCode);
			//If it is already a success / failed status, do not process it.
			if (!requestType.isSuccessOrFailedStatus(env, txn.getStatusCode())) {
				Map<String, String> credentialStatus = getCredentialStatusForEntity(txn);
				if (!credentialStatus.isEmpty()) {
					// Save the new status to the resident transaction entity
					String newStatusCode = credentialStatus.get(STATUS_CODE);
					//If the status did not change, don't process it
					if (!txn.getStatusCode().equals(newStatusCode)) {
						logger.debug(String.format("updating status for : %s as %s", txn.getEventId(), newStatusCode));
						txn.setStatusCode(newStatusCode);

						// Save the reference link if any
						String referenceLink = credentialStatus.get(URL);
						if (referenceLink != null) {
							logger.debug(String.format("saving reference link for : %s", txn.getEventId()));
							txn.setReferenceLink(referenceLink);
						}

						// Send Notification
						if (requestType.isNotificationStatus(env, newStatusCode)) {
							logger.debug("invoking notifications for status: " + newStatusCode);
							requestType.preUpdateInBatchJob(env, utility, txn, credentialStatus, newStatusCode);

							// For bell notification
							txn.setReadStatus(false);
							// Email/SMS notification
							Optional<TemplateType> templateType = getTemplateType(requestType, newStatusCode);
							if (templateType.isPresent()) {
								sendNotification(txn, templateType.get(), requestType);
							}
						}

						updateEntity(txn);
						repo.save(txn);
					}
				}
			}
		}
	}

	private Optional<TemplateType> getTemplateType(RequestType requestType, String newStatusCode) {
		Optional<TemplateType> templateType;
		if(requestType.isSuccessStatus(env, newStatusCode)) {
			templateType = Optional.of(TemplateType.SUCCESS);
		} else if(requestType.isFailedStatus(env, newStatusCode)) {
			templateType = Optional.of(TemplateType.FAILURE);
		} else if(requestType.isInProgressStatus(env, newStatusCode)) {
			templateType = Optional.of(TemplateType.IN_PROGRESS);
		} else {
			templateType = Optional.empty();
		}
		return templateType;
	}
	
	private Map<String, String> getCredentialStatusForEntity(ResidentTransactionEntity txn)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		if (txn.getCredentialRequestId() != null && !txn.getCredentialRequestId().isEmpty()) {
			Map<String, String> eventDetails = getCredentialEventDetails(txn.getCredentialRequestId(), txn);
			return eventDetails;
		}
		return Map.of();
	}

	private void saveEntity(ResidentTransactionEntity txn) {
		repo.save(txn);
	}

	private void updateEntity(ResidentTransactionEntity txn) {
		txn.setUpdBy(RESIDENT);
		txn.setUpdDtimes(DateUtils.getUTCCurrentDateTime());
		saveEntity(txn);
	}

	private void sendNotification(ResidentTransactionEntity txn, TemplateType templateType, RequestType requestType)
			throws ResidentServiceCheckedException {
		NotificationRequestDtoV2 notificationRequestDtoV2 = new NotificationRequestDtoV2();
		notificationRequestDtoV2.setTemplateType(templateType);
		notificationRequestDtoV2.setRequestType(requestType);
		notificationRequestDtoV2.setEventId(txn.getEventId());
		notificationRequestDtoV2.setId(txn.getIndividualId());
		notificationService.sendNotification(notificationRequestDtoV2);
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

}
