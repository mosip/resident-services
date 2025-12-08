package io.mosip.resident.util;

import io.mosip.resident.constant.ResidentConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AttributeBasedOnLangCode
 */
@RunWith(MockitoJUnitRunner.class)
public class AttributeBasedOnLangCodeTest {

    private AttributeBasedOnLangCode attributeBasedOnLangCode;

    @Mock
    private EventStatusBasedOnLangCode eventStatusBasedOnLangCode;

    @Mock
    private TemplateValueFromTemplateTypeCodeAndLangCode templateValueFromTemplateTypeCodeAndLangCode;

    @Before
    public void setUp() {
        attributeBasedOnLangCode = new AttributeBasedOnLangCode();

        // inject mocks into the instance
        ReflectionTestUtils.setField(attributeBasedOnLangCode, "eventStatusBasedOnLangCode", eventStatusBasedOnLangCode);
        ReflectionTestUtils.setField(attributeBasedOnLangCode, "templateValueFromTemplateTypeCodeAndLangCode",
                templateValueFromTemplateTypeCodeAndLangCode);
    }

    @Test
    public void getAttributeBasedOnLangcodeWhenTemplateTypeExistsReturnsTemplateValue() {
        String attributeName = "name";
        String languageCode = "en";
        String propertyKey = String.format("resident.%s.template.property.attribute.list", attributeName);
        String foundTemplateType = "TEMPLATE_ABC";
        String expectedValue = "Label in English";

        // stub to return a template type code
        when(eventStatusBasedOnLangCode.getTemplateTypeCode(eq(propertyKey))).thenReturn(foundTemplateType);

        // stub to return template value for given language and template type
        when(templateValueFromTemplateTypeCodeAndLangCode
                .getTemplateValueFromTemplateTypeCodeAndLangCode(eq(languageCode), eq(foundTemplateType)))
                .thenReturn(expectedValue);

        String result = attributeBasedOnLangCode.getAttributeBasedOnLangcode(attributeName, languageCode);

        assertEquals(expectedValue, result);

        verify(eventStatusBasedOnLangCode, times(1)).getTemplateTypeCode(eq(propertyKey));
        verify(templateValueFromTemplateTypeCodeAndLangCode, times(1))
                .getTemplateValueFromTemplateTypeCodeAndLangCode(eq(languageCode), eq(foundTemplateType));
    }

    @Test
    public void getAttributeBasedOnLangcodeWhenTemplateTypeMissingUsesUnknownPropertyAndReturnsValue() {
        String attributeName = "missingAttr";
        String languageCode = "en";
        String propertyKey = String.format("resident.%s.template.property.attribute.list", attributeName);
        String unknownPropertyKey = ResidentConstants.RESIDENT_UNKNOWN_TEMPLATE_PROPERTY;
        String unknownTemplateType = "UNKNOWN_TEMPLATE";
        String expectedFallbackValue = "Unknown Label";

        // first call for attribute property returns null -> missing
        when(eventStatusBasedOnLangCode.getTemplateTypeCode(eq(propertyKey))).thenReturn(null);
        // fallback should query unknown property and return a template type
        when(eventStatusBasedOnLangCode.getTemplateTypeCode(eq(unknownPropertyKey))).thenReturn(unknownTemplateType);

        // template value for fallback
        when(templateValueFromTemplateTypeCodeAndLangCode
                .getTemplateValueFromTemplateTypeCodeAndLangCode(eq(languageCode), eq(unknownTemplateType)))
                .thenReturn(expectedFallbackValue);

        String result = attributeBasedOnLangCode.getAttributeBasedOnLangcode(attributeName, languageCode);

        assertEquals(expectedFallbackValue, result);

        verify(eventStatusBasedOnLangCode, times(1)).getTemplateTypeCode(eq(propertyKey));
        verify(eventStatusBasedOnLangCode, times(1)).getTemplateTypeCode(eq(unknownPropertyKey));
        verify(templateValueFromTemplateTypeCodeAndLangCode, times(1))
                .getTemplateValueFromTemplateTypeCodeAndLangCode(eq(languageCode), eq(unknownTemplateType));
    }
}
