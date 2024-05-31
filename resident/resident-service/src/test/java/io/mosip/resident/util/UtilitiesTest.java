package io.mosip.resident.util;

import static io.mosip.resident.constant.ResidentConstants.TRANSACTION_TYPE_CODE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.ErrorDTO;
import io.mosip.resident.dto.IdResponseDTO1;
import io.mosip.resident.dto.ResponseDTO1;
import io.mosip.resident.dto.VidResDTO;
import io.mosip.resident.dto.VidResponseDTO1;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.IdRepoAppException;
import io.mosip.resident.exception.IndividualIdNotFoundException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.IdentityService;
import io.mosip.resident.service.ProxyMasterdataService;

@ContextConfiguration(classes = {Utilities.class})
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PrepareForTest
public class UtilitiesTest {

    @InjectMocks
    @Spy
    private Utilities utilities = new Utilities();

    @Mock
    private ObjectMapper objMapper;

    @Mock
    private Environment env;

    @Mock
    private ResidentServiceRestClient residentServiceRestClient;

    @Mock
    @Qualifier("selfTokenRestTemplate")
    private RestTemplate residentRestTemplate;

    @Mock
    IdentityDataUtil identityDataUtil;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private IdentityService identityService;

    @Mock
    private ProxyMasterdataService proxyMasterdataService;

    @Mock
    private Utility utility;

    @Mock
    private GetAcrMappingUtil getAcrMappingUtil;

    @Mock
    private GetAccessTokenUtility getAccessTokenUtility;

    JSONObject identity;

    JSONObject identityVID;

    private Map<String, Object> amrAcrJson;

    @Mock
    private CachedIdentityDataUtil cachedIdentityDataUtil;

    @Mock
    private ProxyMasterDataServiceUtility proxyMasterDataServiceUtility;

    @Before
    public void setUp() throws IOException, ApisResourceAccessException {
        ClassLoader classLoader = getClass().getClassLoader();
        File idJson = new File(classLoader.getResource("Idrepo.json").getFile());
        InputStream is = new FileInputStream(idJson);
        String idJsonString = IOUtils.toString(is, "UTF-8");
        identity = JsonUtil.readValue(idJsonString, JSONObject.class);

        File idJsonVid = new File(classLoader.getResource("IdVidRepo.json").getFile());
        is = new FileInputStream(idJsonVid);
        idJsonString = IOUtils.toString(is, "UTF-8");
        identityVID = JsonUtil.readValue(idJsonString, JSONObject.class);

        File amrAcrJsonFile = new File(classLoader.getResource("amr-acr-mapping.json").getFile());
        InputStream insputStream = new FileInputStream(amrAcrJsonFile);
        String amrAcrJsonString = IOUtils.toString(insputStream, "UTF-8");
        amrAcrJson = JsonUtil.readValue(amrAcrJsonString, Map.class);
        ReflectionTestUtils.setField(utilities, "amrAcrJsonFile", "amr-acr-mapping.json");
    }

    @Test
    public void testRetrieveIdrepoJsonSuccess() throws ApisResourceAccessException, IOException {
        Map<String, String> uin = (Map<String, String>) JsonUtil.getJSONObject(identity, "response").get("identity");
        IdResponseDTO1 idResponseDTO1 = new IdResponseDTO1();
        ResponseDTO1 responseDTO1 = new ResponseDTO1();
        responseDTO1.setStatus("Activated");
        responseDTO1.setIdentity(JsonUtil.getJSONObject(identity, "response").get("identity"));
        idResponseDTO1.setResponse(responseDTO1);

        String identityString = JsonUtil.writeValueAsString(JsonUtil.getJSONObject(identity, "response").get("identity"));
        Mockito.when(residentServiceRestClient.getApi(any(), anyList(), anyString(), anyString(), any(Class.class))).thenReturn(idResponseDTO1);
        Mockito.when(objMapper.writeValueAsString(any())).thenReturn(identityString);

        // UIN
        JSONObject identityJsonObj = utilities.retrieveIdrepoJson("3527812406");
        assertEquals(identityJsonObj.get("UIN"), uin.get("UIN"));
    }

    @Test
    public void testRetrieveIdrepoJsonIfFalse() throws ApisResourceAccessException, IOException {
        // UIN
        JSONObject identityJsonObj = utilities.retrieveIdrepoJson(null);
    }

    @Test
    public void testRetrieveIdrepoJsonIfFalse2() throws ApisResourceAccessException, IOException {
        // UIN
        JSONObject identityJsonObj = utilities.retrieveIdrepoJson("anything");
    }

