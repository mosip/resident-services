package io.mosip.resident.repository;

import io.mosip.resident.entity.ResidentTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The Interface ResidentTransactionRepository.
 * 
 * @author Kamesh Shekhar Prasad.
 * @since 1.2.0.1
 */
@Repository
public interface ResidentTransactionRepository extends JpaRepository<ResidentTransactionEntity, String> {
	List<ResidentTransactionEntity> findByRequestTrnIdAndRefIdOrderByCrDtimesDesc(String requestTrnId, String refId);

	List<ResidentTransactionEntity> findByCredentialRequestId(String credentialRequestId);

	ResidentTransactionEntity findTopByRequestTrnIdAndTokenIdAndStatusCodeOrderByCrDtimesDesc
	(String requestTrnId, String tokenId, String statusCode);
	ResidentTransactionEntity findTopByAidOrderByCrDtimesDesc(String aid);

	ResidentTransactionEntity findTopByRefIdAndStatusCodeOrderByCrDtimesDesc(String refId, String statusCode);

	List<ResidentTransactionEntity> findByTokenId(String token);

	@Query(value = "Select new ResidentTransactionEntity( eventId, statusComment , crDtimes, statusCode, updDtimes, requestTypeCode) "
			+ "from ResidentTransactionEntity where tokenId=:tokenId AND crDtimes>= :fromDateTime AND crDtimes<= :toDateTime  "
			+ " AND authTypeCode in :residentTransactionType " + " AND (eventId like %:searchText%"
			+ " OR statusComment like %:searchText% " + " OR statusCode like %:searchText%) "
			+ "ORDER BY pinnedStatus DESC")
	List<ResidentTransactionEntity> findByTokenAndTransactionType(@Param("tokenId") String tokenId,
			@Param("fromDateTime") LocalDateTime fromDateTime, @Param("toDateTime") LocalDateTime toDateTime,
			@Param("residentTransactionType") List<String> residentTransactionType, Pageable pagaeable,
			@Param("searchText") String searchText);

	@Query(value = "Select new ResidentTransactionEntity( eventId, statusComment , crDtimes, statusCode, updDtimes, requestTypeCode) "
			+ "from ResidentTransactionEntity where tokenId=:tokenId "
			+ " AND authTypeCode in :residentTransactionType " + " AND (eventId like %:searchText%"
			+ " OR statusComment like %:searchText% " + " OR statusCode like %:searchText%) "
			+ " ORDER BY pinnedStatus DESC LIMIT :pageFetch OFFSET :pageStart", nativeQuery = true)
	List<ResidentTransactionEntity> findByTokenWithoutDate(@Param("tokenId") String tokenId,
			@Param("residentTransactionType") List<String> residentTransactionType,
			@Param("pageStart") String pageStart, @Param("pageFetch") String pageFetch,
			@Param("searchText") String searchText);

	@Query(value = "Select new ResidentTransactionEntity(aid) "
			+ "from ResidentTransactionEntity where tokenId=:tokenId "
			+ " AND authTypeCode =:residentTransactionType ORDER BY crDtimes DESC")
	List<ResidentTransactionEntity> findRequestIdByToken(@Param("tokenId") String tokenId,
			@Param("residentTransactionType") String residentTransactionType, Pageable pagaeable);

	@Query(value = "Select new ResidentTransactionEntity( eventId, statusComment , crDtimes, statusCode, updDtimes, requestTypeCode) "
			+ "from ResidentTransactionEntity where tokenId=:tokenId AND crDtimes>= :fromDateTime AND crDtimes<= :toDateTime  "
			+ " AND (eventId like %:searchText%" + " OR statusComment like %:searchText% "
			+ " OR statusCode like %:searchText%) " + "ORDER BY pinnedStatus DESC")
	List<ResidentTransactionEntity> findByTokenWithoutServiceType(@Param("tokenId") String tokenId,
			@Param("fromDateTime") LocalDateTime fromDateTime, @Param("toDateTime") LocalDateTime toDateTime,
			Pageable pagaeable, @Param("searchText") String searchText);

	@Query(value = "Select new ResidentTransactionEntity( eventId, statusComment , crDtimes, statusCode, updDtimes, requestTypeCode) "
			+ "from ResidentTransactionEntity where tokenId=:tokenId " + " AND (eventId like %:searchText%"
			+ " OR statusComment like %:searchText% " + " OR statusCode like %:searchText%) "
			+ " ORDER BY pinnedStatus DESC LIMIT :pageFetch OFFSET :pageStart", nativeQuery = true)
	List<ResidentTransactionEntity> findByTokenWithoutServiceTypeAndDate(@Param("tokenId") String tokenId,
			@Param("pageStart") int pageStart, @Param("pageFetch") int pageFetch,
			@Param("searchText") String searchText);

