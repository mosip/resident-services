package io.mosip.resident.exception;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import io.mosip.resident.mock.exception.CantPlaceOrderException;
import io.mosip.resident.mock.exception.PaymentCanceledException;
import io.mosip.resident.mock.exception.PaymentFailedException;
import io.mosip.resident.mock.exception.TechnicalErrorException;
import io.mosip.resident.util.ObjectWithMetadata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.authcodeflowproxy.api.exception.AuthRestException;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;

import static io.mosip.resident.constant.ResidentConstants.CHECK_STATUS_ID;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	Environment env;

	private static final Logger logger = LoggerConfiguration.logConfig(ApiExceptionHandler.class);

	private static final String EUIN = "resident.euin.id";
	private static final String PRINT_UIN = "resident.printuin.id";
	private static final String UIN = "resident.uin.id";
	private static final String RID = "resident.rid.id";
	private static final String UPDATE_UIN = "resident.updateuin.id";
	private static final String VID = "resident.vid.id";
	private static final String AUTH_LOCK = "resident.authlock.id";
	private static final String AUTH_UNLOCK = "resident.authunlock.id";
	private static final String AUTH_HISTORY = "resident.authhistory.id";
	private static final String RESIDENT_VERSION = "resident.vid.version";

	@ExceptionHandler(ResidentServiceException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> controlDataServiceException(
			HttpServletRequest httpServletRequest, final ResidentServiceException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		logStackTrace(e);
		return getErrorResponseEntity(httpServletRequest, e, HttpStatus.OK);
	}

	@ExceptionHandler(ResidentCredentialServiceException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> controlDataServiceException(
			HttpServletRequest httpServletRequest, final ResidentCredentialServiceException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		logStackTrace(e);
		return getErrorResponseEntity(httpServletRequest, e, HttpStatus.OK);
	}

	@ExceptionHandler(DataNotFoundException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> controlDataNotFoundException(
			HttpServletRequest httpServletRequest, final DataNotFoundException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		logStackTrace(e);
		return getErrorResponseEntity(httpServletRequest, e, HttpStatus.OK);
	}

	@ExceptionHandler(RequestException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> controlRequestException(HttpServletRequest httpServletRequest,
			final RequestException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		logStackTrace(e);
		return getErrorResponseEntity(httpServletRequest, e, HttpStatus.OK);
	}

	private ResponseEntity<ResponseWrapper<ServiceError>> getErrorResponseEntity(HttpServletRequest httpServletRequest,
			BaseUncheckedException e, HttpStatus httpStatus) throws IOException {
		ServiceError error = new ServiceError(e.getErrorCode(), e.getErrorText());
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		errorResponse.getErrors().add(error);
		return createResponseEntity(errorResponse, e, httpStatus);
	}

	private ResponseEntity<ResponseWrapper<ServiceError>> createResponseEntity(
			ResponseWrapper<ServiceError> errorResponse, Exception e, HttpStatus httpStatus) {
		if (e instanceof ObjectWithMetadata && ((ObjectWithMetadata) e).getMetadata() != null
				&& ((ObjectWithMetadata) e).getMetadata().containsKey(ResidentConstants.EVENT_ID)) {
			MultiValueMap<String, String> headers = new HttpHeaders();
			headers.add(ResidentConstants.EVENT_ID,
					(String) ((ObjectWithMetadata) e).getMetadata().get(ResidentConstants.EVENT_ID));
			return new ResponseEntity<>(errorResponse, headers, httpStatus);
		}
		return new ResponseEntity<>(errorResponse, httpStatus);
	}

	private ResponseEntity<ResponseWrapper<ServiceError>> getCheckedErrorEntity(HttpServletRequest httpServletRequest,
			BaseCheckedException e, HttpStatus httpStatus) throws IOException {
		ServiceError error = new ServiceError(e.getErrorCode(), e.getErrorText());
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		errorResponse.getErrors().add(error);
		return createResponseEntity(errorResponse, e, httpStatus);
	}

	@ExceptionHandler(InvalidInputException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> controlRequestException(HttpServletRequest httpServletRequest,
			final InvalidInputException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		logStackTrace(e);
		return getErrorResponseEntity(httpServletRequest, e, HttpStatus.OK);
	}

	@ExceptionHandler(IdRepoAppException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> controlRequestException(HttpServletRequest httpServletRequest,
			final IdRepoAppException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		logStackTrace(e);
		return getErrorResponseEntity(httpServletRequest, e, HttpStatus.OK);
	}

	@ExceptionHandler(OtpValidationFailedException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> controlRequestException(HttpServletRequest httpServletRequest,
			final OtpValidationFailedException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		logStackTrace(e);
		return getCheckedErrorEntity(httpServletRequest, e, HttpStatus.OK);
	}

	@ExceptionHandler(TokenGenerationFailedException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> controlRequestException(HttpServletRequest httpServletRequest,
			final TokenGenerationFailedException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		logStackTrace(e);
		return getErrorResponseEntity(httpServletRequest, e, HttpStatus.OK);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> methodArgumentNotValidException(
			final HttpServletRequest httpServletRequest, final MethodArgumentNotValidException e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		final List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
		fieldErrors.forEach(x -> {
			ServiceError error = new ServiceError(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
					x.getField() + ": " + x.getDefaultMessage());
			errorResponse.getErrors().add(error);
		});
		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
	}

	@ExceptionHandler(PaymentFailedException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> controlRequestException(HttpServletRequest httpServletRequest,
																				 final PaymentFailedException e) throws IOException{
		ExceptionUtils.logRootCause(e);
		logStackTrace(e);
		return getErrorResponseEntity(httpServletRequest, e, HttpStatus.PAYMENT_REQUIRED);
	}

	@ExceptionHandler(PaymentCanceledException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> controlRequestException(HttpServletRequest httpServletRequest,
																				 final PaymentCanceledException e) throws IOException{
		ExceptionUtils.logRootCause(e);
		logStackTrace(e);
		return getErrorResponseEntity(httpServletRequest, e, HttpStatus.PAYMENT_REQUIRED);
	}

	@ExceptionHandler(TechnicalErrorException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> controlRequestException(HttpServletRequest httpServletRequest,
																				 final TechnicalErrorException e) throws IOException{
		ExceptionUtils.logRootCause(e);
		logStackTrace(e);
		return getErrorResponseEntity(httpServletRequest, e, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(CantPlaceOrderException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> controlRequestException(HttpServletRequest httpServletRequest,
																				 final CantPlaceOrderException e) throws IOException{
		ExceptionUtils.logRootCause(e);
		logStackTrace(e);
		return getErrorResponseEntity(httpServletRequest, e, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(CardNotReadyException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> controlRequestException(HttpServletRequest httpServletRequest,
																				 final CardNotReadyException e) throws IOException{
		ExceptionUtils.logRootCause(e);
		logStackTrace(e);
		return getErrorResponseEntity(httpServletRequest, e, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> onHttpMessageNotReadable(
			final HttpServletRequest httpServletRequest, final HttpMessageNotReadableException e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(ResidentErrorCode.BAD_REQUEST.getErrorCode(), e.getMessage());
		errorResponse.getErrors().add(error);
		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
	}

	@ExceptionHandler(value = { Exception.class, RuntimeException.class })
	public ResponseEntity<ResponseWrapper<ServiceError>> defaultErrorHandler(HttpServletRequest httpServletRequest,
			Exception exception) throws IOException {
		if(exception instanceof AuthRestException) {
			return  new ResponseEntity<ResponseWrapper<ServiceError>>(getAuthFailedResponse(), HttpStatus.UNAUTHORIZED);
		}
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(ResidentErrorCode.BAD_REQUEST.getErrorCode(), exception.getMessage());
		errorResponse.getErrors().add(error);
		ExceptionUtils.logRootCause(exception);
		logStackTrace(exception);
		return createResponseEntity(errorResponse, exception, HttpStatus.OK);
	}

	private ResponseWrapper<ServiceError> getAuthFailedResponse() {
		ResponseWrapper<ServiceError> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponsetime(DateUtils.getUTCCurrentDateTime());
		responseWrapper
				.setErrors(List.of(new ServiceError(ResidentErrorCode.UNAUTHORIZED.getErrorCode(),
						ResidentErrorCode.UNAUTHORIZED.getErrorMessage())));
		return responseWrapper;
	}
	
	@ExceptionHandler(RIDInvalidException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> getRidStackTraceHandler(
			final HttpServletRequest httpServletRequest, final RIDInvalidException e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(e.getErrorCode(), e.getErrorText());
		errorResponse.getErrors().add(error);
		ExceptionUtils.logRootCause(e);
		logStackTrace(e);
		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
	}
	
	@ExceptionHandler(ResidentServiceCheckedException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> getResidentServiceStackTraceHandler(
			final HttpServletRequest httpServletRequest, final ResidentServiceCheckedException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		logStackTrace(e);
		return getCheckedErrorEntity(httpServletRequest, e, HttpStatus.OK);
	}

	@ExceptionHandler(ApisResourceAccessException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> getApiResourceStackTraceHandler(
			final HttpServletRequest httpServletRequest, final ApisResourceAccessException e) throws IOException {
		if(e.getCause() instanceof HttpClientErrorException 
				&& ((HttpClientErrorException)e.getCause()).getRawStatusCode() == HttpStatus.UNAUTHORIZED.value()) {
			return  new ResponseEntity<ResponseWrapper<ServiceError>>(getAuthFailedResponse(), HttpStatus.UNAUTHORIZED);
		}
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(ResidentErrorCode.BAD_REQUEST.getErrorCode(), e.getMessage());
		errorResponse.getErrors().add(error);
		ExceptionUtils.logRootCause(e);
		logStackTrace(e);
		return createResponseEntity(errorResponse, e, HttpStatus.OK);
	}

	private static void logStackTrace(Exception e) {
		logger.error(ExceptionUtils.getStackTrace(e));
	}

	private ResponseWrapper<ServiceError> setErrors(HttpServletRequest httpServletRequest) throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponsetime(LocalDateTime.now(ZoneId.of("UTC")));
		String requestBody = null;
		if (httpServletRequest instanceof ContentCachingRequestWrapper) {
			requestBody = new String(((ContentCachingRequestWrapper) httpServletRequest).getContentAsByteArray());
		}
		if (EmptyCheckUtils.isNullEmpty(requestBody)) {
			return responseWrapper;
		}
		objectMapper.registerModule(new JavaTimeModule());
		responseWrapper.setId(setId(httpServletRequest.getRequestURI()));
		responseWrapper.setVersion(env.getProperty(RESIDENT_VERSION));
		return responseWrapper;
	}

	private String setId(String requestURI) {
		Map<String, String> idMap = new HashMap<>();
		idMap.put("/check-status", env.getProperty(CHECK_STATUS_ID));
		idMap.put("/euin", env.getProperty(EUIN));
		idMap.put("/print-uin", env.getProperty(PRINT_UIN));
		idMap.put("/uin", env.getProperty(UIN));
		idMap.put("/rid", env.getProperty(RID));
		idMap.put("/update-uin", env.getProperty(UPDATE_UIN));
		idMap.put("/vid", env.getProperty(VID));
		idMap.put("/auth-lock", env.getProperty(AUTH_LOCK));
		idMap.put("/auth-unlock", env.getProperty(AUTH_UNLOCK));
		idMap.put("/auth-history", env.getProperty(AUTH_HISTORY));

		for (Map.Entry<String, String> entry : idMap.entrySet()) {
			if (requestURI.contains(entry.getKey())) {
				return entry.getValue();
			}
		}
		return null;
	}

}