    @Test(expected = IdRepoAppException.class)
    public void testRetrieveIdrepoJsonThrowIdRepoAppException() throws ApisResourceAccessException, IOException {
        ServiceError error = new ServiceError(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(), ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage());
        List<ServiceError> errorResponse = new ArrayList<>();
        errorResponse.add(error);
        IdResponseDTO1 idResponseDTO1 = new IdResponseDTO1();
        idResponseDTO1.setErrors(errorResponse);
        Mockito.when(residentServiceRestClient.getApi(any(), anyList(), anyString(), anyString(), any(Class.class))).thenReturn(idResponseDTO1);

        // UIN
        utilities.retrieveIdrepoJson("3527812406");
    }

    @Test
    public void testGetRegistrationProcessorMappingJsonWithMappingJsonNotNull() throws IOException {
        JSONObject jsonStringObject = JsonUtil.getJSONObject(identity, "response");
        Mockito.when(objMapper.readValue(anyString(), any(Class.class))).thenReturn(jsonStringObject);

        String identityString = JsonUtil.writeValueAsString(jsonStringObject);
        ReflectionTestUtils.setField(utilities, "mappingJsonString", identityString);

        Object identityObject = jsonStringObject.get("identity");

        JSONObject registrationProcessorMappingJson = utilities.getRegistrationProcessorMappingJson();
        assertEquals(registrationProcessorMappingJson, identityObject);
        verify(utilities, never()).getJson(anyString(), anyString());
    }

    @Test
    public void testGetRegistrationProcessorMappingJsonWithMappingJsonIsNull() throws IOException {
        JSONObject jsonStringObject = JsonUtil.getJSONObject(identity, "response");
        Mockito.when(objMapper.readValue(anyString(), any(Class.class))).thenReturn(jsonStringObject);

        String identityString = JsonUtil.writeValueAsString(jsonStringObject);
        ReflectionTestUtils.setField(utilities, "regProcessorIdentityJson", identityString);

        Object identityObject = jsonStringObject.get("identity");
        JSONObject registrationProcessorMappingJson = utilities.getRegistrationProcessorMappingJson();
        assertEquals(registrationProcessorMappingJson, identityObject);
        verify(residentRestTemplate, never()).getForObject(anyString(), any(Class.class));
    }

    @Test
    public void testGetRegistrationProcessorMappingJsonWithProcessorIdentityJsonIsNull() throws IOException {
        JSONObject jsonStringObject = JsonUtil.getJSONObject(identity, "response");
        Mockito.when(objMapper.readValue(anyString(), any(Class.class))).thenReturn(jsonStringObject);
        String identityString = JsonUtil.writeValueAsString(jsonStringObject);
        Mockito.when(residentRestTemplate.getForObject(anyString(), any(Class.class))).thenReturn(identityString);

        Object identityObject = jsonStringObject.get("identity");
        JSONObject registrationProcessorMappingJson = utilities.getRegistrationProcessorMappingJson();
        assertEquals(registrationProcessorMappingJson, identityObject);
    }

    @Test
    public void testGetUinByVid() throws ApisResourceAccessException, IOException {
        JSONObject response = JsonUtil.getJSONObject(identityVID, "response");
        VidResDTO vidResDTO = new VidResDTO();
        vidResDTO.setVidStatus((String) response.get("vidStatus"));
        vidResDTO.setRestoredVid((VidResDTO) response.get("restoredVid"));
        vidResDTO.setUin((String) response.get("UIN"));
        vidResDTO.setVid((String) response.get("VID"));
        VidResponseDTO1 vidResponseDTO1 = new VidResponseDTO1();
        vidResponseDTO1.setResponse(vidResDTO);
        vidResponseDTO1.setErrors(new ArrayList<>());

        Mockito.when(residentServiceRestClient.getApi(any(), anyList(), anyString(), anyString(), any(Class.class))).thenReturn(vidResponseDTO1);

        // VID
        String uin = utilities.getUinByVid("6241572684701486");
        assertEquals(uin, response.get("UIN"));
    }

    @Test(expected = IndividualIdNotFoundException.class)
    public void testGetUinByVidThrowVidCreationException() throws ApisResourceAccessException, IOException {
        ErrorDTO error = new ErrorDTO(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(), ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage());
        List<ErrorDTO> errorResponse = new ArrayList<>();
        errorResponse.add(error);
        VidResponseDTO1 vidResponseDTO1 = new VidResponseDTO1();
        vidResponseDTO1.setErrors(errorResponse);
        Mockito.when(residentServiceRestClient.getApi(any(), anyList(), anyString(), anyString(), any(Class.class))).thenReturn(vidResponseDTO1);

        // VID
        utilities.getUinByVid("6241572684701486");
    }

