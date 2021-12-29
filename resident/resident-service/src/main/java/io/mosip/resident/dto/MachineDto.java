
package io.mosip.resident.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response dto for Machine Detail
 *
 * @author Sowmya
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineDto {

    private String id;

    /**
     * Field for machine name
     */
    private String name;

    /**
     * Field for machine serial number
     */
    private String serialNum;

    /**
     * Field for machine mac address
     */
    private String macAddress;

    /**
     * Field for machine IP address
     */
    private String ipAddress;

    /**
     * Field for machine specification Id
     */
    private String machineSpecId;

    /**
     * Field for language code
     */
    private String langCode;

    /**
     * Field for is active
     */
    private Boolean isActive;

    /**
     * Field for is validity of the Device
     */
    private String validityDateTime;

    /**
     * Field for zone code
     */
    private String zoneCode;

    /**
     * Field for zone
     */
    private String zone;

    /**
     * Field for Machine Type Name
     */
    private String machineTypeName;

    /**
     * Field for Map Status
     */
    private String mapStatus;

    /**
     * Field for registration center id
     */
    private String regCenterId;

    /**
     * Field for public key
     */
    private String publicKey;

    /**
     * Field for sign public key
     */
    private String signPublicKey;

}
