package io.mosip.resident.service.impl;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
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
    public void updateAuthTransactionCallBackService(Map<String, Object> eventModel) throws ResidentServiceCheckedException, ApisResourceAccessException, NoSuchAlgorithmException {
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

    private void insertInResidentTransactionTable(Map<String, Object> eventModel, String status) throws ApisResourceAccessException, NoSuchAlgorithmException {
        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "AuthTransactionCallbackServiceImpl::insertInResidentTransactionTable()::entry");
        Object eventObj = eventModel.get("event");
		if (eventObj instanceof Map) {
			Map<String, Object> eventMap = (Map<String, Object>) eventObj;
			Object dataObject = eventMap.get("data");
			if (dataObject instanceof Map) {
				Map<String, Object> dataMap = (Map<String, Object>) dataObject;
				ResidentTransactionEntity residentTransactionEntity = utility.createEntity(RequestType.AUTHENTICATION_REQUEST);
				residentTransactionEntity.setEventId(utility.createEventId());
				residentTransactionEntity.setRefId((String) dataMap.get(INDIVIDUAL_ID));
				residentTransactionEntity.setIndividualId((String) dataMap.get(INDIVIDUAL_ID));
				residentTransactionEntity.setRequestSummary(RequestType.AUTHENTICATION_REQUEST.name());
				residentTransactionEntity.setTokenId((String) dataMap.get(TOKEN_ID));
				residentTransactionEntity.setOlvPartnerId((String) dataMap.get(OLV_PARTNER_ID));
				residentTransactionEntity.setRequestTrnId((String) dataMap.get(TRANSACTION_ID));
				residentTransactionEntity.setRefIdType((String) dataMap.get(REFERENCE_ID_TYPE));
				residentTransactionEntity.setRequestedEntityId((String) dataMap.get(ENTITY_ID));
				residentTransactionEntity.setRequestedEntityName((String) dataMap.get(ENTITY_NAME));
				residentTransactionEntity.setRequestSignature((String) dataMap.get(REQUEST_SIGNATURE));
				residentTransactionEntity.setResponseSignature((String) dataMap.get(RESPONSE_SIGNATURE));
				if (status == null) {
					Object object = dataMap.get(STATUS_CODE);
					if (object instanceof String) {
						status = (String) object;
					} else {
						status = EventStatusFailure.N.name();
					}
				}
				residentTransactionEntity.setStatusCode(status);
				residentTransactionEntity.setAuthTypeCode((String) dataMap.get(AUTHTYPE_CODE));
				residentTransactionEntity.setStatusComment((String) dataMap.get(STATUS_COMMENT));
				Object reqdatetimeObj = dataMap.get(REQUESTDATETIME);
				if (reqdatetimeObj != null) {
					residentTransactionEntity
							.setRequestDtimes(objectMapper.convertValue(reqdatetimeObj, LocalDateTime.class));
				}
				residentTransactionRepository.save(residentTransactionEntity);
			}
		}
        
        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "AuthTransactionCallbackServiceImpl::insertInResidentTransactionTable()::exit");
    }
}
