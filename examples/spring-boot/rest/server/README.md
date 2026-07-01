# A2A Spring Boot Server REST Example

This module contains the runnable REST server example.

## Source Of Truth

- [Documentation index](../../../../docs/README.md)

## What It Demonstrates

- Spring Boot auto-configuration from the SDK starter
- A polished `AgentCard` bean with explicit sample prompts
- A dedicated `AgentExecutor` Spring component with hello/help/stream branches
- REST transport endpoints exposed by the server module
- A direct message response for greetings
- A help-style response that explains what the demo understands
- A streaming task flow for `stream`

## Run

```bash
mvn -pl examples/spring-boot/rest/server -am spring-boot:run
```

## Build

```bash
mvn -pl examples/spring-boot/rest/server -am test
```
