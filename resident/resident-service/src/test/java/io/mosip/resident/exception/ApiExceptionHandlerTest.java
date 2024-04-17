package io.mosip.resident.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.openid.bridge.api.exception.AuthRestException;
import io.mosip.kernel.openid.bridge.api.exception.ClientException;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.mock.exception.CantPlaceOrderException;
import io.mosip.resident.mock.exception.PaymentCanceledException;
import io.mosip.resident.mock.exception.PaymentFailedException;
import io.mosip.resident.mock.exception.TechnicalErrorException;
import org.apache.struts.mock.MockHttpServletRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author Kamesh Shekhar Prasad
 */
@ContextConfiguration(classes = {ApiExceptionHandler.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class ApiExceptionHandlerTest {
    @Autowired
    private ApiExceptionHandler apiExceptionHandler;

    @MockBean
    private Environment environment;

    @MockBean
    private ObjectMapper objectMapper;

    @Test
    public void testControlDataServiceException() throws IOException {
        ResponseEntity<ResponseWrapper<ServiceError>> actualControlDataServiceExceptionResult = apiExceptionHandler
                .controlDataServiceException(null, new ResidentServiceException(ResidentErrorCode.NO_RID_FOUND_EXCEPTION));
        assertTrue(actualControlDataServiceExceptionResult.hasBody());
        assertTrue(actualControlDataServiceExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualControlDataServiceExceptionResult.getStatusCode());
        List<ServiceError> errors = actualControlDataServiceExceptionResult.getBody().getErrors();
        assertEquals(1, errors.size());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-408", getResult.getErrorCode());
        assertEquals("RID not found", getResult.getMessage());
    }

    @Test
    public void testControlDataServiceException2() throws IOException {
        ResponseEntity<ResponseWrapper<ServiceError>> actualControlDataServiceExceptionResult = apiExceptionHandler
                .controlDataServiceException(null, new ResidentCredentialServiceException(ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorCode(),
                        ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorMessage()));
        assertTrue(actualControlDataServiceExceptionResult.hasBody());
        assertTrue(actualControlDataServiceExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualControlDataServiceExceptionResult.getStatusCode());
        List<ServiceError> errors = actualControlDataServiceExceptionResult.getBody().getErrors();
        assertEquals(1, errors.size());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-408", getResult.getErrorCode());
        assertEquals("RID not found", getResult.getMessage());
    }

    @Test
    public void testControlDataNotFoundException() throws IOException {
        ResponseEntity<ResponseWrapper<ServiceError>> actualControlDataServiceExceptionResult = apiExceptionHandler
                .controlDataNotFoundException(null, new DataNotFoundException(ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorCode(),
                        ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorMessage()));
        assertTrue(actualControlDataServiceExceptionResult.hasBody());
        assertTrue(actualControlDataServiceExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualControlDataServiceExceptionResult.getStatusCode());
        List<ServiceError> errors = actualControlDataServiceExceptionResult.getBody().getErrors();
        assertEquals(1, errors.size());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-408", getResult.getErrorCode());
        assertEquals("RID not found", getResult.getMessage());
    }

    @Test
    public void testControlRequestException() throws IOException {
        ResponseEntity<ResponseWrapper<ServiceError>> actualControlDataServiceExceptionResult = apiExceptionHandler
                .controlRequestException(null, new RequestException(ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorCode(),
                        ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorMessage()));
        assertTrue(actualControlDataServiceExceptionResult.hasBody());
        assertTrue(actualControlDataServiceExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualControlDataServiceExceptionResult.getStatusCode());
        List<ServiceError> errors = actualControlDataServiceExceptionResult.getBody().getErrors();
        assertEquals(1, errors.size());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-408", getResult.getErrorCode());
        assertEquals("RID not found", getResult.getMessage());
    }

    @Test
    public void testControlRequestException2() throws IOException {
        ResponseEntity<ResponseWrapper<ServiceError>> actualControlDataServiceExceptionResult = apiExceptionHandler
                .controlRequestException(null, new IdRepoAppException(ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorCode(),
                        ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorMessage()));
        assertTrue(actualControlDataServiceExceptionResult.hasBody());
        assertTrue(actualControlDataServiceExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualControlDataServiceExceptionResult.getStatusCode());
        List<ServiceError> errors = actualControlDataServiceExceptionResult.getBody().getErrors();
        assertEquals(1, errors.size());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-408", getResult.getErrorCode());
        assertEquals("RID not found", getResult.getMessage());
    }

    @Test
    public void testMethodArgumentNotValidException2() throws IOException {
        DefaultMultipartHttpServletRequest httpServletRequest = mock(DefaultMultipartHttpServletRequest.class);
        ResponseEntity<ResponseWrapper<ServiceError>> actualMethodArgumentNotValidExceptionResult = apiExceptionHandler
                .methodArgumentNotValidException(httpServletRequest,
                        new MethodArgumentNotValidException(null, new BindException("Target", "Object Name")));
        assertTrue(actualMethodArgumentNotValidExceptionResult.hasBody());
        assertTrue(actualMethodArgumentNotValidExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualMethodArgumentNotValidExceptionResult.getStatusCode());
        assertTrue(actualMethodArgumentNotValidExceptionResult.getBody().getErrors().isEmpty());
    }

    @Test
    public void testTokenGenerationFailedException() throws IOException {
        ResponseEntity<ResponseWrapper<ServiceError>> actualControlDataServiceExceptionResult = apiExceptionHandler
                .controlRequestException(null, new TokenGenerationFailedException());
        assertTrue(actualControlDataServiceExceptionResult.hasBody());
        assertTrue(actualControlDataServiceExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualControlDataServiceExceptionResult.getStatusCode());
        List<ServiceError> errors = actualControlDataServiceExceptionResult.getBody().getErrors();
        assertEquals(1, errors.size());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-409", getResult.getErrorCode());
        assertEquals("Token generation failed", getResult.getMessage());
    }

    @Test
    public void testControlRequestException3() throws IOException {
        ResponseEntity<ResponseWrapper<ServiceError>> actualControlDataServiceExceptionResult = apiExceptionHandler
                .controlRequestException(null, new PaymentFailedException(ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorCode(),
                        ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorMessage()));
        assertTrue(actualControlDataServiceExceptionResult.hasBody());
        assertTrue(actualControlDataServiceExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.PAYMENT_REQUIRED, actualControlDataServiceExceptionResult.getStatusCode());
        List<ServiceError> errors = actualControlDataServiceExceptionResult.getBody().getErrors();
        assertEquals(1, errors.size());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-408", getResult.getErrorCode());
        assertEquals("RID not found", getResult.getMessage());
    }

    @Test
    public void testControlRequestException4() throws IOException {
        ResponseEntity<ResponseWrapper<ServiceError>> actualControlDataServiceExceptionResult = apiExceptionHandler
                .controlRequestException(null, new PaymentCanceledException(ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorCode(),
                        ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorMessage()));
        assertTrue(actualControlDataServiceExceptionResult.hasBody());
        assertTrue(actualControlDataServiceExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.PAYMENT_REQUIRED, actualControlDataServiceExceptionResult.getStatusCode());
        List<ServiceError> errors = actualControlDataServiceExceptionResult.getBody().getErrors();
        assertEquals(1, errors.size());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-408", getResult.getErrorCode());
        assertEquals("RID not found", getResult.getMessage());
    }

    @Test
    public void testControlRequestException5() throws IOException {
        ResponseEntity<ResponseWrapper<ServiceError>> actualControlDataServiceExceptionResult = apiExceptionHandler
                .controlRequestException(null, new TechnicalErrorException(ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorCode(),
                        ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorMessage()));
        assertTrue(actualControlDataServiceExceptionResult.hasBody());
        assertTrue(actualControlDataServiceExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualControlDataServiceExceptionResult.getStatusCode());
        List<ServiceError> errors = actualControlDataServiceExceptionResult.getBody().getErrors();
        assertEquals(1, errors.size());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-408", getResult.getErrorCode());
        assertEquals("RID not found", getResult.getMessage());
    }

    @Test
    public void testControlRequestException6() throws IOException {
        ResponseEntity<ResponseWrapper<ServiceError>> actualControlDataServiceExceptionResult = apiExceptionHandler
                .controlRequestException(null, new CantPlaceOrderException(ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorCode(),
                        ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorMessage()));
        assertTrue(actualControlDataServiceExceptionResult.hasBody());
        assertTrue(actualControlDataServiceExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualControlDataServiceExceptionResult.getStatusCode());
        List<ServiceError> errors = actualControlDataServiceExceptionResult.getBody().getErrors();
        assertEquals(1, errors.size());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-408", getResult.getErrorCode());
        assertEquals("RID not found", getResult.getMessage());
    }

    @Test
    public void testControlRequestException7() throws IOException {
        ResponseEntity<ResponseWrapper<ServiceError>> actualControlDataServiceExceptionResult = apiExceptionHandler
                .controlRequestException(null, new CardNotReadyException(ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorCode(),
                        ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorMessage()));
        assertTrue(actualControlDataServiceExceptionResult.hasBody());
        assertTrue(actualControlDataServiceExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.BAD_REQUEST, actualControlDataServiceExceptionResult.getStatusCode());
        List<ServiceError> errors = actualControlDataServiceExceptionResult.getBody().getErrors();
        assertEquals(1, errors.size());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-408", getResult.getErrorCode());
        assertEquals("RID not found", getResult.getMessage());
    }

    @Test
    public void testControlRequestException8() throws IOException {
        ResponseEntity<ResponseWrapper<ServiceError>> actualControlDataServiceExceptionResult = apiExceptionHandler
                .controlRequestException(null, new VidAlreadyPresentException(ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorCode(),
                        ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorMessage()));
        assertTrue(actualControlDataServiceExceptionResult.hasBody());
        assertTrue(actualControlDataServiceExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualControlDataServiceExceptionResult.getStatusCode());
        List<ServiceError> errors = actualControlDataServiceExceptionResult.getBody().getErrors();
        assertEquals(1, errors.size());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-408", getResult.getErrorCode());
        assertEquals("Maximum allowed VIDs are active. Deactivate VID to generate new one.", getResult.getMessage());
    }

    @Test
    public void testDefaultErrorHandler() throws IOException {
        ResponseEntity<ResponseWrapper<ServiceError>> actualControlDataServiceExceptionResult = apiExceptionHandler
                .defaultErrorHandler(null, new AuthRestException(List.of(
                        new ServiceError(ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorCode(),
                                ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorMessage())), HttpStatus.OK));
        assertTrue(actualControlDataServiceExceptionResult.hasBody());
        assertTrue(actualControlDataServiceExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.UNAUTHORIZED, actualControlDataServiceExceptionResult.getStatusCode());
        List<ServiceError> errors = actualControlDataServiceExceptionResult.getBody().getErrors();
        assertEquals(1, errors.size());
        ServiceError getResult = errors.get(0);
        assertEquals("KER-ATH-401", getResult.getErrorCode());
        assertEquals("Authentication Failed", getResult.getMessage());
    }

    @Test
    public void testDefaultErrorHandler2() throws IOException {
        ResponseEntity<ResponseWrapper<ServiceError>> actualControlDataServiceExceptionResult = apiExceptionHandler
                .defaultErrorHandler(null, new ClientException(ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorCode(),
                        ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorMessage()));
        assertTrue(actualControlDataServiceExceptionResult.hasBody());
        assertTrue(actualControlDataServiceExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.UNAUTHORIZED, actualControlDataServiceExceptionResult.getStatusCode());
        List<ServiceError> errors = actualControlDataServiceExceptionResult.getBody().getErrors();
        assertEquals(1, errors.size());
        ServiceError getResult = errors.get(0);
        assertEquals("KER-ATH-401", getResult.getErrorCode());
        assertEquals("Authentication Failed", getResult.getMessage());
    }

    @Test
    public void testGetRidStackTraceHandler() throws IOException {
        ResponseEntity<ResponseWrapper<ServiceError>> actualControlDataServiceExceptionResult = apiExceptionHandler
                .getRidStackTraceHandler(null, new RIDInvalidException(ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorCode(),
                        ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorMessage()));
        assertTrue(actualControlDataServiceExceptionResult.hasBody());
        assertTrue(actualControlDataServiceExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualControlDataServiceExceptionResult.getStatusCode());
        List<ServiceError> errors = actualControlDataServiceExceptionResult.getBody().getErrors();
        assertEquals(1, errors.size());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-408", getResult.getErrorCode());
        assertEquals("RID not found", getResult.getMessage());
    }

    @Test
    public void testControlRequestException9() throws IOException {
        ResponseEntity<ResponseWrapper<ServiceError>> actualControlDataServiceExceptionResult = apiExceptionHandler
                .controlRequestException(null, new EventIdNotPresentException(ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorCode(),
                        ResidentErrorCode.NO_RID_FOUND_EXCEPTION.getErrorMessage()));
        assertTrue(actualControlDataServiceExceptionResult.hasBody());
        assertTrue(actualControlDataServiceExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.BAD_REQUEST, actualControlDataServiceExceptionResult.getStatusCode());
        List<ServiceError> errors = actualControlDataServiceExceptionResult.getBody().getErrors();
        assertEquals(1, errors.size());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-408", getResult.getErrorCode());
        assertEquals("RID not found", getResult.getMessage());
    }

    @Test
    public void testControlRequestException10() throws IOException {
        ResponseEntity<ResponseWrapper<ServiceError>> actualControlDataServiceExceptionResult = apiExceptionHandler
                .controlRequestException(null, new EidNotBelongToSessionException());
        assertTrue(actualControlDataServiceExceptionResult.hasBody());
        assertTrue(actualControlDataServiceExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.BAD_REQUEST, actualControlDataServiceExceptionResult.getStatusCode());
        List<ServiceError> errors = actualControlDataServiceExceptionResult.getBody().getErrors();
        assertEquals(1, errors.size());
        ServiceError getResult = errors.get(0);
        assertEquals(ResidentErrorCode.EID_NOT_BELONG_TO_SESSION.getErrorCode(), getResult.getErrorCode());
        assertEquals(ResidentErrorCode.EID_NOT_BELONG_TO_SESSION.getErrorMessage(), getResult.getMessage());
    }

    @Test
    public void testControlRequestException11() throws IOException {
        ResponseEntity<ResponseWrapper<ServiceError>> actualControlDataServiceExceptionResult = apiExceptionHandler
                .controlRequestException(null, new DigitalCardRidNotFoundException());
        assertTrue(actualControlDataServiceExceptionResult.hasBody());
        assertTrue(actualControlDataServiceExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.BAD_REQUEST, actualControlDataServiceExceptionResult.getStatusCode());
        List<ServiceError> errors = actualControlDataServiceExceptionResult.getBody().getErrors();
        assertEquals(1, errors.size());
        ServiceError getResult = errors.get(0);
        assertEquals(ResidentErrorCode.DIGITAL_CARD_RID_NOT_FOUND.getErrorCode(), getResult.getErrorCode());
        assertEquals(ResidentErrorCode.DIGITAL_CARD_RID_NOT_FOUND.getErrorMessage(), getResult.getMessage());
    }

    @Test
    public void testGetApiResourceStackTraceHandler() throws IOException {
        ResponseEntity<ResponseWrapper<ServiceError>> actualApiResourceStackTraceHandler = apiExceptionHandler
                .getApiResourceStackTraceHandler(null, new ApisResourceAccessException("An error occurred"));
        assertTrue(actualApiResourceStackTraceHandler.hasBody());
        assertTrue(actualApiResourceStackTraceHandler.getHeaders().isEmpty());
        assertEquals(HttpStatus.BAD_REQUEST, actualApiResourceStackTraceHandler.getStatusCode());
        List<ServiceError> errors = actualApiResourceStackTraceHandler.getBody().getErrors();
        assertEquals(1, errors.size());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-412", getResult.getErrorCode());
        assertEquals("RES-SER-411 --> An error occurred", getResult.getMessage());
    }

    @Test
    public void testHandleAccessDeniedException() throws IOException {
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest("https://example.org/example",
                "https://example.org/example", "https://example.org/example", "https://example.org/example");

        ResponseEntity<ResponseWrapper<ServiceError>> actualHandleAccessDeniedExceptionResult = apiExceptionHandler
                .handleAccessDeniedException(httpServletRequest, new AccessDeniedException("Msg"));
        assertTrue(actualHandleAccessDeniedExceptionResult.hasBody());
        assertTrue(actualHandleAccessDeniedExceptionResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.FORBIDDEN, actualHandleAccessDeniedExceptionResult.getStatusCode());
        List<ServiceError> errors = actualHandleAccessDeniedExceptionResult.getBody().getErrors();
        assertEquals(1, errors.size());
        ServiceError getResult = errors.get(0);
        assertEquals("RES-SER-403", getResult.getErrorCode());
        assertEquals("Msg", getResult.getMessage());
    }

}

