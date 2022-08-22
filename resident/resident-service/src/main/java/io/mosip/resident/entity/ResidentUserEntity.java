package io.mosip.resident.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Builder;
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
@Builder
@NoArgsConstructor

public class ResidentUserEntity {
	@Id
	@Column(name = "ida_token")
	private String idaToken;

	@NotNull
	@Column(name = "last_bell_notif_click_dtimes")
	LocalDateTime lastbellnotifDtimes;

	@NotNull
	@Column(name = "last_login_dtimes")
	private String lastloginDtime;

	/**
	 * The constructor used in retrieval of the specific fields.
	 * 
	 * @param idaToken
	 * @param lastbellnotifDtimes
	 * @param lastloginDtime
	 * 
	 */
	public ResidentUserEntity(String idaToken, LocalDateTime lastbellnotifDtimes, String lastloginDtime) {
		this.idaToken = idaToken;
		this.lastbellnotifDtimes = lastbellnotifDtimes;
		this.lastloginDtime = lastloginDtime;

	}
}
