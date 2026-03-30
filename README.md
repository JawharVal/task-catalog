
# Task Catalog

REST-сервис для управления задачами, реализованный на Kotlin, Spring Boot, Reactor, JdbcClient и native SQL.

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
- Spring Boot 3.4.4
- Spring WebFlux
- Project Reactor (`Mono`, `Flux`)
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
````

## Почему WebFlux + JdbcClient

`JdbcClient` работает поверх JDBC и остаётся блокирующим API. Поэтому reactive-подход реализован в сервисном слое: блокирующие repository-вызовы оборачиваются в Reactor-типы и выполняются на `Schedulers.boundedElastic()`.

Это позволяет:

* соблюдать требование ТЗ по Reactor
* не блокировать event-loop WebFlux
* сохранить простой и читаемый JDBC repository слой

## Запуск

Нужна Java 21.

### Запуск приложения

```bash
./gradlew bootRun
```

Для Windows PowerShell:

```powershell
.\gradlew.bat bootRun
```

После запуска приложение доступно на `http://localhost:8080`.

### Запуск тестов

```bash
./gradlew test
```

Для Windows PowerShell:

```powershell
.\gradlew.bat test
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

* `page` — обязательный
* `size` — обязательный
* `status` — опциональный
* сортировка по `created_at desc`

### 3. Получить задачу по id

`GET /api/tasks/{id}`

Если задача не найдена, возвращается `404 Not Found`.

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

Возвращает `204 No Content`.

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

* `insert into tasks ...`
* `select ... from tasks where id = :id`
* `select ... from tasks where status = :status order by created_at desc limit :limit offset :offset`
* `update tasks set status = :status, updated_at = :updatedAt where id = :id`
* `delete from tasks where id = :id`

## Тесты

Покрыты основные сценарии:

### Service

* успешное создание задачи
* получение задачи по id
* ошибка при отсутствии задачи
* обновление статуса
* удаление задачи
* получение списка задач с фильтрацией и пагинацией

### Controller

* корректные HTTP-статусы
* валидация входных данных
* `404` для отсутствующей задачи

### Repository

Repository покрыт интеграционными тестами с использованием H2.

## Примечание по IDE

В IntelliJ IDEA может отображаться предупреждение вида:

> Cannot access class 'org.mockito.Answers'. Check your module classpath for missing or conflicting dependencies

В данном проекте это является IDE false positive, так как:

* проект успешно собирается через Gradle
* тесты проходят
* приложение запускается корректно

Источник истины для проверки проекта - результат команд:

```powershell
.\gradlew.bat clean test
.\gradlew.bat bootRun
```


