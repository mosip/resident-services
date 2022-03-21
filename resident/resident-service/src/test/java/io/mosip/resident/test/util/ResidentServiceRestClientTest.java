package io.mosip.resident.test.util;

import io.mosip.resident.constant.ApiName;
import io.mosip.resident.dto.AutnTxnResponseDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.util.ResidentServiceRestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ResidentServiceRestClientTest {

    @InjectMocks
    ResidentServiceRestClient residentServiceRestClient;

    @Mock
    RestTemplateBuilder builder;

    @Mock
    Environment environment;

    @Mock
    RestTemplate residentRestTemplate;


    @Before
    public void setup() {
    }

    @Test
    public void testgetApi() throws ApisResourceAccessException {
        AutnTxnResponseDto autnTxnResponseDto = new AutnTxnResponseDto();
        autnTxnResponseDto.setId("ancd");
        ResponseEntity<AutnTxnResponseDto> obj = new ResponseEntity<>(autnTxnResponseDto, HttpStatus.OK);
        URI uri = UriComponentsBuilder.fromUriString("https://int.mosip.io/individualIdType/UIN/individualId/1234").build(false).encode().toUri();
        ResidentServiceRestClient client = Mockito.spy(residentServiceRestClient);
        when(residentRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(), Matchers.<Class<AutnTxnResponseDto>>any())).thenReturn(obj);

        assertTrue(client.getApi(uri, AutnTxnResponseDto.class).toString().contains("ancd"));
    }

    @Test(expected = ApisResourceAccessException.class)
    public void testgetApiException() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, ApisResourceAccessException {
        AutnTxnResponseDto autnTxnResponseDto = new AutnTxnResponseDto();
        autnTxnResponseDto.setId("ancd");

        URI uri = UriComponentsBuilder.fromUriString("https://int.mosip.io/individualIdType/UIN/individualId/1234").build(false).encode().toUri();
        ResidentServiceRestClient client = Mockito.spy(residentServiceRestClient);
        when(residentRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(), Matchers.<Class<AutnTxnResponseDto>>any())).
                thenThrow(new RestClientException(""));

        client.getApi(uri, AutnTxnResponseDto.class);
    }

    @Test
    public void testgetApiObject() throws ApisResourceAccessException {
        AutnTxnResponseDto autnTxnResponseDto = new AutnTxnResponseDto();
        autnTxnResponseDto.setId("ancd");

        when(environment.getProperty(any(String.class))).thenReturn("https://int.mosip.io/");
        ResidentServiceRestClient client = Mockito.spy(residentServiceRestClient);
        doReturn(autnTxnResponseDto).when(client).getApi(any(), any());
        List<String> list = new ArrayList<>();
        list.add("individualIdType");
        list.add("UIN");
        list.add("individualId");
        list.add("1234");


        assertTrue(client.getApi(ApiName.INTERNALAUTHTRANSACTIONS, list, "", null, AutnTxnResponseDto.class).toString().contains("ancd"));
    }

    @Test(expected = ApisResourceAccessException.class)
    public void testgetApiObjectException() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, ApisResourceAccessException {
        AutnTxnResponseDto autnTxnResponseDto = new AutnTxnResponseDto();
        autnTxnResponseDto.setId("ancd");

        when(environment.getProperty(any(String.class))).thenReturn("https://int.mosip.io/");
        ResidentServiceRestClient client = Mockito.spy(residentServiceRestClient);
        doThrow(new ApisResourceAccessException()).when(client).getApi(any(), any());
        List<String> list = new ArrayList<>();
        list.add("individualIdType");
        list.add("UIN");
        list.add("individualId");
        list.add("1234");

        client.getApi(ApiName.INTERNALAUTHTRANSACTIONS, list, "pageFetch,pageStart", "50,1", AutnTxnResponseDto.class);
    }
    
    @Test
    public void testGetApiListQuery() throws ApisResourceAccessException{
    	AutnTxnResponseDto autnTxnResponseDto = new AutnTxnResponseDto();
        autnTxnResponseDto.setId("ancd");
        
        when(environment.getProperty(any(String.class))).thenReturn("https://int.mosip.io/");
        ResidentServiceRestClient client = Mockito.spy(residentServiceRestClient);
        doReturn(autnTxnResponseDto).when(client).getApi(any(), any());
        List<String> list = new ArrayList<>();
        list.add("individualIdType");
        list.add("UIN");
        list.add("individualId");
        list.add("1234");
        
        List<String> queryParamName=new ArrayList<String>();
        queryParamName.add("queryName");
        queryParamName.add("paramName");
        
        List<Object> queryParamValue=new ArrayList<>();
        queryParamValue.add("queryValue");
        queryParamValue.add("paramValue");
        
        assertTrue(client.getApi(ApiName.INTERNALAUTHTRANSACTIONS, list, queryParamName, queryParamValue, AutnTxnResponseDto.class).toString().contains("ancd"));
    }
    
    @Test(expected = ApisResourceAccessException.class)
    public void testGetApiListQueryException() throws ApisResourceAccessException{
    	AutnTxnResponseDto autnTxnResponseDto = new AutnTxnResponseDto();
        autnTxnResponseDto.setId("ancd");
        
        when(environment.getProperty(any(String.class))).thenReturn("https://int.mosip.io/");
        ResidentServiceRestClient client = Mockito.spy(residentServiceRestClient);
        doThrow(new ApisResourceAccessException()).when(client).getApi(any(), any());
        List<String> list = new ArrayList<>();
        list.add("individualIdType");
        list.add("UIN");
        list.add("individualId");
        list.add("1234");
        
        List<String> queryParamName=new ArrayList<String>();
        queryParamName.add("queryName");
        queryParamName.add("paramName");
        
        List<Object> queryParamValue=new ArrayList<>();
        queryParamValue.add("queryValue");
        queryParamValue.add("paramValue");
        
        client.getApi(ApiName.INTERNALAUTHTRANSACTIONS, list, queryParamName, queryParamValue, AutnTxnResponseDto.class);
    }
    
    @Test
    public void testGetApiGenericT() throws Exception{
    	AutnTxnResponseDto autnTxnResponseDto=new AutnTxnResponseDto();
    	autnTxnResponseDto.setId("ancd");
    	
    	when(environment.getProperty(any(String.class))).thenReturn("https://int.mosip.io/");
    	ResidentServiceRestClient client = Mockito.spy(residentServiceRestClient);
        doReturn(autnTxnResponseDto).when(client).getApi(any(), any());
        Map<String, String> pathsegments=new HashMap<String, String>();
        pathsegments.put("individualIdType","mapType");
        pathsegments.put("UIN","mapType");
        pathsegments.put("individualId","mapType");
        pathsegments.put("1234","mapType");
        
        assertTrue(client.getApi(ApiName.INTERNALAUTHTRANSACTIONS, pathsegments, AutnTxnResponseDto.class).toString().contains("ancd"));
    }
    
