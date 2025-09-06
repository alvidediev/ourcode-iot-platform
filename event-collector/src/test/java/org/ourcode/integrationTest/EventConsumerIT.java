package org.ourcode.integrationTest;

import avro.DeviceEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ourcode.config.TestContainersConfiguration;
import org.ourcode.model.EventEntity;
import org.ourcode.service.event.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class EventConsumerIT extends TestContainersConfiguration {

    @Autowired
    private KafkaTemplate<String, DeviceEvent> kafkaTemplate;

    @Autowired
    private EventService eventService;

    @Autowired
    private CassandraOperations cassandraOperations;

    @BeforeEach
    void setUp() {
        cassandraOperations.truncate(EventEntity.class);
    }

    @Test
    void testSingleEventConsumptionAndProcessing() {
        DeviceEvent event = DeviceEvent.newBuilder()
                .setEventId("test-event-1")
                .setDeviceId("test-device-1")
                .setTimestamp(System.currentTimeMillis())
                .setType("temperature")
                .setPayload("{\"value\":25}")
                .build();

        kafkaTemplate.send(new ProducerRecord<>("events.in", event.getDeviceId(), event));

        await().atMost(30, TimeUnit.SECONDS).until(() -> {
            EventEntity savedEvent = eventService.findByEventId(event.getEventId());
            return savedEvent != null &&
                    savedEvent.getDeviceId().equals(event.getDeviceId()) &&
                    savedEvent.getPayload().equals(event.getPayload());
        });

        EventEntity savedEvent = eventService.findByEventId(event.getEventId());
        assertNotNull(savedEvent, "Event should be saved to Cassandra");
        assertEquals(event.getDeviceId(), savedEvent.getDeviceId());
        assertEquals(event.getPayload(), savedEvent.getPayload());
        assertEquals(event.getTimestamp(), savedEvent.getTimestamp());
    }

    @Test
    void testBatchEventConsumption() {
        for (int i = 1; i <= 5; i++) {
            DeviceEvent event = DeviceEvent.newBuilder()
                    .setEventId("batch-event-" + i)
                    .setDeviceId("batch-device-" + (i % 2 + 1))
                    .setTimestamp(System.currentTimeMillis() + i)
                    .setType("temperature")
                    .setPayload("{\"value\":" + (20 + i) + "}")
                    .build();

            kafkaTemplate.send(new ProducerRecord<>("events.in", event.getDeviceId(), event));
        }

        await().atMost(40, TimeUnit.SECONDS).until(() -> {
            List<EventEntity> allEvents = cassandraOperations.select("SELECT * FROM device_event", EventEntity.class);
            return allEvents.size() == 5;
        });

        List<EventEntity> allEvents = cassandraOperations.select("SELECT * FROM device_event", EventEntity.class);
        assertEquals(5, allEvents.size(), "All 5 events should be processed");
    }

    @Test
    void testEventWithDifferentDeviceIds() {
        DeviceEvent event1 = DeviceEvent.newBuilder()
                .setEventId("event-device-1")
                .setDeviceId("device-alpha")
                .setTimestamp(System.currentTimeMillis())
                .setType("humidity")
                .setPayload("{\"value\":60}")
                .build();

        DeviceEvent event2 = DeviceEvent.newBuilder()
                .setEventId("event-device-2")
                .setDeviceId("device-beta")
                .setTimestamp(System.currentTimeMillis())
                .setType("pressure")
                .setPayload("{\"value\":1013}")
                .build();

        kafkaTemplate.send(new ProducerRecord<>("events.in", event1.getDeviceId(), event1));
        kafkaTemplate.send(new ProducerRecord<>("events.in", event2.getDeviceId(), event2));


        await().atMost(30, TimeUnit.SECONDS).until(() -> {
            EventEntity saved1 = eventService.findByEventId(event1.getEventId());
            EventEntity saved2 = eventService.findByEventId(event2.getEventId());
            return saved1 != null && saved2 != null;
        });

        EventEntity saved1 = eventService.findByEventId(event1.getEventId());
        EventEntity saved2 = eventService.findByEventId(event2.getEventId());

        assertEquals("device-alpha", saved1.getDeviceId());
        assertEquals("device-beta", saved2.getDeviceId());
        assertEquals("humidity", saved1.getType());
        assertEquals("pressure", saved2.getType());
    }

    @Test
    void testEventWithLargePayload() {
        String largePayload = "{\"value\":25,\"sensors\":[" +
                "\"temp1\",\"temp2\",\"temp3\",\"humidity1\",\"pressure1\"," +
                "\"voltage\",\"current\",\"power\",\"frequency\",\"status\"" +
                "],\"metadata\":{\"version\":\"1.0\",\"timestamp\":\"2024-01-01T00:00:00Z\"}}";

        DeviceEvent event = DeviceEvent.newBuilder()
                .setEventId("large-payload-event")
                .setDeviceId("industrial-device-1")
                .setTimestamp(System.currentTimeMillis())
                .setType("complex_measurement")
                .setPayload(largePayload)
                .build();

        kafkaTemplate.send(new ProducerRecord<>("events.in", event.getDeviceId(), event));

        await().atMost(30, TimeUnit.SECONDS).until(() -> {
            EventEntity savedEvent = eventService.findByEventId(event.getEventId());
            return savedEvent != null && savedEvent.getPayload().equals(largePayload);
        });

        EventEntity savedEvent = eventService.findByEventId(event.getEventId());
        assertEquals(largePayload, savedEvent.getPayload());
        assertEquals("industrial-device-1", savedEvent.getDeviceId());
    }

    @Test
    void testEventOrderingByTimestamp() {
        long baseTime = System.currentTimeMillis();

        DeviceEvent event1 = DeviceEvent.newBuilder()
                .setEventId("order-event-1")
                .setDeviceId("ordered-device")
                .setTimestamp(baseTime + 1000)
                .setType("temperature")
                .setPayload("{\"value\":30}")
                .build();

        DeviceEvent event2 = DeviceEvent.newBuilder()
                .setEventId("order-event-2")
                .setDeviceId("ordered-device")
                .setTimestamp(baseTime)
                .setType("temperature")
                .setPayload("{\"value\":25}")
                .build();

        kafkaTemplate.send(new ProducerRecord<>("events.in", event1.getDeviceId(), event1));
        kafkaTemplate.send(new ProducerRecord<>("events.in", event2.getDeviceId(), event2));

        await().atMost(30, TimeUnit.SECONDS).until(() -> {
            EventEntity saved1 = eventService.findByEventId(event1.getEventId());
            EventEntity saved2 = eventService.findByEventId(event2.getEventId());
            return saved1 != null && saved2 != null;
        });

        EventEntity saved1 = eventService.findByEventId(event1.getEventId());
        EventEntity saved2 = eventService.findByEventId(event2.getEventId());

        assertEquals(baseTime + 1000, saved1.getTimestamp());
        assertEquals(baseTime, saved2.getTimestamp());
    }
}