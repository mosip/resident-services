package io.mosip.resident.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.mosip.resident.dto.IdResponseDTO1;
import io.mosip.resident.util.*;
import io.mosip.resident.validator.RequestValidator;
import io.mosip.resident.validator.ValidateNewUpdateRequest;
import io.mosip.resident.validator.ValidateSameData;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.FileNotFoundException;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectIOException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectValidationFailedException;
import io.mosip.kernel.core.idobjectvalidator.exception.InvalidIdSchemaException;
import io.mosip.kernel.core.idobjectvalidator.spi.IdObjectValidator;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.MachineCreateResponseDTO;
import io.mosip.resident.dto.MachineDto;
import io.mosip.resident.dto.MachineErrorDTO;
import io.mosip.resident.dto.MachineSearchResponseDTO;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.dto.PacketGeneratorResDto;
import io.mosip.resident.dto.PacketSignPublicKeyErrorDTO;
import io.mosip.resident.dto.PacketSignPublicKeyResponseDTO;
import io.mosip.resident.dto.ResidentDocuments;
import io.mosip.resident.dto.ResidentUpdateRequestDto;
import io.mosip.resident.dto.ResidentUpdateResponseDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.exception.ValidationFailedException;
import io.mosip.resident.handler.service.ResidentUpdateService;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.DocumentService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ProxyMasterdataService;
import reactor.util.function.Tuple2;

@RunWith(SpringRunner.class)
public class ResidentServiceResUpdateTest {
	@InjectMocks
	ResidentServiceImpl residentServiceImpl;

	@Mock
	private ResidentUpdateService residentUpdateService;

	@Mock
	private ResidentServiceRestClient residentServiceRestClient;

	@Mock
	private IdAuthService idAuthService;

	@MockBean
	private DocumentService docService;

	@MockBean
	private ObjectStoreHelper objectStore;

	@Mock
	private UinValidator<String> uinValidator;

	@Mock
	private ResidentTransactionRepository residentTransactionRepository;

	@Mock
	private IdentityServiceImpl identityServiceImpl;

	@Mock
	Environment env;

	@Mock
	NotificationService notificationService;
	@Mock
	private Utility utility;

	@Mock
	private Utilities utilities;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private ValidateSameData validateSameData;
	
	@Mock
	private ProxyMasterdataService proxyMasterdataService;
	
	@Mock
	private IdObjectValidator idObjectValidator;

	@Mock
	private ResidentConfigServiceImpl residentConfigService;

	@Mock
	RequestValidator requestValidator;

	@Mock
	ValidateNewUpdateRequest validateNewUpdateRequest;

	@Mock
	private MaskDataUtility maskDataUtility;

	@Mock
	private AvailableClaimUtility availableClaimUtility;

	@Mock
	private AccessTokenUtility accessTokenUtility;

	ResidentUpdateRequestDto dto;

	PacketGeneratorResDto updateDto;

	private JSONObject idRepoJson;
	private String schemaJson;
	private JSONObject demographicIdentity;
	private IdResponseDTO1 idResponseDto;

