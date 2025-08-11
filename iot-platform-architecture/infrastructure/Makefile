# Makefile for docker-compose management

.PHONY: run up stop restart clean ps logs

# Default target (run the project)
run: up

# Start containers in detached mode
up:
	docker-compose  up -d

# Stop containers
stop:
	docker-compose  stop

# Restart containers
restart: stop up

# Stop and remove containers, networks, volumes
clean:
	docker-compose down -v

# List running containers
ps:
	docker-compose ps

# Show logs for all containers
logs:
	docker-compose logs -f

# Show logs for specific service (make logs-service SERVICE=alloy)
logs-service:
	docker-compose logs -f $(SERVICE)

# Build containers (without cache)
build:
	docker-compose build --no-cache

# Check container statuses
status:
	docker-compose ps

# Open shell in specific container (make shell SERVICE=alloy)
shell:
	docker-compose exec $(SERVICE) sh

# Show help
help:
	@echo "Available commands:"
	@echo "  make run       - Start the project (alias for up)"
	@echo "  make up        - Start containers in detached mode"
	@echo "  make stop      - Stop containers"
	@echo "  make restart   - Restart containers"
	@echo "  make clean     - Stop and remove containers, networks, volumes"
	@echo "  make ps        - List running containers"
	@echo "  make logs      - Show logs for all containers"
	@echo "  make logs-service SERVICE=name - Show logs for specific service"
	@echo "  make build     - Rebuild containers (no cache)"
	@echo "  make status    - Check container statuses"
	@echo "  make shell SERVICE=name - Open shell in specific container"
	@echo "  make help      - Show this help"