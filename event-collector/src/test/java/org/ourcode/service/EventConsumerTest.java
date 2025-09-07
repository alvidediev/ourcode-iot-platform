package org.ourcode.service;

import avro.DeviceEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ourcode.mapper.EventMapper;
import org.ourcode.model.EventEntity;
import org.ourcode.service.event.EventService;
import org.ourcode.service.kafka.consumer.EventConsumer;
import org.ourcode.service.kafka.producer.DeviceIdProducer;
import org.springframework.kafka.support.Acknowledgment;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventConsumerTest {

    @Mock
    private EventService eventService;

    @Mock
    private DeviceIdProducer deviceIdProducer;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private Acknowledgment ack;

    @InjectMocks
    private EventConsumer eventConsumer;

    @Test
    void testHandleBatch_Success() {
        List<ConsumerRecord<String, DeviceEvent>> records = Arrays.asList(
                createConsumerRecord("event-1", "device-1"),
                createConsumerRecord("event-2", "device-2")
        );

        EventEntity entity1 = createEventEntity("event-1", "device-1");
        EventEntity entity2 = createEventEntity("event-2", "device-2");

        when(eventMapper.deviceEventToEntity(any(DeviceEvent.class)))
                .thenReturn(entity1)
                .thenReturn(entity2);
        when(eventService.saveAll(anyList())).thenReturn(Arrays.asList(entity1, entity2));

        eventConsumer.handleBatch(records, ack);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(eventMapper, times(2)).deviceEventToEntity(any(DeviceEvent.class));
            verify(eventService, times(1)).saveAll(anyList());
            verify(ack, times(1)).acknowledge();
            verify(deviceIdProducer, times(1)).sendProcessedDeviceIds(Set.of("device-1", "device-2"));
        });
    }

    @Test
    void testHandleBatch_Error() {
        List<ConsumerRecord<String, DeviceEvent>> records = Arrays.asList(
                createConsumerRecord("event-1", "device-1")
        );

        EventEntity entity = createEventEntity("event-1", "device-1");

        when(eventMapper.deviceEventToEntity(any(DeviceEvent.class))).thenReturn(entity);
        when(eventService.saveAll(anyList())).thenThrow(new RuntimeException("Database error"));

        eventConsumer.handleBatch(records, ack);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(eventMapper, times(1)).deviceEventToEntity(any(DeviceEvent.class));
            verify(eventService, times(1)).saveAll(anyList());
            verify(ack, never()).acknowledge(); // ack не должен вызываться при ошибке
            verify(deviceIdProducer, never()).sendProcessedDeviceIds(anySet());
        });
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

    private EventEntity createEventEntity(String eventId, String deviceId) {
        EventEntity event = new EventEntity();
        event.setEventId(eventId);
        event.setDeviceId(deviceId);
        event.setTimestamp(System.currentTimeMillis());
        event.setType("temperature");
        event.setPayload("{\"value\": 25}");
        return event;
    }
}
