package io.mosip.resident.exception;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.controller.ResidentVidController;
import io.mosip.resident.dto.ResponseWrapper;

@RestControllerAdvice(assignableTypes = ResidentVidController.class)
public class ResidentVidExceptionHandler {

	private static final String RESIDENT_VID_ID = "resident.vid.id";
	private static final String RESIDENT_VID_VERSION = "resident.vid.version";
	private static final String RESIDENT_REVOKE_VID_ID = "resident.revokevid.id";

	@Autowired
	private Environment env;

	private static Logger logger = LoggerConfiguration.logConfig(ResidentVidExceptionHandler.class);

	@ExceptionHandler(ResidentServiceCheckedException.class)
	public ResponseEntity<Object> residentCheckedException(HttpServletRequest httpServletRequest,
			ResidentServiceCheckedException e) {
		logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				e.getErrorCode(), e.getMessage());
		return buildRegStatusExceptionResponse(httpServletRequest, (Exception) e);
	}

	@ExceptionHandler(ResidentServiceException.class)
	public ResponseEntity<Object> residentServiceException(HttpServletRequest httpServletRequest,
			ResidentServiceException e) {
		logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				e.getErrorCode(), e.getMessage());
		return buildRegStatusExceptionResponse(httpServletRequest, (Exception) e);
	}

	@ExceptionHandler(VidAlreadyPresentException.class)
	public ResponseEntity<Object> vidAlreadyPresent(HttpServletRequest httpServletRequest,
			VidAlreadyPresentException e) {
		logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				e.getErrorCode(), e.getMessage());
		return buildRegStatusExceptionResponse(httpServletRequest, (Exception) e);
	}

	@ExceptionHandler(VidCreationException.class)
	public ResponseEntity<Object> vidCreationFailed(HttpServletRequest httpServletRequest, VidCreationException e) {
		logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				e.getErrorCode(), e.getMessage());
		return buildRegStatusExceptionResponse(httpServletRequest, (Exception) e);
	}

	@ExceptionHandler(ApisResourceAccessException.class)
	public ResponseEntity<Object> apiNotAccessible(HttpServletRequest httpServletRequest,
			ApisResourceAccessException e) {
		logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				e.getErrorCode(), e.getMessage());
		return buildRegStatusExceptionResponse(httpServletRequest, (Exception) e);
	}

	@ExceptionHandler(OtpValidationFailedException.class)
	public ResponseEntity<Object> otpValidationFailed(HttpServletRequest httpServletRequest,
			OtpValidationFailedException e) {
		logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				e.getErrorCode(), e.getMessage());
		return buildRegStatusExceptionResponse(httpServletRequest, (Exception) e);
	}

	@ExceptionHandler(InvalidInputException.class)
	public ResponseEntity<Object> invalidInput(HttpServletRequest httpServletRequest, InvalidInputException e) {
		logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				e.getErrorCode(), e.getMessage());
		return buildRegStatusExceptionResponse(httpServletRequest, (Exception) e);
	}

	@ExceptionHandler(VidRevocationException.class)
	public ResponseEntity<Object> vidRevocationFailed(HttpServletRequest httpServletRequest, VidRevocationException e) {
		logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				e.getErrorCode(), e.getMessage());
		return buildRegStatusExceptionResponse(httpServletRequest, (Exception) e);
	}

	@ExceptionHandler(IdRepoAppException.class)
	public ResponseEntity<Object> idRepoAppExceptionFailed(HttpServletRequest httpServletRequest,
			IdRepoAppException e) {
		logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				e.getErrorCode(), e.getMessage());
		return buildRegStatusExceptionResponse(httpServletRequest, (Exception) e);
	}

	private ResponseEntity<Object> buildRegStatusExceptionResponse(HttpServletRequest httpServletRequest,
			Exception ex) {

		ResponseWrapper response = new ResponseWrapper();
		Throwable e = ex;

		if (e instanceof BaseCheckedException) {
			List<String> errorCodes = ((BaseCheckedException) e).getCodes();
			List<String> errorTexts = ((BaseCheckedException) e).getErrorTexts();

			List<ServiceError> errors = errorTexts.parallelStream()
					.map(errMsg -> new ServiceError(errorCodes.get(errorTexts.indexOf(errMsg)), errMsg)).distinct()
					.collect(Collectors.toList());

			response.setErrors(errors);
		} else if (e instanceof BaseUncheckedException) {
			List<String> errorCodes = ((BaseUncheckedException) e).getCodes();
			List<String> errorTexts = ((BaseUncheckedException) e).getErrorTexts();

			List<ServiceError> errors = errorTexts.parallelStream()
					.map(errMsg -> new ServiceError(errorCodes.get(errorTexts.indexOf(errMsg)), errMsg)).distinct()
					.collect(Collectors.toList());

			response.setErrors(errors);
		}
		response.setId(setId(httpServletRequest.getRequestURI()));
		response.setVersion(env.getProperty(RESIDENT_VID_VERSION));
		response.setResponsetime(DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime()));
		response.setResponse(null);

		return ResponseEntity.status(HttpStatus.OK).body(response);

	}

	private String setId(String requestURI) {
		Map<String, String> idMap = new HashMap<>();
		idMap.put("/vid/", env.getProperty(RESIDENT_REVOKE_VID_ID));
		idMap.put("/vid", env.getProperty(RESIDENT_VID_ID));

		for (Map.Entry<String, String> entry : idMap.entrySet()) {
			if (requestURI.contains(entry.getKey())) {
				return entry.getValue();
			}
		}
		return null;
	}

}
