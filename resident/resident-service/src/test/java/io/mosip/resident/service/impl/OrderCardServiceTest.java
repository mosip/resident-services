package io.mosip.resident.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mosip.resident.util.AvailableClaimUtility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.ApiName;
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
import io.mosip.resident.service.ProxyPartnerManagementService;
import io.mosip.resident.service.ResidentCredentialService;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utility;

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
	private Utility utility;

	@Mock
	private IdentityServiceImpl identityServiceImpl;

	@Mock
	private ResidentTransactionRepository residentTransactionRepository;

	@Mock
	private ResidentServiceRestClient restClientWithSelfTOkenRestTemplate;

	@Mock
	private ResidentCredentialService residentCredentialService;
	
	@Mock
	private NotificationService notificationService;
	
	@Mock
	private ProxyPartnerManagementService proxyPartnerManagementService;

	@Mock
	private AvailableClaimUtility availableClaimUtility;

	@Mock
	Environment env;

	private ResponseWrapper responseWrapper;

	private ResidentCredentialResponseDto residentCredentialResponseDto;

	private ResidentCredentialRequestDto residentCredentialRequestDto;
	
	private NotificationResponseDTO notificationResponseDTO;
	
	private Map partnerDetail = new HashMap<>();
	
	private UrlRedirectRequestDTO urlRedirectRequestDTO;


	@Before
	public void setUp() throws Exception {
		ReflectionTestUtils.setField(orderCardService, "isPaymentEnabled", true);
		responseWrapper = new ResponseWrapper<>();
		responseWrapper.setVersion("v1");
		responseWrapper.setId("1");
		urlRedirectRequestDTO = new UrlRedirectRequestDTO();
		urlRedirectRequestDTO.setTrackingId("tracking123456");
		responseWrapper.setResponse(urlRedirectRequestDTO);
		ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
		residentTransactionEntity.setEventId("5092d4bf-8f77-4608-a167-76371cc38b5d");
		when(utility.createEntity(Mockito.any())).thenReturn(residentTransactionEntity);
		when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("8251649601");
		notificationResponseDTO = new NotificationResponseDTO();
		notificationResponseDTO.setStatus("Notification success");
		when(notificationService.sendNotification(Mockito.any(), Mockito.nullable(Map.class))).thenReturn(notificationResponseDTO);

		residentCredentialRequestDto = new ResidentCredentialRequestDto();
		residentCredentialRequestDto.setTransactionID("1234327890");
		residentCredentialRequestDto.setConsent("Accepted");
		residentCredentialRequestDto.setSharableAttributes(List.of("firstName"));
		residentCredentialResponseDto = new ResidentCredentialResponseDto();
		residentCredentialResponseDto.setId("8251649601");
		residentCredentialResponseDto.setRequestId("effc56cd-cf3b-4042-ad48-7277cf90f763");
		when(env.getProperty(anyString(), anyString())).thenReturn("property");
	}

	@Test
	public void testSendPhysicalCard() throws Exception {
		ReflectionTestUtils.setField(orderCardService, "isPaymentEnabled", false);
		when(residentCredentialService.reqCredential(any(), any())).thenReturn(residentCredentialResponseDto);
		ResidentCredentialResponseDto result = orderCardService.sendPhysicalCard(residentCredentialRequestDto);
		assertEquals("effc56cd-cf3b-4042-ad48-7277cf90f763", result.getRequestId());
	}

	@Test
	public void testSendPhysicalCardIf() throws Exception {
		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), (List<String>) any(), (List<String>) any(),
				any(), any())).thenReturn(responseWrapper);
		when(residentCredentialService.reqCredential(any(), any())).thenReturn(residentCredentialResponseDto);

		ResidentCredentialResponseDto result = orderCardService.sendPhysicalCard(residentCredentialRequestDto);
		assertEquals("effc56cd-cf3b-4042-ad48-7277cf90f763", result.getRequestId());
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testCheckOrderStatusWithApisResourceAccessException() throws Exception {
		when(restClientWithSelfTOkenRestTemplate.getApi((ApiName) any(), (List<String>) any(), (List<String>) any(),
				any(), any())).thenThrow(new ApisResourceAccessException());
		orderCardService.sendPhysicalCard(residentCredentialRequestDto);
	}
	
	@Test
	public void testGetRedirectUrl() throws Exception {
		Map detail = new HashMap<>();
		detail.put("orderRedirectUrl", "http://resident-partner-details.com");
		partnerDetail.put("additionalInfo", List.of(detail));
		when(proxyPartnerManagementService.getPartnerDetailFromPartnerIdAndPartnerType(anyString(), anyString())).thenReturn(partnerDetail);
		String  result = orderCardService.getRedirectUrl("12345","URI");
		assertNotNull(result);
	}
	
	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetRedirectUrlNull() throws Exception {
		Map detail = new HashMap<>();
		detail.put("orderRedirectUrl", "");
		partnerDetail.put("additionalInfo", List.of(detail));
		when(proxyPartnerManagementService.getPartnerDetailFromPartnerIdAndPartnerType(anyString(), anyString())).thenReturn(partnerDetail);
		orderCardService.getRedirectUrl("12345","URI");
	}
	
	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetRedirectUrlEmpty() throws Exception {
		when(proxyPartnerManagementService.getPartnerDetailFromPartnerIdAndPartnerType(anyString(), anyString())).thenReturn(partnerDetail);
		orderCardService.getRedirectUrl("12345","URI");
	}

}