package io.mosip.resident.util;

import io.mosip.resident.constant.EventStatusSuccess;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.TemplateVariablesConstants;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.service.impl.EventStatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Kamesh Shekhar Prasad
 */

@Component
public class DescriptionTemplateVariables {

    @Autowired
    private AuthTypeCodeTemplateData authTypeCodeTemplateData;

    @Autowired
    private EventStatusCode eventStatusCode;

    @Autowired
    private AttributeBasedOnLangCode attributeBasedOnLangCode;

    @Autowired
    private AttributesDisplayText attributesDisplayText;

    public String getDescriptionTemplateVariablesForOrderPhysicalCard(
			ResidentTransactionEntity residentTransactionEntity, String fileText, String languageCode) {
		return fileText;
	}

    public String getDescriptionTemplateVariablesForGetMyId(ResidentTransactionEntity residentTransactionEntity,
String fileText, String languageCode) {
        return fileText;
    }

    public String getDescriptionTemplateVariablesForUpdateMyUin(ResidentTransactionEntity residentTransactionEntity,
String fileText, String languageCode) {
        return fileText;
    }

    public String replaceNullWithEmptyString(String input) {
        return input == null ? "" : input;
    }

    public String getDescriptionTemplateVariablesForManageMyVid(ResidentTransactionEntity residentTransactionEntity,
                                                                       String fileText, String languageCode) {
		RequestType requestType = RequestType.DEFAULT;
		String templateData = "";
		fileText = fileText.replace(ResidentConstants.DOLLAR + TemplateVariablesConstants.VID_TYPE,
				replaceNullWithEmptyString(residentTransactionEntity.getRefIdType()));
		fileText = fileText.replace(ResidentConstants.DOLLAR + TemplateVariablesConstants.MASKED_VID,
				replaceNullWithEmptyString(residentTransactionEntity.getRefId()));
		if (RequestType.GENERATE_VID.name().equalsIgnoreCase(residentTransactionEntity.getRequestTypeCode())) {
			requestType = RequestType.GENERATE_VID;
		} else if (RequestType.REVOKE_VID.name().equalsIgnoreCase(residentTransactionEntity.getRequestTypeCode())) {
			requestType = RequestType.REVOKE_VID;
		}
		templateData = attributeBasedOnLangCode.getAttributeBasedOnLangcode(requestType.name(), languageCode);
		fileText = fileText.replace(ResidentConstants.DOLLAR + TemplateVariablesConstants.ACTION_PERFORMED, templateData);
		return fileText;
	}

    public String getDescriptionTemplateVariablesForVidCardDownload(ResidentTransactionEntity residentTransactionEntity,
String fileText, String languageCode) {
        return fileText;
    }

    public String getDescriptionTemplateVariablesForValidateOtp(ResidentTransactionEntity residentTransactionEntity,
String fileText, String languageCode) {
        String channelsTemplateData = attributesDisplayText.getAttributesDisplayText(
                residentTransactionEntity.getAttributeList(), languageCode, RequestType.VALIDATE_OTP);
            fileText = fileText.replace(ResidentConstants.DOLLAR + TemplateVariablesConstants.CHANNEL, channelsTemplateData);
        return fileText;
    }

    public String getDescriptionTemplateVariablesForSecureMyId(ResidentTransactionEntity residentTransactionEntity,
String fileText, String languageCode) {
        String authTypeFromDB;
        if (residentTransactionEntity.getAttributeList() != null && !residentTransactionEntity.getAttributeList().isEmpty()) {
            authTypeFromDB = residentTransactionEntity.getAttributeList();
        } else {
            authTypeFromDB = residentTransactionEntity.getPurpose();
        }
        if (authTypeFromDB != null) {
            List<String> authTypeListFromEntity = List
                    .of(authTypeFromDB.split(ResidentConstants.ATTRIBUTE_LIST_DELIMITER));
            return authTypeListFromEntity.stream().map(authType -> {
                String fileTextTemplate = fileText;
                String templateData = "";
                if (authType.contains(EventStatusSuccess.UNLOCKED.name())) {
                    templateData = attributeBasedOnLangCode.getAttributeBasedOnLangcode(EventStatusSuccess.UNLOCKED.name(), languageCode);
                    fileTextTemplate = fileTextTemplate.replace(ResidentConstants.DOLLAR + ResidentConstants.STATUS,
                            templateData);
                } else {
                    templateData = attributeBasedOnLangCode.getAttributeBasedOnLangcode(EventStatusSuccess.LOCKED.name(), languageCode);
                    fileTextTemplate = fileTextTemplate.replace(ResidentConstants.DOLLAR + ResidentConstants.STATUS,
                            templateData);
                }
                templateData = attributeBasedOnLangCode.getAttributeBasedOnLangcode(authType.split(ResidentConstants.COLON)[0].trim(), languageCode);
                fileTextTemplate = fileTextTemplate.replace(ResidentConstants.DOLLAR + ResidentConstants.AUTH_TYPE,
                        templateData);
                return fileTextTemplate;
            }).collect(Collectors.joining(ResidentConstants.UI_ATTRIBUTE_DATA_DELIMITER));
        }
        return fileText;
    }

    public String getDescriptionTemplateVariablesForDownloadPersonalizedCard(
            ResidentTransactionEntity residentTransactionEntity, String fileText, String languageCode) {
        return fileText;
    }

    public String getDescriptionTemplateVariablesForShareCredentialWithPartner(ResidentTransactionEntity residentTransactionEntity,
                                                                                      String fileText, String languageCode) {
		return fileText;
	}

    public String getDescriptionTemplateVariablesForAuthenticationRequest(
            ResidentTransactionEntity residentTransactionEntity, String fileText, String languageCode) {
        String statusCode = eventStatusCode.getEventStatusCode(residentTransactionEntity.getStatusCode(), languageCode)
                .getT1();
        return authTypeCodeTemplateData.getAuthTypeCodeTemplateData(residentTransactionEntity.getAuthTypeCode(), statusCode, languageCode);
    }
}
