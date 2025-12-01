package io.mosip.resident.util;

import io.mosip.resident.constant.ResidentConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthTypeCodeTemplateData
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthTypeCodeTemplateDataTest {

    private AuthTypeCodeTemplateData authTypeCodeTemplateData;

    @Mock
    private TemplateValueFromTemplateTypeCodeAndLangCode templateValueFromTemplateTypeCodeAndLangCode;

    @Mock
    private EventStatusBasedOnLangCode eventStatusBasedOnLangCode;

    @Before
    public void setUp() {
        authTypeCodeTemplateData = new AuthTypeCodeTemplateData();
        ReflectionTestUtils.setField(authTypeCodeTemplateData, "templateValueFromTemplateTypeCodeAndLangCode",
                templateValueFromTemplateTypeCodeAndLangCode);
        ReflectionTestUtils.setField(authTypeCodeTemplateData, "eventStatusBasedOnLangCode",
                eventStatusBasedOnLangCode);
    }

    @Test
    public void getAuthTypeCodeTemplateData_nullAuthType_returnsEmptyString() {
        String result = authTypeCodeTemplateData.getAuthTypeCodeTemplateData(null, null, "en");
        assertEquals("", result);
        verifyNoInteractions(eventStatusBasedOnLangCode, templateValueFromTemplateTypeCodeAndLangCode);
    }

    @Test
    public void getAuthTypeCodeTemplateData_singleAuthType_noStatus_returnsTemplateValue() {
        String authType = "AT1";
        String language = "en";
        String propertyKey = String.format("resident.auth-type-code.%s.code", authType);
        String templateTypeCode = "TEMPLATE_AT1";
        String templateValue = "Auth Label AT1";

        when(eventStatusBasedOnLangCode.getTemplateTypeCode(eq(propertyKey))).thenReturn(templateTypeCode);
        when(templateValueFromTemplateTypeCodeAndLangCode.getTemplateValueFromTemplateTypeCodeAndLangCode(eq(language), eq(templateTypeCode)))
                .thenReturn(templateValue);

        String result = authTypeCodeTemplateData.getAuthTypeCodeTemplateData(authType, null, language);
        assertEquals(templateValue, result);

        verify(eventStatusBasedOnLangCode, times(1)).getTemplateTypeCode(eq(propertyKey));
        verify(templateValueFromTemplateTypeCodeAndLangCode, times(1))
                .getTemplateValueFromTemplateTypeCodeAndLangCode(eq(language), eq(templateTypeCode));
    }

    @Test
    public void getAuthTypeCodeTemplateData_multipleAuthTypes_joinedByDelimiter_returnsJoinedValues() {
        String auth1 = "AT1";
        String auth2 = "AT2";
        String authTypeFromDB = String.join(ResidentConstants.ATTRIBUTE_LIST_DELIMITER, auth1, auth2);
        String language = "en";

        String prop1 = String.format("resident.auth-type-code.%s.code", auth1);
        String prop2 = String.format("resident.auth-type-code.%s.code", auth2);

        String templateType1 = "T1";
        String templateType2 = "T2";

        String value1 = "Label1";
        String value2 = "Label2";

        when(eventStatusBasedOnLangCode.getTemplateTypeCode(eq(prop1))).thenReturn(templateType1);
        when(eventStatusBasedOnLangCode.getTemplateTypeCode(eq(prop2))).thenReturn(templateType2);

        when(templateValueFromTemplateTypeCodeAndLangCode.getTemplateValueFromTemplateTypeCodeAndLangCode(eq(language), eq(templateType1)))
                .thenReturn(value1);
        when(templateValueFromTemplateTypeCodeAndLangCode.getTemplateValueFromTemplateTypeCodeAndLangCode(eq(language), eq(templateType2)))
                .thenReturn(value2);

        String expected = String.join(ResidentConstants.UI_ATTRIBUTE_DATA_DELIMITER, List.of(value1, value2));

        String result = authTypeCodeTemplateData.getAuthTypeCodeTemplateData(authTypeFromDB, null, language);

        assertEquals(expected, result);

        verify(eventStatusBasedOnLangCode, times(1)).getTemplateTypeCode(eq(prop1));
        verify(eventStatusBasedOnLangCode, times(1)).getTemplateTypeCode(eq(prop2));
        verify(templateValueFromTemplateTypeCodeAndLangCode, times(1))
                .getTemplateValueFromTemplateTypeCodeAndLangCode(eq(language), eq(templateType1));
        verify(templateValueFromTemplateTypeCodeAndLangCode, times(1))
                .getTemplateValueFromTemplateTypeCodeAndLangCode(eq(language), eq(templateType2));
    }

    @Test
    public void getAuthTypeCodeTemplateData_withStatus_usesIdAuthRequestTypeDescrProperty() {
        String authType = "ATX";
        String statusCode = "S1";
        String language = "en";

        String expectedProperty = String.format("resident.id-auth.request-type.%s.%s.descr", authType, statusCode);
        String templateTypeCode = "T_ATX_S1";
        String templateValue = "Label ATX S1";

        when(eventStatusBasedOnLangCode.getTemplateTypeCode(eq(expectedProperty))).thenReturn(templateTypeCode);
        when(templateValueFromTemplateTypeCodeAndLangCode.getTemplateValueFromTemplateTypeCodeAndLangCode(eq(language), eq(templateTypeCode)))
                .thenReturn(templateValue);

        String result = authTypeCodeTemplateData.getAuthTypeCodeTemplateData(authType, statusCode, language);

        assertEquals(templateValue, result);

        verify(eventStatusBasedOnLangCode, times(1)).getTemplateTypeCode(eq(expectedProperty));
        verify(templateValueFromTemplateTypeCodeAndLangCode, times(1))
                .getTemplateValueFromTemplateTypeCodeAndLangCode(eq(language), eq(templateTypeCode));
    }

    @Test
    public void getAuthTypeCodeTemplateData_eventStatusReturnsNull_usesFallbackUnknownProperty() {
        String authType = "MISSING";
        String language = "en";
        String prop = String.format("resident.auth-type-code.%s.code", authType);

        // first call returns null -> missing property
        when(eventStatusBasedOnLangCode.getTemplateTypeCode(eq(prop))).thenReturn(null);

        // fallback should query ResidentConstants.RESIDENT_UNKNOWN_TEMPLATE_PROPERTY
        when(eventStatusBasedOnLangCode.getTemplateTypeCode(eq(ResidentConstants.RESIDENT_UNKNOWN_TEMPLATE_PROPERTY)))
                .thenReturn("UNKNOWN_TEMPLATE");

        when(templateValueFromTemplateTypeCodeAndLangCode.getTemplateValueFromTemplateTypeCodeAndLangCode(eq(language), eq("UNKNOWN_TEMPLATE")))
                .thenReturn("Unknown Label");

        String result = authTypeCodeTemplateData.getAuthTypeCodeTemplateData(authType, null, language);

        assertEquals("Unknown Label", result);

        verify(eventStatusBasedOnLangCode, times(1)).getTemplateTypeCode(eq(prop));
        verify(eventStatusBasedOnLangCode, times(1)).getTemplateTypeCode(eq(ResidentConstants.RESIDENT_UNKNOWN_TEMPLATE_PROPERTY));
        verify(templateValueFromTemplateTypeCodeAndLangCode, times(1))
                .getTemplateValueFromTemplateTypeCodeAndLangCode(eq(language), eq("UNKNOWN_TEMPLATE"));
    }
}
