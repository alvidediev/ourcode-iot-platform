package org.ourcode.integration;

import avro.DeviceEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ourcode.model.EventEntity;
import org.ourcode.repository.OutBoxRepository;
import org.ourcode.service.event.EventService;
import org.ourcode.service.outbox.OutBoxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
class FullFlowIntegrationTest {

    @Container
    static ConfluentKafkaContainer kafka = new ConfluentKafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @Container
    static CassandraContainer<?> cassandra = new CassandraContainer<>("cassandra:4.1")
            .withExposedPorts(9042)
            .withInitScript("init.cql");


    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.cassandra.contact-points",
                () -> cassandra.getHost() + ":" + cassandra.getMappedPort(9042));
        registry.add("spring.cassandra.local-datacenter", () -> "datacenter1");
        registry.add("spring.cassandra.keyspace-name", () -> "test_keyspace");
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("db/migration/V1__postgres_debezium_init_script.sql"),
                    "/docker-entrypoint-initdb.d/init.sql");

    @Autowired
    private KafkaTemplate<String, DeviceEvent> kafkaTemplate;

    @Autowired
    private EventService eventService;

    @Autowired
    private OutBoxService outBoxService;

    @Autowired
    private OutBoxRepository outBoxRepository;

    @Autowired
    private CassandraOperations cassandraTemplate;

    @BeforeEach
    void setUp() {
        cassandraTemplate.truncate(EventEntity.class);
        outBoxRepository.deleteAll();

    }

    @Test
    void testFullEventProcessingFlow() {
        DeviceEvent event = DeviceEvent.newBuilder()
                .setEventId("full-flow-event-1")
                .setDeviceId("full-flow-device-1")
                .setTimestamp(System.currentTimeMillis())
                .setType("humidity")
                .setPayload("{\"value\":60}")
                .build();

        kafkaTemplate.send("events.in", event.getDeviceId(), event);

        await().atMost(30, TimeUnit.SECONDS).until(() ->
                !eventService.findAll().isEmpty());

        assertEquals(1, eventService.findAll().size());
        assertEquals("full-flow-event-1",
                eventService.findAll().get(0).getEventId());

        await().atMost(30, TimeUnit.SECONDS).until(() ->
                !outBoxService.findAllUnprocessed().isEmpty());

        assertEquals(1, outBoxService.findAllUnprocessed().size());
    }
}
