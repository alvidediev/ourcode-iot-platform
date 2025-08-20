package org.ourcode.eventcollector.model.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Getter
@Setter
@Table("event_outbox")
public class EventOutboxEntity {
    @PrimaryKey
    private String eventId;
    private String deviceId;
    private long timestamp;
    private String type;
    private String payload;
    private String status; // NEW, SENT, FAILED
}
