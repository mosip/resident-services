package io.mosip.resident.util;

import io.mosip.resident.exception.ApisResourceAccessException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClaimValueUtility
 * @author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
public class ClaimValueUtilityTest {

    private ClaimValueUtility claimValueUtility;

    @Mock
    private AvailableClaimValueUtility availableClaimValueUtility;

    @Before
    public void setUp() {
        claimValueUtility = new ClaimValueUtility();
        ReflectionTestUtils.setField(claimValueUtility, "availableClaimValueUtility", availableClaimValueUtility);
    }

    @Test
    public void testGetClaimValue_success() throws Exception {
        String claim = "sub";
        when(availableClaimValueUtility.getClaims(eq(claim)))
                .thenReturn(Map.of(claim, "user123"));

        String value = claimValueUtility.getClaimValue(claim);

        assertEquals("user123", value);
        verify(availableClaimValueUtility, times(1)).getClaims(eq(claim));
    }

    @Test(expected = ApisResourceAccessException.class)
    public void testGetClaimValue_whenAvailableClaimThrows_shouldPropagate() throws Exception {
        String claim = "sub";
        when(availableClaimValueUtility.getClaims(eq(claim)))
                .thenThrow(new ApisResourceAccessException("userinfo down"));

        claimValueUtility.getClaimValue(claim);
    }

    @Test
    public void testGetClaimValue_whenClaimMissing_returnsNull() throws Exception {
        String claim = "missing";
        when(availableClaimValueUtility.getClaims(eq(claim)))
                .thenReturn(Map.of()); // empty map

        String value = claimValueUtility.getClaimValue(claim);

        assertNull(value);
        verify(availableClaimValueUtility, times(1)).getClaims(eq(claim));
    }
}
