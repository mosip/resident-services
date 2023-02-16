package io.mosip.resident.batch;

import com.fasterxml.jackson.core.type.TypeReference;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.constant.TemplateType;
import io.mosip.resident.constant.TemplateVariablesConstants;
import io.mosip.resident.dto.NotificationRequestDtoV2;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.function.RunnableWithException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.IdentityService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ResidentService;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.mosip.resident.constant.CredentialUpdateStatus.FAILED;
import static io.mosip.resident.constant.CredentialUpdateStatus.IN_TRANSIT;
import static io.mosip.resident.constant.CredentialUpdateStatus.ISSUED;
import static io.mosip.resident.constant.CredentialUpdateStatus.NEW;
import static io.mosip.resident.constant.CredentialUpdateStatus.PAYMENT_CONFIRMED;
import static io.mosip.resident.constant.CredentialUpdateStatus.PRINTING;
import static io.mosip.resident.constant.CredentialUpdateStatus.PROCESSING;
import static io.mosip.resident.constant.CredentialUpdateStatus.RECEIVED;
import static io.mosip.resident.constant.RequestType.ORDER_PHYSICAL_CARD;
import static io.mosip.resident.constant.RequestType.SHARE_CRED_WITH_PARTNER;
import static io.mosip.resident.constant.RequestType.UPDATE_MY_UIN;
import static io.mosip.resident.constant.RequestType.VID_CARD_DOWNLOAD;
import static io.mosip.resident.constant.ResidentConstants.CREDENTIAL_UPDATE_STATUS_UPDATE_INITIAL_DELAY;
import static io.mosip.resident.constant.ResidentConstants.CREDENTIAL_UPDATE_STATUS_UPDATE_INITIAL_DELAY_DEFAULT;
import static io.mosip.resident.constant.ResidentConstants.CREDENTIAL_UPDATE_STATUS_UPDATE_INTERVAL;
import static io.mosip.resident.constant.ResidentConstants.CREDENTIAL_UPDATE_STATUS_UPDATE_INTERVAL_DEFAULT;
import static io.mosip.resident.constant.ResidentConstants.DOWNLOAD_CARD;
import static io.mosip.resident.constant.ResidentConstants.IS_CREDENTIAL_STATUS_UPDATE_JOB_ENABLED;
import static io.mosip.resident.constant.ResidentConstants.NOTIFICATION_DATE_PATTERN;
import static io.mosip.resident.constant.ResidentConstants.NOTIFICATION_TIME_PATTERN;
import static io.mosip.resident.constant.ResidentConstants.NOTIFICATION_ZONE;
import static io.mosip.resident.constant.ResidentConstants.PUBLIC_URL;
import static io.mosip.resident.constant.ResidentConstants.RESIDENT;
import static io.mosip.resident.constant.ResidentConstants.STATUS_CODE;

/**
 * @author Manoj SP
 *
 */
