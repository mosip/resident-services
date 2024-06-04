package io.mosip.resident.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.*;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PerpetualVidUtility {

    @Autowired
    private Environment env;

    @Autowired
    private ResidentServiceRestClient residentServiceRestClient;

    private static final String VID = "vid";

    private static final String AUTH_TYPE_CODE_SUFFIX = "-AUTH";

    private static final String GENRATED_ON_TIMESTAMP = "genratedOnTimestamp";

    private static final String EXPIRY_TIMESTAMP = "expiryTimestamp";

    private static final String TRANSACTIONS_LEFT_COUNT = "transactionsLeftCount";

    private static final String TRANSACTION_LIMIT = "transactionLimit";

    private static final String MASKED_VID = "maskedVid";

    private static final String HASH_ATTRIBUTES = "hashAttributes";

    private static final Logger logger = LoggerConfiguration.logConfig(PerpetualVidUtility.class);

    @Autowired
    private MaskDataUtility maskDataUtility;

    @Value("${resident.vid.get.id}")
    private String residentVidGetId;

    @Value("${resident.vid.version.new}")
    private String newVersion;

    @Autowired
    private Utility utility;

    @Value("${perpatual.vid-type:PERPETUAL}")
    private String perpatualVidType;

    @Autowired
    private ResidentTransactionRepository residentTransactionRepository;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private IdentityUtil identityUtil;

    public ResponseWrapper<List<Map<String,?>>> retrieveVids(String residentIndividualId, int timeZoneOffset, String locale) throws ResidentServiceCheckedException, ApisResourceAccessException {
        IdentityDTO identityDTO = identityUtil.getIdentity(residentIndividualId);
        return retrieveVids(timeZoneOffset, locale, identityDTO.getUIN());
    }

    public ResponseWrapper<List<Map<String,?>>> retrieveVidsfromUin(String uin, int timeZoneOffset, String locale) throws ResidentServiceCheckedException, ApisResourceAccessException {
        ResponseWrapper response;
        try {
            response = (ResponseWrapper) residentServiceRestClient.getApi(
                    env.getProperty(ApiName.RETRIEVE_VIDS.name()) + uin, ResponseWrapper.class);
        } catch (Exception e) {
            logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
                    uin, ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode()
                            + e.getMessage() + ExceptionUtils.getStackTrace(e));
            throw new ApisResourceAccessException("Unable to retrieve VID : " + e.getMessage());
        }

        List<Map<String, ?>> filteredList = ((List<Map<String, ?>>) response.getResponse()).stream()
                .map(map -> {
                    LinkedHashMap<String, Object> lhm = new LinkedHashMap<String, Object>(map);
                    getMaskedVid(lhm);
                    getRefIdHash(lhm);
                    normalizeTime(EXPIRY_TIMESTAMP, lhm, timeZoneOffset, locale);
                    normalizeTime(GENRATED_ON_TIMESTAMP, lhm, timeZoneOffset, locale);
                    return lhm;
                })
                .filter(map1 -> map1.get(TRANSACTIONS_LEFT_COUNT) == null || (int) map1.get(TRANSACTIONS_LEFT_COUNT) > 0)
                .collect(Collectors.toList());
        ResponseWrapper<List<Map<String, ?>>> res = new ResponseWrapper<List<Map<String, ?>>>();
        res.setId(residentVidGetId);
        res.setVersion(newVersion);
        res.setResponsetime(DateUtils.getUTCCurrentDateTimeString());
        res.setResponse(filteredList);
        return res;

    }

    private Map<String, Object> getMaskedVid(Map<String, Object> map) {
        String maskedvid = maskDataUtility.convertToMaskData(map.get(VID).toString());
        map.put(MASKED_VID, maskedvid);
        return map;
    }

    private Map<String, Object> getRefIdHash(Map<String, Object> map) {
        try {
            if(map.get(TRANSACTION_LIMIT) != null) {
                String hashrefid = utility.getRefIdHash(map.get(VID).toString());
                int countdb = residentTransactionRepository.findByRefIdAndAuthTypeCodeLike(hashrefid, AUTH_TYPE_CODE_SUFFIX);
                int limitCount =  (int) map.get(TRANSACTION_LIMIT);
                int leftcount = limitCount - countdb;
                if(leftcount < 0) {
                    map.put(TRANSACTIONS_LEFT_COUNT, 0);
                } else {
                    map.put(TRANSACTIONS_LEFT_COUNT, leftcount);
                }
            } else  {
                map.put(TRANSACTIONS_LEFT_COUNT, map.get(TRANSACTION_LIMIT));
            }
            map.remove(HASH_ATTRIBUTES);
        } catch (NoSuchAlgorithmException e) {
            logger.error("NoSuchAlgorithmException", ExceptionUtils.getStackTrace(e));
            logger.error("In getRefIdHash method of ResidentVidServiceImpl class", e.getMessage());
        }
        return map;
    }

    private void normalizeTime(String attributeName, LinkedHashMap<String, Object> lhm, int timeZoneOffset, String locale) {
        Object timeObject = lhm.get(attributeName);
        if(timeObject instanceof String) {
            String timeStr = String.valueOf(timeObject);
            LocalDateTime localDateTime = mapper.convertValue(timeStr, LocalDateTime.class);
            //For the big expiry time, assume no expiry time, so set to null
            if(localDateTime.getYear() >= 9999) {
                lhm.put(attributeName, null);
            } else {
                lhm.put(attributeName, utility.formatWithOffsetForUI(timeZoneOffset, locale, localDateTime)) ;
            }
        }
    }

    public ResponseWrapper<List<Map<String, ?>>> retrieveVids(int timeZoneOffset, String locale, String uin)
            throws ResidentServiceCheckedException, ApisResourceAccessException {
        return retrieveVidsfromUin(uin, timeZoneOffset, locale);
    }

    public Optional<String> getPerpatualVid(String uin) throws ResidentServiceCheckedException, ApisResourceAccessException {
        ResponseWrapper<List<Map<String, ?>>> vidResp = retrieveVidsfromUin(uin, ResidentConstants.UTC_TIMEZONE_OFFSET, null);
        List<Map<String, ?>> vids = vidResp.getResponse();
        if(vids != null && !vids.isEmpty()) {
            return vids.stream()
                .filter(map -> map.containsKey(TemplateVariablesConstants.VID_TYPE) &&
                            perpatualVidType.equalsIgnoreCase(String.valueOf(map.get(TemplateVariablesConstants.VID_TYPE))))
                .map(map -> String.valueOf( map.get(VID)))
                .findAny();
        }
        return Optional.empty();
    }
}