//    @Test(expected = Exception.class)
//    public void testGetApiGenericTException() throws Exception{
//    	AutnTxnResponseDto autnTxnResponseDto=new AutnTxnResponseDto();
//    	autnTxnResponseDto.setId("ancd");
//    	
////    	when(environment.getProperty(any(String.class))).thenReturn("https://int.mosip.io/");
//    	ResidentServiceRestClient client = Mockito.spy(residentServiceRestClient);
////        doThrow(new Exception()).when(client).getApi(any(), any());
//    	when(residentRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(), Matchers.<Class<AutnTxnResponseDto>>any())).
//        thenThrow(new RestClientException(""));
//    	when(residentServiceRestClient.getApi(any(), any())).thenThrow(new RestClientException(""));
////        Map<String, String> pathsegments=new HashMap<String, String>();
////        pathsegments.put("individualIdType","mapType");
////        pathsegments.put("UIN","mapType");
////        pathsegments.put("individualId","mapType");
////        pathsegments.put("1234","mapType");
//        
//        client.getApi(any(), any(), any());
//    }

    @Test
    public void testpostApi() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, ApisResourceAccessException {
        AutnTxnResponseDto autnTxnResponseDto = new AutnTxnResponseDto();
        autnTxnResponseDto.setId("ancd");

        ResidentServiceRestClient client = Mockito.spy(residentServiceRestClient);
        when(residentRestTemplate.postForObject(any(String.class), any(), Matchers.<Class<AutnTxnResponseDto>>any())).
                thenReturn(autnTxnResponseDto);

        assertTrue(client.postApi("https://int.mosip.io/individualIdType/UIN/individualId/1234", MediaType.APPLICATION_JSON, autnTxnResponseDto, AutnTxnResponseDto.class).toString().contains("ancd"));
    }

    @Test(expected = ApisResourceAccessException.class)
    public void testpostApiException() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, ApisResourceAccessException {
        AutnTxnResponseDto autnTxnResponseDto = new AutnTxnResponseDto();
        autnTxnResponseDto.setId("ancd");

        ResidentServiceRestClient client = Mockito.spy(residentServiceRestClient);
        when(residentRestTemplate.postForObject(any(String.class), any(), Matchers.<Class<AutnTxnResponseDto>>any())).
                thenThrow(new RestClientException(""));

        assertTrue(client.postApi("https://int.mosip.io/individualIdType/UIN/individualId/1234", MediaType.APPLICATION_JSON, autnTxnResponseDto, AutnTxnResponseDto.class).toString().contains("ancd"));
    }

    @Test
    public void testpatchApi() throws Exception {
        AutnTxnResponseDto autnTxnResponseDto = new AutnTxnResponseDto();
        autnTxnResponseDto.setId("ancd");

        ResidentServiceRestClient client = Mockito.spy(residentServiceRestClient);
        when(residentRestTemplate.patchForObject(any(String.class), any(), Matchers.<Class<AutnTxnResponseDto>>any())).
                thenReturn(autnTxnResponseDto);

        assertTrue(client.patchApi("https://int.mosip.io/individualIdType/UIN/individualId/1234", autnTxnResponseDto, AutnTxnResponseDto.class).toString().contains("ancd"));
    }

    @Test(expected = ApisResourceAccessException.class)
    public void testpatchApiException() throws Exception {
        AutnTxnResponseDto autnTxnResponseDto = new AutnTxnResponseDto();
        autnTxnResponseDto.setId("ancd");

        ResidentServiceRestClient client = Mockito.spy(residentServiceRestClient);
        when(residentRestTemplate.patchForObject(any(String.class), any(), Matchers.<Class<AutnTxnResponseDto>>any())).
                thenThrow(new RestClientException(""));

        assertTrue(client.patchApi("https://int.mosip.io/individualIdType/UIN/individualId/1234", autnTxnResponseDto, AutnTxnResponseDto.class).toString().contains("ancd"));
    }

    @Test
    public void testputApi() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, ApisResourceAccessException {
        AutnTxnResponseDto autnTxnResponseDto = new AutnTxnResponseDto();
        autnTxnResponseDto.setId("ancd");
        ResponseEntity<AutnTxnResponseDto> obj = new ResponseEntity<AutnTxnResponseDto>(autnTxnResponseDto, HttpStatus.OK);

        ResidentServiceRestClient client = Mockito.spy(residentServiceRestClient);
        when(residentRestTemplate.exchange(any(String.class), any(HttpMethod.class), any(), Matchers.<Class<AutnTxnResponseDto>>any())).
                thenReturn(obj);

        assertTrue(client.putApi("https://int.mosip.io/individualIdType/UIN/individualId/1234", autnTxnResponseDto, AutnTxnResponseDto.class, MediaType.APPLICATION_JSON).toString().contains("ancd"));
    }

    @Test(expected = ApisResourceAccessException.class)
    public void testputApiException() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, ApisResourceAccessException {
        AutnTxnResponseDto autnTxnResponseDto = new AutnTxnResponseDto();
        autnTxnResponseDto.setId("ancd");

        ResidentServiceRestClient client = Mockito.spy(residentServiceRestClient);
        when(residentRestTemplate.exchange(any(String.class), any(HttpMethod.class), any(), Matchers.<Class<AutnTxnResponseDto>>any())).
                thenThrow(new RestClientException(""));

        client.putApi("https://int.mosip.io/individualIdType/UIN/individualId/1234", autnTxnResponseDto, AutnTxnResponseDto.class, MediaType.APPLICATION_JSON);
    }

}
