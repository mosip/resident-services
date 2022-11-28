package io.mosip.resident.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;

import io.mosip.resident.util.Utilitiy;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.pdfgenerator.spi.PDFGenerator;
import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.kernel.core.templatemanager.spi.TemplateManagerBuilder;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.RegistrationCenterDto;
import io.mosip.resident.dto.RegistrationCenterResponseDto;
import io.mosip.resident.dto.WorkingDaysDto;
import io.mosip.resident.dto.WorkingDaysResponseDto;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.DownLoadMasterDataService;
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.util.Utilitiy;

/**
 * 
 * @author M1063027 Rama devi
 *
 */
@Component
public class DownLoadMasterDataServiceImpl implements DownLoadMasterDataService {

	private static final String CLASSPATH = "classpath";
	private static final String ENCODE_TYPE = "UTF-8";
	private static final String REGISTRATION_CENTER_TEMPLATE_NAME = "registration-centers-list";
	private static final String SUPPORTING_DOCS_TEMPLATE_NAME = "supporting-docs-list";
	public static final String FILE_TEXT = "fileText";
	
	@Autowired
	Environment env;

	@Autowired
	private ProxyMasterdataService proxyMasterdataService;

	private TemplateManager templateManager;

	@Autowired
	private TemplateManagerBuilder templateManagerBuilder;

	@Autowired
	private PDFGenerator pdfGenerator;

	/** The mapper. */
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
    	private Utilitiy utilitiy;

	private static final Logger logger = LoggerConfiguration.logConfig(ProxyMasterdataServiceImpl.class);

	@PostConstruct
	public void idTemplateManagerPostConstruct() {
		templateManager = templateManagerBuilder.encodingType(ENCODE_TYPE).enableCache(false).resourceLoader(CLASSPATH)
				.build();
	}

	/**
	 * download registration centers based on language code, hierarchyLevel and
	 * center names
	 */
	public InputStream downloadRegistrationCentersByHierarchyLevel(String langCode, Short hierarchyLevel,
			List<String> name) throws ResidentServiceCheckedException, IOException, Exception {
		logger.debug("DownLoadMasterDataService::downloadRegistrationCentersByHierarchyLevel()::entry");
		ResponseWrapper<?> proxyResponseWrapper = proxyMasterdataService
				.getAllTemplateBylangCodeAndTemplateTypeCode(langCode, REGISTRATION_CENTER_TEMPLATE_NAME);
		ResponseWrapper<?> regCentResponseWrapper = proxyMasterdataService.getRegistrationCentersByHierarchyLevel(langCode, hierarchyLevel, name);
		Map<String, Object> regCentersMap = new LinkedHashMap<>();
		List<RegistrationCenterDto> regCentersDtlsList = Collections.emptyList();
		if (regCentResponseWrapper != null) {
			RegistrationCenterResponseDto registrationCentersDtls = mapper.readValue(
					mapper.writeValueAsString(regCentResponseWrapper.getResponse()),
					RegistrationCenterResponseDto.class);
			List<RegistrationCenterDto> regCenterIntialList = registrationCentersDtls.getRegistrationCenters();
			if (regCenterIntialList != null && !regCenterIntialList.isEmpty()) {
				IntStream.range(0, regCenterIntialList.size()).forEach(i -> {
					try {
						addRegistrationCenterDtls(i, regCenterIntialList.get(i));
					} catch (Exception e) {
						throw new ResidentServiceException(ResidentErrorCode.UNABLE_TO_PROCESS, e);
					}
				});
			}
			regCentersMap.put("regCenterIntialList", regCenterIntialList);
		}
		logger.debug("template data from DB:" + proxyResponseWrapper.getResponse());
		Map<String, Object> templateResponse = new LinkedHashMap<>(
				(Map<String, Object>) proxyResponseWrapper.getResponse());
		String fileText = (String) templateResponse.get(FILE_TEXT);
		InputStream downLoadRegCenterTemplate = new ByteArrayInputStream(fileText.getBytes(StandardCharsets.UTF_8));
		InputStream downLoadRegCenterTemplateData = templateManager.merge(downLoadRegCenterTemplate, regCentersMap);
    
    StringWriter writer = new StringWriter();
		IOUtils.copy(downLoadRegCenterTemplateData, writer, "UTF-8");
		return new ByteArrayInputStream(utilitiy.signPdf(new ByteArrayInputStream(writer.toString().getBytes()), null));
	}
	
