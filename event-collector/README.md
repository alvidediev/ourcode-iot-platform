# Events-collector

## Описание

Сервис для получение телеметрии (events) из Кафки.

## Flow

1) При размещении сообщений в кафка топике *events.in* сервис начинается батчевое получение этих сообщений.
2) Сообщения батчем сохраняются в новом потоке в Cassandra для аналитики и дублируются в таблицу Postgres для реализации паттерна OutBox
3) Debezium отслеживает изменения таблицы в Postgres и размещает новые записи из таблицы outbox_events в топик *outbox_events*
4) Сообщения из топика *outbox_events* так же потребляются батчево. Из этого батча вытаскивается лист уникальных device-id и отправляется в отдельный топик *device-ids*
5) После того, как отправка в топик *device-ids* завершится успешно, мы помечаем записи в таблице outbox_events как успешно потребленные

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
* PostgreSQL
* Cassandra
* Kafka
* Debezium
* Prometheus / Grafana
* Loki (логирование)
* Tempo / Alloy (tracing)
* Docker / Docker Compose
*
