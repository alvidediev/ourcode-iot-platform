package com.ourcode.devicecollector.service.deviceCollector;

import avro.DeviceCollector;
import com.ourcode.devicecollector.model.entity.DeviceCollectorEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;

public interface DeviceCollectorService {

    /**
     * Сохранение уникальных device-id c ретрай механизмом
     */
    @Retryable
    @Transactional
    void saveDevice(DeviceCollector deviceCollector);

    /**
     * Отправка в DLT
     */
    void sendToDlt(String originalMessage, String errorMessage);

    /**
     * Найти сущность device по deviceId
     */
    DeviceCollectorEntity findByDeviceId(String deviceId);
}
