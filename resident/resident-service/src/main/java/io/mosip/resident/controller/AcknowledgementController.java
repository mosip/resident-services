package io.mosip.resident.controller;

import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_DESCRIPTION;
import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_ID;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.annotation.Timed;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.AcknowledgementService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.Utility;
import io.mosip.resident.validator.RequestValidator;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.util.function.Tuple2;

/**
 * This class is used to create api for getting acknowledgement.
 * @Author Kamesh Shekhar Prasad
 */
@RestController
@Tag(name="AcknowledgementController", description="AcknowledgementController")
public class AcknowledgementController {

	private static final Logger logger = LoggerConfiguration.logConfig(AcknowledgementController.class);
    
    @Value("${resident.event.ack.download.id}")
    private String ackDownloadId;
    
    @Value("${resident.event.ack.download.version}")
    private String ackDownloadVersion;

    @Autowired
    private AuditUtil auditUtil;

    @Autowired
    private RequestValidator requestValidator;

    @Autowired
    private AcknowledgementService acknowledgementService;

    @Autowired
    private Utility utility;

	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @GetMapping("/ack/download/pdf/event/{eventId}/language/{languageCode}")
    public ResponseEntity<Object> getAcknowledgement(@PathVariable("eventId") String eventId,
                                                  @PathVariable("languageCode") String languageCode,
                                                  @RequestHeader(name = "time-zone-offset", required = false, defaultValue = "0") int timeZoneOffset,
                                                  @RequestHeader(name = "locale", required = false) String locale) throws ResidentServiceCheckedException, IOException {
        logger.debug("AcknowledgementController::getAcknowledgement()::entry");
        InputStreamResource resource = null;
        RequestType requestType;
        try {
        	requestValidator.validateEventIdLanguageCode(eventId, languageCode);
        } catch (ResidentServiceException | InvalidInputException e) {
			throw new ResidentServiceException(e.getErrorCode(), e.getErrorText(), e,
					Map.of(ResidentConstants.HTTP_STATUS_CODE, HttpStatus.BAD_REQUEST, ResidentConstants.REQ_RES_ID,
							ackDownloadId));
		}
        try {
        	logger.debug("AcknowledgementController::get acknowledgement download url");
	        Tuple2<byte[], RequestType> tupleResponse = acknowledgementService.getAcknowledgementPDF(eventId, languageCode, timeZoneOffset, locale);
	        resource = new InputStreamResource(new ByteArrayInputStream(tupleResponse.getT1()));
	        auditUtil.setAuditRequestDto(EventEnum.GET_ACKNOWLEDGEMENT_DOWNLOAD_URL_SUCCESS);
	        requestType = tupleResponse.getT2();
	        logger.debug("AcknowledgementController::getAcknowledgement()::exit");
        } catch(ResidentServiceCheckedException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_ACKNOWLEDGEMENT_DOWNLOAD_URL_FAILURE);
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), ExceptionUtils.getStackTrace(e));
			throw new ResidentServiceCheckedException(e.getErrorCode(), e.getErrorText(), e,
					Map.of(ResidentConstants.HTTP_STATUS_CODE, HttpStatus.BAD_REQUEST, ResidentConstants.REQ_RES_ID,
							ackDownloadId));
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=\"" +
                        utility.getFileNameAsPerFeatureName(eventId, requestType, timeZoneOffset, locale) + ".pdf\"")
                .body(resource);
    }
}
