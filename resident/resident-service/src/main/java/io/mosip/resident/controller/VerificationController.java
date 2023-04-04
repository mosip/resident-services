package io.mosip.resident.controller;

import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.dto.VerificationResponseDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.impl.VerificationServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.validator.RequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@Tag(name = "verification-controller", description = "controller for channel verification status")
public class VerificationController {

    @Autowired
    private AuditUtil audit;

    @Autowired
    private VerificationServiceImpl verificationServiceImpl;

    @Autowired
    private RequestValidator validator;

    private static final Logger logger = LoggerConfiguration.logConfig(VerificationController.class);

    @GetMapping(value = "/channel/verification-status/")
    @Operation(summary = "get channel verification status", description = "get channel verification status")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public VerificationResponseDTO getChannelVerificationStatus(@RequestParam("channel") String channel,
			@RequestParam("individualId") String individualId)
			throws ResidentServiceCheckedException, NoSuchAlgorithmException, ApisResourceAccessException {
		logger.info("getChannelVerificationStatus method started");
        validator.validateChannelVerificationStatus(channel, individualId);
        VerificationResponseDTO verificationResponseDTO = verificationServiceImpl.checkChannelVerificationStatus(channel, individualId);
        return verificationResponseDTO;
    }

}
