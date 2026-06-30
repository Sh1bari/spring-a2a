# Spring Boot REST Example

This document is the source of truth for the runnable REST example applications.

## Example Layout

| App | Module | Artifact | Role |
| --- | --- | --- | --- |
| Server | `examples/spring-boot/rest/server` | `a2a-spring-boot-examples-rest-server` | Exposes the A2A REST server. |
| Client | `examples/spring-boot/rest/client` | `a2a-spring-boot-examples-rest-client` | Calls the server and demonstrates the client flow. |

## Server Configuration

```yaml
server:
  port: 18080

a2a:
  executor:
    core-pool-size: 2
    max-pool-size: 4
    keep-alive-seconds: 60
    queue-capacity: 32
  blocking:
    agent-timeout-seconds: 30
    consumption-timeout-seconds: 5
```

## Client Configuration

```yaml
server:
  port: 18081

a2a:
  example:
    server-url: http://localhost:18080
    hello-message: hello from the Spring Boot REST client
    stream-message: stream from the Spring Boot REST client
    streaming-timeout-seconds: 15
```

## How To Run

Start the server:

```bash
mvn -pl examples/spring-boot/rest/server -am spring-boot:run
```

Start the client in a second terminal:

```bash
mvn -pl examples/spring-boot/rest/client -am spring-boot:run
```

## Manual Verification

Use the REST contract documented here:

- [REST contract](spring-boot-rest-contract.md)

The example is intentionally minimal:

- the server demonstrates the REST transport surface
- the client demonstrates a blocking flow, a streaming flow, and a combined scenario
- Swagger UI is used for manual inspection on the client side
