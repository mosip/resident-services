package io.mosip.resident.service.impl;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.kernel.core.websub.model.EventModel;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.ResidentTransactionType;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.AuthTransactionCallBackService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@Component
public class AuthTransactionCallBackServiceImpl implements AuthTransactionCallBackService {

    private static final Logger logger = LoggerConfiguration.logConfig(AuthTransactionCallBackServiceImpl.class);
    private static final String OLV_PARTNER_ID = "olv_partner_id";
    private static final String COMPLETED = "COMPLETED";
    private static final String FAILED = "FAILED";
    private static final String OTP = "OTP";
    private static final String ENG = "eng";
    private static final String RESIDENT = "RESIDENT";

    @Autowired
    private AuditUtil auditUtil;

    @Autowired
    private ResidentTransactionRepository residentTransactionRepository;

    @Autowired
    private IdentityServiceImpl identityService;

    @Override
    public void updateAuthTransactionCallBackService(EventModel eventModel) throws ResidentServiceCheckedException, ApisResourceAccessException, NoSuchAlgorithmException {
        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "AuthTransactionCallbackServiceImpl::updateAuthTransactionCallBackService()::entry");
        auditUtil.setAuditRequestDto(EventEnum.UPDATE_AUTH_TYPE_STATUS);
        try {
            logger.info("AuthTransactionCallbackServiceImpl::updateAuthTransactionCallBackService()::partnerId");
            insertInResidentTransactionTable(eventModel, COMPLETED);
        } catch (Exception e) {
            logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                    LoggerFileConstant.APPLICATIONID.toString(), "AuthTransactionCallbackServiceImpl::updateAuthTransactionCallBackService()::exception");
            insertInResidentTransactionTable(eventModel, FAILED);
            throw new ResidentServiceCheckedException(ResidentErrorCode.RESIDENT_WEBSUB_UPDATE_AUTH_TYPE_FAILED.getErrorCode(),
                    ResidentErrorCode.RESIDENT_WEBSUB_UPDATE_AUTH_TYPE_FAILED.getErrorMessage(), e);
        }
    }

    private void insertInResidentTransactionTable(EventModel eventModel, String status) throws ApisResourceAccessException, NoSuchAlgorithmException {
        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "AuthTransactionCallbackServiceImpl::insertInResidentTransactionTable()::entry");
        ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
        String id = identityService.getResidentIndvidualId();
        residentTransactionEntity.setAid(HMACUtils2.digestAsPlainText(id.getBytes(StandardCharsets.UTF_8)));
        residentTransactionEntity.setRequestDtimes(LocalDateTime.now());
        residentTransactionEntity.setResponseDtime(LocalDateTime.now());
        residentTransactionEntity.setRequestTrnId(eventModel.getEvent().getTransactionId());
        residentTransactionEntity.setRequestTypeCode("Requested for Subscribing to WebSub");
        residentTransactionEntity.setRequestSummary("Requested for Subscribing to WebSub");
        residentTransactionEntity.setStatusCode(status);
        residentTransactionEntity.setStatusComment(status);
        residentTransactionEntity.setLangCode(ENG);
        residentTransactionEntity.setTokenId(identityService.getIDAToken(identityService.getResidentIndvidualId()));
        residentTransactionEntity.setCrBy(RESIDENT);
        residentTransactionEntity.setCrDtimes(LocalDateTime.now());
        residentTransactionEntity.setAuthTypeCode(ResidentTransactionType.SERVICE_REQUEST.toString());
        residentTransactionEntity.setOlvPartnerId((String) eventModel.getEvent().getData().get(OLV_PARTNER_ID));
        residentTransactionRepository.save(residentTransactionEntity);
        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "AuthTransactionCallbackServiceImpl::insertInResidentTransactionTable()::exit");
    }
}
