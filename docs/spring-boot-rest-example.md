# Spring Boot REST Example

This document is the source of truth for the runnable REST example applications.

## Example Layout

| App | Module | Artifact | Role |
| --- | --- | --- | --- |
| Server | `examples/spring-boot/rest/server` | `a2a-spring-boot-examples-rest-server` | Exposes the A2A REST server and the demo `AgentExecutor`. |
| Client | `examples/spring-boot/rest/client` | `a2a-spring-boot-examples-rest-client` | Renders the browser guide and playground and calls the server over HTTP. |

## What The Example Shows

The client application renders a browser-based playground at `/guide`.

It demonstrates:

1. agent discovery through `GET /.well-known/agent-card.json`;
2. normal A2A message exchange through `POST /message:send`;
3. task creation and inspection through `GET /tasks/{taskId}`;
4. task streaming through `POST /message:stream`;
5. task subscription through `POST /tasks/{taskId}:subscribe`;
6. push notification configuration through the task push config endpoints;
7. push callback delivery in the client inbox.

## Screenshot

![Spring A2A REST example](../assets/rest-example-site.png)

## Server Configuration

```yaml
server:
  port: 18080

spring:
  ai:
    openai:
      base-url: https://openrouter.ai/api/v1
      api-key: ${OPENROUTER_API_KEY:}
      chat:
        options:
          model: openai/gpt-oss-120b:free
  a2a:
    executor:
      core-pool-size: 2
      max-pool-size: 4
      keep-alive-seconds: 60
      queue-capacity: 32
    blocking:
      agent-timeout-seconds: 30
      consumption-timeout-seconds: 5
```

If `OPENROUTER_API_KEY` is missing, the example server returns `Mock response from LLM`.

## Client Configuration

```yaml
server:
  port: 18081

spring:
  a2a:
    example:
      server-url: http://localhost:18080
      hello-message: hello from the Spring Boot REST client
      stream-message: stream from the Spring Boot REST client
      streaming-timeout-seconds: 15
      callback-url: http://localhost:18081/guide/push-notifications/callback
```

## How To Run

Start the server:

```bash
mvn -pl examples/spring-boot/rest/server -am spring-boot:run
```

Start the client in a second terminal:

```bash
mvn -pl examples/spring-boot/rest/client -am spring-boot:run
```

Then open:

- `http://localhost:18081/guide`
- `http://localhost:18081/guide/playground`

## Related Documents

- [REST contract](spring-boot-rest-contract.md)
- [Runtime configuration](spring-boot-runtime-config.md)
- [Compatibility](compatibility.md)
