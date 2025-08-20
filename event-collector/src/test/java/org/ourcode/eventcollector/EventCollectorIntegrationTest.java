package org.ourcode.eventcollector;

import avro.EventEntity;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class EventCollectorIntegrationTest {
    @Container
    static ConfluentKafkaContainer kafka = new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));
    @Container
    static CassandraContainer<?> cassandra = new CassandraContainer<>(DockerImageName.parse("cassandra:4"));

    @Autowired
    ReactiveKafkaProducerTemplate<String, EventEntity> testProducer; // для отправки тестового сообщения

    @BeforeAll
    static void startContainers() {
        kafka.start();
        cassandra.start();
    }

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        // Kafka
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);

        // Cassandra
        registry.add("spring.cassandra.contact-points", () -> cassandra.getHost() + ":" + cassandra.getFirstMappedPort());
        registry.add("spring.cassandra.local-datacenter", () -> "datacenter1");
        registry.add("spring.cassandra.keyspace-name", () -> "test_keyspace");
    }

    @Test
    void shouldConsumeProcessAndPublishDeviceIds() {
        // --- подготовим тестовый event
        EventEntity event = new EventEntity();
        event.setEventId("event-123");
        event.setDeviceId("device-123");
        event.setTimestamp(System.currentTimeMillis());
        event.setType("temperature");
        event.setPayload("{ \"temp\": 42 }");

        // --- отправляем в topic events.in
        StepVerifier.create(testProducer.send("events.in", event.getDeviceId(), event))
                .expectNextCount(1)
                .verifyComplete();

        // --- читаем из device-ids
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("device-ids"));

            var records = consumer.poll(Duration.ofSeconds(10));
            assertThat(records.isEmpty()).isFalse();

            records.forEach(record -> {
                System.out.println("📩 Got from device-ids: key=" + record.key() + ", value=" + record.value());
                assertThat(record.value()).isEqualTo("device-123");
            });
        }
    }
}
