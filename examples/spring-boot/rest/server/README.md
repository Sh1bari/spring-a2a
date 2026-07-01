# A2A Spring Boot Server REST Example

This module contains the runnable REST server example.

## Source Of Truth

- [Documentation index](../../../../docs/README.md)

## What It Demonstrates

- Spring Boot auto-configuration from the SDK starter
- a single `AgentCard` bean with explicit sample prompts
- a dedicated `AgentExecutor` Spring component with hello/help/stream/cancel branches
- a model-backed `help` branch driven by `OPENROUTER_API_KEY` and the A2A `help` flow
- REST transport endpoints exposed by the server module
- discovery through `GET /.well-known/agent-card.json`
- message exchange through `POST /message:send`
- streaming task exchange through `POST /message:stream`
- task lookup through `GET /tasks/{taskId}` and task listing through `GET /tasks`
- task cancellation through `POST /tasks/{taskId}:cancel`
- task subscription through `POST /tasks/{taskId}:subscribe`
- push notification config management through the `/tasks/{taskId}/pushNotificationConfigs` endpoints
- a direct response for greetings
- a help response that shows how the server calls a model inside A2A
- a streaming task flow for `stream`
- a cancellable task flow for `cancel`

## What You Should See

When the server is running, `GET /.well-known/agent-card.json` should return the public agent card.

If you send the messages below through the client or the A2A transport, you should see:

- `hello` - a direct reply from the agent;
- `stream this` - a streaming task with task updates and a final artifact;
- `help` - a model-backed help response returned through the A2A executor;
- `cancel this` - a cancellable task that can be canceled through the REST transport.

If `OPENROUTER_API_KEY` is not configured, `help` still returns `Mock response from LLM` and the app keeps running.

## Run

```bash
mvn -pl examples/spring-boot/rest/server -am spring-boot:run
```

Set `OPENROUTER_API_KEY` before starting the app if you want the help flow to call the model.

The server starts on port `18080`.

## Build

```bash
mvn -pl examples/spring-boot/rest/server -am test
```

## What To Change

- `src/main/resources/application.yml` - set `OPENROUTER_API_KEY` through the environment, or keep it empty to use the fallback branch.
- `SpringBootRestServerExampleConfiguration` - change the `AgentCard` name, description, skills, or examples.
- `SpringBootRestServerAgentExecutor` - change how `hello`, `stream`, or `help` behave.
- `SpringBootRestServerAiService` - change the model prompt used by the `help` flow.
