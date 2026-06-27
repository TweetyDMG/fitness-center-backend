# Fitness Center Backend — Система управления фитнес-центром

Backend-система управления фитнес-центром на Spring Boot 3.4 (Java 21), предоставляющая REST API для десктопного JavaFX-клиента и интеграций. Поддерживает управление клиентами, тренерами, абонементами, расписанием занятий, посещениями, отчетами и ролевой моделью доступа (CLIENT / TRAINER / MANAGER / ADMIN).

---

## 🛠 Технологический стек

При проектировании архитектуры приложения упор делался на надёжность хранения данных и модульность.

*   **Язык разработки:** Java 21 (LTS)
*   **Фреймворки:** Spring Boot 3.4.5 (Web, Data JPA, Security, Actuator, Validation)
*   **Базы данных:** Microsoft SQL Server 2019+ (JDBC 12.10, Hibernate 6)
*   **Кэширование и очереди:** JWT (jjwt 0.12.6, HS512)
*   **Контейнеризация и DevOps:** Docker, Docker Compose, Maven 3.9+
*   **Инструменты тестирования:** JUnit 5, Mockito, Testcontainers (планируется)

---

## 🚀 Ключевой функционал

Система оцифровывает и автоматизирует следующие бизнес-процессы:

*   **Управление пользователями:** Ролевая модель доступа (CLIENT, TRAINER, MANAGER, ADMIN), JWT-аутентификация (HS512, TTL 24ч), BCrypt
*   **Автоматизация логики:** Полный CRUD для 18 сущностей, гибкий построитель отчетов, экспорт в Excel (Apache POI)
*   **Интеграции:** Swagger UI / OpenAPI 3, JavaFX Desktop UI, JPA Specifications (динамические фильтры)
*   **Валидация и безопасность:** Spring Security 6, JWT фильтр (OncePerRequestFilter), ролевой доступ

---

## 📁 Архитектура и структура проекта

В проекте используется слоистая архитектура (Controller → Service → Repository → Entity). Это обеспечивает независимость бизнес-логики от внешних библиотек и баз данных.

```text
fitness-center-backend/
├── pom.xml                          # Maven конфигурация (Java 21, Spring Boot 3.4)
├── Dockerfile                       # Multi-stage Docker build
├── docker-compose.yml               # MSSQL + Backend
├── .env.example                     # Шаблон переменных окружения
├── src/
│   ├── main/
│   │   ├── java/com/fitnesscenter/
│   │   │   ├── FitnessCenterApplication.java      # Spring Boot entry point
│   │   │   ├── JavaFxApplication.java             # JavaFX entry point
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java            # JWT, BCrypt, Role-based access
│   │   │   │   ├── OpenApiConfig.java             # Swagger UI configuration
│   │   │   │   └── JwtAuthenticationFilter.java   # JWT filter
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java            # /api/auth/**
│   │   │   │   ├── AdminController.java           # /api/admin/**
│   │   │   │   ├── ClientController.java          # /api/client/**
│   │   │   │   ├── TrainerController.java         # /api/trainer/**
│   │   │   │   ├── ManagerController.java         # /api/manager/**
│   │   │   │   ├── ReportController.java          # /api/reports/**
│   │   │   │   └── dto/                           # Request/Response DTO
│   │   │   ├── service/                           # Бизнес-логика (10+ сервисов)
│   │   │   ├── repository/                        # 18 JPA Repositories
│   │   │   ├── entity/                            # 18 JPA Entities
│   │   │   ├── dto/                               # DTO для отчетов и запросов
│   │   │   ├── exception/                         # GlobalExceptionHandler
│   │   │   └── ui/                                # JavaFX Controllers (20+)
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-example.properties
│   │       ├── ui/                                # FXML views + style.css
│   │       └── images/
│   └── test/
├── .gitignore
└── README.md
```

### ER-диаграмма (ключевые связи)

```mermaid
erDiagram
    USER ||--o| CLIENT : "1:0..1 (CLIENT role)"
    USER ||--o| TRAINER : "1:0..1 (TRAINER role)"
    CLIENT }|--o{ SALE : "покупает"
    SUBSCRIPTION }|--o{ SALE : "тип абонемента"
    DISCOUNT }|--o{ SALE : "применяется"
    TRAINER }|--o{ SCHEDULE : "ведет занятия"
    FITNESS_SERVICE }|--o{ SCHEDULE : "услуга"
    CLIENT }|--o{ REGISTRATION_OF_VISIT : "посещает"
    SCHEDULE }|--o{ REGISTRATION_OF_VISIT : "запись на занятие"
```

---

## 💻 Локальное развертывание

Для запуска проекта в изолированном окружении вам понадобятся **Docker** и **Docker Compose**, либо JDK 21 и Maven 3.9+.

### 1. Клонирование репозитория

```bash
git clone https://github.com/<ваш-username>/fitness-center-backend.git
cd fitness-center-backend
```

### 2. Настройка переменных окружения

Создайте файл `.env` в корневой директории проекта по образцу `.env.example`:

```env
JWT_SECRET=your_512_bit_base64_secret
```

Также скопируйте пример конфигурации:

```bash
cp src/main/resources/application-example.properties src/main/resources/application-local.properties
```

### 3. Запуск инфраструктуры

Сборка образов и запуск контейнеров приложения, базы данных и сопутствующих сервисов в фоновом режиме:

```bash
export JWT_SECRET=$(openssl rand -base64 64)
docker-compose up --build -d
```

Backend будет доступен по адресу: `http://localhost:8080`

### 4. Ручной запуск Backend

```bash
mvn clean package -DskipTests
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

---

## 🔌 Примеры эндпоинтов API

Полная интерактивная документация (Swagger / OpenAPI) доступна при локальном запуске по адресу: `http://localhost:8080/swagger-ui.html`

| Метод | Эндпоинт | Описание | Доступ |
| --- | --- | --- | --- |
| `POST` | `/api/auth/login` | Авторизация, возврат JWT | Public |
| `POST` | `/api/auth/register` | Регистрация нового пользователя | Public |
| `GET` | `/api/client/profile` | Профиль клиента | `ROLE_CLIENT` |
| `GET` | `/api/trainer/clients` | Мои клиенты | `ROLE_TRAINER` |
| `GET` | `/api/manager/sales` | Продажи (с фильтрами) | `ROLE_MANAGER` |
| `GET` | `/api/admin/users` | Все пользователи | `ROLE_ADMIN` |
| `POST` | `/api/reports/generate` | Генерация отчета (Excel) | `ROLE_MANAGER`, `ROLE_ADMIN` |

### Пример запроса (curl)

```bash
# Логин
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"ivan","password":"secret123"}'

# Авторизованный запрос
curl -X GET http://localhost:8080/api/client/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

---

## 👥 Разработчики

* [**Артем Рогачев**](https://github.com/TweetyDMG) — Backend Developer

## 📜 Лицензия

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Проект распространяется на условиях лицензии **MIT**. Полный текст лицензии находится в файле [LICENSE](./LICENSE).
