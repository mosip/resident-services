package io.mosip.resident.handler.service.impl;

import io.mosip.commons.packet.dto.packet.PacketDto;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.RegistrationConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.PacketInfo;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.handler.service.PacketCreationService;
import io.mosip.resident.util.EncryptorUtil;
import io.mosip.resident.util.IdSchemaUtil;
import io.mosip.resident.util.PacketWriterService;
import io.mosip.resident.util.ServerUtil;
import io.mosip.resident.util.TokenGenerator;
import io.mosip.resident.util.Utilities;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Class for creating the Resident Registration
 * 
 * @author Sowmya
 * @since 1.0.0
 *
 */
@Service
public class PacketCreationServiceImpl implements PacketCreationService {

	private static final String loggerMessage = "Byte array of %s file generated successfully";

	@Autowired
	private Environment environment;

	@Value("${IDSchema.Version}")
	private String idschemaVersion;

	@Autowired
	private IdSchemaUtil idSchemaUtil;

	@Autowired
	private TokenGenerator tokenGenerator;

	@Autowired
	private PacketWriterService packetWriterService;

	@Autowired
	private Utilities utilities;

	private String creationTime = null;

	private final Logger logger = LoggerConfiguration.logConfig(PacketCreationServiceImpl.class);

	private static Map<String, String> categoryPacketMapping = new HashMap<>();

	static {
		categoryPacketMapping.put("pvt", "id");
		categoryPacketMapping.put("kyc", "id");
		categoryPacketMapping.put("none", "id");
		categoryPacketMapping.put("evidence", "evidence");
		categoryPacketMapping.put("optional", "optional");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.service.PacketCreationService#create(io.mosip.
	 * registration.dto.RegistrationDTO)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public byte[] create(final PacketDto packetDto, String centerId, String machineId)
			throws BaseCheckedException, IOException {
		String rid = packetDto.getId();
		try {

			logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					rid, "PacketCreationServiceImpl ::create()::entry");

			// audit log creation
			packetDto.setAudits(utilities.generateAudit(rid));

			logger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					rid, String.format(loggerMessage, RegistrationConstants.AUDIT_JSON_FILE));

			logger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					rid, String.format(loggerMessage, RegistrationConstants.PACKET_META_JSON_NAME));

			String refId = centerId + "_" + machineId;
			float idschema = Float.valueOf(idschemaVersion);
			String idSchema = idSchemaUtil.getIdSchema(Double.valueOf(idschemaVersion));
			packetDto.setSchemaVersion(String.valueOf(idschema));
			packetDto.setSchemaJson(idSchema);

			List<PacketInfo> packetInfos = packetWriterService.createPacket(packetDto);
			/*byte[] packetZip = packetWriterService.createPacket(registrationDTO.getRegistrationId(), idschema,
					idSchema, categoryPacketMapping, encryptorUtil.getPublickey(refId).getPublicKey().getBytes(), null);
*/
			return null;
		} catch (RuntimeException | ApisResourceAccessException | JSONException runtimeException) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					rid, ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.getStackTrace(runtimeException));
			throw new BaseCheckedException(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(), ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorMessage(), runtimeException);
		}
	}
}
