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
@Table(name = "resident_user_actions", schema = "resident")
@Entity
@NoArgsConstructor

public class ResidentUserEntity {
	@Id
	@Column(name = "session_id")
	private String sessionId;
	
	@Column(name = "ida_token")
	private String idaToken;

	@Column(name = "last_bell_notif_click_dtimes")
	private LocalDateTime lastbellnotifDtimes;

	@Column(name = "last_login_dtimes")
	private LocalDateTime lastloginDtime;
	
	@Column(name = "ip_address")
	private String ipAddress;
	
	@Column(name = "host")
	private String host;
	
	@Column(name = "machine_type")
	private String machineType;

	public ResidentUserEntity(String sessionId, String idaToken, LocalDateTime lastbellnotifDtimes, LocalDateTime lastloginDtime,
			String ipAddress, String host, String machineType) {
		this.sessionId = sessionId;
		this.idaToken = idaToken;
		this.lastbellnotifDtimes = lastbellnotifDtimes;
		this.lastloginDtime = lastloginDtime;
		this.ipAddress = ipAddress;
		this.host = host;
		this.machineType = machineType;
	}
}
