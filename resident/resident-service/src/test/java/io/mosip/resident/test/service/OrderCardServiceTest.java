package io.mosip.resident.test.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.dto.ResidentCredentialRequestDto;
import io.mosip.resident.dto.ResidentCredentialResponseDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.OrderCardService;
import io.mosip.resident.service.ResidentCredentialService;
import io.mosip.resident.service.impl.OrderCardServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;

/**
 * Resident order card service test class.
 * 
 * @author Ritik Jain
 */
@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class OrderCardServiceTest {

	@InjectMocks
	private OrderCardService orderCardService = new OrderCardServiceImpl();

	@Mock
	private AuditUtil auditUtil;

	@Mock
	private ResidentServiceRestClient restClientWithSelfTOkenRestTemplate;

	@Mock
	private ResidentCredentialService residentCredentialService;

	private ResponseWrapper<?> responseWrapper;

	private ResidentCredentialResponseDto residentCredentialResponseDto;

	private ResidentCredentialRequestDto residentCredentialRequestDto;

	@Before
	public void setUp() throws Exception {
		ReflectionTestUtils.setField(orderCardService, "isPaymentEnabled", true);
		responseWrapper = new ResponseWrapper<>();
		responseWrapper.setVersion("v1");
		responseWrapper.setId("1");

		residentCredentialRequestDto = new ResidentCredentialRequestDto();
		residentCredentialRequestDto.setTransactionID("1234327890");
		residentCredentialRequestDto.setIndividualId("8251649601");

		residentCredentialResponseDto = new ResidentCredentialResponseDto();
		residentCredentialResponseDto.setId("8251649601");
		residentCredentialResponseDto.setRequestId("effc56cd-cf3b-4042-ad48-7277cf90f763");
	}

	@Test
	public void testSendPhysicalCard() throws Exception {
		ReflectionTestUtils.setField(orderCardService, "isPaymentEnabled", false);
		when(residentCredentialService.reqCredentialV2(any())).thenReturn(residentCredentialResponseDto);

		ResidentCredentialResponseDto result = orderCardService.sendPhysicalCard(residentCredentialRequestDto);
		assertEquals("effc56cd-cf3b-4042-ad48-7277cf90f763", result.getRequestId());
	}

	@Test
	public void testSendPhysicalCardIf() throws Exception {
		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), (List<String>) any(), (List<String>) any(),
				any(), any())).thenReturn(responseWrapper);
		when(residentCredentialService.reqCredentialV2(any())).thenReturn(residentCredentialResponseDto);

		ResidentCredentialResponseDto result = orderCardService.sendPhysicalCard(residentCredentialRequestDto);
		assertEquals("effc56cd-cf3b-4042-ad48-7277cf90f763", result.getRequestId());
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testCheckOrderStatusWithApisResourceAccessException() throws Exception {
		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), (List<String>) any(), (List<String>) any(),
				any(), any())).thenThrow(new ApisResourceAccessException());

		orderCardService.sendPhysicalCard(residentCredentialRequestDto);
	}

}