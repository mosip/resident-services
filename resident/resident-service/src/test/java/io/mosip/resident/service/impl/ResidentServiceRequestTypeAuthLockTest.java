package io.mosip.resident.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.idvalidator.spi.RidValidator;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDtoV2;
import io.mosip.resident.dto.AuthTypeStatusDto;
import io.mosip.resident.dto.AuthTypeStatusDtoV2;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.dto.PacketGeneratorResDto;
import io.mosip.resident.dto.ResidentDocuments;
import io.mosip.resident.dto.ResidentUpdateRequestDto;
import io.mosip.resident.dto.ResponseDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.DocumentService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.PartnerService;
import io.mosip.resident.service.ResidentService;
import io.mosip.resident.util.UINCardDownloadService;
import io.mosip.resident.util.Utility;
import reactor.util.function.Tuple2;


@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class ResidentServiceRequestTypeAuthLockTest {

	@Mock
	private VidValidator<String> vidValidator;

	@Mock
	private UinValidator<String> uinValidator;

	@Mock
	private RidValidator<String> ridValidator;

	@Mock
	private UINCardDownloadService uinCardDownloadService;

	@Mock
	private IdAuthService idAuthService;

	@Mock
	NotificationService notificationService;

	@Mock
	PartnerServiceImpl partnerServiceImpl;

	@Mock
	PartnerService partnerService;

	@Mock
	IdentityServiceImpl identityServiceImpl;
	
	@Mock
	private Utility utility;

	@Mock
	ResidentTransactionRepository residentTransactionRepository;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private DocumentService docService;

	@Mock
	private Environment environment;

	@InjectMocks
	private ResidentService residentService = new ResidentServiceImpl();

	NotificationResponseDTO notificationResponseDTO;

	AuthTypeStatusDto authTypeStatusDto;

	private String individualId;
	
	@Value("${resident.authLockStatusUpdateV2.id}")
	private String authLockStatusUpdateV2Id;

	@Before
	public void setup() throws ApisResourceAccessException, ResidentServiceCheckedException {

		notificationResponseDTO = new NotificationResponseDTO();
		notificationResponseDTO.setStatus("success");
		notificationResponseDTO.setMessage("Notification success");
		individualId = identityServiceImpl.getResidentIndvidualIdFromSession();
		
		List<ResidentTransactionEntity> residentTransactionEntities=new ArrayList<>();
		ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
        residentTransactionEntity.setEventId("12345");
		when(utility.createEntity(Mockito.any())).thenReturn(residentTransactionEntity);
		residentTransactionEntities.add(residentTransactionEntity);
		Mockito.when(utility.createEventId()).thenReturn("12345");
		ArrayList<String> partnerIds = new ArrayList<>();
		partnerIds.add("m-partner-default-auth");
		ReflectionTestUtils.invokeMethod(residentService, "createResidentTransactionEntity", "2157245364", "partnerId", "2157245364");
		ReflectionTestUtils.setField(residentService, "authTypes", "otp,bio-FIR,bio-IIR,bio-FACE");
		ReflectionTestUtils.setField(residentService, "authLockStatusUpdateV2Id", "mosip.resident.auth.lock.status.update");
	}

	@Test
	public void testReqAuthTypeStatusUpdateSuccess()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
		authTypeStatusDto.setAuthType("OTP");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(10L);
		List<AuthTypeStatusDtoV2> authTypeStatusDtoList = new java.util.ArrayList<>();
		authTypeStatusDtoList.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthTypes(authTypeStatusDtoList);
		for (AuthTypeStatusDto authTypeStatusDto1 : authLockOrUnLockRequestDtoV2.getAuthTypes()) {
			Mockito.when(idAuthService.authTypeStatusUpdateForRequestId(any(), any(), any())).thenReturn("123");
			Mockito.when(notificationService.sendNotification(Mockito.any(), Mockito.nullable(Map.class))).thenReturn(notificationResponseDTO);
			Tuple2<ResponseDTO, String> authLockResponse = residentService.reqAauthTypeStatusUpdateV2(authLockOrUnLockRequestDtoV2);
			assertEquals("The chosen authentication types have been successfully locked/unlocked.", authLockResponse.getT1().getMessage());
		}
	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAuthTypeLockFailed()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
		authTypeStatusDto.setAuthType("OTP");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(10L);
		List<AuthTypeStatusDtoV2> authTypeStatusDtoList = new java.util.ArrayList<>();
		authTypeStatusDtoList.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthTypes(authTypeStatusDtoList);
		residentService.reqAauthTypeStatusUpdateV2(authLockOrUnLockRequestDtoV2);

	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testReqAuthTypeLockNotificationFailed()
			throws ApisResourceAccessException, ResidentServiceCheckedException {

		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
		authTypeStatusDto.setAuthType("OTP");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(10L);
		List<AuthTypeStatusDtoV2> authTypeStatusDtoList = new java.util.ArrayList<>();
		authTypeStatusDtoList.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthTypes(authTypeStatusDtoList);
		Mockito.when(notificationService.sendNotification(Mockito.any(), Mockito.nullable(Map.class)))
				.thenThrow(new ResidentServiceCheckedException());
		residentService.reqAauthTypeStatusUpdateV2(authLockOrUnLockRequestDtoV2);

	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAuthTypeLockException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {

		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
		authTypeStatusDto.setAuthType("OTP");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(10L);
		List<AuthTypeStatusDtoV2> authTypeStatusDtoList = new java.util.ArrayList<>();
		authTypeStatusDtoList.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthTypes(authTypeStatusDtoList);
		residentService.reqAauthTypeStatusUpdateV2(authLockOrUnLockRequestDtoV2);
	}

	@Test(expected = ResidentServiceException.class)
	public void testReqAuthTypeLockOTPFailedException()
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
		authTypeStatusDto.setAuthType("OTP");
		authTypeStatusDto.setLocked(false);
		authTypeStatusDto.setUnlockForSeconds(10L);
		List<AuthTypeStatusDtoV2> authTypeStatusDtoList = new java.util.ArrayList<>();
		authTypeStatusDtoList.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthTypes(authTypeStatusDtoList);
		residentService.reqAauthTypeStatusUpdateV2(authLockOrUnLockRequestDtoV2);

	}

	@Test
	public void testGetAuthTypeBasedOnConfigV2(){
		AuthTypeStatusDtoV2 authTypeStatus = new AuthTypeStatusDtoV2();
		authTypeStatus.setAuthType("demo");
		assertEquals("demo",ResidentServiceImpl.getAuthTypeBasedOnConfigV2(authTypeStatus));
	}

	@Test
	public void testGetAuthTypeBasedOnConfigV2WithSubType(){
		AuthTypeStatusDtoV2 authTypeStatus = new AuthTypeStatusDtoV2();
		authTypeStatus.setAuthType("demo");
		authTypeStatus.setAuthSubType("demo");
		assertEquals(null,ResidentServiceImpl.getAuthTypeBasedOnConfigV2(authTypeStatus));
	}

	@Test
	public void testGetAuthTypeBasedOnConfigV2WithoutSubType(){
		AuthTypeStatusDtoV2 authTypeStatus = new AuthTypeStatusDtoV2();
		authTypeStatus.setAuthType("bio");
		authTypeStatus.setAuthSubType("FIR");
		assertEquals("bio-FIR",ResidentServiceImpl.getAuthTypeBasedOnConfigV2(authTypeStatus));
	}

	@Test
	public void testTrySendNotificationFailure() throws ResidentServiceCheckedException {
		Mockito.when(notificationService.sendNotification(any(), Mockito.nullable(Map.class))).thenThrow(new ResidentServiceCheckedException());
		ReflectionTestUtils.invokeMethod(residentService,
				"trySendNotification", "123", null, null);
	}

	@Test
	public void testCreateResidentTransEntity() {
		ResidentUpdateRequestDto residentUpdateRequestDto = new ResidentUpdateRequestDto();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("IDSchemaVersion", null);
		jsonObject.put("UIN", null);
		residentUpdateRequestDto.setIdentity(jsonObject);
		residentUpdateRequestDto.setIndividualId("123434343");
		assertNotNull(ReflectionTestUtils.invokeMethod(residentService,
				"createResidentTransEntity", residentUpdateRequestDto, "1234567890"));
	}

	@Test
	public void testUpdateResidentTransaction() {
		PacketGeneratorResDto response = new PacketGeneratorResDto();
		response.setRegistrationId("123");
		ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
		ReflectionTestUtils.invokeMethod(residentService,
				"updateResidentTransaction", residentTransactionEntity, response);
	}

	@Test
	public void testGetResidentDocuments() {
		ResidentUpdateRequestDto residentUpdateRequestDto = new ResidentUpdateRequestDto();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("IDSchemaVersion", null);
		jsonObject.put("UIN", null);
		residentUpdateRequestDto.setIdentity(jsonObject);
		residentUpdateRequestDto.setIndividualId("123434343");
		JSONObject mappingDocument = new JSONObject();
		mappingDocument.put("key", "value");
		ReflectionTestUtils.invokeMethod(residentService,
				"getResidentDocuments", residentUpdateRequestDto, mappingDocument);
	}

	@Test
	public void testGetResidentDocumentsNullDocuments() {
		ResidentUpdateRequestDto residentUpdateRequestDto = new ResidentUpdateRequestDto();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("IDSchemaVersion", null);
		jsonObject.put("UIN", null);
		residentUpdateRequestDto.setIdentity(jsonObject);
		residentUpdateRequestDto.setIndividualId("123434343");
		ResidentDocuments residentDocuments = new ResidentDocuments("key", "value");
		residentUpdateRequestDto.setDocuments(List.of(residentDocuments));
		ReflectionTestUtils.invokeMethod(residentService,
				"getResidentDocuments", residentUpdateRequestDto, null);
	}

	@Test
	public void testGetResidentDocumentValidDocuments() {
		ResidentUpdateRequestDto residentUpdateRequestDto = new ResidentUpdateRequestDto();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("IDSchemaVersion", null);
		jsonObject.put("UIN", null);
		residentUpdateRequestDto.setIdentity(jsonObject);
		residentUpdateRequestDto.setIndividualId("123434343");
		residentUpdateRequestDto.setTransactionID("123");
		ReflectionTestUtils.invokeMethod(residentService,
				"getResidentDocuments", residentUpdateRequestDto, null);
	}

	@Test(expected = Exception.class)
	public void testGetResidentDocumentInValidDocuments() throws ResidentServiceCheckedException {
		ResidentUpdateRequestDto residentUpdateRequestDto = new ResidentUpdateRequestDto();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("IDSchemaVersion", null);
		jsonObject.put("UIN", null);
		residentUpdateRequestDto.setIdentity(jsonObject);
		residentUpdateRequestDto.setIndividualId("123434343");
		residentUpdateRequestDto.setTransactionID("123");
		Mockito.when(docService.getDocumentsWithMetadata(Mockito.anyString())).thenThrow(new ResidentServiceCheckedException());
		ReflectionTestUtils.invokeMethod(residentService,
				"getResidentDocuments", residentUpdateRequestDto, null);
	}


}
