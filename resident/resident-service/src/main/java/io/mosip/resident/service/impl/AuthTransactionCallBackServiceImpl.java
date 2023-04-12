package io.mosip.resident.service.impl;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.websub.model.EventModel;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.EventStatusFailure;
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
import io.mosip.resident.util.Utility;

@Component
public class AuthTransactionCallBackServiceImpl implements AuthTransactionCallBackService {

    private static final String AUTHTYPE_CODE = "authtypeCode";
	private static final String REQUESTDATETIME = "requestdatetime";
	private static final String STATUS_COMMENT = "statusComment";
	private static final String STATUS_CODE = "statusCode";
	private static final String RESPONSE_SIGNATURE = "responseSignature";
	private static final String REQUEST_SIGNATURE = "requestSignature";
	private static final String ENTITY_NAME = "entityName";
	private static final String REFERENCE_ID_TYPE = "referenceIdType";
	private static final String TRANSACTION_ID = "transactionID";
	private static final String INDIVIDUAL_ID = "individualId";
	private static final String ENTITY_ID = "entityId";
	private static final String TOKEN_ID = "tokenId";
	private static final Logger logger = LoggerConfiguration.logConfig(AuthTransactionCallBackServiceImpl.class);
    private static final String OLV_PARTNER_ID = "olv_partner_id";

    @Autowired
    private AuditUtil auditUtil;

    @Autowired
    private ResidentTransactionRepository residentTransactionRepository;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
	private Utility utility;

    @Override
    public void updateAuthTransactionCallBackService(EventModel eventModel) throws ResidentServiceCheckedException, ApisResourceAccessException, NoSuchAlgorithmException {
        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "AuthTransactionCallbackServiceImpl::updateAuthTransactionCallBackService()::entry");
        auditUtil.setAuditRequestDto(EventEnum.UPDATE_AUTH_TYPE_STATUS);
        try {
            logger.info("AuthTransactionCallbackServiceImpl::updateAuthTransactionCallBackService()::partnerId");
            insertInResidentTransactionTable(eventModel, null);
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
        
		ResidentTransactionEntity residentTransactionEntity = utility.createEntity();
		residentTransactionEntity.setEventId(utility.createEventId());
		residentTransactionEntity.setRequestTypeCode(RequestType.AUTHENTICATION_REQUEST.name());
		residentTransactionEntity.setRefId((String) eventModel.getEvent().getData().get(INDIVIDUAL_ID));
		residentTransactionEntity.setIndividualId((String) eventModel.getEvent().getData().get(INDIVIDUAL_ID));
		residentTransactionEntity.setRequestSummary(RequestType.AUTHENTICATION_REQUEST.name());
		residentTransactionEntity.setTokenId((String) eventModel.getEvent().getData().get(TOKEN_ID));
		residentTransactionEntity.setOlvPartnerId((String) eventModel.getEvent().getData().get(OLV_PARTNER_ID));
		residentTransactionEntity.setRequestTrnId((String) eventModel.getEvent().getData().get(TRANSACTION_ID));
		residentTransactionEntity.setRefIdType((String) eventModel.getEvent().getData().get(REFERENCE_ID_TYPE));
		residentTransactionEntity.setRequestedEntityId((String) eventModel.getEvent().getData().get(ENTITY_ID));
		residentTransactionEntity.setRequestedEntityName((String) eventModel.getEvent().getData().get(ENTITY_NAME));
		residentTransactionEntity.setRequestSignature((String) eventModel.getEvent().getData().get(REQUEST_SIGNATURE));
		residentTransactionEntity.setResponseSignature((String) eventModel.getEvent().getData().get(RESPONSE_SIGNATURE));
		if(status == null) {
			residentTransactionEntity.setStatusCode((String) eventModel.getEvent().getData().get(STATUS_CODE));
		}
		residentTransactionEntity.setAuthTypeCode((String) eventModel.getEvent().getData().get(AUTHTYPE_CODE));
		residentTransactionEntity.setAuthTknId((String) eventModel.getEvent().getData().get(TOKEN_ID));
		residentTransactionEntity.setStatusComment((String) eventModel.getEvent().getData().get(STATUS_COMMENT));
		Object reqdatetimeObj = eventModel.getEvent().getData().get(REQUESTDATETIME);
		if(reqdatetimeObj != null) {
			residentTransactionEntity.setRequestDtimes(objectMapper.convertValue(reqdatetimeObj, LocalDateTime.class));
		}
		residentTransactionRepository.save(residentTransactionEntity);
        
        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "AuthTransactionCallbackServiceImpl::insertInResidentTransactionTable()::exit");
    }
}
