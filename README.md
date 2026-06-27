# Fitness Center Backend

**Backend-система управления фитнес-центром** на Spring Boot 3.4 (Java 21), предоставляющая REST API для десктопного JavaFX-клиента и интеграций. Поддерживает управление клиентами, тренерами, абонементами, расписанием занятий, посещениями, отчетами и ролевой моделью доступа (CLIENT / TRAINER / MANAGER / ADMIN).

---

## 🛠 Технологический стек

| Категория | Технологии |
|-----------|------------|
| **Язык / Runtime** | Java 21 (LTS), Maven 3.9+ |
| **Фреймворк** | Spring Boot 3.4.5 (Web, Data JPA, Security, Actuator, Validation) |
| **База данных** | Microsoft SQL Server 2019+ (JDBC 12.10, Hibernate 6, DDL: `validate`) |
| **Безопасность** | Spring Security 6, JWT (jjwt 0.12.6, HS512), BCrypt |
| **API Документация** | SpringDoc OpenAPI 3 (Swagger UI) |
| **Отчеты / Export** | Apache POI 5.4 (Excel .xlsx) |
| **Desktop UI** | JavaFX 24 (FXML + CSS), внедряется в Spring Context |
| **Архитектура** | Layered: Controller → Service → Repository → Entity (JPA) |
| **Спецификации** | JPA Specifications (динамические фильтры для Client/Sale) |
| **Логирование** | SLF4J + Logback (Spring Boot defaults) |

---

## 🚀 Ключевой функционал

### 🔐 Авторизация и роли
- **JWT-токены** (HS512, TTL 24ч) — stateless аутентификация
- **4 роли**: `CLIENT`, `TRAINER`, `MANAGER`, `ADMIN`
- Регистрация с привязкой к роли, логин/пароль (BCrypt)
- Эндпоинт `/api/auth/me` — текущий пользователь

### 👥 Управление сущностями (Admin API)
Полный CRUD для:
- Пользователи (`User`) и привязка к профилям
- Клиенты (`Client`) — анкета, паспорт, контакты, предпочтения, заболевания
- Тренеры (`Trainer`) — специализация, расписание
- Фитнес-центры (`FitnessCenter`)
- Услуги (`FitnessService`) и история цен (`PriceHistory`)
- Абонементы (`Subscription`) и продажи (`Sale`)
- Расписание (`Schedule`) — занятия тренеров
- Посещения (`RegistrationOfVisit`)
- Скидки (`Discount`), заболевания (`Disease`/`DiseaseType`), предпочтения (`Preference`/`PreferenceType`)
- Рекомендации (`Recommendation`)

### 🎯 Клиентский API (`/api/client/**`)
- Профиль, абонементы, история посещений
- Предпочтения и заболевания (медицинские справки)
- Доступное расписание занятий

### 🏋️ Тренерский API (`/api/trainer/**`)
- Дашборд: мои клиенты, мое расписание, посещения
- Отметка посещений, создание рекомендаций
- Отчеты по своим клиентам

### 📊 Менеджерский API (`/api/manager/**`)
- Управление продажами, клиентами, тренерами
- Финансовые отчеты, экспорт в Excel

### 📈 Отчетность (`/api/reports/**`)
- Гибкий построитель отчетов (DTO `ReportConfigDto`)
- Выборка полей, фильтры, группировки, экспорт в Excel (Apache POI)

---

## 📁 Архитектура и структура проекта

