package io.mosip.resident.service.impl;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.websub.model.EventModel;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.EventStatusFailure;
import io.mosip.resident.constant.EventStatusSuccess;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.AuthTransactionCallBackService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.Utilitiy;

@Component
public class AuthTransactionCallBackServiceImpl implements AuthTransactionCallBackService {

    private static final Logger logger = LoggerConfiguration.logConfig(AuthTransactionCallBackServiceImpl.class);
    private static final String OLV_PARTNER_ID = "olv_partner_id";

    @Autowired
    private AuditUtil auditUtil;

    @Autowired
    private ResidentTransactionRepository residentTransactionRepository;

    @Autowired
    private IdentityServiceImpl identityService;
    
    @Autowired
	private Utilitiy utility;

    @Override
    public void updateAuthTransactionCallBackService(EventModel eventModel) throws ResidentServiceCheckedException, ApisResourceAccessException, NoSuchAlgorithmException {
        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "AuthTransactionCallbackServiceImpl::updateAuthTransactionCallBackService()::entry");
        auditUtil.setAuditRequestDto(EventEnum.UPDATE_AUTH_TYPE_STATUS);
        try {
            logger.info("AuthTransactionCallbackServiceImpl::updateAuthTransactionCallBackService()::partnerId");
            insertInResidentTransactionTable(eventModel, EventStatusSuccess.AUTHENTICATION_SUCCESSFUL.name());
        } catch (Exception e) {
            logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                    LoggerFileConstant.APPLICATIONID.toString(), "AuthTransactionCallbackServiceImpl::updateAuthTransactionCallBackService()::exception");
            insertInResidentTransactionTable(eventModel, EventStatusFailure.AUTHENTICATION_FAILED.name());
            throw new ResidentServiceCheckedException(ResidentErrorCode.RESIDENT_WEBSUB_UPDATE_AUTH_TYPE_FAILED.getErrorCode(),
                    ResidentErrorCode.RESIDENT_WEBSUB_UPDATE_AUTH_TYPE_FAILED.getErrorMessage(), e);
        }
    }

    private void insertInResidentTransactionTable(EventModel eventModel, String status) throws ApisResourceAccessException, NoSuchAlgorithmException {
        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "AuthTransactionCallbackServiceImpl::insertInResidentTransactionTable()::entry");
        
		String individualId = identityService.getResidentIndvidualId();
		ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
		residentTransactionEntity.setEventId(UUID.randomUUID().toString());
		residentTransactionEntity.setRequestTypeCode(RequestType.AUTHENTICATION_REQUEST.name());
		residentTransactionEntity.setStatusCode(status);
		residentTransactionEntity.setRefId(utility.convertToMaskDataFormat(individualId));
		residentTransactionEntity.setRequestSummary("");
		residentTransactionEntity.setTokenId(identityService.getResidentIdaToken());
		residentTransactionEntity.setOlvPartnerId((String) eventModel.getEvent().getData().get(OLV_PARTNER_ID));
		residentTransactionEntity.setRequestDtimes(LocalDateTime.parse(eventModel.getEvent().getTimestamp()));
		residentTransactionEntity.setResponseDtime(LocalDateTime.parse(eventModel.getEvent().getTimestamp()));
		residentTransactionEntity.setCrBy("resident-services");
		residentTransactionEntity.setCrDtimes(DateUtils.getUTCCurrentDateTime());
		residentTransactionRepository.save(residentTransactionEntity);
        
        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "AuthTransactionCallbackServiceImpl::insertInResidentTransactionTable()::exit");
    }
}
