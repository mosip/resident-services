package io.mosip.resident.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test class for RepeatableStreamHttpServletRequest.
 * @author Kamesh Shekhar Prasad
 */

public class RepeatableStreamHttpServletRequestTest {

    private HttpServletRequest originalRequest;
    private RepeatableStreamHttpServletRequest repeatableRequest;

    private static final String REQUEST_BODY = "{\"name\":\"John\",\"age\":20}";

    @Before
    public void setup() throws IOException {
        originalRequest = mock(HttpServletRequest.class);

        // Mock body InputStream
        ByteArrayInputStream byteStream = new ByteArrayInputStream(REQUEST_BODY.getBytes());

        ServletInputStream servletInputStream = new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return byteStream.read();
            }

            @Override
            public boolean isFinished() {
                return byteStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) { }
        };

        when(originalRequest.getInputStream()).thenReturn(servletInputStream);
        when(originalRequest.getReader()).thenReturn(new BufferedReader(new StringReader(REQUEST_BODY)));

        when(originalRequest.getHeaderNames())
                .thenReturn(Collections.enumeration(Collections.singleton("Content-Type")));
        when(originalRequest.getHeader("Content-Type")).thenReturn("application/json");

        repeatableRequest = new RepeatableStreamHttpServletRequest(originalRequest);
    }

    @Test
    public void requestBodyCanBeReadMultipleTimesUsingInputStream() throws IOException {
        String firstRead = new String(repeatableRequest.getInputStream().readAllBytes());
        String secondRead = new String(repeatableRequest.getInputStream().readAllBytes());

        assertEquals(REQUEST_BODY, firstRead);
        assertEquals("Reading again should return same body", REQUEST_BODY, secondRead);
    }

    @Test
    public void requestBodyCanBeReadMultipleTimesUsingReader() throws IOException {
        String firstRead = repeatableRequest.getReader().readLine();
        String secondRead = repeatableRequest.getReader().readLine();

        assertEquals(REQUEST_BODY, firstRead);
        assertEquals(REQUEST_BODY, secondRead);
    }

    @Test
    public void headersAreDelegatedToOriginalRequest() {
        assertEquals("application/json", repeatableRequest.getHeader("Content-Type"));
        assertTrue(repeatableRequest.getHeaderNames().hasMoreElements());
    }
}
