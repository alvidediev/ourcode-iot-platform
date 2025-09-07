package org.ourcode.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ourcode.model.EventEntity;
import org.ourcode.service.event.impl.EventServiceImpl;
import org.springframework.data.cassandra.core.CassandraBatchOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private CassandraTemplate cassandraTemplate;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void testSaveAll_EmptyList() {
        List<EventEntity> result = eventService.saveAll(Collections.emptyList());

        assertTrue(result.isEmpty());
        verifyNoInteractions(cassandraTemplate);
    }

    @Test
    void testSaveAll_SmallBatch() {
        List<EventEntity> events = Arrays.asList(
                createEvent("event-1", "device-1"),
                createEvent("event-2", "device-2")
        );

        when(cassandraTemplate.batchOps()).thenReturn(mock(CassandraBatchOperations.class));

        List<EventEntity> result = eventService.saveAll(events);

        assertEquals(2, result.size());
        verify(cassandraTemplate, times(1)).batchOps();
    }

    @Test
    void testSaveAll_LargeBatch() {
        List<EventEntity> events = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            events.add(createEvent("event-" + i, "device-" + (i % 10)));
        }

        when(cassandraTemplate.batchOps()).thenReturn(mock(CassandraBatchOperations.class));

        List<EventEntity> result = eventService.saveAll(events);

        assertEquals(45, result.size());
        verify(cassandraTemplate, times(3)).batchOps();
    }

    @Test
    void testSaveAll_WithBatchOperationsMock() {
        List<EventEntity> events = Arrays.asList(
                createEvent("event-1", "device-1"),
                createEvent("event-2", "device-2")
        );

        CassandraBatchOperations batchOps = mock(CassandraBatchOperations.class);
        when(cassandraTemplate.batchOps()).thenReturn(batchOps);
        when(batchOps.execute()).thenReturn(null);

        List<EventEntity> result = eventService.saveAll(events);

        assertEquals(2, result.size());
        verify(batchOps, times(2)).insert(any(EventEntity.class));
        verify(batchOps, times(1)).execute();
    }

    private EventEntity createEvent(String eventId, String deviceId) {
        EventEntity event = new EventEntity();
        event.setEventId(eventId);
        event.setDeviceId(deviceId);
        event.setTimestamp(System.currentTimeMillis());
        event.setType("temperature");
        event.setPayload("{\"value\":25}");
        return event;
    }
}
