package io.mosip.resident.test.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.*;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.IdRepoAppException;
import io.mosip.resident.exception.VidCreationException;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utilities;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

        // UIN
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
    public void testGetRegistrationProcessorMappingJsonWithMappingJsonNotNull() throws IOException {
        JSONObject jsonStringObject = JsonUtil.getJSONObject(identity, "response");
        Mockito.when(objMapper.readValue(anyString(), any(Class.class))).thenReturn(jsonStringObject);

        String identityString = JsonUtil.writeValueAsString(jsonStringObject);
        ReflectionTestUtils.setField(utilities, "mappingJsonString", identityString);

        Object identityObject = jsonStringObject.get("identity");

        JSONObject registrationProcessorMappingJson = utilities.getRegistrationProcessorMappingJson();
        assertEquals(registrationProcessorMappingJson, identityObject);
        verify(utilities, never()).getJson(anyString(), anyString());

        IdResponseDTO1 idResponseDTO1 = new IdResponseDTO1();
        ResponseDTO1 responseDTO1 = new ResponseDTO1();
        responseDTO1.setStatus("Activated");
        responseDTO1.setIdentity(JsonUtil.getJSONObject(identity, "response").get("identity"));
        idResponseDTO1.setResponse(responseDTO1);

        //     Mockito.when(residentRestTemplate.getForObject(anyString(), any(Class.class))).thenReturn(idResponseDTO1);
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
}
