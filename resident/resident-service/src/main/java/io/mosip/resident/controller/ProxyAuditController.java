package io.mosip.resident.controller;

import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.dto.AuditRequestDTO;
import io.mosip.resident.dto.AuditRequestDtoV2;
import io.mosip.resident.dto.AuditRequestDtoV3;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.IdentityService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.Utilitiy;
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
@Tag(name = "proxy-audit-controller", description = "Proxy Audit Controller")
public class ProxyAuditController {

	/** The audit util. */
	@Autowired
	private AuditUtil auditUtil;
	
	@Autowired
	private IdentityService identityService;
	
	@Autowired
	private Utilitiy utility;

	/**
	 * Audit log.
	 *
	 * @param auditRequestDtoV2 the audit request dto
	 * @return the response entity
	 * @throws ResidentServiceCheckedException the resident service checked exception
	 * @throws ApisResourceAccessException 
	 * @throws NoSuchAlgorithmException 
	 */
	@ResponseFilter
	@PostMapping("/auth-proxy/audit/log")
	@Operation(summary = "authAuditLog", description = "auth audit log", tags = { "proxy-audit-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<?> authAuditLog(@RequestBody AuditRequestDtoV2 auditRequestDtoV2)
			throws ResidentServiceCheckedException, ApisResourceAccessException, NoSuchAlgorithmException {
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
		String individualId = identityService.getResidentIndvidualId();
		auditRequestDto.setId(utility.getRefIdHash(individualId));
		auditRequestDto.setIdType(identityService.getIndividualIdType(individualId));
		auditRequestDto.setCreatedBy(auditRequestDtoV2.getCreatedBy());
		auditRequestDto.setModuleName(auditRequestDtoV2.getModuleName());
		auditRequestDto.setModuleId(auditRequestDtoV2.getModuleId());
		auditRequestDto.setDescription(auditRequestDtoV2.getDescription());
		auditUtil.callAuditManager(auditRequestDto);
		return ResponseEntity.ok().build();
	}
	
	@ResponseFilter
	@PostMapping("/proxy/audit/log")
	@Operation(summary = "auditLog", description = "audit log", tags = { "proxy-audit-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<?> auditLog(@RequestBody AuditRequestDtoV3 auditRequestDtoV3)
			throws ResidentServiceCheckedException, ApisResourceAccessException, NoSuchAlgorithmException {
		AuditRequestDTO auditRequestDto=new AuditRequestDTO();
		auditRequestDto.setEventId(auditRequestDtoV3.getAuditEventId());
		auditRequestDto.setEventName(auditRequestDtoV3.getAuditEventName());
		auditRequestDto.setEventType(auditRequestDtoV3.getAuditEventType());
		auditRequestDto.setActionTimeStamp(auditRequestDtoV3.getActionTimeStamp());
		auditRequestDto.setHostName(auditRequestDtoV3.getHostName());
		auditRequestDto.setHostIp(auditRequestDtoV3.getHostIp());
		auditRequestDto.setApplicationId(auditRequestDtoV3.getApplicationId());
		auditRequestDto.setApplicationName(auditRequestDtoV3.getApplicationName());
		auditRequestDto.setSessionUserId(auditRequestDtoV3.getSessionUserId());
		auditRequestDto.setSessionUserName(auditRequestDtoV3.getSessionUserName());
		if (auditRequestDtoV3.getId() != null && !StringUtils.isEmpty(auditRequestDtoV3.getId())) {
			auditRequestDto.setId(utility.getRefIdHash(auditRequestDtoV3.getId()));
			auditRequestDto.setIdType(identityService.getIndividualIdType(auditRequestDtoV3.getId()));
		} else {
			auditRequestDto.setId(ResidentConstants.NO_ID);
			auditRequestDto.setIdType(ResidentConstants.NO_ID_TYPE);
		}
		auditRequestDto.setCreatedBy(auditRequestDtoV3.getCreatedBy());
		auditRequestDto.setModuleName(auditRequestDtoV3.getModuleName());
		auditRequestDto.setModuleId(auditRequestDtoV3.getModuleId());
		auditRequestDto.setDescription(auditRequestDtoV3.getDescription());
		auditUtil.callAuditManager(auditRequestDto);
		return ResponseEntity.ok().build();
	}

}
