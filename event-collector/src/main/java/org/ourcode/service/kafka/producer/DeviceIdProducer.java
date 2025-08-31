package org.ourcode.service.kafka.producer;

import lombok.RequiredArgsConstructor;
import org.ourcode.model.OutBoxEntity;
import org.ourcode.service.outbox.OutBoxService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class DeviceIdProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendProcessedDeviceIds(List<String>  deviceIds) {
        CompletableFuture.runAsync(() -> deviceIds.forEach(deviceId -> kafkaTemplate.send("device-ids", deviceId)));
    }
}
