package io.mosip.resident.batch;

import static io.mosip.resident.constant.CredentialUpdateStatus.DELIVERED;
import static io.mosip.resident.constant.CredentialUpdateStatus.FAILED;
import static io.mosip.resident.constant.CredentialUpdateStatus.ISSUED;
import static io.mosip.resident.constant.CredentialUpdateStatus.NEW;
import static io.mosip.resident.constant.CredentialUpdateStatus.PRINTING;
import static io.mosip.resident.constant.CredentialUpdateStatus.PROCESSING;
import static io.mosip.resident.constant.CredentialUpdateStatus.RECEIVED;
import static io.mosip.resident.constant.CredentialUpdateStatus.STORED;
import static io.mosip.resident.constant.NotificationTemplateCode.DOWNLOAD_PERSONALIZED_CARD_FAILED;
import static io.mosip.resident.constant.NotificationTemplateCode.DOWNLOAD_PERSONALIZED_CARD_SUCCESS;
import static io.mosip.resident.constant.NotificationTemplateCode.ORDER_PHYSICAL_CARD_FAILED;
import static io.mosip.resident.constant.NotificationTemplateCode.SHARE_CREDENTIAL_FAILED;
import static io.mosip.resident.constant.NotificationTemplateCode.UIN_UPDATE_FAILED;
import static io.mosip.resident.constant.NotificationTemplateCode.UIN_UPDATE_PRINTING;
import static io.mosip.resident.constant.RequestType.DOWNLOAD_PERSONALIZED_CARD;
import static io.mosip.resident.constant.RequestType.ORDER_PHYSICAL_CARD;
import static io.mosip.resident.constant.RequestType.SHARE_CRED_WITH_PARTNER;
import static io.mosip.resident.constant.RequestType.UPDATE_MY_UIN;
import static io.mosip.resident.constant.RequestType.VID_CARD_DOWNLOAD;
import static io.mosip.resident.constant.ResidentConstants.CREDENTIAL_UPDATE_STATUS_UPDATE_INITIAL_DELAY;
import static io.mosip.resident.constant.ResidentConstants.CREDENTIAL_UPDATE_STATUS_UPDATE_INITIAL_DELAY_DEFAULT;
import static io.mosip.resident.constant.ResidentConstants.CREDENTIAL_UPDATE_STATUS_UPDATE_INTERVAL;
import static io.mosip.resident.constant.ResidentConstants.CREDENTIAL_UPDATE_STATUS_UPDATE_INTERVAL_DEFAULT;
import static io.mosip.resident.constant.ResidentConstants.DATE;
import static io.mosip.resident.constant.ResidentConstants.DOWNLOAD_CARD;
import static io.mosip.resident.constant.ResidentConstants.DOWNLOAD_LINK;
import static io.mosip.resident.constant.ResidentConstants.EVENT_DETAILS;
import static io.mosip.resident.constant.ResidentConstants.EVENT_ID;
import static io.mosip.resident.constant.ResidentConstants.IS_CREDENTIAL_STATUS_UPDATE_JOB_ENABLED;
import static io.mosip.resident.constant.ResidentConstants.NAME;
import static io.mosip.resident.constant.ResidentConstants.NOTIFICATION_ZONE;
import static io.mosip.resident.constant.ResidentConstants.PUBLIC_URL;
import static io.mosip.resident.constant.ResidentConstants.RESIDENT;
import static io.mosip.resident.constant.ResidentConstants.STATUS_CODE;
import static io.mosip.resident.constant.ResidentConstants.TIME;
import static io.mosip.resident.constant.ResidentConstants.TRACK_SERVICE_REQUEST_LINK;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.transaction.Transactional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.NotificationTemplateCode;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.NotificationRequestDto;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.IdentityService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ResidentService;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author Manoj SP
 *
 */
@Component
@Transactional
@ConditionalOnProperty(name = IS_CREDENTIAL_STATUS_UPDATE_JOB_ENABLED, havingValue = "true", matchIfMissing = true)
public class CredentialStatusUpdateBatchJob {

	@Value("${" + PUBLIC_URL + "}")
	private String publicUrl;

	@Value("${" + NOTIFICATION_ZONE + "}")
	private String notificationZone;

	private final Logger logger = LoggerConfiguration.logConfig(CredentialStatusUpdateBatchJob.class);

	@Autowired
	private ResidentTransactionRepository repo;

	@Autowired
	@Qualifier("restClientWithSelfTOkenRestTemplate")
	private ResidentServiceRestClient residentServiceRestClient;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private ResidentService residentService;

	@Autowired
	private IdentityService identityService;

