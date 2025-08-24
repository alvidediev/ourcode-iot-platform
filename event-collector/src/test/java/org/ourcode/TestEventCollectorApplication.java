package org.ourcode;

import org.springframework.boot.SpringApplication;

public class TestEventCollectorApplication {

	public static void main(String[] args) {
		SpringApplication.from(EventCollectorApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
