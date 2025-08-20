package org.ourcode.eventcollector.service.kafka.events.producer;

import avro.DeviceEvent;
import avro.DeviceId;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.ourcode.eventcollector.model.entity.EventOutboxEntity;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class KafkaEventProducer {
    private final ReactiveKafkaProducerTemplate<String, DeviceId> reactiveKafkaProducerTemplate;
    private static final String DEVICE_IDS_TOPIC = "device-ids";


//    @PostConstruct
//    public void init() {
//        for (int i = 0; i < 1000; i++) {
//            DeviceId event = new DeviceId();
//            event.setEventId("event-" + i);
//            event.setDeviceId("device-" + (i % 100));
//            event.setTimestamp(System.currentTimeMillis());
//            event.setType("temp");
//            event.setPayload("{\"value\":" + new Random().nextInt(100) + "}");
//
//            reactiveKafkaProducerTemplate.send("events.in", event.getDeviceId(), event).subscribe();
//        }
//    }

    public Mono<Void> publishDeviceIds(List<EventOutboxEntity> events) {
        List<DeviceId> list = events.stream()
                .map(EventOutboxEntity::getDeviceId)
                .distinct()
                .map(DeviceId::new)
                .toList();
        return Flux.fromIterable(list)
                .map(deviceId -> new ProducerRecord<>(DEVICE_IDS_TOPIC, deviceId.getEventId(), deviceId))
                .flatMap(reactiveKafkaProducerTemplate::send)
                .then();
    }
}
