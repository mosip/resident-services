package io.mosip.testrig.apirig.resident.testscripts;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import io.mosip.testrig.apirig.dto.OutputValidationDto;
import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.resident.utils.ResidentConfigManager;
import io.mosip.testrig.apirig.resident.utils.ResidentUtil;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.apirig.testrunner.HealthChecker;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AuthenticationTestException;
import io.mosip.testrig.apirig.utils.GlobalConstants;
import io.mosip.testrig.apirig.utils.GlobalMethods;
import io.mosip.testrig.apirig.utils.OutputValidationUtil;
import io.mosip.testrig.apirig.utils.ReportUtil;
import io.mosip.testrig.apirig.utils.RestClient;
import io.mosip.testrig.apirig.utils.SecurityXSSException;
import io.restassured.response.Response;

public class PostWithBodyWithOtpGenerateAndPdfDownload extends ResidentUtil implements ITest {
	private static final Logger logger = Logger.getLogger(PostWithBodyWithOtpGenerateAndPdfDownload.class);
	protected String testCaseName = "";
	public byte[] pdf = null;
	public String pdfAsText = null;

	@BeforeClass
	public static void setLogLevel() {
		if (ResidentConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	/**
	 * get current testcaseName
	 */
	@Override
	public String getTestName() {
		return testCaseName;
	}

	/**
	 * Data provider class provides test case list
	 * 
	 * @return object of data provider
	 */
	@DataProvider(name = "testcaselist")
	public Object[] getTestCaseList(ITestContext context) {
		String ymlFile = context.getCurrentXmlTest().getLocalParameters().get("ymlFile");
		logger.info("Started executing yml: " + ymlFile);
		return getYmlTestData(ymlFile);
	}

	/**
	 * Test method for OTP Generation execution
	 * 
	 * @param objTestParameters
	 * @param testScenario
	 * @param testcaseName
	 * @throws AuthenticationTestException
	 * @throws AdminTestException
	 */
	@Test(dataProvider = "testcaselist")
	public void test(TestCaseDTO testCaseDTO) throws AuthenticationTestException, AdminTestException, SecurityXSSException {
		testCaseName = testCaseDTO.getTestCaseName();
		testCaseName = ResidentUtil.isTestCaseValidForExecution(testCaseDTO);
		boolean isNegative = testCaseName.toLowerCase().contains("_neg");
		if (HealthChecker.signalTerminateExecution) {
			throw new SkipException(
					GlobalConstants.TARGET_ENV_HEALTH_CHECK_FAILED + HealthChecker.healthCheckFailureMapS);
		}

		if (testCaseDTO.getTestCaseName().contains("VID") || testCaseDTO.getTestCaseName().contains("Vid")) {
			if (!BaseTestCase.getSupportedIdTypesValueFromActuator().contains("VID")
					&& !BaseTestCase.getSupportedIdTypesValueFromActuator().contains("vid")) {
				throw new SkipException(GlobalConstants.VID_FEATURE_NOT_SUPPORTED);
			}
		}
		JSONObject req = new JSONObject(testCaseDTO.getInput());
		String otpRequest = null;
		String sendOtpReqTemplate = null;
		String sendOtpEndPoint = null;
		if (req.has(GlobalConstants.SENDOTP)) {
			otpRequest = req.get(GlobalConstants.SENDOTP).toString();
			req.remove(GlobalConstants.SENDOTP);
		}
		JSONObject otpReqJson = new JSONObject(otpRequest);
		sendOtpReqTemplate = otpReqJson.getString("sendOtpReqTemplate");
		otpReqJson.remove("sendOtpReqTemplate");
		sendOtpEndPoint = otpReqJson.getString("sendOtpEndPoint");
		otpReqJson.remove("sendOtpEndPoint");

		Response otpResponse = postWithBodyAndCookie(ApplnURI + sendOtpEndPoint,
				getJsonFromTemplate(otpReqJson.toString(), sendOtpReqTemplate), COOKIENAME, GlobalConstants.RESIDENT,
				testCaseDTO.getTestCaseName());

		JSONObject res = new JSONObject(testCaseDTO.getOutput());
		String sendOtpResp = null, sendOtpResTemplate = null;
		if (res.has(GlobalConstants.SENDOTPRESP)) {
			sendOtpResp = res.get(GlobalConstants.SENDOTPRESP).toString();
			res.remove(GlobalConstants.SENDOTPRESP);
		}
		JSONObject sendOtpRespJson = new JSONObject(sendOtpResp);
		sendOtpResTemplate = sendOtpRespJson.getString("sendOtpResTemplate");
		sendOtpRespJson.remove("sendOtpResTemplate");
		Map<String, List<OutputValidationDto>> ouputValidOtp = OutputValidationUtil.doJsonOutputValidation(
				otpResponse.asString(), getJsonFromTemplate(sendOtpRespJson.toString(), sendOtpResTemplate),
				testCaseDTO, otpResponse.getStatusCode());
		Reporter.log(ReportUtil.getOutputValidationReport(ouputValidOtp));

		if (!OutputValidationUtil.publishOutputResult(ouputValidOtp))
			throw new AdminTestException("Failed at otp output validation");

		pdf = postWithBodyAndCookieForPdf(ApplnURI + testCaseDTO.getEndPoint(),
				getJsonFromTemplate(req.toString(), testCaseDTO.getInputTemplate()), COOKIENAME,
				testCaseDTO.getRole(), testCaseDTO.getTestCaseName());
		Response response = RestClient.getPdfDownloadResponse();
		String contentType = response != null ? response.getHeader("Content-Type") : null;
		String rawResponse = pdf != null ? new String(pdf) : null;

		if (!isNegative) {
			if (contentType != null && contentType.contains("application/pdf")) {

				try {
					PdfReader reader;
	            	try {
	                    //First opening pdf without password
	                    reader = new PdfReader(new ByteArrayInputStream(pdf));
	                    if (!reader.isEncrypted()) {
							logger.info("Opened non-encrypted PDF");
						} else {
							reader.close();
							throw new com.itextpdf.text.exceptions.BadPasswordException("Encrypted PDF");
						}

	                } catch (com.itextpdf.text.exceptions.BadPasswordException e) {

	                    // If encrypted, try with password
	                    String password = getPdfPassword();

	                    reader = new PdfReader(
	                            new ByteArrayInputStream(pdf),
	                            password.getBytes());

	                    logger.info("Opened password protected PDF");
	                }
	            	try {
						pdfAsText = PdfTextExtractor.getTextFromPage(reader, 1);
					} finally {
						reader.close();
					}

					GlobalMethods.reportResponse(null, ApplnURI + testCaseDTO.getEndPoint(),
							"PDF Content:\n" + pdfAsText);

				} catch (Exception e) {
					Assert.fail("Invalid PDF received: " + e.getMessage());
				}
			} else {
				GlobalMethods.reportResponse(null, ApplnURI + testCaseDTO.getEndPoint(),
						"Expected PDF but received JSON:\n" + ResidentUtil.formatJson(rawResponse));
				Assert.fail("Testcase failed. Expected PDF but got " + contentType);
			}

		} else {
			if (rawResponse == null || response == null) {
				Assert.fail("Testcase failed. Response is null.");
			} else {
				GlobalMethods.reportResponse(null, ApplnURI + testCaseDTO.getEndPoint(),
						ResidentUtil.formatJson(rawResponse));
				Map<String, List<OutputValidationDto>> ouputValid = OutputValidationUtil.doJsonOutputValidation(
						rawResponse, getJsonFromTemplate(testCaseDTO.getOutput(), testCaseDTO.getOutputTemplate()),
						testCaseDTO, response.getStatusCode());
				Reporter.log(ReportUtil.getOutputValidationReport(ouputValid));
				if (!OutputValidationUtil.publishOutputResult(ouputValid))
					throw new AdminTestException("Failed at output validation");
			}

		}

	}

	/**
	 * The method ser current test name to result
	 * 
	 * @param result
	 */
	@AfterMethod(alwaysRun = true)
	public void setResultTestName(ITestResult result) {
		result.setAttribute("TestCaseName", testCaseName);
	}
}
