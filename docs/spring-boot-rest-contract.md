# Spring Boot REST Contract

This document is the source of truth for the REST transport surface exposed by the Spring Boot modules and the REST example applications.

## Endpoints

| Method | Path |
| --- | --- |
| `GET` | `/.well-known/agent-card.json` |
| `POST` | `/message:send` |
| `POST` | `/message:stream` |
| `GET` | `/tasks/{taskId}` |
| `GET` | `/tasks` |
| `POST` | `/tasks/{taskId}:cancel` |
| `POST` | `/tasks/{taskId}:subscribe` |
| `POST` | `/tasks/{taskId}/pushNotificationConfigs` |
| `GET` | `/tasks/{taskId}/pushNotificationConfigs/{configId}` |
| `GET` | `/tasks/{taskId}/pushNotificationConfigs` |
| `DELETE` | `/tasks/{taskId}/pushNotificationConfigs/{configId}` |

## What Uses This Contract

- `spring-boot/server/rest/spring-boot-server-rest`
- `spring-boot/server/rest/spring-boot-server-rest-sut`
- `spring-boot/server/rest/spring-boot-server-integration-tests`
- `examples/spring-boot/rest/server`
- `examples/spring-boot/rest/client`

## Related Documents

- [Runtime configuration](spring-boot-runtime-config.md)
- [REST TCK](../spring-boot/server/rest/TCK.md)
