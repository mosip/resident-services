package io.mosip.resident.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.pdfgenerator.spi.PDFGenerator;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.dto.IdRepoResponseDto;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.IdRepoAppException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.impl.IdentityServiceImpl;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PrepareForTest({ JsonUtil.class })
public class UtilityTest {
	private static final String LOCALE = "en-US";

	@Mock
	private ResidentServiceRestClient residentServiceRestClient;

	@InjectMocks
	private Utility utility;

	private JSONObject identity;
	
	private JSONObject amrAcrJson;

	@Mock
	private Environment env;

	@Mock
	private IdentityServiceImpl identityService;
	
	@Mock
	private HttpServletRequest request;

	@Mock
	private PDFGenerator pdfGenerator;

	@Mock
	private ResidentTransactionRepository residentTransactionRepository;

	@Mock
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate residentRestTemplate;
	
	@Mock
	private ObjectMapper objectMapper;
	
	private ObjectMapper mapper = new ObjectMapper();

	private String replaceSplChars = "{\" \": \"_\", \",\" : \"\", \":\" : \".\"}";

	@Before
	public void setUp() throws IOException, ApisResourceAccessException {
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("ID.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String idJsonString = IOUtils.toString(is, "UTF-8");
		identity = JsonUtil.readValue(idJsonString, JSONObject.class);
		
		File amrAcrJsonFile = new File(classLoader.getResource("amr-acr-mapping.json").getFile());
		InputStream insputStream = new FileInputStream(amrAcrJsonFile);
		String amrAcrJsonString = IOUtils.toString(insputStream, "UTF-8");
		amrAcrJson = JsonUtil.readValue(amrAcrJsonString, JSONObject.class);
		
		ReflectionTestUtils.setField(utility, "configServerFileStorageURL", "url");
		ReflectionTestUtils.setField(utility, "residentIdentityJson", "json");
		ReflectionTestUtils.setField(utility, "amrAcrJsonFile", "amr-acr-mapping.json");
		ReflectionTestUtils.setField(utility, "formattingStyle", FormatStyle.MEDIUM.name());
		ReflectionTestUtils.setField(utility, "specialCharsReplacementMap", mapper.readValue(replaceSplChars, Map.class));
        when(env.getProperty("resident.ui.datetime.pattern.default")).thenReturn("yyyy-MM-dd");
        when(env.getProperty("resident.filename.datetime.pattern.default")).thenReturn("yyyy-MM-dd hh:mm:ss a");
		request = Mockito.mock(HttpServletRequest.class);
	}

	@Test
	public void retrieveIdrepoJsonSuccessTest() throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<IdRepoResponseDto> response = new ResponseWrapper<>();
		IdRepoResponseDto idRepoResponseDto = new IdRepoResponseDto();
		idRepoResponseDto.setStatus("Activated");
		idRepoResponseDto.setIdentity(JsonUtil.getJSONObject(identity, "identity"));
		response.setResponse(idRepoResponseDto);
		Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
				any(), any(Class.class))).thenReturn(response);
		// UIN
		JSONObject identityJsonObj = utility.retrieveIdrepoJson("3527812406");
		assertEquals(identityJsonObj.get("UIN"), JsonUtil.getJSONObject(identity, "identity").get("UIN"));
		// RID
		JSONObject jsonUsingRID = utility.retrieveIdrepoJson("10008200070004420191203104356");
		assertEquals(jsonUsingRID.get("UIN"), JsonUtil.getJSONObject(identity, "identity").get("UIN"));

	}

	@Test
	public void testRetrieveVidSuccess() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResponseWrapper<IdRepoResponseDto> response = new ResponseWrapper<>();
		IdRepoResponseDto idRepoResponseDto = new IdRepoResponseDto();
		idRepoResponseDto.setStatus("Activated");
		idRepoResponseDto.setIdentity(JsonUtil.getJSONObject(identity, "identity"));
		response.setResponse(idRepoResponseDto);

		Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
				any(), any(Class.class))).thenReturn(response);
		JSONObject jsonUsingVID = utility.retrieveIdrepoJson("5628965106742572");
		assertEquals(jsonUsingVID.get("UIN"), JsonUtil.getJSONObject(identity, "identity").get("UIN"));
	}

	@Test(expected = IdRepoAppException.class)
	public void testRetrieveIdrepoJsonError() throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResponseWrapper<IdRepoResponseDto> response = new ResponseWrapper<>();
		response.setErrors(List.of(new ServiceError("error code", "error msg")));

		Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
				any(), any(Class.class))).thenReturn(response);
		utility.retrieveIdrepoJson("5628965106742572");
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void retrieveIdrepoJsonClientError() throws ApisResourceAccessException, ResidentServiceCheckedException {
		HttpClientErrorException clientExp = new HttpClientErrorException(HttpStatus.BAD_GATEWAY);
		ApisResourceAccessException apiResourceAccessExp = new ApisResourceAccessException("BadGateway", clientExp);
        Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
                any(), any(Class.class))).thenThrow(apiResourceAccessExp);
		utility.retrieveIdrepoJson("3527812406");

	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void retrieveIdrepoJsonServerError() throws ApisResourceAccessException, ResidentServiceCheckedException {
		HttpServerErrorException serverExp = new HttpServerErrorException(HttpStatus.BAD_GATEWAY);
		ApisResourceAccessException apiResourceAccessExp = new ApisResourceAccessException("BadGateway", serverExp);
        Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
                any(), any(Class.class))).thenThrow(apiResourceAccessExp);
		utility.retrieveIdrepoJson("3527812406");

	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void retrieveIdrepoJsonUnknownException()
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		ApisResourceAccessException apiResourceAccessExp = new ApisResourceAccessException("BadGateway",
				new RuntimeException());
        Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
                any(), any(Class.class))).thenThrow(apiResourceAccessExp);
		utility.retrieveIdrepoJson("3527812406");

	}

	@Test(expected = IdRepoAppException.class)
	public void testIdRepoAppException() throws ApisResourceAccessException, ResidentServiceCheckedException {
        Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
                any(), any(Class.class))).thenReturn(null);
		utility.retrieveIdrepoJson("3527812406");

	}

	@Test(expected = IdRepoAppException.class)
	public void vidResponseNull() throws ApisResourceAccessException, ResidentServiceCheckedException {
		List<String> pathsegments = new ArrayList<>();
		pathsegments.add("5628965106742572");
        Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
                any(), any(Class.class))).thenReturn(null);
		utility.retrieveIdrepoJson("5628965106742572");

	}

	@Test
	public void testGetMailingAttributes() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		Utility utilitySpy = Mockito.spy(utility);
		Mockito.doReturn(mappingJson).when(utilitySpy).getMappingJson();

		ResponseWrapper<IdRepoResponseDto> response = new ResponseWrapper<>();
		IdRepoResponseDto idRepoResponseDto = new IdRepoResponseDto();
		idRepoResponseDto.setStatus("Activated");
		idRepoResponseDto.setIdentity(JsonUtil.getJSONObject(identity, "identity"));
		response.setResponse(idRepoResponseDto);
		Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
				any(), any(Class.class))).thenReturn(response);

		Map<String, Object> attributes = utilitySpy.getMailingAttributes("3527812406", new HashSet<String>());
		assertEquals("user@mail.com", attributes.get("email"));
		Map<String, Object> attributes1 = utilitySpy.getMailingAttributes("3527812406", new HashSet<String>());
		assertEquals("user@mail.com", attributes1.get("email"));

	}

	@Test(expected = ResidentServiceException.class)
	public void testGetMailingAttributesIdNull() throws Exception {
		utility.getMailingAttributes(null, new HashSet<String>());
	}
	
	@Test(expected = ResidentServiceException.class)
	public void testGetMailingAttributesIdEmpty() throws Exception {
		utility.getMailingAttributes("", new HashSet<String>());
	}

	@Test
	public void testGetMappingJsonEmpty() throws Exception {
		ReflectionTestUtils.setField(utility, "regProcessorIdentityJson", "");
		utility.getMappingJson();
	}

	@Test
	public void testGetPreferredLanguage() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		Utility utilitySpy = Mockito.spy(utility);
		Mockito.doReturn(mappingJson).when(utilitySpy).getMappingJson();

		ResponseWrapper<IdRepoResponseDto> response = new ResponseWrapper<>();
		IdRepoResponseDto idRepoResponseDto = new IdRepoResponseDto();
		idRepoResponseDto.setStatus("Activated");
		idRepoResponseDto.setIdentity(JsonUtil.getJSONObject(identity, "identity"));
		response.setResponse(idRepoResponseDto);
		Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
				any(), any(Class.class))).thenReturn(response);

		Mockito.doReturn("preferredLang").when(env).getProperty("mosip.default.user-preferred-language-attribute");
		Map<String, Object> attributes = utilitySpy.getMailingAttributes("3527812406", new HashSet<String>());
		assertEquals("eng", attributes.get("preferredLang"));
	}

	@Test
	public void testGetDefaultTemplateLanguages() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		Utility utilitySpy = Mockito.spy(utility);
		Mockito.doReturn(mappingJson).when(utilitySpy).getMappingJson();

		ResponseWrapper<IdRepoResponseDto> response = new ResponseWrapper<>();
		IdRepoResponseDto idRepoResponseDto = new IdRepoResponseDto();
		idRepoResponseDto.setStatus("Activated");
		idRepoResponseDto.setIdentity(JsonUtil.getJSONObject(identity, "identity"));
		response.setResponse(idRepoResponseDto);
		Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
				any(), any(Class.class))).thenReturn(response);

		Mockito.doReturn("preferredLang").when(env).getProperty("mosip.default.template-languages");
		Map<String, Object> attributes = utilitySpy.getMailingAttributes("3527812406", new HashSet<String>());
		assertEquals("eng", attributes.get("preferredLang"));
	}

	@Test
	public void testGetDataCapturedLanguages() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		Utility utilitySpy = Mockito.spy(utility);
		Mockito.doReturn(mappingJson).when(utilitySpy).getMappingJson();

		ResponseWrapper<IdRepoResponseDto> response = new ResponseWrapper<>();
		IdRepoResponseDto idRepoResponseDto = new IdRepoResponseDto();
		idRepoResponseDto.setStatus("Activated");
		idRepoResponseDto.setIdentity(JsonUtil.getJSONObject(identity, "identity"));
		response.setResponse(idRepoResponseDto);
		Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
				any(), any(Class.class))).thenReturn(response);

		Mockito.doReturn(null).when(env).getProperty("mosip.default.template-languages");
		Map<String, Object> attributes = utilitySpy.getMailingAttributes("3527812406", new HashSet<String>());
		assertEquals("eng", attributes.get("preferredLang"));
	}

	@Test
	public void testGetMappingJson() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		ReflectionTestUtils.setField(utility, "regProcessorIdentityJson", mappingJson);

		ResponseWrapper<IdRepoResponseDto> response = new ResponseWrapper<>();
		IdRepoResponseDto idRepoResponseDto = new IdRepoResponseDto();
		idRepoResponseDto.setStatus("Activated");
		idRepoResponseDto.setIdentity(JsonUtil.getJSONObject(identity, "identity"));
		response.setResponse(idRepoResponseDto);
		Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
				any(), any(Class.class))).thenReturn(response);

		Map<String, Object> attributes = utility.getMailingAttributes("3527812406", new HashSet<String>());
		assertEquals("eng", attributes.get("preferredLang"));
		verify(residentRestTemplate, never()).getForObject(anyString(), any(Class.class));
	}

	@Test(expected = ResidentServiceException.class)
	public void testGetMailingAttributesJSONParsingException() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = "";
		Utility utilitySpy = Mockito.spy(utility);
		Mockito.doReturn(mappingJson).when(utilitySpy).getMappingJson();
		Map<String, Object> attributes = utilitySpy.getMailingAttributes("3527812406", new HashSet<String>());
		assertEquals("user@mail.com", attributes.get("email"));

		ReflectionTestUtils.setField(utilitySpy, "languageType", "NA");
		Map<String, Object> attributes1 = utilitySpy.getMailingAttributes("3527812406", new HashSet<String>());
		assertEquals("user@mail.com", attributes1.get("email"));

	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetMailingAttributesIOException() throws IOException, ResidentServiceCheckedException {
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		Utility utilitySpy = Mockito.spy(utility);
		Mockito.doReturn(mappingJson).when(utilitySpy).getMappingJson();
		Mockito.doReturn(JsonUtil.getJSONObject(identity, "identity")).when(utilitySpy)
				.retrieveIdrepoJson(Mockito.anyString());
		PowerMockito.mockStatic(JsonUtil.class);
		PowerMockito.when(JsonUtil.readValue(mappingJson, JSONObject.class)).thenThrow(new IOException());
		utilitySpy.getMailingAttributes("3527812406", new HashSet<String>());

	}

	@Test
	public void testGetFileNameAsPerFeatureNameShareCredWithPartner(){
		assertEquals("SHARE_CRED_WITH_PARTNER", utility.getFileName("123", "SHARE_CRED_WITH_PARTNER", 0, LOCALE));
		assertEquals("GENERATE_VID", utility.getFileName("123", "GENERATE_VID", 0, LOCALE));
		assertEquals("REVOKE_VID", utility.getFileName("123", "REVOKE_VID", 0, LOCALE));
		assertEquals("ORDER_PHYSICAL_CARD", utility.getFileName("123", "ORDER_PHYSICAL_CARD", 0, LOCALE));
		assertEquals("DOWNLOAD_PERSONALIZED_CARD", utility.getFileName("123", "DOWNLOAD_PERSONALIZED_CARD", 0, LOCALE));
		assertEquals("UPDATE_MY_UIN", utility.getFileName("123", "UPDATE_MY_UIN", 0, LOCALE));
		assertEquals("AUTH_TYPE_LOCK_UNLOCK", utility.getFileName("123", "AUTH_TYPE_LOCK_UNLOCK", 0, LOCALE));
		assertEquals("Generic", utility.getFileName("123", "Generic", 0, LOCALE));
	}

	@Test
	public void testGetFileNameAsPerFeatureNameGenerateVid(){
		Mockito.when(env.getProperty(ResidentConstants.ACK_MANAGE_MY_VID_NAMING_CONVENTION_PROPERTY))
				.thenReturn("Ack_Manage_my_VID_{eventId}_{timestamp}.pdf");
		Mockito.when(env.getProperty("resident.datetime.pattern"))
				.thenReturn("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		assertNotNull(utility.getFileName("123", "Ack_Manage_my_VID_{eventId}_{timestamp}.pdf", 0, LOCALE));
	}

	@Test
	public void testGetFileNameNullEventId(){
		Mockito.when(env.getProperty(ResidentConstants.ACK_MANAGE_MY_VID_NAMING_CONVENTION_PROPERTY))
				.thenReturn("Ack_Manage_my_VID_{eventId}_{timestamp}.pdf");
		Mockito.when(env.getProperty("resident.datetime.pattern"))
				.thenReturn("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		assertNotNull(utility.getFileName(null, "Ack_Manage_my_VID_{eventId}_{timestamp}.pdf", 0, LOCALE));
	}

	@Test
	public void testGetIdForResidentTransactionEmail() throws ResidentServiceCheckedException, NoSuchAlgorithmException {
		IdentityDTO identityDTO = new IdentityDTO();
		identityDTO.setUIN("2186705746");
		identityDTO.setEmail("kameshprasad1338@gmail.com");
		identityDTO.setPhone("8809989898");
		Mockito.when(identityService.getIdentity(Mockito.anyString())).thenReturn(identityDTO);
		Mockito.when(identityService.getIDAToken(Mockito.anyString())).thenReturn("2186705746");
		assertEquals(HMACUtils2.digestAsPlainText(("kameshprasad1338@gmail.com"+"2186705746").getBytes()),
				utility.getIdForResidentTransaction("2186705746", List.of("EMAIL")));
	}

	@Test
	public void testGetIdForResidentTransactionPhone() throws ResidentServiceCheckedException, NoSuchAlgorithmException {
		IdentityDTO identityDTO = new IdentityDTO();
		identityDTO.setUIN("2186705746");
		identityDTO.setEmail("kameshprasad1338@gmail.com");
		identityDTO.setPhone("8809989898");
		Mockito.when(identityService.getIdentity(Mockito.anyString())).thenReturn(identityDTO);
		Mockito.when(identityService.getIDAToken(Mockito.anyString())).thenReturn("2186705746");
		assertEquals(HMACUtils2.digestAsPlainText(("8809989898"+"2186705746").getBytes()),
				utility.getIdForResidentTransaction("2186705746", List.of("PHONE")));
	}

	@Test
	public void testGetIdForResidentTransactionPhoneEmail() throws ResidentServiceCheckedException, NoSuchAlgorithmException {
		IdentityDTO identityDTO = new IdentityDTO();
		identityDTO.setUIN("2186705746");
		identityDTO.setEmail("kameshprasad1338@gmail.com");
		identityDTO.setPhone("8809989898");
		Mockito.when(identityService.getIdentity(Mockito.anyString())).thenReturn(identityDTO);
		Mockito.when(identityService.getIDAToken(Mockito.anyString())).thenReturn("2186705746");
		assertEquals(HMACUtils2.digestAsPlainText(("kameshprasad1338@gmail.com"+"8809989898"+"2186705746").getBytes()),
				utility.getIdForResidentTransaction("2186705746", List.of("PHONE","EMAIL")));
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetIdForResidentTransactionPhoneEmailFailure() throws ResidentServiceCheckedException, NoSuchAlgorithmException {
		IdentityDTO identityDTO = new IdentityDTO();
		identityDTO.setUIN("2186705746");
		identityDTO.setEmail("kameshprasad1338@gmail.com");
		identityDTO.setPhone("8809989898");
		Mockito.when(identityService.getIdentity(Mockito.anyString())).thenReturn(identityDTO);
		Mockito.when(identityService.getIDAToken(Mockito.anyString())).thenReturn("2186705746");
		assertEquals(HMACUtils2.digestAsPlainText(("kameshprasad1338@gmail.com"+"8809989898"+"2186705746").getBytes()),
				utility.getIdForResidentTransaction("2186705746", List.of("PH")));
	}

	@Test
	public void testSignPdf() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] array = "pdf".getBytes();
		out.write(array);
		Mockito.when(pdfGenerator.generate((InputStream) any())).thenReturn(out);
		utility.signPdf(new ByteArrayInputStream("pdf".getBytes()), null);
	}

	@Test
	public void testCreateDownloadLinkFailure(){
		assertEquals("NA", utility.createDownloadCardLinkFromEventId(new ResidentTransactionEntity()));
	}

	@Test
	public void testCreateDownloadLinkSuccess(){
		ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
		ReflectionTestUtils.setField(utility, "downloadCardUrl", "http://mosip/event/{eventId}");
		residentTransactionEntity.setReferenceLink("http://mosip");
		residentTransactionEntity.setEventId("123455678");
		assertEquals("http://mosip/event/123455678", utility.createDownloadCardLinkFromEventId(residentTransactionEntity));
	}

	@Test
	public void testCreateTrackServiceRequestLink(){
		ReflectionTestUtils.setField(utility, "trackServiceUrl", "http://mosip");
		assertEquals(("http://mosip"+"2186705746111111"), utility.createTrackServiceRequestLink("2186705746111111"));
	}

	@Test
	public void testCreateEventId(){
		ReflectionTestUtils.setField(utility, "trackServiceUrl", "http://mosip");
		assertEquals(16,utility.createEventId().length());
	}

	@Test
	public void testCreateEntity(){
		assertEquals("Unknown",utility.createEntity(RequestType.SHARE_CRED_WITH_PARTNER).getCrBy());
	}

	@Test
	public void testGetFileNameAsPerFeatureName(){
		Mockito.when(env.getProperty(Mockito.anyString()))
				.thenReturn("AckFileName");
		assertEquals("AckFileName", utility.getFileNameAsPerFeatureName("123", "SHARE_CRED_WITH_PARTNER", 0, LOCALE));
		assertEquals("AckFileName", utility.getFileNameAsPerFeatureName("123", "GENERATE_VID", 0, LOCALE));
		assertEquals("AckFileName", utility.getFileNameAsPerFeatureName("123", "REVOKE_VID", 0, LOCALE));
		assertEquals("AckFileName", utility.getFileNameAsPerFeatureName("123", "ORDER_PHYSICAL_CARD", 0, LOCALE));
		assertEquals("AckFileName", utility.getFileNameAsPerFeatureName("123", "DOWNLOAD_PERSONALIZED_CARD", 0, LOCALE));
		assertEquals("AckFileName", utility.getFileNameAsPerFeatureName("123", "UPDATE_MY_UIN", 0, LOCALE));
		assertEquals("AckFileName", utility.getFileNameAsPerFeatureName("123", "AUTH_TYPE_LOCK_UNLOCK", 0, LOCALE));
		assertEquals("AckFileName", utility.getFileNameAsPerFeatureName("123", "Generic", 0, LOCALE));
	}

	@Test
	public void testGetClientIp() {
		Mockito.when(request.getHeader(Mockito.anyString())).thenReturn("1.2.3,1.3");
		String ipAddress = utility.getClientIp(request);
		assertEquals("1.2.3", ipAddress);
	}

	@Test
	public void testGetClientIpEmpty() {
		Mockito.when(request.getHeader(Mockito.anyString())).thenReturn("");
		Mockito.when(request.getRemoteAddr()).thenReturn("1.1.5");
		String ipAddress = utility.getClientIp(request);
		assertEquals("1.1.5", ipAddress);
	}

	@Test
	public void testGetClientIpNull() {
		Mockito.when(request.getHeader(Mockito.anyString())).thenReturn(null);
		Mockito.when(request.getRemoteAddr()).thenReturn("1.5.5");
		String ipAddress = utility.getClientIp(request);
		assertEquals("1.5.5", ipAddress);
	}
	
	@Test
	public void test_formatWithOffsetForFileName_en_US() {
		LocalDateTime localDateTime = LocalDateTime.of(1993, 8, 14, 16, 54);
		String formatWithOffsetForFileName = utility.formatWithOffsetForFileName(0, "en-US", localDateTime);
		assertEquals("Aug_14_1993_4.54.00_PM", formatWithOffsetForFileName);
	}
	
	@Test
	public void test_formatWithOffsetForFileName_en_IN() {
		LocalDateTime localDateTime = LocalDateTime.of(1993, 8, 14, 16, 54);
		String formatWithOffsetForFileName = utility.formatWithOffsetForFileName(-330, "en-IN", localDateTime);
		assertEquals("14-Aug-1993_10.24.00_PM", formatWithOffsetForFileName);
	}
	
	@Test
	public void test_formatWithOffsetForFileName_null_locale() {
		LocalDateTime localDateTime = LocalDateTime.of(1993, 8, 14, 16, 54);
		String formatWithOffsetForFileName = utility.formatWithOffsetForFileName(0, null, localDateTime);
		assertEquals("1993-08-14_04.54.00_PM", formatWithOffsetForFileName);
	}
	

	
	@Test
	public void test_formatWithOffsetForUI_en_US() {
		LocalDateTime localDateTime = LocalDateTime.of(1993, 8, 14, 16, 54);
		String formatWithOffsetForFileName = utility.formatWithOffsetForUI(0, "en-US", localDateTime);
		assertEquals("Aug 14, 1993, 4:54:00 PM", formatWithOffsetForFileName);
	}
	
	@Test
	public void test_formatWithOffsetForUI_en_IN() {
		LocalDateTime localDateTime = LocalDateTime.of(1993, 8, 14, 16, 54);
		String formatWithOffsetForFileName = utility.formatWithOffsetForUI(-330, "en-IN", localDateTime);
		assertEquals("14-Aug-1993, 10:24:00 PM", formatWithOffsetForFileName);
	}
	
	@Test
	public void test_formatWithOffsetForUI_null_locale() {
		LocalDateTime localDateTime = LocalDateTime.of(1993, 8, 14, 16, 54);
		String formatWithOffsetForFileName = utility.formatWithOffsetForUI(0, null, localDateTime);
		assertEquals("1993-08-14", formatWithOffsetForFileName);
	}
	
}
