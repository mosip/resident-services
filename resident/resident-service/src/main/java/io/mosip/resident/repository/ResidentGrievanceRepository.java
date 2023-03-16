package io.mosip.resident.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.resident.entity.ResidentGrievanceEntity;

/**
 * The Interface ResidentGrievanceRepository.
 * 
 * @author Kamesh Shekhar Prasad.
 * @since 1.2.0.1
 */
@Repository
public interface ResidentGrievanceRepository extends JpaRepository<ResidentGrievanceEntity, String> {
}
