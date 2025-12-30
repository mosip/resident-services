package io.mosip.resident.util;

import io.mosip.kernel.core.idvalidator.exception.InvalidIDException;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.ResidentErrorCode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UinVidValidator
 * @author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
public class UinVidValidatorTest {

    private UinVidValidator validator;

    @Mock
    private UinValidator<String> uinValidator;

    @Mock
    private VidValidator<String> vidValidator;

    @Before
    public void setUp() {
        validator = new UinVidValidator();
        ReflectionTestUtils.setField(validator, "uinValidator", uinValidator);
        ReflectionTestUtils.setField(validator, "vidValidator", vidValidator);
    }

    // ------ validateVid tests ------

    @Test
    public void validateVidWhenValidatorReturnsTrueShouldReturnTrue() throws Exception {
        when(vidValidator.validateId("VID123")).thenReturn(true);
        assertTrue(validator.validateVid("VID123"));
        verify(vidValidator, times(1)).validateId("VID123");
    }

    @Test
    public void validateVidWhenValidatorReturnsFalseShouldReturnFalse() throws Exception {
        when(vidValidator.validateId("VID123")).thenReturn(false);
        assertFalse(validator.validateVid("VID123"));
        verify(vidValidator, times(1)).validateId("VID123");
    }

    @Test
    public void validateVidWhenValidatorThrowsShouldReturnFalse() throws Exception {
        when(vidValidator.validateId("BAD")).thenThrow(new InvalidIDException(ResidentErrorCode.INVALID_REQUEST_EXCEPTION.getErrorCode(), "invalid"));
        assertFalse(validator.validateVid("BAD"));
        verify(vidValidator, times(1)).validateId("BAD");
    }

    // ------ validateUin tests ------

    @Test
    public void validateUin_whenValidatorReturnsTrue_shouldReturnTrue() throws Exception {
        when(uinValidator.validateId("UIN123")).thenReturn(true);
        assertTrue(validator.validateUin("UIN123"));
        verify(uinValidator, times(1)).validateId("UIN123");
    }

    @Test
    public void validateUinWhenValidatorReturnsFalseShouldReturnFalse() throws Exception {
        when(uinValidator.validateId("UIN123")).thenReturn(false);
        assertFalse(validator.validateUin("UIN123"));
        verify(uinValidator, times(1)).validateId("UIN123");
    }

    @Test
    public void validateUinWhenValidatorThrowsShouldReturnFalse() throws Exception {
        when(uinValidator.validateId("BAD")).thenThrow(new InvalidIDException(ResidentErrorCode.INVALID_REQUEST_EXCEPTION.getErrorCode(), "invalid"));
        assertFalse(validator.validateUin("BAD"));
        verify(uinValidator, times(1)).validateId("BAD");
    }

    // ------ getIndividualIdType tests ------

    @Test
    public void getIndividualIdTypeWhenUinValidReturnsUIN() throws Exception {
        when(uinValidator.validateId("ID1")).thenReturn(true);
        // vidValidator should not be called in this scenario
        IdType type = validator.getIndividualIdType("ID1");
        assertEquals(IdType.UIN, type);
        verify(uinValidator, times(1)).validateId("ID1");
        verifyNoInteractions(vidValidator);
    }

    @Test
    public void getIndividualIdTypeWhenUinInvalidButVidValidReturnsVID() throws Exception {
        when(uinValidator.validateId("ID2")).thenReturn(false);
        when(vidValidator.validateId("ID2")).thenReturn(true);
        IdType type = validator.getIndividualIdType("ID2");
        assertEquals(IdType.VID, type);
        verify(uinValidator, times(1)).validateId("ID2");
        verify(vidValidator, times(1)).validateId("ID2");
    }

    @Test
    public void getIndividualIdTypeWhenBothInvalidReturnsAID() throws Exception {
        when(uinValidator.validateId("ID3")).thenReturn(false);
        when(vidValidator.validateId("ID3")).thenReturn(false);
        IdType type = validator.getIndividualIdType("ID3");
        assertEquals(IdType.AID, type);
        verify(uinValidator, times(1)).validateId("ID3");
        verify(vidValidator, times(1)).validateId("ID3");
    }

    @Test
    public void getIndividualIdTypeWhenValidatorsThrowExceptionsAreHandledAndReturnsAID() throws Exception {
        when(uinValidator.validateId("ID4")).thenThrow(new InvalidIDException(ResidentErrorCode.INVALID_REQUEST_EXCEPTION.getErrorCode(), "bad uin"));
        when(vidValidator.validateId("ID4")).thenThrow(new InvalidIDException(ResidentErrorCode.INVALID_REQUEST_EXCEPTION.getErrorCode(), "bad vid"));
        IdType type = validator.getIndividualIdType("ID4");
        // both validators throw -> validateUin and validateVid return false -> result should be AID
        assertEquals(IdType.AID, type);
        verify(uinValidator, times(1)).validateId("ID4");
        verify(vidValidator, times(1)).validateId("ID4");
    }
}
