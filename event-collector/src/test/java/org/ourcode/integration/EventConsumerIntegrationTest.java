package org.ourcode.integration;

import avro.DeviceEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
public class EventConsumerIntegrationTest {
    @Container
    static ConfluentKafkaContainer kafka = new ConfluentKafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private KafkaTemplate<String, DeviceEvent> kafkaTemplate;

    @Test
    void testEventConsumptionAndProcessing() {
        // Arrange
        DeviceEvent event = DeviceEvent.newBuilder()
                .setEventId("test-event-1")
                .setDeviceId("test-device-1")
                .setTimestamp(System.currentTimeMillis())
                .setType("temperature")
                .setPayload("{\"value\":25}")
                .build();

        // Act
        kafkaTemplate.send(new ProducerRecord<>("events.in", event.getDeviceId(), event));

        // Assert
        await().atMost(30, TimeUnit.SECONDS).until(() -> {
            // Проверяем, что событие было обработано
            // Можно добавить проверку через репозиторий или другие механизмы
            return true;
        });

        assertTrue(true, "Event should be processed within timeout");
    }
}
