package io.mosip.resident.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Table(name = "resident_transaction", schema = "resident")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResidentTransactionEntity {

    @Id
    private String aid;

    @NotNull
    @Column(name = "request_dtimes")
    private LocalDateTime requestDtimes;

    @NotNull
    @Column(name = "response_dtime")
    private LocalDateTime responseDtime;

    @Column(name = "request_trn_id")
    private String requestTrnId;

    @NotNull
    @Column(name = "request_type_code")
    private String requestTypeCode;

    @NotNull
    @Column(name = "request_summary")
    private String requestSummary;

    @NotNull
    @Column(name = "status_code")
    private String statusCode;

    @Column(name = "status_comment")
    private String statusComment;

    @Column(name = "lang_code")
    private String langCode;

    @Column(name = "ref_id_type")
    private String refIdType;

    @Column(name = "ref_id")
    private String refId;

    @NotNull
    @Column(name = "token_id")
    private String tokenId;

    @Column(name = "requested_entity_type")
    private String requestedEntityType;

    @Column(name = "requested_entity_id")
    private String requestedEntityId;

    @Column(name = "requested_entity_name")
    private String requestedEntityName;

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
    private Boolean isDeleted=false;

    @Column(name = "del_dtimes")
    private LocalDateTime delDtimes;

}
