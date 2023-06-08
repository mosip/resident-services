package io.mosip.resident.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
	List<ResidentTransactionEntity> findByRequestTrnIdAndRefIdOrderByCrDtimesDesc(String requestTrnId, String refId);

	List<ResidentTransactionEntity> findByRequestTrnId(String requestTrnId);

	ResidentTransactionEntity findTopByRequestTrnIdAndTokenIdAndStatusCodeInOrderByCrDtimesDesc
	(String requestTrnId, String tokenId, List<String> statusCodes);
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

	public List<ResidentTransactionEntity> findByStatusCodeInAndRequestTypeCodeInAndCredentialRequestIdIsNotNullOrderByCrDtimesAsc(List<String> statusCodes, List<String> requestTypes);

	Long countByTokenId(String tokenId);

	@Query(value = "SELECT COUNT(*) from ResidentTransactionEntity where tokenId=:tokenId AND read_status='false' and requestTypeCode in (:requestTypes) AND (olvPartnerId IS NULL OR olvPartnerId = :olvPartnerId)")
	Long countByIdAndUnreadStatusForRequestTypes(@Param("tokenId") String tokenId, @Param("requestTypes") List<String> requestTypes, @Param("olvPartnerId") String olvPartnerId);
	
	@Query(value = "SELECT COUNT(*) from ResidentTransactionEntity where tokenId=:tokenId AND (crDtimes>= :notificationClickTime OR updDtimes>= :notificationClickTime) AND read_status='false' AND requestTypeCode in (:requestTypes) AND (olvPartnerId IS NULL OR olvPartnerId = :olvPartnerId)")
	Long countByIdAndUnreadStatusForRequestTypesAfterNotificationClick(@Param("tokenId") String tokenId,@Param("notificationClickTime") LocalDateTime notificationClickTime, @Param("requestTypes") List<String> requestTypes, @Param("olvPartnerId") String olvPartnerId);

	@Query(value = "Select new ResidentTransactionEntity(eventId, requestSummary, statusCode,requestDtimes,requestTypeCode) "
			+ "from ResidentTransactionEntity where tokenId=:tokenId AND read_status='false' and requestTypeCode in (:requestTypes)")
	List<ResidentTransactionEntity> findByIdAndUnreadStatusForRequestTypes(@Param("tokenId") String tokenId, @Param("requestTypes") List<String> requestTypes);

	/**
	 * AuthTransaction entries only will be expected here. This wouldn't fetch the otp Requested performed in resident service.
	 */
	@Query(value = "SELECT COUNT(*) from ResidentTransactionEntity where ref_id=:hashRefId AND auth_type_code like %:authType")
	Integer findByRefIdAndAuthTypeCodeLike(@Param("hashRefId") String hashRefId, @Param("authType") String authType);
	
	@Modifying
    @Transactional
	@Query("update ResidentTransactionEntity set read_status='true' where event_id=:eventId")
	int updateReadStatus(@Param("eventId") String eventId);

	Page<ResidentTransactionEntity> findByTokenIdAndOlvPartnerIdIsNullOrOlvPartnerId(String tokenId, String olvPartnerId,Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndCrDtimesBetweenAndRequestTypeCodeInAndStatusCodeInAndEventIdLikeAndOlvPartnerIdIsNullOrOlvPartnerId(
			String tokenId,
			LocalDateTime startDate,
			LocalDateTime endDate,
			List<String> requestTypeCodes,
			List<String> statusCodes,
			String eventId,
			String onlineVerificationPartnerId, Pageable pageable
	);

	Page<ResidentTransactionEntity> findByEventIdLikeAndOlvPartnerIdIsNullOrOlvPartnerId(String eventId, String onlineVerificationPartnerId, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndCrDtimesBetweenAndRequestTypeCodeInAndStatusCodeInAndOlvPartnerIdIsNullOrOlvPartnerId(
			String tokenId, LocalDateTime startDate, LocalDateTime endDate, List<String> requestTypeCodes,
			List<String> statusCodes, String onlineVerificationPartnerId, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndCrDtimesBetweenAndRequestTypeCodeInAndEventIdLikeAndOlvPartnerIdIsNullOrOlvPartnerId(
			String tokenId, LocalDateTime startDate,
			LocalDateTime endDate,
			List<String> requestTypeCodes, String eventId, String onlineVerificationPartnerId, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndCrDtimesBetweenAndStatusCodeInAndEventIdLikeAndOlvPartnerIdIsNullOrOlvPartnerId(
			String tokenId, LocalDateTime startDate, LocalDateTime endDate, List<String> statusCodes, String eventId, String onlineVerificationPartnerId, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndRequestTypeCodeInAndStatusCodeInAndEventIdLikeAndOlvPartnerIdIsNullOrOlvPartnerId(
			String tokenId, List<String> requestTypeCodes, List<String> statusCodes, String eventId, String onlineVerificationPartnerId, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndRequestTypeCodeInAndStatusCodeInAndOlvPartnerIdIsNullOrOlvPartnerId(
			String tokenId, List<String> requestTypeCodes, List<String> statusCodes, String onlineVerificationPartnerId, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndRequestTypeCodeInAndEventIdLikeAndOlvPartnerIdIsNullOrOlvPartnerId(
			String tokenId, List<String> requestTypeCodes, String eventId, String onlineVerificationPartnerId, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndStatusCodeInAndEventIdLikeAndOlvPartnerIdIsNullOrOlvPartnerId(
			String tokenId, List<String> statusCodes, String eventId, String onlineVerificationPartnerId, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndCrDtimesBetweenAndEventIdLikeAndOlvPartnerIdIsNullOrOlvPartnerId(
			String tokenId, LocalDateTime startDate, LocalDateTime endDate, String eventId, String onlineVerificationPartnerId, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndCrDtimesBetweenAndStatusCodeInAndOlvPartnerIdIsNullOrOlvPartnerId(
			String tokenId, LocalDateTime startDate, LocalDateTime endDate, List<String> statusCodes, String onlineVerificationPartnerId, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndCrDtimesBetweenAndRequestTypeCodeInAndOlvPartnerIdIsNullOrOlvPartnerId(
			String tokenId, LocalDateTime startDate, LocalDateTime endDate, List<String> requestTypeCodes, String onlineVerificationPartnerId, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndCrDtimesBetweenAndOlvPartnerIdIsNullOrOlvPartnerId(
			String tokenId, LocalDateTime startDate, LocalDateTime endDate, String onlineVerificationPartnerId, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndRequestTypeCodeInAndOlvPartnerIdIsNullOrOlvPartnerId(
			String tokenId, List<String> requestTypeCodes, String onlineVerificationPartnerId, Pageable pageable);

	Page<ResidentTransactionEntity> findByTokenIdAndStatusCodeInAndOlvPartnerIdIsNullOrOlvPartnerId(String tokenId, List<String> statusCodes, String onlineVerificationPartnerId, Pageable pageable);

	Optional<ResidentTransactionEntity> findOneByCredentialRequestId(String requestId);
}
