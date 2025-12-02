package io.mosip.resident.service.impl;

import io.mosip.resident.constant.*;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.util.PurposeTemplateTypeCode;
import io.mosip.resident.util.TemplateValueFromTemplateTypeCodeAndLangCode;
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
 * Unit tests for DescriptionForLangCode
 * @author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
public class DescriptionForLangCodeTest {

    private DescriptionForLangCode descriptionForLangCode;

    @Mock
    private ReplacePlaceholderValueInTemplate replacePlaceholderValueInTemplate;

    @Mock
    private TemplateValueFromTemplateTypeCodeAndLangCode templateValueFromTemplateTypeCodeAndLangCode;

    @Mock
    private PurposeTemplateTypeCode purposeTemplateTypeCode;

    @Before
    public void setUp() {
        descriptionForLangCode = new DescriptionForLangCode();
        ReflectionTestUtils.setField(descriptionForLangCode, "replacePlaceholderValueInTemplate", replacePlaceholderValueInTemplate);
        ReflectionTestUtils.setField(descriptionForLangCode, "templateValueFromTemplateTypeCodeAndLangCode", templateValueFromTemplateTypeCodeAndLangCode);
        ReflectionTestUtils.setField(descriptionForLangCode, "purposeTemplateTypeCode", purposeTemplateTypeCode);
    }

    @Test
    public void getDescriptionForLangCode_whenStatusSuccess_usesSuccessTemplate() throws Exception {
        ResidentTransactionEntity entity = new ResidentTransactionEntity();
        String lang = "en";
        String statusCode = EventStatus.SUCCESS.name();
        RequestType requestType = RequestType.UPDATE_MY_UIN;

        String templateTypeCode = "T_SUCCESS";
        String fileText = "SuccessTemplate";
        String replaced = "SuccessReplaced";

        when(purposeTemplateTypeCode.getPurposeTemplateTypeCode(eq(requestType), eq(TemplateType.SUCCESS)))
                .thenReturn(templateTypeCode);
        when(templateValueFromTemplateTypeCodeAndLangCode.getTemplateValueFromTemplateTypeCodeAndLangCode(eq(lang), eq(templateTypeCode)))
                .thenReturn(fileText);
        when(replacePlaceholderValueInTemplate.replacePlaceholderValueInTemplate(eq(entity), eq(fileText), eq(requestType), eq(lang)))
                .thenReturn(replaced);

        String result = descriptionForLangCode.getDescriptionForLangCode(entity, lang, statusCode, requestType);

        assertEquals(replaced, result);
        verify(purposeTemplateTypeCode, times(1)).getPurposeTemplateTypeCode(eq(requestType), eq(TemplateType.SUCCESS));
        verify(templateValueFromTemplateTypeCodeAndLangCode, times(1))
                .getTemplateValueFromTemplateTypeCodeAndLangCode(eq(lang), eq(templateTypeCode));
        verify(replacePlaceholderValueInTemplate, times(1))
                .replacePlaceholderValueInTemplate(eq(entity), eq(fileText), eq(requestType), eq(lang));
    }

    @Test
    public void getDescriptionForLangCode_whenStatusCanceled_usesCanceledTemplate() throws Exception {
        ResidentTransactionEntity entity = new ResidentTransactionEntity();
        String lang = "en";
        String statusCode = EventStatusCanceled.CANCELED.name();
        RequestType requestType = RequestType.UPDATE_MY_UIN;

        String templateTypeCode = "T_CANCEL";
        String fileText = "CancelTemplate";
        String replaced = "CancelReplaced";

        when(purposeTemplateTypeCode.getPurposeTemplateTypeCode(eq(requestType), eq(TemplateType.CANCELED)))
                .thenReturn(templateTypeCode);
        when(templateValueFromTemplateTypeCodeAndLangCode.getTemplateValueFromTemplateTypeCodeAndLangCode(eq(lang), eq(templateTypeCode)))
                .thenReturn(fileText);
        when(replacePlaceholderValueInTemplate.replacePlaceholderValueInTemplate(eq(entity), eq(fileText), eq(requestType), eq(lang)))
                .thenReturn(replaced);

        String result = descriptionForLangCode.getDescriptionForLangCode(entity, lang, statusCode, requestType);

        assertEquals(replaced, result);
        verify(purposeTemplateTypeCode, times(1)).getPurposeTemplateTypeCode(eq(requestType), eq(TemplateType.CANCELED));
    }

    @Test
    public void getDescriptionForLangCode_whenResidentStatusIdentityUpdated_usesRegprocSuccess() throws Exception {
        ResidentTransactionEntity entity = new ResidentTransactionEntity();
        // set resident entity status to replicate identity-updated branch
        entity.setStatusCode(EventStatusInProgress.IDENTITY_UPDATED.name());

        String lang = "en";
        String statusCode = "ANY_OTHER_STATUS";
        RequestType requestType = RequestType.UPDATE_MY_UIN;

        String templateTypeCode = "T_REGPROC";
        String fileText = "RegprocTemplate";
        String replaced = "RegprocReplaced";

        when(purposeTemplateTypeCode.getPurposeTemplateTypeCode(eq(requestType), eq(TemplateType.REGPROC_SUCCESS)))
                .thenReturn(templateTypeCode);
        when(templateValueFromTemplateTypeCodeAndLangCode.getTemplateValueFromTemplateTypeCodeAndLangCode(eq(lang), eq(templateTypeCode)))
                .thenReturn(fileText);
        when(replacePlaceholderValueInTemplate.replacePlaceholderValueInTemplate(eq(entity), eq(fileText), eq(requestType), eq(lang)))
                .thenReturn(replaced);

        String result = descriptionForLangCode.getDescriptionForLangCode(entity, lang, statusCode, requestType);

        assertEquals(replaced, result);
        verify(purposeTemplateTypeCode, times(1)).getPurposeTemplateTypeCode(eq(requestType), eq(TemplateType.REGPROC_SUCCESS));
    }

    @Test
    public void getDescriptionForLangCode_whenFallback_usesFailureTemplate() throws Exception {
        ResidentTransactionEntity entity = new ResidentTransactionEntity();
        entity.setStatusCode("SOME_OTHER");
        String lang = "en";
        String statusCode = "UNKNOWN_STATUS";
        RequestType requestType = RequestType.UPDATE_MY_UIN;

        String templateTypeCode = "T_FAIL";
        String fileText = "FailTemplate";
        String replaced = "FailReplaced";

        when(purposeTemplateTypeCode.getPurposeTemplateTypeCode(eq(requestType), eq(TemplateType.FAILURE)))
                .thenReturn(templateTypeCode);
        when(templateValueFromTemplateTypeCodeAndLangCode.getTemplateValueFromTemplateTypeCodeAndLangCode(eq(lang), eq(templateTypeCode)))
                .thenReturn(fileText);
        when(replacePlaceholderValueInTemplate.replacePlaceholderValueInTemplate(eq(entity), eq(fileText), eq(requestType), eq(lang)))
                .thenReturn(replaced);

        String result = descriptionForLangCode.getDescriptionForLangCode(entity, lang, statusCode, requestType);

        assertEquals(replaced, result);
        verify(purposeTemplateTypeCode, times(1)).getPurposeTemplateTypeCode(eq(requestType), eq(TemplateType.FAILURE));
    }
}