	public List<ResidentTransactionEntity> findByStatusCodeInAndRequestTypeCodeInOrderByCrDtimesAsc(List<String> statusCodes, List<String> requestTypes);

	Long countByTokenId(String tokenId);

	@Query(value = "SELECT COUNT(*) from ResidentTransactionEntity where tokenId=:tokenId AND read_status='false' and requestTypeCode in (:requestTypes)")
	Long countByIdAndUnreadStatusForRequestTypes(@Param("tokenId") String tokenId, @Param("requestTypes") List<String> requestTypes);
	
	@Query(value = "SELECT COUNT(*) from ResidentTransactionEntity where tokenId=:tokenId AND crDtimes>= :notificationClickTime  AND read_status='false' AND requestTypeCode in (:requestTypes)")
	Long countByIdAndUnreadStatusForRequestTypesAfterNotificationClick(@Param("tokenId") String tokenId,@Param("notificationClickTime") LocalDateTime notificationClickTime, @Param("requestTypes") List<String> requestTypes);

	@Query(value = "Select new ResidentTransactionEntity(eventId, requestSummary, statusCode,requestDtimes,requestTypeCode) "
			+ "from ResidentTransactionEntity where tokenId=:tokenId AND read_status='false' and requestTypeCode in (:requestTypes)")
	List<ResidentTransactionEntity> findByIdAndUnreadStatusForRequestTypes(@Param("tokenId") String tokenId, @Param("requestTypes") List<String> requestTypes);

	@Query(value = "SELECT COUNT(*) from ResidentTransactionEntity where ref_id=:hashrefid AND auth_type_code !='OTP_REQUESTED'")
	/**
	 * AuthTransaction entries only will be expected here. This wouldn't fetch the otp Requested performed in resident service.
	 */
	Integer findByrefIdandauthtype(@Param("hashrefid") String hashrefid);
	
	@Modifying
    @Transactional
	@Query("update ResidentTransactionEntity set read_status='true' where event_id=:eventId")
	int updateReadStatus(@Param("eventId") String eventId);

	Page<ResidentTransactionEntity> findByTokenId(String tokenId, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndCrDtimesBetweenAndRequestTypeCodeInAndStatusCodeInAndEventIdLike(
			String tokenId,
			LocalDateTime startDate,
			LocalDateTime endDate,
			List<String> requestTypeCodes,
			List<String> statusCodes,
			String eventId,
			Pageable pageable
	);

	Page<ResidentTransactionEntity> findByEventIdLike(String eventId, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndCrDtimesBetweenAndRequestTypeCodeInAndStatusCodeIn(
			String tokenId, LocalDateTime startDate, LocalDateTime endDate, List<String> requestTypeCodes,
			List<String> statusCodes, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndCrDtimesBetweenAndRequestTypeCodeInAndEventIdLike(
			String tokenId, LocalDateTime startDate,
			LocalDateTime endDate,
			List<String> requestTypeCodes, String eventId, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndCrDtimesBetweenAndStatusCodeInAndEventIdLike(
			String tokenId, LocalDateTime startDate, LocalDateTime endDate, List<String> statusCodes, String eventId, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndRequestTypeCodeInAndStatusCodeInAndEventIdLike(
			String tokenId, List<String> requestTypeCodes, List<String> statusCodes, String eventId, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndRequestTypeCodeInAndStatusCodeIn(
			String tokenId, List<String> requestTypeCodes, List<String> statusCodes, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndRequestTypeCodeInAndEventIdLike(
			String tokenId, List<String> requestTypeCodes, String eventId, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndStatusCodeInAndEventIdLike(
			String tokenId, List<String> statusCodes, String eventId, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndCrDtimesBetweenAndEventIdLike(
			String tokenId, LocalDateTime startDate, LocalDateTime endDate, String eventId, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndCrDtimesBetweenAndStatusCodeIn(
			String tokenId, LocalDateTime startDate, LocalDateTime endDate, List<String> statusCodes, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndCrDtimesBetweenAndRequestTypeCodeIn(
			String tokenId, LocalDateTime startDate, LocalDateTime endDate, List<String> requestTypeCodes, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndCrDtimesBetween(
			String tokenId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndRequestTypeCodeIn(
			String tokenId, List<String> requestTypeCodes, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndStatusCodeIn(String tokenId, List<String> statusCodes, Pageable pageable);
}
