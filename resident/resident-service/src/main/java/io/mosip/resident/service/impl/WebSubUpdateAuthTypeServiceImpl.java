package io.mosip.resident.service.impl;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.kernel.core.websub.model.EventModel;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.constant.TemplateType;
import io.mosip.resident.dto.NotificationRequestDtoV2;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.dto.ResidentTransactionType;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.WebSubUpdateAuthTypeService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class WebSubUpdateAuthTypeServiceImpl implements WebSubUpdateAuthTypeService {

    private static final Logger logger = LoggerConfiguration.logConfig(WebSubUpdateAuthTypeServiceImpl.class);

    @Autowired
    private AuditUtil auditUtil;

    @Autowired
    private IdentityServiceImpl identityServiceImpl;
    
    @Autowired
	private NotificationService notificationService;

    @Autowired
    private ResidentTransactionRepository residentTransactionRepository;

    @Override
    public void updateAuthTypeStatus(EventModel eventModel) throws ResidentServiceCheckedException, ApisResourceAccessException {
        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "WebSubUpdateAuthTypeServiceImpl::updateAuthTypeStatus()::entry");
        auditUtil.setAuditRequestDto(EventEnum.UPDATE_AUTH_TYPE_STATUS);
        try{
            logger.info( "WebSubUpdateAuthTypeServiceImpl::updateAuthTypeStatus()::partnerId");
            ResidentTransactionEntity residentTransactionEntity = insertInResidentTransactionTable(eventModel,"COMPLETED");
            sendNotificationV2(TemplateType.SUCCESS, residentTransactionEntity.getEventId());
        }
        catch (Exception e) {
            logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                    LoggerFileConstant.APPLICATIONID.toString(), "WebSubUpdateAuthTypeServiceImpl::updateAuthTypeStatus()::exception");
            ResidentTransactionEntity residentTransactionEntity = insertInResidentTransactionTable(eventModel,"FAILED");
            sendNotificationV2(TemplateType.FAILURE, residentTransactionEntity.getEventId());
            throw new ResidentServiceCheckedException(ResidentErrorCode.RESIDENT_WEBSUB_UPDATE_AUTH_TYPE_FAILED.getErrorCode(),
                    ResidentErrorCode.RESIDENT_WEBSUB_UPDATE_AUTH_TYPE_FAILED.getErrorMessage(), e);
        }
    }

    private ResidentTransactionEntity insertInResidentTransactionTable(EventModel eventModel,  String status) {

        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "WebSubUpdateAuthTypeServiceImpl::insertInResidentTransactionTable()::entry");

        ResidentTransactionEntity residentTransactionEntity = null;
        try {
            residentTransactionEntity = new ResidentTransactionEntity();
            residentTransactionEntity.setEventId(UUID.randomUUID().toString());
            String id = identityServiceImpl.getResidentIndvidualId();
            residentTransactionEntity.setAid(HMACUtils2.digestAsPlainText(id.getBytes(StandardCharsets.UTF_8)));
            residentTransactionEntity.setRequestDtimes(LocalDateTime.now());
            residentTransactionEntity.setResponseDtime(LocalDateTime.now());
            residentTransactionEntity.setRequestTrnId(eventModel.getEvent().getTransactionId());
            residentTransactionEntity.setRequestTypeCode(RequestType.AUTH_TYPE_LOCK_UNLOCK.name());
            residentTransactionEntity.setRequestSummary("Requested for Subscribing to WebSub");
            residentTransactionEntity.setStatusCode(status);
            residentTransactionEntity.setStatusComment(status);
            residentTransactionEntity.setLangCode("eng");
            residentTransactionEntity.setTokenId(identityServiceImpl.getIDAToken(identityServiceImpl.getResidentIndvidualId()));
            residentTransactionEntity.setCrBy("mosip");
            residentTransactionEntity.setCrDtimes(LocalDateTime.now());
            residentTransactionEntity.setAuthTypeCode(ResidentTransactionType.AUTHENTICATION_REQUEST.toString());

            residentTransactionRepository.save(residentTransactionEntity);
        }
        catch (Exception e) {
            logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                    LoggerFileConstant.APPLICATIONID.toString(), "WebSubUpdateAuthTypeServiceImpl::insertInResidentTransactionTable()::exception");
        }
        return residentTransactionEntity;
    }
    
    private NotificationResponseDTO sendNotificationV2(TemplateType templateType, String eventId) throws ResidentServiceCheckedException, ApisResourceAccessException {

		NotificationRequestDtoV2 notificationRequestDtoV2 = new NotificationRequestDtoV2();
		String id = identityServiceImpl.getResidentIndvidualId();
		notificationRequestDtoV2.setId(id);
		notificationRequestDtoV2.setRequestType(RequestType.AUTH_TYPE_LOCK_UNLOCK);
		notificationRequestDtoV2.setTemplateType(templateType);
		notificationRequestDtoV2.setEventId(eventId);

		return notificationService.sendNotification(notificationRequestDtoV2);
	}
}
