package com.ourcode.devicecollector.unitTests;

import avro.DeviceCollector;
import com.ourcode.devicecollector.exception.DeviceCollectorException;
import com.ourcode.devicecollector.repository.DeviceCollectorRepository;
import com.ourcode.devicecollector.service.deviceCollector.impl.DeviceCollectorServiceImpl;
import com.ourcode.devicecollector.service.metrics.DeviceCollectorMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@EnableRetry
class DeviceCollectorServiceImplTest {
    @Mock
    private DeviceCollectorMetrics deviceCollectorMetrics;

    @Mock
    private DeviceCollectorRepository repository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private DeviceCollectorServiceImpl service;


    @Test
    void testSaveDevice_Success() {
        DeviceCollector device = createTestDevice();
        doNothing().when(repository).upsertDevice(anyString(), anyString(), anyLong(), anyString());

        service.saveDevice(device);

        verify(repository, times(1)).upsertDevice(
                eq("device-1"), eq("type-1"), anyLong(), eq("{\"value\": 42}")
        );
    }

    @Test
    void testSaveDevice_Idempotent() {
        DeviceCollector device = createTestDevice();
        doNothing().when(repository).upsertDevice(anyString(), anyString(), anyLong(), anyString());

        service.saveDevice(device);
        service.saveDevice(device);
        service.saveDevice(device);

        verify(repository, times(3)).upsertDevice(
                eq("device-1"), eq("type-1"), anyLong(), eq("{\"value\": 42}")
        );
    }

    @Test
    void testSaveDevice_MaxRetriesExceeded() {
        DeviceCollector device = createTestDevice();
        doThrow(new RuntimeException("Permanent DB error"))
                .when(repository).upsertDevice(anyString(), anyString(), anyLong(), anyString());

        assertThrows(DeviceCollectorException.class, () -> {
            service.saveDevice(device);
        });
    }

    @Test
    void testSendToDlt() {
        String originalMessage = "test message";
        String errorMessage = "test error";

        service.sendToDlt(originalMessage, errorMessage);

        verify(kafkaTemplate, times(1)).send(
                eq("device-dlt-topic"),
                eq("test message | Error: test error")
        );
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
