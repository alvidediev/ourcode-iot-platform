package org.ourcode.eventcollector.service.OutBoxPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ourcode.eventcollector.model.entity.EventOutboxEntity;
import org.ourcode.eventcollector.repository.OutboxRepository;
import org.ourcode.eventcollector.service.kafka.events.producer.KafkaEventProducer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPublisher {
    private final OutboxRepository outboxRepository;
    private final KafkaEventProducer kafkaEventProducer;

    @Scheduled(fixedDelay = 5000) // каждые 5 секунд
    public void publishOutbox() {
        outboxRepository.findByStatus("NEW")
                .collectList()
                .flatMapMany(event ->
                        kafkaEventProducer.publishDeviceIds(event)
                                .thenMany(outboxRepository.saveAll(markSent(event)))
                                .onErrorResume(ex -> {
                                    log.error("Failed to publish event {}", ex.getMessage());
                                    return outboxRepository.saveAll(markFailed(event));
                                }))
                .subscribe();
    }

    private List<EventOutboxEntity> markSent(List<EventOutboxEntity> e) {
        e.forEach(events -> events.setStatus("SENT"));
        return e;
    }

    private List<EventOutboxEntity> markFailed(List<EventOutboxEntity> e) {
        e.forEach(events -> events.setStatus("FAILED"));
        return e;
    }
}
