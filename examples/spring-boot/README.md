# A2A Spring Boot Examples

Runnable Spring Boot examples for the A2A protocol support in this repository.

## Source Of Truth

- [Documentation index](../../docs/README.md)

## Module Map

| Path | Artifact | Status |
| --- | --- | --- |
| `rest/server` | `a2a-spring-boot-examples-rest-server` | Runnable REST server example. |
| `rest/client` | `a2a-spring-boot-examples-rest-client` | Runnable REST client example. |
| `jsonrpc/*` | n/a | Reserved for future JSON-RPC examples. |
| `grpc/*` | n/a | Reserved for future gRPC examples. |

## Build

```bash
mvn -pl examples/spring-boot -am test
```
