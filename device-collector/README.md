# Device-collector

## Описание

Сервис для получения уникальный device-id из топика (device-id-topic) из Кафки.  И отправки этих device-id в Apache ShardingSphere

## Flow

1) При размещении сообщений в Kafka топике *device-id-topic* сервис начинается батчевое получение этих сообщений.
2) Сообщения сохраняются Apache ShardingSphere.
3) При достижении лимита ретрая, сообщение отправляется в *dead-letter-topic*

## Diagrams

[sequence](./diagrams/sequence.puml)

[component](./diagrams/component.puml)

## Билд проекта

```bash
make run
```

## Перезапуск проекта

```bash
make restart
```



## Используемые технологии

* Java 24
* Spring Boot 3 (Web, Kafka, Data, Actuator)
* Apache ShardingSphere
* PostgreSQL
* Kafka
* Prometheus / Grafana
* Loki (логирование)
* Tempo / Alloy (tracing)
* Docker / Docker Compose
*
