package com.ourcode.devicecollector.unitTests;

import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@ExtendWith(MockitoExtension.class)
class ShardingStrategyTest {

    @Test
    void testShardingKeyCalculation() {
        String deviceId1 = "device-123";
        String deviceId2 = "device-456";
        String deviceId3 = "device-123";

        int shardCount = 2;

        int shard1 = Math.abs(deviceId1.hashCode()) % shardCount;
        int shard2 = Math.abs(deviceId2.hashCode()) % shardCount;
        int shard3 = Math.abs(deviceId3.hashCode()) % shardCount;

        assertEquals(shard1, shard3);
        assertNotEquals(shard1, shard2);
    }

    @Test
    void testShardingConfiguration() {
        StandardShardingStrategyConfiguration strategy =
                new StandardShardingStrategyConfiguration("device_id", "deviceid_hash_mod");

        assertEquals("device_id", strategy.getShardingColumn());
        assertEquals("deviceid_hash_mod", strategy.getShardingAlgorithmName());
    }
}
