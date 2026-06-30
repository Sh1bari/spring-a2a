# База для issue по Spring Boot integration

Этот документ собирает в одном месте:

- правила для контрибьюторов из репозитория;
- текущее состояние Spring Boot integration;
- что уже реализовано;
- как это собирать и проверять;
- что важно указать в issue, если вы хотите открыть задачу на доработку.

Документ составлен по текущим `README.md`, `CONTRIBUTING.md`, `CONTRIBUTING_INTEGRATIONS.md`, `AGENTS.md`, workflow-файлам и README внутри `integrations/spring-boot` и `examples/spring-boot`.

## 1. Общие правила контрибьютора

### Базовый процесс

- Нужно форкнуть `a2a-java`, клонировать форк и работать в отдельной ветке.
- Для каждой issue рекомендуется отдельная ветка.
- Лучше использовать название ветки по номеру issue, например `issue-20`.
- PR открывается в `main`.
- Для PR желательно иметь понятное описание и ссылку на issue.

### Требования к PR

- В PR должны быть тесты для добавляемой функциональности.
- В PR должна быть документация для новой функциональности.
- Коммиты должны следовать Conventional Commits.
- Если commit/PR связан с GitHub issue, в сообщении коммита нужно добавить `Fixes #N` в конце.
- Все изменения должны проходить code review.

### Требования к интеграциям

Чтобы добавить новую интеграцию в список интеграций репозитория, нужно:

- открыть PR, который добавит ссылку на проект в основной `README.md`;
- чтобы страница проекта содержала минимум:
  - как использовать интеграцию;
  - желательно пример использования;
  - тесты, которые расширяют `AbstractA2AServerTest`;
  - прохождение A2A TCK;
  - если Maven Central невозможен, то понятную инструкцию по сборке;
- если какие-то пункты пока не выполнены, это нужно явно указать в PR.

### Локальная разработка

- Требуется Java 17+.
- Базовая команда сборки всего проекта:

```bash
mvn clean install
```

- Тесты в этом репозитории по умолчанию пишут вывод в файлы.

## 2. Что уже есть в Spring Boot integration

### Общая структура

Сейчас Spring Boot integration выделена в отдельный модульный деревья:

- `integrations/spring-boot/`
  - корневой aggregator;
- `integrations/spring-boot/server/`
  - server-side tree;
- `integrations/spring-boot/server/rest/`
  - REST/MVC ветка;
- `integrations/spring-boot/server/jsonrpc/`
  - заготовка под будущий JSON-RPC tree;
- `integrations/spring-boot/server/grpc/`
  - заготовка под будущий gRPC tree;
- `integrations/spring-boot/client/`
  - зарезервировано под будущую client-ветку.

### Server-side модули

В server tree сейчас есть:

- `spring-boot-server-autoconfigure`
  - общий runtime auto-configuration;
  - не зависит от Servlet API;
  - поднимает runtime-beans для A2A;
- `spring-boot-server-rest`
  - Servlet / Spring MVC adapter;
  - controller + response mapper + servlet auto-configuration;
- `spring-boot-starter-server-rest`
  - dependency-only starter;
  - тянет runtime + webmvc + `spring-boot-starter-web`;
- `spring-boot-server-integration-tests`
  - интеграционные тесты всей REST integration;
- `spring-boot-server-rest-sut`
  - runnable SUT для внешнего A2A TCK.

### Artifacts

Текущие artifactId в server REST ветке:

- `a2a-java-sdk-spring-boot-server-autoconfigure`
- `a2a-java-sdk-spring-boot-server-rest`
- `a2a-java-sdk-spring-boot-starter-server-rest`
- `a2a-java-sdk-spring-boot-server-integration-tests`
- `a2a-java-sdk-spring-boot-server-rest-sut`

### Package layout

Пакеты приведены к server-oriented структуре:

- `io.github.sh1bari.springa2a.integrations.springboot.server.autoconfigure`
- `io.github.sh1bari.springa2a.integrations.springboot.server.rest`
- `io.github.sh1bari.springa2a.integrations.springboot.server.tck.rest`

### Конфигурация

Общая runtime-конфигурация читается через `a2a.*` properties.

