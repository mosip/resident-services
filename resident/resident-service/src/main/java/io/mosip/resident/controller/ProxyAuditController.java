package io.mosip.resident.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.resident.dto.AuditRequestDTO;
import io.mosip.resident.dto.AuditRequestDtoV2;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.util.AuditUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Audit log proxy.
 *
 * @author Loganathan.S
 */
@RestController
@RequestMapping("/auth-proxy/audit")
@Tag(name = "proxy-audit-controller", description = "Proxy Audit Controller")
public class ProxyAuditController {

	/** The audit util. */
	@Autowired
	private AuditUtil auditUtil;

	/**
	 * Audit log.
	 *
	 * @param auditRequestDtoV2 the audit request dto
	 * @return the response entity
	 * @throws ResidentServiceCheckedException the resident service checked
	 *                                         exception
	 */
	@ResponseFilter
	@PostMapping("/log")
	@Operation(summary = "auditLog", description = "audit log", tags = { "proxy-audit-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<?> auditLog(@RequestBody AuditRequestDtoV2 auditRequestDtoV2)
			throws ResidentServiceCheckedException {
		AuditRequestDTO auditRequestDto=new AuditRequestDTO();
		auditRequestDto.setEventId(auditRequestDtoV2.getAuditEventId());
		auditRequestDto.setEventName(auditRequestDtoV2.getAuditEventName());
		auditRequestDto.setEventType(auditRequestDtoV2.getAuditEventType());
		auditRequestDto.setActionTimeStamp(auditRequestDtoV2.getActionTimeStamp());
		auditRequestDto.setHostName(auditRequestDtoV2.getHostName());
		auditRequestDto.setHostIp(auditRequestDtoV2.getHostIp());
		auditRequestDto.setApplicationId(auditRequestDtoV2.getApplicationId());
		auditRequestDto.setApplicationName(auditRequestDtoV2.getApplicationName());
		auditRequestDto.setSessionUserId(auditRequestDtoV2.getSessionUserId());
		auditRequestDto.setSessionUserName(auditRequestDtoV2.getSessionUserName());
		auditRequestDto.setId(auditRequestDtoV2.getId());
		auditRequestDto.setIdType(auditRequestDtoV2.getIdType());
		auditRequestDto.setCreatedBy(auditRequestDtoV2.getCreatedBy());
		auditRequestDto.setModuleName(auditRequestDtoV2.getModuleName());
		auditRequestDto.setModuleId(auditRequestDtoV2.getModuleId());
		auditRequestDto.setDescription(auditRequestDtoV2.getDescription());
		auditUtil.callAuditManager(auditRequestDto);
		return ResponseEntity.ok().build();
	}

}
