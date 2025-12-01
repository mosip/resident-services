package io.mosip.resident.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Unit tests for MaskDataUtility
 * @author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
public class MaskDataUtilityTest {

    private MaskDataUtility maskDataUtility;

    @Before
    public void setUp() {
        maskDataUtility = new MaskDataUtility();

        // Inject a simple MapVariableResolverFactory as functionFactory (required by the class)
        MapVariableResolverFactory varFactory = new MapVariableResolverFactory(new HashMap<>());
        ReflectionTestUtils.setField(maskDataUtility, "functionFactory", varFactory);
    }

    @Test
    public void testMaskEmail_basicMasking() {
        // Define an inline MVEL function: takes value, returns first char + '***' + domain part
        String emailMaskFn = "def em(v){ v.substring(0,1) + '***' + v.substring(v.indexOf('@')) }; em";
        ReflectionTestUtils.setField(maskDataUtility, "emailMaskFunction", emailMaskFn);

        String input = "user@example.com";
        String masked = maskDataUtility.maskEmail(input);

        // expected: 'u***@example.com'
        assertEquals("u***@example.com", masked);
    }

    @Test
    public void testMaskPhone_basicMasking() {
        // Define inline MVEL function for phone: first 3 chars + '****' + last 2 chars
        String phoneMaskFn = "def ph(v){ v.substring(0,3) + '****' + v.substring(7) }; ph";
        ReflectionTestUtils.setField(maskDataUtility, "phoneMaskFunction", phoneMaskFn);

        String input = "9876543210";
        String masked = maskDataUtility.maskPhone(input);
        
        assertEquals("987****210", masked);
    }

    @Test
    public void testConvertToMaskData_handlesNullInput() {
        // Define a generic masking function that prefixes 'X' to the provided string
        String maskFn = "def m(v){ 'X' + v }; m";
        ReflectionTestUtils.setField(maskDataUtility, "maskingFunction", maskFn);

        // null is converted to the String "null" by String.valueOf(object) in maskData
        String masked = maskDataUtility.convertToMaskData(null);

        assertEquals("Xnull", masked);
    }
}
