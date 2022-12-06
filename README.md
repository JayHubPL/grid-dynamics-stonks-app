# Stock trading broker project

## Team
- [Michael Bragilevsky](https://gitlab.griddynamics.net/mbragilevsky) - mentor
- [Jakub Łaba](https://gitlab.griddynamics.net/jlaba)
- [Hubert Mazur](https://gitlab.griddynamics.net/hmazur)

## Tech stack
- Spring Boot
- Hibernate
- PostgreSQL
- Docker

## Business requirements
1. Main players are `User`, `Stock`, `Broker`, `Order`
2. CRUD operations for User and Order
3. Commission percent needs to be calculated based on formula, you decide
4. All relevant actions need to be validated with appropriate error handling
5. Real time stock price can be provided by [finnhub.io](https://finnhub.io)
6. Mapping between DTOs and model repository objects
7. Unit tests for all parts of the code, including Controllers, BL, persistence layer
8. All the APIs need to be available in Swagger UI
9. **(OPTIONAL)** User authentication
10. **(OPTIONAL)** Stock price caching

## Running the application
### Requirements
> docker, docker-compose, some java 17 jdk, gradle (optional)
### Run
1. Build the project jar
```sh
gradle clean bootJar
```
Or use the wrapper if you don't have gradle installed:
```sh
./gradlew clean bootJar
```
2. Run the app
```sh
docker-compose up -d
```
### Stop
```sh
docker-compose down
```