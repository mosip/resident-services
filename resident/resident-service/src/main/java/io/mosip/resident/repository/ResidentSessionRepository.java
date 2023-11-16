package io.mosip.resident.repository;

import static io.mosip.resident.constant.ResidentConstants.*;
import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_ID;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.core.annotation.Timed;
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
	
	@Timed(value=DB_QUERY_RESPONSE_TIME_ID,description=DB_QUERY_RESPONSE_TIME_DESCRIPTION, extraTags = {"repo" , "ResidentSessionRepository" , "queryName", "io.mosip.resident.repository.ResidentSessionRepository.findFirst2ByIdaTokenOrderByLoginDtimesDesc(String)"}, percentiles = {0.5, 0.9, 0.95, 0.99} )
	List<ResidentSessionEntity> findFirst2ByIdaTokenOrderByLoginDtimesDesc(String idaToken);

	Optional<ResidentSessionEntity> findFirstByIdaTokenOrderByLoginDtimesDesc(String idaToken);
}
