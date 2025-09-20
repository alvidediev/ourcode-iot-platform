package com.ourcode.devicecollector.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FlywayConfig {
    @Value("${sharding.shard0Master.url:jdbc:postgresql://localhost:7001/device_collector_db1_master}")
    private String shar0MasterUrl;
    @Value("${sharding.shard1Master.url:jdbc:postgresql://localhost:7003/device_collector_db2_master}")
    private String shar1MasterUrl;
    @Value("${datasource.username:postgres}")
    private String username;
    @Value("${datasource.password:postgres}")
    private String password;

    @Bean(initMethod = "migrate")
    public Flyway flywayShard0() {
        return Flyway.configure()
                .dataSource(shar0MasterUrl,
                            username, password)
                .schemas("public")
                .table("flyway_schema_history")
                .load();
    }

    @Bean(initMethod = "migrate")
    public Flyway flywayShard1() {
        return Flyway.configure()
                .dataSource(shar1MasterUrl,
                            username, password)
                .schemas("public")
                .table("flyway_schema_history")
                .load();
    }
}
