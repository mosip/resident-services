package io.mosip.resident.repository;

import io.mosip.resident.entity.ResidentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Interface ResidentTransactionRepository.
 * @author Kamesh Shekhar Prasad.
 * @since 1.2.0.1
 */
@Repository
public interface ResidentTransactionRepository extends JpaRepository<ResidentTransactionEntity, String> {
    public List<ResidentTransactionEntity> findByRequestTrnIdAndRefIdOrderByCrDtimes(String requestTrnId, String refId);
    public ResidentTransactionEntity findByAid(String aid);
}