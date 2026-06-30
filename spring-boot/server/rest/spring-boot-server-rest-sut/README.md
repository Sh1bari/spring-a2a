# A2A Spring Boot REST TCK SUT

This module is a runnable Spring Boot REST system-under-test for the external A2A TCK.

## Source Of Truth

- [Documentation index](../../../../docs/README.md)
- [REST TCK workflow](../TCK.md)

## What It Is For

- Start a real Spring Boot REST server that exposes the A2A protocol endpoints.
- Feed that server to the external `a2a-tck` runner.
- Keep the REST SUT separate from JSON-RPC and gRPC so each transport can be validated independently later.

## Run

From the repository root:

```bash
mvn -pl server/rest/spring-boot-server-rest-sut -am spring-boot:run
```

The SUT listens on `http://localhost:9999` by default.

## Build

```bash
mvn -pl server/rest/spring-boot-server-rest-sut -am test
```
