# Spring A2A

[![Build](https://github.com/Sh1bari/spring-a2a/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/Sh1bari/spring-a2a/actions/workflows/build.yml)
[![Spring Boot REST TCK](https://github.com/Sh1bari/spring-a2a/actions/workflows/run-spring-boot-rest-tck.yml/badge.svg?branch=master)](https://github.com/Sh1bari/spring-a2a/actions/workflows/run-spring-boot-rest-tck.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

Spring A2A provides Spring Boot auto-configuration, starter modules, and runnable examples for applications that communicate through the [Agent2Agent Protocol](https://a2a-protocol.org/) using the official [A2A Java SDK](https://github.com/a2aproject/a2a-java).

> **You implement the agent. Spring A2A handles the protocol integration.**

> [!IMPORTANT]
> The current implementation provides the **REST server integration**.
> REST client, JSON-RPC, and gRPC integrations are reserved for future work.

## What Is Spring A2A?

A2A defines how independent agents discover one another, exchange messages, execute tasks, and stream task updates.

The A2A Java SDK provides the protocol model and runtime abstractions.

Spring A2A connects those abstractions to Spring Boot by:

- configuring the A2A server runtime;
- exposing transport endpoints;
- publishing the agent card;
- delegating requests to application-defined agent logic;
- allowing runtime components to be replaced with custom Spring beans.

## What You Will See

The runnable REST example shows a complete A2A flow:

- a client discovers the server via `GET /demo`;
- the client can fetch the raw `AgentCard` if needed;
- the client sends blocking and streaming A2A requests;
- the client sends `help` through A2A;
- the server answers `help` through a model-backed Spring AI call inside the A2A executor;
- the same flow is documented in Swagger UI for quick manual testing.

If `OPENROUTER_API_KEY` is not set, the help flow still works and returns a clear fallback message instead of failing the app.

## Source Of Truth

- [Documentation index](docs/README.md)
- [Compatibility](docs/compatibility.md)
- [Contributing](CONTRIBUTING.md)

## How It Works

<p align="center">
  <img
    src="assets/intro.png"
    alt="An A2A client sends protocol requests to Spring A2A, which configures the A2A Java SDK runtime and delegates execution to application-defined AgentCard and AgentExecutor beans"
    width="100%"
  />
</p>

A server application provides two main components:

- `AgentCard` - describes the agent and its capabilities;
- `AgentExecutor` - implements the agent behavior.

Spring A2A configures the protocol infrastructure around them:

```text
A2A Client
    |
    | HTTP + JSON
    v
Spring MVC REST Transport
    |
    v
Spring A2A Auto-Configuration
    |
    v
A2A Java SDK Runtime
    |
    v
AgentCard + AgentExecutor
```

## Getting Started

### Requirements

- Java 17 or later
- Maven 3.9 or later

### Build From Source

```shell
git clone https://github.com/Sh1bari/spring-a2a.git
cd spring-a2a
mvn clean install
```

### Add The REST Server Starter

```xml
<dependency>
    <groupId>io.github.sh1bari</groupId>
    <artifactId>a2a-spring-boot-starter-server-rest</artifactId>
    <version>0.1.2</version>
</dependency>
```

### Define Your Agent

Provide an `AgentCard` bean and an `AgentExecutor` bean:

```java
@Configuration(proxyBeanMethods = false)
class AgentConfiguration {

    @Bean
    AgentCard agentCard() {
        // Describe the agent, its capabilities, skills, and interfaces.
    }

    @Bean
    AgentExecutor agentExecutor() {
        return new MyAgentExecutor();
    }

}
```

Spring Boot detects these beans, configures the A2A runtime, and exposes the REST transport automatically.

The public agent card is available at:

```text
GET /.well-known/agent-card.json
```

### Run The Example

```shell
mvn -pl examples/spring-boot/rest/server -am spring-boot:run
```

The example server starts on port `18080`.

In another terminal, start the client:

```shell
mvn -pl examples/spring-boot/rest/client -am spring-boot:run
```

Then try:

- `GET http://localhost:18081/demo`
- `POST http://localhost:18081/demo/blocking`
- `POST http://localhost:18081/demo/streaming`
- `POST http://localhost:18081/demo/help`
- `POST http://localhost:18081/demo/full-flow`

See the complete [REST server example](examples/spring-boot/rest/server).

## Project Structure

```text
spring-a2a
|-- spring-boot
|   |-- server
|   |   |-- spring-boot-server-autoconfigure
|   |   `-- rest
|   |       |-- spring-boot-server-rest
|   |       |-- spring-boot-starter-server-rest
|   |       |-- spring-boot-server-integration-tests
|   |       `-- spring-boot-server-rest-sut
|   |-- client
|   |   `-- reserved for future Spring Boot client integration
|   |-- jsonrpc
|   |   `-- reserved for future JSON-RPC support
|   `-- grpc
|       `-- reserved for future gRPC support
|-- examples
|   `-- spring-boot
|       |-- rest
|       |   |-- server
|       |   `-- client
|       |-- jsonrpc
|       `-- grpc
|-- docs
`-- scripts
```

The JSON-RPC, gRPC, and Spring Boot client library modules currently reserve the intended project structure and do not yet represent completed integrations.

## Examples

- [REST server example](examples/spring-boot/rest/server) - demonstrates auto-configuration, `AgentCard`, `AgentExecutor`, model-backed help, and streaming task flows.
- [REST client demo](examples/spring-boot/rest/client) - uses the upstream A2A Java SDK client to exercise the REST server and show the full walkthrough.

The current REST client demo is an example application, not yet a Spring Boot client starter.

## Validation

There are two independent checks:

- `Build` verifies compilation and tests on Java 17, 21, and 25.
- `Spring Boot REST TCK` validates the REST server against the official A2A Technology Compatibility Kit.

To run the regular build locally:

```shell
mvn -B verify
```

To run the REST TCK locally:

```shell
bash ./scripts/run-spring-boot-rest-tck.sh
```

## Releases

Spring A2A releases are versioned, signed, and published in two places:

- Maven Central for dependency consumers;
- GitHub Releases for direct downloads of the same library artifacts.

Each release includes:

- the three library jars;
- matching `sources.jar` and `javadoc.jar` artifacts where the module has source code and published API docs;
- SHA-256 checksums for the GitHub Release assets.

Release process:

- build and test the project with `mvn -B verify`;
- publish the three libraries to Maven Central with the release script;
- create a GitHub Release for the same version tag.

Example release headline:

`Spring A2A 0.1.0`

## License

Spring A2A is released under the [Apache License 2.0](LICENSE).
