# A2A Spring Boot Integration

This directory contains the Spring Boot library modules for the A2A protocol support.

## Source Of Truth

- [Documentation index](../docs/README.md)
- [REST TCK workflow](server/rest/TCK.md)
- [Contributing](../CONTRIBUTING.md)

## Module Map

| Path | Artifact | Role |
| --- | --- | --- |
| `server/spring-boot-server-autoconfigure` | `a2a-spring-boot-server-autoconfigure` | Shared auto-configuration and default bean wiring. |
| `server/rest/spring-boot-server-rest` | `a2a-spring-boot-server-rest` | REST/MVC adapter for the server runtime. |
| `server/rest/spring-boot-starter-server-rest` | `a2a-spring-boot-starter-server-rest` | Dependency starter for REST server applications. |
| `server/rest/spring-boot-server-integration-tests` | `a2a-spring-boot-server-integration-tests` | Integration tests for the REST server surface. |
| `server/rest/spring-boot-server-rest-sut` | `a2a-spring-boot-server-rest-sut` | Runnable SUT used by the REST TCK. |
| `server/jsonrpc` | `a2a-spring-boot-server-jsonrpc-parent` | Reserved for JSON-RPC server support. |
| `server/grpc` | `a2a-spring-boot-server-grpc-parent` | Reserved for gRPC server support. |
| `client` | `a2a-spring-boot-client` | Reserved for future client-side Spring integration. |

## Build

```bash
mvn -pl spring-boot -am test
```
