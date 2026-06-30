# A2A Spring Boot Server Integration Tests

This module contains integration tests for the Spring Boot REST stack.

## Source Of Truth

- [Documentation index](../../../../docs/README.md)

## Artifact

- `a2a-spring-boot-server-integration-tests`

## Coverage

- Spring Boot application context startup
- REST endpoint wiring
- request delegation into the A2A runtime
- server-sent events responses
- starter-level dependency behavior

## Purpose

The tests verify the assembled Spring Boot integration rather than individual unit classes only.

## Build

```bash
mvn -pl server/rest/spring-boot-server-integration-tests -am test
```
