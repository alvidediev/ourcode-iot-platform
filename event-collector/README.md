# Events-collector

## Описание

Сервис для получение телеметрии (events) из Кафки.

## Flow

1) При размещении сообщений в Kafka топике *events.in* сервис начинается батчевое получение этих сообщений.
2) Сообщения батчем сохраняются в новом потоке в Cassandra для аналитики.
3) При успешном сохранении в Cassandra, отправляем *acknowledge()* в Kafka.
4) Отправляем уникальные device-ids в топик *device-ids*

## Diagrams

[sequence](./diagrams/sequence.puml)

[component](./diagrams/component.puml)

## Имитация работы

При желании (кроме тестов) можно обратиться по curl для имитации работы

```bash
make test
```

данный curl отправит в кафка топик events.in 1000 сообщений

## Используемые технологии

* Java 24
* Spring Boot 3 (Web, Kafka, Data, Actuator)
* Cassandra
* Kafka
* Prometheus / Grafana
* Loki (логирование)
* Tempo / Alloy (tracing)
* Docker / Docker Compose
*
