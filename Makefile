COMPOSE=docker-compose
COMPOSE_FLAGS=-d
IMAGES=stonks-jh-app

start:
	$(COMPOSE) up $(COMPOSE_FLAGS)
stop:
	$(COMPOSE) down
clean:
	docker image rm $(IMAGES)