# A2A Spring Boot REST Client Example

This module contains the runnable REST client example and its UI-friendly walkthrough.

## Source Of Truth

- [Documentation index](../../../../docs/README.md)

## Source Layout

- `config/` Spring configuration and typed properties
- `controller/` REST endpoints that expose the demo flow
- `dto/` request and response payloads
- `service/` A2A client orchestration and transport logic

## What It Demonstrates

- Discovering the available scenarios via `/demo`
- Fetching the raw server `AgentCard` only when needed
- Creating an A2A REST client from the SDK
- Running a blocking demo flow through a controller endpoint
- Running a streaming demo flow through a controller endpoint
- Running a full scenario that combines both flows
- Inspecting and replaying the demo through Swagger UI

## Suggested Order

1. Open `GET /demo` to inspect the available scenarios and sample prompts.
2. Call `GET /demo/agent-card` only if you want the raw discovery payload.
3. Call `POST /demo/blocking` with `hello`.
4. Call `POST /demo/streaming` with `stream this`.
5. Call `POST /demo/full-flow` to see both flows in one response.

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
