# Task Manager API

REST API для создания и управления задачами. Приложение разработано на Java 21 и Spring Boot, использует Spring MVC, Spring Data JPA и H2.

## Возможности

- Создание задачи со статусом `NEW`.
- Получение списка задач и задачи по ID.
- Изменение статуса задачи.
- Удаление задачи.
- Валидация входных данных и единый формат ошибок.

## Требования

- JDK 21
- Maven 3.9+ (или Maven Wrapper из репозитория)

## Запуск

Из корневой папки проекта выполните:

```powershell
.\mvnw.cmd spring-boot:run
```

Сервер запустится на порту `8081`. Для изменения порта задайте `server.port` в `src/main/resources/application.properties`.

Сборка и запуск тестов:

```powershell
.\mvnw.cmd test
```

## База данных

По умолчанию приложение использует файловую H2-базу данных `./data/taskdb`. H2 создаёт базу автоматически; данные сохраняются между перезапусками.

Параметры по умолчанию:

```properties
spring.datasource.url=jdbc:h2:file:./data/taskdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
server.port=8081
```
## API

Базовый URL: `http://localhost:8081/api/tasks`

| Метод | Путь | Описание | Ответ |
|---|---|---|---|
| `POST` | `/api/tasks` | Создать задачу | `201 Created` |
| `GET` | `/api/tasks` | Получить все задачи | `200 OK` |
| `GET` | `/api/tasks/{id}` | Получить задачу по ID | `200 OK` |
| `PATCH` | `/api/tasks/{id}/status` | Изменить статус | `200 OK` |
| `DELETE` | `/api/tasks/{id}` | Удалить задачу | `204 No Content` |

### Создать задачу

```http
POST http://localhost:8081/api/tasks
```

```json
{
  "title": "Подготовить README",
  "description": "Добавить инструкцию по запуску проекта"
}
```

`title` обязателен и должен содержать от 3 до 100 символов. `description` необязателен, максимум 500 символов. Новая задача автоматически получает статус `NEW` и дату создания.

Пример ответа:

```json
{
  "id": 1,
  "title": "Подготовить README",
  "description": "Добавить инструкцию по запуску проекта",
  "status": "NEW",
  "createdAt": "2026-09-29T19:00:00"
}
```

### Получить задачи

```http
GET http://localhost:8081/api/tasks
GET http://localhost:8081/api/tasks/1
```

### Изменить статус

Допустимые значения: `NEW`, `IN_PROGRESS`, `DONE`.

```http
PATCH http://localhost:8081/api/tasks/1/status
```

```json
{
  "status": "IN_PROGRESS"
}
```

### Удалить задачу

```http
DELETE http://localhost:8081/api/tasks/1
```

## Обработка ошибок

- `400 Bad Request` — невалидные данные или некорректное тело запроса.
- `404 Not Found` — задача с указанным ID не найдена.

Пример ответа при ошибке:

```json
{
  "timestamp": "2026-09-29T19:00:00",
  "status": 400,
  "error": "Validation failed",
  "fields": {
    "title": "Название должно содержать от 3 до 100 символов"
  }
}
```
