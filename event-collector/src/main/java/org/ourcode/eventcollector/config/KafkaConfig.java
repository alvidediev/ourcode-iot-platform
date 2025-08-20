package org.ourcode.eventcollector.config;

import avro.DeviceEvent;
import avro.DeviceId;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singleton;

@Configuration
@Slf4j
public class KafkaConfig {
    @Bean
    public ReactiveKafkaConsumerTemplate<String, DeviceEvent> reactiveKafkaConsumerTemplate(
            KafkaProperties kafkaProperties
    ) {
        Map<String, Object> consumerProps = new HashMap<>(kafkaProperties.buildConsumerProperties());

        // Явно указываем десериализаторы
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        consumerProps.put("specific.avro.reader", "true");
        consumerProps.put("schema.registry.url", "http://localhost:8081");

        log.info("Kafka consumer properties: {}", consumerProps);

        ReceiverOptions<String, DeviceEvent> receiverOptions =
                ReceiverOptions.<String, DeviceEvent>create(consumerProps)
                        .subscription(singleton("events.in"));

        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @Bean
    public ReactiveKafkaProducerTemplate<String, DeviceId> reactiveKafkaProducerTemplate(
            KafkaProperties kafkaProperties
    ) {
        Map<String, Object> producerProps = new HashMap<>(kafkaProperties.buildProducerProperties());

        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
        producerProps.put("schema.registry.url", "http://localhost:8081");

        SenderOptions<String, DeviceId> senderOptions =
                SenderOptions.create(producerProps);

        return new ReactiveKafkaProducerTemplate<>(senderOptions);
    }
}
