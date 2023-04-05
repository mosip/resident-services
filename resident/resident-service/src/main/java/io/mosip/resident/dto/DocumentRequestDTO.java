package io.mosip.resident.dto;

import lombok.Data;

/**
 * DocumentRequestDTO is a class that has three fields: docCatCode, docTypCode,
 * and langCode
 * 
 * @author Manoj SP
 */
@Data
public class DocumentRequestDTO {

    private String docCatCode;

    private String docTypCode;

    private String langCode;
    
    private String referenceId;

}
