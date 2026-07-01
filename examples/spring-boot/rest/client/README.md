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
- Calling the A2A help flow and receiving the model-backed server response
- Running a full scenario that combines both flows
- Inspecting and replaying the demo through Swagger UI

## What You Should See

When the client is running, open `GET /demo` first. The response should list the available demo routes and sample prompts.

Then try:

- `POST /demo/blocking` with `hello`
- `POST /demo/streaming` with `stream this`
- `POST /demo/help` with `help`
- `POST /demo/full-flow` to see the combined result

For `help`, the client sends the request through the A2A server and returns the model-backed response from the server.

## Suggested Order

1. Open `GET /demo` to inspect the available scenarios and sample prompts.
2. Call `GET /demo/agent-card` only if you want the raw discovery payload.
3. Call `POST /demo/blocking` with `hello`.
4. Call `POST /demo/streaming` with `stream this`.
5. Call `POST /demo/help` to make the client request the server model response through A2A.
6. Call `POST /demo/full-flow` to see the blocking, streaming, and help flows together.

## Run

```bash
mvn -pl examples/spring-boot/rest/server -am spring-boot:run
```

```bash
mvn -pl examples/spring-boot/rest/client -am spring-boot:run
```

The client starts on port `18081`.

## Build

```bash
mvn -pl examples/spring-boot/rest/client -am test
```

## What To Change

- `src/main/resources/application.yml` - update `spring.a2a.example.server-url` if the server runs on a different host or port.
- `SpringBootRestClientExampleProperties` - change the default `hello`, `stream`, or timeout values.
- `SpringBootRestClientDemoService` - change how the client fetches the agent card or runs the demo flows.
- `SpringBootRestClientDemoController` - add or remove demo endpoints.
