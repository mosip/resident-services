package io.mosip.resident.repository;

import io.mosip.resident.entity.ResidentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResidentTransactionRepository extends JpaRepository<ResidentTransactionEntity, String> {
}