package org.ourcode.service.imitation.impl;

import avro.DeviceEvent;
import lombok.RequiredArgsConstructor;
import org.ourcode.service.imitation.ImitationService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class ImitationServiceImpl implements ImitationService {

    private final KafkaTemplate<String, DeviceEvent> kafkaTemplate;

    @Override
    public void startImitation() {
        for (int i = 0; i < 1000; i++) {
            DeviceEvent event = new DeviceEvent();
            event.setEventId("event-" + i);
            event.setDeviceId("device-" + (i % 100));
            event.setTimestamp(System.currentTimeMillis());
            event.setType("temp");
            event.setPayload("{\"value\":" + new Random().nextInt(100) + "}");

            kafkaTemplate.send("events.in", event.getDeviceId(), event);
        }
    }
}
