package org.ourcode.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ourcode.service.kafka.consumer.CdcEventConsumer;
import org.ourcode.service.kafka.producer.DeviceIdProducer;
import org.ourcode.service.outbox.OutBoxService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CdcEventConsumerTest {

    @Mock
    private OutBoxService outBoxService;

    @Mock
    private DeviceIdProducer deviceIdProducer;

    @InjectMocks
    private CdcEventConsumer cdcEventConsumer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testHandleCdcEvents() throws Exception {
        // Arrange
        String cdcMessage = "{\"after\": {\"eventid\": \"event-1\", \"deviceid\": \"device-1\", \"timestamp\": 123456789, \"type\": \"temperature\", \"payload\": \"{}\", \"is_processed\": false}}";

        List<ConsumerRecord<String, String>> records = Arrays.asList(
                new ConsumerRecord<>("ourcode.public.outbox_events", 0, 0, "key", cdcMessage)
        );

        // Act
        cdcEventConsumer.handleCdcEvents(records);

        // Assert
        verify(outBoxService, times(1)).markAsProcessed(anyString());
        verify(deviceIdProducer, times(1)).sendProcessedDeviceIds();
    }

    @Test
    void testHandleCdcEventsWithNullAfter() throws Exception {
        // Arrange
        String cdcMessage = "{\"after\": null}";

        List<ConsumerRecord<String, String>> records = Arrays.asList(
                new ConsumerRecord<>("ourcode.public.outbox_events", 0, 0, "key", cdcMessage)
        );

        // Act
        cdcEventConsumer.handleCdcEvents(records);

        // Assert
        verify(outBoxService, never()).markAsProcessed(anyString());
        verify(deviceIdProducer, times(1)).sendProcessedDeviceIds();
    }
}
