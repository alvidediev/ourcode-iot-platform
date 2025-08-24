package org.ourcode.service.kafka.consumer;

import avro.DeviceEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.ourcode.model.EventEntity;
import org.ourcode.model.OutBoxEntity;
import org.ourcode.service.event.EventService;
import org.ourcode.service.outbox.OutBoxService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventConsumer {
    private final EventService eventService;
    private final OutBoxService outBoxService;

    //Кафка клиент для получения ивентов батчем
    private final KafkaTemplate<String, DeviceEvent> kafkaTemplate;
    //Кафка клиент для отправки DeviceId

    //Кэш для дедупликации
    private final Set<String> publishedDeviceIds = ConcurrentHashMap.newKeySet();

    //Имитация работы TODO: удалить
//    @PostConstruct
//    public void init() {
//        for (int i = 0; i < 1000; i++) {
//            DeviceEvent event = new DeviceEvent();
//            event.setEventId("event-" + i);
//            event.setDeviceId("device-" + (i % 100));
//            event.setTimestamp(System.currentTimeMillis());
//            event.setType("temp");
//            event.setPayload("{\"value\":" + new Random().nextInt(100) + "}");
//
//            kafkaTemplate.send("events.in", event.getDeviceId(), event);
//        }
//    }

    @KafkaListener(topics = "events.in", containerFactory = "batchListenerFactory")
    public void handleBatch(List<ConsumerRecord<String, DeviceEvent>> records) {
        // 1. Сохраняем все события в Cassandra асинхронно
        List<EventEntity> events = records.stream()
                .map(ConsumerRecord::value)
                .map(this::getEventEntity)
                .collect(Collectors.toList());

        List<OutBoxEntity> collect = records.stream()
                .map(ConsumerRecord::value)
                .map(this::getOutBoxEntity)
                .toList();

        CompletableFuture<Void> saveFuture = CompletableFuture.runAsync(() -> {
            try {
                eventService.saveAll(events);
                outBoxService.saveAll(collect);
            } catch (Exception e) {
                throw new RuntimeException("Failed to save events", e);
            }
        }).exceptionally(ex -> {
            System.err.println("Error saving events: " + ex.getMessage());
            ex.printStackTrace();
            return null; // Возвращаем null, так как Void
        });

//        // 2. Публикуем уникальные device_id
//        CompletableFuture<Void> publishFuture = CompletableFuture.runAsync(() -> events.stream()
//                .map(EventEntity::getDeviceId)
//                .distinct()
//                .filter(deviceId -> !publishedDeviceIds.contains(deviceId))
//                .forEach(deviceId -> {
//                    kafkaTemplateStr.send("device-ids", deviceId, deviceId);
//                    publishedDeviceIds.add(deviceId);
//                }));

        // Ждём завершения обоих операций
        CompletableFuture.allOf(saveFuture).join();
    }

    private EventEntity getEventEntity(DeviceEvent s) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setEventId(s.getEventId());
        eventEntity.setDeviceId(s.getDeviceId());
        eventEntity.setTimestamp(s.getTimestamp());
        eventEntity.setPayload(s.getPayload());
        return eventEntity;
    }

    private OutBoxEntity  getOutBoxEntity(DeviceEvent s) {
        OutBoxEntity outBoxEntity = new OutBoxEntity();
        outBoxEntity.setEventId(s.getEventId());
        outBoxEntity.setDeviceId(s.getDeviceId());
        outBoxEntity.setTimestamp(s.getTimestamp());
        outBoxEntity.setPayload(s.getPayload());
        outBoxEntity.setProcessed(false);
        return outBoxEntity;
    }
}
