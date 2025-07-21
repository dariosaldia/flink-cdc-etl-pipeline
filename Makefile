COMPOSE = docker compose
PROFILE = --profile app

## Start infra only (MySQL, Kafka, Connect, connector-init)
infra-up: down
	$(COMPOSE) up -d

build:
	$(COMPOSE) $(PROFILE) build

## Start full stack (infra + Flink job)
full-up: build down
	$(COMPOSE) $(PROFILE) up -d

## Tear everything down
down:
	$(COMPOSE) down -v

## Tail all logs
logs:
	$(COMPOSE) logs -f