Пример YAML:

```yaml
a2a:
  executor:
    core-pool-size: 5
    max-pool-size: 50
    keep-alive-seconds: 60
    queue-capacity: 100
  blocking:
    agent-timeout-seconds: 30
    consumption-timeout-seconds: 5
  agent-card:
    cache:
      max-age: 3600
```

## 3. Что уже реализовано в REST server integration

### Runtime auto-configuration

Общий runtime auto-configuration:

- создаёт runtime beans для A2A server;
- работает без Servlet-зависимостей;
- используется и в web, и в non-web Spring Boot приложениях;
- регистрируется через `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.

Ожидаемые runtime-beans:

- `DefaultValuesConfigProvider`
- `A2AConfigProvider`
- `TaskStore`
- `MainEventBus`
- `QueueManager`
- `MainEventBusProcessor`
- `PushNotificationConfigStore`
- `PushNotificationSender`
- internal executor beans
- `RequestHandler`
- thread factories

### REST transport adapter

REST transport layer включает:

- `A2AServletWebAutoConfiguration`
- `A2ASpringBootHttpResponseMapper`
- `A2ASpringBootMvcController`
- `A2ASpringBootMvcExceptionHandler`
- `A2APushNotificationConfigRequestMapper`
- `StreamingSubscriptionObserver`

### MVC / HTTP endpoints

Публичные endpoints, которые сейчас ожидаются в REST example/server:

- `/.well-known/agent-card.json`
- `POST /message:send`
- `POST /message:stream`
- `GET /tasks/{taskId}`
- `GET /tasks`
- `POST /tasks/{taskId}:cancel`
- `POST /tasks/{taskId}:subscribe`
- `POST /tasks/{taskId}/pushNotificationConfigs`
- `GET /tasks/{taskId}/pushNotificationConfigs/{configId}`
- `GET /tasks/{taskId}/pushNotificationConfigs`
- `DELETE /tasks/{taskId}/pushNotificationConfigs/{configId}`
- `GET /extendedAgentCard`

Также в примерах и сервере поддерживаются tenant-prefixed варианты для transport endpoints.

### Required application beans

Чтобы server integration работала, приложение должно предоставить:

- `AgentCard`
- `AgentExecutor`

### Override points

Пользовательские бины могут переопределить:

- `TaskStore`
- `PushNotificationConfigStore`
- `PushNotificationSender`
- `RequestHandler`
- `A2ASpringBootHttpResponseMapper`
- `A2ASpringBootMvcController`

## 4. Что важно для HTTP contract

### TCK / contract focus

Для REST SUT и примеров важно подтверждать:

- `/.well-known/agent-card.json` доступен;
- `POST /message:send` работает;
- `POST /message:stream` работает;
- task endpoints работают;
- push notification config endpoints работают;
- `GET /extendedAgentCard` работает;
- REST transport не ломает JSON contract.

### Точки, которые нужно явно проверять в issue

Если issue касается REST contract, в ней стоит явно указать:

- какой endpoint меняется;
- какой DTO участвует;
- какой статус-код ожидается;
- какой content type ожидается;
- требуется ли tenant-prefixed форма;
- должен ли endpoint быть доступен в non-web приложении или только в Servlet web app;
- нужна ли проверка `AgentExecutor` / `AgentCard` bean presence;
- нужно ли это покрыть TCK.

## 5. TCK и CI

### Что уже есть

Для REST SUT есть:

- отдельный runnable SUT module;
- отдельный README;
- отдельный checked-in helper script;
- отдельный GitHub Actions workflow.

### Run script

Единый локальный и CI-friendly запуск:

```bash
bash ./scripts/run-spring-boot-rest-tck.sh
```

Скрипт:

1. стартует REST SUT;
2. ждёт `http://localhost:9999/.well-known/agent-card.json`;
3. запускает внешний `a2a-tck`;
4. пишет логи;
5. сам останавливает SUT при выходе.

### Workflow

Workflow лежит здесь:

```text
.github/workflows/run-spring-boot-rest-tck.yml
```

Он:

- собирает `integrations/spring-boot/server`;
- checkout'ит внешний `a2a-tck`;
- запускает helper script;
- сохраняет артефакты с логами.

