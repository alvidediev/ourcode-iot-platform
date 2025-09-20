.PHONY: run up stop restart clean ps

up:
	cd ../iot-platform-architecture/infrastructure/ && docker-compose up -d device-collector shard0 replica0 shard1 replica1 zookeeper schema-registry kafka kafka-ui postgres-exporter kafka-exporter node_exporter loki alloy tempo create-tempo-bucket prometheus grafana

run: up

ps:
	cd ../iot-platform-architecture/infrastructure/ && docker-compose ps

clean:
	cd ../iot-platform-architecture/infrastructure/ && docker-compose down -v

stop:
	cd ../iot-platform-architecture/infrastructure/ && docker-compose  stop

restart: stop up