    @Test
    public void testRetrieveIdrepoJsonStatus() throws ApisResourceAccessException, IOException {
        JSONObject response = JsonUtil.getJSONObject(identity, "response");
        IdResponseDTO1 idResponseDTO1 = new IdResponseDTO1();
        ResponseDTO1 responseDTO1 = new ResponseDTO1();
        responseDTO1.setStatus((String) response.get("status"));
        responseDTO1.setIdentity(response.get("identity"));
        idResponseDTO1.setResponse(responseDTO1);

        String identityString = JsonUtil.writeValueAsString(response.get("identity"));
        Mockito.when(residentServiceRestClient.getApi(any(), anyList(), anyString(), anyString(), any(Class.class))).thenReturn(idResponseDTO1);
        Mockito.when(objMapper.writeValueAsString(any())).thenReturn(identityString);

        // Status
        String status = utilities.retrieveIdrepoJsonStatus("3527812406");
        assertEquals(status, response.get("status"));
    }

    @Test
    public void testRetrieveIdrepoJsonStatusNestedIf() throws ApisResourceAccessException, IOException {

        Mockito.when(residentServiceRestClient.getApi(any(), anyList(), anyString(), anyString(), any(Class.class))).thenReturn(null);

        // Status
        String status = utilities.retrieveIdrepoJsonStatus("3527812406");
    }

    @Test
    public void testRetrieveIdrepoJsonStatusWithUinNull() throws ApisResourceAccessException, IOException {
        utilities.retrieveIdrepoJsonStatus(null);
    }

    @Test(expected = IdRepoAppException.class)
    public void testRetrieveIdrepoJsonStatusThrowIdRepoAppException() throws ApisResourceAccessException, IOException {
        ServiceError error = new ServiceError(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(), ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage());
        List<ServiceError> errorResponse = new ArrayList<>();
        errorResponse.add(error);
        IdResponseDTO1 idResponseDTO1 = new IdResponseDTO1();
        idResponseDTO1.setErrors(errorResponse);
        Mockito.when(residentServiceRestClient.getApi(any(), anyList(), anyString(), anyString(), any(Class.class))).thenReturn(idResponseDTO1);

        // UIN
        utilities.retrieveIdrepoJsonStatus("3527812406");
    }

    @Test
    public void testGenerateAudit() {
        List<Map<String, String>> mapList = utilities.generateAudit("12345");
        assertEquals("12345", mapList.get(0).get("id"));
    }

    @Test
    public void testGetLanguageCode() {
        when(env.getProperty(any())).thenReturn("mandatory languages");

        String result = utilities.getLanguageCode();
        assertNotNull(result);
    }

    @Test
    public void testGetLanguageCodeElse() {
        when(env.getProperty(any())).thenReturn("");

        utilities.getLanguageCode();
    }

    @Test
    public void testGetLanguageCodeNestedIf() {
        when(env.getProperty("mosip.optional-languages")).thenReturn("optional-languages");

        String result = utilities.getLanguageCode();
        assertNotNull(result);
    }

    @Test
    public void testGetEmailAttribute() throws ResidentServiceCheckedException, IOException {
        thrown.expect(Exception.class);
        JSONObject jsonStringObject = JsonUtil.getJSONObject(identity, "response");
        Mockito.when(objMapper.readValue(anyString(), any(Class.class))).thenReturn(jsonStringObject);

        String identityString = JsonUtil.writeValueAsString(jsonStringObject);
        ReflectionTestUtils.setField(utilities, "mappingJsonString", identityString);

        utilities.getEmailAttribute();
    }

    @Test
    public void testGetAmrAcrMapping() throws ResidentServiceCheckedException, IOException {
    	Mockito.when(residentRestTemplate.getForObject(anyString(), any(Class.class))).thenReturn(amrAcrJson.toString());
    	Mockito.when(objMapper.readValue(amrAcrJson.toString().getBytes(UTF_8), Map.class)).thenReturn(amrAcrJson);
        getAcrMappingUtil.getAmrAcrMapping();
    }

