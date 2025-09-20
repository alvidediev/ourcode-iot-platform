package com.ourcode.devicecollector.integrationTests;

import avro.DeviceCollector;
import com.ourcode.devicecollector.integrationTests.config.BaseIntegrationTestClass;
import com.ourcode.devicecollector.model.entity.DeviceCollectorEntity;
import com.ourcode.devicecollector.service.deviceCollector.DeviceCollectorService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ShardingIT extends BaseIntegrationTestClass {

    @Autowired
    private DeviceCollectorService deviceService;

    @Autowired
    private KafkaTemplate<String, DeviceCollector> kafkaTemplate;

    @Test
    void testMessageGoesToDLTOnFailure() {
        DeviceCollector invalidDevice = new DeviceCollector();
        invalidDevice.setDeviceId("bad-device");
        invalidDevice.setDeviceType("bad");
        invalidDevice.setCreatedAt(System.currentTimeMillis());
        invalidDevice.setMeta("{\"fail\":true}");

        kafkaTemplate.send("device-id-topic", invalidDevice.getDeviceId(), invalidDevice);

        KafkaConsumer<String, String> consumer = createDLTConsumer();
        consumer.subscribe(Collections.singletonList("device-dlt-topic"));

        await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
            ConsumerRecord<String, String> record =
                    KafkaTestUtils.getSingleRecord(consumer, "device-dlt-topic");
            assertTrue(record.value().contains("bad-device"));
        });
    }

    @Test
    void testDeviceSharding() {
        DeviceCollector device1 = createDevice("device-1", "type-a");
        DeviceCollector device2 = createDevice("device-2", "type-b");
        DeviceCollector device3 = createDevice("device-3", "type-c");
        DeviceCollector device4 = createDevice("device-4", "type-d");
        DeviceCollector device5 = createDevice("device-5", "type-e");

        // Save devices
        deviceService.saveDevice(device1);
        deviceService.saveDevice(device2);
        deviceService.saveDevice(device3);
        deviceService.saveDevice(device4);
        deviceService.saveDevice(device5);

        DeviceCollectorEntity deviceEntity1 = deviceService.findByDeviceId("device-1");
        DeviceCollectorEntity deviceEntity2 = deviceService.findByDeviceId("device-2");
        DeviceCollectorEntity deviceEntity3 = deviceService.findByDeviceId("device-3");
        DeviceCollectorEntity deviceEntity4 = deviceService.findByDeviceId("device-4");
        DeviceCollectorEntity deviceEntity5 = deviceService.findByDeviceId("device-5");


        assertEquals("device-1", deviceEntity1.getDeviceId());
        assertEquals("device-2", deviceEntity2.getDeviceId());
        assertEquals("device-3", deviceEntity3.getDeviceId());
        assertEquals("device-4", deviceEntity4.getDeviceId());
        assertEquals("device-5", deviceEntity5.getDeviceId());
    }

    @Test
    void testDeviceShardingDistributionIdempotent() {
        DeviceCollector device1 = createDevice("device-1", "type-a");
        DeviceCollector device2 = createDevice("device-1", "type-b");
        DeviceCollector device3 = createDevice("device-1", "type-c");

        deviceService.saveDevice(device1);
        deviceService.saveDevice(device2);
        deviceService.saveDevice(device3);

        DeviceCollectorEntity byDeviceId = deviceService.findByDeviceId("device-1");

        assertEquals("device-1", byDeviceId.getDeviceId());
        assertEquals("type-c", byDeviceId.getDeviceType());

    }

    private KafkaConsumer<String, String> createDLTConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafka.getBootstrapServers());
        props.put("group.id", "dlt-test-group");
        props.put("auto.offset.reset", "earliest");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        return new KafkaConsumer<>(props);
    }

    private DeviceCollector createDevice(String deviceId, String deviceType) {
        DeviceCollector device = new DeviceCollector();
        device.setDeviceId(deviceId);
        device.setDeviceType(deviceType);
        device.setCreatedAt(System.currentTimeMillis());
        device.setMeta("{\"test\": \"data\"}");
        return device;
    }
}
