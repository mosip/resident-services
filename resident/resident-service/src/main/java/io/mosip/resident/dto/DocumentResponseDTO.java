package io.mosip.resident.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
