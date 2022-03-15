package io.mosip.resident.handler.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.dto.packet.PacketDto;
import io.mosip.commons.packet.exception.PacketCreatorException;
import io.mosip.commons.packet.facade.PacketWriter;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectIOException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectValidationFailedException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.FileUtils;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.exception.JsonProcessingException;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.CardType;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.MappingJsonConstants;
import io.mosip.resident.constant.PacketMetaInfoConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.FieldValue;
import io.mosip.resident.dto.PacketGeneratorResDto;
import io.mosip.resident.dto.RegProcRePrintRequestDto;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.VidRequestDto1;
import io.mosip.resident.dto.VidResponseDTO1;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.VidCreationException;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.IdSchemaUtil;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.validator.RequestHandlerRequestValidator;

@Service
public class UinCardRePrintService {
    /** The env. */
    @Autowired
    private Environment env;

    @Value("${IDSchema.Version}")
    private String idschemaVersion;

    @Autowired
    private IdSchemaUtil idSchemaUtil;

    @Autowired
    private ObjectMapper mapper;

    /** The rest client service. */
    @Autowired
    private ResidentServiceRestClient restClientService;

    /** The sync upload encryption service. */
    @Autowired
    SyncAndUploadService syncUploadEncryptionService;

    /** The validator. */
    @Autowired
    private RequestHandlerRequestValidator validator;

    @Autowired
    private PacketWriter packetWriter;

    /** The utilities. */
    @Autowired
    Utilities utilities;
    
    @Autowired
    AuditUtil audit;

    /** The vid type. */
    @Value("${id.repo.vidType}")
    private String vidType;

    /** The Constant VID_CREATE_ID. */
    public static final String VID_CREATE_ID = "vid.create.id";

    /** The Constant REG_PROC_APPLICATION_VERSION. */
    public static final String REG_PROC_APPLICATION_VERSION = "resident.vid.version";

    /** The Constant DATETIME_PATTERN. */
    public static final String DATETIME_PATTERN = "resident.datetime.pattern";

    /** The Constant UIN. */
    public static final String UIN = "UIN";

    /** The Constant VID. */
    public static final String VID = "VID";

    /** The reg proc logger. */
    private final Logger logger = LoggerConfiguration.logConfig(UinCardRePrintService.class);

    public static final String VID_TYPE = "id.repo.vidType";

