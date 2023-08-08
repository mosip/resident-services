package io.mosip.resident.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.resident.entity.ResidentTransactionEntity;

/**
 * The Interface ResidentTransactionRepository.
 * 
 * @author Kamesh Shekhar Prasad.
 * @since 1.2.0.1
 */
@Repository
public interface ResidentTransactionRepository extends JpaRepository<ResidentTransactionEntity, String> {

	List<ResidentTransactionEntity> findByRequestTrnId(String requestTrnId);

	ResidentTransactionEntity findTopByRequestTrnIdAndTokenIdAndStatusCodeInOrderByCrDtimesDesc
	(String requestTrnId, String tokenId, List<String> statusCodes);

	ResidentTransactionEntity findTopByRefIdAndStatusCodeOrderByCrDtimesDesc(String refId, String statusCode);

	public List<ResidentTransactionEntity> findByStatusCodeInAndRequestTypeCodeInAndCredentialRequestIdIsNotNullOrderByCrDtimesAsc(List<String> statusCodes, List<String> requestTypes);


	@Query(value = "SELECT COUNT(*) from ResidentTransactionEntity where tokenId=:tokenId AND read_status='false' and requestTypeCode in (:requestTypes) AND (olvPartnerId IS NULL OR olvPartnerId = :olvPartnerId)")
	Long countByIdAndUnreadStatusForRequestTypes(@Param("tokenId") String tokenId, @Param("requestTypes") List<String> requestTypes, @Param("olvPartnerId") String olvPartnerId);
	
	@Query(value = "SELECT COUNT(*) from ResidentTransactionEntity where tokenId=:tokenId AND (crDtimes>= :notificationClickTime OR updDtimes>= :notificationClickTime) AND read_status='false' AND requestTypeCode in (:requestTypes) AND (olvPartnerId IS NULL OR olvPartnerId = :olvPartnerId)")
	Long countByIdAndUnreadStatusForRequestTypesAfterNotificationClick(@Param("tokenId") String tokenId,@Param("notificationClickTime") LocalDateTime notificationClickTime, @Param("requestTypes") List<String> requestTypes, @Param("olvPartnerId") String olvPartnerId);

	/**
	 * AuthTransaction entries only will be expected here. This wouldn't fetch the otp Requested performed in resident service.
	 */
	@Query(value = "SELECT COUNT(*) from ResidentTransactionEntity where ref_id=:hashRefId AND auth_type_code like %:authType")
	Integer findByRefIdAndAuthTypeCodeLike(@Param("hashRefId") String hashRefId, @Param("authType") String authType);
	
	@Modifying
    @Transactional
	@Query("update ResidentTransactionEntity set read_status='true' where event_id=:eventId")
	int updateReadStatus(@Param("eventId") String eventId);

	@Modifying
    @Transactional
	@Query("update ResidentTransactionEntity set pinned_status='true' where event_id=:eventId")
	int updatePinnedStatus(@Param("eventId") String eventId);

	Optional<ResidentTransactionEntity> findOneByCredentialRequestId(String requestId);

