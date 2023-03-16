package io.mosip.resident.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This entity class defines the database table details for resident_grievance_ticket
 * table.
 *
 * @author Kamesh Shekhar Prasad
 * @since 1.2.0.1
 *
 */

@Data
@Table(name = "resident_grievance_ticket", schema = "resident")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResidentGrievanceEntity {

	@Id
	@Column(name = "id")
	private String id;

	@NotNull
	@Column(name = "eventId")
	private String eventId;

	@NotNull
	@Column(name = "name")
	private String name;

	@Column(name = "emailId")
	private String emailId;

	@Column(name = "alternateEmailId")
	private String alternateEmailId;

	@Column(name = "phoneNo")
	private String phoneNo;

	@Column(name = "alternatePhoneNo")
	private String alternatePhoneNo;

	@NotNull
	@Column(name = "message")
	private String message;

	@NotNull
	@Column(name = "hasAttachment")
	private boolean hasAttachment = false;

	@NotNull
	@Column(name = "status")
	private String status;

	@NotNull
	@Column(name = "cr_by")
	private String crBy;

	@NotNull
	@Column(name = "cr_dtimes")
	private LocalDateTime crDtimes;

	@Column(name = "upd_by")
	private String updBy;

	@Column(name = "upd_dtimes")
	private LocalDateTime updDtimes;

	@NotNull
	@Column(name = "is_deleted")
	private Boolean isDeleted = false;

	@Column(name = "del_dtimes")
	private LocalDateTime delDtimes;

}