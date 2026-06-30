# Spring Boot Runtime Configuration

This document is the source of truth for the Spring Boot runtime configuration used by:

- `spring-boot/server/spring-boot-server-autoconfigure`
- `spring-boot/server/rest/spring-boot-starter-server-rest`
- `spring-boot/server/rest/spring-boot-server-rest`
- `spring-boot/server/rest/spring-boot-server-integration-tests`
- `spring-boot/server/rest/spring-boot-server-rest-sut`
- `examples/spring-boot/rest/server`
- `examples/spring-boot/rest/client`

## Configuration Properties

| Property | Default | Purpose |
| --- | --- | --- |
| `a2a.executor.core-pool-size` | `5` | Core size of the internal executor. |
| `a2a.executor.max-pool-size` | `50` | Maximum size of the internal executor. |
| `a2a.executor.keep-alive-seconds` | `60` | Idle timeout for extra executor threads. |
| `a2a.executor.queue-capacity` | `100` | Queue size for the internal executor. |
| `a2a.blocking.agent-timeout-seconds` | `30` | Timeout for agent-side blocking operations. |
| `a2a.blocking.consumption-timeout-seconds` | `5` | Timeout for event consumption operations. |
| `a2a.agent-card.cache.max-age` | `3600` | Agent card cache max age in seconds. |

## Shared Runtime Example

Use this as the baseline runtime configuration for server modules:

```yaml
a2a:
  executor:
    core-pool-size: 5
    max-pool-size: 50
    keep-alive-seconds: 60
    queue-capacity: 100
  blocking:
    agent-timeout-seconds: 30
    consumption-timeout-seconds: 5
  agent-card:
    cache:
      max-age: 3600
```

## Bean Override Model

Application beans override the defaults when they are present in the Spring context.

### Runtime Beans

- `TaskStore`
- `MainEventBus`
- `QueueManager`
- `MainEventBusProcessor`
- `PushNotificationConfigStore`
- `PushNotificationSender`
- `RequestHandler`

### Executor Beans

- `a2aInternalExecutor`
- `a2aEventConsumerExecutor`

### Configuration Provider Chain

`A2AConfigProvider` reads Spring `Environment` properties first and falls back to the classpath defaults loaded by `DefaultValuesConfigProvider`.

## Notes

- This module set does not require Servlet APIs for the core runtime.
- `RequestHandler` is created only when the application provides an `AgentExecutor`.
- The auto-configuration is registered through `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
