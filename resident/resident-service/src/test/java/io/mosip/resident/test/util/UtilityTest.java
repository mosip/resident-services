package io.mosip.resident.test.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.dto.IdRepoResponseDto;
import io.mosip.resident.dto.VidGeneratorResponseDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.IdRepoAppException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utilitiy;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PrepareForTest({ JsonUtil.class })
public class UtilityTest {
	@Mock
	private ResidentServiceRestClient residentServiceRestClient;

	@InjectMocks
	private Utilitiy utility;

	private JSONObject identity;

	@Mock
	private Environment env;

	@Before
	public void setUp() throws IOException, ApisResourceAccessException {
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("ID.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String idJsonString = IOUtils.toString(is, "UTF-8");
		identity = JsonUtil.readValue(idJsonString, JSONObject.class);
		ResponseWrapper<IdRepoResponseDto> response = new ResponseWrapper<>();
		IdRepoResponseDto idRepoResponseDto = new IdRepoResponseDto();
		idRepoResponseDto.setStatus("Activated");
		idRepoResponseDto.setIdentity(JsonUtil.getJSONObject(identity, "identity"));
		response.setResponse(idRepoResponseDto);
        Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
                any(), any(Class.class))).thenReturn(response);
		ReflectionTestUtils.setField(utility, "configServerFileStorageURL", "url");
		ReflectionTestUtils.setField(utility, "residentIdentityJson", "json");

	}

	@Test
	public void retrieveIdrepoJsonSuccessTest() throws ResidentServiceCheckedException, ApisResourceAccessException {
		// UIN
		JSONObject identityJsonObj = utility.retrieveIdrepoJson("3527812406");
		assertEquals(identityJsonObj.get("UIN"), JsonUtil.getJSONObject(identity, "identity").get("UIN"));
		// RID
		JSONObject jsonUsingRID = utility.retrieveIdrepoJson("10008200070004420191203104356");
		assertEquals(jsonUsingRID.get("UIN"), JsonUtil.getJSONObject(identity, "identity").get("UIN"));

	}

	@Test
	@Ignore
	public void testRetrieveVidSuccess() throws ApisResourceAccessException, ResidentServiceCheckedException {
		List<String> pathsegments = new ArrayList<>();
		pathsegments.add("5628965106742572");
		VidGeneratorResponseDto vidResponse = new VidGeneratorResponseDto();
		vidResponse.setUIN("3527812406");
		vidResponse.setVidStatus("Active");
		ResponseWrapper<VidGeneratorResponseDto> response = new ResponseWrapper<>();
		response.setResponse(vidResponse);

		ResponseWrapper<IdRepoResponseDto> response1 = new ResponseWrapper<>();
		IdRepoResponseDto idRepoResponseDto = new IdRepoResponseDto();
		idRepoResponseDto.setStatus("Activated");
		idRepoResponseDto.setIdentity(JsonUtil.getJSONObject(identity, "identity"));
		response1.setResponse(idRepoResponseDto);

		Mockito.when(residentServiceRestClient.getApi(any(), any(), anyString(),
				any(), any(Class.class))).thenReturn(response).thenReturn(response1);
		JSONObject jsonUsingVID = utility.retrieveIdrepoJson("5628965106742572");
		assertEquals(jsonUsingVID.get("UIN"), JsonUtil.getJSONObject(identity, "identity").get("UIN"));
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

	@Test(expected = ResidentServiceCheckedException.class)
	@Ignore
	public void tokenGeneratorException() throws ResidentServiceCheckedException {
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
		Utilitiy utilitySpy = Mockito.spy(utility);
		Mockito.doReturn(mappingJson).when(utilitySpy).getMappingJson();
		Map<String, Object> attributes = utilitySpy.getMailingAttributes("3527812406", new HashSet<String>());
		assertEquals("girish.yarru@mindtree.com", attributes.get("email"));
		Map<String, Object> attributes1 = utilitySpy.getMailingAttributes("3527812406", new HashSet<String>());
		assertEquals("girish.yarru@mindtree.com", attributes1.get("email"));

	}
	
	@Test(expected = ResidentServiceException.class)
	public void testGetMailingAttributesJSONParsingException() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = "";
		Utilitiy utilitySpy = Mockito.spy(utility);
		Mockito.doReturn(mappingJson).when(utilitySpy).getMappingJson();
		Map<String, Object> attributes = utilitySpy.getMailingAttributes("3527812406", new HashSet<String>());
		assertEquals("girish.yarru@mindtree.com", attributes.get("email"));

		ReflectionTestUtils.setField(utilitySpy, "languageType", "NA");
		Map<String, Object> attributes1 = utilitySpy.getMailingAttributes("3527812406", new HashSet<String>());
		assertEquals("girish.yarru@mindtree.com", attributes1.get("email"));

	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGetMailingAttributesIOException() throws IOException, ResidentServiceCheckedException {
		ClassLoader classLoader = getClass().getClassLoader();
		File idJson = new File(classLoader.getResource("IdentityMapping.json").getFile());
		InputStream is = new FileInputStream(idJson);
		String mappingJson = IOUtils.toString(is, "UTF-8");
		Utilitiy utilitySpy = Mockito.spy(utility);
		Mockito.doReturn(mappingJson).when(utilitySpy).getMappingJson();
		Mockito.doReturn(JsonUtil.getJSONObject(identity, "identity")).when(utilitySpy)
				.retrieveIdrepoJson(Mockito.anyString());
		PowerMockito.mockStatic(JsonUtil.class);
		PowerMockito.when(JsonUtil.readValue(mappingJson, JSONObject.class)).thenThrow(new IOException());
		utilitySpy.getMailingAttributes("3527812406", new HashSet<String>());

	}
}
