package io.mosip.testrig.apirig.resident.testscripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.mosip.testrig.apirig.dbaccess.DBManager;
import io.mosip.testrig.apirig.dto.OutputValidationDto;
import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.resident.utils.ResidentConfigManager;
import io.mosip.testrig.apirig.resident.utils.ResidentUtil;
import io.mosip.testrig.apirig.testrunner.HealthChecker;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AuthenticationTestException;
import io.mosip.testrig.apirig.utils.GlobalConstants;
import io.mosip.testrig.apirig.utils.OutputValidationUtil;
import io.mosip.testrig.apirig.utils.ReportUtil;
import io.mosip.testrig.apirig.utils.SecurityXSSException;
import io.restassured.response.Response;

public class AuditValidator extends ResidentUtil implements ITest {
	private static final Logger logger = Logger.getLogger(AuditValidator.class);
	protected String testCaseName = "";
	public Response response = null;
	private String auditEventIds = null;
	private String auditAppId = "RES_SER";
	private static final long DEFAULT_AUDIT_EVENT_MAX_WAIT_SECONDS = 15;
	private static final int AUDIT_EVENT_POLL_INTERVAL_MS = 1000;

	/**
	 * get current testcaseName
	 */
	@Override
	public String getTestName() {
		return testCaseName;
	}

	@BeforeClass
	public static void setLogLevel() {
		if (ResidentConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	/*
	 * Data provider class provides test case list
	 * 
	 * @return object of data provider
	 */
	@DataProvider(name = "testcaselist")
	public Object[] getTestCaseList(ITestContext context) {
		String ymlFile = context.getCurrentXmlTest().getLocalParameters().get("ymlFile");
		logger.info("Started executing yml: " + ymlFile);
		Map<String, String> localParameters = context.getCurrentXmlTest().getLocalParameters();
		auditEventIds = localParameters.get("auditEventIds");
		if (localParameters.containsKey("auditAppId"))
			auditAppId = localParameters.get("auditAppId");
		return getYmlTestData(ymlFile);
	}

	@Test(dataProvider = "testcaselist")
	public void test(TestCaseDTO testCaseDTO) throws AuthenticationTestException, AdminTestException, SecurityXSSException {
		testCaseName = testCaseDTO.getTestCaseName();
		testCaseName = ResidentUtil.isTestCaseValidForExecution(testCaseDTO);
		if (HealthChecker.signalTerminateExecution) {
			throw new SkipException(
					GlobalConstants.TARGET_ENV_HEALTH_CHECK_FAILED + HealthChecker.healthCheckFailureMapS);
		}
		String query = testCaseDTO.getEndPoint();
		boolean isEventValidation = query.contains("$AUDITEVENTIDS$");
		if (isEventValidation) {
			if (ResidentUtil.ResidentAuditCheckpoint == null) {
				throw new SkipException("No audit checkpoint captured; run an AuditLogCheckpoint test before this one");
			}
			if (auditEventIds == null || auditEventIds.isBlank()) {
				throw new AdminTestException("auditEventIds parameter is missing for the suite entry");
			}
			query = query.replace("$AUDITCHECKPOINT$", ResidentUtil.ResidentAuditCheckpoint)
					.replace("$AUDITAPPID$", sanitizeSqlValue(auditAppId))
					.replace("$AUDITEVENTIDS$", toSqlInList(auditEventIds));
		}
		logger.info(query);
		Map<String, Object> response = DBManager.executeQueryAndGetRecord(testCaseDTO.getRole(), query);
		long deadline = System.currentTimeMillis() + getAuditEventMaxWaitSeconds() * 1000L;
		while (isEventValidation && getAuditEventCount(response) == 0 && System.currentTimeMillis() < deadline) {
			try {
				Thread.sleep(AUDIT_EVENT_POLL_INTERVAL_MS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
			response = DBManager.executeQueryAndGetRecord(testCaseDTO.getRole(), query);
		}

		Map<String, List<OutputValidationDto>> objMap = new HashMap<>();
		List<OutputValidationDto> objList = new ArrayList<>();
		OutputValidationDto objOpDto = new OutputValidationDto();
		String previousCheckpoint = ResidentUtil.ResidentAuditCheckpoint;

		Object checkpoint = response.get("checkpoint");
		boolean isCheckpointCaptured = checkpoint != null && !checkpoint.toString().isBlank();
		// Clear on a failed capture so the next validation skips instead of using a stale checkpoint
		if (isCheckpointCaptured) {
			ResidentUtil.ResidentAuditCheckpoint = checkpoint.toString();
		} else if (!isEventValidation) {
			ResidentUtil.ResidentAuditCheckpoint = null;
		}

		if (isEventValidation) {
			long eventCount = getAuditEventCount(response);
			objOpDto.setFieldName("audit events " + auditEventIds + " (app_id " + auditAppId + ") after " + previousCheckpoint + " UTC");
			objOpDto.setExpValue("> 0");
			objOpDto.setActualValue(String.valueOf(eventCount));
			objOpDto.setStatus(eventCount > 0 ? "PASS" : GlobalConstants.FAIL_STRING);
		} else {
			objOpDto.setFieldName("audit checkpoint");
			objOpDto.setExpValue("latest log_dtimes (UTC)");
			objOpDto.setActualValue(String.valueOf(checkpoint));
			objOpDto.setStatus(isCheckpointCaptured ? "PASS" : GlobalConstants.FAIL_STRING);
		}

		objList.add(objOpDto);
		objMap.put(GlobalConstants.EXPECTED_VS_ACTUAL, objList);
		Reporter.log(ReportUtil.getOutputValidationReport(objMap));

		if (!OutputValidationUtil.publishOutputResult(objMap))
			throw new AdminTestException("Failed at output validation");
	}

	// Audit entries are written asynchronously, so wait for them up to this limit
	private static long getAuditEventMaxWaitSeconds() {
		String maxWaitStr = ResidentConfigManager.getproperty("auditEventMaxWaitSeconds");
		if (maxWaitStr != null && !maxWaitStr.isBlank()) {
			try {
				return Long.parseLong(maxWaitStr.trim());
			} catch (NumberFormatException e) {
				logger.warn("Invalid auditEventMaxWaitSeconds property: " + maxWaitStr + ", using default: "
						+ DEFAULT_AUDIT_EVENT_MAX_WAIT_SECONDS);
			}
		}
		return DEFAULT_AUDIT_EVENT_MAX_WAIT_SECONDS;
	}

	private static long getAuditEventCount(Map<String, Object> response) {
		Object count = response.get("count");
		return count instanceof Number ? ((Number) count).longValue() : 0;
	}

	/*
	 * The method set current test name to result
	 * 
	 * @param result
	 */
	@AfterMethod(alwaysRun = true)
	public void setResultTestName(ITestResult result) {
		result.setAttribute("TestCaseName", testCaseName);
	}
}