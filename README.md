# Spring A2A

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

## Source Of Truth

- [Documentation index](docs/README.md)
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
    <version>0.1.0-SNAPSHOT</version>
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

- [REST server example](examples/spring-boot/rest/server) - demonstrates auto-configuration, `AgentCard`, `AgentExecutor`, direct responses, and streaming task flows.
- [REST client demo](examples/spring-boot/rest/client) - uses the upstream A2A Java SDK client to exercise the REST server.

The current REST client demo is an example application, not yet a Spring Boot client starter.

## Validation

The REST server integration is validated with automated tests and the official A2A REST Technology Compatibility Kit.

```shell
bash ./scripts/run-spring-boot-rest-tck.sh
```

## License

Spring A2A is released under the [Apache License 2.0](LICENSE).
