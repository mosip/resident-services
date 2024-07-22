package io.mosip.resident.validator;

import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Kamesh Shekhar Prasad
 */

@Component
public class ValidateSameData {

    private static final String AND_LANGUAGE_CODE = " and Language code: ";

    public void validateSameData(JSONObject idRepoJson, JSONObject identity) throws ResidentServiceCheckedException, JSONException {
        for (Object keyObj : identity.keySet()) {
            String key = (String) keyObj;
            Object identityValue = identity.get(key);

            if (identityValue instanceof String) {
                if (idRepoJson.containsKey(key) && idRepoJson.get(key).toString().equalsIgnoreCase((String) identityValue)) {
                    throw new ResidentServiceCheckedException(ResidentErrorCode.SAME_ATTRIBUTE_ALREADY_PRESENT.getErrorCode(),
                            String.format(ResidentErrorCode.SAME_ATTRIBUTE_ALREADY_PRESENT.getErrorMessage(), key));
                }
            } else if (identityValue instanceof ArrayList<?>) {
                if (idRepoJson.containsKey(key)) {
                    ArrayList<Object> identityArray = (ArrayList<Object>) identityValue;
                    JSONArray repoArray = (JSONArray) idRepoJson.get(key);

                    for (Object object : identityArray) {
                        Map identityObj = (Map) object;
                        String language = (String) identityObj.get(ResidentConstants.LANGUAGE);
                        String value = (String) identityObj.get(ResidentConstants.VALUE);

                        for (Object o : repoArray) {
                            JSONObject repoObj = (JSONObject) o;
                            if (repoObj.get(ResidentConstants.LANGUAGE).equals(language)) {
                                if (repoObj.get(ResidentConstants.VALUE).toString().equalsIgnoreCase(value)) {
                                    throw new ResidentServiceCheckedException(ResidentErrorCode.SAME_ATTRIBUTE_ALREADY_PRESENT.getErrorCode(),
                                            String.format(ResidentErrorCode.SAME_ATTRIBUTE_ALREADY_PRESENT.getErrorMessage(), key + AND_LANGUAGE_CODE + language));
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
