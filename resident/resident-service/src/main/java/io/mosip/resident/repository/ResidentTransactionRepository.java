package io.mosip.resident.repository;

import java.time.LocalDateTime;
import java.util.List;

import io.mosip.resident.dto.ServiceHistoryResponseDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.resident.entity.ResidentTransactionEntity;

/**
 * The Interface ResidentTransactionRepository.
 * @author Kamesh Shekhar Prasad.
 * @since 1.2.0.1
 */
@Repository
public interface ResidentTransactionRepository extends JpaRepository<ResidentTransactionEntity, String> {
    public List<ResidentTransactionEntity> findByRequestTrnIdAndRefIdOrderByCrDtimesDesc(String requestTrnId, String refId);
    public ResidentTransactionEntity findByAid(String aid);

     @Query(value = "select * from resident_transaction where token_id = ?1 ", nativeQuery = true)
    List<ResidentTransactionEntity> findByToken( String tokenId,
                                                 LocalDateTime fromDateTime,
                                                 LocalDateTime toDateTime, Pageable pagaeable);

     @Query(value = "SELECT request_trn_id, status_comment, cr_dtimes, status_code FROM resident.resident_transaction where token_id=?1 " +
             "AND cr_dtimes>= ?2 AND cr_dtimes<= ?3" +
             "    AND auth_type_code= ?4 OR auth_type_code = ?5" +
             "    ORDER BY cr_dtimes ?6 LIMIT ?7" +
             "OFFSET ?8", nativeQuery = true)
    List<ResidentTransactionEntity> findByTokenId(String tokenId, LocalDateTime fromDateTime, LocalDateTime toDateTime, String authTypeCode
     , String authTypeCodeSecond, String sortType , String pageSize, String pageStart);
}