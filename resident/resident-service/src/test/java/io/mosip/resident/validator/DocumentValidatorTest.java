package io.mosip.resident.validator;

import static io.mosip.resident.constant.ResidentErrorCode.INVALID_INPUT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.virusscanner.exception.VirusScannerException;
import io.mosip.kernel.core.virusscanner.spi.VirusScanner;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.dto.DocumentRequestDTO;
import io.mosip.resident.exception.ResidentServiceException;

/**
 * @author Manoj SP
 *
 */
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration
public class DocumentValidatorTest {
	
	@InjectMocks
	private DocumentValidator validator;

	@Mock
	private VirusScanner<Boolean, InputStream> virusScanner;

	private MockEnvironment env = new MockEnvironment();
	
	@Before
	public void init() {
		ReflectionTestUtils.setField(validator, "env", env);
	}
	
	@Test
	public void testValidateRequestSuccess() {
		DocumentRequestDTO request = new DocumentRequestDTO();
		request.setDocCatCode("a");
		request.setDocTypCode("b");
		request.setLangCode("c");
		validator.validateRequest(request);
	}
	
	@Test
	public void testNullRequest() {
		try {
			validator.validate(env, null);
			validator.validateRequest(null);
		} catch (NullPointerException e) {
			assertEquals(String.format(INVALID_INPUT.getErrorMessage() + "request"), e.getMessage());
		}
	}
	
	@Test
	public void testNullDocCatCode() {
		try {
			DocumentRequestDTO request = new DocumentRequestDTO();
			request.setDocCatCode(null);
			validator.validateRequest(request);
		} catch (NullPointerException e) {
			assertEquals(String.format(INVALID_INPUT.getErrorMessage() + "request/docCatCode"), e.getMessage());
		}
	}
	
	@Test
	public void testBlankDocCatCode() {
		try {
			DocumentRequestDTO request = new DocumentRequestDTO();
			request.setDocCatCode(" ");
			validator.validateRequest(request);
		} catch (NullPointerException e) {
			assertEquals(String.format(INVALID_INPUT.getErrorMessage() + "request/docCatCode"), e.getMessage());
		}
	}
	
	@Test
	public void testNullDocTypCode() {
		try {
			DocumentRequestDTO request = new DocumentRequestDTO();
			request.setDocCatCode("a");
			request.setDocTypCode(null);
			validator.validateRequest(request);
		} catch (NullPointerException e) {
			assertEquals(String.format(INVALID_INPUT.getErrorMessage() + "request/docTypCode"), e.getMessage());
		}
	}
	
	@Test
	public void testBlankDocTypCode() {
		try {
			DocumentRequestDTO request = new DocumentRequestDTO();
			request.setDocCatCode("a");
			request.setDocTypCode(" ");
			validator.validateRequest(request);
		} catch (NullPointerException e) {
			assertEquals(String.format(INVALID_INPUT.getErrorMessage() + "request/docTypCode"), e.getMessage());
		}
	}
	
	@Test
	public void testNullLangCode() {
		try {
			DocumentRequestDTO request = new DocumentRequestDTO();
			request.setDocCatCode("a");
			request.setDocTypCode("b");
			request.setLangCode(null);
			validator.validateRequest(request);
		} catch (NullPointerException e) {
			assertEquals(String.format(INVALID_INPUT.getErrorMessage() + "request/langCode"), e.getMessage());
		}
	}
	
	@Test
	public void testBlankLangCode() {
		try {
			DocumentRequestDTO request = new DocumentRequestDTO();
			request.setDocCatCode("a");
			request.setDocTypCode("b");
			request.setLangCode(" ");
			validator.validateRequest(request);
		} catch (NullPointerException e) {
			assertEquals(String.format(INVALID_INPUT.getErrorMessage() + "request/langCode"), e.getMessage());
		}
	}
	
	@Test
	public void testScanForVirusesSuccess() {
		env.setProperty(ResidentConstants.VIRUS_SCANNER_ENABLED, "true");
		when(virusScanner.scanFile(any(InputStream.class))).thenReturn(true);
		validator.scanForViruses(new MockMultipartFile("name", "a".getBytes()));
	}
	
	@Test
	public void testScanForVirusesDisabledSuccess() {
		env.setProperty(ResidentConstants.VIRUS_SCANNER_ENABLED, "false");
		validator.scanForViruses(new MockMultipartFile("name", "a".getBytes()));
	}
	
	@Test(expected = ResidentServiceException.class)
	public void testScanForVirusesFailed() {
		env.setProperty(ResidentConstants.VIRUS_SCANNER_ENABLED, "true");
		when(virusScanner.scanFile(any(InputStream.class))).thenThrow(new VirusScannerException());
		validator.scanForViruses(new MockMultipartFile("name", "a".getBytes()));
	}
	
	@Test
	public void testSupportsSuccess() {
		assertTrue(validator.supports(RequestWrapper.class));
	}
	
	@Test
	public void testSupportsFailed() {
		assertFalse(validator.supports(MockMultipartFile.class));
	}

	@Test
	public void testValidateGetDocumentByDocumentIdInputSuccess() {
		String transactionId = "123";
		validator.validateGetDocumentByDocumentIdInput(transactionId);
	}

	@Test(expected = ResidentServiceException.class)
	public void testValidateGetDocumentByDocumentIdFailed() {
		String transactionId = "123a";
		validator.validateGetDocumentByDocumentIdInput(transactionId);
	}
}