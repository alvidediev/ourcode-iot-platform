# Our code

## Описание

Проект для сбора телеметрии с IoT устройств

Планируемые сервисы:

* `api-gateway` — маршрутизация, авторизация 
* `api-orchestrator` — оркестратор для вызовов других микросервисов
* `command-service` — принимает команды по gRPC и отдается их устройствам. Работает с отдельной БД PostgreSQL
* `device-service` — CRUD API по устройствам
* `events-service` — чтение событиый из Cassandra
* `device-service` — Получает device_id сохраняется в PostgreSQL
* `failed-events-service` — DLT -> JSON в MinIO

Готовое:

* `monitoring` — инфраструктура мониторинга и трассировки: Grafana, Prometheus, Loki, Tempo, Alloy
* [Events-collector](../../event-collector/README.md) — Kafka -> Cassandra + device-id

Диаграммы:

[Containers diagram](../diagrams/containers.puml)
[Context diagram](../diagrams/context.puml)

В проекте реализованы:

- Сбор, хранение и визуализация метрик сервисов через Prometheus и Grafana
- Централизованное логирование (Loki)
- Трассировка распределённых запросов (Tempo, Alloy)
- Отдельные Grafana dashboards для сервисов и общей инфраструктуры

## Быстрый старт через Docker Compose

```bash
make run
```

Остальные команды можно узнать с помощью команды:

```bash
make help 
```

Запускаются:

* PostgreSQL (`localhost:5432`)
* PostgreSQL (`localhost:5433`, keycloak) 
* Keycloak (`localhost:5432`, keycloak) 
* Kafka (`localhost:9092`)
* Kafka UI (`localhost:8070`)
* Grafana (`localhost:3000`, логин: admin/admin)
* Prometheus (`localhost:9090`)
* Tempo (`localhost:3200`)
* Loki (`localhost:3100`)
* Alloy (`localhost:9080`)
* Redis (`localhost:6379`)
* RedisInsight (`localhost:8001`)
* Camunda (`localhost:8088`)
* Cassandra (`localhost:9042`)
* MinIO (`localhost:9001`, логин: miniouser/miniopassword)
* events-collector (`localhost:9050`)

## Структура проекта

```
ourcode-iot-platform/
│
├── events-collector
│  
├── iot-platform-architecture/     (диаграммы архитектуры)
│   ├── infrastructure
│   │       └── monitoring/
│   │               ├── prometheus/
│   │               │   ├── prometheus.yml
│   │               │   └── alert.rules.yml
│   │               ├── grafana/
│   │               │   ├── dashboards/
│   │               │   │   ├── dashboards.yaml
│   │               │   │   ├── kafka-dashboard.json
│   │               │   │   ├── node_exporter_dashboard.json
│   │               │   │   └── postgresql_dashboard.json
│   │               │   └── datasources/
│   │               │       └── datasources.yaml
│   │               ├── loki/
│   │               │   └── loki-config.yaml
│   │               ├── tempo/
│   │               │   └── tempo.yaml
│   │               └── alloy/
│   │                 └── config.alloy
│   └── diagrams/       
│          ├── containers.puml       
│          └── context.puml              
│   ├── .env.example
│   ├── Makefile
│   └── docker-compose.yaml   
├── README.md
└── .gitignore
```



## Технологии

* Java 24
* Golang
* Spring Boot 3 (Web, WebFlux, Kafka, Data, Actuator)
* PostgreSQL
* Cassandra
* Camunda
* Kafka
* Prometheus / Grafana
* Loki (логирование)
* Tempo / Alloy (tracing)
* Docker / Docker Compose
* MinIO
*

