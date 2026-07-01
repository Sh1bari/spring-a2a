# Spring Boot REST Example

This page is the entry point for the runnable REST example set.

It currently includes:

- a server example that exposes the A2A REST transport;
- a client example that renders the browser guide and playground;
- live callback handling for push notification demos.

## REST Example Docs

- [Server example](server/README.md)
- [Client example](client/README.md)
- [Example overview](../../../docs/spring-boot-rest-example.md)
- [REST contract](../../../docs/spring-boot-rest-contract.md)

## What This Example Is For

The REST example is intentionally practical:

- it shows how the client discovers the server;
- it shows how a user message becomes a task or a direct reply;
- it shows how streaming, subscription, and push callbacks work;
- it keeps the demo logic isolated from the library modules.

The detailed browser workflow lives in the client README.
