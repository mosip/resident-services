package io.mosip.resident.controller;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.AcknowledgementService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.util.Utilitiy;
import io.mosip.resident.validator.RequestValidator;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

/**
 * This class is used to create api for getting acknowledgement.
 * @Author Kamesh Shekhar Prasad
 */
@RestController
@Tag(name="AcknowledgementController", description="AcknowledgementController")
public class AcknowledgementController {

    private static final Logger logger = LoggerConfiguration.logConfig(ResidentController.class);
    
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
    private TemplateUtil templateUtil;

    @Autowired
    private Utilitiy utilitiy;

    @GetMapping("/ack/download/pdf/event/{eventId}/language/{languageCode}")
    public ResponseEntity<Object> getAcknowledgement(@PathVariable("eventId") String eventId,
                                                  @PathVariable("languageCode") String languageCode) throws ResidentServiceCheckedException, IOException {
        logger.debug("AcknowledgementController::acknowledgement()::entry");
        InputStreamResource resource = null;
        String featureName = null;
        try {
        auditUtil.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.GET_ACKNOWLEDGEMENT_DOWNLOAD_URL, "acknowledgement"));
        requestValidator.validateEventIdLanguageCode(eventId, languageCode);
        byte[] pdfBytes = acknowledgementService.getAcknowledgementPDF(eventId, languageCode);
        resource = new InputStreamResource(new ByteArrayInputStream(pdfBytes));
        auditUtil.setAuditRequestDto(EventEnum.GET_ACKNOWLEDGEMENT_DOWNLOAD_URL_SUCCESS);
        logger.debug("AcknowledgementController::acknowledgement()::exit");
        featureName = templateUtil.getFeatureName(eventId);
        } catch(ResidentServiceCheckedException e) {
        	ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
        	responseWrapper.setId(ackDownloadId);
    		responseWrapper.setVersion(ackDownloadVersion);
			auditUtil.setAuditRequestDto(EventEnum.GET_ACKNOWLEDGEMENT_DOWNLOAD_URL_FAILURE);
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), ExceptionUtils.getStackTrace(e));
			responseWrapper.setErrors(List.of(new ServiceError(e.getErrorCode(), e.getErrorText())));
			return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
	                .body(responseWrapper);
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=\"" +
                        utilitiy.getFileNameAsPerFeatureName(eventId, featureName) + ".pdf\"")
                .body(resource);
    }
}
