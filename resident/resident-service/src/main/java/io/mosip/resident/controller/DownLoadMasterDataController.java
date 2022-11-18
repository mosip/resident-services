package io.mosip.resident.controller;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.DownLoadMasterDataService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.validator.RequestValidator;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * @author M1063027 Rama Devi
 *
 */
@RestController
@Tag(name = "downLoad-masterdata-controller", description = "downLoad Masterdata Controller")
public class DownLoadMasterDataController {

	private static String DOWNLOADABLE_REGCEN_FILENAME = "";

	@Autowired
	private DownLoadMasterDataService downLoadMasterDataService;

	@Autowired
	private AuditUtil auditUtil;

	@Autowired
	private RequestValidator validator;

	@Autowired
	private AuditUtil audit;

	private static final Logger logger = LoggerConfiguration.logConfig(ProxyMasterdataController.class);

	/**
	 * download registration centers based on language code and selected names of
	 * registration centers
	 * 
	 * @param langCode
	 * @param hierarchyLevel
	 * @param name
	 * @return
	 * @throws ResidentServiceCheckedException
	 */
	@GetMapping("download/proxy/masterdata/registrationcenters/{langcode}/{hierarchylevel}/names")
	public ResponseEntity<Object> downloadRegistrationCentersByHierarchyLevel(@PathVariable("langcode") String langCode,
			@PathVariable("hierarchylevel") Short hierarchyLevel, @RequestParam("name") List<String> name)
			throws ResidentServiceCheckedException, IOException, Exception {
		logger.debug("ProxyMasterdataController::getRegistrationCentersByHierarchyLevel()::entry");
		DOWNLOADABLE_REGCEN_FILENAME = "regcenter-";
		DOWNLOADABLE_REGCEN_FILENAME = DOWNLOADABLE_REGCEN_FILENAME + getCurrentDateAndTime();
		auditUtil.setAuditRequestDto(EventEnum.GET_REG_CENTERS_FOR_LOCATION_CODE);
		validator.validateOnlyLanguageCode(langCode);
		InputStream pdfInputStream = downLoadMasterDataService.downloadRegistrationCentersByHierarchyLevel(langCode,
				hierarchyLevel, name);
		InputStreamResource resource = new InputStreamResource(pdfInputStream);
		audit.setAuditRequestDto(EventEnum.DOWNLOAD_REGISTRATION_CENTER_SUCCESS);
		logger.debug("downLoad file name::" + DOWNLOADABLE_REGCEN_FILENAME);
		logger.debug("AcknowledgementController::acknowledgement()::exit");
		return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/pdf"))
				.header("Content-Disposition", "attachment; filename=\"" + DOWNLOADABLE_REGCEN_FILENAME + ".pdf\"")
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
