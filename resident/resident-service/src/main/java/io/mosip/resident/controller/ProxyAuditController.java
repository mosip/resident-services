package io.mosip.resident.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.dto.AuditRequestDTO;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.util.AuditUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/proxy/audit")
@Tag(name = "proxy-audit-controller", description = "Proxy Audit Controller")
public class ProxyAuditController {
	
	@Autowired
	private AuditUtil auditUtil;
	
	@ResponseFilter
	@GetMapping("/log")
	@Operation(summary = "getImmediateChildrenByLocCodeAndLangCode", description = "getImmediateChildrenByLocCodeAndLangCode", tags = {
			"proxy-masterdata-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<?> getImmediateChildrenByLocCodeAndLangCode(
			@RequestBody AuditRequestDTO auditRequestDto)
			throws ResidentServiceCheckedException {
		auditUtil.callAuditManager(auditRequestDto);
		return ResponseEntity.ok().build();
	}

}
