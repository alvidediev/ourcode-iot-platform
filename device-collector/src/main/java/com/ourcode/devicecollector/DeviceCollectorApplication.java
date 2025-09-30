package com.ourcode.devicecollector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude = {FlywayAutoConfiguration.class })
public class DeviceCollectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeviceCollectorApplication.class, args);
	}

}