@Component
@Transactional
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

	@Autowired
	private ResidentService residentService;

	@Autowired
	private IdentityService identityService;

	@Value("${resident.batchjob.process.status.list}")
	private String statusCodes;

	@Value("${resident.async.request.types}")
	private String requestTypeCodes;

	private void handleWithTryCatch(RunnableWithException runnableWithException) {
		try {
			runnableWithException.run();
		} catch (ApisResourceAccessException e) {
			logErrorForBatchJob(e);
		} catch (ResidentServiceCheckedException e){
			logErrorForBatchJob(e);
		}
	}

	private void logErrorForBatchJob(Exception e) {
		logger.error(String.format("Error in batch job: %s : %s : %s", e.getClass().getSimpleName(), e.getMessage(),
				(e.getCause() != null ? "rootcause: " + e.getCause().getMessage() : "")));
	}

	@Scheduled(initialDelayString = "${" + CREDENTIAL_UPDATE_STATUS_UPDATE_INITIAL_DELAY + ":"
			+ CREDENTIAL_UPDATE_STATUS_UPDATE_INITIAL_DELAY_DEFAULT + "}", fixedDelayString = "${"
					+ CREDENTIAL_UPDATE_STATUS_UPDATE_INTERVAL + ":" + CREDENTIAL_UPDATE_STATUS_UPDATE_INTERVAL_DEFAULT
					+ "}")
	public void scheduleCredentialStatusUpdateJob() throws ResidentServiceCheckedException {
		List<ResidentTransactionEntity> residentTxnList = repo
				.findByStatusCodeInAndRequestTypeCodeInOrderByCrDtimesAsc(List.of(statusCodes.split(",")), List.of(requestTypeCodes.split(",")));
			for (ResidentTransactionEntity txn : residentTxnList) {
				logger.info("Processing event:" + txn.getEventId());
				handleWithTryCatch( () -> updateVidCardDownloadTxnStatus(txn));
				handleWithTryCatch( () -> updateOrderPhysicalCardTxnStatus(txn));
				handleWithTryCatch( () -> updateShareCredentialWithPartnerTxnStatus(txn));
				handleWithTryCatch( () -> updateUinDemoDataUpdateTxnStatus(txn));
			}
		repo.saveAll(residentTxnList);
	}

	private void updateVidCardDownloadTxnStatus(ResidentTransactionEntity txn)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		if (txn.getRequestTypeCode().contentEquals(VID_CARD_DOWNLOAD.name())) {
			trackAndUpdateNewOrIssuedStatus(txn);
			trackAnddownloadPrintingOrReceivedStatus(txn,TemplateType.SUCCESS,RequestType.VID_CARD_DOWNLOAD);//mentioned in sheet and in story also
			trackAndUpdateFailedStatus(txn, TemplateType.FAILURE,RequestType.VID_CARD_DOWNLOAD);
		}
	}

	private void updateOrderPhysicalCardTxnStatus(ResidentTransactionEntity txn)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		if (txn.getRequestTypeCode().contentEquals(ORDER_PHYSICAL_CARD.name())) {
			trackAndUpdateNewOrIssuedStatus(txn);
			trackAndUpdatePaymentConfirmedStatus(txn);
			trackAnddownloadPrintingOrIntransitStatus(txn,TemplateType.SUCCESS,RequestType.ORDER_PHYSICAL_CARD);
			trackAndUpdateFailedStatus(txn, TemplateType.FAILURE, RequestType.ORDER_PHYSICAL_CARD);
		}
	}


	private void updateShareCredentialWithPartnerTxnStatus(ResidentTransactionEntity txn)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		if (txn.getRequestTypeCode().contentEquals(SHARE_CRED_WITH_PARTNER.name())) {
			trackAndUpdateNewOrIssuedStatus(txn);
			trackAndUpdateFailedStatus(txn, TemplateType.FAILURE, RequestType.SHARE_CRED_WITH_PARTNER);
			
		}
	}

	private void updateUinDemoDataUpdateTxnStatus(ResidentTransactionEntity txn)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		if (txn.getRequestTypeCode().contentEquals(UPDATE_MY_UIN.name())) {
			updateStatusForAID(txn);//not mentioned in sheet
			trackAndUpdatePrintingOrReceivedStatus(txn,TemplateType.SUCCESS,RequestType.UPDATE_MY_UIN);
			trackAndUpdateFailedStatus(txn, TemplateType.FAILURE, RequestType.UPDATE_MY_UIN);
		}
	}

	private void trackAndUpdateNewOrIssuedStatus(ResidentTransactionEntity txn)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		if (txn.getStatusCode().contentEquals(NEW) || txn.getStatusCode().contentEquals(ISSUED)) {
			Map<String, String> eventDetails = getCredentialEventDetails(txn.getCredentialRequestId());
			txn.setStatusCode(eventDetails.get(STATUS_CODE));
			txn.setReadStatus(false);
			txn.setUpdBy(RESIDENT);
			txn.setUpdDtimes(DateUtils.getUTCCurrentDateTime());
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

	private void trackAndUpdatePrintingOrReceivedStatus(ResidentTransactionEntity txn,TemplateType templateType,RequestType requestType) throws ResidentServiceCheckedException {
		if (txn.getStatusCode().contentEquals(PRINTING) || txn.getStatusCode().contentEquals(RECEIVED)) {
			createResidentDwldUrlAndNotify(txn,templateType, requestType);
		}
	}
	
	private void trackAnddownloadPrintingOrReceivedStatus(ResidentTransactionEntity txn,TemplateType templateType,RequestType requestType) throws ResidentServiceCheckedException {
		if (txn.getStatusCode().contentEquals(PRINTING)) {
			createResidentDwldUrlAndNotify(txn, templateType,requestType);
		}
	}
	
	private void trackAnddownloadPrintingOrIntransitStatus(ResidentTransactionEntity txn,TemplateType templateType,RequestType requestType) throws ResidentServiceCheckedException, ApisResourceAccessException {
		if (txn.getStatusCode().contentEquals(PRINTING) || txn.getStatusCode().contentEquals(IN_TRANSIT) ) {
		    String trackingId=getTrackingId(txn.getRequestTrnId(),txn.getIndividualId());
		    txn.setTrackingId(trackingId);
			createResidentDwldUrlAndNotify(txn, templateType,requestType);
		}
	}
	
   private void trackAndUpdatePaymentConfirmedStatus(ResidentTransactionEntity txn) throws ResidentServiceCheckedException, ApisResourceAccessException {
		if (txn.getStatusCode().contentEquals(PAYMENT_CONFIRMED)) {
			Map<String, String> eventDetails = getCredentialEventDetails(txn.getCredentialRequestId());
			txn.setStatusCode(eventDetails.get(STATUS_CODE));
			txn.setReadStatus(false);
			txn.setUpdBy(RESIDENT);
			txn.setUpdDtimes(DateUtils.getUTCCurrentDateTime());
		}		
	}

	private void createResidentDwldUrlAndNotify(ResidentTransactionEntity txn, TemplateType templateType,RequestType requestType) throws ResidentServiceCheckedException {
		txn.setReferenceLink(publicUrl.concat(DOWNLOAD_CARD).concat(txn.getEventId()));
		txn.setUpdBy(RESIDENT);
		txn.setUpdDtimes(DateUtils.getUTCCurrentDateTime());
		repo.save(txn);
		NotificationRequestDtoV2 notificationRequestDtoV2= new NotificationRequestDtoV2();
		notificationRequestDtoV2.setTemplateType(templateType);
		notificationRequestDtoV2.setRequestType(requestType);
		notificationRequestDtoV2.setEventId(txn.getEventId());
		notificationRequestDtoV2.setId(txn.getIndividualId());
		notificationService.sendNotification(notificationRequestDtoV2);
	}
	
	private void trackAndUpdateFailedStatus(ResidentTransactionEntity txn, TemplateType templateType,RequestType requestType
			) throws ResidentServiceCheckedException, ApisResourceAccessException {
		if (txn.getStatusCode().contentEquals(FAILED)) {
			Map<String, String> credEventDetails = getCredentialEventDetails(txn.getCredentialRequestId());
			if (credEventDetails.get(STATUS_CODE).contentEquals(FAILED)) {
				NotificationRequestDtoV2 notificationRequestDtoV2= new NotificationRequestDtoV2();
				notificationRequestDtoV2.setTemplateType(templateType);
				notificationRequestDtoV2.setRequestType(requestType);
				notificationRequestDtoV2.setEventId(txn.getEventId());
				notificationRequestDtoV2.setId(txn.getIndividualId());
				notificationService.sendNotification(notificationRequestDtoV2);

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
		if(individualId == null) {
			logger.error("individualId is null");
			throw new ResidentServiceCheckedException(ResidentErrorCode.UNKNOWN_EXCEPTION);
		}
		return identityService.getIdentity(individualId).getFullName();
	}

	private String getAIDStatusFromRegProc(String aid) {
		return residentService.getRidStatus(aid).getRidStatus();
	}

	private Tuple2<String, String> getDateAndTime(LocalDateTime timestamp) {
		ZonedDateTime dateTime = ZonedDateTime.of(timestamp, ZoneId.of("UTC"))
				.withZoneSameInstant(ZoneId.of(notificationZone));
		String date = dateTime.format(DateTimeFormatter.ofPattern(notificationDatePattern));
		String time = dateTime.format(DateTimeFormatter.ofPattern(notificationTimePattern));
		return Tuples.of(date, time);
	}
	
	private String getTrackingId(String transactionId, String individualId)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		Object object = residentServiceRestClient.getApi(ApiName.GET_ORDER_STATUS_URL, List.of(),
				List.of(TemplateVariablesConstants.TRANSACTION_ID, TemplateVariablesConstants.INDIVIDUAL_ID), List.of(transactionId, individualId), ResponseWrapper.class);
		ResponseWrapper<Map<String, String>> responseWrapper = JsonUtil.convertValue(object,
				new TypeReference<ResponseWrapper<Map<String, String>>>() {
				});
		if (Objects.nonNull(responseWrapper.getErrors()) && !responseWrapper.getErrors().isEmpty()) {
			logger.error("ORDER_STATUS_URL returned error " + responseWrapper.getErrors());
			throw new ResidentServiceCheckedException(ResidentErrorCode.UNKNOWN_EXCEPTION);
		}
		return responseWrapper.getResponse().get(TemplateVariablesConstants.TRACKING_ID);
	}

}
