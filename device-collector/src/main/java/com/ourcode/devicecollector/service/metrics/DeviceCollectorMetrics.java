package com.ourcode.devicecollector.service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class DeviceCollectorMetrics {

    private final MeterRegistry meterRegistry;
    private final Counter successfulMessagesCounter;
    private final Counter failedMessagesCounter;
    private final Counter dltMessagesCounter;
    private final Timer processingTimer;
    private final ConcurrentHashMap<String, Counter> shardCounters;

    public DeviceCollectorMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.successfulMessagesCounter = Counter.builder("device.collector.messages.success")
                .description("Number of successfully processed messages")
                .register(meterRegistry);

        this.failedMessagesCounter = Counter.builder("device.collector.messages.failed")
                .description("Number of failed messages")
                .register(meterRegistry);

        this.dltMessagesCounter = Counter.builder("device.collector.messages.dlt")
                .description("Number of messages sent to DLT")
                .register(meterRegistry);

        this.processingTimer = Timer.builder("device.collector.processing.time")
                .description("Time taken to process messages")
                .register(meterRegistry);

        this.shardCounters = new ConcurrentHashMap<>();
    }

    public void recordSuccess() {
        successfulMessagesCounter.increment();
    }

    public void recordFailure() {
        failedMessagesCounter.increment();
    }

    public void recordDltMessage() {
        dltMessagesCounter.increment();
    }

    public void recordShardOperation(String shardName, boolean success) {
        String status = success ? "success" : "failure";
        String metricName = "device.collector.shard.operations." + status;

        shardCounters.computeIfAbsent(metricName + "." + shardName, key ->
                Counter.builder(metricName)
                        .tag("shard", shardName)
                        .description("Shard operations by status")
                        .register(meterRegistry)
        ).increment();
    }

    public Timer.Sample startProcessingTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopProcessingTimer(Timer.Sample sample) {
        sample.stop(processingTimer);
    }
}
