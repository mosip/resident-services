package io.mosip.resident.service.impl;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.PinUnpinStatusService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * This class is used to implement service class of pin or unpin status api based on event id.
 * @Author Kamesh Shekhar Prasad
 */
@Component
public class PinUnpinStatusServiceImpl implements PinUnpinStatusService {

    private static final Logger logger = LoggerConfiguration.logConfig(PinUnpinStatusServiceImpl.class);

    @Autowired
    private AuditUtil audit;

    @Autowired
    private ResidentTransactionRepository residentTransactionRepository;

    @Override
    public ResponseEntity<?> pinStatus(String eventId, boolean status) {
        try {
            Optional<ResidentTransactionEntity> optionalResidentTransactionEntity = residentTransactionRepository.findById(eventId);
            if (optionalResidentTransactionEntity.isPresent()) {
                ResidentTransactionEntity residentTransactionEntity = optionalResidentTransactionEntity.get();
                residentTransactionEntity.setPinnedStatus(status);
                residentTransactionRepository.save(residentTransactionEntity);
            } else {
                throw new ResidentServiceCheckedException(ResidentErrorCode.EVENT_STATUS_NOT_FOUND);
            }
            if(status){
                audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.PIN_STATUS_SUCCESS, eventId));
            } else{
                audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.UN_PIN_STATUS_SUCCESS, eventId));
            }
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (Exception e){
            if(status){
                audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.PIN_STATUS_FAILURE, eventId));
            } else{
                audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.UN_PIN_STATUS_FAILURE, eventId));
            }
            logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                    LoggerFileConstant.APPLICATIONID.toString(), ExceptionUtils.getStackTrace(e));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
