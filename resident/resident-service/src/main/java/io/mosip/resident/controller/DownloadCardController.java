package io.mosip.resident.controller;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.dto.DownloadCardRequestDTO;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.service.DownloadCardService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.validator.RequestValidator;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;

/**
 * @author Kamesh Shekhar Prasad
 * This class is used to download card using Aid/vid/UIN.
 */

@RestController
@Tag(name = "DownloadCardController", description = "Download-Card-Controller")
public class DownloadCardController {

    @Autowired
    AuditUtil auditUtil;

    @Autowired
    RequestValidator requestValidator;

    @Autowired
    DownloadCardService downloadCardService;

    private static final Logger logger = LoggerConfiguration.logConfig(DownloadCardController.class);

    @PostMapping("/download-card")
    public ResponseEntity<Object> downloadCard(@Validated @RequestBody MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO){
        logger.debug("DownloadCardController::downloadCard()::entry");
        auditUtil.setAuditRequestDto(EventEnum.REQ_CARD);
        requestValidator.validateDownloadCardRequest(downloadCardRequestDTOMainRequestDTO);
        byte[] pdfBytes = downloadCardService.getDownloadCardPDF(downloadCardRequestDTOMainRequestDTO);
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(pdfBytes));
        auditUtil.setAuditRequestDto(EventEnum.GET_ACKNOWLEDGEMENT_DOWNLOAD_URL_SUCCESS);
        logger.debug("AcknowledgementController::acknowledgement()::exit");
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/pdf"))
                .header("Content-Disposition", "attachment; filename=\"" +
                        downloadCardRequestDTOMainRequestDTO.getRequest().getIndividualId() + ".pdf\"")
                .body(resource);
    }
}
