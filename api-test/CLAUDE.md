# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Context

This is the **resident-services API test module** (`apitest-resident`) — one module in a larger multi-service MOSIP API automation framework. It tests external REST endpoints of the MOSIP Resident Services API using TestNG + REST Assured, driven entirely by YAML test definitions.

The shared test infrastructure lives in `apitest-commons` (a separate repo: `mosip-functional-tests/apitest-commons`). If `apitest-commons` has been changed, build it first before building this module.

## Build and Run

```powershell
# Build (from this directory)
mvn clean install -Dgpg.skip=true -Dmaven.gitcommitid.skip=true

# Run via JAR (primary method)
java -Dmodules=resident -Denv.user=api-internal.<envName> -Denv.endpoint=<baseUrl> -Denv.testLevel=smokeAndRegression -jar target/apitest-resident-1.2.1-SNAPSHOT-jar-with-dependencies.jar

# Run via IDE (Eclipse/VS Code): Main class is
# io.mosip.testrig.apirig.resident.testrunner.MosipTestRunner
# VM args: -Dmodules=resident -Denv.user=api-internal.<envName> -Denv.endpoint=<baseUrl> -Denv.testLevel=smokeAndRegression
```

`env.testLevel` options: `smoke` (positive cases only), `regression`, `smokeAndRegression`.

Reports are written to `api-test/testng-report/` after execution — two reports are generated (one for prerequisites, one for core tests).

## Architecture

### Execution Flow

`MosipTestRunner.main()` orchestrates the full lifecycle:
1. Extracts resources (JAR mode) or copies them (IDE mode)
2. Initializes config via `ResidentConfigManager.init()` — loads `resident.properties`
3. Starts a `Watchdog` (default 120 min timeout, configurable via `watchdogTimeoutMinutes`)
4. Runs Keycloak user setup, cert generation, biometric data generation
5. Invokes TestNG against `testNgXmlFiles/residentMasterTestSuite.xml`, which chains:
   - `residentPrerequisiteSuite.xml` — runs `AddIdentity` to create the test UIN/identity used by downstream tests
   - `residentSuite.xml` — runs all resident API test cases in order
6. DB cleanup and Keycloak user teardown on exit

### Test Definition Pattern

Tests are **YAML-driven**. Each `<test>` in the suite XML points a generic Java test class at a YAML file:

```xml
<test name="GenerateVID">
  <parameter name="ymlFile" value="resident/GenerateVID/createGenerateVID.yml" />
  <parameter name="idKeyName" value="vid" />          <!-- optional: captures response field -->
  <parameter name="sendEsignetToken" value="true" />  <!-- optional: send eSignet token -->
  <classes>
    <class name="io.mosip.testrig.apirig.resident.testscripts.PostWithAutogenIdWithOtpGenerate" />
  </classes>
</test>
```

Each YAML test case specifies `endPoint`, `restMethod`, `role`, `input` JSON, `output` JSON, `inputTemplate` (`.hbs`), and `outputTemplate` (`.hbs`). Dynamic values use `$KEYWORD$` placeholders (e.g., `$TIMESTAMP$`, `$TRANSACTIONID$`). Cross-test data references use `$ID:<TestCaseName>_<FIELD>$` (e.g., `$ID:AddIdentity_Positive_PRE_smoke_UIN$`).

YAML files live under `src/main/resources/resident/<FeatureName>/`.

### Generic Test Script Classes

Located in `src/main/java/.../resident/testscripts/`. Pick the right one when adding a new suite entry:

| Class | Use case |
|---|---|
| `SimplePost` | POST without OTP; handles `sendEsignetToken` |
| `SimplePatch` / `SimplePut` | PATCH/PUT without OTP |
| `SimplePatchForAutoGenId` | PATCH that captures an ID from response via `idKeyName` |
| `GetWithParam` | GET with path/query params |
| `PostWithBodyWithOtpGenerate` | POST that generates OTP inline |
| `PostWithAutogenIdWithOtpGenerate` | POST + OTP + captures an ID from response via `idKeyName` |
| `SimplePostForAutoGenId` | POST that captures IDs (e.g., `transactionId`, `code`) via `idKeyName` |
| `GetWithParamForAutoGenId` | GET that captures IDs from response |
| `GetWithParamForDownloadCard` | GET returning PDF binary |
| `GetWithQueryParamForDownloadCard` | GET with query params returning PDF binary |
| `PostWithBodyWithPdfDownload` | POST returning PDF binary |
| `PostWithParamAndFile` | POST with multipart file upload |
| `AuditValidator` | Validates audit log entries (uses `AuditLogValidation.yml`) |
| `DeleteWithParam` | DELETE with path params |
| `AddIdentity` | Prerequisite only — creates test identity in IDREPO |

### Key Configuration (`src/main/resources/config/resident.properties`)

