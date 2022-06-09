package io.mosip.resident.dto;

import java.beans.PropertyEditorSupport;

public class ResidentTransactionTypeEnumConverter extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {

        String capitalized = text.toUpperCase();

        if(capitalized.equalsIgnoreCase(ResidentTransactionType.AUTHENTICATION_REQUEST.toString()) ||
                capitalized.equalsIgnoreCase(ResidentTransactionType.SERVICE_REQUEST.toString())
                || capitalized.equalsIgnoreCase(ResidentTransactionType.DATA_UPDATE_REQUEST.toString())
                || capitalized.equalsIgnoreCase(ResidentTransactionType.ID_MANAGEMENT_REQUEST.toString())
                || capitalized.equalsIgnoreCase(ResidentTransactionType.DATA_SHARE_REQUEST.toString())) {
            setValue(capitalized);
        } else{
            throw new IllegalArgumentException("Invalid ResidentTransactionType");
        }
    }
}
