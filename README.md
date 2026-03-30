# Task Catalog

REST сервис для управления задачами, реализованный на Kotlin + Spring Boot + Reactor + JdbcClient + native SQL.

## Что реализовано

- создание задачи
- получение задачи по id
- список задач с пагинацией и опциональной фильтрацией по статусу
- обновление только статуса задачи
- удаление задачи
- централизованная обработка ошибок через `@RestControllerAdvice`
- валидация входных данных
- миграции через Flyway
- unit tests для service и controller
- integration tests для repository

## Стек

- Kotlin
- Spring Boot 4.0.5
- Spring WebFlux
- Project Reactor (`Mono`, `Flux`-совместимый сервисный слой)
- Spring JDBC `JdbcClient`
- H2
- Flyway
- JUnit 5

## Архитектура

```text
src/main/kotlin/com/example/taskcatalog
├── controller
├── dto
├── exception
├── mapper
├── model
├── repository
└── service
```

## Почему WebFlux + JdbcClient

`JdbcClient` работает поверх JDBC и остаётся блокирующим API. Поэтому reactive-подход здесь реализован в сервисном слое: блокирующие repository-вызовы оборачиваются в `Mono.fromCallable { ... }` / `Mono.fromRunnable { ... }` и выполняются на `Schedulers.boundedElastic()`.

Это позволяет:

- соблюдать требование ТЗ по Reactor
- не блокировать event-loop WebFlux
- сохранить простой и читаемый JDBC repository слой

## Запуск

Нужна Java 17+ и Gradle 8.14+.

```bash
./gradlew bootRun
```

или

```bash
gradle bootRun
```

После запуска приложение доступно на `http://localhost:8080`.

## Запуск тестов

```bash
./gradlew test
```

или

```bash
gradle test
```

## API

### 1. Создать задачу

`POST /api/tasks`

Request:

```json
{
  "title": "Prepare report",
  "description": "Monthly financial report"
}
```

### 2. Получить список задач

`GET /api/tasks?page=0&size=10&status=NEW`

- `page` — обязательный
- `size` — обязательный
- `status` — опциональный
- сортировка по `created_at desc`

### 3. Получить задачу по id

`GET /api/tasks/{id}`

### 4. Обновить статус

`PATCH /api/tasks/{id}/status`

Request:

```json
{
  "status": "DONE"
}
```

### 5. Удалить задачу

`DELETE /api/tasks/{id}`

## Примеры curl

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Prepare report","description":"Monthly financial report"}'
```

```bash
curl "http://localhost:8080/api/tasks?page=0&size=10&status=NEW"
```

```bash
curl http://localhost:8080/api/tasks/1
```

```bash
curl -X PATCH http://localhost:8080/api/tasks/1/status \
  -H "Content-Type: application/json" \
  -d '{"status":"DONE"}'
```

```bash
curl -X DELETE http://localhost:8080/api/tasks/1
```

## SQL-подход

Repository использует только `JdbcClient` и native SQL. ORM/JPA/Hibernate не используются.

Основные запросы:

- `insert into tasks ...`
- `select ... from tasks where id = :id`
- `select ... from tasks where status = :status order by created_at desc limit :limit offset :offset`
- `update tasks set status = :status, updated_at = :updatedAt where id = :id`
- `delete from tasks where id = :id`

## Замечание по Gradle Wrapper

В архиве сохранён полноценный исходный код проекта. Если в вашей среде нет Gradle Wrapper, можно либо:

- сгенерировать его командой `gradle wrapper` в корне проекта,
- либо открыть проект в IntelliJ IDEA и запустить Gradle tasks из IDE.


## Примечание по IDE

В IntelliJ IDEA может отображаться предупреждение вида:

Cannot access class 'org.mockito.Answers'. Check your module classpath for missing or conflicting dependencies

В данном проекте это является IDE false positive, так как:

- проект успешно собирается через Gradle
- тесты проходят
- приложение запускается корректно
