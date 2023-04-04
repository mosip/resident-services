package io.mosip.resident.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DocumentResponseDTO is a class that has a String transactionId, a String
 * docId, a String docName, a
 * String docCatCode, a String docTypCode, and a String docFileFormat.
 * 
 * @author Manoj SP
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponseDTO {

    private String transactionId;

    private String docId;

    private String docName;

    private String docCatCode;

    private String docTypCode;

    private String docFileFormat;

}
