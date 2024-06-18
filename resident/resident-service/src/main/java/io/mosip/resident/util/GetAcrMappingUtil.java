package io.mosip.resident.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
@author Kamesh Shekhar Prasad
 **/

@Component
public class GetAcrMappingUtil {


    @Autowired
    private ObjectMapper objMapper;

    @Autowired
    @Qualifier("selfTokenRestTemplate")
    private RestTemplate residentRestTemplate;

    @Value("${config.server.file.storage.uri}")
    private String configServerFileStorageURL;

    /** The acr-amr mapping json file. */
    @Value("${amr-acr.json.filename}")
    private String amrAcrJsonFile;

    private static final String ACR_AMR = "acr_amr";

    @Cacheable(value = "amr-acr-mapping")
    public Map<String, String> getAmrAcrMapping() throws ResidentServiceCheckedException {
        String amrAcrJson = residentRestTemplate.getForObject(configServerFileStorageURL + amrAcrJsonFile,
                String.class);
        Map<String, Object> amrAcrMap = Map.of();
        try {
            if (amrAcrJson != null) {
                amrAcrMap = objMapper.readValue(amrAcrJson.getBytes(UTF_8), Map.class);
            }
        } catch (IOException e) {
            throw new ResidentServiceCheckedException(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(),
                    ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorMessage(), e);
        }
        Object obj = amrAcrMap.get(ACR_AMR);
        Map<String, Object> map = (Map<String, Object>) obj;
        Map<String, String> acrAmrMap = map.entrySet().stream().collect(
                Collectors.toMap(entry -> entry.getKey(), entry -> (String) ((ArrayList) entry.getValue()).get(0)));
        return acrAmrMap;
    }

}