```
fitness-center-backend/
├── pom.xml                          # Maven конфигурация (Java 21, Spring Boot 3.4)
├── Dockerfile                       # Multi-stage Docker build
├── docker-compose.yml               # MSSQL + Backend
├── src/
│   ├── main/
│   │   ├── java/com/fitnesscenter/
│   │   │   ├── FitnessCenterApplication.java      # Spring Boot entry point (Backend)
│   │   │   ├── JavaFxApplication.java             # JavaFX entry point (Desktop UI)
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java            # JWT, BCrypt, Role-based access
│   │   │   │   ├── OpenApiConfig.java             # Swagger UI configuration
│   │   │   │   └── JwtAuthenticationFilter.java   # JWT filter (OncePerRequestFilter)
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java            # /api/auth/** — login, register, me
│   │   │   │   ├── AdminController.java           # /api/admin/** — CRUD все сущности
│   │   │   │   ├── ClientController.java          # /api/client/** — клиентский профиль
│   │   │   │   ├── TrainerController.java         # /api/trainer/** — тренерский функционал
│   │   │   │   ├── ManagerController.java         # /api/manager/** — менеджерский функционал
│   │   │   │   ├── ReportController.java          # /api/reports/** — отчетность
│   │   │   │   └── dto/                           # Request/Response DTO
│   │   │   │       ├── RegisterRequest.java
│   │   │   │       ├── LoginRequest.java
│   │   │   │       ├── UpdateRequest.java
│   │   │   │       └── SaleRequest.java
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java               # Регистрация, логин, JWT генерация
│   │   │   │   ├── AdminService.java              # Бизнес-логика админки
│   │   │   │   ├── ClientService.java             # Клиентский функционал
│   │   │   │   ├── TrainerService.java            # Тренерский функционал
│   │   │   │   ├── ManagerService.java            # Менеджерский функционал
│   │   │   │   ├── ReportService.java             # Генерация отчетов + Excel
│   │   │   │   ├── UserDetailsServiceImpl.java    # Spring Security UserDetailsService
│   │   │   │   ├── ClientSpecifications.java      # JPA Specs для фильтрации клиентов
│   │   │   │   └── SaleSpecifications.java        # JPA Specs для фильтрации продаж
│   │   │   │
│   │   │   ├── repository/                        # 18 JPA Repositories (extends JpaRepository)
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── ClientRepository.java
│   │   │   │   ├── TrainerRepository.java
│   │   │   │   ├── FitnessCenterRepository.java
│   │   │   │   ├── SubscriptionRepository.java
│   │   │   │   ├── SaleRepository.java
│   │   │   │   ├── ScheduleRepository.java
│   │   │   │   ├── ServiceRepository.java
│   │   │   │   ├── DiscountRepository.java
│   │   │   │   ├── DiseaseRepository.java
│   │   │   │   ├── DiseaseTypeRepository.java
│   │   │   │   ├── PreferenceRepository.java
│   │   │   │   ├── PreferenceTypeRepository.java
│   │   │   │   ├── PriceHistoryRepository.java
│   │   │   │   ├── RecommendationRepository.java
│   │   │   │   ├── RegistrationOfVisitRepository.java
│   │   │   │   ├── FitnessCenterRepository.java
│   │   │   │   └── TrainerRepository.java
│   │   │   │
│   │   │   ├── entity/                            # 18 JPA Entities (Lombok @Data)
│   │   │   │   ├── User.java                      # Роли: CLIENT, TRAINER, MANAGER, ADMIN
│   │   │   │   ├── Client.java
│   │   │   │   ├── Trainer.java
│   │   │   │   ├── FitnessCenter.java
│   │   │   │   ├── Subscription.java              # Типы абонементов
│   │   │   │   ├── Sale.java                      # Проданные абонементы (клиент + подписка + скидка)
│   │   │   │   ├── Schedule.java                  # Расписание занятий (тренер + услуга + время)
│   │   │   │   ├── FitnessService.java            # Услуги фитнес-центра
│   │   │   │   ├── Discount.java
│   │   │   │   ├── Disease.java / DiseaseType.java
│   │   │   │   ├── Preference.java / PreferenceType.java
│   │   │   │   ├── PriceHistory.java              # История цен услуг
│   │   │   │   ├── Recommendation.java            # Рекомендации тренеров клиентам
│   │   │   │   └── RegistrationOfVisit.java       # Учет посещений
│   │   │   │
│   │   │   ├── dto/                               # DTO для отчетов и сложных запросов
│   │   │   │   ├── VisitResponse.java
│   │   │   │   ├── ClientFilterRequest.java
│   │   │   │   ├── ScheduleRequest.java
│   │   │   │   ├── ReportConfigDto.java
│   │   │   │   ├── SaleFilterRequest.java
│   │   │   │   ├── ClientProfileResponse.java
│   │   │   │   └── SubscriptionResponse.java
│   │   │   │
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java    # @ControllerAdvice
│   │   │   │   ├── NotFoundException.java
│   │   │   │   ├── InvalidRequestException.java
│   │   │   │   └── SubscriptionExpiredException.java
│   │   │   │
│   │   │   └── ui/                                # JavaFX Controllers (FXML + CSS)
│   │   │       ├── LoginController.java
│   │   │       ├── ClientDashboardController.java
│   │   │       ├── TrainerMainViewController.java
│   │   │       ├── ManagerViewController.java
│   │   │       ├── AdminController.java
│   │   │       └── ... (20+ FXML контроллеров)
│   │   │
│   │   └── resources/
│   │       ├── application.properties             # Шаблон (НЕ КОММИТИТЬ СЕКРЕТЫ!)
│   │       ├── application-example.properties     # Пример конфигурации
│   │       ├── ui/                                # FXML views + style.css
│   │       ├── fonts/                             # Шрифты для JavaFX
│   │       └── images/                            # Иконки/картинки
│   │
│   └── test/                                      # Unit/Integration тесты
│
├── .gitignore
└── README.md
```

