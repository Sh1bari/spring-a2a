# A2A Spring Boot Server REST Example

This module contains the runnable REST server example.

## Source Of Truth

- [Documentation index](../../../../docs/README.md)

## What It Demonstrates

- Spring Boot auto-configuration from the SDK starter
- A minimal `AgentCard` bean
- A dedicated `AgentExecutor` Spring component
- REST transport endpoints exposed by the server module
- A direct message response for `hello`
- A streaming task flow for `stream`

## Run

```bash
mvn -pl examples/spring-boot/rest/server -am spring-boot:run
```

## Build

```bash
mvn -pl examples/spring-boot/rest/server -am test
```
