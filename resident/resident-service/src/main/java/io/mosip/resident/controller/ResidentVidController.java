package io.mosip.resident.controller;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentVidRequestDto;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.VidResponseDto;
import io.mosip.resident.dto.VidRevokeRequestDTO;
import io.mosip.resident.dto.VidRevokeResponseDTO;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.validator.RequestValidator;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Resident VID controller class.
 * @Author : Monobikash Das
 */
@RestController
public class ResidentVidController {

    Logger logger = LoggerConfiguration.logConfig(ResidentVidController.class);

    @Autowired
    private ResidentVidService residentVidService;

    @Autowired
    private RequestValidator validator;
    
    @Autowired
    private AuditUtil auditUtil;

    @ResponseFilter
    @PostMapping(path = "/vid", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<VidResponseDto> generateVid(@RequestBody(required = true) ResidentVidRequestDto requestDto) throws OtpValidationFailedException, ResidentServiceCheckedException {
    	auditUtil.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST,"Request to generate VID"));
    	validator.validateVidCreateRequest(requestDto);
        auditUtil.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.GENERATE_VID,requestDto.getRequest().getIndividualId()));
        ResponseWrapper<VidResponseDto> vidResponseDto = residentVidService.generateVid(requestDto.getRequest());
        auditUtil.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.GENERATE_VID_SUCCESS,requestDto.getRequest().getIndividualId()));
        return vidResponseDto;
    }

    @ResponseFilter
    @PatchMapping(path = "/vid/{vid}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<VidRevokeResponseDTO> revokeVid(@RequestBody(required = true) RequestWrapper<VidRevokeRequestDTO> requestDto, @PathVariable String vid) throws OtpValidationFailedException, ResidentServiceCheckedException {
    	auditUtil.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST,"Request to revoke VID"));
    	validator.validateVidRevokeRequest(requestDto);
        auditUtil.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REVOKE_VID,requestDto.getRequest().getIndividualId()));
        ResponseWrapper<VidRevokeResponseDTO> vidResponseDto = residentVidService.revokeVid(requestDto.getRequest(),vid);
        auditUtil.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REVOKE_VID_SUCCESS,requestDto.getRequest().getIndividualId()));
        return vidResponseDto;
    }
}
