package io.mosip.resident.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.resident.entity.ResidentUserEntity;

/**
 * The Interface ResidentUserRepository.
 * 
 * @author Neha Farheen
 * @since 1.2.0.1
 */
@Transactional
@Repository
public interface ResidentUserRepository extends JpaRepository<ResidentUserEntity, String> {
	Optional<ResidentUserEntity> findById(String Id);

	@Modifying
	@Query("update ResidentUserEntity res set res.lastbellnotifDtimes =:datetime where res.idaToken =:tokenId")
	int updateByIdandTime(@Param("tokenId") String tokenId, @Param("datetime") LocalDateTime datetime);
	
	@Modifying
	@Query("update ResidentUserEntity res set res.lastloginDtime =:datetime, res.ipAddress =:ipAddress, res.host =:host, res.machineType =:machineType where res.idaToken =:tokenId")
	void updateUserData(@Param("tokenId") String tokenId, @Param("datetime") LocalDateTime datetime,
			@Param("ipAddress") String ipAddress, @Param("host") String host, @Param("machineType") String machineType);

	@Modifying
    @Transactional
	@Query(value = "INSERT INTO resident.resident_user_actions(\r\n"
			+ "	ida_token, last_bell_notif_click_dtimes)\r\n"
			+ "	VALUES (:tokenId, :datetime);" , nativeQuery=true)
	int saveandupdateByIdandTime(@Param("tokenId") String tokenId, @Param("datetime") LocalDateTime datetime);
}