### 🔗 Ключевые связи БД (ER-диаграмма в тексте)

```mermaid
erDiagram
    USER ||--o| CLIENT : "1:0..1 (CLIENT role)"
    USER ||--o| TRAINER : "1:0..1 (TRAINER role)"
    CLIENT }|--o{ SALE : "покупает"
    SUBSCRIPTION }|--o{ SALE : "тип абонемента"
    DISCOUNT }|--o{ SALE : "применяется"
    TRAINER }|--o{ SCHEDULE : "ведет занятия"
    FITNESS_SERVICE }|--o{ SCHEDULE : "услуга"
    FITNESS_CENTER }|--o{ SCHEDULE : "место"
    CLIENT }|--o{ REGISTRATION_OF_VISIT : "посещает"
    SCHEDULE }|--o{ REGISTRATION_OF_VISIT : "запись на занятие"
    CLIENT }|--o{ PREFERENCE : "предпочтения"
    CLIENT }|--o{ DISEASE : "заболевания"
    FITNESS_SERVICE }|--o{ PRICE_HISTORY : "история цен"
    TRAINER }|--o{ RECOMMENDATION : "дает рекомендации"
    CLIENT }|--o{ RECOMMENDATION : "получает"
```

---

## 💻 Локальное развертывание

### 🔧 Предварительные требования
- **JDK 21** (LTS) — рекомендуется [Eclipse Temurin](https://adoptium.net/temurin/releases/) или Oracle JDK
- **Maven 3.9+**
- **Microsoft SQL Server 2019+** (или Docker-образ `mcr.microsoft.com/mssql/server:2022-latest`)

### 1. Клонирование репозитория
```bash
git clone https://github.com/<your-org>/fitness-center-backend.git
cd fitness-center-backend
```

### 2. Настройка конфигурации
Скопируйте пример конфигурации и заполните своими значениями:
```bash
cp src/main/resources/application-example.properties src/main/resources/application-local.properties
```

Отредактируйте `application-local.properties`:
```properties
# Database
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=FitnessCenter;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=YOUR_STRONG_PASSWORD

# JWT Secret (generate: openssl rand -base64 64)
jwt.secret=YOUR_512_BIT_BASE64_SECRET
jwt.expiration=86400000
```

> **Никогда не коммитьте реальные секреты в `application.properties`!** Файл `application-local.properties` добавлен в `.gitignore`.

### 3. База данных
Схема создается **вручную** (Hibernate `ddl-auto=validate`). Выполните DDL-скрипты перед запуском:
```sql
-- Пример создания БД
CREATE DATABASE FitnessCenter;
GO
-- Далее — CREATE TABLE для всех 18 сущностей (см. entity/*.java с @Table)
```
> ⚠️ В проекте нет Flyway/Liquibase — миграции нужно накатывать вручную или добавить.

### 4. Сборка и запуск Backend (REST API)
```bash
# Сборка
mvn clean package -DskipTests

# Запуск (профиль local)
mvn spring-boot:run -Dspring-boot.run.profiles=local
# Или через JAR:
java -jar target/fitness-center-backend-1.0-SNAPSHOT.jar --spring.profiles.active=local
```

Backend поднимется на: **http://localhost:8080**

### 5. Запуск Desktop UI (JavaFX)
```bash
# Из корня проекта (требует JavaFX модули в classpath)
mvn javafx:run
# Или запустите класс JavaFxApplication из IDE
```

### 6. Docker (Рекомендуемый способ — «в один клик»)

```bash
# Сгенерируйте JWT_SECRET (512 бит base64):
export JWT_SECRET=$(openssl rand -base64 64)

# Запуск (поднимет MSSQL + Backend)
docker-compose up --build -d
```

Сервисы будут доступны:
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **MSSQL**: localhost:1433 (sa / YourStrong!Passw0rd)

Остановка:
```bash
docker-compose down
```

Остановка с удалением данных БД:
```bash
docker-compose down -v
```

---

## 🔌 API Эндпоинты (Swagger UI)

> **Swagger UI**: http://localhost:8080/swagger-ui.html  
> **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### 📋 Основные группы эндпоинтов

| Метод | Путь | Описание | Доступ |
|-------|------|----------|--------|
| **POST** | `/api/auth/login` | Авторизация, возврат JWT | Public |
| **POST** | `/api/auth/register` | Регистрация нового пользователя | Public |
| **GET** | `/api/auth/me` | Профиль текущего пользователя | Authenticated |
| **GET** | `/api/client/profile` | Профиль клиента | `ROLE_CLIENT` |
| **GET** | `/api/client/subscriptions` | Мои абонементы | `ROLE_CLIENT` |
| **GET** | `/api/client/schedule` | Доступное расписание | `ROLE_CLIENT` |
| **GET** | `/api/client/visits` | История посещений | `ROLE_CLIENT` |
| **POST** | `/api/client/visits/{scheduleId}` | Запись на занятие | `ROLE_CLIENT` |
| **DELETE** | `/api/client/visits/{visitId}` | Отмена записи | `ROLE_CLIENT` |
| **GET** | `/api/trainer/clients` | Мои клиенты | `ROLE_TRAINER` |
| **GET** | `/api/trainer/schedule` | Мое расписание | `ROLE_TRAINER` |
| **POST** | `/api/trainer/schedule` | Добавить занятие | `ROLE_TRAINER` |
| **GET** | `/api/trainer/schedule/{id}/registrations` | Клиенты на занятии | `ROLE_TRAINER` |
| **GET** | `/api/manager/clients` | Клиенты (с фильтрами, пагинация) | `ROLE_MANAGER` |
| **GET** | `/api/manager/sales` | Продажи (с фильтрами, пагинация) | `ROLE_MANAGER` |
| **POST** | `/api/manager/sales` | Создать продажу | `ROLE_MANAGER` |
| **GET** | `/api/admin/users` | Все пользователи | `ROLE_ADMIN` |
| **POST** | `/api/admin/clients` | Создать клиента | `ROLE_ADMIN` |
| **GET** | `/api/admin/schedule` | Все расписание | `ROLE_ADMIN` |
| **POST** | `/api/reports/generate` | Генерация отчета (Excel) | `ROLE_MANAGER`, `ROLE_ADMIN` |

### 📦 Примеры запросов

**Логин:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"ivan","password":"secret123"}'
# Response: "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpdmFuIiwiaWF0IjoxNz..."
```

**Авторизованный запрос:**
```bash
curl -X GET http://localhost:8080/api/client/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