	/**
	 * download nearest registration centers
	 */
	public InputStream getNearestRegistrationcenters(String langCode, double longitude, double latitude,
			int proximityDistance) throws ResidentServiceCheckedException, IOException, Exception {
		logger.debug("DownLoadMasterDataService::downloadRegistrationCentersByHierarchyLevel()::entry");
		ResponseWrapper<?> proxyResponseWrapper = proxyMasterdataService
				.getAllTemplateBylangCodeAndTemplateTypeCode(langCode, REGISTRATION_CENTER_TEMPLATE_NAME);
		ResponseWrapper<?> regCentResponseWrapper =  proxyMasterdataService.getCoordinateSpecificRegistrationCenters(langCode,
				longitude, latitude, proximityDistance);
		Map<String, Object> regCentersMap = new LinkedHashMap<>();
		List<RegistrationCenterDto> regCentersDtlsList = Collections.emptyList();
		if (regCentResponseWrapper != null) {
			RegistrationCenterResponseDto registrationCentersDtls = mapper.readValue(
					mapper.writeValueAsString(regCentResponseWrapper.getResponse()),
					RegistrationCenterResponseDto.class);
			List<RegistrationCenterDto> regCenterIntialList = registrationCentersDtls.getRegistrationCenters();
			if (regCenterIntialList != null && !regCenterIntialList.isEmpty()) {
				IntStream.range(0, regCenterIntialList.size()).forEach(i -> {
					try {
						addRegistrationCenterDtls(i, regCenterIntialList.get(i));
					} catch (Exception e) {
						throw new ResidentServiceException(ResidentErrorCode.UNABLE_TO_PROCESS, e);
					}
				});
			}
			regCentersMap.put("regCenterIntialList", regCenterIntialList);
		}
		logger.debug("template data from DB:" + proxyResponseWrapper.getResponse());
		Map<String, Object> templateResponse = new LinkedHashMap<>(
				(Map<String, Object>) proxyResponseWrapper.getResponse());
		String fileText = (String) templateResponse.get(FILE_TEXT);
		InputStream downLoadRegCenterTemplate = new ByteArrayInputStream(fileText.getBytes(StandardCharsets.UTF_8));
		InputStream downLoadRegCenterTemplateData = templateManager.merge(downLoadRegCenterTemplate, regCentersMap);

		StringWriter writer = new StringWriter();
		IOUtils.copy(downLoadRegCenterTemplateData, writer, "UTF-8");
		return new ByteArrayInputStream(utilitiy.signPdf(new ByteArrayInputStream(writer.toString().getBytes()), null));
	}

	public InputStream convertByteArrayToInputStream(byte[] bytes){
		ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
		baos.write(bytes, 0, bytes.length);
		InputStream inputStream = convertOutputStreamToInputStream(baos);
		return inputStream;
	}
	
	
	/**
	 * download registration centers based on language code, hierarchyLevel and
	 * center names
	 */
	public InputStream downloadSupportingDocsByLanguage(String langCode) throws ResidentServiceCheckedException, IOException, Exception {
		logger.debug("ResidentServiceImpl::getResidentServicePDF()::entry");
		ResponseWrapper<?> proxyResponseWrapper = proxyMasterdataService
				.getAllTemplateBylangCodeAndTemplateTypeCode(langCode, SUPPORTING_DOCS_TEMPLATE_NAME);
		logger.debug("template data from DB:" + proxyResponseWrapper.getResponse());
		Map<String, Object> templateResponse = new LinkedHashMap<>((Map<String, Object>) proxyResponseWrapper.getResponse());
		String fileText = (String) templateResponse.get(FILE_TEXT);		
		Map<String, Object> supportingsDocsMap = new HashMap<>();
		supportingsDocsMap.put("supportingsDocMap", supportingsDocsMap);
		InputStream supportingDocsTemplate = new ByteArrayInputStream(fileText.getBytes(StandardCharsets.UTF_8));
		InputStream supportingDocsTemplateData = templateManager.merge(supportingDocsTemplate, supportingsDocsMap);	
    
		StringWriter writer = new StringWriter();
		IOUtils.copy(supportingDocsTemplateData, writer, "UTF-8");
		return new ByteArrayInputStream(utilitiy.signPdf(new ByteArrayInputStream(writer.toString().getBytes()), null));
	}
  
