# A2A Spring Boot REST Example

This directory contains the runnable REST demo.

## Source Of Truth

- [Documentation index](../../../docs/README.md)

## Modules

- `server/` - the A2A agent runtime and REST transport server.
- `client/` - a small web app that exercises the server through scenario endpoints and Swagger UI.

## Build

```bash
mvn -pl examples/spring-boot/rest -am test
```
