package org.ourcode.eventcollector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ourcode.eventcollector.service.kafka.events.consumer.KafkaConsumer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;

import static reactor.util.retry.Retry.backoff;

@Slf4j
@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor
public class EventCollectorApplication {

    private final KafkaConsumer kafkaEventConsumer;

    public static void main(String[] args) {
        SpringApplication.run(EventCollectorApplication.class, args);
    }

//    @EventListener(ApplicationReadyEvent.class)
//    public void startKafkaConsumer() {
//        log.info("Starting kafka consumer ...");
//        kafkaEventConsumer.consumeEvents()
//                .doOnError(error -> log.error("Error in Kafka consumer: {}", error.getMessage()))
//                .retryWhen(
//                        backoff(Long.MAX_VALUE, Duration.ofSeconds(5))
//                                .maxBackoff(Duration.ofMinutes(1))
//                                .doBeforeRetry(sig -> log.info("Restarting Kafka consumer..."))
//                )
//                .subscribe(
//                        null,
//                        err -> log.error("Consumer stream terminated: {}", err.getMessage()),
//                        () -> log.info("Consumer stream completed")
//                );
//    }

}