	@Query(value = "SELECT * FROM resident_transaction WHERE token_id = :tokenId " +
			" AND request_type_code IN (:requestTypeCodes) " +
			"AND (olv_partner_id is null OR \n" +
			"olv_partner_id=:olvPartnerId)" +
			" order by pinned_status desc, " +
			" cr_dtimes DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
	List<ResidentTransactionEntity> findByTokenId(@Param("tokenId") String tokenId , @Param("limit") int limit, @Param("offset") int offset,
												  @Param("olvPartnerId") String olvPartnerId,
												  @Param("requestTypeCodes") List<String> requestTypeCodes);

	@Query(value = "SELECT COUNT(*) FROM resident_transaction " +
			"WHERE token_id = :tokenId " +
			" AND request_type_code IN (:requestTypeCodes) " +
			"AND (olv_partner_id is null OR \n" +
			"olv_partner_id=:olvPartnerId)" , nativeQuery = true)
	int countByTokenId(@Param("tokenId") String tokenId,  @Param("olvPartnerId") String olvPartnerId,
					   @Param("requestTypeCodes") List<String> requestTypeCodes);

	@Query(value = "SELECT * FROM resident_transaction WHERE token_id = :tokenId " +
			" AND request_type_code IN (:requestTypeCodes) " +
			"AND (olv_partner_id is null OR \n" +
			"olv_partner_id=:olvPartnerId)" +
			"AND event_id LIKE CONCAT('%', :eventId, '%') " +
			" order by pinned_status desc, " +
			" cr_dtimes DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
	List<ResidentTransactionEntity> findByTokenIdAndSearchEventId(@Param("tokenId") String tokenId , @Param("limit") int limit, @Param("offset") int offset,
												  @Param("olvPartnerId") String olvPartnerId,
												  @Param("requestTypeCodes") List<String> requestTypeCodes, @Param("eventId") String eventId);

	@Query(value = "SELECT COUNT(*) FROM resident_transaction " +
			"WHERE token_id = :tokenId " +
			" AND request_type_code IN (:requestTypeCodes) " +
			"AND (olv_partner_id is null OR \n" +
			"olv_partner_id=:olvPartnerId) " +
			"AND event_id LIKE CONCAT('%', :eventId, '%') "
			, nativeQuery = true)
	int countByTokenIdAndSearchEventId(@Param("tokenId") String tokenId,  @Param("olvPartnerId") String olvPartnerId,
					   @Param("requestTypeCodes") List<String> requestTypeCodes , @Param("eventId") String eventId);

	@Query(value = "SELECT * FROM resident_transaction WHERE token_id = :tokenId " +
			" AND request_type_code IN (:requestTypeCodes) " +
			"AND (olv_partner_id is null OR \n" +
			"olv_partner_id=:olvPartnerId)" +
			"AND cr_dtimes BETWEEN :startDate AND :endDate " +
			" order by pinned_status desc, " +
			" cr_dtimes DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
	List<ResidentTransactionEntity> findByTokenIdBetweenCrDtimes(@Param("tokenId") String tokenId , @Param("limit") int limit, @Param("offset") int offset,
												  @Param("olvPartnerId") String olvPartnerId,
												  @Param("requestTypeCodes") List<String> requestTypeCodes, @Param("startDate") LocalDateTime startDate,
																 @Param("endDate") LocalDateTime endDate);

	@Query(value = "SELECT COUNT(*) FROM resident_transaction " +
			"WHERE token_id = :tokenId " +
			" AND request_type_code IN (:requestTypeCodes) " +
			"AND (olv_partner_id is null OR \n" +
			"olv_partner_id=:olvPartnerId)" +
			"AND cr_dtimes BETWEEN :startDate AND :endDate ", nativeQuery = true)
	int countByTokenIdBetweenCrDtimes(@Param("tokenId") String tokenId,  @Param("olvPartnerId") String olvPartnerId,
					   @Param("requestTypeCodes") List<String> requestTypeCodes, @Param("startDate") LocalDateTime startDate,
									  @Param("endDate") LocalDateTime endDate);

	@Query(value = "SELECT * FROM resident_transaction WHERE token_id = :tokenId " +
			" AND request_type_code IN (:requestTypeCodes) " +
			" AND status_code IN (:statusCode) " +
			"AND (olv_partner_id is null OR \n" +
			"olv_partner_id=:olvPartnerId)" +
			" order by pinned_status desc, " +
			" cr_dtimes DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
	List<ResidentTransactionEntity> findByTokenIdInStatus(@Param("tokenId") String tokenId , @Param("limit") int limit, @Param("offset") int offset,
												  @Param("olvPartnerId") String olvPartnerId,
												  @Param("requestTypeCodes") List<String> requestTypeCodes,
														  @Param("statusCode") List<String> statusCode);

	@Query(value = "SELECT COUNT(*) FROM resident_transaction " +
			"WHERE token_id = :tokenId " +
			" AND request_type_code IN (:requestTypeCodes) " +
			" AND status_code IN (:statusCode) " +
			"AND (olv_partner_id is null OR \n" +
			"olv_partner_id=:olvPartnerId)" , nativeQuery = true)
	int countByTokenIdInStatus(@Param("tokenId") String tokenId,  @Param("olvPartnerId") String olvPartnerId,
					   @Param("requestTypeCodes") List<String> requestTypeCodes,
							   @Param("statusCode") List<String> statusCode);

	@Query(value = "SELECT * FROM resident_transaction WHERE token_id = :tokenId " +
			" AND request_type_code IN (:requestTypeCodes) " +
			" AND status_code IN (:statusCode) " +
			"AND (olv_partner_id is null OR \n" +
			"olv_partner_id=:olvPartnerId)" +
			"AND cr_dtimes BETWEEN :startDate AND :endDate " +
			" order by pinned_status desc, " +
			" cr_dtimes DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
	List<ResidentTransactionEntity> findByTokenIdInStatusBetweenCrDtimes(@Param("tokenId") String tokenId , @Param("limit") int limit, @Param("offset") int offset,
														  @Param("olvPartnerId") String olvPartnerId,
														  @Param("requestTypeCodes") List<String> requestTypeCodes,
														  @Param("statusCode") List<String> statusCode,
																		 @Param("startDate") LocalDateTime startDate,
																		 @Param("endDate") LocalDateTime endDate);

	@Query(value = "SELECT COUNT(*) FROM resident_transaction " +
			"WHERE token_id = :tokenId " +
			" AND request_type_code IN (:requestTypeCodes) " +
			" AND status_code IN (:statusCode) " +
			"AND (olv_partner_id is null OR \n" +
			"olv_partner_id=:olvPartnerId)"+
			"AND cr_dtimes BETWEEN :startDate AND :endDate ", nativeQuery = true)
	int countByTokenIdInStatusBetweenCrDtimes(@Param("tokenId") String tokenId,  @Param("olvPartnerId") String olvPartnerId,
							   @Param("requestTypeCodes") List<String> requestTypeCodes,
							   @Param("statusCode") List<String> statusCode, @Param("startDate") LocalDateTime startDate,
											  @Param("endDate") LocalDateTime endDate);

	@Query(value = "SELECT * FROM resident_transaction WHERE token_id = :tokenId " +
			" AND request_type_code IN (:requestTypeCodes) " +
			"AND (olv_partner_id is null OR \n" +
			"olv_partner_id=:olvPartnerId)" +
			"AND cr_dtimes BETWEEN :startDate AND :endDate " +
			"AND event_id LIKE CONCAT('%', :eventId, '%') " +
			" order by pinned_status desc, " +
			" cr_dtimes DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
	List<ResidentTransactionEntity> findByTokenIdBetweenCrDtimesSearchEventId(@Param("tokenId") String tokenId , @Param("limit") int limit, @Param("offset") int offset,
																 @Param("olvPartnerId") String olvPartnerId,
																 @Param("requestTypeCodes") List<String> requestTypeCodes,
																			  @Param("startDate") LocalDateTime startDate,
																 @Param("endDate") LocalDateTime endDate, @Param("eventId") String eventId);

	@Query(value = "SELECT COUNT(*) FROM resident_transaction " +
			"WHERE token_id = :tokenId " +
			" AND request_type_code IN (:requestTypeCodes) " +
			"AND (olv_partner_id is null OR \n" +
			"olv_partner_id=:olvPartnerId)" +
			"AND cr_dtimes BETWEEN :startDate AND :endDate "+
			"AND event_id LIKE CONCAT('%', :eventId, '%') ", nativeQuery = true)
	int countByTokenIdBetweenCrDtimesSearchEventId(@Param("tokenId") String tokenId,  @Param("olvPartnerId") String olvPartnerId,
									  @Param("requestTypeCodes") List<String> requestTypeCodes, @Param("startDate") LocalDateTime startDate,
									  @Param("endDate") LocalDateTime endDate, @Param("eventId") String eventId);

	@Query(value = "SELECT * FROM resident_transaction WHERE token_id = :tokenId " +
			" AND request_type_code IN (:requestTypeCodes) " +
			" AND status_code IN (:statusCode) " +
			"AND (olv_partner_id is null OR \n" +
			"olv_partner_id=:olvPartnerId)" +
			"AND event_id LIKE CONCAT('%', :eventId, '%') " +
			" order by pinned_status desc, " +
			" cr_dtimes DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
	List<ResidentTransactionEntity> findByTokenIdInStatusSearchEventId(@Param("tokenId") String tokenId , @Param("limit") int limit, @Param("offset") int offset,
														  @Param("olvPartnerId") String olvPartnerId,
														  @Param("requestTypeCodes") List<String> requestTypeCodes,
														  @Param("statusCode") List<String> statusCode, @Param("eventId") String eventId);

	@Query(value = "SELECT COUNT(*) FROM resident_transaction " +
			"WHERE token_id = :tokenId " +
			" AND request_type_code IN (:requestTypeCodes) " +
			" AND status_code IN (:statusCode) " +
			"AND (olv_partner_id is null OR \n" +
			"olv_partner_id=:olvPartnerId)"+
			"AND event_id LIKE CONCAT('%', :eventId, '%') " , nativeQuery = true)
	int countByTokenIdInStatusSearchEventId(@Param("tokenId") String tokenId,  @Param("olvPartnerId") String olvPartnerId,
							   @Param("requestTypeCodes") List<String> requestTypeCodes,
							   @Param("statusCode") List<String> statusCode, @Param("eventId") String eventId);

}