	@Before
	public void setUp() throws BaseCheckedException, IOException {

		dto = new ResidentUpdateRequestDto();
		ResidentDocuments document = new ResidentDocuments();
		document.setName("POA_Certificate of residence");
		document.setValue(
				"_9j_4AAQSkZJRgABAQAAAQABAAD_2wCEAAkGBxMTEhUSExIVFRUVFRUVFRUWFxUVFRcVFRYWFhUVFRYYHiggGBolHhcVITEhJSkrLi4uFx8zODMtNygtLisBCgoKDg0OGhAQGy0dHR0tLS0tLS0tLS0tLS0tLS0tKy0tKy0tLS0tLS0tKy0tLS0tKystLS0tLS0tLS0tLSsrK__AABEIALMBGgMBIgACEQEDEQH_xAAbAAABBQEBAAAAAAAAAAAAAAADAAIEBQYBB__EA");
		List<ResidentDocuments> documents = new ArrayList<>();
		documents.add(document);
		dto.setDocuments(documents);
		dto.setIdentityJson(
				"ewogICJpZGVudGl0eSIgOiB7CiAgICAiZGF0ZU9mQmlydGgiIDogIjE5OTUvMDgvMDgiLAogICAgImFnZSIgOiAyNywKICAgICJwaG9uZSIgOiAiOTc4NjU0MzIxMCIsCiAgICAiZW1haWwiIDogImdpcmlzaC55YXJydUBtaW5kdHJlZS5jb20iLAogICAgInByb29mT2ZBZGRyZXNzIiA6IHsKICAgICAgInZhbHVlIiA6ICJQT0FfQ2VydGlmaWNhdGUgb2YgcmVzaWRlbmNlIiwKICAgICAgInR5cGUiIDogIkNPUiIsCiAgICAgICJmb3JtYXQiIDogImpwZyIKICAgIH0sCgkiVUlOIjogIjM1Mjc4MTI0MDYiLAogICAgIklEU2NoZW1hVmVyc2lvbiIgOiAxLjAKICB9Cn0=");
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("IDENTITY", "name");
		dto.setIdentity(new JSONObject());
		dto.setIndividualId("3527812406");
		dto.setIndividualIdType(IdType.UIN.name());
		dto.setTransactionID("12345");
		dto.setOtp("12345");
		dto.setConsent("Accepted");
		ReflectionTestUtils.setField(residentServiceImpl, "centerId", "10008");
		ReflectionTestUtils.setField(residentServiceImpl, "machineId", "10008");

		Map identityResponse = new LinkedHashMap();
		Map identityMap = new LinkedHashMap();
		identityMap.put("UIN", "8251649601");
		identityMap.put("email", "manojvsp12@gmail.com");
		identityResponse.put("identity", identityMap);
		
		ResponseWrapper idSchemaResponse = new ResponseWrapper();
		JSONObject object = new JSONObject();
		Object schema = "{\\\"$schema\\\":\\\"http:\\/\\/json-schema.org\\/draft-07\\/schema#\\\",\\\"description\\\":\\\"MOSIP Sample identity\\\",\\\"additionalProperties\\\":false,\\\"title\\\":\\\"MOSIP identity\\\",\\\"type\\\":\\\"object\\\",\\\"definitions\\\":{\\\"simpleType\\\":{\\\"uniqueItems\\\":true,\\\"additionalItems\\\":false,\\\"type\\\":\\\"array\\\",\\\"items\\\":{\\\"additionalProperties\\\":false,\\\"type\\\":\\\"object\\\",\\\"required\\\":[\\\"language\\\",\\\"value\\\"],\\\"properties\\\":{\\\"language\\\":{\\\"type\\\":\\\"string\\\"},\\\"value\\\":{\\\"type\\\":\\\"string\\\"}}}},\\\"documentType\\\":{\\\"additionalProperties\\\":false,\\\"type\\\":\\\"object\\\",\\\"properties\\\":{\\\"format\\\":{\\\"type\\\":\\\"string\\\"},\\\"type\\\":{\\\"type\\\":\\\"string\\\"},\\\"value\\\":{\\\"type\\\":\\\"string\\\"},\\\"refNumber\\\":{\\\"type\\\":[\\\"string\\\",\\\"null\\\"]}}},\\\"biometricsType\\\":{\\\"additionalProperties\\\":false,\\\"type\\\":\\\"object\\\",\\\"properties\\\":{\\\"format\\\":{\\\"type\\\":\\\"string\\\"},\\\"version\\\":{\\\"type\\\":\\\"number\\\",\\\"minimum\\\":0},\\\"value\\\":{\\\"type\\\":\\\"string\\\"}}}},\\\"properties\\\":{\\\"identity\\\":{\\\"additionalProperties\\\":false,\\\"type\\\":\\\"object\\\",\\\"required\\\":[\\\"IDSchemaVersion\\\",\\\"fullName\\\",\\\"dateOfBirth\\\",\\\"gender\\\",\\\"addressLine1\\\",\\\"addressLine2\\\",\\\"addressLine3\\\",\\\"region\\\",\\\"province\\\",\\\"city\\\",\\\"zone\\\",\\\"postalCode\\\",\\\"phone\\\",\\\"email\\\",\\\"proofOfIdentity\\\",\\\"individualBiometrics\\\"],\\\"properties\\\":{\\\"proofOfAddress\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"gender\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"city\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{0,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"postalCode\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^[(?i)A-Z0-9]{5}$|^NA$\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"proofOfException-1\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"evidence\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"referenceIdentityNumber\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^([0-9]{10,30})$\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"kyc\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"individualBiometrics\\\":{\\\"bioAttributes\\\":[\\\"leftEye\\\",\\\"rightEye\\\",\\\"rightIndex\\\",\\\"rightLittle\\\",\\\"rightRing\\\",\\\"rightMiddle\\\",\\\"leftIndex\\\",\\\"leftLittle\\\",\\\"leftRing\\\",\\\"leftMiddle\\\",\\\"leftThumb\\\",\\\"rightThumb\\\",\\\"face\\\"],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/biometricsType\\\"},\\\"province\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{0,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"zone\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"proofOfDateOfBirth\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"addressLine1\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{0,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"addressLine2\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{3,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"residenceStatus\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"kyc\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"addressLine3\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{3,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"email\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^[A-Za-z0-9_\\\\\\\\-]+(\\\\\\\\.[A-Za-z0-9_]+)*@[A-Za-z0-9_-]+(\\\\\\\\.[A-Za-z0-9_]+)*(\\\\\\\\.[a-zA-Z]{2,})$\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"introducerRID\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"evidence\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"introducerBiometrics\\\":{\\\"bioAttributes\\\":[\\\"leftEye\\\",\\\"rightEye\\\",\\\"rightIndex\\\",\\\"rightLittle\\\",\\\"rightRing\\\",\\\"rightMiddle\\\",\\\"leftIndex\\\",\\\"leftLittle\\\",\\\"leftRing\\\",\\\"leftMiddle\\\",\\\"leftThumb\\\",\\\"rightThumb\\\",\\\"face\\\"],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/biometricsType\\\"},\\\"fullName\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{3,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"dateOfBirth\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(1869|18[7-9][0-9]|19[0-9][0-9]|20[0-9][0-9])\\/([0][1-9]|1[0-2])\\/([0][1-9]|[1-2][0-9]|3[01])$\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"individualAuthBiometrics\\\":{\\\"bioAttributes\\\":[\\\"leftEye\\\",\\\"rightEye\\\",\\\"rightIndex\\\",\\\"rightLittle\\\",\\\"rightRing\\\",\\\"rightMiddle\\\",\\\"leftIndex\\\",\\\"leftLittle\\\",\\\"leftRing\\\",\\\"leftMiddle\\\",\\\"leftThumb\\\",\\\"rightThumb\\\",\\\"face\\\"],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/biometricsType\\\"},\\\"introducerUIN\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"evidence\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"proofOfIdentity\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"IDSchemaVersion\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"none\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"number\\\",\\\"fieldType\\\":\\\"default\\\",\\\"minimum\\\":0},\\\"proofOfException\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"evidence\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"phone\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^[+]*([0-9]{1})([0-9]{9})$\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"introducerName\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"evidence\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"proofOfRelationship\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"UIN\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"none\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"region\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{0,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"}}}}}";
		object.put("schemaJson", schema);
		idSchemaResponse.setResponse(object);
		Map<String, ?> map = new HashMap<>();
		when(objectMapper.convertValue(object, Map.class)).thenReturn(object);

		when(proxyMasterdataService.getLatestIdSchema(0.2, null, null)).thenReturn(idSchemaResponse);
		JSONObject obj = new JSONObject();
		obj.put("IDSchemaVersion", 0.2);
		
		when(utilities.retrieveIdrepoJson(any())).thenReturn(obj);

		ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
		residentTransactionEntity.setEventId(UUID.randomUUID().toString());
		when(utility.createEntity(Mockito.any())).thenReturn(residentTransactionEntity);
		when(utility.createEventId()).thenReturn("1232312321432432");
		byte[] str = CryptoUtil.decodeURLSafeBase64(dto.getIdentityJson());
		when(objectMapper.readValue(str, Map.class)).thenReturn(identityResponse);

		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		Mockito.when(utility.getMappingJson()).thenReturn(mappingJson);

		Mockito.when(idAuthService.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(true);

		NotificationResponseDTO notificationResponse = new NotificationResponseDTO();
		notificationResponse.setMessage("Notification sent");
		notificationResponse.setStatus("success");
		Mockito.when(notificationService.sendNotification(any(), Mockito.nullable(Map.class))).thenReturn(notificationResponse);

		Mockito.when(env.getProperty(ApiName.MACHINESTATUSUPDATE.name())).thenReturn("http://localhost");
		when(residentServiceRestClient.patchApi(any(), any(), any(), any())).thenReturn(new io.mosip.kernel.core.http.ResponseWrapper<>());

		String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuGXPqbFOIZhB_N_fbTXOMIsRgq_LMdL9DJ5kWYAneCj_LPw3OEm2ncLVIRyJsF2DcSQwvzt_Njdvg1Cr54nD1uHBu3Vt9G1sy3p6uwbeK1l5mJSMNe5oGe11fmehtsR2QcB_45_us_IiiiUzzHJrySexmDfdOiPdy-dID4DYRDAf-HXlMIEf4Di_8NV3wVrA3jq1tuNkXX3qKtM4NhZOihp0HmB9E7RHttSV9VJNh00BrC57qdMfa5xqsHok3qftU5SAan4BGuPklN2fzOVcsa-V-B8JbwxRfPdwMkq-jW7Eu1LcNhNVQYJGEWDLAQDGKY_fOB_YwBzn8xvYRjqSfQIDAQAB";
		List<MachineDto> machineDtos = new ArrayList<>();
		MachineDto machineDto = new MachineDto();
		machineDto.setMachineSpecId("1001");
		machineDto.setIsActive(false);
		machineDto.setId("10147");
		machineDto.setName("resident_machine_1640777004542");
		machineDto.setValidityDateTime("2024-12-29T11:23:24.541Z");
		machineDto.setPublicKey(publicKey);
		machineDto.setSignPublicKey(publicKey);
		machineDtos.add(machineDto);
		MachineSearchResponseDTO.MachineSearchDto response = MachineSearchResponseDTO.MachineSearchDto.builder()
				.fromRecord(0).toRecord(0).toRecord(0).data(machineDtos).build();
		MachineSearchResponseDTO machineSearchResponseDTO = new MachineSearchResponseDTO();
		machineSearchResponseDTO.setId("null");
		machineSearchResponseDTO.setVersion("1.0");
		machineSearchResponseDTO.setResponsetime("2022-01-28T06:25:23.958Z");
		machineSearchResponseDTO.setResponse(response);

		PacketSignPublicKeyResponseDTO responseDto = new PacketSignPublicKeyResponseDTO();
		PacketSignPublicKeyResponseDTO.PacketSignPublicKeyResponse publicKeyResponse = new PacketSignPublicKeyResponseDTO.PacketSignPublicKeyResponse();
		publicKeyResponse.setPublicKey(publicKey);
		responseDto.setId(null);
		responseDto.setVersion(null);
		responseDto.setResponsetime("2022-01-28T06:51:30.286Z");
		responseDto.setResponse(publicKeyResponse);
		responseDto.setErrors(new ArrayList<>());

		updateDto = new PacketGeneratorResDto();
		updateDto.setRegistrationId("10008100670001720191120095702");
		Mockito.when(residentUpdateService.createPacket(any(), any(), any(), any())).thenReturn(updateDto);
		Mockito.when(residentUpdateService.createPacket(any(), any())).thenReturn(updateDto);

		Mockito.when(env.getProperty(ApiName.PACKETSIGNPUBLICKEY.name())).thenReturn("PACKETSIGNPUBLICKEY");
		Mockito.when(env.getProperty(ApiName.MACHINESEARCH.name())).thenReturn("MACHINESEARCH");

		Mockito.when(residentServiceRestClient.postApi(eq("PACKETSIGNPUBLICKEY"), any(MediaType.class),
				any(HttpEntity.class), eq(PacketSignPublicKeyResponseDTO.class))).thenReturn(responseDto);
		Mockito.when(residentServiceRestClient.postApi(eq("MACHINESEARCH"), any(MediaType.class), any(HttpEntity.class),
				eq(MachineSearchResponseDTO.class))).thenReturn(machineSearchResponseDTO);

		when(utilities.getLanguageCode()).thenReturn("eng");
		idRepoJson = new JSONObject();
		idRepoJson.put("UIN", "8251649601");
		idRepoJson.put("email", "manojvsp12@gmail.com");
		schemaJson = (String) schema;

		byte[] decodedDemoJson = CryptoUtil.decodeURLSafeBase64(dto.getIdentityJson());
		JSONObject demographicJsonObject;
		demographicJsonObject = JsonUtil.readValue(new String(decodedDemoJson), JSONObject.class);
		demographicIdentity = JsonUtil.getJSONObject(demographicJsonObject, "identity");
	}

	@Test(expected = ResidentServiceException.class)
	public void reqUinUpdateGetPublicKeyFromKeyManagerThrowsApiResourceExceptionTest()
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		when(residentServiceRestClient.postApi(eq("PACKETSIGNPUBLICKEY"), any(MediaType.class), any(HttpEntity.class),
				eq(PacketSignPublicKeyResponseDTO.class))).thenThrow(new ApisResourceAccessException());
		residentServiceImpl.reqUinUpdate(dto);
	}

	@Test(expected = ResidentServiceException.class)
	public void reqUinUpdateGetPublicKeyFromKeyManagerThrowsResidentServiceTPMSignKeyExceptionTest()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		PacketSignPublicKeyResponseDTO responseDto = new PacketSignPublicKeyResponseDTO();
		List<PacketSignPublicKeyErrorDTO> errorDTOS = new ArrayList<>();
		PacketSignPublicKeyErrorDTO errorDTO = new PacketSignPublicKeyErrorDTO();
		errorDTO.setErrorCode(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode());
		errorDTO.setMessage(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage());
		errorDTOS.add(errorDTO);
		responseDto.setId(null);
		responseDto.setVersion(null);
		responseDto.setResponsetime("2022-01-28T06:51:30.286Z");
		responseDto.setErrors(errorDTOS);
		when(residentServiceRestClient.postApi(any(), any(), any(), any(Class.class))).thenReturn(responseDto);
		residentServiceImpl.reqUinUpdate(dto);
	}

	@Test(expected = ResidentServiceException.class)
	public void reqUinUpdateGetPublicKeyFromKeyManagerThrowsResidentServiceTPMSignKeyExceptionWithNullResponseTest()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		PacketSignPublicKeyResponseDTO responseDto = new PacketSignPublicKeyResponseDTO();
		responseDto.setId(null);
		responseDto.setVersion(null);
		responseDto.setResponsetime("2022-01-28T06:51:30.286Z");
		responseDto.setResponse(null);
		when(residentServiceRestClient.postApi(any(), any(), any(), any(Class.class))).thenReturn(responseDto);
		residentServiceImpl.reqUinUpdate(dto);
	}

	@Test(expected = ResidentServiceException.class)
	public void reqUinUpdateSearchMachineInMasterServiceThrowsApisResourceAccessExceptionTest()
			throws ApisResourceAccessException, ResidentServiceCheckedException, OtpValidationFailedException {
		Mockito.when(residentServiceRestClient.postApi(eq("MACHINESEARCH"), any(MediaType.class), any(HttpEntity.class),
				eq(MachineSearchResponseDTO.class))).thenThrow(new ApisResourceAccessException());
		residentServiceImpl.reqUinUpdate(dto);

	}

	@Test(expected = ResidentServiceException.class)
	public void reqUinUpdateSearchMachineInMasterServiceThrowsResidentMachineServiceExceptionTest()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		List<MachineErrorDTO> errorDTOS = new ArrayList<>();
		MachineErrorDTO errorDTO = new MachineErrorDTO();
		errorDTO.setErrorCode(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode());
		errorDTO.setMessage(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage());
		errorDTOS.add(errorDTO);

		MachineSearchResponseDTO machineSearchResponseDTO = new MachineSearchResponseDTO();
		machineSearchResponseDTO.setId("null");
		machineSearchResponseDTO.setVersion("1.0");
		machineSearchResponseDTO.setResponsetime("2022-01-28T06:25:23.958Z");
		machineSearchResponseDTO.setErrors(errorDTOS);
		when(residentServiceRestClient.postApi(eq("MACHINESEARCH"), any(MediaType.class), any(HttpEntity.class),
				eq(MachineSearchResponseDTO.class))).thenReturn(machineSearchResponseDTO);
		residentServiceImpl.reqUinUpdate(dto);
	}

	@Test
	public void reqUinUpdateGetMachineIdTest() throws BaseCheckedException {
		Tuple2<Object, String> residentUpdateResponseDTO = residentServiceImpl.reqUinUpdate(dto);
		assertEquals(((ResidentUpdateResponseDTO) residentUpdateResponseDTO.getT1()).getRegistrationId(), updateDto.getRegistrationId());
	}

	@Test(expected = ResidentServiceException.class)
	public void reqUinUpdateGetMachineIdTestWithSecureSession() throws BaseCheckedException, IOException {
		IdentityServiceTest.getAuthUserDetailsFromAuthentication();
		dto.setConsent(null);
		Tuple2<Object, String> residentUpdateResponseDTO = residentServiceImpl.reqUinUpdate
				(dto, demographicIdentity, false, idRepoJson, schemaJson, idResponseDto);
		assertEquals(((ResidentUpdateResponseDTO) residentUpdateResponseDTO.getT1()).getRegistrationId(), updateDto.getRegistrationId());
	}

	@Test(expected = ResidentServiceException.class)
	public void reqUinUpdateGetMachineIdTestWithSecureSessionDemographicEntityFailed() throws BaseCheckedException, IOException {
		IdentityServiceTest.getAuthUserDetailsFromAuthentication();
		Mockito.when(utility.getMappingJson()).thenReturn(null);
		Tuple2<Object, String> residentUpdateResponseDTO = residentServiceImpl.reqUinUpdate(dto, demographicIdentity, false, idRepoJson, schemaJson, idResponseDto);
		assertEquals(((ResidentUpdateResponseDTO) residentUpdateResponseDTO.getT1()).getRegistrationId(), updateDto.getRegistrationId());
	}

	@Test
	public void reqUinUpdateGetMachineIdTestWithSecureSessionSuccess() throws BaseCheckedException, IOException {
		IdentityServiceTest.getAuthUserDetailsFromAuthentication();
		Tuple2<Object, String> residentUpdateResponseDTO = residentServiceImpl.reqUinUpdate
				(dto, demographicIdentity, false, idRepoJson, schemaJson, idResponseDto);
		assertEquals("10008100670001720191120095702", updateDto.getRegistrationId());
	}

	@Test
	public void reqUinUpdateGetMachineIdIsNullTest() throws BaseCheckedException, IOException {
		String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuGXPqbFOIZhB_N_fbTXOMIsRgq_LMdL9DJ5kWYAneCj_LPw3OEm2ncLVIRyJsF2DcSQwvzt_Njdvg1Cr54nD1uHBu3Vt9G1sy3p6uwbeK1l5mJSMNe5oGe11fmehtsR2QcB_45_us_IiiiUzzHJrySexmDfdOiPdy-dID4DYRDAf-HXlMIEf4Di_8NV3wVrA3jq1tuNkXX3qKtM4NhZOihp0HmB9E7RHttSV9VJNh00BrC57qdMfa5xqsHok3qftU5SAan4BGuPklN2fzOVcsa-V-B8JbwxRfPdwMkq-jW7Eu1LcNhNVQYJGEWDLAQDGKY_fOB_YwBzn8xvYRjqSfQIDAQAB";

		List<MachineDto> machineDtos = new ArrayList<>();
		MachineDto machineDto = new MachineDto();
		machineDto.setMachineSpecId("1001");
		machineDto.setIsActive(false);
		machineDto.setId("10147");
		machineDto.setName("resident_machine_1640777004542");
		machineDto.setValidityDateTime("2024-12-29T11:23:24.541Z");
		machineDto.setSignPublicKey("");
		machineDtos.add(machineDto);
		MachineSearchResponseDTO.MachineSearchDto response = MachineSearchResponseDTO.MachineSearchDto.builder()
				.fromRecord(0).toRecord(0).toRecord(0).data(machineDtos).build();
		MachineSearchResponseDTO machineSearchResponseDTO = new MachineSearchResponseDTO();
		machineSearchResponseDTO.setId("null");
		machineSearchResponseDTO.setVersion("1.0");
		machineSearchResponseDTO.setResponsetime("2022-01-28T06:25:23.958Z");
		machineSearchResponseDTO.setResponse(response);
		Mockito.when(residentServiceRestClient.postApi(eq("MACHINESEARCH"), any(MediaType.class), any(HttpEntity.class),
				eq(MachineSearchResponseDTO.class))).thenReturn(machineSearchResponseDTO);

		MachineCreateResponseDTO machineCreateResponseDTO = new MachineCreateResponseDTO();
		MachineDto newMachineDTO = new MachineDto();
		newMachineDTO.setMachineSpecId("1001");
		newMachineDTO.setIsActive(false);
		newMachineDTO.setId("10147");
		newMachineDTO.setName("resident_machine_1640777004542");
		newMachineDTO.setValidityDateTime("2024-12-29T11:23:24.541Z");
		newMachineDTO.setPublicKey(publicKey);
		newMachineDTO.setSignPublicKey(publicKey);
		machineCreateResponseDTO.setResponse(newMachineDTO);
		Mockito.when(env.getProperty(ApiName.MACHINECREATE.name())).thenReturn("MACHINECREATE");
		Mockito.when(residentServiceRestClient.postApi(eq("MACHINECREATE"), any(MediaType.class), any(HttpEntity.class),
				eq(MachineCreateResponseDTO.class))).thenReturn(machineCreateResponseDTO);
		Tuple2<Object, String> residentUpdateResponseDTO = residentServiceImpl.reqUinUpdate(dto);
		assertEquals(((ResidentUpdateResponseDTO) residentUpdateResponseDTO.getT1()).getRegistrationId(), updateDto.getRegistrationId());
		verify(residentServiceRestClient, atLeast(3)).postApi(any(), any(), any(), any(Class.class));
	}

	@Test
	public void reqUinUpdateGetMachineIdReturnsTest() throws BaseCheckedException, IOException {
		Tuple2<Object, String> residentUpdateResponseDTO = residentServiceImpl.reqUinUpdate(dto);
		assertEquals(((ResidentUpdateResponseDTO) residentUpdateResponseDTO.getT1()).getRegistrationId(), updateDto.getRegistrationId());
		verify(residentServiceRestClient, atLeast(2)).postApi(any(), any(), any(), any(Class.class));
	}

	@Test(expected = ResidentServiceException.class)
	public void validateOtpException()
			throws OtpValidationFailedException, IOException, ResidentServiceCheckedException {
		Mockito.when(idAuthService.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(false);
		residentServiceImpl.reqUinUpdate(dto);

	}

	@Test(expected = ResidentServiceException.class)
	public void validateOtpExceptionWithSecureSession()
			throws OtpValidationFailedException, IOException, ResidentServiceCheckedException {
		IdentityServiceTest.getAuthUserDetailsFromAuthentication();
		Mockito.when(idAuthService.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(false);
		residentServiceImpl.reqUinUpdate(dto, demographicIdentity, false, idRepoJson, schemaJson, idResponseDto);

	}

	@Test(expected = ResidentServiceException.class)
	public void JsonParsingException() throws ResidentServiceCheckedException {
		Mockito.when(utility.getMappingJson()).thenReturn(null);
		residentServiceImpl.reqUinUpdate(dto);

	}

	@Test(expected = ResidentServiceException.class)
	public void testIOException() throws BaseCheckedException, IOException {
		HttpClientErrorException exp = new HttpClientErrorException(HttpStatus.BAD_GATEWAY);
		Mockito.when(residentUpdateService.createPacket(any(), any())).thenThrow(new IOException("badgateway", exp));
		residentServiceImpl.reqUinUpdate(dto);
	}

	@Test(expected = ResidentServiceException.class)
	public void testApiResourceAccessExceptionServer() throws BaseCheckedException, IOException {
		HttpServerErrorException exp = new HttpServerErrorException(HttpStatus.BAD_GATEWAY);
		Mockito.when(residentUpdateService.createPacket(any(), any()))
				.thenThrow(new ApisResourceAccessException("badgateway", exp));
		residentServiceImpl.reqUinUpdate(dto);
	}

	@Test(expected = ResidentServiceException.class)
	public void testBaseCheckedException() throws BaseCheckedException, IOException {
		Mockito.when(residentUpdateService.createPacket(any(), any()))
				.thenThrow(new BaseCheckedException("erorcode", "badgateway", new RuntimeException()));
		residentServiceImpl.reqUinUpdate(dto);
	}

	@Test(expected = ResidentServiceException.class)
	public void otpValidationFailedException() throws OtpValidationFailedException, ResidentServiceCheckedException {
		Mockito.when(idAuthService.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenThrow(new OtpValidationFailedException());
		residentServiceImpl.reqUinUpdate(dto);

	}

	@Test
	public void testValidationOfAuthIndividualIdWithUIN() throws ResidentServiceCheckedException,
			OtpValidationFailedException, ApisResourceAccessException, FileNotFoundException, IOException {
		dto.setIndividualId("3527812407");
		try {
			residentServiceImpl.reqUinUpdate(dto);
		} catch (ResidentServiceException e) {
			assertEquals(ResidentErrorCode.INDIVIDUAL_ID_UIN_MISMATCH.getErrorCode(),
					((ValidationFailedException) e.getCause()).getErrorCode());
		}
	}

	@Test
	public void testValidationOfAuthIndividualIdWithUINWithSecureSession() throws ResidentServiceCheckedException {
		IdentityServiceTest.getAuthUserDetailsFromAuthentication();
		dto.setIndividualId("3527812407");
		try {
			residentServiceImpl.reqUinUpdate(dto, demographicIdentity, false, idRepoJson, schemaJson, idResponseDto);
		} catch (ResidentServiceException e) {
			assertEquals(ResidentErrorCode.INDIVIDUAL_ID_UIN_MISMATCH.getErrorCode(),
					((ValidationFailedException) e.getCause()).getErrorCode());
		}
	}

	@Test
	public void testValidationOfAuthIndividualIdWithVIDSuccess() throws ResidentServiceCheckedException,
			OtpValidationFailedException, ApisResourceAccessException, FileNotFoundException, IOException {
		Mockito.when(utilities.getUinByVid(anyString())).thenReturn("3527812406");
		dto.setIndividualIdType(IdType.VID.name());
		dto.setIndividualId("4447812406");
		residentServiceImpl.reqUinUpdate(dto);
	}

	@Test
	public void testValidationOfAuthIndividualIdWithVIDFailure() throws ResidentServiceCheckedException,
			OtpValidationFailedException, ApisResourceAccessException, FileNotFoundException, IOException {

		Mockito.when(utilities.getUinByVid(anyString())).thenReturn("3527812407");
		dto.setIndividualIdType(IdType.VID.name());
		dto.setIndividualId("4447812406");
		try {
			residentServiceImpl.reqUinUpdate(dto);
		} catch (ResidentServiceException e) {
			e.printStackTrace();
			assertEquals(ResidentErrorCode.INDIVIDUAL_ID_UIN_MISMATCH.getErrorCode(),
					((ValidationFailedException) e.getCause()).getErrorCode());
		}
	}

	@Test(expected = ResidentServiceException.class)
	public void reqUinUpdateGetMachineIdMachineServiceException() throws BaseCheckedException, IOException {
		IdentityServiceTest.getAuthUserDetailsFromAuthentication();
		String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuGXPqbFOIZhB_N_fbTXOMIsRgq_LMdL9DJ5kWYAneCj_LPw3OEm2ncLVIRyJsF2DcSQwvzt_Njdvg1Cr54nD1uHBu3Vt9G1sy3p6uwbeK1l5mJSMNe5oGe11fmehtsR2QcB_45_us_IiiiUzzHJrySexmDfdOiPdy-dID4DYRDAf-HXlMIEf4Di_8NV3wVrA3jq1tuNkXX3qKtM4NhZOihp0HmB9E7RHttSV9VJNh00BrC57qdMfa5xqsHok3qftU5SAan4BGuPklN2fzOVcsa-V-B8JbwxRfPdwMkq-jW7Eu1LcNhNVQYJGEWDLAQDGKY_fOB_YwBzn8xvYRjqSfQIDAQAB";

		List<MachineDto> machineDtos = new ArrayList<>();
		MachineDto machineDto = new MachineDto();
		machineDto.setMachineSpecId("1001");
		machineDto.setIsActive(false);
		machineDto.setId("10147");
		machineDto.setName("resident_machine_1640777004542");
		machineDto.setValidityDateTime("2024-12-29T11:23:24.541Z");
		machineDto.setSignPublicKey("");
		machineDtos.add(machineDto);
		MachineSearchResponseDTO.MachineSearchDto response = MachineSearchResponseDTO.MachineSearchDto.builder()
				.fromRecord(0).toRecord(0).toRecord(0).data(machineDtos).build();
		MachineSearchResponseDTO machineSearchResponseDTO = new MachineSearchResponseDTO();
		machineSearchResponseDTO.setId("null");
		machineSearchResponseDTO.setVersion("1.0");
		machineSearchResponseDTO.setResponsetime("2022-01-28T06:25:23.958Z");
		machineSearchResponseDTO.setResponse(response);
		Mockito.when(residentServiceRestClient.postApi(eq("MACHINESEARCH"), any(MediaType.class), any(HttpEntity.class),
				eq(MachineSearchResponseDTO.class))).thenReturn(machineSearchResponseDTO);

		MachineCreateResponseDTO machineCreateResponseDTO = new MachineCreateResponseDTO();
		MachineDto newMachineDTO = new MachineDto();
		newMachineDTO.setMachineSpecId("1001");
		newMachineDTO.setIsActive(false);
		newMachineDTO.setId("10147");
		newMachineDTO.setName("resident_machine_1640777004542");
		newMachineDTO.setValidityDateTime("2024-12-29T11:23:24.541Z");
		newMachineDTO.setPublicKey(publicKey);
		newMachineDTO.setSignPublicKey(publicKey);
		machineCreateResponseDTO.setResponse(newMachineDTO);
		MachineErrorDTO machineErrorDTO = new MachineErrorDTO();
		machineErrorDTO.setErrorCode(ResidentErrorCode.MACHINE_MASTER_CREATE_EXCEPTION.getErrorCode());
		machineErrorDTO.setMessage(ResidentErrorCode.MACHINE_MASTER_CREATE_EXCEPTION.getErrorMessage());
		machineCreateResponseDTO.setErrors(List.of(machineErrorDTO));
		Mockito.when(env.getProperty(ApiName.MACHINECREATE.name())).thenReturn("MACHINECREATE");
		Mockito.when(residentServiceRestClient.postApi(eq("MACHINECREATE"), any(MediaType.class), any(HttpEntity.class),
				eq(MachineCreateResponseDTO.class))).thenReturn(machineCreateResponseDTO);
		Tuple2<Object, String> residentUpdateResponseDTO = residentServiceImpl.reqUinUpdate(dto, demographicIdentity, false, idRepoJson, schemaJson, idResponseDto);
		assertEquals(((ResidentUpdateResponseDTO) residentUpdateResponseDTO.getT1()).getRegistrationId(), updateDto.getRegistrationId());
		verify(residentServiceRestClient, atLeast(3)).postApi(any(), any(), any(), any(Class.class));
	}

	@Test(expected = ResidentServiceException.class)
	public void reqUinUpdateGetMachineIdEmptyResponseException() throws BaseCheckedException, IOException {
		IdentityServiceTest.getAuthUserDetailsFromAuthentication();
		String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuGXPqbFOIZhB_N_fbTXOMIsRgq_LMdL9DJ5kWYAneCj_LPw3OEm2ncLVIRyJsF2DcSQwvzt_Njdvg1Cr54nD1uHBu3Vt9G1sy3p6uwbeK1l5mJSMNe5oGe11fmehtsR2QcB_45_us_IiiiUzzHJrySexmDfdOiPdy-dID4DYRDAf-HXlMIEf4Di_8NV3wVrA3jq1tuNkXX3qKtM4NhZOihp0HmB9E7RHttSV9VJNh00BrC57qdMfa5xqsHok3qftU5SAan4BGuPklN2fzOVcsa-V-B8JbwxRfPdwMkq-jW7Eu1LcNhNVQYJGEWDLAQDGKY_fOB_YwBzn8xvYRjqSfQIDAQAB";

		List<MachineDto> machineDtos = new ArrayList<>();
		MachineDto machineDto = new MachineDto();
		machineDto.setMachineSpecId("1001");
		machineDto.setIsActive(false);
		machineDto.setId("10147");
		machineDto.setName("resident_machine_1640777004542");
		machineDto.setValidityDateTime("2024-12-29T11:23:24.541Z");
		machineDto.setSignPublicKey("");
		machineDtos.add(machineDto);
		MachineSearchResponseDTO.MachineSearchDto response = MachineSearchResponseDTO.MachineSearchDto.builder()
				.fromRecord(0).toRecord(0).toRecord(0).data(machineDtos).build();
		MachineSearchResponseDTO machineSearchResponseDTO = new MachineSearchResponseDTO();
		machineSearchResponseDTO.setId("null");
		machineSearchResponseDTO.setVersion("1.0");
		machineSearchResponseDTO.setResponsetime("2022-01-28T06:25:23.958Z");
		machineSearchResponseDTO.setResponse(response);
		Mockito.when(residentServiceRestClient.postApi(eq("MACHINESEARCH"), any(MediaType.class), any(HttpEntity.class),
				eq(MachineSearchResponseDTO.class))).thenReturn(machineSearchResponseDTO);

		MachineCreateResponseDTO machineCreateResponseDTO = new MachineCreateResponseDTO();
		machineCreateResponseDTO.setResponse(null);
		Mockito.when(env.getProperty(ApiName.MACHINECREATE.name())).thenReturn("MACHINECREATE");
		Mockito.when(residentServiceRestClient.postApi(eq("MACHINECREATE"), any(MediaType.class), any(HttpEntity.class),
				eq(MachineCreateResponseDTO.class))).thenReturn(machineCreateResponseDTO);
		Tuple2<Object, String> residentUpdateResponseDTO = residentServiceImpl.reqUinUpdate(dto, demographicIdentity, false, idRepoJson, schemaJson, idResponseDto);
		assertEquals(((ResidentUpdateResponseDTO) residentUpdateResponseDTO.getT1()).getRegistrationId(), updateDto.getRegistrationId());
		verify(residentServiceRestClient, atLeast(3)).postApi(any(), any(), any(), any(Class.class));
	}

	@Test
	public void testReqUinUpdate() throws ResidentServiceCheckedException {
		Map identityMap = new LinkedHashMap();
		identityMap.put("IDSchemaVersion", "0.1");
		identityMap.put("UIN", "3527812406");
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("identity", identityMap);
		jsonObject.put("UIN", "3527812406");
		residentServiceImpl.reqUinUpdate(dto, jsonObject, true, idRepoJson, schemaJson, idResponseDto);
	}

	@Test(expected = ResidentServiceException.class)
	public void testReqUinUpdateFailed() throws ResidentServiceCheckedException, IdObjectIOException, InvalidIdSchemaException, IdObjectValidationFailedException {
		Map identityMap = new LinkedHashMap();
		identityMap.put("IDSchemaVersion", "0.1");
		identityMap.put("UIN", "3527812406");
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("identity", identityMap);
		jsonObject.put("UIN", "3527812406");
		Mockito.when(idObjectValidator.validateIdObject(any(), any())).thenThrow(new IdObjectValidationFailedException(
				ResidentErrorCode.INVALID_INPUT.getErrorCode(), ResidentConstants.INVALID_INPUT_PARAMETER));
		residentServiceImpl.reqUinUpdate(dto, jsonObject, true, idRepoJson, schemaJson, idResponseDto);
	}

	@Test(expected = ResidentServiceException.class)
	public void testReqUinUpdateBadIdentityJson() throws ResidentServiceCheckedException {
		dto.setIdentityJson("abc");
		residentServiceImpl.reqUinUpdate(dto);
	}

	@Test
	public void testValidationOfFailedStatusForUpdateUin() throws ResidentServiceCheckedException {
		ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
		residentTransactionEntity.setEventId(UUID.randomUUID().toString());
		residentTransactionEntity.setRequestSummary("failed");
		when(utility.createEntity(Mockito.any())).thenReturn(residentTransactionEntity);
		IdentityServiceTest.getAuthUserDetailsFromAuthentication();
		dto.setIndividualId("3527812407");
		try {
			residentServiceImpl.reqUinUpdate(dto, demographicIdentity, false, idRepoJson, schemaJson, idResponseDto);
		} catch (ResidentServiceException e) {
			assertEquals(ResidentErrorCode.INDIVIDUAL_ID_UIN_MISMATCH.getErrorCode(),
					((ValidationFailedException) e.getCause()).getErrorCode());
		}
	}

}
