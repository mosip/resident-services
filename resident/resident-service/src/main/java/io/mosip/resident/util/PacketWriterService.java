package io.mosip.resident.util;/*
package io.mosip.resident.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.commons.packet.dto.packet.PacketDto;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.exception.JsonProcessingException;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.dto.PacketInfo;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.PacketManagerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PacketWriterService {

    private final Logger logger = LoggerConfiguration.logConfig(PacketWriterService.class);
    private static final String ID = "mosip.commmons.packetmanager";
    private static final String VERSION = "v1";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Environment env;

    @Autowired
    private TokenGenerator tokenGenerator;

    @Autowired
    private ResidentServiceRestClient restApi;

    public List<PacketInfo> createPacket(PacketDto packetDto) throws ApisResourceAccessException, PacketManagerException, JsonProcessingException, IOException {
        List<PacketInfo> packetInfos = new ArrayList<>();
        RequestWrapper<PacketDto> request = new RequestWrapper<>();
        request.setId(ID);
        request.setVersion(VERSION);
        request.setRequesttime(DateUtils.getUTCCurrentDateTime());
        request.setRequest(packetDto);

        ResponseWrapper<List<PacketInfo>> response = (ResponseWrapper) restApi.putApi(
                env.getProperty(ApiName.PACKETMANAGER_CREATE.name()), request, ResponseWrapper.class, MediaType.APPLICATION_JSON, tokenGenerator.getToken());

        if (response.getErrors() != null && response.getErrors().size() > 0) {
            logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), packetDto.getId(), JsonUtils.javaObjectToJsonString(response));
            throw new PacketManagerException(response.getErrors().get(0).getErrorCode(), response.getErrors().get(0).getMessage());
        }

        for (Object o : response.getResponse()) {
            PacketInfo fieldResponseDto = objectMapper.readValue(JsonUtils.javaObjectToJsonString(o), PacketInfo.class);
            packetInfos.add(fieldResponseDto);
        }

        return packetInfos;
    }

}
*/
