package com.ourcode.devicecollector.model.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "device_collector")
public class DeviceCollectorEntity {
    @Id
    private Long id;
    @Column(name = "device_id", unique = true, nullable = false)
    private String deviceId;
    @Column(name = "device_type")
    private String deviceType;
    @Column(name = "created_at")
    private Long createdAt;
    @Column(name = "meta")
    private String meta;
}
