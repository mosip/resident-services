package io.mosip.resident.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The audit request dto v2.
 * 
 * @author Ritik Jain
 */
@Data
@NoArgsConstructor
public class AuditRequestDtoV2 {
	/** The event id. */
	@NotNull
	@Size(min = 1, max = 64)
	private String auditEventId;

	/** The event name. */
	@NotNull
	@Size(min = 1, max = 128)
	private String auditEventName;

	/** The event type. */
	@NotNull
	@Size(min = 1, max = 64)
	private String auditEventType;

	/** The action time stamp. */
	@NotNull
	private LocalDateTime actionTimeStamp;

	/** The host name. */
	@NotNull
	@Size(min = 1, max = 32)
	private String hostName;

	/** The host ip. */
	@NotNull
	@Size(min = 1, max = 16)
	private String hostIp;

	/** The application id. */
	@NotNull
	@Size(min = 1, max = 64)
	private String applicationId;

	/** The application name. */
	@NotNull
	@Size(min = 1, max = 128)
	private String applicationName;

	/** The session user id. */
	@NotNull
	@Size(min = 1, max = 64)
	private String sessionUserId;

	/** The session user name. */
	@Size(min = 1, max = 128)
	private String sessionUserName;

	/** The created by. */
	@NotNull
	@Size(min = 1, max = 255)
	private String createdBy;

	/** The module name. */
	@Size(max = 128)
	private String moduleName;

	/** The module id. */
	@Size(max = 64)
	private String moduleId;

	/** The description. */
	@Size(max = 2048)
	private String description;
}
