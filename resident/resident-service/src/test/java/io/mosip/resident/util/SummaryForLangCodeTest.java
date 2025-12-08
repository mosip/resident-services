package io.mosip.resident.util;

import io.mosip.resident.constant.*;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.service.impl.DescriptionForLangCode;
import io.mosip.resident.service.impl.ReplacePlaceholderValueInTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SummaryForLangCode
 * @author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
public class SummaryForLangCodeTest {

    private SummaryForLangCode summaryForLangCode;

    @Mock
    private DescriptionForLangCode descriptionForLangCode;

    @Mock
    private TemplateValueFromTemplateTypeCodeAndLangCode templateValueFromTemplateTypeCodeAndLangCode;

    @Mock
    private ReplacePlaceholderValueInTemplate replacePlaceholderValueInTemplate;

    @Mock
    private EventStatusBasedOnLangCode eventStatusBasedOnLangCode;

    @Before
    public void setUp() {
        summaryForLangCode = new SummaryForLangCode();

        ReflectionTestUtils.setField(summaryForLangCode, "descriptionForLangCode", descriptionForLangCode);
        ReflectionTestUtils.setField(summaryForLangCode, "templateValueFromTemplateTypeCodeAndLangCode",
                templateValueFromTemplateTypeCodeAndLangCode);
        ReflectionTestUtils.setField(summaryForLangCode, "replacePlaceholderValueInTemplate",
                replacePlaceholderValueInTemplate);
        ReflectionTestUtils.setField(summaryForLangCode, "eventStatusBasedOnLangCode", eventStatusBasedOnLangCode);
    }

    @Test
    public void getSummaryTemplateTypeCodeDelegatesToEventStatusBasedOnLangCode() {
        // Arrange
        RequestType requestType = RequestType.UPDATE_MY_UIN; // present in project
        TemplateType templateType = TemplateType.SUCCESS;
        String expectedTemplateTypeCode = "TEMPLATE_CODE_1";

        // The method composes a property using requestType and templateType; we simply mock the eventStatusBasedOnLangCode
        when(eventStatusBasedOnLangCode.getTemplateTypeCode(anyString())).thenReturn(expectedTemplateTypeCode);

        // Act
        String result = summaryForLangCode.getSummaryTemplateTypeCode(requestType, templateType);

        // Assert
        assertEquals(expectedTemplateTypeCode, result);
        verify(eventStatusBasedOnLangCode, times(1)).getTemplateTypeCode(anyString());
    }

    @Test
    public void getSummaryForLangCodeStatusSuccessUsesSuccessTemplateType() throws Exception {
        // Arrange
        ResidentTransactionEntity entity = new ResidentTransactionEntity();
        // entity status may be anything here
        String lang = "en";
        String statusCode = EventStatus.SUCCESS.name();
        RequestType requestType = RequestType.UPDATE_MY_UIN;

        String templateTypeCode = "T_SUCCESS";
        String fileText = "FileTextSuccess";
        String replaced = "ReplacedSuccess";

        when(eventStatusBasedOnLangCode.getTemplateTypeCode(anyString())).thenReturn(templateTypeCode);
        when(templateValueFromTemplateTypeCodeAndLangCode.getTemplateValueFromTemplateTypeCodeAndLangCode(eq(lang), eq(templateTypeCode)))
                .thenReturn(fileText);
        when(replacePlaceholderValueInTemplate.replacePlaceholderValueInTemplate(eq(entity), eq(fileText), eq(requestType), eq(lang)))
                .thenReturn(replaced);

        // Act
        String result = summaryForLangCode.getSummaryForLangCode(entity, lang, statusCode, requestType);

        // Assert
        assertEquals(replaced, result);
        verify(eventStatusBasedOnLangCode, times(1)).getTemplateTypeCode(anyString());
        verify(templateValueFromTemplateTypeCodeAndLangCode, times(1))
                .getTemplateValueFromTemplateTypeCodeAndLangCode(eq(lang), eq(templateTypeCode));
        verify(replacePlaceholderValueInTemplate, times(1))
                .replacePlaceholderValueInTemplate(eq(entity), eq(fileText), eq(requestType), eq(lang));
    }

