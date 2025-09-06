package org.ourcode.service.kafka.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class DeviceIdProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final static String DEVICE_IDS_TOPIC = "device-ids";

    public void sendProcessedDeviceIds(Set<String> deviceIds) {
        CompletableFuture.runAsync(() -> deviceIds.forEach(deviceId -> kafkaTemplate.send(DEVICE_IDS_TOPIC, deviceId)));
    }
}
