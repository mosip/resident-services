package io.mosip.resident.service.impl;

import io.mosip.resident.constant.RequestType;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.util.DescriptionTemplateVariables;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/*
    * unit tests for ReplacePlaceholderValueInTemplate
    * @author Kamesh Shekhar Prasad
 */

public class ReplacePlaceholderValueInTemplateTest {

    @InjectMocks
    private ReplacePlaceholderValueInTemplate sut;

    @Mock
    private DescriptionTemplateVariables descriptionTemplateVariables;

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testReplacePlaceholder_UpdateMyUin() {
        // Arrange
        ResidentTransactionEntity entity = new ResidentTransactionEntity();
        String fileText = "TEMPLATE_TEXT";
        String lang = "en";
        RequestType requestType = RequestType.UPDATE_MY_UIN;

        String expected = "Processed template";

        when(descriptionTemplateVariables
                .getDescriptionTemplateVariablesForUpdateMyUin(entity, fileText, lang))
                .thenReturn(expected);

        // Act
        String result = sut.replacePlaceholderValueInTemplate(entity, fileText, requestType, lang);

        // Assert
        assertEquals(expected, result);

        verify(descriptionTemplateVariables)
                .getDescriptionTemplateVariablesForUpdateMyUin(entity, fileText, lang);
    }

    @Test
    public void testReplacePlaceholder_ValidateOtp() {
        // Arrange
        ResidentTransactionEntity entity = new ResidentTransactionEntity();
        String fileText = "OTP_TEMPLATE";
        String lang = "en";

        RequestType requestType = RequestType.VALIDATE_OTP;

        String expected = "OTP description";

        when(descriptionTemplateVariables
                .getDescriptionTemplateVariablesForValidateOtp(entity, fileText, lang))
                .thenReturn(expected);

        // Act
        String result = sut.replacePlaceholderValueInTemplate(entity, fileText, requestType, lang);

        // Assert
        assertEquals(expected, result);

        verify(descriptionTemplateVariables)
                .getDescriptionTemplateVariablesForValidateOtp(entity, fileText, lang);
    }
}