	/**
	 * update the registration center details
	 */
	private void addRegistrationCenterDtls(int index, RegistrationCenterDto regCenterDto)
			throws ResidentServiceCheckedException, Exception {
		String workingHours = "";
		String fullAddress = getFullAddress(regCenterDto.getAddressLine1(), regCenterDto.getAddressLine2(),
				regCenterDto.getAddressLine3());
		regCenterDto.setSerialNumber(index + 1);
		regCenterDto.setFullAddress(fullAddress);
		List<WorkingDaysDto> workingDaysList = getRegCenterWorkingDays(regCenterDto.getId(),
				regCenterDto.getLangCode());
		workingHours = workingDaysList.get(0).getName() + "-" + workingDaysList.get(1).getName() + "|"
				+ getTime(regCenterDto.getCenterStartTime()) + "-" + getTime(regCenterDto.getCenterEndTime());
		regCenterDto.setWorkingHours(workingHours);
	}

	/**
	 * convert output stream to input stream
	 * 
	 * @param orgByteOutStream
	 * @return
	 */
	private static InputStream convertOutputStreamToInputStream(ByteArrayOutputStream orgByteOutStream) {
		PipedInputStream in = new PipedInputStream();
		new Thread(new Runnable() {
			public void run() {
				try (final PipedOutputStream out = new PipedOutputStream(in)) {
					orgByteOutStream.writeTo(out);
				} catch (IOException e) {
					logger.error("convert Output stream to input stream" + e.getMessage());
				}
			}
		}).start();
		return in;
	}

	/**
	 * return the full address
	 * 
	 * @param address1
	 * @param address2
	 * @param address3
	 * @return
	 */
	private String getFullAddress(String address1, String address2, String address3) {
		StringBuilder fullAddress = new StringBuilder();
		fullAddress.append(address1 + "," + address2 + "," + address3);
		return fullAddress.toString();
	}

	/**
	 * return the starting and ending working day details
	 * 
	 * @param regCenterId
	 * @param langCode
	 * @return
	 * @throws ResidentServiceCheckedException
	 * @throws Exception
	 */
	private List<WorkingDaysDto> getRegCenterWorkingDays(String regCenterId, String langCode)
			throws ResidentServiceCheckedException, Exception {
		ResponseWrapper<?> responseWrapper;
		responseWrapper = proxyMasterdataService.getRegistrationCenterWorkingDays(regCenterId, langCode);
		WorkingDaysResponseDto workingDaysResponeDtls = mapper
				.readValue(mapper.writeValueAsString(responseWrapper.getResponse()), WorkingDaysResponseDto.class);
		List<WorkingDaysDto> workingDaysList = workingDaysResponeDtls.getWorkingdays();

		WorkingDaysDto startDay = workingDaysList.stream().min(Comparator.comparing(WorkingDaysDto::getOrder))
				.orElseThrow(NoSuchElementException::new);

		WorkingDaysDto endDay = workingDaysList.stream().max(Comparator.comparing(WorkingDaysDto::getOrder))
				.orElseThrow(NoSuchElementException::new);

		List<WorkingDaysDto> workingDaysHoursList = new ArrayList<>();
		workingDaysHoursList.add(startDay);
		workingDaysHoursList.add(endDay);
		return workingDaysHoursList;
	}

	/**
	 * get AM and PM details for given time
	 * 
	 * @param time
	 * @return
	 */
	private String getTime(String time) {
		SimpleDateFormat sdf1 = new SimpleDateFormat("hh:mm:ss");
		SimpleDateFormat sdf2 = new SimpleDateFormat("hh.mm aa");
		Date date = new Date();
		try {
			date = sdf1.parse(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return sdf2.format(date);
	}

}
