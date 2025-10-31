# README

> **Project**: sporty-events REST Service  
> The original assignment (“Service with scheduled REST calls”) is available in the provided documentation.

---

## Quick Overview

This application is a **Spring Boot microservice** that tracks live sports events.  
It exposes a REST API to update the status of events (“live” / “not_live”), stores them in a **local database**, and — for each *live* event — schedules a **periodic REST call (every 10 seconds)** using **Apache Camel**.  
The fetched data is then published to a **Kafka topic**.

---

## Features

- `POST /events/status` — Updates or creates an event with a given status (`true` for live or `false` for not live).
- Periodic REST calls (every 10 seconds) for live events (handled by Apache Camel).
- Transformation of the external API response into a Kafka message.
- Local database persistence of event data and state (H2 by default, configurable).
- Basic retry and logging for reliability and observability.

---

## Requirements (prerequisites)

- **Java 25 (JDK 25) **
- **Gradle wrapper** included (`./gradlew` / `gradlew.bat`)
- **Docker & Docker Compose** (to run Kafka and the service locally via `docker-compose`)
- **Local database** (H2 by default; can be switched to PostgreSQL/MySQL via config)

---

## Build and Run Locally (with Gradle)

### 1) Build
Build the application JAR:
```bash
./gradlew clean build
```

### 2) Run (Locally)

**Prerequisites**

Into the `docker` folder there is a `docker-compose.yml` that brings up Kafka. Use it to run a local stack easily:

```bash
cd docker
docker-compose up -d
docker-compose logs -f
# to stop
docker-compose down
```

**Start the service**

Start the local dependencies (Kafka, other services) and the application with `docker-compose` (see next section), or run the jar directly:

```bash
# run the app directly (requires Kafka already running)
java -jar build/libs/<artifact-name>.jar
# or
./gradlew bootRun
```
---

## Database Configuration

By default, the service uses an **H2 in-memory/local database**, automatically initialized at startup.  
You can switch to another database (PostgreSQL, MySQL, etc.) by updating `application.properties`.

Example configuration:

```properties
spring.datasource.url=jdbc:h2:file:./data/sportydb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=sa
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

---

## Apache Camel Integration

Apache Camel is used **only** for the scheduling and external REST calls.

For each event marked as **true**, a Camel route is started that:
1. Triggers every **10 seconds**.
2. Performs a REST call to the configured external endpoint (an AWS Lambda).
3. Transforms the response.
4. Publishes it to a Kafka topic (`eventScores`).

---

## Kafka Configuration

Kafka is used to publish messages derived from the REST API responses.

Example configuration (`application.properties`):

```properties
kafka.topic=eventScores
kafka.dlq-topic=eventScores_dlq
kafka.bootstrap-servers=localhost:29092
```

When running with `docker-compose`, ensure the `bootstrap-servers` in `application.properties` matches the Kafka service address exposed by the compose network (e.g. `kafka:29092`).

---

## Example Requests

### Set event live
```bash
curl -X POST http://localhost:8080/events/status   -H "Content-Type: application/json"   -d '{"eventId": "1234", "status": true}'
```

### Set event not_live
```bash
curl -X POST http://localhost:8080/events/status   -H "Content-Type: application/json"   -d '{"eventId": "1234", "status": false}'
```

---

## Test and Validation

Run all tests:
```bash
./gradlew test
```

Included test coverage:
- REST controller validation (status updates)
- Persistence layer (local database)
- Apache Camel scheduling and REST calls
- Kafka message publishing with error handling

---

## Production Tips

- Use an external database for persistent state storage.
- Add metrics, tracing, and structured logging for observability.
- Add Swagger/OpenAPI documentation for REST endpoints.
- Use CI/CD to build, test, and publish Docker images automatically.

---

## Design Decisions

This section explains the main architectural choices made in this project and the rationale behind them.

### Spring Web (for controllers)
- **Maturity and ecosystem**: Spring Web (Spring MVC / Spring Boot web starter) is a well-established framework with first-class support for building RESTful APIs, rich documentation, and a large ecosystem of integrations (validation, security, monitoring).
- **Developer productivity**: Spring’s annotations (`@RestController`, `@RequestMapping`, `@Valid`, etc.) and auto-configuration speed up development and reduce boilerplate, which is ideal for a small prototype.
- **Separation of concerns**: Using Spring Web for HTTP handling keeps controller logic, validation, and error handling separate from integration flows (which are handled by Camel), leading to cleaner, testable code.
- **Testability**: Spring provides testing support (MockMvc, test slices) that makes unit and integration testing of controllers straightforward.

**Trade-offs / Alternatives**: For ultra-lightweight or reactive workloads one could use Spring WebFlux or a minimal HTTP server (e.g., Micronaut or plain Jetty), but Spring Web offers the best balance of simplicity and features for this assignment.

### H2 (local database)
- **Zero friction for development**: H2 can run in-memory or as a local file, so there is no external database to install for quick development and tests.
- **Fast tests and resets**: Using an in-memory DB makes automated tests fast and deterministic; schema setup/teardown is simple.
- **Sufficient for prototype**: The assignment requires local persistence of event state; H2 satisfies this requirement without introducing external dependencies.
- **Configurable for production**: The data access layer is implemented with Spring Data JPA, so switching to PostgreSQL or MySQL in production is straightforward (change the `datasource` config and driver).

**Trade-offs / Alternatives**: H2 is not suitable for production scale or multi-instance deployments. For production a durable RDBMS (Postgres, MySQL) or a distributed datastore would be recommended.

### Apache Camel (for scheduling and REST polling)
- **Integration DSL**: Camel’s routing DSL expresses periodic polling, REST calls, transformations, and error handling in a compact and readable way.
- **Lifecycle and management**: Camel routes can be easily scheduled and they can implement a complete task flow, which fits the requirement well.
- **Built-in components**: Camel provides timers, HTTP/REST components, retry/error handling policies, and Kafka components out of the box—reducing custom glue code.
- **Observability and extensibility**: Camel integrates with metrics and tracers and allows adding filters, processors and error handlers without scattering orchestration code across services.

**Trade-offs / Alternatives**: A custom scheduler (e.g., `ScheduledExecutorService` or Spring’s `@Scheduled`) plus manual HTTP/Kafka code would work and reduce a dependency. However, Camel simplifies orchestration and increases maintainability for an integration-focused flow.

### Apache Kafka (message broker)
- **Decoupling producers and consumers**: Kafka allows publishing event updates and decouples downstream consumers (analytics, storage, other microservices) from the polling producer.
- **Durability and replayability**: Kafka persists messages and enables consumers to replay streams, which is useful for debugging or rebuilding downstream state.
- **Scalability**: Kafka scales well for high-throughput scenarios; using Kafka in the prototype shows how the design can handle larger loads.
- **Industry standard**: Kafka is a widely-used streaming platform; adopting it aligns the prototype with common production architectures.

**Trade-offs / Alternatives**: For a very small or single-process deployment, an in-memory queue or a simpler broker (RabbitMQ) could suffice. Kafka adds operational complexity, but provides strong guarantees and scalability that are valuable if the system grows.

## AI Usage Disclosure

This project documentation was partially generated with **ChatGPT (OpenAI GPT-5)**.  
AI assistance was used to draft the README structure and design explanations, then **reviewed, verified, and refined manually** to ensure technical accuracy and consistency with the implemented code.

AI assistance has been used also to retrieve documentation and examples of Apache Camel components.
