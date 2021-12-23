package io.mosip.resident.controller;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.dto.*;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.validator.RequestValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Resident VID controller class.
 * @Author : Monobikash Das
 */
@RestController
@Tag(name = "resident-vid-controller", description = "Resident Vid Controller")
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
    @Operation(summary = "generateVid", description = "generateVid", tags = { "resident-vid-controller" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
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
    @Operation(summary = "revokeVid", description = "revokeVid", tags = { "resident-vid-controller" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "No Content" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<VidRevokeResponseDTO> revokeVid(@RequestBody(required = true) RequestWrapper<VidRevokeRequestDTO> requestDto, @PathVariable String vid) throws OtpValidationFailedException, ResidentServiceCheckedException {
    	auditUtil.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST,"Request to revoke VID"));
    	validator.validateVidRevokeRequest(requestDto);
        auditUtil.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REVOKE_VID,requestDto.getRequest().getIndividualId()));
        ResponseWrapper<VidRevokeResponseDTO> vidResponseDto = residentVidService.revokeVid(requestDto.getRequest(),vid);
        auditUtil.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.REVOKE_VID_SUCCESS,requestDto.getRequest().getIndividualId()));
        return vidResponseDto;
    }
}