This file (not committed with secrets — only a template is checked in) must be populated before running locally:
- `keycloak-external-url` — Keycloak IAM URL
- `db-server`, `db-port` — PostgreSQL host
- `audit_url`, `partner_url` — JDBC connection strings
- `keycloak_Password`, `audit_password`, `postgres-password` — credentials
- `mosip_resident_client_secret`, `AuthClientSecret`, and other `mosip_*_client_secret` values
- `eSignetbaseurl` — required for tests with `sendEsignetToken=true`
- `testCasesToExecute` — comma-separated `uniqueIdentifier` values to run a subset; empty runs all
- `watchdogTimeoutMinutes` — max execution time (default 120)

### Audit Log Validation Pattern

Many flows interleave `AuditLogValidation` tests before and after the main API call. These use `AuditValidator` and verify audit records written to the database. The `ResidentAuditCount` static counter in `ResidentUtil` tracks the baseline count.

### Suite Ordering Constraint

The last four tests in `residentSuite.xml` (`SendOtpForContactDet`, `UpdateContactDetails`, `SendOtpToUserId`, `ValidateWithUserIdOtp`) modify the UIN's contact details — **always keep these at the end** of the suite, as they change the OTP delivery address and would break earlier OTP-dependent tests.

### Adding a New API Test

1. Create `src/main/resources/resident/<FeatureName>/<FeatureName>.yml` with test cases
2. Create `.hbs` templates for request body and expected response in the same directory
3. Add a `<test>` entry in `testNgXmlFiles/residentSuite.xml` pointing to the appropriate generic test script class
4. If the test requires a prerequisite identity field, reference it via `$ID:<AddIdentityTestCaseName>_<FIELD>$`

## Patterns and Pitfalls

### HBS output template pattern
Result `.hbs` files define the **response structure** with `{{placeholder}}` variables. The YAML `output` field provides only the **values**. The framework merges them at runtime.

Example (`RidCheckStatus`):
- `createRidCheckStatusResult.hbs`: `{ "response": { "ridStatus": "{{ridStatus}}" } }`
- YAML `output`: `{ "ridStatus": "PROCESSED" }`

Always follow this pattern for new result HBS files — do **not** put the full `"response": { ... }` structure directly in the YAML output field, and do **not** leave result HBS files as `{}` when an assertion is needed.

### One YAML per suite entry scope
A `<test>` entry in `residentSuite.xml` runs **all test cases** in the pointed YAML file — the data provider loads the whole file. If the same YAML is referenced by two different suite entries (e.g., a "before operations" entry and a "after operations" entry), all test cases run in both positions.

**Rule**: when two suite entries need different subsets of test cases from the same feature area, create separate YAML files (e.g., `GetNotificationsCountBeforeOperations.yml` vs `GetNotificationsCount.yml`).

### AuditValidator shared counter
`AuditValidator` uses a single static `ResidentUtil.ResidentAuditCount` across the entire suite run. It works on a Before/After pair model: Before captures count N, After verifies count > N.

- Always use the **same YAML and same table** for a Before/After pair
- Reuse the existing `resident/AuditLogValidation/AuditLogValidation.yml` (`audit.app_audit_log`) for all audit pairs — do not create per-feature audit YAML files
- Do **not** use `AuditValidator` for tables outside `audit.app_audit_log` (e.g., `pms.partner`) — the count mismatch between tables breaks the shared counter

### DB record existence checks
`AuditValidator` is not suitable for verifying that a record exists in any arbitrary table (e.g., `pms.partner`). It only checks count increases. A new `DbRecordExistenceValidator` test script class would be needed for that pattern — not currently in the codebase.

### eSignet token ordering
Tests with `sendEsignetToken=true` generate notifications for the `residentNew`/`residentNewVid` identities. Any test that asserts a specific notification count (e.g., `unreadCount: 0`) must be placed **before** the first `sendEsignetToken=true` test in the suite, which is currently `GetRemainingupdatecountbyIndividualIdRequest2`.

### Token expiry tests
The framework auto-generates a **fresh eSignet token on every API call** when `sendEsignetToken=true`. Testing true token timeout (wait N minutes, retry, expect 401) requires an explicit `Thread.sleep` and is not feasible without adding that wait time to the suite. Use an invalid/empty `role` to test the 401 error path immediately instead.

### OTP expiry pattern (zero-sleep)
The `SendOtpForExpiration` → `ValidateExpiredOTP` pattern avoids a sleep by placing the OTP generation early in the suite and the expiry validation much later — enough other tests run in between to cover the OTP TTL naturally. This works because the framework captures and stores the OTP value for reuse. Token values cannot use this same pattern (no capture-and-reuse mechanism exists).

### Smoke vs regression test cases
Negative test cases (`_Neg` suffix in test case names) only run with `env.testLevel=regression` or `smokeAndRegression`. Smoke-only runs execute only `_Smoke` suffix test cases. Coverage analysis must account for the test level in use.