    /**
     * Creates the packet.
     *
     * @param requestDto the uin card re print request dto
     * @return the packet generator res dto
     * @throws BaseCheckedException the reg base checked exception
     * @throws IOException             Signals that an I/O exception has occurred.
     */
    public PacketGeneratorResDto createPacket(RegProcRePrintRequestDto requestDto)
            throws BaseCheckedException, IOException {
        String uin = null;
        String vid = null;
        byte[] packetZipBytes = null;
        PacketGeneratorResDto packetGeneratorResDto = new PacketGeneratorResDto();
        File file = null;
        try {
            if (validator.isValidCenter(requestDto.getCenterId())
                    && validator.isValidMachine(requestDto.getMachineId())
                    && validator
                    .isValidRePrintRegistrationType(requestDto.getRegistrationType())
                    && validator.isValidIdType(requestDto.getIdType())
                    && validator.isValidCardType(requestDto.getCardType())
                    && isValidUinVID(requestDto)) {
                String cardType = requestDto.getCardType();
                String regType = requestDto.getRegistrationType();

                if (requestDto.getIdType().equalsIgnoreCase(UIN))
                    uin = requestDto.getId();
                else
                    vid = requestDto.getId();

                if (cardType.equalsIgnoreCase(CardType.MASKED_UIN.toString()) && vid == null) {

                    VidRequestDto1 vidRequestDto = new VidRequestDto1();
                    RequestWrapper<VidRequestDto1> request = new RequestWrapper<>();
                    VidResponseDTO1 response;
                    vidRequestDto.setUIN(uin);
                    vidRequestDto.setVidType(env.getProperty(VID_TYPE));
                    request.setId(env.getProperty(VID_CREATE_ID));
                    request.setRequest(vidRequestDto);
                    request.setRequesttime(DateUtils.formatToISOString(LocalDateTime.now()));
                    request.setVersion(env.getProperty(REG_PROC_APPLICATION_VERSION));

                    logger.debug(LoggerFileConstant.SESSIONID.toString(),
                            LoggerFileConstant.REGISTRATIONID.toString(), "",
                            "UinCardRePrintService::createPacket():: post CREATEVID service call started with request data : "
                                    + JsonUtil.objectMapperObjectToJson(vidRequestDto));

                    response = restClientService.postApi(env.getProperty(ApiName.CREATEVID.name()), MediaType.APPLICATION_JSON, request,
                            VidResponseDTO1.class);

                    logger.debug(LoggerFileConstant.SESSIONID.toString(),
                            LoggerFileConstant.REGISTRATIONID.toString(), "",
                            "UinCardRePrintService::createPacket():: post CREATEVID service call ended successfully");

                    if (!CollectionUtils.isEmpty(response.getErrors())) {
                    	throw new VidCreationException(ResidentErrorCode.VID_CREATION_EXCEPTION.getErrorMessage());

                    } else {
                        vid = response.getResponse().getVid();
                    }

                }
                if (uin == null) {
                    uin = utilities.getUinByVid(vid);
                }

                // set packet info
                PacketDto packetDto = new PacketDto();
                packetDto.setId(generateRegistrationId(requestDto.getCenterId(), requestDto.getMachineId()));
                packetDto.setSource(utilities.getDefaultSource());
                packetDto.setProcess(requestDto.getRegistrationType());
                packetDto.setSchemaVersion(idschemaVersion);
                packetDto.setSchemaJson(idSchemaUtil.getIdSchema(Double.valueOf(idschemaVersion)));
                packetDto.setFields(getDemographicDTO(uin));
                packetDto.setMetaInfo(getRegistrationMetaData(uin, requestDto.getRegistrationType(),
                        requestDto.getCenterId(), requestDto.getMachineId(), vid, requestDto.getCardType()));
                packetDto.setAudits(utilities.generateAudit(packetDto.getId()));
				packetDto.setOfflineMode(false);
                packetDto.setRefId(requestDto.getCenterId() + "_" + requestDto.getMachineId());
				List<PacketInfo> packetInfos = packetWriter.createPacket(packetDto);

                if (CollectionUtils.isEmpty(packetInfos) || packetInfos.iterator().next().getId() == null) {
                	audit.setAuditRequestDto(EventEnum.PACKET_CREATED_EXCEPTION);
                    throw new PacketCreatorException(ResidentErrorCode.PACKET_CREATION_EXCEPTION.getErrorCode(), ResidentErrorCode.PACKET_CREATION_EXCEPTION.getErrorMessage());
            }
                file = new File(env.getProperty("object.store.base.location")
                        + File.separator + env.getProperty("packet.manager.account.name")
                        + File.separator + packetInfos.iterator().next().getId() + ".zip");

                FileInputStream fis = new FileInputStream(file);

                packetZipBytes = IOUtils.toByteArray(fis);
				String creationTime = DateUtils.formatToISOString(LocalDateTime.now());

                packetGeneratorResDto = syncUploadEncryptionService.uploadUinPacket(
                        packetDto.getId(), creationTime, regType, packetZipBytes);

            }
            return packetGeneratorResDto;
        } catch (ApisResourceAccessException e) {
            logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
                    "", ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage()
                            + ExceptionUtils.getStackTrace(e));
            throw new BaseCheckedException(ResidentErrorCode.JSON_PROCESSING_EXCEPTION.getErrorCode(), ResidentErrorCode.JSON_PROCESSING_EXCEPTION.getErrorCode(), e);
        } catch (VidCreationException e) {
            logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
                    "", ResidentErrorCode.VID_CREATION_EXCEPTION.getErrorMessage()
                            + ExceptionUtils.getStackTrace(e));
            throw new BaseCheckedException(ResidentErrorCode.VID_CREATION_EXCEPTION.getErrorCode(), ResidentErrorCode.VID_CREATION_EXCEPTION.getErrorMessage(), e);
        } catch (IdObjectValidationFailedException | IdObjectIOException | JSONException e) {
            logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
                    "",
                    ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorMessage() + ExceptionUtils.getStackTrace(e));
            throw new BaseCheckedException(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(), ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorMessage(), e);
        } finally {
            if (file != null && file.exists())
                FileUtils.forceDelete(file);
        }
    }

    private Map<String, String> getRegistrationMetaData(String uin, String registrationType, String centerId,
                                                        String machineId, String vid, String cardType) throws JsonProcessingException {

        Map<String, String> metadata = new HashMap<>();

        FieldValue[] fieldValues = new FieldValue[6];

        FieldValue fieldValue0 = new FieldValue();
        FieldValue fieldValue1 = new FieldValue();
        FieldValue fieldValue2 = new FieldValue();
        FieldValue fieldValue3 = new FieldValue();
        FieldValue fieldValue4 = new FieldValue();
        FieldValue fieldValue5 = new FieldValue();
        fieldValue0.setLabel(PacketMetaInfoConstants.CENTERID);
        fieldValue0.setValue(centerId);
        fieldValues[0] = fieldValue0;

        fieldValue1.setLabel(PacketMetaInfoConstants.MACHINEID);
        fieldValue1.setValue(machineId);
        fieldValues[1] = fieldValue1;

        fieldValue2.setLabel(PacketMetaInfoConstants.REGISTRATION_TYPE);
        fieldValue2.setValue(registrationType);
        fieldValues[2] = fieldValue2;

        fieldValue3.setLabel(PacketMetaInfoConstants.UIN);
        fieldValue3.setValue(uin);
        fieldValues[3] = fieldValue3;

        fieldValue4.setLabel(PacketMetaInfoConstants.VID);
        fieldValue4.setValue(vid);
        fieldValues[4] = fieldValue4;

        fieldValue5.setLabel(PacketMetaInfoConstants.CARD_TYPE);
        fieldValue5.setValue(cardType);
        fieldValues[5] = fieldValue5;

        metadata.put("metaData", JsonUtils.javaObjectToJsonString(fieldValues));
        return metadata;
    }

    /**
     * Gets the demographic DTO.
     *
     * @param uin the uin
     * @return the demographic DTO
     */
    private Map<String, String> getDemographicDTO(String uin) throws IOException {
        Map<String, String> jsonMap = new HashMap<>();

        JSONObject regProcessorIdentityJson = utilities.getRegistrationProcessorMappingJson();
        String schemaVersion = JsonUtil.getJSONValue(
                JsonUtil.getJSONObject(regProcessorIdentityJson, MappingJsonConstants.IDSCHEMA_VERSION),
                MappingJsonConstants.VALUE);

        String uinLabel = JsonUtil.getJSONValue(
                JsonUtil.getJSONObject(regProcessorIdentityJson, MappingJsonConstants.UIN),
                MappingJsonConstants.VALUE);

        jsonMap.put(schemaVersion, idschemaVersion);
        jsonMap.put(uinLabel, uin);
        return jsonMap;
    }

    /**
     * Generate registration id.
     *
     * @param centerId  the center id
     * @param machineId the machine id
     * @return the string
     * @throws BaseCheckedException the reg base checked exception
     */
    private String generateRegistrationId(String centerId, String machineId) throws BaseCheckedException {

        List<String> pathsegments = new ArrayList<>();
        pathsegments.add(centerId);
        pathsegments.add(machineId);
        String rid = null;
        ResponseWrapper<?> responseWrapper;
        JSONObject ridJson;
        try {

            logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
                    "", "UinCardRePrintServiceImpl::generateRegistrationId():: RIDgeneration Api call started");
            responseWrapper = (ResponseWrapper<?>) restClientService.getApi(ApiName.RIDGENERATION, pathsegments, "", "",
                    ResponseWrapper.class);
            if (CollectionUtils.isEmpty(responseWrapper.getErrors())) {
                ridJson = mapper.readValue(mapper.writeValueAsString(responseWrapper.getResponse()), JSONObject.class);
                logger.debug(LoggerFileConstant.SESSIONID.toString(),
                        LoggerFileConstant.REGISTRATIONID.toString(), "",
                        "\"UinCardRePrintServiceImpl::generateRegistrationId():: RIDgeneration Api call  ended with response data : "
                                + JsonUtil.objectMapperObjectToJson(ridJson));
                rid = (String) ridJson.get("rid");

            } else {
                List<ServiceError> error = responseWrapper.getErrors();
                logger.debug(LoggerFileConstant.SESSIONID.toString(),
                        LoggerFileConstant.REGISTRATIONID.toString(), "",
                        "\"UinCardRePrintServiceImpl::generateRegistrationId():: RIDgeneration Api call  ended with response data : "
                                + error.get(0).getMessage());
                throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(),
                        error.get(0).getMessage(), new Throwable());
            }

        } catch (ApisResourceAccessException e) {
            if (e.getCause() instanceof HttpClientErrorException) {
                throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(), e.getMessage(), e);
            }
        } catch (IOException e) {
            throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(), e.getMessage(), e);
        }
        return rid;
    }

    /**
     * Checks if is valid uin VID.
     *
     * @param requestDto the uin card re print request dto
     * @return true, if is valid uin VID
     * @throws BaseCheckedException the reg base checked exception
     */
    public boolean isValidUinVID(RegProcRePrintRequestDto requestDto) throws BaseCheckedException, IOException {
        boolean isValid = false;
        if (requestDto.getIdType().equalsIgnoreCase(UIN)) {
            isValid = validator.isValidUin(requestDto.getId());
        } else if (requestDto.getIdType().equalsIgnoreCase(VID)) {
            isValid = validator.isValidVid(requestDto.getId());
        }
        return isValid;
    }

}
