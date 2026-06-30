# A2A Spring Boot Server REST Aggregator

This directory aggregates the REST-specific Spring Boot server modules.

## Source Of Truth

- [Documentation index](../../../docs/README.md)
- [REST TCK workflow](TCK.md)

## Modules

- `spring-boot-server-rest`
  - Servlet and Spring MVC transport adapter.
- `spring-boot-starter-server-rest`
  - Dependency-only starter for REST/MVC applications.
- `spring-boot-server-integration-tests`
  - Integration tests for the REST server stack.
- `spring-boot-server-rest-sut`
  - Runnable REST SUT for the external A2A TCK.

## Build

```bash
mvn -pl server/rest -am test
```

