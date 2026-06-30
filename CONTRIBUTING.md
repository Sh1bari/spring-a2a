# Contributing

This repository follows the Spring Java Format toolchain and Spring contribution conventions as closely as possible.

## Sources Of Truth

- [Spring Java Format](docs/spring-javaformat.md)

## Local Rules

- Run `mvn spring-javaformat:validate` before opening a PR.
- Run `mvn spring-javaformat:apply` before committing Java changes.
- Keep new public classes documented with Javadoc.
- Keep new Java sources in Spring-style auto-configuration and component patterns.
- Prefer small, focused commits and tests that cover the changed behavior.

## Repository Expectations

- The Spring Boot integration modules are library code, not application code.
- Auto-configuration should remain conditional and override-friendly.
- Public API changes should preserve the existing contract unless the change is explicitly intentional.

## PR Checklist

- Formatting passes.
- Tests cover the change.
- README or module docs are updated when behavior changes.
