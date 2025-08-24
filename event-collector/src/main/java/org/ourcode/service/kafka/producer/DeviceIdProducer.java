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
    private final OutBoxService outBoxService;

    public void sendProcessedDeviceIds() {

        CompletableFuture.runAsync(() -> {
            List<OutBoxEntity> allProcessed = outBoxService.findAllProcessed();

            allProcessed.stream().map(OutBoxEntity::getDeviceId)
                    .distinct()
                    .forEach(deviceId -> {
                        kafkaTemplate.send("device-ids", deviceId);
                    });
        });
    }
}
