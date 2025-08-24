package org.ourcode.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.ourcode.model.OutBoxEntity;
import org.ourcode.model.dto.OutBoxDto;
import org.ourcode.repository.OutBoxRepository;
import org.ourcode.service.outbox.OutBoxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
        "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer"
})
class CdcFlowIntegrationTest {

    @Container
    static ConfluentKafkaContainer kafka = new ConfluentKafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withCopyFileToContainer(MountableFile.forClasspathResource("db/migration/V1__postgres_debezium_init_script.sql"),
                    "/docker-entrypoint-initdb.d/init.sql");

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private OutBoxRepository outBoxRepository;

    @Autowired
    private OutBoxService outBoxService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testCdcEventProcessing() throws Exception {
        ObjectNode afterNode = objectMapper.createObjectNode();
        afterNode.put("eventid", "cdc-test-event");
        afterNode.put("deviceid", "cdc-test-device");
        afterNode.put("timestamp", System.currentTimeMillis());
        afterNode.put("type", "temperature");
        afterNode.put("payload", "{\"value\":42}");
        afterNode.put("is_processed", false);

        ObjectNode sourceNode = objectMapper.createObjectNode();
        sourceNode.put("version", "2.5.4.Final");
        sourceNode.put("connector", "postgresql");
        sourceNode.put("name", "ourcode");
        sourceNode.put("ts_ms", System.currentTimeMillis());
        sourceNode.put("snapshot", "false");
        sourceNode.put("db", "ourcode");
        sourceNode.put("sequence", "[\"27567448\",\"27567504\"]");
        sourceNode.put("schema", "public");
        sourceNode.put("table", "outbox_events");
        sourceNode.put("txId", 855);
        sourceNode.put("lsn", 27567504);
        sourceNode.putNull("xmin");

        ObjectNode cdcMessage = objectMapper.createObjectNode();
        cdcMessage.putNull("before");
        cdcMessage.set("after", afterNode);
        cdcMessage.set("source", sourceNode);
        cdcMessage.put("op", "c");
        cdcMessage.put("ts_ms", System.currentTimeMillis());
        cdcMessage.putNull("transaction");

        OutBoxDto outBoxDto = objectMapper.readValue(afterNode.toString(), OutBoxDto.class);

        OutBoxEntity outBoxEntity = new OutBoxEntity();
        outBoxEntity.setEventId(outBoxDto.getEventId());
        outBoxEntity.setProcessed(outBoxDto.isProcessed());
        outBoxEntity.setDeviceId(outBoxDto.getDeviceId());
        outBoxEntity.setTimestamp(outBoxDto.getTimestamp());
        outBoxEntity.setPayload(outBoxDto.getPayload());
        outBoxEntity.setType(outBoxDto.getType());
        outBoxEntity.setTimestamp(outBoxDto.getTimestamp());

        outBoxRepository.save(outBoxEntity);

        // ⚠️ Сериализуем правильно через writeValueAsString
        kafkaTemplate.send(
                "ourcode.public.outbox_events",
                objectMapper.writeValueAsString(cdcMessage)
        );

        // Assert - проверяем, что CDC обработчик отметил событие как обработанное
        await().atMost(30, TimeUnit.SECONDS).until(() ->
                outBoxService.findAllProcessed().stream()
                        .anyMatch(event -> event.getDeviceId().equals("cdc-test-device"))
        );

        assertTrue(outBoxService.findAllProcessed().stream()
                .anyMatch(event -> event.getDeviceId().equals("cdc-test-device")));
    }
}