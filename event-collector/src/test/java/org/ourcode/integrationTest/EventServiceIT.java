package org.ourcode.integrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ourcode.model.EventEntity;
import org.ourcode.service.event.impl.EventServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.cassandra.DataCassandraTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataCassandraTest
@Import(EventServiceImpl.class)
@Testcontainers
class EventServiceIT {

    @Container
    static CassandraContainer<?> cassandra = new CassandraContainer<>("cassandra:4.1")
            .withExposedPorts(9042)
            .withInitScript("init.cql");


    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cassandra.contact-points",
                () -> cassandra.getHost() + ":" + cassandra.getMappedPort(9042));
        registry.add("spring.cassandra.local-datacenter", () -> "datacenter1");
        registry.add("spring.cassandra.keyspace-name", () -> "test_keyspace");
        registry.add("spring.cassandra.username", cassandra::getUsername);
        registry.add("spring.cassandra.password", cassandra::getPassword);
        registry.add("spring.cassandra.schema-action", () -> "CREATE_IF_NOT_EXISTS");
    }

    @Autowired
    private EventServiceImpl eventService;

    @Autowired
    private CassandraOperations cassandraTemplate;

    @BeforeEach
    void setUp() {
        cassandraTemplate.truncate(EventEntity.class);
    }

    @Test
    void testSaveAndFindAllEvents() {
        List<EventEntity> events = Arrays.asList(
                createEvent("event-1", "device-1"),
                createEvent("event-2", "device-2")
        );

        List<EventEntity> savedEvents = eventService.saveAll(events);
        List<EventEntity> foundEvents = eventService.findAll();

        assertNotNull(savedEvents);
        assertEquals(2, savedEvents.size());
        assertEquals(2, foundEvents.size());
        assertEquals("event-1", foundEvents.get(0).getEventId());
        assertEquals("device-1", foundEvents.get(0).getDeviceId());
    }

    @Test
    void testSaveLargeBatch() {
        List<EventEntity> events = Arrays.asList(
                createEvent("event-1", "device-1"),
                createEvent("event-2", "device-1"),
                createEvent("event-3", "device-2"),
                createEvent("event-4", "device-3"),
                createEvent("event-5", "device-3")
        );

        List<EventEntity> savedEvents = eventService.saveAll(events);
        List<EventEntity> foundEvents = eventService.findAll();

        assertEquals(5, savedEvents.size());
        assertEquals(5, foundEvents.size());
    }

    private EventEntity createEvent(String eventId, String deviceId) {
        EventEntity event = new EventEntity();
        event.setEventId(eventId);
        event.setDeviceId(deviceId);
        event.setTimestamp(System.currentTimeMillis());
        event.setType("temperature");
        event.setPayload("{\"value\": 25}");
        return event;
    }
}
