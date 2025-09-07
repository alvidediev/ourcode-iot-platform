# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/3.5.4/gradle-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.5.4/gradle-plugin/packaging-oci-image.html)
* [Spring Boot Testcontainers support](https://docs.spring.io/spring-boot/3.5.4/reference/testing/testcontainers.html#testing.testcontainers)
* [Testcontainers Cassandra Module Reference Guide](https://java.testcontainers.org/modules/databases/cassandra/)
* [Testcontainers Kafka Modules Reference Guide](https://java.testcontainers.org/modules/kafka/)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/3.5.4/reference/actuator/index.html)
* [Spring Data for Apache Cassandra](https://docs.spring.io/spring-boot/3.5.4/reference/data/nosql.html#data.nosql.cassandra)
* [Spring for Apache Kafka](https://docs.spring.io/spring-boot/3.5.4/reference/messaging/kafka.html)
* [Testcontainers](https://java.testcontainers.org/)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
* [Spring Data for Apache Cassandra](https://spring.io/guides/gs/accessing-data-cassandra/)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)

### Testcontainers support

This project uses [Testcontainers at development time](https://docs.spring.io/spring-boot/3.5.4/reference/features/dev-services.html#features.dev-services.testcontainers).

Testcontainers has been configured to use the following Docker images:

* [`cassandra:latest`](https://hub.docker.com/_/cassandra)
* [`apache/kafka-native:latest`](https://hub.docker.com/r/apache/kafka-native)

Please review the tags of the used images and set them to the same as you're running in production.

