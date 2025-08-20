package org.ourcode.eventcollector;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ourcode.eventcollector.model.entity.EventEntity;
import org.ourcode.eventcollector.service.kafka.events.producer.KafkaEventProducer;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.kafka.sender.SenderResult;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class KafkaEventProducerTest {

    @Test
    void shouldPublishDeviceIdsSuccessfully() {
        // given
        ReactiveKafkaProducerTemplate<String, String> producerMock = Mockito.mock(ReactiveKafkaProducerTemplate.class);

        // Заглушаем producer.send() чтобы он всегда возвращал Mono.success

        Mono<SenderResult<Void>> dummyResult = Mono.just(Mockito.mock(SenderResult.class));
        when(producerMock.send(any(ProducerRecord.class))).thenReturn(dummyResult);

        KafkaEventProducer kafkaEventProducer = new KafkaEventProducer(producerMock);

        EventEntity event1 = buildEvent("event-1", "device-1");
        EventEntity event2 = buildEvent("event-2", "device-2");
        List<EventEntity> events = List.of(event1, event2);

        // when
        Mono<Void> result = kafkaEventProducer.publishDeviceIds(events);

        // then
        StepVerifier.create(result)
                .verifyComplete();

        Mockito.verify(producerMock, Mockito.times(2)).send(any(ProducerRecord.class));
    }

    @Test
    void shouldFailIfProducerErrors() {
        // given
        ReactiveKafkaProducerTemplate<String, String> producerMock = Mockito.mock(ReactiveKafkaProducerTemplate.class);

        when(producerMock.send(any(ProducerRecord.class)))
                .thenReturn(Mono.error(new RuntimeException("Kafka send failed")));

        KafkaEventProducer kafkaEventProducer = new KafkaEventProducer(producerMock);

        EventEntity event = buildEvent("event-1", "device-1");

        // when
        Mono<Void> result = kafkaEventProducer.publishDeviceIds(List.of(event));

        // then
        StepVerifier.create(result)
                .expectErrorMatches(err -> err instanceof RuntimeException &&
                        err.getMessage().equals("Kafka send failed"))
                .verify();

        Mockito.verify(producerMock, Mockito.times(1)).send(any(ProducerRecord.class));
    }

    private EventEntity buildEvent(String eventId, String deviceId) {
        EventEntity event = new EventEntity();
        // через reflection или сеттеры, если их добавишь
        try {
            var idField = EventEntity.class.getDeclaredField("eventId");
            idField.setAccessible(true);
            idField.set(event, eventId);

            var devField = EventEntity.class.getDeclaredField("deviceId");
            devField.setAccessible(true);
            devField.set(event, deviceId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return event;
    }
}

