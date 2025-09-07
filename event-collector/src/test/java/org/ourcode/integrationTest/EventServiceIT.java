package org.ourcode.integrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ourcode.config.TestContainersConfiguration;
import org.ourcode.model.EventEntity;
import org.ourcode.repository.EventRepository;
import org.ourcode.service.event.impl.EventServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.cassandra.DataCassandraTest;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataCassandraTest
class EventServiceIT extends TestContainersConfiguration {

    @Autowired
    private CassandraTemplate cassandraTemplate;

    @Autowired
    private EventRepository eventRepository;

    private EventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventServiceImpl(cassandraTemplate, eventRepository);
        cassandraTemplate.truncate(EventEntity.class);
    }

    @Test
    void testSaveAll_WithSmallBatch() {
        List<EventEntity> events = Arrays.asList(
                createEvent("event-1", "device-1"),
                createEvent("event-2", "device-2"),
                createEvent("event-3", "device-3")
        );

        List<EventEntity> savedEvents = eventService.saveAll(events);

        assertNotNull(savedEvents);
        assertEquals(3, savedEvents.size());

        List<EventEntity> allEvents = cassandraTemplate.select("SELECT * FROM device_event", EventEntity.class);
        assertEquals(3, allEvents.size());
    }

    @Test
    void testSaveAll_WithLargeBatch() {
        List<EventEntity> events = Arrays.asList(
                createEvent("event-1", "device-1"),
                createEvent("event-2", "device-2"),
                createEvent("event-3", "device-3"),
                createEvent("event-4", "device-4"),
                createEvent("event-5", "device-5"),
                createEvent("event-6", "device-6"),
                createEvent("event-7", "device-7"),
                createEvent("event-8", "device-8"),
                createEvent("event-9", "device-9"),
                createEvent("event-10", "device-10"),
                createEvent("event-11", "device-11"),
                createEvent("event-12", "device-12"),
                createEvent("event-13", "device-13"),
                createEvent("event-14", "device-14"),
                createEvent("event-15", "device-15"),
                createEvent("event-16", "device-16"),
                createEvent("event-17", "device-17"),
                createEvent("event-18", "device-18"),
                createEvent("event-19", "device-19"),
                createEvent("event-20", "device-20"),
                createEvent("event-21", "device-21"),
                createEvent("event-22", "device-22")
        );

        List<EventEntity> savedEvents = eventService.saveAll(events);

        assertEquals(22, savedEvents.size());

        List<EventEntity> allEvents = cassandraTemplate.select("SELECT * FROM device_event", EventEntity.class);
        assertEquals(22, allEvents.size());
    }

    @Test
    void testSaveAll_EmptyList() {
        List<EventEntity> events = List.of();

        List<EventEntity> savedEvents = eventService.saveAll(events);

        assertNotNull(savedEvents);
        assertEquals(0, savedEvents.size());

        List<EventEntity> allEvents = cassandraTemplate.select("SELECT * FROM device_event", EventEntity.class);
        assertEquals(0, allEvents.size());
    }

    @Test
    void testSaveAll_WithDuplicateEventIds() {
        List<EventEntity> events = Arrays.asList(
                createEvent("same-id", "device-1"),
                createEvent("same-id", "device-2")
        );

        List<EventEntity> savedEvents = eventService.saveAll(events);

        assertEquals(2, savedEvents.size());

        List<EventEntity> allEvents = cassandraTemplate.select("SELECT * FROM device_event", EventEntity.class);
        assertEquals(1, allEvents.size());
        assertEquals("device-2", allEvents.get(0).getDeviceId());
    }

    @Test
    void testSaveAll_WithDifferentDevices() {
        List<EventEntity> events = Arrays.asList(
                createEvent("event-1", "device-1"),
                createEvent("event-2", "device-1"),
                createEvent("event-3", "device-2"),
                createEvent("event-4", "device-2")
        );

        List<EventEntity> savedEvents = eventService.saveAll(events);

        assertEquals(4, savedEvents.size());

        List<EventEntity> device1Events = cassandraTemplate.select(
                "SELECT * FROM device_event WHERE deviceId = 'device-1' ALLOW FILTERING",
                EventEntity.class);
        assertEquals(2, device1Events.size());

        List<EventEntity> device2Events = cassandraTemplate.select(
                "SELECT * FROM device_event WHERE deviceId = 'device-2' ALLOW FILTERING",
                EventEntity.class);
        assertEquals(2, device2Events.size());
    }

    private EventEntity createEvent(String eventId, String deviceId) {
        EventEntity event = new EventEntity();
        event.setEventId(eventId);
        event.setDeviceId(deviceId);
        event.setTimestamp(System.currentTimeMillis());
        event.setType("temperature");
        event.setPayload("{\"value\":" + (Math.random() * 100) + "}");
        return event;
    }
}
