package org.ourcode.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ourcode.model.OutBoxEntity;
import org.ourcode.service.kafka.producer.DeviceIdProducer;
import org.ourcode.service.outbox.OutBoxService;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceIdProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private OutBoxService outBoxService;

    @InjectMocks
    private DeviceIdProducer deviceIdProducer;

    @Test
    void testSendProcessedDeviceIds() {
        List<OutBoxEntity> processedEntities = Arrays.asList(
                createOutBoxEntity("event-1", "device-1", true),
                createOutBoxEntity("event-2", "device-1", true), // duplicate device
                createOutBoxEntity("event-3", "device-2", true)
        );

        when(outBoxService.findAllProcessed()).thenReturn(processedEntities);

        deviceIdProducer.sendProcessedDeviceIds();

        // Verify that send was called for each unique device ID
        verify(kafkaTemplate, times(1)).send(eq("device-ids"), eq("device-1"));
        verify(kafkaTemplate, times(1)).send(eq("device-ids"), eq("device-2"));
        verify(kafkaTemplate, times(2)).send(eq("device-ids"), anyString());
    }

    private OutBoxEntity createOutBoxEntity(String eventId, String deviceId, boolean processed) {
        OutBoxEntity entity = new OutBoxEntity();
        entity.setEventId(eventId);
        entity.setDeviceId(deviceId);
        entity.setTimestamp(System.currentTimeMillis());
        entity.setType("temperature");
        entity.setPayload("{\"value\": 25}");
        entity.setProcessed(processed);
        return entity;
    }
}
