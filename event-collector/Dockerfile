FROM gradle:8.14.3-jdk-21-and-24 as builder
WORKDIR /app
COPY . .
RUN sed -i '/org.gradle.java.home/d' gradle.properties
RUN gradle clean build

FROM eclipse-temurin:24-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]