**Получение расписания (клиент):**
```bash
curl -X GET http://localhost:8080/api/client/schedule \
  -H "Authorization: Bearer <token>"
```

---

## 🛡 Безопасность и лучшие практики

1. **Секреты** — только в переменных окружения / Vault / Spring Cloud Config.
2. **JWT Secret** — минимум 512 бит (64 байта), генерируйте при деплое: `openssl rand -base64 64`.
3. **Пароли БД** — сильные, ротация по политике компании.
4. **HTTPS** — в продакшене только за reverse proxy (Nginx/Traefik) с TLS.
5. **CORS** — настройте `allowed-origins` под ваш фронтенд.
6. **Rate Limiting** — добавьте `Bucket4j` или `Resilience4j` для `/api/auth/**`.

---

## 🧪 Тестирование

```bash
# Unit тесты
mvn test

# Integration тесты (требуют Testcontainers + MSSQL)
mvn verify -Pintegration-tests
```

> В проекте **нет тестов** — приоритет: добавить JUnit 5 + Mockito (unit) + Testcontainers (integration).

---

## 📦 CI/CD (Рекомендация)

Добавьте `.github/workflows/ci.yml`:
```yaml
name: CI
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: 'temurin', java-version: '21' }
      - name: Build & Test
        run: mvn -B verify
      - name: Docker Build
        run: docker build -t fitness-center-backend .
```

---

## 📄 Лицензия

Проект распространяется под лицензией **MIT**.

---

## 👥 Авторы

- **Рогачев Артем Юрьевич** — архитектура, backend, JavaFX UI
- Контакты: [GitHub](https://github.com/tweetydmg) / [Email](mailto:artemrogachev3000@gmail.com)

---