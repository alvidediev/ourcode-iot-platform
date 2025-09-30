package com.ourcode.devicecollector.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

@Configuration
public class ShardingSphereConfig {

    @Value("${sharding.shard0Master.url:jdbc:postgresql://localhost:7001/device_collector_db1_master}")
    private String shard0MasterUrl;
    @Value("${sharding.shard0Replica.url:jdbc:postgresql://localhost:7002/device_collector_db1_master}")
    private String shard0ReplicaUrl;
    @Value("${sharding.shard1Master.url:jdbc:postgresql://localhost:7003/device_collector_db2_master}")
    private String shard1MasterUrl;
    @Value("${sharding.shard1Replica.url:jdbc:postgresql://localhost:7004/device_collector_db2_master}")
    private String shard1ReplicaUrl;

    @Value("${datasource.username:postgres}")
    private String username;
    @Value("${datasource.password:postgres}")
    private String password;
    @Value("${datasource.driverClassName:org.postgresql.Driver}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("shard0_master", createDataSource(shard0MasterUrl));
        dataSourceMap.put("shard0_replica", createDataSource(shard0ReplicaUrl));
        dataSourceMap.put("shard1_master", createDataSource(shard1MasterUrl));
        dataSourceMap.put("shard1_replica", createDataSource(shard1ReplicaUrl));

        ShardingRuleConfiguration shardingRuleConfig = createShardingRuleConfiguration();
        ReadwriteSplittingRuleConfiguration readwriteSplittingRuleConfig = createReadwriteSplittingRuleConfiguration();

        return ShardingSphereDataSourceFactory.createDataSource(
                dataSourceMap,
                List.of(shardingRuleConfig, readwriteSplittingRuleConfig),
                new Properties()
        );
    }

    private DataSource createDataSource(String url) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        return dataSource;
    }

    private ReadwriteSplittingRuleConfiguration createReadwriteSplittingRuleConfiguration() {

        ReadwriteSplittingDataSourceGroupRuleConfiguration s1 = new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                "shard0",
                "shard0_master",
                Collections.singletonList("shard0_replica"),
                "round_robin"
        );

        ReadwriteSplittingDataSourceGroupRuleConfiguration s2 = new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                "shard1",
                "shard1_master",
                Collections.singletonList("shard1_replica"),
                "round_robin"
        );

        return new ReadwriteSplittingRuleConfiguration(
                List.of(s1,s2),Map.of("round_robin",
                new AlgorithmConfiguration("ROUND_ROBIN", new Properties()))

        );
    }

    private ShardingRuleConfiguration createShardingRuleConfiguration() {
        ShardingRuleConfiguration config = new ShardingRuleConfiguration();

        ShardingTableRuleConfiguration devicesTableConfig = new ShardingTableRuleConfiguration(
                "device_collector",
                "shard${0..1}.device_collector"
        );

        devicesTableConfig.setDatabaseShardingStrategy(
                new StandardShardingStrategyConfiguration("device_id", "deviceid_hash_mod")
        );

        config.getTables().add(devicesTableConfig);

        Properties algorithmProps = new Properties();
        algorithmProps.setProperty("algorithm-expression", "shard${Math.abs(device_id.hashCode()) % 2}");
        config.getShardingAlgorithms().put("deviceid_hash_mod",
                new AlgorithmConfiguration("INLINE", algorithmProps));

        return config;
    }
}
