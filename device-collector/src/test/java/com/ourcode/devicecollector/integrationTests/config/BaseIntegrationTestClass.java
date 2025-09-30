package com.ourcode.devicecollector.integrationTests.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseIntegrationTestClass {

    private static final Network NETWORK = Network.newNetwork();

    @Container
    public static GenericContainer<?> postgres1 = new GenericContainer<>("bitnami/postgresql:14")
            .withEnv("POSTGRES_DB", "device_collector_db1_master")
            .withNetworkAliases("postgres1")
            .withEnv("POSTGRES_USER", "postgres")
            .withEnv("POSTGRES_PASSWORD", "postgres")
            .withEnv("POSTGRESQL_REPLICATION_MODE", "master")
            .withEnv("POSTGRESQL_REPLICATION_USER", "repl_user")
            .withEnv("POSTGRESQL_REPLICATION_PASSWORD", "repl_pass")
            .withExposedPorts(5432)
            .withNetwork(NETWORK)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5)));

    @Container
    public static GenericContainer<?> postgres2 = new GenericContainer<>("bitnami/postgresql:14")
            .withEnv("POSTGRES_DB", "device_collector_db1_master")
            .withEnv("POSTGRES_USER", "postgres")
            .withNetworkAliases("postgres2")
            .withEnv("POSTGRES_PASSWORD", "postgres")
            .withEnv("POSTGRESQL_MASTER_HOST", "postgres1")
            .withEnv("POSTGRESQL_REPLICATION_MODE", "slave")
            .withEnv("POSTGRESQL_REPLICATION_USER", "repl_user")
            .withEnv("POSTGRESQL_REPLICATION_PASSWORD", "repl_pass")
            .withExposedPorts(5432)
            .dependsOn(postgres1)
            .withNetwork(NETWORK)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5)));

    @Container
    public static GenericContainer<?> postgres3 = new GenericContainer<>("bitnami/postgresql:14")
            .withEnv("POSTGRES_DB", "device_collector_db2_master")
            .withNetworkAliases("postgres3")
            .withEnv("POSTGRES_USER", "postgres")
            .withEnv("POSTGRES_PASSWORD", "postgres")
            .withEnv("POSTGRESQL_REPLICATION_MODE", "master")
            .withEnv("POSTGRESQL_REPLICATION_USER", "repl_user")
            .withEnv("POSTGRESQL_REPLICATION_PASSWORD", "repl_pass")
            .withExposedPorts(5432)
            .withNetwork(NETWORK)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5)));

    @Container
    public static GenericContainer<?> postgres4 = new GenericContainer<>("bitnami/postgresql:14")
            .withEnv("POSTGRES_DB", "device_collector_db2_master")
            .withEnv("POSTGRES_USER", "postgres")
            .withNetworkAliases("postgres4")
            .withEnv("POSTGRES_PASSWORD", "postgres")
            .withEnv("POSTGRESQL_MASTER_HOST", "postgres3")
            .withEnv("POSTGRESQL_REPLICATION_MODE", "slave")
            .withEnv("POSTGRESQL_REPLICATION_USER", "repl_user")
            .withEnv("POSTGRESQL_REPLICATION_PASSWORD", "repl_pass")
            .withExposedPorts(5432)
            .dependsOn(postgres3)
            .withNetwork(NETWORK)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5)));

    @Container
    public static ConfluentKafkaContainer kafka = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.6.1")
            .withExposedPorts(9092)
            .withNetworkAliases("kafka")
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
            .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://kafka:9092")
            .waitingFor(Wait.forLogMessage(".*started.*", 1))
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
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.key-serializer",
                () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add("spring.kafka.producer.value-serializer",
                () -> "io.confluent.kafka.serializers.KafkaAvroSerializer");
        registry.add("spring.kafka.consumer.key-deserializer",
                () -> "org.apache.kafka.common.serialization.StringDeserializer");
        registry.add("spring.kafka.consumer.value-deserializer",
                () -> "io.confluent.kafka.serializers.KafkaAvroDeserializer");

        // Schema Registry
        registry.add("spring.kafka.properties.schema.registry.url", () ->
                "http://" + schemaRegistry.getHost() + ":" + schemaRegistry.getMappedPort(8081));

        // Consumer settings
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.consumer.enable-auto-commit", () -> "false");
        registry.add("spring.kafka.consumer.max-poll-records", () -> "100");
        registry.add("spring.kafka.consumer.group-id", () -> "test-group");

        //App settings
        registry.add("datasource.username", () -> "postgres");
        registry.add("datasource.password", () -> "postgres");
        registry.add("datasource.driverClassName", () -> "org.postgresql.Driver");
        registry.add("sharding.shard0Master.url", () ->
                "jdbc:postgresql://" + postgres1.getHost() + ":" + postgres1.getFirstMappedPort() + "/device_collector_db1_master");
        registry.add("sharding.shard0Replica.url", () ->
                "jdbc:postgresql://" + postgres2.getHost() + ":" + postgres2.getFirstMappedPort() + "/device_collector_db1_master");
        registry.add("sharding.shard1Master.url", () ->
                "jdbc:postgresql://" + postgres3.getHost() + ":" + postgres3.getFirstMappedPort() + "/device_collector_db2_master");
        registry.add("sharding.shard1Replica.url", () ->
                "jdbc:postgresql://" + postgres4.getHost() + ":" + postgres4.getFirstMappedPort() + "/device_collector_db2_master");
    }
}
