package com.ourcode.devicecollector.unitTests;

import avro.DeviceCollector;
import com.ourcode.devicecollector.service.deviceCollector.DeviceCollectorService;
import com.ourcode.devicecollector.service.kafka.consumer.DeviceCollectorKafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceCollectorKafkaConsumerTest {

    @Mock
    private DeviceCollectorService deviceService;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private DeviceCollectorKafkaConsumer consumer;

    @Test
    void testConsumeBatchSuccess() {
        DeviceCollector device = createTestDevice();
        ConsumerRecord<String, DeviceCollector> record =
                new ConsumerRecord<>("device-id-topic", 0, 0, "device-1", device);

        List<ConsumerRecord<String, DeviceCollector>> records = List.of(record);

        consumer.consumeBatch(records, acknowledgment);

        verify(deviceService, times(1)).saveDevice(device);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    void testConsumeBatchWithException() {
        DeviceCollector device = createTestDevice();
        ConsumerRecord<String, DeviceCollector> record =
                new ConsumerRecord<>("device-id-topic", 0, 0, "device-1", device);

        List<ConsumerRecord<String, DeviceCollector>> records = List.of(record);

        doThrow(new RuntimeException("Test error")).when(deviceService).saveDevice(device);

        consumer.consumeBatch(records, acknowledgment);

        verify(deviceService, times(1)).saveDevice(device);
        verify(acknowledgment, times(1)).acknowledge();
    }

    private DeviceCollector createTestDevice() {
        DeviceCollector device = new DeviceCollector();
        device.setDeviceId("device-1");
        device.setDeviceType("type-1");
        device.setCreatedAt(System.currentTimeMillis());
        device.setMeta("{\"value\": 42}");
        return device;
    }
}
