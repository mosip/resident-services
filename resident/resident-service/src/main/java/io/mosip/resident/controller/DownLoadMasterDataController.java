package io.mosip.resident.controller;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.DownLoadMasterDataService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.Utilitiy;
import io.mosip.resident.validator.RequestValidator;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * 
 * @author M1063027 Rama Devi
 *
 */
@RestController
@Tag(name = "downLoad-masterdata-controller", description = "downLoad Masterdata Controller")
public class DownLoadMasterDataController {

	private static String DOWNLOADABLE_REGCEN_FILENAME = "";
	private static String DOWNLOADABLE_SUPPORTING_FILENAME = "";

	@Autowired
	private DownLoadMasterDataService downLoadMasterDataService;

	@Autowired
	private AuditUtil auditUtil;

	@Autowired
	private RequestValidator validator;

	@Autowired
	private AuditUtil audit;

	@Autowired
	private Utilitiy utilitiy;

	@Autowired
	private Environment environment;

	private static final Logger logger = LoggerConfiguration.logConfig(ProxyMasterdataController.class);

	/**
	 * download registration centers based on language code and selected names  of
	 * registration centers
	 * 
	 * @param langCode
	 * @param hierarchyLevel
	 * @param name
	 * @return
	 * @throws ResidentServiceCheckedException
	 */
	@GetMapping("/download/registration-centers-list")
	public ResponseEntity<Object> downloadRegistrationCentersByHierarchyLevel(@RequestParam(name="langcode") String langCode,
			@RequestParam(name="hierarchylevel") Short hierarchyLevel, @RequestParam("name") List<String> name)
			throws ResidentServiceCheckedException, IOException, Exception {
		logger.debug("DownLoadMasterDataController::getRegistrationCentersByHierarchyLevel()::entry");
		DOWNLOADABLE_REGCEN_FILENAME = "regcenter-";
		DOWNLOADABLE_REGCEN_FILENAME = DOWNLOADABLE_REGCEN_FILENAME + getCurrentDateAndTime();
		auditUtil.setAuditRequestDto(EventEnum.DOWNLOAD_REGISTRATION_CENTER);
		validator.validateOnlyLanguageCode(langCode);
		InputStream pdfInputStream = downLoadMasterDataService.downloadRegistrationCentersByHierarchyLevel(langCode,hierarchyLevel, name);
		InputStreamResource resource = new InputStreamResource(pdfInputStream);
		audit.setAuditRequestDto(EventEnum.DOWNLOAD_REGISTRATION_CENTER_SUCCESS);
		logger.debug("downLoad file name::" + DOWNLOADABLE_REGCEN_FILENAME);
		logger.debug("AcknowledgementController::acknowledgement()::exit");
		return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/pdf"))
				.header("Content-Disposition", "attachment; filename=\"" + utilitiy.getFileName(null,
						Objects.requireNonNull(this.environment.getProperty(
								ResidentConstants.DOWNLOAD_REGISTRATION_CENTRE_FILE_NAME_CONVENTION_PROPERTY))) + ".pdf\"")
				.body(resource);
	}
	
	@GetMapping("/download/nearestRegistrationcenters")
	public ResponseEntity<Object> downloadNearestRegistrationcenters(@RequestParam(name="langcode") String langCode,
			@RequestParam(name="longitude") double longitude, @RequestParam(name="latitude") double latitude,
			@RequestParam(name="proximitydistance") int proximityDistance)
			throws ResidentServiceCheckedException, IOException, Exception {
		logger.debug("DownLoadMasterDataController::getRegistrationCentersByHierarchyLevel()::entry");
		DOWNLOADABLE_REGCEN_FILENAME = "regcenter-";
		DOWNLOADABLE_REGCEN_FILENAME = DOWNLOADABLE_REGCEN_FILENAME + getCurrentDateAndTime();
		auditUtil.setAuditRequestDto(EventEnum.DOWNLOAD_REGISTRATION_CENTER);
		validator.validateOnlyLanguageCode(langCode);
		InputStream pdfInputStream = downLoadMasterDataService.getNearestRegistrationcenters(langCode,longitude, latitude, proximityDistance);
		InputStreamResource resource = new InputStreamResource(pdfInputStream);
		audit.setAuditRequestDto(EventEnum.DOWNLOAD_REGISTRATION_CENTER_SUCCESS);
		logger.debug("downLoad file name::" + DOWNLOADABLE_REGCEN_FILENAME);
		logger.debug("AcknowledgementController::acknowledgement()::exit");
		return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/pdf"))
				.header("Content-Disposition", "attachment; filename=\"" + utilitiy.getFileName(null,
						Objects.requireNonNull(this.environment.getProperty(
								ResidentConstants.DOWNLOAD_NEAREST_REGISTRATION_CENTRE_FILE_NAME_CONVENTION_PROPERTY))) + ".pdf\"")
				.body(resource);
	}
	
	@GetMapping(path = "/download/supporting-documents", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<Object> downloadSupportingDocsByLanguage(@RequestParam(name = "langcode") String langCode)
			throws ResidentServiceCheckedException, IOException, Exception {
		logger.debug("DownLoadMasterDataController::getSupportingDocsByLanguageCode()::entry");
		DOWNLOADABLE_SUPPORTING_FILENAME = "supportingDocs-";
		DOWNLOADABLE_SUPPORTING_FILENAME = DOWNLOADABLE_SUPPORTING_FILENAME + getCurrentDateAndTime();
		auditUtil.setAuditRequestDto(EventEnum.DOWNLOAD_SUPPORTING_DOCS);
		validator.validateOnlyLanguageCode(langCode);
		InputStream pdfInputStream = downLoadMasterDataService.downloadSupportingDocsByLanguage(langCode);
		InputStreamResource resource = new InputStreamResource(pdfInputStream);
		audit.setAuditRequestDto(EventEnum.DOWNLOAD_SUPPORTING_DOCS_SUCCESS);
		logger.debug("downLoad file name::" + DOWNLOADABLE_SUPPORTING_FILENAME);
		logger.debug("AcknowledgementController::acknowledgement()::exit");
		return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/pdf"))
				.header("Content-Disposition", "attachment; filename=\"" + utilitiy.getFileName(null,
						Objects.requireNonNull(this.environment.getProperty(
								ResidentConstants.DOWNLOAD_SUPPORTING_DOCUMENT_FILE_NAME_CONVENTION_PROPERTY))) + ".pdf\"")
				.body(resource);
	}
	/**
	 * this method return the current date and time
	 * 
	 * @return
	 */
	private String getCurrentDateAndTime() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy-HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}
}
