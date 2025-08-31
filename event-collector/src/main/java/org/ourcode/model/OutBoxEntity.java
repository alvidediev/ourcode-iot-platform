package org.ourcode.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "outbox_events")
public class OutBoxEntity {
    @Id
    @Column(name = "eventid")
    private String eventId;
    @Column(name = "deviceid")
    private String deviceId;
    @Column(name = "timestamp")
    private long timestamp;
    @Column(name = "type")
    private String type;
    @Column(name = "payload")
    private String payload;
    @Column(name = "is_processed")
    private boolean isProcessed;
}
