package org.ourcode.service;

import avro.DeviceEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ourcode.model.EventEntity;
import org.ourcode.model.OutBoxEntity;
import org.ourcode.service.event.EventService;
import org.ourcode.service.kafka.consumer.EventConsumer;
import org.ourcode.service.outbox.OutBoxService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventConsumerTest {

    @Mock
    private EventService eventService;

    @Mock
    private OutBoxService outBoxService;

    @InjectMocks
    private EventConsumer eventConsumer;

    @Test
    void testHandleBatch() {
        List<ConsumerRecord<String, DeviceEvent>> records = Arrays.asList(
                createConsumerRecord("event-1", "device-1"),
                createConsumerRecord("event-2", "device-2")
        );

        when(eventService.saveAll(anyList())).thenReturn(Arrays.asList(new EventEntity(), new EventEntity()));
        when(outBoxService.saveAll(anyList())).thenReturn(Arrays.asList(new OutBoxEntity(), new OutBoxEntity()));

        eventConsumer.handleBatch(records);

        verify(eventService, times(1)).saveAll(anyList());
        verify(outBoxService, times(1)).saveAll(anyList());
    }

    private ConsumerRecord<String, DeviceEvent> createConsumerRecord(String eventId, String deviceId) {
        DeviceEvent deviceEvent = DeviceEvent.newBuilder()
                .setEventId(eventId)
                .setDeviceId(deviceId)
                .setTimestamp(System.currentTimeMillis())
                .setType("temperature")
                .setPayload("{\"value\": 25}")
                .build();

        return new ConsumerRecord<>("events.in", 0, 0, deviceId, deviceEvent);
    }
}
