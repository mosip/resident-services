package io.mosip.resident.service.impl;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.EventStatusFailure;
import io.mosip.resident.constant.EventStatusInProgress;
import io.mosip.resident.constant.PacketStatus;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.TemplateType;
import io.mosip.resident.dto.NotificationRequestDtoV2;
import io.mosip.resident.dto.WorkflowCompletedEventDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.WebSubRegprocWorkFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

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
    NotificationService notificationService;

    @Override
    public void updateResidentStatus(WorkflowCompletedEventDTO workflowCompletedEventDTO) throws ResidentServiceCheckedException {

        ResidentTransactionEntity residentTransactionEntity = null;
        if (workflowCompletedEventDTO.getResultCode() != null) {
            if (workflowCompletedEventDTO.getInstanceId() != null) {
                residentTransactionEntity =
                        residentTransactionRepository.findByAid(workflowCompletedEventDTO.getInstanceId());
            }
            if (residentTransactionEntity != null) {
                if (PacketStatus.getStatusCodeList(PacketStatus.FAILURE, environment).contains(workflowCompletedEventDTO.getResultCode())) {
                    updateEntity(EventStatusFailure.FAILED.name(), RequestType.UPDATE_MY_UIN.name() + " - " + ResidentConstants.FAILED,
                            false, "Packet Failed in Regproc with status code-" +
                            workflowCompletedEventDTO.getResultCode(), residentTransactionEntity);
                    sendNotification(residentTransactionEntity, TemplateType.REGPROC_FAILED);
                } else if (PacketStatus.getStatusCodeList(PacketStatus.SUCCESS, environment).contains(workflowCompletedEventDTO.getResultCode())) {
                    updateEntity(residentTransactionEntity.getStatusCode(), EventStatusInProgress.IDENTITY_UPDATED.name(), false,
                            "Packet processed in Regproc with status code-" +
                            workflowCompletedEventDTO.getResultCode(), residentTransactionEntity);
                    sendNotification(residentTransactionEntity, TemplateType.REGPROC_SUCCESS);
                }
            }
        }
    }

    public void updateEntity(String statusCode, String requestSummary, boolean readStatus, String statusComment, ResidentTransactionEntity residentTransactionEntity) {
        residentTransactionEntity.setStatusCode(statusCode);
        residentTransactionEntity.setRequestSummary(requestSummary);
        residentTransactionEntity.setReadStatus(readStatus);
        residentTransactionEntity.setStatusComment(statusComment);
        residentTransactionEntity.setUpdBy(ResidentConstants.RESIDENT);
        residentTransactionEntity.setUpdDtimes(DateUtils.getUTCCurrentDateTime());
        saveEntity(residentTransactionEntity);
    }

    private void saveEntity(ResidentTransactionEntity residentTransactionEntity) {
        residentTransactionRepository.save(residentTransactionEntity);
    }

    private void sendNotification(ResidentTransactionEntity txn, TemplateType templateType)
            throws ResidentServiceCheckedException {
        try {
            NotificationRequestDtoV2 notificationRequestDtoV2 = new NotificationRequestDtoV2();
            notificationRequestDtoV2.setTemplateType(templateType);
            notificationRequestDtoV2.setRequestType(RequestType.UPDATE_MY_UIN);
            notificationRequestDtoV2.setEventId(txn.getEventId());

            // For failure case this aid will not work as /identity api will fail with no record found error
            // Need to discuss this.
            notificationRequestDtoV2.setId(txn.getAid());

            notificationService.sendNotification(notificationRequestDtoV2, null);
        }catch (ResidentServiceCheckedException exception){
            logger.error("Error while sending notification:- "+ exception);
        }
    }

}
