package org.ourcode.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ourcode.model.OutBoxEntity;
import org.ourcode.repository.OutBoxRepository;
import org.ourcode.service.outbox.OutBoxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.data.cassandra.enabled=false",
        "spring.cassandra.connection.timeout=100ms"
})
@Testcontainers
class OutBoxServiceIntegrationTest {

    private static final Network NETWORK = Network.newNetwork();


    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("db/migration/V1__postgres_debezium_init_script.sql"),
                    "/docker-entrypoint-initdb.d/init.sql")
            .withNetwork(NETWORK);

    @Container
    static ConfluentKafkaContainer kafka = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.6.1")
            .withNetwork(NETWORK)
            .withExposedPorts(9092)
            .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://kafka:9092")
            .withNetworkAliases("kafka")
            .waitingFor(Wait.forLogMessage(".*started.*", 1));;

    @Container
    static GenericContainer<?> schemaRegistry =
            new GenericContainer<>(DockerImageName.parse("confluentinc/cp-schema-registry:7.6.1"))
                    .withExposedPorts(8081)
                    .withNetwork(NETWORK)
                    .withNetworkAliases("schema-registry")
                    .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
                    .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:9092")
                    .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
                    .dependsOn(kafka);


    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.properties.schema.registry.url", () ->
                "http://" + schemaRegistry.getHost() + ":" + schemaRegistry.getMappedPort(8081));

    }

    @Autowired
    private OutBoxService outBoxService;

    @Test
    void testSaveAndMarkAsProcessed() {
        OutBoxEntity entity = new OutBoxEntity();
        entity.setEventId("event-123");
        entity.setDeviceId("device-1");
        entity.setTimestamp(System.currentTimeMillis());
        entity.setType("temperature");
        entity.setPayload("{\"value\":42}");
        entity.setProcessed(false);

        outBoxService.saveAll(List.of(entity));

        List<OutBoxEntity> unprocessed = outBoxService.findAllUnprocessed();
        assertThat(unprocessed).hasSize(1);

        outBoxService.markAsProcessed("device-1");

        List<OutBoxEntity> processed = outBoxService.findAllProcessed();
        assertThat(processed).hasSize(1);
        assertThat(processed.get(0).isProcessed()).isTrue();
    }
}