	@Scheduled(initialDelayString = "${" + CREDENTIAL_UPDATE_STATUS_UPDATE_INITIAL_DELAY + ":"
			+ CREDENTIAL_UPDATE_STATUS_UPDATE_INITIAL_DELAY_DEFAULT + "}", fixedDelayString = "${"
					+ CREDENTIAL_UPDATE_STATUS_UPDATE_INTERVAL + ":" + CREDENTIAL_UPDATE_STATUS_UPDATE_INTERVAL_DEFAULT
					+ "}")
	public void scheduleCredentialStatusUpdateJob() throws ResidentServiceCheckedException {
		List<ResidentTransactionEntity> residentTxnList = repo
				.findByStatusCodeIn(List.of(NEW, ISSUED, RECEIVED, PRINTING, FAILED, DELIVERED));
		for (ResidentTransactionEntity txn : residentTxnList) {
			try {
				updateDownloadPersonalizedCardTxnStatus(txn);
				updateVidCardDownloadTxnStatus(txn);
				updateOrderPhysicalCardTxnStatus(txn);
				updateShareCredentialWithPartnerTxnStatus(txn);
				updateUinDemoDataUpdateTxnStatus(txn);
			} catch (ApisResourceAccessException e) {
				logger.error(ExceptionUtils.getStackTrace(e));
				throw new ResidentServiceCheckedException(ResidentErrorCode.UNKNOWN_EXCEPTION, e);
			}
		}
		repo.saveAll(residentTxnList);
	}

