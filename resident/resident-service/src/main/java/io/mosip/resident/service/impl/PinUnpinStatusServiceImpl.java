package io.mosip.resident.service.impl;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.ResponseDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.PinUnpinStatusService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;

/**
 * This class is used to implement service class of pin or unpin status api based on event id.
 * @Author Kamesh Shekhar Prasad
 */
@Component
public class PinUnpinStatusServiceImpl implements PinUnpinStatusService {

    private static final Logger logger = LoggerConfiguration.logConfig(PinUnpinStatusServiceImpl.class);

    private static final String SUCCESS = "SUCCESS";
    private static final String FAILURE = "FAILURE";

    @Autowired
    private AuditUtil audit;

    @Autowired
    private ResidentTransactionRepository residentTransactionRepository;

    @Value("${resident.service.pin.status.id}")
    private String pinnedStatusId;

    @Value("${resident.service.pin.status.version}")
    private String pinnedStatusVersion;

    @Value("${resident.service.unpin.status.id}")
    private String unPinnedStatusId;

    @Value("${resident.service.unpin.status.version}")
    private String unPinnedStatusVersion;

    @Override
	public ResponseWrapper<ResponseDTO> pinStatus(String eventId, boolean status)
			throws ResidentServiceCheckedException {
        try {
            Optional<ResidentTransactionEntity> optionalResidentTransactionEntity = residentTransactionRepository.findById(eventId);
            if (optionalResidentTransactionEntity.isPresent()) {
                ResidentTransactionEntity residentTransactionEntity = optionalResidentTransactionEntity.get();
                residentTransactionEntity.setPinnedStatus(status);
                residentTransactionRepository.save(residentTransactionEntity);
            } else {
                throw new ResidentServiceCheckedException(ResidentErrorCode.EVENT_STATUS_NOT_FOUND);
            }
            ResponseWrapper<ResponseDTO> responseWrapper = new ResponseWrapper<>();
            ResponseDTO responseDTO = new ResponseDTO();
            responseDTO.setStatus(HttpStatus.OK.toString());
            responseDTO.setMessage(SUCCESS);
            responseWrapper.setResponse(responseDTO);
            if(status){
                responseWrapper.setId(pinnedStatusId);
                responseWrapper.setVersion(pinnedStatusVersion);
                audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.PIN_STATUS_SUCCESS, eventId));
            } else{
                responseWrapper.setId(unPinnedStatusId);
                responseWrapper.setVersion(unPinnedStatusVersion);
                audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.UN_PIN_STATUS_SUCCESS, eventId));
            }
            return responseWrapper;
        }
        catch (Exception e){
            logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                    LoggerFileConstant.APPLICATIONID.toString(), ExceptionUtils.getStackTrace(e));
            ResponseWrapper<ResponseDTO> responseWrapper = new ResponseWrapper<>();
            if(status){
                responseWrapper.setId(pinnedStatusId);
                responseWrapper.setVersion(pinnedStatusVersion);
                audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.PIN_STATUS_FAILURE, eventId));
            } else{
                responseWrapper.setId(unPinnedStatusId);
                responseWrapper.setVersion(unPinnedStatusVersion);
                audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.UN_PIN_STATUS_FAILURE, eventId));
            }
            responseWrapper.setErrors(List.of(new ServiceError(ResidentErrorCode.EVENT_STATUS_NOT_FOUND.getErrorCode(), ResidentErrorCode.EVENT_STATUS_NOT_FOUND.getErrorMessage())));
            return responseWrapper;
        }
    }
}
