package io.mosip.resident.controller;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.dto.CheckStatusResponseDTO;
import io.mosip.resident.dto.DownloadCardRequestDTO;
import io.mosip.resident.dto.DownloadPersonalizedCardDto;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.VidDownloadCardResponseDto;
import io.mosip.resident.exception.CardNotReadyException;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.DownloadCardService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.Utility;
import io.mosip.resident.validator.RequestValidator;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.util.function.Tuple2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

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
    
    @Autowired
    private Utility utility;
    
    @Autowired
    private Environment environment;

    private static final Logger logger = LoggerConfiguration.logConfig(DownloadCardController.class);

    @PostMapping("/download-card")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Card successfully downloaded", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseWrapper.class)))),
			@ApiResponse(responseCode = "400", description = "Download card failed", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
    public ResponseEntity<Object> downloadCard(@Validated @RequestBody MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO){
        logger.debug("DownloadCardController::downloadCard()::entry");
        auditUtil.setAuditRequestDto(EventEnum.REQ_CARD);
        requestValidator.validateDownloadCardRequest(downloadCardRequestDTOMainRequestDTO);
		Tuple2<byte[], String> tupleResponse = downloadCardService
				.getDownloadCardPDF(downloadCardRequestDTOMainRequestDTO);
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(tupleResponse.getT1()));
        if(tupleResponse.getT1().length==0){
            throw new CardNotReadyException();
        }
        auditUtil.setAuditRequestDto(EventEnum.GET_ACKNOWLEDGEMENT_DOWNLOAD_URL_SUCCESS);
        logger.debug("AcknowledgementController::acknowledgement()::exit");
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/pdf"))
                .header("Content-Disposition", "attachment; filename=\"" +
                        downloadCardRequestDTOMainRequestDTO.getRequest().getIndividualId() + ".pdf\"")
                .header(ResidentConstants.EVENT_ID, tupleResponse.getT2())
                .body(resource);
    }
    
    @PreAuthorize("@scopeValidator.hasAllScopes(" + "@authorizedScopes.getPostPersonalizedCard()" + ")")
    @PostMapping("/download/personalized-card")
    public ResponseEntity<Object> downloadPersonalizedCard(@Validated @RequestBody MainRequestDTO<DownloadPersonalizedCardDto> downloadPersonalizedCardMainRequestDTO,
    		@RequestHeader(name = "time-zone-offset", required = false, defaultValue = "0") int timeZoneOffset){
        logger.debug("DownloadCardController::downloadPersonalizedCard()::entry");
        auditUtil.setAuditRequestDto(EventEnum.DOWNLOAD_PERSONALIZED_CARD);
		try {
			requestValidator.validateDownloadPersonalizedCard(downloadPersonalizedCardMainRequestDTO);
		} catch (InvalidInputException e) {
			throw new ResidentServiceException(e.getErrorCode(), e.getErrorText(), e,
					Map.of(ResidentConstants.HTTP_STATUS_CODE, HttpStatus.BAD_REQUEST, ResidentConstants.REQ_RES_ID,
							environment.getProperty(ResidentConstants.MOSIP_RESIDENT_DOWNLOAD_PERSONALIZED_CARD_ID)));
		}
        Tuple2<byte[], String> tupleResponse = downloadCardService.downloadPersonalizedCard(downloadPersonalizedCardMainRequestDTO, timeZoneOffset);
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(tupleResponse.getT1()));
        if(tupleResponse.getT1().length==0){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/pdf"))
				.header("Content-Disposition", "attachment; filename=\""
						+ utility.getFileName(tupleResponse.getT2(),
								Objects.requireNonNull(this.environment.getProperty(
										ResidentConstants.DOWNLOAD_PERSONALIZED_CARD_NAMING_CONVENTION_PROPERTY)), timeZoneOffset)
						+ ".pdf\"")
				.header(ResidentConstants.EVENT_ID, tupleResponse.getT2())
                .body(resource);
    }

    @GetMapping("/request-card/vid/{VID}")
    public ResponseEntity<Object> requestVidCard(@PathVariable("VID") String vid, 
    		@RequestHeader(name = "time-zone-offset", required = false, defaultValue = "0") int timeZoneOffset) throws BaseCheckedException {
        requestValidator.validateDownloadCardVid(vid);
        Tuple2<ResponseWrapper<VidDownloadCardResponseDto>, String> tupleResponse = downloadCardService.getVidCardEventId(vid, timeZoneOffset);
        return ResponseEntity.ok()
				.header(ResidentConstants.EVENT_ID, tupleResponse.getT2())
				.body(tupleResponse.getT1());
    }

    @GetMapping("/aid-stage/{aid}")
    public ResponseEntity<Object> getStatus(@PathVariable("aid") String aid) throws BaseCheckedException, IOException {
        ResponseWrapper<CheckStatusResponseDTO> responseWrapper = downloadCardService.getIndividualIdStatus(aid);
        return ResponseEntity.ok()
                .body(responseWrapper);
    }
}
