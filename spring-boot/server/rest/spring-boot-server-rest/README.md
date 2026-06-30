# A2A Spring Boot Server REST

This module provides the REST transport adapter for the A2A runtime.

## Source Of Truth

- [Documentation index](../../../../docs/README.md)

## Artifact

- `a2a-spring-boot-server-rest`

## Responsibilities

- Expose the HTTP response mapper used by MVC endpoints.
- Expose the Spring MVC controller for A2A HTTP transport.
- Register servlet-specific auto-configuration.
- Centralize HTTP error mapping through `A2ASpringBootMvcExceptionHandler`.

## Extension Point

Applications can replace the default mapper or controller by defining their own Spring beans.

## Build

```bash
mvn -pl server/rest/spring-boot-server-rest -am test
```
