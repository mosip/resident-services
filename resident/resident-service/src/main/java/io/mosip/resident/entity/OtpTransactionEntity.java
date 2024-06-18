package io.mosip.resident.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * This class is used to create entity for otp_transaction table.
 * @author Kamesh Shekhar Prasad
 */
@Table(
        name = "otp_transaction",
        schema = "resident"
)
@Entity
public class OtpTransactionEntity {
    @Id
    private String id;
    @Column(
            name = "ref_id"
    )
    private String refId;
    @Column(
            name = "otp_hash"
    )
    private String otpHash;
    @Column(
            name = "generated_dtimes"
    )
    private LocalDateTime generatedDtimes;
    @Column(
            name = "expiry_dtimes"
    )
    private LocalDateTime expiryDtimes;
    @Column(
            name = "validation_retry_count"
    )
    private Integer validationRetryCount;
    @Column(
            name = "status_code"
    )
    private String statusCode;
    @Column(
            name = "lang_code"
    )
    private String langCode;
    @Column(
            name = "cr_by"
    )
    private String crBy;
    @Column(
            name = "cr_dtimes"
    )
    private LocalDateTime crDtimes;
    @Column(
            name = "upd_by"
    )
    private String updBy;
    @Column(
            name = "upd_dtimes"
    )
    private LocalDateTime updDTimes;
    @Column(
            name = "is_deleted"
    )
    private Boolean isDeleted;
    @Column(
            name = "del_dtimes"
    )
    private LocalDateTime delDtimes;

    public OtpTransactionEntity() {
    }

    public String getId() {
        return this.id;
    }

    public String getRefId() {
        return this.refId;
    }

    public String getOtpHash() {
        return this.otpHash;
    }

    public LocalDateTime getGeneratedDtimes() {
        return this.generatedDtimes;
    }

    public LocalDateTime getExpiryDtimes() {
        return this.expiryDtimes;
    }

    public Integer getValidationRetryCount() {
        return this.validationRetryCount;
    }

    public String getStatusCode() {
        return this.statusCode;
    }

    public String getLangCode() {
        return this.langCode;
    }

    public String getCrBy() {
        return this.crBy;
    }

    public LocalDateTime getCrDtimes() {
        return this.crDtimes;
    }

    public String getUpdBy() {
        return this.updBy;
    }

    public LocalDateTime getUpdDTimes() {
        return this.updDTimes;
    }

    public Boolean getIsDeleted() {
        return this.isDeleted;
    }

