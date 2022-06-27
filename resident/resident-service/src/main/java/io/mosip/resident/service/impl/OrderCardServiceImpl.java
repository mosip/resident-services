package io.mosip.resident.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.ResidentCredentialRequestDto;
import io.mosip.resident.dto.ResidentCredentialResponseDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.OrderCardService;
import io.mosip.resident.service.ResidentCredentialService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.ResidentServiceRestClient;

/**
 * Order card service implementation class.
 * 
 * @author Ritik Jain
 */
@Component
public class OrderCardServiceImpl implements OrderCardService {

	@Autowired
	private ResidentCredentialService residentCredentialService;

	@Autowired
	@Qualifier("restClientWithSelfTOkenRestTemplate")
	private ResidentServiceRestClient restClientWithSelfTOkenRestTemplate;

	@Autowired
	private AuditUtil auditUtil;

	@Value("${mosip.resident.order.card.payment.enabled}")
	private boolean isPaymentEnabled;

	private static final Logger logger = LoggerConfiguration.logConfig(OrderCardServiceImpl.class);

	@Override
	public ResidentCredentialResponseDto sendPhysicalCard(ResidentCredentialRequestDto requestDto)
			throws ResidentServiceCheckedException {
		logger.debug("OrderCardServiceImpl::sendPhysicalCard()::entry");
		ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();

		if (isPaymentEnabled) {
			checkOrderStatus(requestDto.getTransactionID(), requestDto.getIndividualId());
		}
		residentCredentialResponseDto = residentCredentialService.reqCredentialV2(requestDto);
		logger.debug("OrderCardServiceImpl::sendPhysicalCard()::exit");
		return residentCredentialResponseDto;
	}

	private void checkOrderStatus(String transactionId, String individualId) throws ResidentServiceCheckedException {
		logger.debug("OrderCardServiceImpl::checkOrderStatus()::entry");
		List<String> pathsegments = null;

		List<String> queryParamName = new ArrayList<String>();
		queryParamName.add("transactionId");
		queryParamName.add("individualId");

		List<Object> queryParamValue = new ArrayList<>();
		queryParamValue.add(transactionId);
		queryParamValue.add(individualId);

		try {
			ResponseWrapper<?> responseWrapper = (ResponseWrapper<?>) restClientWithSelfTOkenRestTemplate.getApi(
					ApiName.GET_ORDER_STATUS_URL, pathsegments, queryParamName, queryParamValue, ResponseWrapper.class);

		} catch (ApisResourceAccessException e) {
			logger.error("Error occured in checking order status %s", e.getMessage());
			auditUtil.setAuditRequestDto(EventEnum.CHECK_ORDER_STATUS_EXCEPTION);
			throw new ResidentServiceCheckedException(ResidentErrorCode.PAYMENT_REQUIRED.getErrorCode(),
					ResidentErrorCode.PAYMENT_REQUIRED.getErrorMessage());
		}
		logger.debug("OrderCardServiceImpl::checkOrderStatus()::exit");
	}

}
