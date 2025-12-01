package io.mosip.resident.service.impl;

import io.mosip.resident.constant.EventStatus;
import io.mosip.resident.util.EventStatusBasedOnLangCode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.util.function.Tuple2;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for EventStatusCode
 * @author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
public class EventStatusCodeTest {

    private EventStatusCode eventStatusCode;

    @Mock
    private EventStatusBasedOnLangCode eventStatusBasedOnLangCode;

    @Before
    public void setUp() {
        eventStatusCode = new EventStatusCode();
        ReflectionTestUtils.setField(eventStatusCode, "eventStatusBasedOnLangCode", eventStatusBasedOnLangCode);
    }

    @Test
    public void getEventStatusCode_whenFailureStatus_returnsFailedTuple() {
        // Pick a status that EventStatusFailure.containsStatus recognizes.
        // Use EventStatus.FAILED.name() as a representative input (adjust if your enum uses different literal)
        String statusCode = EventStatus.FAILED.name();
        String lang = "en";
        String expectedLabel = "Failed Label";

        when(eventStatusBasedOnLangCode.getEventStatusBasedOnLangcode(eq(EventStatus.FAILED), eq(lang)))
                .thenReturn(expectedLabel);

        Tuple2<String, String> result = eventStatusCode.getEventStatusCode(statusCode, lang);

        assertEquals(EventStatus.FAILED.name(), result.getT1());
        assertEquals(expectedLabel, result.getT2());
    }

    @Test
    public void getEventStatusCode_whenCanceledStatus_returnsCanceledTuple() {
        // Pick a status that EventStatusCanceled.containsStatus recognizes.
        // Use EventStatus.CANCELED.name() as representative input.
        String statusCode = EventStatus.CANCELED.name();
        String lang = "en";
        String expectedLabel = "Canceled Label";

        when(eventStatusBasedOnLangCode.getEventStatusBasedOnLangcode(eq(EventStatus.CANCELED), eq(lang)))
                .thenReturn(expectedLabel);

        Tuple2<String, String> result = eventStatusCode.getEventStatusCode(statusCode, lang);

        assertEquals(EventStatus.CANCELED.name(), result.getT1());
        assertEquals(expectedLabel, result.getT2());
    }

    @Test
    public void getEventStatusCode_whenUnknownStatus_returnsInProgressTuple() {
        // A status that isn't in success/failure/canceled sets should map to IN_PROGRESS
        String statusCode = "SOME_RANDOM_STATUS";
        String lang = "en";
        String expectedLabel = "In Progress Label";

        when(eventStatusBasedOnLangCode.getEventStatusBasedOnLangcode(eq(EventStatus.IN_PROGRESS), eq(lang)))
                .thenReturn(expectedLabel);

        Tuple2<String, String> result = eventStatusCode.getEventStatusCode(statusCode, lang);

        assertEquals(EventStatus.IN_PROGRESS.name(), result.getT1());
        assertEquals(expectedLabel, result.getT2());
    }
}
