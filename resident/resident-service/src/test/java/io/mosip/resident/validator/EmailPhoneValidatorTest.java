package io.mosip.resident.validator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;

/**
 * @author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
public class EmailPhoneValidatorTest {

    private EmailPhoneValidator validator;

    @Before
    public void setUp() {
        validator = new EmailPhoneValidator();

        // inject regex values exactly like @Value would do
        ReflectionTestUtils.setField(validator, "phoneRegex", "^[0-9]{10}$");
        ReflectionTestUtils.setField(validator, "emailRegex", "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    // ---------------- Email tests ----------------

    @Test
    public void testEmailValidator_validEmail_shouldReturnTrue() {
        assertTrue(validator.emailValidator("test.email@domain.com"));
        assertTrue(validator.emailValidator("user123+tag@gmail.com"));
    }

    @Test
    public void testEmailValidator_invalidEmail_shouldReturnFalse() {
        assertFalse(validator.emailValidator("invalid-email"));
        assertFalse(validator.emailValidator("user@"));
        assertFalse(validator.emailValidator("@domain.com"));
    }

    // ---------------- Phone tests ----------------

    @Test
    public void testPhoneValidator_validPhone_shouldReturnTrue() {
        assertTrue(validator.phoneValidator("9876543210"));
        assertTrue(validator.phoneValidator("1234567890"));
    }

    @Test
    public void testPhoneValidator_invalidPhone_shouldReturnFalse() {
        assertFalse(validator.phoneValidator("12345"));         // too short
        assertFalse(validator.phoneValidator("12345678901"));   // too long
        assertFalse(validator.phoneValidator("abc1234567"));    // alphabets not allowed
    }
}
