package org.ourcode.integrationTest;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
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
@ActiveProfiles("test")
class FullFlowIT {
    private final static Network NETWORK = Network.newNetwork();
    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withExposedPorts(5432)
            .withNetworkAliases("postgres")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("db/migration/V1__postgres_debezium_init_script.sql"),
                    "/docker-entrypoint-initdb.d/init.sql")
            .withNetwork(NETWORK);
    @Container
    public  static ConfluentKafkaContainer kafka = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.6.1")
            .withExposedPorts(9092)
            .withNetworkAliases("kafka")
            .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://kafka:9092")
            .waitingFor(Wait.forLogMessage(".*started.*", 1))
            .withNetwork(NETWORK);
    @Container
    public   static CassandraContainer<?> cassandra = new CassandraContainer<>("cassandra:4.1")
            .withExposedPorts(9042)
            .withInitScript("init.cql")
            .withNetwork(NETWORK);
    @Container
    public static GenericContainer<?> schemaRegistry =
            new GenericContainer<>(DockerImageName.parse("confluentinc/cp-schema-registry:7.6.1"))
                    .withExposedPorts(8081)
                    .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
                    .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:9092")
                    .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
                    .dependsOn(kafka)
                    .withNetwork(NETWORK);

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        //Postgres
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        //Cassandra
        registry.add("spring.cassandra.contact-points",
                () -> cassandra.getHost() + ":" + cassandra.getMappedPort(9042));
        registry.add("spring.cassandra.local-datacenter", () -> "datacenter1");
        registry.add("spring.cassandra.keyspace-name", () -> "test_keyspace");
        registry.add("spring.cassandra.username", cassandra::getUsername);
        registry.add("spring.cassandra.password", cassandra::getPassword);
        registry.add("spring.cassandra.schema-action", () -> "CREATE_IF_NOT_EXISTS");
        //Kafka
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.key-serializer", () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add("spring.kafka.producer.value-serializer", () -> "io.confluent.kafka.serializers.KafkaAvroSerializer");
        registry.add("spring.kafka.consumer.key-deserializer", () -> "org.apache.kafka.common.serialization.StringDeserializer");
        registry.add("spring.kafka.consumer.value-deserializer", () -> "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        //SchemaRegistry
        registry.add("spring.kafka.properties.schema.registry.url", () ->
                "http://" + schemaRegistry.getHost() + ":" + schemaRegistry.getMappedPort(8081));
        // JPA/Hibernate properties
        registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
    }

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

        await().atMost(60, TimeUnit.SECONDS).until(() ->
                !eventService.findAll().isEmpty());

        assertEquals(1, eventService.findAll().size());
        assertEquals("full-flow-event-1",
                eventService.findAll().get(0).getEventId());

        await().atMost(30, TimeUnit.SECONDS).until(() ->
                !outBoxService.findAllUnprocessed().isEmpty());

        assertEquals(1, outBoxService.findAllUnprocessed().size());
    }
}
