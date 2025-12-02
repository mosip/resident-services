package io.mosip.resident.validator;

import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ResidentServiceException;
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
public class ValidateOtpCharLimitTest {

    private ValidateOtpCharLimit validateOtpCharLimit;

    @Before
    public void setUp() {
        validateOtpCharLimit = new ValidateOtpCharLimit();
        // set otpLength via ReflectionTestUtils (mimics @Value injection)
        ReflectionTestUtils.setField(validateOtpCharLimit, "otpLength", 6);
    }

    @Test
    public void testValidateOtpCharLimit_withinLimit_shouldNotThrow() {
        // OTP length equal to configured limit (6) -> no exception expected
        try {
            validateOtpCharLimit.validateOtpCharLimit("123456");
        } catch (Exception e) {
            fail("No exception expected for OTP within limit, but got: " + e.getMessage());
        }
    }

    @Test
    public void testValidateOtpCharLimit_exceedsLimit_shouldThrowResidentServiceException() {
        String otp = "1234567"; // length 7 > 6
        try {
            validateOtpCharLimit.validateOtpCharLimit(otp);
            fail("Expected ResidentServiceException when OTP exceeds configured length");
        } catch (ResidentServiceException ex) {
            // verify error code
            assertEquals("Error code should match", ResidentErrorCode.CHAR_LIMIT_EXCEEDS.getErrorCode(), ex.getErrorCode());
            // message should mention configured length and the provided OTP (implementation uses String.format with otpLength and otp)
            assertTrue("Error message should contain otp length", ex.getMessage().contains("6"));
            assertTrue("Error message should contain actual otp", ex.getMessage().contains(otp));
        } catch (Exception e) {
            fail("Expected ResidentServiceException but got different exception: " + e);
        }
    }
}
