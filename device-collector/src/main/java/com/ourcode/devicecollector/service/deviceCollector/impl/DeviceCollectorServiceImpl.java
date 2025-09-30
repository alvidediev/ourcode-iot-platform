package com.ourcode.devicecollector.service.deviceCollector.impl;

import avro.DeviceCollector;
import com.ourcode.devicecollector.exception.DeviceCollectorException;
import com.ourcode.devicecollector.model.entity.DeviceCollectorEntity;
import com.ourcode.devicecollector.repository.DeviceCollectorRepository;
import com.ourcode.devicecollector.service.metrics.DeviceCollectorMetrics;
import com.ourcode.devicecollector.service.deviceCollector.DeviceCollectorService;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceCollectorServiceImpl implements DeviceCollectorService {
    private final DeviceCollectorRepository deviceCollectorRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final DeviceCollectorMetrics deviceCollectorMetrics;


    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 5000))
    @Transactional
    @Override
    public void saveDevice(DeviceCollector deviceCollector) {
        Timer.Sample timer = deviceCollectorMetrics.startProcessingTimer();
        try {
            deviceCollectorRepository.upsertDevice(
                    deviceCollector.getDeviceId(),
                    deviceCollector.getDeviceType(),
                    deviceCollector.getCreatedAt(),
                    deviceCollector.getMeta());

            int shardIndex = getShardIndex(deviceCollector.getDeviceId());
            deviceCollectorMetrics.recordShardOperation("shard" + shardIndex, true);
        } catch (Exception e) {
            deviceCollectorMetrics.recordFailure();
            int shardIndex = getShardIndex(deviceCollector.getDeviceId());
            deviceCollectorMetrics.recordShardOperation("shard" + shardIndex, false);
            throw new DeviceCollectorException("Failed to process device after retries", e);
        } finally {
            deviceCollectorMetrics.stopProcessingTimer(timer);
        }
    }


    @Override
    public void sendToDlt(String originalMessage, String errorMessage) {
        try {
            kafkaTemplate.send("device-dlt-topic", originalMessage + " | Error: " + errorMessage);
            deviceCollectorMetrics.recordDltMessage();
        } catch (Exception e) {
            log.error("Error while trying send to dlt: {}", e.getMessage());
            deviceCollectorMetrics.recordFailure();
        }
    }

    @Override
    public DeviceCollectorEntity findByDeviceId(String deviceId) {
        int shardId = getShardIndex(deviceId);
        Timer.Sample sample = deviceCollectorMetrics.startProcessingTimer();

        try {
            deviceCollectorMetrics.recordSuccess();
            return deviceCollectorRepository.findByDeviceId(deviceId);
        } catch (Exception e) {
            log.error("Failed to find device {} on shard {}: {}", deviceId, shardId, e.getMessage(), e);
            deviceCollectorMetrics.recordFailure();
            throw e;
        } finally {
            deviceCollectorMetrics.stopProcessingTimer(sample);
        }
    }

    private static int getShardIndex(String deviceId) {
        return Math.abs(deviceId.hashCode()) % 2;
    }
}
