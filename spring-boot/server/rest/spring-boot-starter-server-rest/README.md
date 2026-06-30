# A2A Spring Boot Server REST Starter

This module is a dependency-only starter for Spring Boot REST/MVC applications.

## Source Of Truth

- [Documentation index](../../../../docs/README.md)

## Artifact

- `a2a-spring-boot-starter-server-rest`

## Transitive Dependencies

- `a2a-spring-boot-server-autoconfigure`
- `a2a-spring-boot-server-rest`
- `spring-boot-starter-web`

## Purpose

Add this starter to get the full Spring Boot server integration with a single dependency.

## Application Requirements

The application should still provide:

- `AgentCard`
- `AgentExecutor`

Those beans drive the agent identity and runtime execution logic.

## Build

```bash
mvn -pl server/rest/spring-boot-starter-server-rest -am test
```