    public LocalDateTime getDelDtimes() {
        return this.delDtimes;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public void setGeneratedDtimes(LocalDateTime generatedDtimes) {
        this.generatedDtimes = generatedDtimes;
    }

    public void setExpiryDtimes(LocalDateTime expiryDtimes) {
        this.expiryDtimes = expiryDtimes;
    }

    public void setValidationRetryCount(Integer validationRetryCount) {
        this.validationRetryCount = validationRetryCount;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

    public void setCrBy(String crBy) {
        this.crBy = crBy;
    }

    public void setCrDtimes(LocalDateTime crDtimes) {
        this.crDtimes = crDtimes;
    }

    public void setUpdBy(String updBy) {
        this.updBy = updBy;
    }

    public void setUpdDTimes(LocalDateTime updDTimes) {
        this.updDTimes = updDTimes;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public void setDelDtimes(LocalDateTime delDtimes) {
        this.delDtimes = delDtimes;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof OtpTransactionEntity)) {
            return false;
        } else {
            OtpTransactionEntity other = (OtpTransactionEntity)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$id = this.getId();
                Object other$id = other.getId();
                if (this$id == null) {
                    if (other$id != null) {
                        return false;
                    }
                } else if (!this$id.equals(other$id)) {
                    return false;
                }

                Object this$refId = this.getRefId();
                Object other$refId = other.getRefId();
                if (this$refId == null) {
                    if (other$refId != null) {
                        return false;
                    }
                } else if (!this$refId.equals(other$refId)) {
                    return false;
                }

                Object this$otpHash = this.getOtpHash();
                Object other$otpHash = other.getOtpHash();
                if (this$otpHash == null) {
                    if (other$otpHash != null) {
                        return false;
                    }
                } else if (!this$otpHash.equals(other$otpHash)) {
                    return false;
                }

                label158: {
                    Object this$generatedDtimes = this.getGeneratedDtimes();
                    Object other$generatedDtimes = other.getGeneratedDtimes();
                    if (this$generatedDtimes == null) {
                        if (other$generatedDtimes == null) {
                            break label158;
                        }
                    } else if (this$generatedDtimes.equals(other$generatedDtimes)) {
                        break label158;
                    }

                    return false;
                }

                label151: {
                    Object this$expiryDtimes = this.getExpiryDtimes();
                    Object other$expiryDtimes = other.getExpiryDtimes();
                    if (this$expiryDtimes == null) {
                        if (other$expiryDtimes == null) {
                            break label151;
                        }
                    } else if (this$expiryDtimes.equals(other$expiryDtimes)) {
                        break label151;
                    }

                    return false;
                }

                Object this$validationRetryCount = this.getValidationRetryCount();
                Object other$validationRetryCount = other.getValidationRetryCount();
                if (this$validationRetryCount == null) {
                    if (other$validationRetryCount != null) {
                        return false;
                    }
                } else if (!this$validationRetryCount.equals(other$validationRetryCount)) {
                    return false;
                }

                label137: {
                    Object this$statusCode = this.getStatusCode();
                    Object other$statusCode = other.getStatusCode();
                    if (this$statusCode == null) {
                        if (other$statusCode == null) {
                            break label137;
                        }
                    } else if (this$statusCode.equals(other$statusCode)) {
                        break label137;
                    }

                    return false;
                }

                label130: {
                    Object this$langCode = this.getLangCode();
                    Object other$langCode = other.getLangCode();
                    if (this$langCode == null) {
                        if (other$langCode == null) {
                            break label130;
                        }
                    } else if (this$langCode.equals(other$langCode)) {
                        break label130;
                    }

                    return false;
                }

                Object this$crBy = this.getCrBy();
                Object other$crBy = other.getCrBy();
                if (this$crBy == null) {
                    if (other$crBy != null) {
                        return false;
                    }
                } else if (!this$crBy.equals(other$crBy)) {
                    return false;
                }

                Object this$crDtimes = this.getCrDtimes();
                Object other$crDtimes = other.getCrDtimes();
                if (this$crDtimes == null) {
                    if (other$crDtimes != null) {
                        return false;
                    }
                } else if (!this$crDtimes.equals(other$crDtimes)) {
                    return false;
                }

                label109: {
                    Object this$updBy = this.getUpdBy();
                    Object other$updBy = other.getUpdBy();
                    if (this$updBy == null) {
                        if (other$updBy == null) {
                            break label109;
                        }
                    } else if (this$updBy.equals(other$updBy)) {
                        break label109;
                    }

                    return false;
                }

                label102: {
                    Object this$updDTimes = this.getUpdDTimes();
                    Object other$updDTimes = other.getUpdDTimes();
                    if (this$updDTimes == null) {
                        if (other$updDTimes == null) {
                            break label102;
                        }
                    } else if (this$updDTimes.equals(other$updDTimes)) {
                        break label102;
                    }

                    return false;
                }

                Object this$isDeleted = this.getIsDeleted();
                Object other$isDeleted = other.getIsDeleted();
                if (this$isDeleted == null) {
                    if (other$isDeleted != null) {
                        return false;
                    }
                } else if (!this$isDeleted.equals(other$isDeleted)) {
                    return false;
                }

                Object this$delDtimes = this.getDelDtimes();
                Object other$delDtimes = other.getDelDtimes();
                if (this$delDtimes == null) {
                    if (other$delDtimes != null) {
                        return false;
                    }
                } else if (!this$delDtimes.equals(other$delDtimes)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof OtpTransactionEntity;
    }

    public int hashCode() {
        int result = 1;
        Object $id = this.getId();
        result = result * 59 + ($id == null ? 43 : $id.hashCode());
        Object $refId = this.getRefId();
        result = result * 59 + ($refId == null ? 43 : $refId.hashCode());
        Object $otpHash = this.getOtpHash();
        result = result * 59 + ($otpHash == null ? 43 : $otpHash.hashCode());
        Object $generatedDtimes = this.getGeneratedDtimes();
        result = result * 59 + ($generatedDtimes == null ? 43 : $generatedDtimes.hashCode());
        Object $expiryDtimes = this.getExpiryDtimes();
        result = result * 59 + ($expiryDtimes == null ? 43 : $expiryDtimes.hashCode());
        Object $validationRetryCount = this.getValidationRetryCount();
        result = result * 59 + ($validationRetryCount == null ? 43 : $validationRetryCount.hashCode());
        Object $statusCode = this.getStatusCode();
        result = result * 59 + ($statusCode == null ? 43 : $statusCode.hashCode());
        Object $langCode = this.getLangCode();
        result = result * 59 + ($langCode == null ? 43 : $langCode.hashCode());
        Object $crBy = this.getCrBy();
        result = result * 59 + ($crBy == null ? 43 : $crBy.hashCode());
        Object $crDtimes = this.getCrDtimes();
        result = result * 59 + ($crDtimes == null ? 43 : $crDtimes.hashCode());
        Object $updBy = this.getUpdBy();
        result = result * 59 + ($updBy == null ? 43 : $updBy.hashCode());
        Object $updDTimes = this.getUpdDTimes();
        result = result * 59 + ($updDTimes == null ? 43 : $updDTimes.hashCode());
        Object $isDeleted = this.getIsDeleted();
        result = result * 59 + ($isDeleted == null ? 43 : $isDeleted.hashCode());
        Object $delDtimes = this.getDelDtimes();
        result = result * 59 + ($delDtimes == null ? 43 : $delDtimes.hashCode());
        return result;
    }

    public String toString() {
        String var10000 = this.getId();
        return "OtpTransaction(id=" + var10000 + ", refId=" + this.getRefId() + ", otpHash=" + this.getOtpHash() + ", generatedDtimes=" + this.getGeneratedDtimes() + ", expiryDtimes=" + this.getExpiryDtimes() + ", validationRetryCount=" + this.getValidationRetryCount() + ", statusCode=" + this.getStatusCode() + ", langCode=" + this.getLangCode() + ", crBy=" + this.getCrBy() + ", crDtimes=" + this.getCrDtimes() + ", updBy=" + this.getUpdBy() + ", updDTimes=" + this.getUpdDTimes() + ", isDeleted=" + this.getIsDeleted() + ", delDtimes=" + this.getDelDtimes() + ")";
    }
}
