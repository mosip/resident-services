package io.mosip.resident.controller;

import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.dto.AuditRequestDTO;
import io.mosip.resident.dto.AuthenticatedAuditRequestDto;
import io.mosip.resident.dto.UnauthenticatedAuditRequestDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.IdentityService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.Utility;
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
	private Utility utility;

	/**
	 * Auth audit log.
	 *
	 * @param requestDto the authenticated audit request dto
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
	public ResponseEntity<?> authAuditLog(@RequestBody AuthenticatedAuditRequestDto requestDto, HttpServletRequest req)
			throws ResidentServiceCheckedException, ApisResourceAccessException, NoSuchAlgorithmException {
		AuditRequestDTO auditRequestDto=new AuditRequestDTO();
		auditRequestDto.setEventId(requestDto.getAuditEventId());
		auditRequestDto.setEventName(requestDto.getAuditEventName());
		auditRequestDto.setEventType(requestDto.getAuditEventType());
		auditRequestDto.setActionTimeStamp(requestDto.getActionTimeStamp());
		auditRequestDto.setHostName(req.getRemoteHost());
		auditRequestDto.setHostIp(utility.getClientIp(req));
		auditRequestDto.setApplicationId(requestDto.getApplicationId());
		auditRequestDto.setApplicationName(requestDto.getApplicationName());
		auditRequestDto.setSessionUserId(requestDto.getSessionUserId());
		auditRequestDto.setSessionUserName(requestDto.getSessionUserName());
		String individualId = identityService.getResidentIndvidualId();
		auditRequestDto.setId(utility.getRefIdHash(individualId));
		auditRequestDto.setIdType(identityService.getIndividualIdType(individualId));
		auditRequestDto.setCreatedBy(requestDto.getCreatedBy());
		auditRequestDto.setModuleName(requestDto.getModuleName());
		auditRequestDto.setModuleId(requestDto.getModuleId());
		auditRequestDto.setDescription(requestDto.getDescription());
		auditUtil.callAuditManager(auditRequestDto);
		return ResponseEntity.ok().build();
	}

	/**
	 * Audit log.
	 * 
	 * @param requestDto the unauthenticated audit request dto
	 * @return the response entity
	 * @throws ResidentServiceCheckedException
	 * @throws ApisResourceAccessException
	 * @throws NoSuchAlgorithmException
	 */
	@ResponseFilter
	@PostMapping("/proxy/audit/log")
	@Operation(summary = "auditLog", description = "audit log", tags = { "proxy-audit-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<?> auditLog(@RequestBody UnauthenticatedAuditRequestDto requestDto, HttpServletRequest req)
			throws ResidentServiceCheckedException, ApisResourceAccessException, NoSuchAlgorithmException {
		AuditRequestDTO auditRequestDto=new AuditRequestDTO();
		auditRequestDto.setEventId(requestDto.getAuditEventId());
		auditRequestDto.setEventName(requestDto.getAuditEventName());
		auditRequestDto.setEventType(requestDto.getAuditEventType());
		auditRequestDto.setActionTimeStamp(requestDto.getActionTimeStamp());
		auditRequestDto.setHostName(req.getRemoteHost());
		auditRequestDto.setHostIp(utility.getClientIp(req));
		auditRequestDto.setApplicationId(requestDto.getApplicationId());
		auditRequestDto.setApplicationName(requestDto.getApplicationName());
		auditRequestDto.setSessionUserId(requestDto.getSessionUserId());
		auditRequestDto.setSessionUserName(requestDto.getSessionUserName());
		if (requestDto.getId() != null && !StringUtils.isEmpty(requestDto.getId())) {
			auditRequestDto.setId(utility.getRefIdHash(requestDto.getId()));
			auditRequestDto.setIdType(identityService.getIndividualIdType(requestDto.getId()));
		} else {
			auditRequestDto.setId(ResidentConstants.NO_ID);
			auditRequestDto.setIdType(ResidentConstants.NO_ID_TYPE);
		}
		auditRequestDto.setCreatedBy(requestDto.getCreatedBy());
		auditRequestDto.setModuleName(requestDto.getModuleName());
		auditRequestDto.setModuleId(requestDto.getModuleId());
		auditRequestDto.setDescription(requestDto.getDescription());
		auditUtil.callAuditManager(auditRequestDto);
		return ResponseEntity.ok().build();
	}

}
