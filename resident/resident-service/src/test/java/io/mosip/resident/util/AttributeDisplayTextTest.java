package io.mosip.resident.util;

import io.mosip.resident.constant.RequestType;
import io.mosip.resident.handler.service.ResidentConfigService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Kamesh
 */

@RunWith(MockitoJUnitRunner.class)
public class AttributeDisplayTextTest {

    @InjectMocks
    private AttributesDisplayText attributesDisplayText = new AttributesDisplayText();

    @Mock
    ResidentConfigService residentConfigService;

    @Test
    public void testGetAttributesDisplayText(){
        Map<String, Map<String, Map<String, Object>>> keyMap = new HashMap<>();
        Map<String, Map<String, Object>> attributeMap = new HashMap<>();
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("testKey", "testobj");
        attributeMap.put("key", stringObjectMap);
        keyMap.put("eng", attributeMap);
        Mockito.when(residentConfigService.getUISchemaCacheableData(Mockito.anyString())).thenReturn(keyMap);
        assertEquals("test",
                attributesDisplayText.getAttributesDisplayText("test", "eng", RequestType.UPDATE_MY_UIN));
    }
}
