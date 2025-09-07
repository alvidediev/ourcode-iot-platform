package org.ourcode.service.kafka.consumer;

import avro.DeviceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.ourcode.mapper.EventMapper;
import org.ourcode.model.EventEntity;
import org.ourcode.service.event.EventService;
import org.ourcode.service.kafka.producer.DeviceIdProducer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventConsumer {
    private final EventService eventService;
    private final DeviceIdProducer deviceIdProducer;
    private final EventMapper eventMapper;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @KafkaListener(topics = "events.in", containerFactory = "batchListenerFactory")
    public void handleBatch(List<ConsumerRecord<String, DeviceEvent>> records, Acknowledgment ack) {
        List<EventEntity> events = records.stream()
                .map(ConsumerRecord::value)
                .map(eventMapper::deviceEventToEntity)
                .collect(Collectors.toList());

        CompletableFuture.runAsync(() -> eventService.saveAll(events), executorService).whenComplete((res, ex) -> {
            if (ex == null) {
                ack.acknowledge();
                Set<String> uniqueDeviceIds = events.stream()
                        .map(EventEntity::getDeviceId)
                        .collect(Collectors.toSet());
                deviceIdProducer.sendProcessedDeviceIds(uniqueDeviceIds);
            } else {
                log.error("Error saving events: ", ex);
            }
        });
    }
}