### Local commands

Сборка server tree:

```bash
mvn -pl integrations/spring-boot/server -am test
```

Запуск только REST SUT:

```bash
mvn -pl integrations/spring-boot/server/rest/spring-boot-server-rest-sut -am spring-boot:run
```

Полный TCK run:

```bash
bash ./scripts/run-spring-boot-rest-tck.sh
```

## 6. Что есть в examples

### Spring Boot examples tree

В `examples/spring-boot/` сейчас есть transport-first структура:

- `rest/server`
- `rest/client`
- `jsonrpc/server`
- `jsonrpc/client`
- `grpc/server`
- `grpc/client`

### REST demo

REST example уже показывает:

- серверный A2A flow;
- отдельный `AgentExecutor` bean;
- client-side demo app;
- Swagger UI для client demo;
- полный сценарий `agent-card -> blocking -> streaming`.

### Что показывает server example

`examples/spring-boot/rest/server` демонстрирует:

- как поднять Spring Boot REST server;
- как задать `AgentCard`;
- как реализовать `AgentExecutor` как отдельный Spring component;
- как сервер отвечает на `hello` и `stream`;
- какие endpoints доступны.

### Что показывает client example

`examples/spring-boot/rest/client` демонстрирует:

- как клиент забирает `AgentCard`;
- как он запускает blocking flow;
- как он запускает streaming flow;
- как посмотреть и повторить сценарии через Swagger UI;
- как работает полный end-to-end сценарий.

### Build commands for examples

```bash
mvn -pl examples/spring-boot/rest/server -am spring-boot:run
mvn -pl examples/spring-boot/rest/client -am spring-boot:run
```

## 7. Как описывать будущую issue

Если открываешь issue на Spring Boot integration, полезно указать:

- какой модуль меняется;
- это runtime, REST adapter, starter, example или TCK SUT;
- это server-only или касается examples тоже;
- это только REST или с перспективой на jsonrpc/grpc;
- какой контракт должен остаться неизменным;
- какие тесты нужно добавить или обновить;
- нужен ли `AbstractA2AServerTest`;
- нужен ли TCK;
- как проверить руками;
- какие команды запуска использовать;
- должен ли быть отдельный README / workflow / script.

### Хорошая структура issue

1. Цель.
2. Текущее поведение.
3. Ожидаемое поведение.
4. Где смотреть в коде.
5. Какие тесты должны пройти.
6. Как проверить руками.
7. Какие артефакты должны быть обновлены.

## 8. Уже подтвержденные build commands

Для текущей Spring Boot server ветки:

```bash
mvn -pl integrations/spring-boot/server -am test
```

Для REST aggregator:

```bash
mvn -pl integrations/spring-boot/server/rest -am test
```

Для REST SUT:

```bash
mvn -pl integrations/spring-boot/server/rest/spring-boot-server-rest-sut -am spring-boot:run
```

Для полного TCK сценария:

```bash
bash ./scripts/run-spring-boot-rest-tck.sh
```

## 9. Краткий список файлов, на которые обычно стоит смотреть

- `CONTRIBUTING.md`
- `CONTRIBUTING_INTEGRATIONS.md`
- `integrations/spring-boot/README.md`
- `integrations/spring-boot/server/README.md`
- `integrations/spring-boot/server/rest/README.md`
- `integrations/spring-boot/server/rest/TCK.md`
- `integrations/spring-boot/server/rest/spring-boot-server-rest-sut/README.md`
- `examples/spring-boot/README.md`
- `examples/spring-boot/rest/README.md`
- `examples/spring-boot/rest/server/README.md`
- `examples/spring-boot/rest/client/README.md`
- `.github/workflows/run-spring-boot-rest-tck.yml`
- `scripts/run-spring-boot-rest-tck.sh`

## 10. Что можно писать в issue как итог

Если задача про Spring Boot integration, обычно формулировка должна содержать:

- какой модуль;
- какой endpoint / bean / transport / example;
- что считается done;
- какие README и workflow должны быть обновлены;
- как подтверждается TCK;
- как подтверждается `AbstractA2AServerTest` / integration coverage;
- как пользователь должен запускать всё локально.