    @Test
    public void getSummaryForLangCodeStatusCanceledUsesCanceledTemplateType() throws Exception {
        // Arrange
        ResidentTransactionEntity entity = new ResidentTransactionEntity();
        String lang = "en";
        String statusCode = EventStatusCanceled.CANCELED.name();
        RequestType requestType = RequestType.UPDATE_MY_UIN;

        String templateTypeCode = "T_CANCEL";
        String fileText = "FileTextCancel";
        String replaced = "ReplacedCancel";

        when(eventStatusBasedOnLangCode.getTemplateTypeCode(anyString())).thenReturn(templateTypeCode);
        when(templateValueFromTemplateTypeCodeAndLangCode.getTemplateValueFromTemplateTypeCodeAndLangCode(eq(lang), eq(templateTypeCode)))
                .thenReturn(fileText);
        when(replacePlaceholderValueInTemplate.replacePlaceholderValueInTemplate(eq(entity), eq(fileText), eq(requestType), eq(lang)))
                .thenReturn(replaced);

        // Act
        String result = summaryForLangCode.getSummaryForLangCode(entity, lang, statusCode, requestType);

        // Assert
        assertEquals(replaced, result);
        verify(eventStatusBasedOnLangCode, times(1)).getTemplateTypeCode(anyString());
        verify(templateValueFromTemplateTypeCodeAndLangCode, times(1))
                .getTemplateValueFromTemplateTypeCodeAndLangCode(eq(lang), eq(templateTypeCode));
        verify(replacePlaceholderValueInTemplate, times(1))
                .replacePlaceholderValueInTemplate(eq(entity), eq(fileText), eq(requestType), eq(lang));
    }

    @Test
    public void getSummaryForLangCodeResidentStatusIdentityUpdatedUsesRegprocSuccess() throws Exception {
        // Arrange
        ResidentTransactionEntity entity = new ResidentTransactionEntity();
        entity.setStatusCode(EventStatusInProgress.IDENTITY_UPDATED.name()); // triggers REGPROC_SUCCESS branch
        String lang = "en";
        String statusCode = "SOME_OTHER_STATUS";
        RequestType requestType = RequestType.UPDATE_MY_UIN;

        String templateTypeCode = "T_REGPROC";
        String fileText = "FileTextRegproc";
        String replaced = "ReplacedRegproc";

        when(eventStatusBasedOnLangCode.getTemplateTypeCode(anyString())).thenReturn(templateTypeCode);
        when(templateValueFromTemplateTypeCodeAndLangCode.getTemplateValueFromTemplateTypeCodeAndLangCode(eq(lang), eq(templateTypeCode)))
                .thenReturn(fileText);
        when(replacePlaceholderValueInTemplate.replacePlaceholderValueInTemplate(eq(entity), eq(fileText), eq(requestType), eq(lang)))
                .thenReturn(replaced);

        // Act
        String result = summaryForLangCode.getSummaryForLangCode(entity, lang, statusCode, requestType);

        // Assert
        assertEquals(replaced, result);
        verify(eventStatusBasedOnLangCode, times(1)).getTemplateTypeCode(anyString());
        verify(templateValueFromTemplateTypeCodeAndLangCode, times(1))
                .getTemplateValueFromTemplateTypeCodeAndLangCode(eq(lang), eq(templateTypeCode));
        verify(replacePlaceholderValueInTemplate, times(1))
                .replacePlaceholderValueInTemplate(eq(entity), eq(fileText), eq(requestType), eq(lang));
    }

    @Test
    public void getSummaryForLangCodeFallbackUsesDescriptionForLangCode() throws Exception {
        // Arrange: statusCode not SUCCESS, not CANCELED, and resident entity status not IDENTITY_UPDATED
        ResidentTransactionEntity entity = new ResidentTransactionEntity();
        entity.setStatusCode("OTHER_STATUS");
        String lang = "en";
        String statusCode = "SOME_STATUS";
        RequestType requestType = RequestType.UPDATE_MY_UIN;

        String description = "DescValue";

        when(descriptionForLangCode.getDescriptionForLangCode(eq(entity), eq(lang), eq(statusCode), eq(requestType)))
                .thenReturn(description);

        // Act
        String result = summaryForLangCode.getSummaryForLangCode(entity, lang, statusCode, requestType);

        // Assert
        assertEquals(description, result);
        verify(descriptionForLangCode, times(1)).getDescriptionForLangCode(eq(entity), eq(lang), eq(statusCode), eq(requestType));
        verifyNoInteractions(templateValueFromTemplateTypeCodeAndLangCode, replacePlaceholderValueInTemplate);
    }
}
