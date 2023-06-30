package io.mosip.resident.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.pdfgenerator.spi.PDFGenerator;
import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.kernel.signature.dto.SignatureResponseDto;
import io.mosip.kernel.templatemanager.velocity.builder.TemplateManagerBuilderImpl;
import io.mosip.resident.dto.RegistrationCenterDto;
import io.mosip.resident.dto.RegistrationCenterInfoResponseDto;
import io.mosip.resident.dto.WorkingDaysDto;
import io.mosip.resident.dto.WorkingDaysResponseDto;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.util.Utility;

/**
 * This class is used to create service class test for download master data
 * service impl.
 * 
 * @Author Kamesh Shekhar Prasad
 * @Author Ritik Jain
 */
@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class DownloadmasterDataServiceImplTest {

	@InjectMocks
	private DownLoadMasterDataServiceImpl downLoadMasterDataService = new DownLoadMasterDataServiceImpl();

	@Mock
	private ResidentTransactionRepository residentTransactionRepository;

	@Mock
	private TemplateUtil templateUtil;

	@Mock
	private ProxyMasterdataService proxyMasterdataService;

	@InjectMocks
	private TemplateManagerBuilderImpl templateManagerBuilder;

	@Mock
	private PDFGenerator pdfGenerator;

	@Mock
	private Environment environment;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private ResidentServiceRestClient residentServiceRestClient;

	@Mock
	private Utility utility;

	private byte[] result;
	private String eventId;
	private String languageCode;
	private Optional<ResidentTransactionEntity> residentTransactionEntity;
	private Map<String, String> templateVariables;

	@Mock
	private TemplateManager templateManager;
	private static final String CLASSPATH = "classpath";
	private static final String ENCODE_TYPE = "UTF-8";
	private Map<String, Object> values;

	private String langCode;
	private Short hierarchyLevel;
	private String name;

	@Before
	public void setup() throws Exception {
		templateVariables = new LinkedHashMap<>();
		values = new LinkedHashMap<>();
		values.put("test", String.class);
		templateVariables.put("eventId", eventId);
		result = "test".getBytes(StandardCharsets.UTF_8);
		eventId = "bf42d76e-b02e-48c8-a17a-6bb842d85ea9";
		languageCode = "eng";

		Mockito.when(
				templateUtil.getTemplateValueFromTemplateTypeCodeAndLangCode(Mockito.anyString(), Mockito.anyString()))
				.thenReturn("file text template");
		ReflectionTestUtils.setField(downLoadMasterDataService, "templateManagerBuilder", templateManagerBuilder);
		templateManagerBuilder.encodingType(ENCODE_TYPE).enableCache(false).resourceLoader(CLASSPATH).build();
		InputStream stream = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));
		Mockito.when(templateManager.merge(any(), Mockito.anyMap())).thenReturn(stream);
		OutputStream outputStream = new ByteArrayOutputStream(1024);
		outputStream.write("test".getBytes(StandardCharsets.UTF_8));
		SignatureResponseDto signatureResponseDto = new SignatureResponseDto();
		signatureResponseDto.setData("data");
		ResponseWrapper<SignatureResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(signatureResponseDto);
		Mockito.when(utility.signPdf(Mockito.any(), Mockito.any())).thenReturn("data".getBytes());
		Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("supporting-docs-list");
		langCode = "eng";
		hierarchyLevel = 4;
		name = "name1";
	}

	@Test
	public void testDownloadRegistrationCentersByHierarchyLevel() throws Exception {
		ReflectionTestUtils.setField(downLoadMasterDataService, "maxRegistrationCenterPageSize", 10);
		ResponseWrapper regCentResponseWrapper = new ResponseWrapper();
		Map<String, Object> regCenterMap = new HashMap();
		regCenterMap.put("id", 21006);
		regCenterMap.put("name", "Banglore Center Mehdia");
		regCentResponseWrapper.setResponse(Map.of("data", List.of(regCenterMap)));
		Mockito.when(proxyMasterdataService.getRegistrationCenterByHierarchyLevelAndTextPaginated(Mockito.anyString(),
				Mockito.anyShort(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.any(),
				Mockito.nullable(String.class))).thenReturn(regCentResponseWrapper);

		RegistrationCenterInfoResponseDto registrationCentersDtls = new RegistrationCenterInfoResponseDto();
		RegistrationCenterDto registrationCenterDto = getRegCenterData();
		registrationCentersDtls.setData(List.of(registrationCenterDto));
		when(objectMapper.writeValueAsString(Mockito.any())).thenReturn("registration centers data");
		when(objectMapper.readValue(anyString(), eq(RegistrationCenterInfoResponseDto.class)))
				.thenReturn(registrationCentersDtls);

		getRegCenterWorkingDaysData();

		InputStream actualResult = downLoadMasterDataService.downloadRegistrationCentersByHierarchyLevel(langCode,
				hierarchyLevel, name);
		assertNotNull(actualResult);
	}

	@Test(expected = ResidentServiceException.class)
	public void testDownloadRegistrationCentersByHierarchyLevelWithException() throws Exception {
		ReflectionTestUtils.setField(downLoadMasterDataService, "maxRegistrationCenterPageSize", 10);
		Mockito.when(proxyMasterdataService.getRegistrationCenterByHierarchyLevelAndTextPaginated(Mockito.anyString(),
				Mockito.anyShort(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.any(),
				Mockito.nullable(String.class))).thenReturn(new ResponseWrapper());

		RegistrationCenterInfoResponseDto registrationCentersDtls = new RegistrationCenterInfoResponseDto();
		registrationCentersDtls.setData(List.of(new RegistrationCenterDto()));
		when(objectMapper.writeValueAsString(Mockito.any())).thenReturn("registration centers data");
		when(objectMapper.readValue(anyString(), eq(RegistrationCenterInfoResponseDto.class)))
				.thenReturn(registrationCentersDtls);

		downLoadMasterDataService.downloadRegistrationCentersByHierarchyLevel(langCode, hierarchyLevel, name);
	}

	@Test
	public void testDownloadRegistrationCentersByHierarchyLevelEmptyRegCenterList() throws Exception {
		ReflectionTestUtils.setField(downLoadMasterDataService, "maxRegistrationCenterPageSize", 10);
		Mockito.when(proxyMasterdataService.getRegistrationCenterByHierarchyLevelAndTextPaginated(Mockito.anyString(),
				Mockito.anyShort(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.any(),
				Mockito.nullable(String.class))).thenReturn(new ResponseWrapper());

		RegistrationCenterInfoResponseDto registrationCentersDtls = new RegistrationCenterInfoResponseDto();
		registrationCentersDtls.setData(List.of());
		registrationCentersDtls.setRegistrationCenters(List.of());
		when(objectMapper.writeValueAsString(Mockito.any())).thenReturn("registration centers data");
		when(objectMapper.readValue(anyString(), eq(RegistrationCenterInfoResponseDto.class)))
				.thenReturn(registrationCentersDtls);

		downLoadMasterDataService.downloadRegistrationCentersByHierarchyLevel(langCode, hierarchyLevel, name);
	}

	@Test
	public void testDownloadRegistrationCentersByHierarchyLevelWithRegCenter() throws Exception {
		ReflectionTestUtils.setField(downLoadMasterDataService, "maxRegistrationCenterPageSize", 10);
		Mockito.when(proxyMasterdataService.getRegistrationCenterByHierarchyLevelAndTextPaginated(Mockito.anyString(),
				Mockito.anyShort(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.any(),
				Mockito.nullable(String.class))).thenReturn(new ResponseWrapper());

		RegistrationCenterInfoResponseDto registrationCentersDtls = new RegistrationCenterInfoResponseDto();
		RegistrationCenterDto registrationCenterDto = getRegCenterData();
		registrationCentersDtls.setRegistrationCenters(List.of(registrationCenterDto));
		when(objectMapper.writeValueAsString(Mockito.any())).thenReturn("registration centers data");
		when(objectMapper.readValue(anyString(), eq(RegistrationCenterInfoResponseDto.class)))
				.thenReturn(registrationCentersDtls);

		getRegCenterWorkingDaysData();

		InputStream actualResult = downLoadMasterDataService.downloadRegistrationCentersByHierarchyLevel(langCode,
				hierarchyLevel, name);
		assertNotNull(actualResult);
	}

	private RegistrationCenterDto getRegCenterData() {
		RegistrationCenterDto registrationCenterDto = new RegistrationCenterDto();
		registrationCenterDto.setId("21006");
		registrationCenterDto.setName("Banglore Center Mehdia");
		registrationCenterDto.setCenterTypeCode("REG");
		registrationCenterDto.setLangCode("eng");
		registrationCenterDto.setAddressLine1("Mehdia Road Amria mehdia");
		registrationCenterDto.setAddressLine2("Kenitra");
		registrationCenterDto.setAddressLine3("Maroc");
		registrationCenterDto.setCenterStartTime("09:00:00");
		registrationCenterDto.setCenterEndTime("17:00:00");
		return registrationCenterDto;
	}

	private void getRegCenterWorkingDaysData()
			throws ResidentServiceCheckedException, JsonProcessingException, JsonMappingException {
		Mockito.when(proxyMasterdataService.getRegistrationCenterWorkingDays(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(new ResponseWrapper());
		WorkingDaysResponseDto workingDaysResponeDtls = new WorkingDaysResponseDto();
		WorkingDaysDto workingDaysDto1 = new WorkingDaysDto();
		workingDaysDto1.setCode("102");
		workingDaysDto1.setOrder(2);
		workingDaysDto1.setLanguage("eng");
		workingDaysDto1.setName("MON");
		WorkingDaysDto workingDaysDto2 = new WorkingDaysDto();
		workingDaysDto2.setCode("103");
		workingDaysDto2.setOrder(3);
		workingDaysDto2.setLanguage("eng");
		workingDaysDto2.setName("TUE");
		workingDaysResponeDtls.setWorkingdays(List.of(workingDaysDto1, workingDaysDto2));
		when(objectMapper.writeValueAsString(Mockito.any())).thenReturn("working days data");
		when(objectMapper.readValue(anyString(), eq(WorkingDaysResponseDto.class))).thenReturn(workingDaysResponeDtls);
	}

	@Test
	public void testGetNearestRegistrationcenters() throws Exception {
		byte[] actualResult = downLoadMasterDataService.getNearestRegistrationcenters(langCode, 4L, 4L, 3)
				.readAllBytes();
		assertNotNull(actualResult);
	}

	@Test
	public void testDownloadSupportingDocsByLanguage() throws Exception {
		byte[] actualResult = downLoadMasterDataService.downloadSupportingDocsByLanguage(langCode).readAllBytes();
		assertNotNull(actualResult);
	}

	@Test
	public void testgetTime() throws Exception {
		RegistrationCenterDto registrationCenterDto = new RegistrationCenterDto();
		registrationCenterDto.setCenterTypeCode("Ind");
		WorkingDaysResponseDto workingDaysResponseDto = new WorkingDaysResponseDto();
		WorkingDaysDto workingDaysDto = new WorkingDaysDto();
		workingDaysDto.setCode("123");
		workingDaysResponseDto.setWorkingdays(List.of(workingDaysDto));
		ResponseWrapper responseWrapper1 = new ResponseWrapper<>();
		responseWrapper1.setResponse(workingDaysResponseDto);
		ReflectionTestUtils.invokeMethod(downLoadMasterDataService, "getTime", String.valueOf(LocalTime.of(12, 2, 2)));

	}

	@Test
	public void testgetTimeFailed() throws Exception {
		RegistrationCenterDto registrationCenterDto = new RegistrationCenterDto();
		registrationCenterDto.setCenterTypeCode("Ind");
		WorkingDaysResponseDto workingDaysResponseDto = new WorkingDaysResponseDto();
		WorkingDaysDto workingDaysDto = new WorkingDaysDto();
		workingDaysDto.setCode("123");
		workingDaysResponseDto.setWorkingdays(List.of(workingDaysDto));
		ResponseWrapper responseWrapper1 = new ResponseWrapper<>();
		responseWrapper1.setResponse(workingDaysResponseDto);
		ReflectionTestUtils.invokeMethod(downLoadMasterDataService, "getTime", "123");

	}
}