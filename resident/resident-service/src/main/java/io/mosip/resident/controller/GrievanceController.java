package io.mosip.resident.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.dto.GrievanceRequestDTO;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.GrievanceService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.validator.RequestValidator;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Kamesh Shekhar Prasad
 * This class is used to generate ticket of grievance.
 */

@RestController
@Tag(name = "GrievanceController", description = "Grievance-Controller")
public class GrievanceController {

    @Autowired
    AuditUtil auditUtil;

    @Autowired
    RequestValidator requestValidator;

    @Autowired
    GrievanceService grievanceService;

    private static final Logger logger = LoggerConfiguration.logConfig(GrievanceController.class);

    @PostMapping("/grievance/ticket")
    public ResponseWrapper<Object> grievanceTicket(@Validated @RequestBody MainRequestDTO<GrievanceRequestDTO>
                                                               grievanceRequestDTOMainRequestDTO) throws ResidentServiceCheckedException,
            ApisResourceAccessException, IOException {
        logger.debug("DownloadCardController::grievanceTicket()::entry");
        auditUtil.setAuditRequestDto(EventEnum.GRIEVANCE_TICKET_REQUEST);
        requestValidator.validateGrievanceRequestDto(grievanceRequestDTOMainRequestDTO);
        return grievanceService.getGrievanceTicket(grievanceRequestDTOMainRequestDTO);
    }
}