	private void updateDownloadPersonalizedCardTxnStatus(ResidentTransactionEntity txn)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		if (txn.getRequestTypeCode().contentEquals(DOWNLOAD_PERSONALIZED_CARD.name())) {
			trackAndUpdateNewOrIssuedStatus(txn);
			trackAndUpdateStoredStatus(txn);
			trackAndUpdateFailedStatus(txn, DOWNLOAD_PERSONALIZED_CARD_FAILED, "customize and download card");
		}
	}

	private void updateVidCardDownloadTxnStatus(ResidentTransactionEntity txn)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		if (txn.getRequestTypeCode().contentEquals(VID_CARD_DOWNLOAD.name())) {
			trackAndUpdateNewOrIssuedStatus(txn);
			trackAndUpdateStoredStatus(txn);
			trackAndUpdateFailedStatus(txn, DOWNLOAD_PERSONALIZED_CARD_FAILED, "vid card download");
		}
	}

	private void updateOrderPhysicalCardTxnStatus(ResidentTransactionEntity txn)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		if (txn.getRequestTypeCode().contentEquals(ORDER_PHYSICAL_CARD.name())) {
			trackAndUpdateNewOrIssuedStatus(txn);
			trackAndUpdateFailedStatus(txn, ORDER_PHYSICAL_CARD_FAILED, "Order physical card");
		}
	}

	private void updateShareCredentialWithPartnerTxnStatus(ResidentTransactionEntity txn)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		if (txn.getRequestTypeCode().contentEquals(SHARE_CRED_WITH_PARTNER.name())) {
			trackAndUpdateNewOrIssuedStatus(txn);
			trackAndUpdateFailedStatus(txn, SHARE_CREDENTIAL_FAILED, "share credential with partner");
		}
	}

	private void updateUinDemoDataUpdateTxnStatus(ResidentTransactionEntity txn)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		if (txn.getRequestTypeCode().contentEquals(UPDATE_MY_UIN.name())) {
			updateStatusForAID(txn);
			trackAndUpdatePrintingOrReceivedStatus(txn, UIN_UPDATE_PRINTING);
			trackAndUpdateFailedStatus(txn, UIN_UPDATE_FAILED, "update uin demographic data");
		}
	}

	private void trackAndUpdateNewOrIssuedStatus(ResidentTransactionEntity txn)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		if (txn.getStatusCode().contentEquals(NEW) || txn.getStatusCode().contentEquals(ISSUED)) {
			Map<String, String> eventDetails = getCredentialEventDetails(txn.getEventId());
			txn.setStatusCode(eventDetails.get(STATUS_CODE));
			txn.setReadStatus(false);
			txn.setUpdBy(RESIDENT);
			txn.setUpdDtimes(DateUtils.getUTCCurrentDateTime());
		}
	}

	private void trackAndUpdateStoredStatus(ResidentTransactionEntity txn) throws ResidentServiceCheckedException {
		if (txn.getStatusCode().contentEquals(STORED)) {
			createResidentDwldUrlAndNotify(txn, DOWNLOAD_PERSONALIZED_CARD_SUCCESS, "customize and download card");
		}

	}

	private void updateStatusForAID(ResidentTransactionEntity txn) throws ResidentServiceCheckedException {
		if (isRecordAvailableInIdRepo(txn.getAid())) {
			txn.setStatusCode(PROCESSING);
			txn.setReadStatus(false);
			txn.setUpdBy(RESIDENT);
			txn.setUpdDtimes(DateUtils.getUTCCurrentDateTime());
		} else {
			txn.setStatusCode(getAIDStatusFromRegProc(txn.getAid()));
			txn.setReadStatus(false);
			txn.setUpdBy(RESIDENT);
			txn.setUpdDtimes(DateUtils.getUTCCurrentDateTime());
		}
	}

	private void trackAndUpdatePrintingOrReceivedStatus(ResidentTransactionEntity txn,
			NotificationTemplateCode templateCode) throws ResidentServiceCheckedException {
		if (txn.getStatusCode().contentEquals(PRINTING) || txn.getStatusCode().contentEquals(RECEIVED)) {
			createResidentDwldUrlAndNotify(txn, templateCode, "update uin demographic data");
		}
	}

	private void createResidentDwldUrlAndNotify(ResidentTransactionEntity txn, NotificationTemplateCode templateCode,
			String eventDetails) throws ResidentServiceCheckedException {
		txn.setReferenceLink(publicUrl.concat(DOWNLOAD_CARD).concat(txn.getAid()));
		txn.setUpdBy(RESIDENT);
		txn.setUpdDtimes(DateUtils.getUTCCurrentDateTime());
		Tuple2<String, String> dateAndTime = getDateAndTime(
				DateUtils.parseToLocalDateTime(DateUtils.getUTCCurrentDateTimeString()));
		notificationService.sendNotification(new NotificationRequestDto(txn.getAid(), templateCode,
				Map.of(NAME, getNameForIndividualId(txn.getIndividualId()), EVENT_DETAILS, eventDetails, DATE,
						dateAndTime.getT1(), TIME, dateAndTime.getT2(), EVENT_ID, txn.getEventId(), DOWNLOAD_LINK,
						txn.getReferenceLink())));
	}

	private void trackAndUpdateFailedStatus(ResidentTransactionEntity txn, NotificationTemplateCode templateCode,
			String eventDetails) throws ResidentServiceCheckedException, ApisResourceAccessException {
		if (txn.getStatusCode().contentEquals(FAILED)) {
			Map<String, String> credEventDetails = getCredentialEventDetails(txn.getEventId());
			if (credEventDetails.get(STATUS_CODE).contentEquals(FAILED)) {
				Tuple2<String, String> dateAndTime = getDateAndTime(
						DateUtils.parseToLocalDateTime(DateUtils.getUTCCurrentDateTimeString()));
				notificationService.sendNotification(new NotificationRequestDto(txn.getAid(), templateCode,
						Map.of(NAME, getNameForIndividualId(txn.getIndividualId()), EVENT_DETAILS, eventDetails, DATE,
								dateAndTime.getT1(), TIME, dateAndTime.getT2(), EVENT_ID, txn.getEventId(),
								TRACK_SERVICE_REQUEST_LINK, txn.getReferenceLink())));
			}
		}
	}

	private Map<String, String> getCredentialEventDetails(String eventId)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		Object object = residentServiceRestClient.getApi(ApiName.CREDENTIAL_STATUS_URL, List.of(eventId),
				Collections.emptyList(), Collections.emptyList(), ResponseWrapper.class);
		ResponseWrapper<Map<String, String>> responseWrapper = JsonUtil.convertValue(object,
				new TypeReference<ResponseWrapper<Map<String, String>>>() {
				});
		if (Objects.nonNull(responseWrapper.getErrors()) && !responseWrapper.getErrors().isEmpty()) {
			logger.error("CREDENTIAL_STATUS_URL returned error " + responseWrapper.getErrors());
			throw new ResidentServiceCheckedException(ResidentErrorCode.UNKNOWN_EXCEPTION);
		}
		return responseWrapper.getResponse();
	}

	private boolean isRecordAvailableInIdRepo(String individualId) throws ResidentServiceCheckedException {
		try {
			getNameForIndividualId(individualId);
		} catch (ResidentServiceCheckedException e) {
			logger.error("individualId not available in IDRepo");
			return false;
		}
		return true;
	}

	private String getNameForIndividualId(String individualId) throws ResidentServiceCheckedException {
		return identityService.getIdentity(individualId).getFullName();
	}

	private String getAIDStatusFromRegProc(String aid) {
		return residentService.getRidStatus(aid).getRidStatus();
	}

	private Tuple2<String, String> getDateAndTime(LocalDateTime timestamp) {
		ZonedDateTime dateTime = ZonedDateTime.of(timestamp, ZoneId.of("UTC"))
				.withZoneSameInstant(ZoneId.of(notificationZone));
		String date = dateTime.format(DateTimeFormatter.ofPattern(notificationZone));
		String time = dateTime.format(DateTimeFormatter.ofPattern(notificationZone));
		return Tuples.of(date, time);
	}

}
