package io.mosip.resident.validator;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.impl.RemainingUpdateCountByIndividualId;
import io.mosip.resident.util.AvailableClaimUtility;
import io.mosip.resident.util.ResidentServiceRestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RemainingUpdateCountByIndividualId.
 * @author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
public class RemainingUpdateCountByIndividualIdTest {

    private RemainingUpdateCountByIndividualId remainingUpdateCountByIndividualId;

    @Mock
    private AvailableClaimUtility availableClaimUtility;

    @Mock
    private ResidentServiceRestClient residentServiceRestClient;

    @Before
    public void setUp() {
        // create real instance and inject mocks manually so Mockito mocks are actually used
        remainingUpdateCountByIndividualId = new RemainingUpdateCountByIndividualId();

        ReflectionTestUtils.setField(remainingUpdateCountByIndividualId, "availableClaimUtility", availableClaimUtility);
        ReflectionTestUtils.setField(remainingUpdateCountByIndividualId, "residentServiceRestClient", residentServiceRestClient);
    }

    @Test
    public void getRemainingUpdateCountByIndividualId_success_returnsResponseWrapper()
            throws Exception {
        // arrange
        ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponse("dummy-response"); // simple payload for assertion

        when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("IND123");

        // stub getApi with proper matchers (ApiName enum class type used as any())
        when(residentServiceRestClient.getApi(
                any(),                 // ApiName
                anyMap(),              // path segments map
                anyList(),             // queryParamName
                anyList(),             // queryParamValue
                eq(ResponseWrapper.class) // response class
        )).thenReturn(responseWrapper);

        // act
        ResponseWrapper<?> result = remainingUpdateCountByIndividualId.getRemainingUpdateCountByIndividualId(null);

        // assert
        assertNotNull(result);
        assertEquals(responseWrapper, result);

        // verify interactions
        verify(availableClaimUtility, times(1)).getResidentIndvidualIdFromSession();
        verify(residentServiceRestClient, times(1)).getApi(
                any(),
                anyMap(),
                anyList(),
                anyList(),
                eq(ResponseWrapper.class)
        );
    }

    @Test
    public void getRemainingUpdateCountByIndividualId_whenApiThrowsApisResourceAccessException_mapsToCheckedException()
            throws Exception {
        // arrange
        when(availableClaimUtility.getResidentIndvidualIdFromSession()).thenReturn("IND456");

        // make residentServiceRestClient.getApi throw ApisResourceAccessException
        when(residentServiceRestClient.getApi(
                any(),
                anyMap(),
                anyList(),
                anyList(),
                eq(ResponseWrapper.class)
        )).thenThrow(new ApisResourceAccessException("resource down"));

        try {
            // act
            remainingUpdateCountByIndividualId.getRemainingUpdateCountByIndividualId(List.of());
            fail("Expected ResidentServiceCheckedException to be thrown");
        } catch (ResidentServiceCheckedException ex) {
            // assert: the method maps the ApisResourceAccessException to API_RESOURCE_ACCESS_EXCEPTION
            assertEquals(io.mosip.resident.constant.ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
                    ex.getErrorCode());
            // message should not be null
            assertNotNull(ex.getMessage());
        }

        // verify interactions
        verify(availableClaimUtility, times(1)).getResidentIndvidualIdFromSession();
        verify(residentServiceRestClient, times(1)).getApi(
                any(),
                anyMap(),
                anyList(),
                anyList(),
                eq(ResponseWrapper.class)
        );
    }
}
