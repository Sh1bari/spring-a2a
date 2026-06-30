# A2A Spring Boot

Spring Boot integration and runnable examples for the Agent2Agent Protocol (A2A).

## Source Of Truth

- [Documentation index](docs/README.md)
- [Contributing](CONTRIBUTING.md)

## What Is Here

| Path | Purpose |
| --- | --- |
| `spring-boot/` | Library modules for the Spring Boot integration surface. |
| `examples/spring-boot/` | Runnable REST example applications. |
| `.github/workflows/run-spring-boot-rest-tck.yml` | CI workflow for the REST TCK flow. |

## Quick Start

Prerequisites:

- JDK 21
- Maven 3.9+

Build and test the full reactor:

```bash
mvn test
```

Validate Spring Java Format:

```bash
mvn spring-javaformat:validate
```

Apply Spring Java Format:

```bash
mvn spring-javaformat:apply
```

## Versioning

The repository uses two version controls:

- `revision` for all modules published from this repository
- `a2a.sdk.version` for the upstream `org.a2aproject.sdk` dependencies

This keeps the repository release train independent from the upstream SDK version.

## Using The Starter

For applications that expose the REST transport, depend on the starter:

```xml
<dependency>
    <groupId>io.github.sh1bari</groupId>
    <artifactId>a2a-spring-boot-starter-server-rest</artifactId>
</dependency>
```

## REST TCK

Run the REST TCK locally:

```bash
bash ./scripts/run-spring-boot-rest-tck.sh
```

Run only the SUT if you want to exercise the server manually:

```bash
mvn -pl spring-boot/server/rest/spring-boot-server-rest-sut -am spring-boot:run
```

The SUT listens on `http://localhost:9999` by default.

## License

This project is licensed under the Apache License 2.0. See [LICENSE](LICENSE).
