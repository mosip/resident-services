package io.mosip.resident.controller;

import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_DESCRIPTION;
import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_ID;

import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.annotation.Timed;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.dto.AuditRequestDTO;
import io.mosip.resident.dto.AuthenticatedAuditRequestDto;
import io.mosip.resident.dto.UnauthenticatedAuditRequestDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.Utility;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.util.function.Tuple2;

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
	private Utility utility;

	/**
	 * Auth audit log.
	 *
	 * @param requestDto the authenticated audit request dto
	 * @return the response entity
	 */
	@ResponseFilter
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PostMapping("/auth-proxy/audit/log")
	@Operation(summary = "authAuditLog", description = "auth audit log", tags = { "proxy-audit-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<Object> authAuditLog(@RequestBody AuthenticatedAuditRequestDto requestDto, HttpServletRequest req) {
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
		Tuple2<String, String> refIdHashAndType = auditUtil.getRefIdHashAndType();
		auditRequestDto.setId(refIdHashAndType.getT1());
		auditRequestDto.setIdType(refIdHashAndType.getT2());
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
	 * @throws NoSuchAlgorithmException
	 */
	@ResponseFilter
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PostMapping("/proxy/audit/log")
	@Operation(summary = "auditLog", description = "audit log", tags = { "proxy-audit-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<Object> auditLog(@RequestBody UnauthenticatedAuditRequestDto requestDto, HttpServletRequest req)
			throws NoSuchAlgorithmException {
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
			Tuple2<String, String> refIdHashAndType = auditUtil.getRefIdHashAndTypeFromIndividualId(requestDto.getId());
			auditRequestDto.setId(refIdHashAndType.getT1());
			auditRequestDto.setIdType(refIdHashAndType.getT2());
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
