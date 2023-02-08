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

> docker, docker-compose

### Run

1. Start the application

```shell
make start
```

2. Stop the application

```shell
make stop
```

3. Clean up the docker images

```shell
make clean
```

## API docs

After running the application, you can access Swagger UI with api docs at

```
{application context}/swagger-ui.html
```

Or you can get the raw OpenAPI JSON at

```
{application context}/v3/api-docs
```

For example, when running the app locally, the URLs will be:

- Swagger UI

```
http://localhost:8080/stonks-jh/swagger-ui.html
```

- OpenAPI JSON

```
http://localhost:8080/stonks-jh/v3/api-docs
```

## Contribution conventions

### Commit messages

Capitalize 1st letter and use present simple
> ~~Implemented X~~ => Implement X \
> ~~Docker config~~ => Configure docker

### Branch naming

Use labels - `label/branch-name`
> `dev` - development of new features\
> `test` - writing tests\
> `fix` - fixes of existing features\
> `refactor` - self explanatory\
> `config` - configurations (docker, pipelines, etc.)\
> `doc` - documentation changes

### Merge requests

#### Naming

Name MRs same way as you would name a commit message - since it's
most likely going to end up as one once you squash and merge.

#### Merging

Let's squash commits to keep the history more readable and concise.
If one is interested in investigating individual squashed commits,
they can always explore merged MRs.

### Issues

Same naming guidelines as MRs, try to remember linking MRs to issues for
more comfortable navigation around the repo.
