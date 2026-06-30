# A2A Spring Boot Extract

This repository contains only the Spring Boot integration, Spring Boot examples, and the REST TCK workflow.

## Layout

- `integrations/spring-boot`
  - Spring Boot server/client integration modules.
- `examples/spring-boot`
  - Runnable Spring Boot server/client examples.
- `.github/workflows/run-spring-boot-rest-tck.yml`
  - GitHub Actions workflow for the REST TCK.

## Build

```bash
mvn test
```

## REST TCK

Run the REST TCK locally:

```bash
bash ./scripts/run-spring-boot-rest-tck.sh
```
