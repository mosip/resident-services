package io.mosip.resident.service.impl;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.ConsentStatusType;
import io.mosip.resident.constant.EventStatusFailure;
import io.mosip.resident.constant.EventStatusInProgress;
import io.mosip.resident.constant.EventStatusSuccess;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.constant.TemplateType;
import io.mosip.resident.constant.TemplateVariablesConstants;
import io.mosip.resident.dto.NotificationRequestDtoV2;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.dto.ResidentCredentialRequestDto;
import io.mosip.resident.dto.ResidentCredentialResponseDto;
import io.mosip.resident.dto.UrlRedirectRequestDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.OrderCardService;
import io.mosip.resident.service.ResidentCredentialService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utilitiy;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Order card service implementation class.
 * 
 * @author Ritik Jain
 */
@Component
public class OrderCardServiceImpl implements OrderCardService {

	private static final String PARTNER_TYPE = "partnerType";
	private static final String ORGANIZATION_NAME = "organizationName";

	@Autowired
	private ResidentCredentialService residentCredentialService;

	@Autowired
	private IdentityServiceImpl identityServiceImpl;

	@Autowired
	@Qualifier("restClientWithSelfTOkenRestTemplate")
	private ResidentServiceRestClient restClientWithSelfTOkenRestTemplate;

	@Autowired
	private AuditUtil auditUtil;

	@Autowired
	private Utilitiy utility;

	@Autowired
	NotificationService notificationService;
	
	@Autowired
    private ProxyPartnerManagementServiceImpl proxyPartnerManagementServiceImpl;

	@Autowired
	private ResidentTransactionRepository residentTransactionRepository;

	@Value("${mosip.resident.order.card.payment.enabled}")
	private boolean isPaymentEnabled;
	
	private static final Logger logger = LoggerConfiguration.logConfig(OrderCardServiceImpl.class);

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public ResidentCredentialResponseDto sendPhysicalCard(ResidentCredentialRequestDto requestDto)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		logger.debug("OrderCardServiceImpl::sendPhysicalCard()::entry");
		ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();
		String individualId = identityServiceImpl.getResidentIndvidualId();
		ResidentTransactionEntity residentTransactionEntity = createResidentTransactionEntity(requestDto, individualId);
		if (requestDto.getConsent() == null || requestDto.getConsent().equalsIgnoreCase(ConsentStatusType.DENIED.name())
				|| requestDto.getConsent().trim().isEmpty() || requestDto.getConsent().equals("null") || !requestDto.getConsent().equalsIgnoreCase(ConsentStatusType.ACCEPTED.name())) {
			checkConsent(requestDto.getConsent(), residentTransactionEntity);
		} else {

			if (isPaymentEnabled) {
				checkOrderStatus(requestDto.getTransactionID(), individualId, residentTransactionEntity);
			}
			residentCredentialResponseDto = residentCredentialService.reqCredential(requestDto, individualId);
			updateResidentTransaction(residentTransactionEntity, residentCredentialResponseDto);
			sendNotificationV2(individualId, RequestType.ORDER_PHYSICAL_CARD,
					TemplateType.REQUEST_RECEIVED, residentTransactionEntity.getEventId(), null);
			logger.debug("OrderCardServiceImpl::sendPhysicalCard()::exit");

		}

