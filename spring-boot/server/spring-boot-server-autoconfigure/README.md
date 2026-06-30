# A2A Spring Boot Server AutoConfiguration

This module provides Spring Boot auto-configuration for the A2A server runtime layer.

## Source Of Truth

- [Documentation index](../../../docs/README.md)

## Artifact

- `a2a-spring-boot-server-autoconfigure`

## Responsibilities

- Bind `a2a.*` configuration properties.
- Adapt Spring `Environment` to the A2A `A2AConfigProvider` contract.
- Provide runtime beans for the server core:
  - `DefaultValuesConfigProvider`
  - `A2AConfigProvider`
  - `TaskStore`
  - `MainEventBus`
  - `QueueManager`
  - `MainEventBusProcessor`
  - `PushNotificationConfigStore`
  - `PushNotificationSender`
  - internal executor beans
- `RequestHandler`

## Notes

- This module does not depend on Servlet APIs.
- It is safe to use in non-web Spring Boot applications.
- `RequestHandler` is created only when the application provides an `AgentExecutor`.
- The module registers its auto-configuration through `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.

## Build

```bash
mvn -pl server/spring-boot-server-autoconfigure -am test
```
