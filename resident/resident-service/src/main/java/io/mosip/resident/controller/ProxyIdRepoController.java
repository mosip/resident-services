package io.mosip.resident.controller;

import static io.mosip.resident.util.EventEnum.GET_IDENTITY_UPDATE_COUNT_EXCEPTION;
import static io.mosip.resident.util.EventEnum.GET_IDENTITY_UPDATE_COUNT_SUCCESS;

import java.util.List;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.idrepository.core.exception.IdRepoAppException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.util.AuditUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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
		try {
			ResponseWrapper<?> responseWrapper = proxySerivce
					.getRemainingUpdateCountByIndividualId(filterAttributeList);
			auditUtil.setAuditRequestDto(GET_IDENTITY_UPDATE_COUNT_SUCCESS);
			return ResponseEntity.ok(responseWrapper);
		} catch (ResidentServiceCheckedException e) {
			auditUtil.setAuditRequestDto(GET_IDENTITY_UPDATE_COUNT_EXCEPTION);
			ExceptionUtils.logRootCause(e);
			ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
			responseWrapper.setErrors(List.of(new ServiceError(e.getErrorCode(), e.getErrorText())));
			return ResponseEntity.ok(responseWrapper);
		}
	}
}
