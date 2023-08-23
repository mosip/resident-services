package io.mosip.resident.util;

import static io.mosip.resident.constant.ResidentConstants.TRANSACTION_TYPE_CODE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.ApiName;
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
import io.mosip.resident.exception.VidCreationException;

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

    JSONObject identity;

    JSONObject identityVID;

    private JSONObject amrAcrJson;

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
        amrAcrJson = JsonUtil.readValue(amrAcrJsonString, JSONObject.class);
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

    @Test(expected = VidCreationException.class)
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
    public void testRetrieveIdrepoJsonStatusWithUinNull() throws ApisResourceAccessException, IOException{
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
    	List<Map<String, String>> mapList=utilities.generateAudit("12345");
    	assertEquals("12345", mapList.get(0).get("id"));
    }
    
    @Test
    public void testGetLanguageCode() {
    	when(env.getProperty(any())).thenReturn("mandatory languages");
    	
    	String result=utilities.getLanguageCode();
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
    	
    	String result=utilities.getLanguageCode();
    	assertNotNull(result);
    }

    @Test
    public void testGetRidByIndividualId() throws ApisResourceAccessException {
        ResponseWrapper response = new ResponseWrapper<>();
        response.setResponse(Map.of("rid","123"));
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

    @Test
    public void testGetRidStatus() throws ApisResourceAccessException, IOException, ResidentServiceCheckedException {
        ResponseWrapper<ArrayList> response = new ResponseWrapper<>();
        ArrayList arrayList = new ArrayList<>();
        arrayList.add("123");
        response.setResponse(arrayList);
        Mockito.when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(response);
        utilities.getRidStatus("123");
    }

    @Test
    public void testGetTransactionTypeCode() throws ApisResourceAccessException, IOException {
    	when(env.getProperty(any())).thenReturn("PACKET_UPLOAD,PACKET_RECEIVER");
        HashMap<String ,Object> packetStatus = new HashMap<>();
        packetStatus.put(TRANSACTION_TYPE_CODE, "PACKET_RECEIVER");
        assertEquals(Optional.of("REQUEST_RECEIVED"),
                ReflectionTestUtils.invokeMethod(utilities, "getTransactionTypeCode", packetStatus));
    }

    @Test
    public void testGetTransactionTypeCodeFailed() throws ApisResourceAccessException, IOException {
    	when(env.getProperty(any())).thenReturn("status,packet");
        HashMap<String ,Object> packetStatus = new HashMap<>();
        packetStatus.put(TRANSACTION_TYPE_CODE, "test");
        ReflectionTestUtils.invokeMethod(utilities, "getTransactionTypeCode", packetStatus);
    }

    @Test
    public void testGetJson(){
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

    @Test(expected = ResidentServiceCheckedException.class)
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

    @Test
    public void testGetPacketStatus() throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
        ResponseWrapper<ArrayList> response = new ResponseWrapper<>();
        ArrayList arrayList = new ArrayList<>();
        arrayList.add("123");
        response.setResponse(arrayList);
        Mockito.when(residentServiceRestClient.getApi((ApiName) any(), any(), any())).thenReturn(response);
        ArrayList<Object> objectArrayList = new ArrayList<>();
        Map<String, Object> packetData = new HashMap<>();
        packetData.put("statusCode", "SUCCESS");
        packetData.put("transactionTypeCode", "SUCCESS");
        objectArrayList.add(packetData);
        Mockito.when(objMapper.readValue(Mockito.anyString(), (Class<Object>) any())).thenReturn(objectArrayList);
        Mockito.when(objMapper.writeValueAsString(Mockito.any())).thenReturn(String.valueOf(objectArrayList));
        Mockito.when(env.getProperty(Mockito.anyString())).thenReturn("SUCCESS");
        Map<String, String> result = utilities.getPacketStatus("10241102241004720230627060344");
        assertEquals("SUCCESS",result.get("aidStatus"));
    }

    @Test
    public void testGetDefaultSource(){
        ReflectionTestUtils.setField(utilities, "provider",
                "source:RESIDENT,process:ACTIVATED|DEACTIVATED|RES_UPDATE|LOST|RES_REPRINT,classname:io.mosip.commons.packet.impl.PacketWriterImpl\n");
        assertEquals("RESIDENT", utilities.getDefaultSource());
    }
}
