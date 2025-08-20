package org.ourcode.eventcollector.model.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Table("device_events")
public class EventEntity {
    @PrimaryKey
    private String eventId;
    private String deviceId;
    private long timestamp;
    private String type;
    private String payload;
}


