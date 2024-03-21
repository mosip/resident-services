package io.mosip.resident.dto;

import lombok.Data;

/**
 * @author Kamesh Shekhar Prasad
 */
@Data
public class WorkflowCompletedEventDTO {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new message DTO.
     */
    public WorkflowCompletedEventDTO() {
        super();
    }

    /** The registration id. */
    private String instanceId;
    private String resultCode;
    private String workflowType;
    private String errorCode;
}
