package io.mosip.resident.util;

import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.handler.service.ResidentConfigService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author Kamesh
 */

@RunWith(MockitoJUnitRunner.class)
public class AttributeDisplayTextTest {

    @InjectMocks
    private AttributesDisplayText attributesDisplayText = new AttributesDisplayText();

    @Mock
    ResidentConfigService residentConfigService;

    private Map<String, Map<String, Object>> mockUISchemaDataMap;

    @Before
    public void setUp() {
        // Mock UI Schema Data
        mockUISchemaDataMap = new HashMap<>();

        Map<String, Object> attributeMap = new HashMap<>();
        attributeMap.put(ResidentConstants.LABEL, "Mocked Label");

        Map<String, String> formatOptionMap = new HashMap<>();
        formatOptionMap.put("format1", "Formatted 1");
        formatOptionMap.put("format2", "Formatted 2");

        attributeMap.put(ResidentConstants.FORMAT_OPTION, formatOptionMap);

        mockUISchemaDataMap.put("attr1", attributeMap);

        // Mock ResidentConfigService response
        when(residentConfigService.getUISchemaCacheableData(Mockito.anyString()))
                .thenReturn(Map.of("en", mockUISchemaDataMap));
    }

    @Test
    public void testGetAttributesDisplayText_withSchemaType() {
        String attributesFromDB = "en:format1,format2";
        String languageCode = "en";
        RequestType requestType = RequestType.UPDATE_MY_UIN;

        String result = attributesDisplayText.getAttributesDisplayText(attributesFromDB, languageCode, requestType);

        assertEquals("en", result);
    }

    @Test
    public void testGetAttributesDisplayText_withoutSchemaType() {
        String attributesFromDB = "attr1";
        String languageCode = "en";
        RequestType requestType = RequestType.UPDATE_MY_UIN;
        String result = attributesDisplayText.getAttributesDisplayText(attributesFromDB, languageCode, requestType);

        assertEquals("Mocked Label", result);
    }

    @Test
    public void testGetAttributesDisplayText_withNullAttributes() {
        String attributesFromDB = null;
        String languageCode = "en";
        RequestType requestType = RequestType.UPDATE_MY_UIN;

        String result = attributesDisplayText.getAttributesDisplayText(attributesFromDB, languageCode, requestType);

        assertEquals("", result);
    }

    @Test
    public void testGetAttributesDisplayText_withEmptyAttributes() {
        String attributesFromDB = "";
        String languageCode = "en";
        RequestType requestType = RequestType.UPDATE_MY_UIN;

        String result = attributesDisplayText.getAttributesDisplayText(attributesFromDB, languageCode, requestType);

        assertEquals("", result);
    }

    @Test
    public void testGetAttributesDisplayText(){
        Map<String, Map<String, Map<String, Object>>> keyMap = new HashMap<>();
        Map<String, Map<String, Object>> attributeMap = new HashMap<>();
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("testKey", "testobj");
        attributeMap.put("key", stringObjectMap);
        keyMap.put("eng", attributeMap);
        when(residentConfigService.getUISchemaCacheableData(Mockito.anyString())).thenReturn(keyMap);
        assertEquals("test",
                attributesDisplayText.getAttributesDisplayText("test", "eng", RequestType.UPDATE_MY_UIN));
    }
}