    @Test
    public void testGetDynamicFieldBasedOnLangCodeAndFieldName() throws ResidentServiceCheckedException {
        ResponseWrapper responseWrapper = new ResponseWrapper<>();
        responseWrapper.setErrors(new ArrayList<>());
        responseWrapper.setId("https://example.org/example");
        responseWrapper.setMetadata("Metadata");
        responseWrapper.setResponse("Response");
        responseWrapper.setResponsetime(LocalDateTime.of(1, 1, 1, 1, 1));
        responseWrapper.setVersion("https://example.org/example");
        when((ResponseWrapper<Object>) proxyMasterdataService.getDynamicFieldBasedOnLangCodeAndFieldName(
                (String) org.mockito.Mockito.any(), (String) org.mockito.Mockito.any(), anyBoolean()))
                .thenReturn(responseWrapper);
        assertSame(responseWrapper,
                proxyMasterDataServiceUtility.getDynamicFieldBasedOnLangCodeAndFieldName("Field Name", "Lang Code", true));
        verify(proxyMasterdataService).getDynamicFieldBasedOnLangCodeAndFieldName((String) org.mockito.Mockito.any(),
                (String) org.mockito.Mockito.any(), anyBoolean());
    }
    @Test
    public void testGetDynamicFieldBasedOnLangCodeAndFieldName2() throws ResidentServiceCheckedException {
        when((ResponseWrapper<Object>) proxyMasterdataService.getDynamicFieldBasedOnLangCodeAndFieldName(
                (String) org.mockito.Mockito.any(), (String) org.mockito.Mockito.any(), anyBoolean()))
                .thenThrow(new IdRepoAppException("An error occurred", "An error occurred"));
        thrown.expect(IdRepoAppException.class);
        proxyMasterDataServiceUtility.getDynamicFieldBasedOnLangCodeAndFieldName("Field Name", "Lang Code", true);
        verify(proxyMasterdataService).getDynamicFieldBasedOnLangCodeAndFieldName((String) org.mockito.Mockito.any(),
                (String) org.mockito.Mockito.any(), anyBoolean());
    }

    @Test
    public void testGetRidByIndividualId() throws ApisResourceAccessException {
        ResponseWrapper response = new ResponseWrapper<>();
        response.setResponse(Map.of("rid", "123"));
        Mockito.when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(response);
        String rid = utilities.getRidByIndividualId("123");
        assertEquals("123", rid);
    }

    @Test(expected = IndividualIdNotFoundException.class)
    public void testGetRidByIndividualIdFailed() throws ApisResourceAccessException {
        ResponseWrapper<?> response = new ResponseWrapper<>();
        response.setErrors(List.of(new ServiceError(ResidentErrorCode.INVALID_INDIVIDUAL_ID.getErrorCode(),
                ResidentErrorCode.INVALID_INDIVIDUAL_ID.getErrorMessage())));
        Mockito.when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(response);
        utilities.getRidByIndividualId("123");
    }

    @Test(expected = Exception.class)
    public void testGetRidStatus() throws ApisResourceAccessException, IOException, ResidentServiceCheckedException {
        ResponseWrapper<ArrayList> response = new ResponseWrapper<>();
        ArrayList<Map> arrayList = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        map.put("createdDateTimes", "2023-09-21T08:38:34.280Z");
        arrayList.add(map);
        response.setResponse(arrayList);
        Mockito.when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(response);
        utilities.getRidStatus("123");
    }

    @Test
    public void testGetTransactionTypeCode() throws ApisResourceAccessException, IOException {
        when(env.getProperty(any())).thenReturn("PACKET_UPLOAD,PACKET_RECEIVER");
        HashMap<String, Object> packetStatus = new HashMap<>();
        packetStatus.put(TRANSACTION_TYPE_CODE, "PACKET_RECEIVER");
        assertEquals(Optional.of("REQUEST_RECEIVED"),
                ReflectionTestUtils.invokeMethod(utilities, "getTransactionTypeCode", packetStatus));
    }

    @Test
    public void testGetTransactionTypeCodeFailed() throws ApisResourceAccessException, IOException {
        when(env.getProperty(any())).thenReturn("status,packet");
        HashMap<String, Object> packetStatus = new HashMap<>();
        packetStatus.put(TRANSACTION_TYPE_CODE, "test");
        ReflectionTestUtils.invokeMethod(utilities, "getTransactionTypeCode", packetStatus);
    }

    @Test
    public void testGetJson() {
    	ReflectionTestUtils.setField(utilities, "regProcessorIdentityJson", null);
        utilities.getJson("http://localhost", "http://localhost");
    }

