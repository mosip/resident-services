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
import io.mosip.resident.entity.AutnTxn;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.AutnTxnRepository;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.AuthTransactionCallBackService;
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
public class AuthTransactionCallBackServiceImpl implements AuthTransactionCallBackService {

    private static final Logger logger = LoggerConfiguration.logConfig(AuthTransactionCallBackServiceImpl.class);

    @Autowired
    private AuditUtil auditUtil;

    @Autowired
    private AutnTxnRepository autnTxnRepository;

    @Autowired
    private IdentityServiceImpl identityService;

    @Override
    public void updateAuthTransactionCallBackService(EventModel eventModel, String partnerId) throws ResidentServiceCheckedException {
        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "AuthTransactionCallbackServiceImpl::updateAuthTransactionCallBackService()::entry");
        auditUtil.setAuditRequestDto(EventEnum.UPDATE_AUTH_TYPE_STATUS);
        try {
            logger.info(partnerId, "", "", "AuthTransactionCallbackServiceImpl::updateAuthTransactionCallBackService()::partnerId");

            insertInResidentTransactionTable(eventModel, partnerId, "COMPLETED");

        } catch (Exception e) {
            logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                    LoggerFileConstant.APPLICATIONID.toString(), "AuthTransactionCallbackServiceImpl::updateAuthTransactionCallBackService()::exception");
            insertInResidentTransactionTable(eventModel, partnerId, "FAILED");
            throw new ResidentServiceCheckedException(ResidentErrorCode.RESIDENT_WEBSUB_UPDATE_AUTH_TYPE_FAILED.getErrorCode(),
                    ResidentErrorCode.RESIDENT_WEBSUB_UPDATE_AUTH_TYPE_FAILED.getErrorMessage(), e);
        }
    }

    private void insertInResidentTransactionTable(EventModel eventModel, String partnerId, String status) {

        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "AuthTransactionCallbackServiceImpl::insertInResidentTransactionTable()::entry");

        try {
            AutnTxn autnTxn = new AutnTxn();
            String id = eventModel.getEvent().getId()+partnerId;
            String hash = HMACUtils2.digestAsPlainText(id.getBytes(StandardCharsets.UTF_8));
            AutnTxn autnTxn1 = autnTxnRepository.findById(hash);
            if (autnTxn1 != null) {
                updateAuthTxnTable(autnTxn1, eventModel, partnerId, status);
            } else {
                insertAuthTxnTable(autnTxn, eventModel, partnerId, status, hash);
            }

        } catch (Exception e) {
            logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                    LoggerFileConstant.APPLICATIONID.toString(), "AuthTransactionCallbackServiceImpl::insertInResidentTransactionTable()::exception");
        }

    }

    private void insertAuthTxnTable(AutnTxn autnTxn, EventModel eventModel, String partnerId, String status, String hash) {
        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "AuthTransactionCallbackServiceImpl::insertAuthTxnTable()::entry");
        try {
            autnTxn.setId(hash);
            autnTxn.setRequestDTtimes(LocalDateTime.now());
            autnTxn.setResponseDTimes(LocalDateTime.now());
            autnTxn.setRequestTrnId(eventModel.getEvent().getId());
            autnTxn.setAuthTypeCode("OTP");
            autnTxn.setStatusCode(status);
            autnTxn.setStatusComment(status);
            autnTxn.setLangCode("eng");
            autnTxn.setCrBy("RESIDENT");
            autnTxn.setCrDTimes(LocalDateTime.now());
            autnTxn.setToken(identityService.getIDAToken(eventModel.getEvent().getId()));
            autnTxn.setOlvPartnerId(partnerId);
            autnTxnRepository.save(autnTxn);
        } catch (Exception e) {
            logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                    LoggerFileConstant.APPLICATIONID.toString(), "AuthTransactionCallbackServiceImpl::insertAuthTxnTable()::exception");
        }


    }

    private void updateAuthTxnTable(AutnTxn autnTxn1, EventModel eventModel, String partnerId, String status) {
        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "AuthTransactionCallbackServiceImpl::updateAuthTxnTable()::entry");
        try {
            autnTxn1.setResponseDTimes(LocalDateTime.now());
            autnTxn1.setStatusCode(status);
            autnTxn1.setStatusComment(status);
            autnTxn1.setUpdBy("RESIDENT");
            autnTxn1.setUpdDTimes(LocalDateTime.now());
            autnTxn1.setOlvPartnerId(partnerId);
            autnTxnRepository.save(autnTxn1);
        } catch (Exception e) {
            logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                    LoggerFileConstant.APPLICATIONID.toString(), "AuthTransactionCallbackServiceImpl::updateAuthTxnTable()::exception");
        }

    }


}
