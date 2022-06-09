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

//    @Query(value = "Select new ResidentTransactionEntity( requestTrnId, requestDtimes, authTypeCode, statusCode, statusComment, refId, refIdType, entityName, requestSignature, responseSignature ) " +
//            "from ResidentTransactionEntity where tokenId=:tokenId AND crDTimes>= :fromDateTime AND crDTimes<= :toDateTime  ORDER BY crDTimes DESC")
    @Query(value = "select * from resident_transaction where token_id = ?1 ", nativeQuery = true)
    List<ResidentTransactionEntity> findByToken( String tokenId,
                                                 LocalDateTime fromDateTime,
                                                 LocalDateTime toDateTime, Pageable pagaeable);
}