package org.ourcode.eventcollector.service.kafka.events.consumer;

import avro.DeviceEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ourcode.eventcollector.service.processor.EventBatchProcessor;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;

import java.time.Duration;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final ReactiveKafkaConsumerTemplate<String, DeviceEvent> reactiveKafkaConsumer;
    private final EventBatchProcessor eventBatchProcessor;

    private final static int CONCURRENCY = 5;

    @PostConstruct
    public void consumeEvents() {
        reactiveKafkaConsumer
                .receive()
                .doOnNext(record -> log.debug("Received record: key={}, value={}",
                        record.key(), record.value()))
                .bufferTimeout(100, Duration.ofMillis(500))
                .flatMap(batch -> {
                    List<DeviceEvent> events = batch.stream()
                            .map(ReceiverRecord::value)
                            .toList();

                    log.info("Processing batch of {} events", events.size());

                    return eventBatchProcessor.processBatch(events)
                            .then(commitOffsets(batch));
                }, CONCURRENCY)
                .doOnError(err -> log.error("Error while consuming events", err))
                .subscribe(
                        null,
                        error -> log.error("Kafka consumer stream error", error),
                        () -> log.info("Kafka consumer stream completed")
                );
    }

    private Mono<Void> commitOffsets(List<ReceiverRecord<String, DeviceEvent>> records) {
        return Flux.fromIterable(records)
                .groupBy(r -> r.receiverOffset().topicPartition())
                .flatMap(group -> group
                        .reduce((r1, r2) -> r1.offset() > r2.offset() ? r1 : r2)
                        .flatMap(r -> r.receiverOffset().commit()))
                .then();
    }
}
