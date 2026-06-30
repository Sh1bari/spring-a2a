# A2A Spring Boot REST Client Example

This module contains the runnable REST client example.

## Source Of Truth

- [Documentation index](../../../../docs/README.md)

## What It Demonstrates

- Fetching the server `AgentCard`
- Creating an A2A REST client from the SDK
- Running a blocking demo flow through a controller endpoint
- Running a streaming demo flow through a controller endpoint
- Running a full scenario that combines both flows
- Inspecting and replaying the demo through Swagger UI

## Run

```bash
mvn -pl examples/spring-boot/rest/server -am spring-boot:run
```

```bash
mvn -pl examples/spring-boot/rest/client -am spring-boot:run
```

## Build

```bash
mvn -pl examples/spring-boot/rest/client -am test
```
