package io.mosip.resident.util;

import io.mosip.resident.constant.EventStatus;
import io.mosip.resident.constant.ResidentConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EventStatusBasedOnLangCode
 * @author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
public class EventStatusBasedOnLangCodeTest {

    private EventStatusBasedOnLangCode eventStatusBasedOnLangCode;

    @Mock
    private Environment environment;

    @Mock
    private TemplateValueFromTemplateTypeCodeAndLangCode templateValueFromTemplateTypeCodeAndLangCode;

    @Before
    public void setUp() {
        eventStatusBasedOnLangCode = new EventStatusBasedOnLangCode();
        ReflectionTestUtils.setField(eventStatusBasedOnLangCode, "environment", environment);
        ReflectionTestUtils.setField(eventStatusBasedOnLangCode, "templateValueFromTemplateTypeCodeAndLangCode",
                templateValueFromTemplateTypeCodeAndLangCode);
    }

    @Test
    public void getTemplateTypeCodeDelegatesToEnvironment() {
        String propertyKey = "resident.event.status.APPROVED.template.property";
        when(environment.getProperty(eq(propertyKey))).thenReturn("TEMPLATE_APPROVED");

        String result = eventStatusBasedOnLangCode.getTemplateTypeCode(propertyKey);

        assertEquals("TEMPLATE_APPROVED", result);
        verify(environment, times(1)).getProperty(eq(propertyKey));
    }

    @Test
    public void getEventStatusBasedOnLangcodeWhenPropertyExistsReturnsTemplateValue() {
        EventStatus eventStatus = EventStatus.SUCCESS;
        String languageCode = "en";
        String propKey = String.format("resident.event.status.%s.template.property", eventStatus.name());
        String templateTypeCode = "TEMPLATE_APPROVED";
        String resolvedValue = "Approved Label";

        when(environment.getProperty(eq(propKey))).thenReturn(templateTypeCode);
        when(templateValueFromTemplateTypeCodeAndLangCode.getTemplateValueFromTemplateTypeCodeAndLangCode(eq(languageCode), eq(templateTypeCode)))
                .thenReturn(resolvedValue);

        String result = eventStatusBasedOnLangCode.getEventStatusBasedOnLangcode(eventStatus, languageCode);

        assertEquals(resolvedValue, result);
        verify(environment, times(1)).getProperty(eq(propKey));
        verify(templateValueFromTemplateTypeCodeAndLangCode, times(1))
                .getTemplateValueFromTemplateTypeCodeAndLangCode(eq(languageCode), eq(templateTypeCode));
    }

    @Test
    public void getEventStatusBasedOnLangcodeWhenPropertyMissingUsesUnknownFallback() {
        EventStatus eventStatus = EventStatus.FAILED;
        String languageCode = "en";
        String propKey = String.format("resident.event.status.%s.template.property", eventStatus.name());
        String unknownPropKey = ResidentConstants.RESIDENT_UNKNOWN_TEMPLATE_PROPERTY;
        String fallbackTemplateTypeCode = "TEMPLATE_UNKNOWN";
        String fallbackValue = "Unknown Label";

        // main property missing
        when(environment.getProperty(eq(propKey))).thenReturn(null);
        // fallback property present
        when(environment.getProperty(eq(unknownPropKey))).thenReturn(fallbackTemplateTypeCode);
        when(templateValueFromTemplateTypeCodeAndLangCode.getTemplateValueFromTemplateTypeCodeAndLangCode(eq(languageCode), eq(fallbackTemplateTypeCode)))
                .thenReturn(fallbackValue);

        String result = eventStatusBasedOnLangCode.getEventStatusBasedOnLangcode(eventStatus, languageCode);

        assertEquals(fallbackValue, result);
        verify(environment, times(1)).getProperty(eq(propKey));
        verify(environment, times(1)).getProperty(eq(unknownPropKey));
        verify(templateValueFromTemplateTypeCodeAndLangCode, times(1))
                .getTemplateValueFromTemplateTypeCodeAndLangCode(eq(languageCode), eq(fallbackTemplateTypeCode));
    }
}
