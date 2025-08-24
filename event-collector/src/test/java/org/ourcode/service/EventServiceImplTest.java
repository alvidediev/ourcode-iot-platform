package org.ourcode.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ourcode.model.EventEntity;
import org.ourcode.repository.EventRepository;
import org.ourcode.service.event.impl.EventServiceImpl;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceImplTest {
    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void testSaveAll() {
        List<EventEntity> events = Arrays.asList(
                createEvent("event-1", "device-1"),
                createEvent("event-2", "device-2")
        );

        when(eventRepository.saveAll(anyList())).thenReturn(events);

        List<EventEntity> result = eventService.saveAll(events);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(eventRepository, times(1)).saveAll(events);
    }

    @Test
    void testFindAll() {
        List<EventEntity> events = Arrays.asList(
                createEvent("event-1", "device-1"),
                createEvent("event-2", "device-2")
        );

        when(eventRepository.findAll()).thenReturn(events);

        List<EventEntity> result = eventService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(eventRepository, times(1)).findAll();
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