### Auto-generated ID capture (`_sid` convention)
Test script classes that end in `ForAutoGenId` (e.g., `GetWithParamForAutoGenId`, `SimplePatchForAutoGenId`) read `idKeyName` from the suite XML parameter and call `writeAutoGeneratedId` — but only when the **test case name contains `_sid`** (case-insensitive). The stored cache key is derived as:

```text
testCaseName strip up to first "_"  +  "_"  +  fieldName
```

Example: test case `Resident_UpdateUINNew_all_Valid_Smoke_sid` + `idKeyName=eventId`
→ stored key: `UpdateUINNew_all_Valid_Smoke_sid_eventId`
→ referenced in YAML as: `$ID:UpdateUINNew_all_Valid_Smoke_sid_eventId$`

**Convention**: never add `idKeyName` support to a plain class (e.g., `SimplePatch`). Always create a dedicated `ForAutoGenId` variant — consistent with `GetWithParam`/`GetWithParamForAutoGenId`.

### eSignet + auto-gen ID PATCH limitation
`AdminTestUtil.patchWithBodyAndCookieForAutoGeneratedId` does **not** support eSignet dual-token flow. For PATCH operations that need both `sendEsignetToken=true` AND ID capture, use `SimplePatchForAutoGenId` — it calls `patchWithBodyAndCookie(..., sendEsignetToken)` and then calls `writeAutoGeneratedId` manually.

### Output comparison keywords
Keywords available in the YAML `output` field:

| Keyword | Behaviour |
|---------|-----------|
| `$IGNORE$` | Always passes — skips comparison even if field is absent from actual response. Does **not** verify field existence. |
| `$TIMESTAMP$` / `$TIMESTAMPZ$` | Validates the value is a valid timestamp string. |
| `$REMOVE$` | Input fields only — removes the field from the request body. Not valid in `output`. |

There is **no `$NOTEMPTY$`** keyword. Dynamic fields (URLs, event IDs, timestamps) must use `$IGNORE$` or an exact value.

### `checkErrorsOnlyInResponse` behaviour
- `checkErrorsOnlyInResponse: true` + `output: '{}'` → test **passes** as long as the response has no `errors` field (no value comparison performed).
- Without this flag + `output: '{}'` → test is marked **SKIPPED** (no comparison done, not a pass).

### OTP fetch failure — two distinct root causes

When a test fails with `RES-SER-422 OTP is invalid`, the debug log tells you which of two problems occurred:

**1. Wrong OTP extracted (regex false match)**
Symptom: `Extracted OTP=XXXXXX` is logged but the 6 digits are the **tail of the masked ID** (e.g., `489079` from `XXXXXXXX26489079`), not the actual OTP.
Cause: The `OTP_PATTERN` third alternative `\bOTP\b.*?\b(\d{6})\b` scans lazily after the `OTP` keyword and can latch onto 6 digits at the end of a longer digit sequence if there is no word boundary before them.
Fix: The pattern in `NotificationListener.java` uses `\b(\d{6})\b` which requires the 6 digits to be a standalone number — already fixed. If this recurs after a commons change, verify the third alternative still has `\b` on both sides.

**2. OTP email not delivered (environment)**
Symptom: `[POLL LOOP N] No message yet for email=...@mosip.net` repeats until `[POLL TIMEOUT]`. No `[STORE]` log appears for that email at all.
Cause: The MOSIP notification service sent the SMS OTP but did not relay the email OTP to the mock SMTP WebSocket. This is an intermittent server-side delivery issue, not a code bug.
Distinguish from case 1: if a `[STORE]` for the email appears with "OTP not found in message", the message arrived but the regex didn't match. If no `[STORE]` appears for that email at all, the message was never delivered.

### OTP channel mismatch in YAML (`sendOtpReqTemplate` vs `otp` field)

For tests that use the `sendOtp` + `validateOtp` nested structure (e.g., `GetChannelVerificationStatus`), the channel used in `sendOtpReqTemplate` **must match** the suffix used in the `otp` lookup key:

| `sendOtpReqTemplate` used | Correct `otp` field value |
|---|---|
| `createSendOTPAsEmail` | `$ID:<testCase>_EMAIL$` (polls email queue) |
| `createSendOTPAsPhone` | `$ID:<testCase>_PHONE$@phone` (polls phone/SMS queue) |

If you use `createSendOTPAsPhone` but set `otp` to an email address, the framework polls `otpQueues` for an email OTP that was never sent — causing a full **180-second timeout** before proceeding with an empty OTP. The test may still pass (depending on `checkErrorsOnlyInResponse`) but wastes the entire OTP expiry window.

Example fix (TC_Resident_GetChannelVerificationStatus_09):
```yaml
# Wrong — waits 180s because OTP was sent only to PHONE
"otp": "$ID:AddIdentity_Positive_PDEA_smoke_EMAIL$"

# Correct — gets OTP from SMS immediately
"otp": "$ID:AddIdentity_Positive_PDEA_smoke_PHONE$@phone"
```

Use `checkErrorsOnlyInResponse: true` when the goal is just to confirm the API returns a valid response with no errors, without asserting specific field values.