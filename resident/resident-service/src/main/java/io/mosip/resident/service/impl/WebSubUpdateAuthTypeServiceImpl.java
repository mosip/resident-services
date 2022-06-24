package io.mosip.resident.service.impl;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.kernel.core.websub.model.EventModel;
import io.mosip.kernel.core.websub.spi.PublisherClient;
import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;
import io.mosip.kernel.websub.api.model.UnsubscriptionRequest;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.ResidentTransactionType;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.WebSubUpdateAuthTypeService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
public class WebSubUpdateAuthTypeServiceImpl implements WebSubUpdateAuthTypeService {

    private static final Logger logger = LoggerConfiguration.logConfig(WebSubUpdateAuthTypeServiceImpl.class);

    @Autowired
    private AuditUtil auditUtil;

    /** The publisher. */
    @Autowired
    private PublisherClient<String, Object, HttpHeaders> publisher;

    @Autowired
    SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse> subscribe;


    @Value("${resident.websub.authtype-status.topic}")
    private String topic;

    @Value("${websub.publish.url}")
    private String publishUrl;

    @Value("${websub.hub.url}")
    private String hubUrl;

    @Value("${resident.websub.authtype-status.secret}")
    private String secret;

    @Value("${resident.websub.callback.authtype-status.url}")
    private String callbackUrl;

    @Autowired
    private IdentityServiceImpl identityServiceImpl;

    @Autowired
    private ResidentTransactionRepository residentTransactionRepository;

    @Override
    public void updateAuthTypeStatus(EventModel eventModel) throws ResidentServiceCheckedException {
        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "WebSubUpdateAuthTypeServiceImpl::updateAuthTypeStatus()::entry");
        auditUtil.setAuditRequestDto(EventEnum.UPDATE_AUTH_TYPE_STATUS);
        try{
            logger.info( "WebSubUpdateAuthTypeServiceImpl::updateAuthTypeStatus()::partnerId");

            publisher.publishUpdate(topic, eventModel, MediaType.APPLICATION_JSON_VALUE, null, publishUrl);

            SubscriptionChangeRequest subscriptionRequest = new SubscriptionChangeRequest();
            logger.info(callbackUrl, "", "", "WebSubUpdateAuthTypeServiceImpl::updateAuthTypeStatus()::callbackUrl");
            subscriptionRequest.setCallbackURL(callbackUrl);
            subscriptionRequest.setSecret(secret);
            subscriptionRequest.setTopic(topic);
            subscriptionRequest.setHubURL(hubUrl);
            subscribe.subscribe(subscriptionRequest);

            insertInResidentTransactionTable(eventModel,"COMPLETED");

        }
        catch (Exception e) {
            logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                    LoggerFileConstant.APPLICATIONID.toString(), "WebSubUpdateAuthTypeServiceImpl::updateAuthTypeStatus()::exception");
            insertInResidentTransactionTable(eventModel,"FAILED");
            throw new ResidentServiceCheckedException(ResidentErrorCode.RESIDENT_WEBSUB_UPDATE_AUTH_TYPE_FAILED.getErrorCode(),
                    ResidentErrorCode.RESIDENT_WEBSUB_UPDATE_AUTH_TYPE_FAILED.getErrorMessage(), e);
        }
    }

    private void insertInResidentTransactionTable(EventModel eventModel,  String status) {

        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "WebSubUpdateAuthTypeServiceImpl::insertInResidentTransactionTable()::entry");

        try {
            ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
            String id = identityServiceImpl.getResidentIndvidualId();
            residentTransactionEntity.setAid(HMACUtils2.digestAsPlainText(id.getBytes(StandardCharsets.UTF_8)));
            residentTransactionEntity.setRequestDtimes(LocalDateTime.now());
            residentTransactionEntity.setResponseDtime(LocalDateTime.now());
            residentTransactionEntity.setRequestTrnId(eventModel.getEvent().getTransactionId());
            residentTransactionEntity.setRequestTypeCode("Requested for Subscribing to WebSub");
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

    }
}
