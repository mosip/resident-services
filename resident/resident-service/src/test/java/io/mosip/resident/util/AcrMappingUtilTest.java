package io.mosip.resident.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for AcrMappingUtil
 *
 * @author Kamesh Shekhar Prasad
 */

@RunWith(MockitoJUnitRunner.class)
public class AcrMappingUtilTest {

    private AcrMappingUtil acrMappingUtil;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RestTemplate residentRestTemplate;

    @Before
    public void setUp() {
        acrMappingUtil = new AcrMappingUtil();

        // inject mocks
        ReflectionTestUtils.setField(acrMappingUtil, "objMapper", objectMapper);
        ReflectionTestUtils.setField(acrMappingUtil, "residentRestTemplate", residentRestTemplate);

        // inject @Value fields
        ReflectionTestUtils.setField(acrMappingUtil, "configServerFileStorageURL", "http://config/");
        ReflectionTestUtils.setField(acrMappingUtil, "amrAcrJsonFile", "amr-acr.json");
    }

    @Test
    public void testGetAmrAcrMappingSuccess() throws Exception {

        // raw JSON as String returned from RestTemplate
        String json = "{\"acr_amr\":{\"acr1\":[\"amr1\"],\"acr2\":[\"amr2\"]}}";

        when(residentRestTemplate.getForObject("http://config/amr-acr.json", String.class))
                .thenReturn(json);

        // prepare ObjectMapper response: make sure list values are ArrayList instances
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("acr1", new ArrayList<>(java.util.List.of("amr1")));
        innerMap.put("acr2", new ArrayList<>(java.util.List.of("amr2")));

        Map<String, Object> topLevelMap = new HashMap<>();
        topLevelMap.put("acr_amr", innerMap);

        when(objectMapper.readValue(json.getBytes(UTF_8), Map.class)).thenReturn(topLevelMap);

        // call method
        Map<String, String> result = acrMappingUtil.getAmrAcrMapping();

        // assertions
        assertEquals(2, result.size());
        assertEquals("amr1", result.get("acr1"));
        assertEquals("amr2", result.get("acr2"));

        verify(residentRestTemplate, times(1)).getForObject("http://config/amr-acr.json", String.class);
        verify(objectMapper, times(1)).readValue(any(byte[].class), eq(Map.class));
    }

    @Test
    public void testGetAmrAcrMappingWhenIOExceptionThrownShouldThrowResidentServiceCheckedException() throws Exception {
        String json = "{\"acr_amr\":{\"acr1\":[\"amr1\"]}}";

        when(residentRestTemplate.getForObject(anyString(), eq(String.class))).thenReturn(json);

        when(objectMapper.readValue(any(byte[].class), eq(Map.class)))
                .thenThrow(new IOException("JSON parse error"));

        try {
            acrMappingUtil.getAmrAcrMapping();
            fail("Expected ResidentServiceCheckedException");
        } catch (ResidentServiceCheckedException ex) {
            assertEquals(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(), ex.getErrorCode());
        }

        verify(residentRestTemplate, times(1)).getForObject("http://config/amr-acr.json", String.class);
        verify(objectMapper, times(1)).readValue(any(byte[].class), eq(Map.class));
    }
}
