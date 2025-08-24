package org.ourcode.service.kafka.consumer;

import avro.DeviceEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.ourcode.model.OutBoxEntity;
import org.ourcode.model.dto.OutBoxDto;
import org.ourcode.service.kafka.producer.DeviceIdProducer;
import org.ourcode.service.outbox.OutBoxService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CdcEventConsumer {
    private final OutBoxService outBoxService;
    private final DeviceIdProducer deviceIdProducer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(
            topics = "ourcode.public.outbox_events",
            containerFactory = "cdcListenerFactory", // Нужно создать специальный factory
            groupId = "cdc-group"
    )
    public void handleCdcEvents(List<ConsumerRecord<String, String>> records) {
        records.forEach(record -> {
            String value = record.value();
            try {
                JsonNode root = objectMapper.readTree(record.value());
                JsonNode after = root.get("after"); // <-- берем "после"
                if (after != null && !after.isNull()) {
                    OutBoxDto deviceEvent = objectMapper.treeToValue(after, OutBoxDto.class);
                    outBoxService.markAsProcessed(deviceEvent.getDeviceId());
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }



        });
        deviceIdProducer.sendProcessedDeviceIds();
    }
}
