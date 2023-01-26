package io.mosip.resident.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This entity class defines the database table details for
 * resident_user_actions table.
 *
 * @author Neha Farheen
 * @since 1.2.0.1
 *
 */
@Data
@Table(name = "resident_session", schema = "resident")
@Entity
@NoArgsConstructor

public class ResidentSessionEntity {
	@Id
	@Column(name = "session_id")
	private String sessionId;
	
	@Column(name = "ida_token")
	private String idaToken;

	@Column(name = "login_dtimes")
	private LocalDateTime loginDtimes;
	
	@Column(name = "ip_address")
	private String ipAddress;
	
	@Column(name = "host")
	private String host;
	
	@Column(name = "machine_type")
	private String machineType;

	public ResidentSessionEntity(String sessionId, String idaToken, LocalDateTime loginDtimes,
			String ipAddress, String host, String machineType) {
		this.sessionId = sessionId;
		this.idaToken = idaToken;
		this.loginDtimes = loginDtimes;
		this.ipAddress = ipAddress;
		this.host = host;
		this.machineType = machineType;
	}
}
