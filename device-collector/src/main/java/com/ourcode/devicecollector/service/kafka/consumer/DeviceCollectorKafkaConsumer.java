package com.ourcode.devicecollector.service.kafka.consumer;

import avro.DeviceCollector;
import com.ourcode.devicecollector.exception.DeviceCollectorException;
import com.ourcode.devicecollector.service.metrics.DeviceCollectorMetrics;
import com.ourcode.devicecollector.service.deviceCollector.DeviceCollectorService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceCollectorKafkaConsumer {

    private final DeviceCollectorService deviceService;
    private final KafkaTemplate<String, DeviceCollector> kafkaTemplate;
    private final DeviceCollectorMetrics deviceCollectorMetrics;

    @KafkaListener(topics = "device-id-topic", groupId = "device-group", containerFactory = "batchListenerFactory")
    public void consumeBatch(List<ConsumerRecord<String, DeviceCollector>> records,
                             Acknowledgment ack) {
        for (ConsumerRecord<String, DeviceCollector> record : records) {
            try {
                DeviceCollector device = record.value();
                if(!device.getDeviceId().startsWith("device-")) {
                    throw new DeviceCollectorException("Now valid device-id", new Throwable("Device ID is invalid"));
                }
                deviceService.saveDevice(device);
            } catch (Exception e) {
                deviceService.sendToDlt(record.value().toString(), e.getMessage());
            }
        }
        ack.acknowledge();
    }
}
