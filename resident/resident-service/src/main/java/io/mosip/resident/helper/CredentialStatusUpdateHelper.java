package io.mosip.resident.helper;

import static io.mosip.resident.constant.ResidentConstants.NOTIFICATION_DATE_PATTERN;
import static io.mosip.resident.constant.ResidentConstants.NOTIFICATION_TIME_PATTERN;
import static io.mosip.resident.constant.ResidentConstants.NOTIFICATION_ZONE;
import static io.mosip.resident.constant.ResidentConstants.PUBLIC_URL;
import static io.mosip.resident.constant.ResidentConstants.RESIDENT;
import static io.mosip.resident.constant.ResidentConstants.STATUS;
import static io.mosip.resident.constant.ResidentConstants.STATUS_CODE;
import static io.mosip.resident.constant.ResidentConstants.URL;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.TemplateType;
import io.mosip.resident.dto.NotificationRequestDtoV2;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.util.Utility;

/**
 * Helper to update the resident transaction status based on the credential
 * status and to send bell/email/sms notifications accordingly
 * 
 * @author Loganathan S
 *
 */
@Component
public class CredentialStatusUpdateHelper {
	
	private final Logger logger = LoggerConfiguration.logConfig(CredentialStatusUpdateHelper.class);

	private static final String DEFAULT_NOTIF_DATE_PATTERN = "dd-MM-yyyy";
	private static final String DEFAULT_NOTIF_TIME_PATTERN = "HH:mm:ss";
	
	@Autowired
	private Environment env;
	
	@Value("${" + NOTIFICATION_DATE_PATTERN + ":" + DEFAULT_NOTIF_DATE_PATTERN + "}")
	private String notificationDatePattern;
	
	@Autowired
	private NotificationService notificationService;
	
	@Value("${" + NOTIFICATION_TIME_PATTERN + ":" + DEFAULT_NOTIF_TIME_PATTERN + "}")
	private String notificationTimePattern;
	
	@Value("${" + NOTIFICATION_ZONE + "}")
	private String notificationZone;
	
	@Value("${" + PUBLIC_URL + "}")
	private String publicUrl;
	
	@Autowired
	private ResidentTransactionRepository repo;
	
	@Value("#{'${resident.async.request.types}'.split(',')}")
	private List<String> requestTypeCodesToProcessInBatchJob;
	
	@Autowired
	private Utility utility;

	private Optional<TemplateType> getTemplateType(RequestType requestType, String newStatusCode) {
		Optional<TemplateType> templateType;
		if (requestType.isSuccessStatus(env, newStatusCode)) {
			templateType = Optional.of(TemplateType.SUCCESS);
		} else if (requestType.isFailedStatus(env, newStatusCode)) {
			templateType = Optional.of(TemplateType.FAILURE);
		} else if (requestType.isInProgressStatus(env, newStatusCode)) {
			templateType = Optional.of(TemplateType.IN_PROGRESS);
		} else {
			templateType = Optional.empty();
		}
		return templateType;
	}

	public void saveEntity(ResidentTransactionEntity txn) {
		repo.save(txn);
	}

	private void sendNotification(ResidentTransactionEntity txn, TemplateType templateType, RequestType requestType)
			throws ResidentServiceCheckedException {
		NotificationRequestDtoV2 notificationRequestDtoV2 = new NotificationRequestDtoV2();
		notificationRequestDtoV2.setTemplateType(templateType);
		notificationRequestDtoV2.setRequestType(requestType);
		notificationRequestDtoV2.setEventId(txn.getEventId());
		notificationRequestDtoV2.setId(txn.getIndividualId());
		notificationService.sendNotification(notificationRequestDtoV2, null);
	}

	public void updateEntity(ResidentTransactionEntity txn) {
		txn.setUpdBy(RESIDENT);
		txn.setUpdDtimes(DateUtils.getUTCCurrentDateTime());
		saveEntity(txn);
	}

	public void updateStatus(ResidentTransactionEntity txn, Map<String, String> credentialStatus)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		String requestTypeCode = txn.getRequestTypeCode();
		RequestType requestType = RequestType.getRequestTypeFromString(requestTypeCode);

		if (!credentialStatus.isEmpty()) {
			// Save the new status to the resident transaction entity
			String newStatusCode = getStatusCode(credentialStatus);
			// If the status did not change, don't process it
			if (newStatusCode != null && !txn.getStatusCode().equals(newStatusCode)) {
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
					logger.debug(String.format("invoking notifications for status: %s", newStatusCode));
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

	private String getStatusCode(Map<String, String> credentialStatus) {
		String status = credentialStatus.get(STATUS);
		return status == null ? credentialStatus.get(STATUS_CODE) : status;
	}

}
