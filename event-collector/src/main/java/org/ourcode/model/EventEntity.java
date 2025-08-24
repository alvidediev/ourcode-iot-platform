package org.ourcode.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Getter
@Setter
@Table("device_event")
public class EventEntity {
    @PrimaryKey
    private String eventId;
    private String deviceId;
    private long timestamp;
    private String type;
    private String payload;
}
