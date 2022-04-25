package io.mosip.resident.repository;

import io.mosip.resident.entity.ResidentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The Interface ResidentTransactionRepository.
 * @author Kamesh Shekhar Prasad.
 * @since 1.2.0.1
 */
public interface ResidentTransactionRepository extends JpaRepository<ResidentTransactionEntity, String> {
}