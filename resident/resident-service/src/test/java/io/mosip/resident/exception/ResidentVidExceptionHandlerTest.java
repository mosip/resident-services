package io.mosip.resident.exception;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.ResponseWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Kamesh Shekhar Prasad
 */

@ContextConfiguration(classes = {ResidentVidExceptionHandler.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class ResidentVidExceptionHandlerTest {
    @MockBean
    private Environment environment;

    @Autowired
    private ResidentVidExceptionHandler residentVidExceptionHandler;

    private HttpServletRequest  mockedRequest;

    @Before
    public void setup() throws Exception {
        mockedRequest = Mockito.mock(HttpServletRequest.class);
        when(mockedRequest.getRequestURI()).thenReturn("https://example.org/example");
    }

    @Test
    public void testResidentCheckedException2() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        ResponseEntity<Object> actualResidentCheckedExceptionResult = residentVidExceptionHandler
                .residentCheckedException(mockedRequest,
                        new ResidentServiceCheckedException(ResidentErrorCode.NO_RID_FOUND_EXCEPTION));
        assertTrue(actualResidentCheckedExceptionResult.hasBody());
        assertTrue(actualResidentCheckedExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualResidentCheckedExceptionResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualResidentCheckedExceptionResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualResidentCheckedExceptionResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualResidentCheckedExceptionResult.getBody())
                .getErrors();
        assertEquals(1, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualResidentCheckedExceptionResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-408", getResult.getErrorCode());
        assertEquals("RID not found", getResult.getMessage());
        verify(environment, atLeast(1)).getProperty((String) any());
    }

    @Test
    public void testResidentCheckedException3() {
        when(environment.getProperty((String) any())).thenReturn("Property");
        when(mockedRequest.getRequestURI()).thenReturn("https://example.org/example/vid/");
        ResponseEntity<Object> actualResidentCheckedExceptionResult = residentVidExceptionHandler
                .residentCheckedException(mockedRequest,
                        new ResidentServiceCheckedException(ResidentErrorCode.NO_RID_FOUND_EXCEPTION));
        assertTrue(actualResidentCheckedExceptionResult.hasBody());
        assertTrue(actualResidentCheckedExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualResidentCheckedExceptionResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualResidentCheckedExceptionResult.getBody()).getResponse());
        assertEquals("Property", ((ResponseWrapper<Object>) actualResidentCheckedExceptionResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualResidentCheckedExceptionResult.getBody())
                .getErrors();
        assertEquals(1, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualResidentCheckedExceptionResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-408", getResult.getErrorCode());
        assertEquals("RID not found", getResult.getMessage());
        verify(environment, atLeast(1)).getProperty((String) any());
    }

    @Test
    public void testResidentCheckedException5() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        ResidentServiceCheckedException residentServiceCheckedException = new ResidentServiceCheckedException(
                ResidentErrorCode.NO_RID_FOUND_EXCEPTION);
        residentServiceCheckedException.addInfo("An error occurred", "An error occurred");
        ResponseEntity<Object> actualResidentCheckedExceptionResult = residentVidExceptionHandler
                .residentCheckedException(mockedRequest, residentServiceCheckedException);
        assertTrue(actualResidentCheckedExceptionResult.hasBody());
        assertTrue(actualResidentCheckedExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualResidentCheckedExceptionResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualResidentCheckedExceptionResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualResidentCheckedExceptionResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualResidentCheckedExceptionResult.getBody())
                .getErrors();
        assertEquals(2, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualResidentCheckedExceptionResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("An error occurred", getResult.getMessage());
        ServiceError getResult1 = errors.get(1);
        assertEquals("RID not found", getResult1.getMessage());
        assertEquals("RES-SER-408", getResult1.getErrorCode());
        assertEquals("An error occurred", getResult.getErrorCode());
        verify(environment, atLeast(1)).getProperty((String) any());
    }


    @Test
    public void testResidentCheckedException6() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        ResidentServiceCheckedException residentServiceCheckedException = mock(ResidentServiceCheckedException.class);
        when(residentServiceCheckedException.getErrorCode()).thenReturn("An error occurred");
        when(residentServiceCheckedException.getMessage()).thenReturn("An error occurred");
        ArrayList<String> stringList = new ArrayList<>();
        when(residentServiceCheckedException.getCodes()).thenReturn(stringList);
        when(residentServiceCheckedException.getErrorTexts()).thenReturn(new ArrayList<>());
        ResponseEntity<Object> actualResidentCheckedExceptionResult = residentVidExceptionHandler
                .residentCheckedException(mockedRequest, residentServiceCheckedException);
        assertTrue(actualResidentCheckedExceptionResult.hasBody());
        assertTrue(actualResidentCheckedExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualResidentCheckedExceptionResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualResidentCheckedExceptionResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualResidentCheckedExceptionResult.getBody()).getId());
        assertEquals(stringList, ((ResponseWrapper<Object>) actualResidentCheckedExceptionResult.getBody()).getErrors());
        assertEquals("Property", ((ResponseWrapper<Object>) actualResidentCheckedExceptionResult.getBody()).getVersion());
        verify(environment, atLeast(1)).getProperty((String) any());
        verify(residentServiceCheckedException).getErrorCode();
        verify(residentServiceCheckedException).getMessage();
        verify(residentServiceCheckedException).getCodes();
        verify(residentServiceCheckedException).getErrorTexts();
    }


    @Test
    public void testResidentServiceException2() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        ResponseEntity<Object> actualResidentServiceExceptionResult = residentVidExceptionHandler
                .residentServiceException(mockedRequest,
                        new ResidentServiceException(ResidentErrorCode.NO_RID_FOUND_EXCEPTION));
        assertTrue(actualResidentServiceExceptionResult.hasBody());
        assertTrue(actualResidentServiceExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualResidentServiceExceptionResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualResidentServiceExceptionResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualResidentServiceExceptionResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualResidentServiceExceptionResult.getBody())
                .getErrors();
        assertEquals(1, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualResidentServiceExceptionResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-408", getResult.getErrorCode());
        assertEquals("RID not found", getResult.getMessage());
        verify(environment, atLeast(1)).getProperty((String) any());
    }

    @Test
    public void testResidentServiceException3() {
        when(environment.getProperty((String) any())).thenReturn("Property");
        when(mockedRequest.getRequestURI()).thenReturn("https://example.org/example/vid/");
        ResponseEntity<Object> actualResidentServiceExceptionResult = residentVidExceptionHandler
                .residentServiceException(mockedRequest,
                        new ResidentServiceException(ResidentErrorCode.NO_RID_FOUND_EXCEPTION));
        assertTrue(actualResidentServiceExceptionResult.hasBody());
        assertTrue(actualResidentServiceExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualResidentServiceExceptionResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualResidentServiceExceptionResult.getBody()).getResponse());
        assertEquals("Property", ((ResponseWrapper<Object>) actualResidentServiceExceptionResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualResidentServiceExceptionResult.getBody())
                .getErrors();
        assertEquals(1, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualResidentServiceExceptionResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-408", getResult.getErrorCode());
        assertEquals("RID not found", getResult.getMessage());
        verify(environment, atLeast(1)).getProperty((String) any());
    }


    @Test
    public void testResidentServiceException5() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        ResidentServiceException residentServiceException = new ResidentServiceException(
                ResidentErrorCode.NO_RID_FOUND_EXCEPTION);
        residentServiceException.addInfo("An error occurred", "An error occurred");
        ResponseEntity<Object> actualResidentServiceExceptionResult = residentVidExceptionHandler
                .residentServiceException(mockedRequest, residentServiceException);
        assertTrue(actualResidentServiceExceptionResult.hasBody());
        assertTrue(actualResidentServiceExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualResidentServiceExceptionResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualResidentServiceExceptionResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualResidentServiceExceptionResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualResidentServiceExceptionResult.getBody())
                .getErrors();
        assertEquals(2, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualResidentServiceExceptionResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("An error occurred", getResult.getMessage());
        ServiceError getResult1 = errors.get(1);
        assertEquals("RID not found", getResult1.getMessage());
        assertEquals("RES-SER-408", getResult1.getErrorCode());
        assertEquals("An error occurred", getResult.getErrorCode());
        verify(environment, atLeast(1)).getProperty((String) any());
    }


    @Test
    public void testResidentServiceException6() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        ResidentServiceException residentServiceException = mock(ResidentServiceException.class);
        when(residentServiceException.getErrorCode()).thenReturn("An error occurred");
        when(residentServiceException.getMessage()).thenReturn("An error occurred");
        ArrayList<String> stringList = new ArrayList<>();
        when(residentServiceException.getCodes()).thenReturn(stringList);
        when(residentServiceException.getErrorTexts()).thenReturn(new ArrayList<>());
        ResponseEntity<Object> actualResidentServiceExceptionResult = residentVidExceptionHandler
                .residentServiceException(mockedRequest, residentServiceException);
        assertTrue(actualResidentServiceExceptionResult.hasBody());
        assertTrue(actualResidentServiceExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualResidentServiceExceptionResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualResidentServiceExceptionResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualResidentServiceExceptionResult.getBody()).getId());
        assertEquals(stringList, ((ResponseWrapper<Object>) actualResidentServiceExceptionResult.getBody()).getErrors());
        assertEquals("Property", ((ResponseWrapper<Object>) actualResidentServiceExceptionResult.getBody()).getVersion());
        verify(environment, atLeast(1)).getProperty((String) any());
        verify(residentServiceException).getErrorCode();
        verify(residentServiceException).getMessage();
        verify(residentServiceException).getCodes();
        verify(residentServiceException).getErrorTexts();
    }


    @Test
    public void testVidAlreadyPresent2() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        ResponseEntity<Object> actualVidAlreadyPresentResult = residentVidExceptionHandler.vidAlreadyPresent(
                mockedRequest, new VidAlreadyPresentException("An error occurred", "An error occurred"));
        assertTrue(actualVidAlreadyPresentResult.hasBody());
        assertTrue(actualVidAlreadyPresentResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualVidAlreadyPresentResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualVidAlreadyPresentResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualVidAlreadyPresentResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualVidAlreadyPresentResult.getBody()).getErrors();
        assertEquals(1, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualVidAlreadyPresentResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("An error occurred", getResult.getErrorCode());
        assertEquals("Maximum allowed VIDs are active. Deactivate VID to generate new one.", getResult.getMessage());
        verify(environment, atLeast(1)).getProperty((String) any());
    }


    @Test
    public void testVidAlreadyPresent3() {
        when(environment.getProperty((String) any())).thenReturn("Property");
        when(mockedRequest.getRequestURI()).thenReturn("https://example.org/example/vid/");
        ResponseEntity<Object> actualVidAlreadyPresentResult = residentVidExceptionHandler.vidAlreadyPresent(
                mockedRequest, new VidAlreadyPresentException("An error occurred", "An error occurred"));
        assertTrue(actualVidAlreadyPresentResult.hasBody());
        assertTrue(actualVidAlreadyPresentResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualVidAlreadyPresentResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualVidAlreadyPresentResult.getBody()).getResponse());
        assertEquals("Property", ((ResponseWrapper<Object>) actualVidAlreadyPresentResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualVidAlreadyPresentResult.getBody()).getErrors();
        assertEquals(1, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualVidAlreadyPresentResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("An error occurred", getResult.getErrorCode());
        assertEquals("Maximum allowed VIDs are active. Deactivate VID to generate new one.", getResult.getMessage());
        verify(environment, atLeast(1)).getProperty((String) any());
    }

    @Test
    public void testVidAlreadyPresent5() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        VidAlreadyPresentException vidAlreadyPresentException = new VidAlreadyPresentException("An error occurred",
                "An error occurred");
        vidAlreadyPresentException.addInfo("An error occurred", "An error occurred");
        ResponseEntity<Object> actualVidAlreadyPresentResult = residentVidExceptionHandler
                .vidAlreadyPresent(mockedRequest, vidAlreadyPresentException);
        assertTrue(actualVidAlreadyPresentResult.hasBody());
        assertTrue(actualVidAlreadyPresentResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualVidAlreadyPresentResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualVidAlreadyPresentResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualVidAlreadyPresentResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualVidAlreadyPresentResult.getBody()).getErrors();
        assertEquals(2, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualVidAlreadyPresentResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("An error occurred", getResult.getMessage());
        ServiceError getResult1 = errors.get(1);
        assertEquals("Maximum allowed VIDs are active. Deactivate VID to generate new one.", getResult1.getMessage());
        assertEquals("An error occurred", getResult1.getErrorCode());
        assertEquals("An error occurred", getResult.getErrorCode());
        verify(environment, atLeast(1)).getProperty((String) any());
    }


    @Test
    public void testVidAlreadyPresent6() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        VidAlreadyPresentException vidAlreadyPresentException = mock(VidAlreadyPresentException.class);
        when(vidAlreadyPresentException.getErrorCode()).thenReturn("An error occurred");
        when(vidAlreadyPresentException.getMessage()).thenReturn("An error occurred");
        ArrayList<String> stringList = new ArrayList<>();
        when(vidAlreadyPresentException.getCodes()).thenReturn(stringList);
        when(vidAlreadyPresentException.getErrorTexts()).thenReturn(new ArrayList<>());
        ResponseEntity<Object> actualVidAlreadyPresentResult = residentVidExceptionHandler
                .vidAlreadyPresent(mockedRequest, vidAlreadyPresentException);
        assertTrue(actualVidAlreadyPresentResult.hasBody());
        assertTrue(actualVidAlreadyPresentResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualVidAlreadyPresentResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualVidAlreadyPresentResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualVidAlreadyPresentResult.getBody()).getId());
        assertEquals(stringList, ((ResponseWrapper<Object>) actualVidAlreadyPresentResult.getBody()).getErrors());
        assertEquals("Property", ((ResponseWrapper<Object>) actualVidAlreadyPresentResult.getBody()).getVersion());
        verify(environment, atLeast(1)).getProperty((String) any());
        verify(vidAlreadyPresentException).getErrorCode();
        verify(vidAlreadyPresentException).getMessage();
        verify(vidAlreadyPresentException).getCodes();
        verify(vidAlreadyPresentException).getErrorTexts();
    }


    @Test
    public void testVidCreationFailed2() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        ResponseEntity<Object> actualVidCreationFailedResult = residentVidExceptionHandler
                .vidCreationFailed(mockedRequest, new VidCreationException());
        assertTrue(actualVidCreationFailedResult.hasBody());
        assertTrue(actualVidCreationFailedResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualVidCreationFailedResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualVidCreationFailedResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualVidCreationFailedResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualVidCreationFailedResult.getBody()).getErrors();
        assertEquals(1, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualVidCreationFailedResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-406", getResult.getErrorCode());
        assertEquals("Exception while creating VID", getResult.getMessage());
        verify(environment, atLeast(1)).getProperty((String) any());
    }


    @Test
    public void testVidCreationFailed3() {
        when(environment.getProperty((String) any())).thenReturn("Property");
        when(mockedRequest.getRequestURI()).thenReturn("https://example.org/example/vid/");
        ResponseEntity<Object> actualVidCreationFailedResult = residentVidExceptionHandler
                .vidCreationFailed(mockedRequest, new VidCreationException());
        assertTrue(actualVidCreationFailedResult.hasBody());
        assertTrue(actualVidCreationFailedResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualVidCreationFailedResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualVidCreationFailedResult.getBody()).getResponse());
        assertEquals("Property", ((ResponseWrapper<Object>) actualVidCreationFailedResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualVidCreationFailedResult.getBody()).getErrors();
        assertEquals(1, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualVidCreationFailedResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-406", getResult.getErrorCode());
        assertEquals("Exception while creating VID", getResult.getMessage());
        verify(environment, atLeast(1)).getProperty((String) any());
    }


    @Test
    public void testVidCreationFailed5() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        VidCreationException vidCreationException = new VidCreationException();
        vidCreationException.addInfo("An error occurred", "An error occurred");
        ResponseEntity<Object> actualVidCreationFailedResult = residentVidExceptionHandler
                .vidCreationFailed(mockedRequest, vidCreationException);
        assertTrue(actualVidCreationFailedResult.hasBody());
        assertTrue(actualVidCreationFailedResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualVidCreationFailedResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualVidCreationFailedResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualVidCreationFailedResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualVidCreationFailedResult.getBody()).getErrors();
        assertEquals(2, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualVidCreationFailedResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("An error occurred", getResult.getMessage());
        ServiceError getResult1 = errors.get(1);
        assertEquals("Exception while creating VID", getResult1.getMessage());
        assertEquals("RES-SER-406", getResult1.getErrorCode());
        assertEquals("An error occurred", getResult.getErrorCode());
        verify(environment, atLeast(1)).getProperty((String) any());
    }


    @Test
    public void testVidCreationFailed6() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        VidCreationException vidCreationException = mock(VidCreationException.class);
        when(vidCreationException.getErrorCode()).thenReturn("An error occurred");
        when(vidCreationException.getMessage()).thenReturn("An error occurred");
        ArrayList<String> stringList = new ArrayList<>();
        when(vidCreationException.getCodes()).thenReturn(stringList);
        when(vidCreationException.getErrorTexts()).thenReturn(new ArrayList<>());
        ResponseEntity<Object> actualVidCreationFailedResult = residentVidExceptionHandler
                .vidCreationFailed(mockedRequest, vidCreationException);
        assertTrue(actualVidCreationFailedResult.hasBody());
        assertTrue(actualVidCreationFailedResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualVidCreationFailedResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualVidCreationFailedResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualVidCreationFailedResult.getBody()).getId());
        assertEquals(stringList, ((ResponseWrapper<Object>) actualVidCreationFailedResult.getBody()).getErrors());
        assertEquals("Property", ((ResponseWrapper<Object>) actualVidCreationFailedResult.getBody()).getVersion());
        verify(environment, atLeast(1)).getProperty((String) any());
        verify(vidCreationException).getErrorCode();
        verify(vidCreationException).getMessage();
        verify(vidCreationException).getCodes();
        verify(vidCreationException).getErrorTexts();
    }


    @Test
    public void testApiNotAccessible2() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        ResponseEntity<Object> actualApiNotAccessibleResult = residentVidExceptionHandler
                .apiNotAccessible(mockedRequest, new ApisResourceAccessException("An error occurred"));
        assertTrue(actualApiNotAccessibleResult.hasBody());
        assertTrue(actualApiNotAccessibleResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualApiNotAccessibleResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualApiNotAccessibleResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualApiNotAccessibleResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualApiNotAccessibleResult.getBody()).getErrors();
        assertEquals(1, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualApiNotAccessibleResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-411", getResult.getErrorCode());
        assertEquals("An error occurred", getResult.getMessage());
        verify(environment, atLeast(1)).getProperty((String) any());
    }


    @Test
    public void testApiNotAccessible3() {
        when(environment.getProperty((String) any())).thenReturn("Property");
        when(mockedRequest.getRequestURI()).thenReturn("https://example.org/example/vid/");
        ResponseEntity<Object> actualApiNotAccessibleResult = residentVidExceptionHandler
                .apiNotAccessible(mockedRequest, new ApisResourceAccessException("An error occurred"));
        assertTrue(actualApiNotAccessibleResult.hasBody());
        assertTrue(actualApiNotAccessibleResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualApiNotAccessibleResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualApiNotAccessibleResult.getBody()).getResponse());
        assertEquals("Property", ((ResponseWrapper<Object>) actualApiNotAccessibleResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualApiNotAccessibleResult.getBody()).getErrors();
        assertEquals(1, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualApiNotAccessibleResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-411", getResult.getErrorCode());
        assertEquals("An error occurred", getResult.getMessage());
        verify(environment, atLeast(1)).getProperty((String) any());
    }


    @Test
    public void testApiNotAccessible5() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        ApisResourceAccessException apisResourceAccessException = mock(ApisResourceAccessException.class);
        when(apisResourceAccessException.getErrorCode()).thenReturn("An error occurred");
        when(apisResourceAccessException.getMessage()).thenReturn("An error occurred");
        ArrayList<String> stringList = new ArrayList<>();
        when(apisResourceAccessException.getCodes()).thenReturn(stringList);
        when(apisResourceAccessException.getErrorTexts()).thenReturn(new ArrayList<>());
        ResponseEntity<Object> actualApiNotAccessibleResult = residentVidExceptionHandler
                .apiNotAccessible(mockedRequest, apisResourceAccessException);
        assertTrue(actualApiNotAccessibleResult.hasBody());
        assertTrue(actualApiNotAccessibleResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualApiNotAccessibleResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualApiNotAccessibleResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualApiNotAccessibleResult.getBody()).getId());
        assertEquals(stringList, ((ResponseWrapper<Object>) actualApiNotAccessibleResult.getBody()).getErrors());
        assertEquals("Property", ((ResponseWrapper<Object>) actualApiNotAccessibleResult.getBody()).getVersion());
        verify(environment, atLeast(1)).getProperty((String) any());
        verify(apisResourceAccessException).getErrorCode();
        verify(apisResourceAccessException).getMessage();
        verify(apisResourceAccessException).getCodes();
        verify(apisResourceAccessException).getErrorTexts();
    }


    @Test
    public void testOtpValidationFailed2() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        ResponseEntity<Object> actualOtpValidationFailedResult = residentVidExceptionHandler
                .otpValidationFailed(mockedRequest, new OtpValidationFailedException());
        assertTrue(actualOtpValidationFailedResult.hasBody());
        assertTrue(actualOtpValidationFailedResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualOtpValidationFailedResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualOtpValidationFailedResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualOtpValidationFailedResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualOtpValidationFailedResult.getBody()).getErrors();
        assertEquals(1, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualOtpValidationFailedResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-422", getResult.getErrorCode());
        assertEquals("OTP validation failed", getResult.getMessage());
        verify(environment, atLeast(1)).getProperty((String) any());
    }


    @Test
    public void testOtpValidationFailed3() {
        when(environment.getProperty((String) any())).thenReturn("Property");
        when(mockedRequest.getRequestURI()).thenReturn("https://example.org/example/vid/");
        ResponseEntity<Object> actualOtpValidationFailedResult = residentVidExceptionHandler
                .otpValidationFailed(mockedRequest, new OtpValidationFailedException());
        assertTrue(actualOtpValidationFailedResult.hasBody());
        assertTrue(actualOtpValidationFailedResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualOtpValidationFailedResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualOtpValidationFailedResult.getBody()).getResponse());
        assertEquals("Property", ((ResponseWrapper<Object>) actualOtpValidationFailedResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualOtpValidationFailedResult.getBody()).getErrors();
        assertEquals(1, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualOtpValidationFailedResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-422", getResult.getErrorCode());
        assertEquals("OTP validation failed", getResult.getMessage());
        verify(environment, atLeast(1)).getProperty((String) any());
    }


    @Test
    public void testOtpValidationFailed5() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        OtpValidationFailedException otpValidationFailedException = mock(OtpValidationFailedException.class);
        when(otpValidationFailedException.getErrorCode()).thenReturn("An error occurred");
        when(otpValidationFailedException.getMessage()).thenReturn("An error occurred");
        ArrayList<String> stringList = new ArrayList<>();
        when(otpValidationFailedException.getCodes()).thenReturn(stringList);
        when(otpValidationFailedException.getErrorTexts()).thenReturn(new ArrayList<>());
        ResponseEntity<Object> actualOtpValidationFailedResult = residentVidExceptionHandler
                .otpValidationFailed(mockedRequest, otpValidationFailedException);
        assertTrue(actualOtpValidationFailedResult.hasBody());
        assertTrue(actualOtpValidationFailedResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualOtpValidationFailedResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualOtpValidationFailedResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualOtpValidationFailedResult.getBody()).getId());
        assertEquals(stringList, ((ResponseWrapper<Object>) actualOtpValidationFailedResult.getBody()).getErrors());
        assertEquals("Property", ((ResponseWrapper<Object>) actualOtpValidationFailedResult.getBody()).getVersion());
        verify(environment, atLeast(1)).getProperty((String) any());
        verify(otpValidationFailedException).getErrorCode();
        verify(otpValidationFailedException).getMessage();
        verify(otpValidationFailedException).getCodes();
        verify(otpValidationFailedException).getErrorTexts();
    }


    @Test
    public void testInvalidInput2() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        ResponseEntity<Object> actualInvalidInputResult = residentVidExceptionHandler.invalidInput(mockedRequest,
                new InvalidInputException());
        assertTrue(actualInvalidInputResult.hasBody());
        assertTrue(actualInvalidInputResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualInvalidInputResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualInvalidInputResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualInvalidInputResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualInvalidInputResult.getBody()).getErrors();
        assertEquals(1, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualInvalidInputResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-410", getResult.getErrorCode());
        assertEquals("Invalid Input Parameter- ", getResult.getMessage());
        verify(environment, atLeast(1)).getProperty((String) any());
    }


    @Test
    public void testInvalidInput3() {
        when(environment.getProperty((String) any())).thenReturn("Property");
        when(mockedRequest.getRequestURI()).thenReturn("https://example.org/example/vid/");
        ResponseEntity<Object> actualInvalidInputResult = residentVidExceptionHandler.invalidInput(mockedRequest,
                new InvalidInputException());
        assertTrue(actualInvalidInputResult.hasBody());
        assertTrue(actualInvalidInputResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualInvalidInputResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualInvalidInputResult.getBody()).getResponse());
        assertEquals("Property", ((ResponseWrapper<Object>) actualInvalidInputResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualInvalidInputResult.getBody()).getErrors();
        assertEquals(1, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualInvalidInputResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-410", getResult.getErrorCode());
        assertEquals("Invalid Input Parameter- ", getResult.getMessage());
        verify(environment, atLeast(1)).getProperty((String) any());
    }


    @Test
    public void testInvalidInput5() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        InvalidInputException invalidInputException = new InvalidInputException();
        invalidInputException.addInfo("An error occurred", "An error occurred");
        ResponseEntity<Object> actualInvalidInputResult = residentVidExceptionHandler.invalidInput(mockedRequest,
                invalidInputException);
        assertTrue(actualInvalidInputResult.hasBody());
        assertTrue(actualInvalidInputResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualInvalidInputResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualInvalidInputResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualInvalidInputResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualInvalidInputResult.getBody()).getErrors();
        assertEquals(2, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualInvalidInputResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("An error occurred", getResult.getMessage());
        ServiceError getResult1 = errors.get(1);
        assertEquals("Invalid Input Parameter- ", getResult1.getMessage());
        assertEquals("RES-SER-410", getResult1.getErrorCode());
        assertEquals("An error occurred", getResult.getErrorCode());
        verify(environment, atLeast(1)).getProperty((String) any());
    }


    @Test
    public void testInvalidInput6() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        InvalidInputException invalidInputException = mock(InvalidInputException.class);
        when(invalidInputException.getErrorCode()).thenReturn("An error occurred");
        when(invalidInputException.getMessage()).thenReturn("An error occurred");
        ArrayList<String> stringList = new ArrayList<>();
        when(invalidInputException.getCodes()).thenReturn(stringList);
        when(invalidInputException.getErrorTexts()).thenReturn(new ArrayList<>());
        ResponseEntity<Object> actualInvalidInputResult = residentVidExceptionHandler.invalidInput(mockedRequest,
                invalidInputException);
        assertTrue(actualInvalidInputResult.hasBody());
        assertTrue(actualInvalidInputResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualInvalidInputResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualInvalidInputResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualInvalidInputResult.getBody()).getId());
        assertEquals(stringList, ((ResponseWrapper<Object>) actualInvalidInputResult.getBody()).getErrors());
        assertEquals("Property", ((ResponseWrapper<Object>) actualInvalidInputResult.getBody()).getVersion());
        verify(environment, atLeast(1)).getProperty((String) any());
        verify(invalidInputException).getErrorCode();
        verify(invalidInputException).getMessage();
        verify(invalidInputException).getCodes();
        verify(invalidInputException).getErrorTexts();
    }


    @Test
    public void testVidRevocationFailed2() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        ResponseEntity<Object> actualVidRevocationFailedResult = residentVidExceptionHandler
                .vidRevocationFailed(mockedRequest, new VidRevocationException());
        assertTrue(actualVidRevocationFailedResult.hasBody());
        assertTrue(actualVidRevocationFailedResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualVidRevocationFailedResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualVidRevocationFailedResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualVidRevocationFailedResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualVidRevocationFailedResult.getBody()).getErrors();
        assertEquals(1, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualVidRevocationFailedResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-407", getResult.getErrorCode());
        assertEquals("VID revocation request failed. Please visit the nearest registration center for assistance.",
                getResult.getMessage());
        verify(environment, atLeast(1)).getProperty((String) any());
    }


    @Test
    public void testVidRevocationFailed3() {
        when(mockedRequest.getRequestURI()).thenReturn("https://example.org/example/vid/");
        when(environment.getProperty((String) any())).thenReturn("Property");

        ResponseEntity<Object> actualVidRevocationFailedResult = residentVidExceptionHandler
                .vidRevocationFailed(mockedRequest, new VidRevocationException());
        assertTrue(actualVidRevocationFailedResult.hasBody());
        assertTrue(actualVidRevocationFailedResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualVidRevocationFailedResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualVidRevocationFailedResult.getBody()).getResponse());
        assertEquals("Property", ((ResponseWrapper<Object>) actualVidRevocationFailedResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualVidRevocationFailedResult.getBody()).getErrors();
        assertEquals(1, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualVidRevocationFailedResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-407", getResult.getErrorCode());
        assertEquals("VID revocation request failed. Please visit the nearest registration center for assistance.",
                getResult.getMessage());
        verify(environment, atLeast(1)).getProperty((String) any());
    }


    @Test
    public void testVidRevocationFailed5() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        VidRevocationException vidRevocationException = new VidRevocationException();
        vidRevocationException.addInfo("An error occurred", "An error occurred");
        ResponseEntity<Object> actualVidRevocationFailedResult = residentVidExceptionHandler
                .vidRevocationFailed(mockedRequest, vidRevocationException);
        assertTrue(actualVidRevocationFailedResult.hasBody());
        assertTrue(actualVidRevocationFailedResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualVidRevocationFailedResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualVidRevocationFailedResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualVidRevocationFailedResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualVidRevocationFailedResult.getBody()).getErrors();
        assertEquals(2, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualVidRevocationFailedResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("An error occurred", getResult.getMessage());
        ServiceError getResult1 = errors.get(1);
        assertEquals("VID revocation request failed. Please visit the nearest registration center for assistance.",
                getResult1.getMessage());
        assertEquals("RES-SER-407", getResult1.getErrorCode());
        assertEquals("An error occurred", getResult.getErrorCode());
        verify(environment, atLeast(1)).getProperty((String) any());
    }


    @Test
    public void testVidRevocationFailed6() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        VidRevocationException vidRevocationException = mock(VidRevocationException.class);
        when(vidRevocationException.getErrorCode()).thenReturn("An error occurred");
        when(vidRevocationException.getMessage()).thenReturn("An error occurred");
        ArrayList<String> stringList = new ArrayList<>();
        when(vidRevocationException.getCodes()).thenReturn(stringList);
        when(vidRevocationException.getErrorTexts()).thenReturn(new ArrayList<>());
        ResponseEntity<Object> actualVidRevocationFailedResult = residentVidExceptionHandler
                .vidRevocationFailed(mockedRequest, vidRevocationException);
        assertTrue(actualVidRevocationFailedResult.hasBody());
        assertTrue(actualVidRevocationFailedResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualVidRevocationFailedResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualVidRevocationFailedResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualVidRevocationFailedResult.getBody()).getId());
        assertEquals(stringList, ((ResponseWrapper<Object>) actualVidRevocationFailedResult.getBody()).getErrors());
        assertEquals("Property", ((ResponseWrapper<Object>) actualVidRevocationFailedResult.getBody()).getVersion());
        verify(environment, atLeast(1)).getProperty((String) any());
        verify(vidRevocationException).getErrorCode();
        verify(vidRevocationException).getMessage();
        verify(vidRevocationException).getCodes();
        verify(vidRevocationException).getErrorTexts();
    }


    @Test
    public void testIdRepoAppExceptionFailed2() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        ResponseEntity<Object> actualIdRepoAppExceptionFailedResult = residentVidExceptionHandler
                .idRepoAppExceptionFailed(mockedRequest,
                        new IdRepoAppException("An error occurred", "An error occurred"));
        assertTrue(actualIdRepoAppExceptionFailedResult.hasBody());
        assertTrue(actualIdRepoAppExceptionFailedResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualIdRepoAppExceptionFailedResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualIdRepoAppExceptionFailedResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualIdRepoAppExceptionFailedResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualIdRepoAppExceptionFailedResult.getBody())
                .getErrors();
        assertEquals(1, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualIdRepoAppExceptionFailedResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("An error occurred", getResult.getErrorCode());
        assertEquals("An error occurred", getResult.getMessage());
        verify(environment, atLeast(1)).getProperty((String) any());
    }


    @Test
    public void testIdRepoAppExceptionFailed3() {
        when(environment.getProperty((String) any())).thenReturn("Property");
        when(mockedRequest.getRequestURI()).thenReturn("https://example.org/example/vid/");
        ResponseEntity<Object> actualIdRepoAppExceptionFailedResult = residentVidExceptionHandler
                .idRepoAppExceptionFailed(mockedRequest,
                        new IdRepoAppException("An error occurred", "An error occurred"));
        assertTrue(actualIdRepoAppExceptionFailedResult.hasBody());
        assertTrue(actualIdRepoAppExceptionFailedResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualIdRepoAppExceptionFailedResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualIdRepoAppExceptionFailedResult.getBody()).getResponse());
        assertEquals("Property", ((ResponseWrapper<Object>) actualIdRepoAppExceptionFailedResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualIdRepoAppExceptionFailedResult.getBody())
                .getErrors();
        assertEquals(1, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualIdRepoAppExceptionFailedResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("An error occurred", getResult.getErrorCode());
        assertEquals("An error occurred", getResult.getMessage());
        verify(environment, atLeast(1)).getProperty((String) any());
    }


    @Test
    public void testIdRepoAppExceptionFailed5() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        IdRepoAppException idRepoAppException = new IdRepoAppException("An error occurred", "An error occurred");
        idRepoAppException.addInfo("An error occurred", "An error occurred");
        ResponseEntity<Object> actualIdRepoAppExceptionFailedResult = residentVidExceptionHandler
                .idRepoAppExceptionFailed(mockedRequest, idRepoAppException);
        assertTrue(actualIdRepoAppExceptionFailedResult.hasBody());
        assertTrue(actualIdRepoAppExceptionFailedResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualIdRepoAppExceptionFailedResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualIdRepoAppExceptionFailedResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualIdRepoAppExceptionFailedResult.getBody()).getId());
        List<ServiceError> errors = ((ResponseWrapper<Object>) actualIdRepoAppExceptionFailedResult.getBody())
                .getErrors();
        assertEquals(1, errors.size());
        assertEquals("Property", ((ResponseWrapper<Object>) actualIdRepoAppExceptionFailedResult.getBody()).getVersion());
        ServiceError getResult = errors.get(0);
        assertEquals("An error occurred", getResult.getErrorCode());
        assertEquals("An error occurred", getResult.getMessage());
        verify(environment, atLeast(1)).getProperty((String) any());
    }


    @Test
    public void testIdRepoAppExceptionFailed6() {
        when(environment.getProperty((String) any())).thenReturn("Property");

        IdRepoAppException idRepoAppException = mock(IdRepoAppException.class);
        when(idRepoAppException.getErrorCode()).thenReturn("An error occurred");
        when(idRepoAppException.getMessage()).thenReturn("An error occurred");
        ArrayList<String> stringList = new ArrayList<>();
        when(idRepoAppException.getCodes()).thenReturn(stringList);
        when(idRepoAppException.getErrorTexts()).thenReturn(new ArrayList<>());
        ResponseEntity<Object> actualIdRepoAppExceptionFailedResult = residentVidExceptionHandler
                .idRepoAppExceptionFailed(mockedRequest, idRepoAppException);
        assertTrue(actualIdRepoAppExceptionFailedResult.hasBody());
        assertTrue(actualIdRepoAppExceptionFailedResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualIdRepoAppExceptionFailedResult.getStatusCode());
        assertNull(((ResponseWrapper<Object>) actualIdRepoAppExceptionFailedResult.getBody()).getResponse());
        assertNull(((ResponseWrapper<Object>) actualIdRepoAppExceptionFailedResult.getBody()).getId());
        assertEquals(stringList, ((ResponseWrapper<Object>) actualIdRepoAppExceptionFailedResult.getBody()).getErrors());
        assertEquals("Property", ((ResponseWrapper<Object>) actualIdRepoAppExceptionFailedResult.getBody()).getVersion());
        verify(environment, atLeast(1)).getProperty((String) any());
        verify(idRepoAppException).getErrorCode();
        verify(idRepoAppException).getMessage();
        verify(idRepoAppException).getCodes();
        verify(idRepoAppException).getErrorTexts();
    }
}

