package org.ourcode.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OutBoxDto {
    @JsonProperty(value = "eventid")
    private String eventId;
    @JsonProperty(value = "deviceid")
    private String deviceId;
    private long timestamp;
    private String type;
    private String payload;
    @JsonProperty("is_processed")
    private boolean processed;
}
