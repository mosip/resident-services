package io.mosip.resident.controller;

import io.micrometer.core.annotation.Timed;
import io.mosip.idrepository.core.exception.IdRepoAppException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.dto.DraftResidentResponseDto;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.service.impl.PendingDrafts;
import io.mosip.resident.service.impl.RemainingUpdateCountByIndividualId;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.validator.RequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Nullable;
import java.util.List;

import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_DESCRIPTION;
import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_ID;
import static io.mosip.resident.util.AuditEnum.DISCARD_DRAFT_EXCEPTION;
import static io.mosip.resident.util.AuditEnum.DISCARD_DRAFT_SUCCESS;
import static io.mosip.resident.util.AuditEnum.GET_IDENTITY_UPDATE_COUNT_EXCEPTION;
import static io.mosip.resident.util.AuditEnum.GET_IDENTITY_UPDATE_COUNT_SUCCESS;
import static io.mosip.resident.util.AuditEnum.GET_PENDING_DRAFT_FAILURE;
import static io.mosip.resident.util.AuditEnum.GET_PENDING_DRAFT_SUCCESS;

/**
 * @author Manoj SP
 *
 */
@RestController
@RequestMapping("/identity")
@Tag(name = "proxy-idrepo-controller", description = "Proxy IdRepo Controller")
public class ProxyIdRepoController {

	@Autowired
	private ProxyIdRepoService proxySerivce;

	@Autowired
	private AuditUtil auditUtil;

	@Autowired
	private RequestValidator requestValidator;

	@Autowired
	private Environment environment;

	@Autowired
	private RemainingUpdateCountByIndividualId remainingUpdateCountByIndividualId;

	@Autowired
	private PendingDrafts pendingDrafts;

	private static final Logger logger = LoggerConfiguration.logConfig(ProxyIdRepoController.class);

	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @GetMapping(path = "/update-count", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Get Remaining update count by Individual Id Request", description = "Get Remaining update count by Individual Id Request", tags = {
			"proxy-id-repo-identity-update-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Request authenticated successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = IdRepoAppException.class)))),
			@ApiResponse(responseCode = "400", description = "No Records Found", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<ResponseWrapper<?>> getRemainingUpdateCountByIndividualId(
			@RequestParam(name = "filter_attribute_list", required = false) @Nullable List<String> filterAttributeList) {
		logger.debug("ProxyIdRepoController::getRemainingUpdateCountByIndividualId()::entry");
		try {
			ResponseWrapper<?> responseWrapper = remainingUpdateCountByIndividualId
					.getRemainingUpdateCountByIndividualId(filterAttributeList);
			auditUtil.setAuditRequestDto(GET_IDENTITY_UPDATE_COUNT_SUCCESS);
			logger.debug("ProxyIdRepoController::getRemainingUpdateCountByIndividualId()::exit");
			return ResponseEntity.ok(responseWrapper);
		} catch (ResidentServiceCheckedException e) {
			auditUtil.setAuditRequestDto(GET_IDENTITY_UPDATE_COUNT_EXCEPTION);
			ExceptionUtils.logRootCause(e);
			ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
			responseWrapper.setErrors(List.of(new ServiceError(e.getErrorCode(), e.getErrorText())));
			return ResponseEntity.ok(responseWrapper);
		}
	}

	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
	@GetMapping(path = "/get-pending-drafts/{langCode}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Get Pending Drafts", description = "Get Pending Drafts", tags = {
			"proxy-id-repo-identity-update-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Request authenticated successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = IdRepoAppException.class)))),
			@ApiResponse(responseCode = "400", description = "No Records Found", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<ResponseWrapper<DraftResidentResponseDto>> getPendingDrafts(@PathVariable String langCode) {
		logger.debug("ProxyIdRepoController::getPendingDrafts()::entry");
		try {
			requestValidator.validateLanguageCode(langCode);
			ResponseWrapper<DraftResidentResponseDto> responseWrapper = pendingDrafts
					.getPendingDrafts(langCode);
			auditUtil.setAuditRequestDto(GET_PENDING_DRAFT_SUCCESS);
			requestValidator.validateLanguageCode(langCode);
			logger.debug("ProxyIdRepoController::getPendingDrafts()::exit");
			return ResponseEntity.ok(responseWrapper);
		} catch (ResidentServiceCheckedException e) {
			auditUtil.setAuditRequestDto(GET_PENDING_DRAFT_FAILURE);
			ExceptionUtils.logRootCause(e);
			ResponseWrapper<DraftResidentResponseDto> responseWrapper = new ResponseWrapper<>();
			responseWrapper.setErrors(List.of(new ServiceError(e.getErrorCode(), e.getErrorText())));
			return ResponseEntity.ok(responseWrapper);
		}
	}

	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
	@PostMapping(path = "/discardPendingDraft/{eid}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Discard pending draft", description = "Discard pending draft", tags = {
			"proxy-id-repo-identity-update-controller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Request authenticated successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = IdRepoAppException.class)))),
			@ApiResponse(responseCode = "400", description = "No Records Found", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<Object> discardPendingDraft(
			@PathVariable String eid) {
		logger.debug("ProxyIdRepoController::discardPendingDraft()::entry");
		ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setId(environment.getProperty(ResidentConstants.DISCARD_DRAFT_ID));
		responseWrapper.setVersion(environment.getProperty(ResidentConstants.DISCARD_DRAFT_VERSION));
		try {
			requestValidator.validateEventId(eid);
			auditUtil.setAuditRequestDto(DISCARD_DRAFT_SUCCESS);
			logger.debug("ProxyIdRepoController::discardPendingDraft()::exit");
			responseWrapper.setResponse(proxySerivce
					.discardDraft(eid));
		} catch (ResidentServiceCheckedException e) {
			auditUtil.setAuditRequestDto(DISCARD_DRAFT_EXCEPTION);
			ExceptionUtils.logRootCause(e);
			responseWrapper.setErrors(List.of(new ServiceError(e.getErrorCode(), e.getErrorText())));
		}
		return ResponseEntity.ok(responseWrapper);
	}
}
