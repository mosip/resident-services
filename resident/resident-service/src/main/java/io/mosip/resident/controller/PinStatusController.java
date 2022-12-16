package io.mosip.resident.controller;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.dto.ResponseDTO;
import io.mosip.resident.service.PinUnpinStatusService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.validator.RequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class is used to pin or unpin status based on event id.
 * @Author Kamesh Shekhar Prasad
 */
@RestController
public class PinStatusController {

    private static final Logger logger = LoggerConfiguration.logConfig(PinStatusController.class);
    @Autowired
    private AuditUtil audit;

    @Autowired
    private RequestValidator requestValidator;

    @Autowired
    private PinUnpinStatusService pinUnpinStatusService;

    @PreAuthorize("@scopeValidator.hasAllScopes("
            + "@authorizedScopes.getPostPinStatus()"
            + ")")
    @PostMapping(path = "pinned/{eventId}")
    public ResponseWrapper<ResponseDTO> pinStatus(@PathVariable("eventId") String eventId){
        audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "Pin Status API"));
        requestValidator.validateEventId(eventId);
        audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.PIN_STATUS, eventId));
        return pinUnpinStatusService.pinStatus(eventId, true);
    }

    @PreAuthorize("@scopeValidator.hasAllScopes("
            + "@authorizedScopes.getPostUnPinStatus()"
            + ")")
    @PostMapping(path = "unpinned/{eventId}")
    public ResponseWrapper<ResponseDTO> unPinStatus(@PathVariable("eventId") String eventId){
        audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "Pin Status API"));
        requestValidator.validateEventId(eventId);
        audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.UN_PIN_STATUS, eventId));
        return pinUnpinStatusService.pinStatus(eventId, false);
    }
}
