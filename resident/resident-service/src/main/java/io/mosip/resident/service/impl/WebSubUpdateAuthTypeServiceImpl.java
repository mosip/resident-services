package io.mosip.resident.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.EventStatusSuccess;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.constant.TemplateType;
import io.mosip.resident.dto.NotificationRequestDtoV2;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.WebSubUpdateAuthTypeService;
import io.mosip.resident.util.Utility;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Component
public class WebSubUpdateAuthTypeServiceImpl implements WebSubUpdateAuthTypeService {

	private static final Logger logger = LoggerConfiguration.logConfig(WebSubUpdateAuthTypeServiceImpl.class);

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private ResidentTransactionRepository residentTransactionRepository;

	@Autowired
	private Utility utility;

	@Value("${ida.online-verification-partner-id}")
	private String onlineVerificationPartnerId;

	@Override
	public void updateAuthTypeStatus(Map<String, Object> eventModel)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(),
				"WebSubUpdateAuthTypeServiceImpl::updateAuthTypeStatus()::entry");
		try {
			Tuple2<String, String> tupleResponse = updateInResidentTransactionTable(eventModel,
					EventStatusSuccess.COMPLETED.name());
			// only if the event belongs to the current online verification partner, the
			// individualId will not be blank, and hence the notification will be sent
			if (!StringUtils.isBlank(tupleResponse.getT1()) && !StringUtils.isBlank(tupleResponse.getT2())) {
				sendNotificationV2(TemplateType.SUCCESS, tupleResponse.getT1(), tupleResponse.getT2());
			}
		} catch (Exception e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					"WebSubUpdateAuthTypeServiceImpl::updateAuthTypeStatus()::exception");
			Tuple2<String, String> tupleResponse = updateInResidentTransactionTable(eventModel,
					EventStatusSuccess.COMPLETED.name());
			sendNotificationV2(TemplateType.FAILURE, tupleResponse.getT1(), tupleResponse.getT2());
			throw new ResidentServiceCheckedException(
					ResidentErrorCode.RESIDENT_WEBSUB_UPDATE_AUTH_TYPE_FAILED.getErrorCode(),
					ResidentErrorCode.RESIDENT_WEBSUB_UPDATE_AUTH_TYPE_FAILED.getErrorMessage(), e);
		}
	}

	private Tuple2<String, String> updateInResidentTransactionTable(Map<String, Object> eventModel, String status) {

		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(),
				"WebSubUpdateAuthTypeServiceImpl::insertInResidentTransactionTable()::entry");
		String eventId = "";
		String individualId = "";
		List<ResidentTransactionEntity> residentTransactionEntities = List.of();
		try {
			Object eventObj = eventModel.get(ResidentConstants.EVENT);
			if (eventObj instanceof Map) {
				Map<String, Object> eventMap = (Map<String, Object>) eventObj;
				Object dataObject = eventMap.get(ResidentConstants.DATA);
				if (dataObject instanceof Map) {
					Map<String, Object> dataMap = (Map<String, Object>) dataObject;
					Object authStatusListObj = (List<Map<String, Object>>) dataMap.get(ResidentConstants.AUTH_TYPES);
					if (authStatusListObj instanceof List) {
						List<Map<String, Object>> authTypeStatusList = (List<Map<String, Object>>) authStatusListObj;
						residentTransactionEntities = authTypeStatusList.stream()
								.map(authTypeStatus -> (String) authTypeStatus.get(ResidentConstants.REQUEST_ID))
								.filter(Objects::nonNull).distinct()
								.flatMap(authTypeStatusStr -> residentTransactionRepository
										.findByRequestTrnId(authTypeStatusStr).stream())
								.collect(Collectors.toList());
						// Get the values before saving, otherwise individual ID will be updated in
						// encrypted format in the entity
						if (residentTransactionEntities != null && !residentTransactionEntities.isEmpty()) {
							eventId = residentTransactionEntities.stream()
									.filter(entity -> entity.getOlvPartnerId().equals(onlineVerificationPartnerId))
									.map(entity -> entity.getEventId()).findAny()
									.orElse(ResidentConstants.NOT_AVAILABLE);

							individualId = residentTransactionEntities.stream()
									.filter(entity -> entity.getOlvPartnerId().equals(onlineVerificationPartnerId))
									.map(entity -> entity.getIndividualId()).findAny()
									.orElse(ResidentConstants.NOT_AVAILABLE);

							// Update status
							residentTransactionEntities.stream().forEach(residentTransactionEntity -> {
								residentTransactionEntity.setStatusCode(status);
								residentTransactionEntity.setReadStatus(false);
								residentTransactionEntity.setUpdBy(utility.getSessionUserName());
								residentTransactionEntity.setUpdDtimes(DateUtils.getUTCCurrentDateTime());
							});
							residentTransactionRepository.saveAll(residentTransactionEntities);
						} else {
							logger.debug("No records found to update.");
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					"WebSubUpdateAuthTypeServiceImpl::insertInResidentTransactionTable()::exception");
		}
		return Tuples.of(eventId, individualId);
	}

	private NotificationResponseDTO sendNotificationV2(TemplateType templateType, String eventId, String individualId)
			throws ResidentServiceCheckedException {

		NotificationRequestDtoV2 notificationRequestDtoV2 = new NotificationRequestDtoV2();
		notificationRequestDtoV2.setId(individualId);
		notificationRequestDtoV2.setRequestType(RequestType.AUTH_TYPE_LOCK_UNLOCK);
		notificationRequestDtoV2.setTemplateType(templateType);
		notificationRequestDtoV2.setEventId(eventId);

		return notificationService.sendNotification(notificationRequestDtoV2, null);
	}
}
