package io.mosip.resident.util;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProxyMasterDataServiceUtility
 * @author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
public class ProxyMasterDataServiceUtilityTest {

    private ProxyMasterDataServiceUtility proxyMasterDataServiceUtility;

    @Mock
    private DynamicFieldBasedOnLangCodeAndFieldName dynamicFieldBasedOnLangCodeAndFieldName;

    @Before
    public void setUp() {
        proxyMasterDataServiceUtility = new ProxyMasterDataServiceUtility();
        ReflectionTestUtils.setField(proxyMasterDataServiceUtility,
                "dynamicFieldBasedOnLangCodeAndFieldName",
                dynamicFieldBasedOnLangCodeAndFieldName);
    }

    @Test
    public void testGetDynamicFieldBasedOnLangCodeAndFieldName_success() throws Exception {
        String fieldName = "address";
        String langCode = "en";
        boolean withValue = true;

        ResponseWrapper expectedResponse = new ResponseWrapper<>();
        expectedResponse.setResponse("OK");

        when(dynamicFieldBasedOnLangCodeAndFieldName
                .getDynamicFieldBasedOnLangCodeAndFieldName(eq(fieldName), eq(langCode), eq(withValue)))
                .thenReturn(expectedResponse);

        ResponseWrapper<?> result =
                proxyMasterDataServiceUtility.getDynamicFieldBasedOnLangCodeAndFieldName(fieldName, langCode, withValue);

        assertEquals(expectedResponse, result);
        verify(dynamicFieldBasedOnLangCodeAndFieldName, times(1))
                .getDynamicFieldBasedOnLangCodeAndFieldName(eq(fieldName), eq(langCode), eq(withValue));
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetDynamicFieldBasedOnLangCodeAndFieldName_whenExceptionThrown_shouldPropagate()
            throws Exception {

        when(dynamicFieldBasedOnLangCodeAndFieldName
                .getDynamicFieldBasedOnLangCodeAndFieldName(anyString(), anyString(), anyBoolean()))
                .thenThrow(new ResidentServiceCheckedException("ERR", "Test error"));

        proxyMasterDataServiceUtility.getDynamicFieldBasedOnLangCodeAndFieldName("field", "en", false);
    }
}
