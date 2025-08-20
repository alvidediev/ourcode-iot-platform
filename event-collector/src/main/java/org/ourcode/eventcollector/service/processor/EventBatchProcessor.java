package org.ourcode.eventcollector.service.processor;

import avro.DeviceEvent;
import lombok.RequiredArgsConstructor;
import org.ourcode.eventcollector.model.entity.EventOutboxEntity;
import org.ourcode.eventcollector.repository.EventRepository;
import org.ourcode.eventcollector.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventBatchProcessor {
    private final EventRepository eventRepository;
    private final OutboxRepository outboxRepository;

    public Mono<Void> processBatch(List<DeviceEvent> events) {
        List<org.ourcode.eventcollector.model.entity.EventEntity> list = events.stream()
                .map(this::toCassandraEntity)
                .toList();


        return eventRepository.saveAll(list)
                .thenMany(outboxRepository.saveAll(
                        events.stream().map(this::toOutbox).toList()
                ))
                .then();
    }

    private org.ourcode.eventcollector.model.entity.EventEntity toCassandraEntity(DeviceEvent eventEntity) {
        org.ourcode.eventcollector.model.entity.EventEntity cassandraEntity = new org.ourcode.eventcollector.model.entity.EventEntity();
        cassandraEntity.setType(eventEntity.getType());
        cassandraEntity.setTimestamp(eventEntity.getTimestamp());
        cassandraEntity.setPayload(eventEntity.getPayload());
        cassandraEntity.setDeviceId(eventEntity.getDeviceId());
        cassandraEntity.setEventId(eventEntity.getEventId());
        return cassandraEntity;
    }

    private EventOutboxEntity toOutbox(DeviceEvent e) {
        EventOutboxEntity out = new EventOutboxEntity();
        out.setEventId(e.getEventId());
        out.setDeviceId(e.getDeviceId());
        out.setTimestamp(e.getTimestamp());
        out.setType(e.getType());
        out.setPayload(e.getPayload());
        out.setStatus("NEW");
        return out;
    }
}
