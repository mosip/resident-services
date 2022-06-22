package io.mosip.resident.controller;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.service.ResidentCredentialService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;

/**
 * Mock API Controller class.
 *
 * @author Kamesh Shekhar Prasad
 */
@RequestMapping("/mock")
@RestController
@Tag(name = "mock-api-controller", description = "Mock API Controller")
public class MockApiController {

    @Autowired
    private AuditUtil auditUtil;

    @Autowired
    private ResidentCredentialService residentCredentialService;

    private static final Logger logger = LoggerConfiguration.logConfig(MockApiController.class);

    @PostMapping("/rid-digital-card")
    public ResponseEntity<Object> getRIDDigitalCard(
            @RequestParam("rid") String rid) throws Exception {
        auditUtil.setAuditRequestDto(EventEnum.RID_DIGITAL_CARD_REQ);
        byte[] pdfBytes = residentCredentialService.getRIDDigitalCardV2(rid);
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(pdfBytes));
        auditUtil.setAuditRequestDto(EventEnum.RID_DIGITAL_CARD_REQ_SUCCESS);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/pdf"))
                .header("Content-Disposition", "attachment; filename=\"" +
                        rid + ".pdf\"")
                .body((Object) resource);
    }
}
