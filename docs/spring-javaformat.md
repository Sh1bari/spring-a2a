# Spring Java Format

This repository uses Spring Java Format for Java source formatting.

## What To Run

From the repository root:

```powershell
mvn spring-javaformat:validate
mvn spring-javaformat:apply
```

If Maven is started from another directory, use the repo root explicitly:

```powershell
mvn -f <repo-root>/pom.xml spring-javaformat:validate
mvn -f <repo-root>/pom.xml spring-javaformat:apply
```

## IntelliJ

You do not need a plugin for the build to work.

- Use the repository root of your local checkout.
- Run the commands above from the root when you want the Spring formatter to update files.
- If you have a Spring Java Format plugin available in your IDE, keep it aligned with the commands above.

## IntelliJ Settings

For the closest match to Spring formatting without a plugin:

- `Settings` -> `Editor` -> `Code Style` -> `General`
  - `Use tab character` enabled.
  - `Right margin` set to `120`.
- `Settings` -> `Editor` -> `Code Style` -> `Java`
  - `Tabs and Indents`
    - `Tab size = 4`
    - `Indent = 4`
    - `Continuation indent = 4`
    - `Use tab character` enabled
  - `Wrapping and Braces`
    - leave defaults unless the formatter output shows a mismatch
- `Settings` -> `Editor` -> `Code Style` -> `Imports`
  - set star-import thresholds very high so imports stay explicit
  - enable optimizing imports only if you are happy with IntelliJ's import ordering

## Files

- `.springjavaformatconfig`
  - Spring Java Format IDE and CLI configuration.

## Workflow

1. Make code changes.
2. Run `mvn spring-javaformat:apply` from the root.
3. Run `mvn spring-javaformat:validate` from the root.
4. Run the relevant tests.

## Spring References

- Spring Framework code style: https://github.com/spring-projects/spring-framework/wiki/Code-Style
- Spring Framework contributing guide: https://raw.githubusercontent.com/spring-projects/spring-framework/main/CONTRIBUTING.md
- Spring Boot contributing guide: https://raw.githubusercontent.com/spring-projects/spring-boot/main/CONTRIBUTING.adoc
- Spring Java Format: https://github.com/spring-io/spring-javaformat