		return residentCredentialResponseDto;
	}

	private void checkConsent(String consent, ResidentTransactionEntity residentTransactionEntity)
			throws ResidentServiceCheckedException {
		try {
			residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
			throw new ResidentServiceCheckedException(ResidentErrorCode.CONSENT_DENIED.getErrorCode(),
					ResidentErrorCode.CONSENT_DENIED.getErrorMessage());
		} catch (Exception e) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.CONSENT_DENIED.getErrorCode(),
					ResidentErrorCode.CONSENT_DENIED.getErrorMessage());
		} finally {
			residentTransactionRepository.save(residentTransactionEntity);
		}

	}

	private ResidentTransactionEntity createResidentTransactionEntity(ResidentCredentialRequestDto requestDto, String individualId)
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResidentTransactionEntity residentTransactionEntity = utility.createEntity();
		residentTransactionEntity.setEventId(utility.createEventId());
		String attributeList = requestDto.getSharableAttributes().stream().collect(Collectors.joining(", "));
		residentTransactionEntity.setAttributeList(attributeList);
		residentTransactionEntity.setRequestTypeCode(RequestType.ORDER_PHYSICAL_CARD.name());
		residentTransactionEntity.setRefId(utility.convertToMaskDataFormat(individualId));
		residentTransactionEntity.setRequestedEntityId(requestDto.getIssuer());
		Map<String, ?> partnerDetail = proxyPartnerManagementServiceImpl.getPartnerDetailFromPartnerId(requestDto.getIssuer());
		residentTransactionEntity.setRequestedEntityName((String) partnerDetail.get(ORGANIZATION_NAME));
		residentTransactionEntity.setRequestedEntityType((String) partnerDetail.get(PARTNER_TYPE));
		residentTransactionEntity.setTokenId(identityServiceImpl.getResidentIdaToken());
		residentTransactionEntity.setAuthTypeCode(identityServiceImpl.getResidentAuthenticationMode());
		residentTransactionEntity.setRequestSummary(EventStatusSuccess.CARD_DELIVERED.name());
		residentTransactionEntity.setConsent(requestDto.getConsent());
		// TODO: need to fix transaction ID (need partner's end transactionId)
		residentTransactionEntity.setRequestTrnId(requestDto.getTransactionID());
		return residentTransactionEntity;
	}

	private void updateResidentTransaction(ResidentTransactionEntity residentTransEntity,
			ResidentCredentialResponseDto residentCredentialResponseDto) {
		residentTransEntity.setStatusCode(EventStatusSuccess.CARD_DELIVERED.name());
		residentTransEntity.setStatusComment(EventStatusSuccess.CARD_DELIVERED.name());
		residentTransEntity.setAid(residentCredentialResponseDto.getRequestId());
		residentTransactionRepository.save(residentTransEntity);
	}
	
	private void checkOrderStatus(String transactionId, String individualId,
			ResidentTransactionEntity residentTransactionEntity) throws ResidentServiceCheckedException {
		 checkOrderStatus(transactionId,individualId,null,residentTransactionEntity,null,null,null);
	}

	private String checkOrderStatus(String transactionId, String individualId, String redirectUrl,
			ResidentTransactionEntity residentTransactionEntity, String errorCode, String errorMessage,
			String address) throws ResidentServiceCheckedException {
		logger.debug("OrderCardServiceImpl::checkOrderStatus()::entry");
		String url;
		String newUrl = null;
		if(redirectUrl != null) {
		 url = new String(Base64.decodeBase64(redirectUrl.getBytes()));
		 newUrl = url.contains("?") ? url + "&" : url + "?";
		}
		StringBuilder builder = new StringBuilder();
		Map<String, String> queryParams = new HashMap<>();
		String orderRedirectURL = null;
		List<String> pathsegments = null;
		List<String> queryParamName = new ArrayList<String>();
		queryParamName.add("transactionId");
		queryParamName.add("individualId");

		List<Object> queryParamValue = new ArrayList<>();
		queryParamValue.add(transactionId);
		queryParamValue.add(individualId);

		try {
			if (errorCode != null && !errorCode.isEmpty()) {
				queryParams.put("error_code", errorCode);
				queryParams.put("error_message", errorMessage);
				for (Map.Entry<String, String> entry : queryParams.entrySet()) {
					String keyValueParam = entry.getKey() + "=" + entry.getValue();
					if (!builder.toString().isEmpty()) {
						builder.append("&");
					}
					builder.append(keyValueParam);
					orderRedirectURL = newUrl + builder.toString();
					residentTransactionEntity.setStatusCode(EventStatusFailure.PAYMENT_FAILED.name());
				}
			} else {
				ResponseWrapper<?> responseWrapper = (ResponseWrapper<?>) restClientWithSelfTOkenRestTemplate.getApi(
						ApiName.GET_ORDER_STATUS_URL, pathsegments, queryParamName, queryParamValue,
						ResponseWrapper.class);

				if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
					residentTransactionEntity.setStatusCode(responseWrapper.getErrors().get(0).getErrorCode() + "->"
							+ responseWrapper.getErrors().get(0).getMessage());
					queryParams.put("paymentTransactionId", transactionId);
					queryParams.put("error_code", responseWrapper.getErrors().get(0).getErrorCode());
					queryParams.put("error_message", responseWrapper.getErrors().get(0).getMessage());
					for (Map.Entry<String, String> entry : queryParams.entrySet()) {
						String keyValueParam = entry.getKey() + "=" + entry.getValue();
						if (!builder.toString().isEmpty()) {
							builder.append("&");
						}
						builder.append(keyValueParam);
						orderRedirectURL = newUrl + builder.toString();
						residentTransactionEntity.setStatusCode(EventStatusFailure.PAYMENT_FAILED.name());
					}
				} else {
					UrlRedirectRequestDTO responseDto = new UrlRedirectRequestDTO();
					responseDto = JsonUtil.readValue(JsonUtil.writeValueAsString(responseWrapper.getResponse()),
							UrlRedirectRequestDTO.class);
					queryParams.put("trackingId", responseDto.getTrackingId());
					queryParams.put("paymentTransactionId", responseDto.getTransactionId());
					queryParams.put("residentFullAddress", address);
					queryParams.put("eventId", residentTransactionEntity.getEventId());
					for (Map.Entry<String, String> entry : queryParams.entrySet()) {
						String keyValueParam = entry.getKey() + "=" + entry.getValue();
						if (!builder.toString().isEmpty()) {
							builder.append("&");
						}
						builder.append(keyValueParam);
						orderRedirectURL = newUrl + builder.toString();
					}
					residentTransactionEntity.setStatusCode(EventStatusInProgress.PAYMENT_CONFIRMED.name());
				}
			}
		} catch (ApisResourceAccessException e) {
			residentTransactionEntity.setStatusCode(EventStatusFailure.PAYMENT_FAILED.name());
			logger.error("Error occured in checking order status %s", e.getMessage());
			auditUtil.setAuditRequestDto(EventEnum.CHECK_ORDER_STATUS_EXCEPTION);
			sendNotificationV2(individualId, RequestType.ORDER_PHYSICAL_CARD, TemplateType.FAILURE,
					residentTransactionEntity.getEventId(), null);
			throw new ResidentServiceCheckedException(ResidentErrorCode.PAYMENT_REQUIRED.getErrorCode(),
					ResidentErrorCode.PAYMENT_REQUIRED.getErrorMessage());
		} catch (IOException e) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		} finally {
			residentTransactionRepository.save(residentTransactionEntity);
		}
		logger.debug("OrderCardServiceImpl::checkOrderStatus()::exit");
		return orderRedirectURL;

	}

	private NotificationResponseDTO sendNotificationV2(String id, RequestType requestType, TemplateType templateType,
			String eventId, Map<String, Object> additionalAttributes) throws ResidentServiceCheckedException {
		NotificationRequestDtoV2 notificationRequestDtoV2 = new NotificationRequestDtoV2();
		notificationRequestDtoV2.setId(id);
		notificationRequestDtoV2.setRequestType(requestType);
		notificationRequestDtoV2.setTemplateType(templateType);
		notificationRequestDtoV2.setEventId(eventId);
		notificationRequestDtoV2.setAdditionalAttributes(additionalAttributes);
		return notificationService.sendNotification(notificationRequestDtoV2);
	}

	@Override
	public String getRedirectUrl(String partnerId, String individualId)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		Map<String, ?> partnerDetail = proxyPartnerManagementServiceImpl.getPartnerDetailFromPartnerId(partnerId);
		 
		ResidentTransactionEntity residentTransactionEntity = createResidentTransactionEntityOrderCard(partnerId,
				individualId);
		if (partnerDetail.isEmpty()) {
			residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
			residentTransactionRepository.save(residentTransactionEntity);
			throw new ResidentServiceCheckedException(ResidentErrorCode.PATNER_NOT_FOUND.getErrorCode(),
					ResidentErrorCode.PATNER_NOT_FOUND.getErrorMessage());
		} else {
			List<Map<String, ?>> info = (List<Map<String, ?>>) partnerDetail.get("additionalInfo");
			String redirectUrl = info.stream().map(map -> (String) map.get("orderRedirectUrl")).findAny().orElse("");
			if (redirectUrl.isEmpty()) {
				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
				residentTransactionRepository.save(residentTransactionEntity);

				throw new ResidentServiceCheckedException(ResidentErrorCode.REDIRECT_URL_NOT_FOUND.getErrorCode(),
						ResidentErrorCode.REDIRECT_URL_NOT_FOUND.getErrorMessage());
			}
			residentTransactionEntity.setStatusCode(EventStatusSuccess.PHYSICAL_CARD_ORDERED.name());
			residentTransactionEntity.setStatusComment(EventStatusSuccess.PHYSICAL_CARD_ORDERED.name());
			residentTransactionRepository.save(residentTransactionEntity);
			String newUrl = redirectUrl.contains("?") ? redirectUrl + "&" : redirectUrl + "?";
			StringBuilder builder = new StringBuilder();
			Map<String, String> queryParams = new HashMap<>();
			queryParams.put(TemplateVariablesConstants.EVENT_ID, residentTransactionEntity.getEventId());
			for (Map.Entry<String, String> entry : queryParams.entrySet()) {
				String keyValueParam = entry.getKey() + "=" + entry.getValue();
				if (!builder.toString().isEmpty()) {
					builder.append("&");
				}
				builder.append(keyValueParam);
			}
			return newUrl + builder.toString();
		}
	}

	private ResidentTransactionEntity createResidentTransactionEntityOrderCard(String partnerId, String individualId) throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResidentTransactionEntity residentTransactionEntity = utility.createEntity();
		residentTransactionEntity.setEventId(utility.createEventId());
		residentTransactionEntity.setRequestTypeCode(RequestType.ORDER_PHYSICAL_CARD.name());
		residentTransactionEntity.setRefId(utility.convertToMaskDataFormat(individualId));
		residentTransactionEntity.setRequestedEntityId(partnerId);
		Map<String, ?> partnerDetail = proxyPartnerManagementServiceImpl.getPartnerDetailFromPartnerId(partnerId);
		residentTransactionEntity.setRequestedEntityName((String) partnerDetail.get(ORGANIZATION_NAME));
		residentTransactionEntity.setRequestedEntityType((String) partnerDetail.get(PARTNER_TYPE));
		residentTransactionEntity.setTokenId(identityServiceImpl.getResidentIdaToken());
		residentTransactionEntity.setAuthTypeCode(identityServiceImpl.getResidentAuthenticationMode());
		residentTransactionEntity.setRequestSummary(EventStatusSuccess.PHYSICAL_CARD_ORDERED.name());
		return residentTransactionEntity;
	}

	@Override
	public String physicalCardOrder(String redirectUrl, String paymentTransactionId, String eventId,
			String residentFullAddress, String individualId, String errorCode, String errorMessage)
			throws ResidentServiceCheckedException {
		ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();
		Optional<ResidentTransactionEntity> residentTransactionEntity = residentTransactionRepository.findById(eventId);
		String reponse = null;
		ResidentCredentialRequestDto requestDto = new ResidentCredentialRequestDto();
		if (residentTransactionEntity.isPresent()) {
		requestDto.setIssuer(residentTransactionEntity.get().getRequestedEntityId());
		if (isPaymentEnabled) {
			reponse = checkOrderStatus(paymentTransactionId, individualId, redirectUrl, residentTransactionEntity.get(),
					 errorCode, errorMessage, residentFullAddress);
		}
		residentCredentialResponseDto = residentCredentialService.reqCredential(requestDto, individualId);
		updateResidentTransaction(residentTransactionEntity.get(), residentCredentialResponseDto);
		}
		return reponse;
	}

}