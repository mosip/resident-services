package io.mosip.resident.service.impl;

import static io.mosip.resident.constant.ResidentConstants.RESIDENT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.kernel.core.websub.model.EventModel;
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
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Component
public class WebSubUpdateAuthTypeServiceImpl implements WebSubUpdateAuthTypeService {

    private static final Logger logger = LoggerConfiguration.logConfig(WebSubUpdateAuthTypeServiceImpl.class);

    private static final String AUTH_TYPES = "authTypes";
    private static final String REQUEST_ID = "requestId";

    @Autowired
    private AuditUtil auditUtil;

    @Autowired
	private NotificationService notificationService;

    @Autowired
    private ResidentTransactionRepository residentTransactionRepository;

    @Value("${ida.online-verification-partner-id}")
	private String onlineVerificationPartnerId;

    @Override
    public void updateAuthTypeStatus(EventModel eventModel) throws ResidentServiceCheckedException, ApisResourceAccessException {
        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "WebSubUpdateAuthTypeServiceImpl::updateAuthTypeStatus()::entry");
        auditUtil.setAuditRequestDto(EventEnum.UPDATE_AUTH_TYPE_STATUS);
        try{
			logger.info("WebSubUpdateAuthTypeServiceImpl::updateAuthTypeStatus()::partnerId");
			Tuple2<String, String> tupleResponse = updateInResidentTransactionTable(eventModel, EventStatusSuccess.COMPLETED.name());
			// only if the event belongs to the current online verification partner, the
			// individualId will not be blank, and hence the notification will be sent
			if(!StringUtils.isBlank(tupleResponse.getT1()) && !StringUtils.isBlank(tupleResponse.getT2())) {
				sendNotificationV2(TemplateType.SUCCESS, tupleResponse.getT1(), tupleResponse.getT2());
			}
        }
        catch (Exception e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					"WebSubUpdateAuthTypeServiceImpl::updateAuthTypeStatus()::exception");
			Tuple2<String, String> tupleResponse = updateInResidentTransactionTable(eventModel, EventStatusSuccess.COMPLETED.name());
			sendNotificationV2(TemplateType.FAILURE, tupleResponse.getT1(), tupleResponse.getT2());
			throw new ResidentServiceCheckedException(
					ResidentErrorCode.RESIDENT_WEBSUB_UPDATE_AUTH_TYPE_FAILED.getErrorCode(),
					ResidentErrorCode.RESIDENT_WEBSUB_UPDATE_AUTH_TYPE_FAILED.getErrorMessage(), e);
        }
    }

    private Tuple2<String, String> updateInResidentTransactionTable(EventModel eventModel,  String status) {

        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "WebSubUpdateAuthTypeServiceImpl::insertInResidentTransactionTable()::entry");
		String eventId = "";
		String individualId = "";
        List<ResidentTransactionEntity> residentTransactionEntities = List.of();
        try {
            List<Map<String, Object>> authTypeStatusList = (List<Map<String, Object>>) eventModel.getEvent().getData().get(AUTH_TYPES);
            
            residentTransactionEntities = authTypeStatusList.stream()
            				.flatMap(authTypeStatus -> residentTransactionRepository.findByRequestTrnId((String)authTypeStatus.get(REQUEST_ID)).stream())
            				.distinct()
            				.collect(Collectors.toList());
            //Get the values before saving, otherwise individual ID will be updated in encrypted format in the entity
            if (residentTransactionEntities != null && !residentTransactionEntities.isEmpty()) {
				eventId = residentTransactionEntities.stream()
						.filter(entity -> entity.getOlvPartnerId().equals(onlineVerificationPartnerId))
						.map(entity -> entity.getEventId())
						.findAny()
						.orElse(ResidentConstants.NOT_AVAILABLE);
				
				individualId = residentTransactionEntities.stream()
						.filter(entity -> entity.getOlvPartnerId().equals(onlineVerificationPartnerId))
						.map(entity -> entity.getIndividualId())
						.findAny()
						.orElse(ResidentConstants.NOT_AVAILABLE);
			}
            
            //Update status
			if (residentTransactionEntities != null) {
				residentTransactionEntities.stream().forEach(residentTransactionEntity -> {
					residentTransactionEntity.setStatusCode(status);
					residentTransactionEntity.setReadStatus(false);
					residentTransactionEntity.setUpdBy(RESIDENT);
					residentTransactionEntity.setUpdDtimes(DateUtils.getUTCCurrentDateTime());

				});
			}
			residentTransactionRepository.saveAll(residentTransactionEntities);
        }
        catch (Exception e) {
            logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                    LoggerFileConstant.APPLICATIONID.toString(), "WebSubUpdateAuthTypeServiceImpl::insertInResidentTransactionTable()::exception");
        }
        return Tuples.of(eventId, individualId);
    }
    
    private NotificationResponseDTO sendNotificationV2(TemplateType templateType, String eventId, String individualId) throws ResidentServiceCheckedException, ApisResourceAccessException {

		NotificationRequestDtoV2 notificationRequestDtoV2 = new NotificationRequestDtoV2();
		notificationRequestDtoV2.setId(individualId);
		notificationRequestDtoV2.setRequestType(RequestType.AUTH_TYPE_LOCK_UNLOCK);
		notificationRequestDtoV2.setTemplateType(templateType);
		notificationRequestDtoV2.setEventId(eventId);

		return notificationService.sendNotification(notificationRequestDtoV2);
	}
}