    @Test(expected = IdRepoAppException.class)
    public void testRetrieveIdrepoJsonFailure() throws ApisResourceAccessException, IOException {
        Map<String, String> uin = (Map<String, String>) JsonUtil.getJSONObject(identity, "response").get("identity");
        IdResponseDTO1 idResponseDTO1 = new IdResponseDTO1();
        ResponseDTO1 responseDTO1 = new ResponseDTO1();
        responseDTO1.setStatus("Activated");
        responseDTO1.setIdentity(JsonUtil.getJSONObject(identity, "response").get("identity"));
        idResponseDTO1.setResponse(responseDTO1);

        Mockito.when(residentServiceRestClient.getApi(any(), anyList(), anyString(), anyString(), any(Class.class))).thenReturn(idResponseDTO1);
        Mockito.when(objMapper.writeValueAsString(any())).thenReturn("identityString");

        // UIN
        JSONObject identityJsonObj = utilities.retrieveIdrepoJson("3527812406");
        assertEquals(identityJsonObj.get("UIN"), uin.get("UIN"));
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetRidStatusFailed() throws ApisResourceAccessException, IOException, ResidentServiceCheckedException {
        ResponseWrapper<ArrayList> response = new ResponseWrapper<>();
        ArrayList arrayList = new ArrayList<>();
        arrayList.add("123");
        response.setResponse(arrayList);
        response.setErrors(List.of(new ServiceError(ResidentErrorCode.RID_NOT_FOUND.getErrorCode(),
                ResidentErrorCode.RID_NOT_FOUND.getErrorMessage())));
        Mockito.when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(response);
        utilities.getRidStatus("123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRidStatusWithParseException() throws ApisResourceAccessException, IOException, ResidentServiceCheckedException {
    	ResponseWrapper<ArrayList> response = new ResponseWrapper<>();
    	ArrayList<Object> objectArrayList = new ArrayList<>();
        Map<String, Object> packetData = new HashMap<>();
        packetData.put("createdDateTimes", "12/10/2012");
        objectArrayList.add(packetData);
        packetData.put("createdDateTimes", "05/09/2012");
        objectArrayList.add(packetData);
        response.setResponse(objectArrayList);
        Mockito.when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(response);
        Mockito.when(objMapper.readValue(Mockito.anyString(), (Class<Object>) any())).thenReturn(objectArrayList);
        Mockito.when(objMapper.writeValueAsString(Mockito.any())).thenReturn(String.valueOf(objectArrayList));
        Mockito.when(env.getProperty(Mockito.anyString())).thenReturn("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        utilities.getRidStatus("123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPacketStatusFailed() throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
        ResponseWrapper<ArrayList> response = new ResponseWrapper<>();
        ArrayList arrayList = new ArrayList<>();
        arrayList.add("123");
        response.setResponse(arrayList);
        Mockito.when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(response);
        ArrayList<Object> objectArrayList = new ArrayList<>();
        objectArrayList.add("t");
        Mockito.when(objMapper.readValue(Mockito.anyString(), (Class<Object>) any())).thenReturn(objectArrayList);
        Mockito.when(objMapper.writeValueAsString(Mockito.any())).thenReturn(String.valueOf(objectArrayList));
        utilities.getPacketStatus("10241102241004720230627060344");
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetPacketStatusError() throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
        ResponseWrapper<ArrayList> response = new ResponseWrapper<>();
        ArrayList arrayList = new ArrayList<>();
        arrayList.add("123");
        response.setResponse(arrayList);
        Mockito.when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(response);
        ArrayList<Object> objectArrayList = new ArrayList<>();
        Map<String, Object> packetData = new HashMap<>();
        packetData.put("statusCode", "");
        packetData.put("transactionTypeCode", "");
        objectArrayList.add(packetData);
        Mockito.when(objMapper.readValue(Mockito.anyString(), (Class<Object>) any())).thenReturn(objectArrayList);
        Mockito.when(objMapper.writeValueAsString(Mockito.any())).thenReturn(String.valueOf(objectArrayList));
        utilities.getPacketStatus("10241102241004720230627060344");
    }

    @Test
    public void testGetPacketStatus() throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
    	ResponseWrapper<ArrayList> response = new ResponseWrapper<>();
    	ArrayList<Object> objectArrayList = new ArrayList<>();
        Map<String, Object> packetData = new HashMap<>();
        packetData.put("statusCode", "SUCCESS");
        packetData.put("transactionTypeCode", "SUCCESS");
        packetData.put("createdDateTimes", "2012-10-15");
        objectArrayList.add(packetData);
        packetData.put("statusCode", "SUCCESS");
        packetData.put("transactionTypeCode", "SUCCESS");
        packetData.put("createdDateTimes", "2012-09-02");
        objectArrayList.add(packetData);
        response.setResponse(objectArrayList);
        Mockito.when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(response);
        Mockito.when(objMapper.readValue(Mockito.anyString(), (Class<Object>) any())).thenReturn(objectArrayList);
        Mockito.when(objMapper.writeValueAsString(Mockito.any())).thenReturn(String.valueOf(objectArrayList));
        Mockito.when(env.getProperty(Mockito.anyString())).thenReturn("yyyy-MM-dd").thenReturn("SUCCESS");
        Map<String, String> result = utilities.getPacketStatus("10241102241004720230627060344");
        assertEquals("SUCCESS", result.get("aidStatus"));
    }

    @Test
    public void testGetDefaultSource() {
        ReflectionTestUtils.setField(utilities, "provider",
                "source:RESIDENT,process:ACTIVATED|DEACTIVATED|RES_UPDATE|LOST|RES_REPRINT,classname:io.mosip.commons.packet.impl.PacketWriterImpl\n");
        assertEquals("RESIDENT", utilities.getDefaultSource());
    }

    @Test(expected = Exception.class)
    public void testGetIdentityDataFromIndividualIDNullIdeResponseDto()
            throws ApisResourceAccessException, ResidentServiceCheckedException, IOException {
        when(getAccessTokenUtility.getAccessToken()).thenReturn("ABC123");
        when(objMapper.writeValueAsString((Object) org.mockito.Mockito.any())).thenReturn("{} - {} - {} - {}");

        ResponseDTO1 responseDTO1 = new ResponseDTO1();
        responseDTO1.setDocuments(new ArrayList<>());
        responseDTO1.setEntity("Utilities::retrieveIdrepoJson()::entry");
        responseDTO1.setIdentity("Identity");
        responseDTO1.setStatus("Utilities::retrieveIdrepoJson()::entry");

        IdResponseDTO1 idResponseDTO1 = new IdResponseDTO1();
        idResponseDTO1.setErrors(new ArrayList<>());
        idResponseDTO1.setId("42");
        idResponseDTO1.setResponse(responseDTO1);
        idResponseDTO1.setResponsetime("Utilities::retrieveIdrepoJson()::entry");
        idResponseDTO1.setVersion("1.0.2");
        when(cachedIdentityDataUtil.getCachedIdentityData((String) org.mockito.Mockito.any(), (String) org.mockito.Mockito.any(),
                (Class<Object>) org.mockito.Mockito.any())).thenReturn(null);
        thrown.expect(IdRepoAppException.class);
        identityDataUtil.getIdentityDataFromIndividualID("42");
        verify(getAccessTokenUtility).getAccessToken();
        verify(objMapper).writeValueAsString((Object) org.mockito.Mockito.any());
        cachedIdentityDataUtil.getCachedIdentityData((String) org.mockito.Mockito.any(), (String) org.mockito.Mockito.any(),
                (Class<Object>) org.mockito.Mockito.any());
    }

    @Test(expected = IdRepoAppException.class)
    public void testGetIdentityDataFromIndividualIDIdRepoAppException()
            throws ApisResourceAccessException, ResidentServiceCheckedException, IOException {
        when(getAccessTokenUtility.getAccessToken()).thenReturn("ABC123");
        when(objMapper.writeValueAsString((Object) org.mockito.Mockito.any())).thenReturn("{} - {} - {} - {}");

        ResponseDTO1 responseDTO1 = new ResponseDTO1();
        responseDTO1.setDocuments(new ArrayList<>());
        responseDTO1.setEntity("Utilities::retrieveIdrepoJson()::entry");
        responseDTO1.setIdentity("Identity");
        responseDTO1.setStatus("Utilities::retrieveIdrepoJson()::entry");

        IdResponseDTO1 idResponseDTO1 = new IdResponseDTO1();
        idResponseDTO1.setErrors(List.of(new ServiceError(ResidentErrorCode.NO_RECORDS_FOUND.getErrorCode(),
                ResidentErrorCode.NO_RECORDS_FOUND.getErrorMessage())));
        idResponseDTO1.setId("42");
        idResponseDTO1.setResponse(responseDTO1);
        idResponseDTO1.setResponsetime("Utilities::retrieveIdrepoJson()::entry");
        idResponseDTO1.setVersion("1.0.2");
        when(cachedIdentityDataUtil.getCachedIdentityData((String) org.mockito.Mockito.any(), (String) org.mockito.Mockito.any(),
                (Class<Object>) org.mockito.Mockito.any())).thenReturn(idResponseDTO1);
        thrown.expect(IdRepoAppException.class);
        identityDataUtil.getIdentityDataFromIndividualID("42");
        verify(getAccessTokenUtility).getAccessToken();
        verify(objMapper).writeValueAsString((Object) org.mockito.Mockito.any());
        cachedIdentityDataUtil.getCachedIdentityData((String) org.mockito.Mockito.any(), (String) org.mockito.Mockito.any(),
                (Class<Object>) org.mockito.Mockito.any());
    }

    @Test
    public void testGetIdentityDataFromIndividual()
            throws ApisResourceAccessException, ResidentServiceCheckedException, IOException {
        when(getAccessTokenUtility.getAccessToken()).thenReturn("ABC123");
        JSONObject identity = new JSONObject();
        identity.put(ResidentConstants.ID_SCHEMA_VERSION, "0.1");
        when(objMapper.writeValueAsString((Object) org.mockito.Mockito.any())).thenReturn(identity.toJSONString());
        ResponseWrapper idSchemaResponse = new ResponseWrapper();
        JSONObject object = new JSONObject();
        Object schema = "{\\\"$schema\\\":\\\"http:\\/\\/json-schema.org\\/draft-07\\/schema#\\\",\\\"description\\\":\\\"MOSIP Sample identity\\\",\\\"additionalProperties\\\":false,\\\"title\\\":\\\"MOSIP identity\\\",\\\"type\\\":\\\"object\\\",\\\"definitions\\\":{\\\"simpleType\\\":{\\\"uniqueItems\\\":true,\\\"additionalItems\\\":false,\\\"type\\\":\\\"array\\\",\\\"items\\\":{\\\"additionalProperties\\\":false,\\\"type\\\":\\\"object\\\",\\\"required\\\":[\\\"language\\\",\\\"value\\\"],\\\"properties\\\":{\\\"language\\\":{\\\"type\\\":\\\"string\\\"},\\\"value\\\":{\\\"type\\\":\\\"string\\\"}}}},\\\"documentType\\\":{\\\"additionalProperties\\\":false,\\\"type\\\":\\\"object\\\",\\\"properties\\\":{\\\"format\\\":{\\\"type\\\":\\\"string\\\"},\\\"type\\\":{\\\"type\\\":\\\"string\\\"},\\\"value\\\":{\\\"type\\\":\\\"string\\\"},\\\"refNumber\\\":{\\\"type\\\":[\\\"string\\\",\\\"null\\\"]}}},\\\"biometricsType\\\":{\\\"additionalProperties\\\":false,\\\"type\\\":\\\"object\\\",\\\"properties\\\":{\\\"format\\\":{\\\"type\\\":\\\"string\\\"},\\\"version\\\":{\\\"type\\\":\\\"number\\\",\\\"minimum\\\":0},\\\"value\\\":{\\\"type\\\":\\\"string\\\"}}}},\\\"properties\\\":{\\\"identity\\\":{\\\"additionalProperties\\\":false,\\\"type\\\":\\\"object\\\",\\\"required\\\":[\\\"IDSchemaVersion\\\",\\\"fullName\\\",\\\"dateOfBirth\\\",\\\"gender\\\",\\\"addressLine1\\\",\\\"addressLine2\\\",\\\"addressLine3\\\",\\\"region\\\",\\\"province\\\",\\\"city\\\",\\\"zone\\\",\\\"postalCode\\\",\\\"phone\\\",\\\"email\\\",\\\"proofOfIdentity\\\",\\\"individualBiometrics\\\"],\\\"properties\\\":{\\\"proofOfAddress\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"gender\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"city\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{0,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"postalCode\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^[(?i)A-Z0-9]{5}$|^NA$\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"proofOfException-1\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"evidence\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"referenceIdentityNumber\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^([0-9]{10,30})$\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"kyc\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"individualBiometrics\\\":{\\\"bioAttributes\\\":[\\\"leftEye\\\",\\\"rightEye\\\",\\\"rightIndex\\\",\\\"rightLittle\\\",\\\"rightRing\\\",\\\"rightMiddle\\\",\\\"leftIndex\\\",\\\"leftLittle\\\",\\\"leftRing\\\",\\\"leftMiddle\\\",\\\"leftThumb\\\",\\\"rightThumb\\\",\\\"face\\\"],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/biometricsType\\\"},\\\"province\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{0,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"zone\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"proofOfDateOfBirth\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"addressLine1\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{0,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"addressLine2\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{3,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"residenceStatus\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"kyc\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"addressLine3\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{3,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"email\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^[A-Za-z0-9_\\\\\\\\-]+(\\\\\\\\.[A-Za-z0-9_]+)*@[A-Za-z0-9_-]+(\\\\\\\\.[A-Za-z0-9_]+)*(\\\\\\\\.[a-zA-Z]{2,})$\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"introducerRID\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"evidence\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"introducerBiometrics\\\":{\\\"bioAttributes\\\":[\\\"leftEye\\\",\\\"rightEye\\\",\\\"rightIndex\\\",\\\"rightLittle\\\",\\\"rightRing\\\",\\\"rightMiddle\\\",\\\"leftIndex\\\",\\\"leftLittle\\\",\\\"leftRing\\\",\\\"leftMiddle\\\",\\\"leftThumb\\\",\\\"rightThumb\\\",\\\"face\\\"],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/biometricsType\\\"},\\\"fullName\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{3,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"dateOfBirth\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(1869|18[7-9][0-9]|19[0-9][0-9]|20[0-9][0-9])\\/([0][1-9]|1[0-2])\\/([0][1-9]|[1-2][0-9]|3[01])$\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"individualAuthBiometrics\\\":{\\\"bioAttributes\\\":[\\\"leftEye\\\",\\\"rightEye\\\",\\\"rightIndex\\\",\\\"rightLittle\\\",\\\"rightRing\\\",\\\"rightMiddle\\\",\\\"leftIndex\\\",\\\"leftLittle\\\",\\\"leftRing\\\",\\\"leftMiddle\\\",\\\"leftThumb\\\",\\\"rightThumb\\\",\\\"face\\\"],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/biometricsType\\\"},\\\"introducerUIN\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"evidence\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"proofOfIdentity\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"IDSchemaVersion\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"none\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"number\\\",\\\"fieldType\\\":\\\"default\\\",\\\"minimum\\\":0},\\\"proofOfException\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"evidence\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"phone\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^[+]*([0-9]{1})([0-9]{9})$\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"introducerName\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"evidence\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"},\\\"proofOfRelationship\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/documentType\\\"},\\\"UIN\\\":{\\\"bioAttributes\\\":[],\\\"fieldCategory\\\":\\\"none\\\",\\\"format\\\":\\\"none\\\",\\\"type\\\":\\\"string\\\",\\\"fieldType\\\":\\\"default\\\"},\\\"region\\\":{\\\"bioAttributes\\\":[],\\\"validators\\\":[{\\\"validator\\\":\\\"^(?=.{0,50}$).*\\\",\\\"arguments\\\":[],\\\"type\\\":\\\"regex\\\"}],\\\"fieldCategory\\\":\\\"pvt\\\",\\\"format\\\":\\\"none\\\",\\\"fieldType\\\":\\\"default\\\",\\\"$ref\\\":\\\"#\\/definitions\\/simpleType\\\"}}}}}";
        object.put("schemaJson", schema);
        idSchemaResponse.setResponse(object);
        Map schemaJson = new HashMap<>();
        schemaJson.put("schemaJson", "schema");
        when(objMapper.convertValue((Object) any(), (Class<Object>) any())).thenReturn(schemaJson);
        when(proxyMasterdataService.getLatestIdSchema(Double.parseDouble("0.1"), null, null)).thenReturn(idSchemaResponse);

        ResponseDTO1 responseDTO1 = new ResponseDTO1();
        responseDTO1.setDocuments(new ArrayList<>());
        responseDTO1.setEntity("Utilities::retrieveIdrepoJson()::entry");
        responseDTO1.setIdentity("Identity");
        responseDTO1.setStatus("Utilities::retrieveIdrepoJson()::entry");

        responseDTO1.setIdentity(identity);

        IdResponseDTO1 idResponseDTO1 = new IdResponseDTO1();
        idResponseDTO1.setId("42");
        idResponseDTO1.setResponse(responseDTO1);
        idResponseDTO1.setResponsetime("Utilities::retrieveIdrepoJson()::entry");
        idResponseDTO1.setVersion("1.0.2");
        when(cachedIdentityDataUtil.getCachedIdentityData((String) org.mockito.Mockito.any(), (String) org.mockito.Mockito.any(),
                (Class<Object>) org.mockito.Mockito.any())).thenReturn(idResponseDTO1);
        assertEquals("schema", identityDataUtil.getIdentityDataFromIndividualID("42").getT2());
        verify(getAccessTokenUtility).getAccessToken();
        verify(objMapper).writeValueAsString((Object) org.mockito.Mockito.any());
        cachedIdentityDataUtil.getCachedIdentityData((String) org.mockito.Mockito.any(), (String) org.mockito.Mockito.any(),
                (Class<Object>) org.mockito.Mockito.any());
    }


}
