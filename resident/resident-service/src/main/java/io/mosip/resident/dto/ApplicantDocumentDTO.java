package io.mosip.resident.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * This class used to capture the documents, photograph, exceptional photograph
 * and Acknowledgement Receipt of the Individual.
 *
 * @author Sowmya
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApplicantDocumentDTO extends BaseDTO implements Serializable {
	private byte[] document;
	private String value;
	private String type;
	private String category;
	private String owner;
	private String format;
}
