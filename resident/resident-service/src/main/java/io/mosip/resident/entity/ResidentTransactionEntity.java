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

/**
 * This entity class defines the database table details for resident_transaction table.
 *
 * @author Kamesh Shekhar Prasad
 * @since 1.2.0.1
 *
 */

@Data
@Table(name = "resident_transaction", schema = "resident")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResidentTransactionEntity {

    @Id
    @Column(name = "event_id")
    private String eventId;

    @Column(name = "request_trn_id")
    private String requestTrnId;

    @Column(name = "aid")
    private String aid;

    @NotNull
    @Column(name = "request_dtimes")
    private LocalDateTime requestDtimes;

    @NotNull
    @Column(name = "response_dtime")
    private LocalDateTime responseDtime;

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

    @Column(name = "auth_type_code")
    private String authTypeCode;

    @Column(name = "static_tkn_id")
    private String authTknId;

    @Column(name = "request_signature")
    private String requestSignature;

    @Column(name = "response_signature")
    private String responseSignature;

    @Column(name = "olv_partner_id")
    private String olvPartnerId;
    
    @Column(name = "reference_link")
    private String referenceLink;

    @NotNull
    @Column(name = "read_status")
    private boolean readStatus=false;

    @NotNull
    @Column(name = "pinned_status")
    private boolean pinnedStatus=false;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "credential_request_id")
    private String credentialRequestId;
    
    @Column(name = "attribute_list")
    private String attributeList;

    @Column(name = "individual_id")
    private String individualId;

    /**
     * The constructor used in retrieval of the specific fields.
     * @param requestTrnId
     * @param requestDTtimes
     * @param authTypeCode
     * @param statusCode
     * @param statusComment
     * @param refId
     * @param entityName
     */
    public ResidentTransactionEntity(String requestTrnId, LocalDateTime requestDTtimes, String authTypeCode, String statusCode,
                   String statusComment, String refId, String refIdType, String entityName, String requestSignature,
                   String responseSignature) {
        this.requestTrnId = requestTrnId;
        this.requestDtimes = requestDTtimes;
        this.authTypeCode = authTypeCode;
        this.statusCode = statusCode;
        this.statusComment = statusComment;
        this.refId = refId;
        this.refIdType = refIdType;
        this.requestedEntityName = entityName;
        this.requestSignature = requestSignature;
        this.responseSignature = responseSignature;
    }

    /**
     * The constructor used in retrieval of the specific fields.
     *
     */
    public ResidentTransactionEntity(String requestTrnId, String statusComment ,LocalDateTime crDtimes, String statusCode) {
        this.requestTrnId = requestTrnId;
        this.statusComment = statusComment;
        this.crDtimes = crDtimes;
        this.statusCode = statusCode;
    }

    public ResidentTransactionEntity(String eventId, String statusComment ,LocalDateTime crDtimes,
                                     String statusCode, LocalDateTime updDtimes, String requestTypeCode) {
        this.eventId = eventId;
        this.statusComment = statusComment;
        this.crDtimes = crDtimes;
        this.statusCode = statusCode;
        this.updDtimes = updDtimes;
        this.requestTypeCode = requestTypeCode;
    }

    public ResidentTransactionEntity(String aid) {
        this.aid = aid;
    }

}
