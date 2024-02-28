package io.mosip.resident.service.impl;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.EventStatusFailure;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.PacketStatus;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.dto.WorkflowCompletedEventDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.WebSubRegprocWorkFlowService;
import io.mosip.resident.util.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class WebSubRegprocWorkFlowServiceImpl implements WebSubRegprocWorkFlowService {

    private static final Logger logger = LoggerConfiguration.logConfig(WebSubRegprocWorkFlowServiceImpl.class);

    @Autowired
    Environment environment;

    @Autowired
    ResidentTransactionRepository residentTransactionRepository;

    @Autowired
    Utility utility;

    @Override
    public void updateResidentStatus(WorkflowCompletedEventDTO workflowCompletedEventDTO) throws ResidentServiceCheckedException, ApisResourceAccessException {
        try {
            if (workflowCompletedEventDTO.getResultCode() != null) {
                if (PacketStatus.getStatusCodeList(PacketStatus.FAILURE, environment).contains(workflowCompletedEventDTO.getResultCode())) {
                    if (workflowCompletedEventDTO.getInstanceId() != null) {
                        ResidentTransactionEntity residentTransactionEntity =
                                residentTransactionRepository.findByAid(workflowCompletedEventDTO.getInstanceId());
                        if (residentTransactionEntity != null) {
                            residentTransactionRepository.updateEventStatus(residentTransactionEntity.getEventId(),
                                    RequestType.UPDATE_MY_UIN.name() + " - " + ResidentConstants.FAILED,
                                    EventStatusFailure.FAILED.name(), "Packet Failed in Regproc with status code-" +
                                            workflowCompletedEventDTO.getResultCode(),
                                    utility.getSessionUserName(), DateUtils.getUTCCurrentDateTime());
                        }
                    }

                }
            }
        }catch (Exception exception){
            logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                    LoggerFileConstant.APPLICATIONID.toString(),
                    "WebSubRegprocWorkFlowServiceImpl::updateResidentStatus()::exception");
        }
    }
}
