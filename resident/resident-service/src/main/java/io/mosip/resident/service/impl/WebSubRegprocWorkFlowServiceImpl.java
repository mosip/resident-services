package io.mosip.resident.service.impl;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.EventStatusFailure;
import io.mosip.resident.constant.EventStatusInProgress;
import io.mosip.resident.constant.PacketStatus;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.dto.WorkflowCompletedEventDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.WebSubRegprocWorkFlowService;
import io.mosip.resident.util.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author Kamesh Shekhar Prasad
 */

@Component
public class WebSubRegprocWorkFlowServiceImpl implements WebSubRegprocWorkFlowService {

    private static final Logger logger = LoggerConfiguration.logConfig(WebSubRegprocWorkFlowServiceImpl.class);

    @Autowired
    Environment environment;

    @Autowired
    ResidentTransactionRepository residentTransactionRepository;

    @Autowired
    Utility utility;

    @Override
    public void updateResidentStatus(WorkflowCompletedEventDTO workflowCompletedEventDTO) {

        ResidentTransactionEntity residentTransactionEntity = null;
        if (workflowCompletedEventDTO.getResultCode() != null) {
            if (workflowCompletedEventDTO.getInstanceId() != null) {
                residentTransactionEntity =
                        residentTransactionRepository.findByAid(workflowCompletedEventDTO.getInstanceId());
            }
            if (residentTransactionEntity != null) {
                if (PacketStatus.getStatusCodeList(PacketStatus.FAILURE, environment).contains(workflowCompletedEventDTO.getResultCode())) {
                    updateEventStatusInDb(residentTransactionEntity.getEventId(),
                            RequestType.UPDATE_MY_UIN.name() + " - " + ResidentConstants.FAILED,
                            EventStatusFailure.FAILED.name(), "Packet Failed in Regproc with status code-" +
                                    workflowCompletedEventDTO.getResultCode(),
                            utility.getSessionUserName(), DateUtils.getUTCCurrentDateTime());
                } else if (PacketStatus.getStatusCodeList(PacketStatus.SUCCESS, environment).contains(workflowCompletedEventDTO.getResultCode())) {
                    updateEventStatusInDb(residentTransactionEntity.getEventId(),
                            EventStatusInProgress.IDENTITY_UPDATED.name(),
                            residentTransactionEntity.getStatusCode(), "Packet processed in Regproc with status code-" +
                                    workflowCompletedEventDTO.getResultCode(),
                            utility.getSessionUserName(), DateUtils.getUTCCurrentDateTime());
                }
            }
        }
    }

    public void updateEventStatusInDb(String eventId, String requestSummary, String statusCode, String statusComment,
                                      String updBy, LocalDateTime updDtimes) {
        int updateStatus =
                residentTransactionRepository.updateEventStatus(eventId, requestSummary, statusCode, statusComment, updBy, updDtimes);
        if (updateStatus == ResidentConstants.ZERO) {
            logger.info("EventId-" + eventId + " status Not updated.");
        } else {
            logger.info("EventId-" + eventId + " status Updated.");
        }
    }

}
