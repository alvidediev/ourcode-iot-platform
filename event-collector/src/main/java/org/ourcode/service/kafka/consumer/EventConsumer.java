package org.ourcode.service.kafka.consumer;

import avro.DeviceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.ourcode.model.EventEntity;
import org.ourcode.model.OutBoxEntity;
import org.ourcode.service.event.EventService;
import org.ourcode.service.outbox.OutBoxService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventConsumer {
    private final EventService eventService;
    private final OutBoxService outBoxService;

    @KafkaListener(topics = "events.in", containerFactory = "batchListenerFactory")
    public void handleBatch(List<ConsumerRecord<String, DeviceEvent>> records) {
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
            log.error("Error saving events {}: ", ex.getMessage());
            return null;
        });

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
