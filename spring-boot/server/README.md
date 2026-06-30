# A2A Spring Boot Server Integration

This directory contains the server-side Spring Boot integration modules.

## Source Of Truth

- [Documentation index](../../docs/README.md)
- [REST TCK workflow](rest/TCK.md)
- [Contributing](../../CONTRIBUTING.md)

## Layout

- `spring-boot-server-autoconfigure/`
  - Shared runtime auto-configuration for all Spring Boot transports.
- `rest/`
  - REST/MVC transport integration, starter, tests, and SUT.
- `jsonrpc/`
  - Reserved for JSON-RPC server integration.
- `grpc/`
  - Reserved for gRPC server integration.

## Build

```bash
mvn -pl server -am test
```

