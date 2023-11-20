package io.mosip.resident.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.resident.entity.ResidentSessionEntity;

/**
 * The Interface ResidentUserRepository.
 * 
 * @author Neha Farheen
 * @since 1.2.0.1
 */
@Transactional
@Repository
public interface ResidentSessionRepository extends JpaRepository<ResidentSessionEntity, String> {
	Optional<ResidentSessionEntity> findById(String id);
	
	List<ResidentSessionEntity> findFirst2ByIdaTokenOrderByLoginDtimesDesc(String idaToken);

	Optional<ResidentSessionEntity> findFirstByIdaTokenOrderByLoginDtimesDesc(String idaToken);
}
