package io.mosip.resident.controller;

import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_DESCRIPTION;
import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_ID;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.annotation.Timed;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.dto.GrievanceRequestDTO;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.GrievanceService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.AuditEnum;
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
    
	@Autowired
	private Environment environment;

    private static final Logger logger = LoggerConfiguration.logConfig(GrievanceController.class);

    @Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PostMapping("/grievance/ticket")
	public ResponseWrapper<Object> grievanceTicket(
			@Validated @RequestBody MainRequestDTO<GrievanceRequestDTO> grievanceRequestDTOMainRequestDTO)
            throws ResidentServiceCheckedException, ApisResourceAccessException, IOException, NoSuchAlgorithmException, io.mosip.resident.exception.NoSuchAlgorithmException {
		logger.debug("GrievanceController::grievanceTicket()::entry");
		ResponseWrapper<Object> response = null;
		try {
			requestValidator.validateGrievanceRequestDto(grievanceRequestDTOMainRequestDTO);
			response = grievanceService.getGrievanceTicket(grievanceRequestDTOMainRequestDTO);
		} catch (ResidentServiceException | InvalidInputException | ResidentServiceCheckedException |
                 ApisResourceAccessException | io.mosip.resident.exception.NoSuchAlgorithmException e) {
			auditUtil.setAuditRequestDto(AuditEnum.GRIEVANCE_TICKET_REQUEST_FAILED);
			e.setMetadata(Map.of(ResidentConstants.REQ_RES_ID,
                    Objects.requireNonNull(environment.getProperty(ResidentConstants.GRIEVANCE_REQUEST_ID))));
			throw e;
		}
        auditUtil.setAuditRequestDto(AuditEnum.GRIEVANCE_TICKET_REQUEST_SUCCESS);
		logger.debug("GrievanceController::grievanceTicket()::exit");
		return response;
	}
}
