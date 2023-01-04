package io.mosip.resident.repository;

import io.mosip.resident.entity.OtpTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * The Interface OtpTxnRepository.
 * 
 * @author Kamesh Shekhar Prasad.
 * @since 1.2.0.1
 */
@Repository
public interface OtpTransactionRepository extends JpaRepository<OtpTransactionEntity, String> {
	Boolean existsByOtpHashAndStatusCode(String otpHash, String statusCode);

	OtpTransactionEntity findTopByOtpHashAndStatusCode(String otpHash, String statusCode);

	/**
	 * Obtain the number of count of request_dTtimes for particular userId with
	 * within the otpRequestDTime and oneMinuteBeforeTime.
	 *
	 * @param otpRequestDTime     the otp request D time
	 * @param oneMinuteBeforeTime the one minute before time
	 * @param refId               the ref id
	 * @return the int
	 */
	@Query("Select count(1) from OtpTransactionEntity  where generatedDtimes <= :otpRequestDTime and "
			+ "generatedDtimes >= :oneMinuteBeforeTime and refId=:refId")
	public int countRequestDTime(@Param("otpRequestDTime") LocalDateTime otpRequestDTime,
								 @Param("oneMinuteBeforeTime") LocalDateTime oneMinuteBeforeTime, @Param("refId") String refId);

	@Query("Select count(1) from OtpTransactionEntity  where refId = :refId and " + "statusCode = :statusCode and "
			+ "expiryDtimes > :currenttime and" +
			" crDtimes > :allowedTimeDuration")
	int checkotpsent(@Param("refId") String userid, @Param("statusCode") String statusCode,
					 @Param("currenttime") LocalDateTime currenttime, @Param("allowedTimeDuration") LocalDateTime allowedTimeDuration);

}
