package io.mosip.resident.service.impl;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.websub.model.EventModel;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.WebSubUpdateAuthTypeService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WebSubUpdateAuthTypeServiceImpl implements WebSubUpdateAuthTypeService {

    private static final Logger logger = LoggerConfiguration.logConfig(WebSubUpdateAuthTypeServiceImpl.class);

    @Autowired
    private AuditUtil auditUtil;

    @Override
    public void updateAuthTypeStatus(EventModel eventModel, String partnerId) throws ResidentServiceCheckedException {
        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "WebSubUpdateAuthTypeServiceImpl::updateAuthTypeStatus()::entry");
        auditUtil.setAuditRequestDto(EventEnum.UPDATE_AUTH_TYPE_STATUS);
        try{
            logger.info(partnerId, "", "", "WebSubUpdateAuthTypeServiceImpl::updateAuthTypeStatus()::partnerId");
        }
        catch (Exception e) {
            logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                    LoggerFileConstant.APPLICATIONID.toString(), "WebSubUpdateAuthTypeServiceImpl::updateAuthTypeStatus()::exception");
            throw new ResidentServiceCheckedException(ResidentErrorCode.RESIDENT_WEBSUB_UPDATE_AUTH_TYPE_FAILED.getErrorCode(),
                    ResidentErrorCode.RESIDENT_WEBSUB_UPDATE_AUTH_TYPE_FAILED.getErrorMessage(), e);
        }
    }
}


//        webSubUpdateAuthTypeService.updateAuthTypeStatus("1234","1234");
//
//        publisher.registerTopic("AUTH_TYPE_STATUS_UPDATE_ACK","http://localhost:9191/websub/publish");
//
//        EventModel e1=new EventModel();
//        Event event=new Event();
//        event.setTransactionId("1234");
//
//        e1.setEvent(event);
//        e1.setTopic("AUTH_TYPE_STATUS_UPDATE_ACK");
//        e1.setPublishedOn(String.valueOf(LocalDateTime.now()));
//        e1.setPublisher("AUTH_TYPE_STATUS_UPDATE_ACK");
//
//        publisher.publishUpdate("AUTH_TYPE_STATUS_UPDATE_ACK", e1, MediaType.APPLICATION_JSON_VALUE, null, "http://localhost:9191/websub/publish");
//
//        SubscriptionChangeRequest subscriptionRequest = new SubscriptionChangeRequest();
//        subscriptionRequest.setCallbackURL("/resident/v1/callback/authTypeCallback/AUTH_TYPE_STATUS_UPDATE_ACK");
//        subscriptionRequest.setSecret("abc123");
//        subscriptionRequest.setTopic("AUTH_TYPE_STATUS_UPDATE_ACK");
//        subscriptionRequest.setHubURL("http://localhost:9191/websub/hub");
//        subscribe.subscribe(subscriptionRequest);
