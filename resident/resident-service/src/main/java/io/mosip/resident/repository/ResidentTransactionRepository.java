package io.mosip.resident.repository;

import io.mosip.resident.entity.ResidentTransactionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The Interface ResidentTransactionRepository.
 * @author Kamesh Shekhar Prasad.
 * @since 1.2.0.1
 */
@Repository
public interface ResidentTransactionRepository extends JpaRepository<ResidentTransactionEntity, String> {
    public List<ResidentTransactionEntity> findByRequestTrnIdAndRefIdOrderByCrDtimesDesc(String requestTrnId, String refId);
    public ResidentTransactionEntity findByAid(String aid);


    @Query(value = "Select new ResidentTransactionEntity( requestTrnId, statusComment , crDtimes, statusCode) " +
            "from ResidentTransactionEntity where tokenId=:tokenId AND crDtimes>= :fromDateTime AND crDtimes<= :toDateTime  " +
            " AND authTypeCode in :residentTransactionType" )
    List<ResidentTransactionEntity> findByToken( @Param("tokenId") String tokenId,
                                                 @Param("fromDateTime") LocalDateTime fromDateTime,
                                                @Param("toDateTime") LocalDateTime toDateTime,
                                                @Param("residentTransactionType") List<String> residentTransactionType,
                                                Pageable pagaeable);

    @Query(value = "Select new ResidentTransactionEntity( requestTrnId, statusComment , crDtimes, statusCode) " +
            "from ResidentTransactionEntity where tokenId=:tokenId " +
            " AND authTypeCode in :residentTransactionType" )
    List<ResidentTransactionEntity> findByTokenWithoutDate(@Param("tokenId") String tokenId,
                                                           @Param("residentTransactionType") List<String> residentTransactionType,
                                                           Pageable pagaeable);

    @Query(value = "Select new ResidentTransactionEntity(aid) " +
            "from ResidentTransactionEntity where tokenId=:tokenId "  +
            " AND authTypeCode =:residentTransactionType ORDER BY crDtimes DESC" )
    List<ResidentTransactionEntity> findRequestIdByToken(@Param("tokenId") String tokenId,@Param("residentTransactionType") String residentTransactionType,
                                                         Pageable pagaeable);

}
