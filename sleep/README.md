# Sleep Logger API

Backend service for tracking sleep sessions and computing rolling statistics.

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│  Web Layer          REST controllers, DTOs, validation  │
├─────────────────────────────────────────────────────────┤
│  Application        Use cases, orchestration            │
├─────────────────────────────────────────────────────────┤
│  Domain              Entities, business rules, ports    │
├─────────────────────────────────────────────────────────┤
│  Infrastructure     Persistence, DB config, adapters    │
└─────────────────────────────────────────────────────────┘
```

**Dependency direction:** Web → Application → Domain ← Infrastructure

Business logic lives in the **domain layer** (framework-free, pure Kotlin). Infrastructure implements repository
**ports** defined by the domain.

## API

### User Management

**POST** `/v1/users`

- Body: `{"username": "Italo"}`
- Validation: Username must be non-blank, 2-100 characters
- Response: 201 - `{"id": "uuid"}`
- Errors:
    - 400: Invalid/missing username
    - 409: Username already taken

### Sleep Logs

**POST** `/v1/users/{userId}/sleep-logs`

- Path: `userId` (UUID)
- Body: `sleepDate` (date), `bedTime` (time), `wakeTime` (time), `mood` (BAD|OK|GOOD)
- Uniqueness: only one sleep log per user per `sleepDate`
- Response: 201 - `{"id": "uuid", "userId": "uuid", "sleepDate": "2024-01-15", "bedTime": "23:00:00", "wakeTime": "07:00:00", "mood": "GOOD", "durationInSeconds": 28800}`
- Errors:
    - 404: User not found
    - 400: Invalid sleep data (e.g., bedTime == wakeTime)
    - 409: Duplicate sleep log for the same `sleepDate`

**GET** `/v1/users/{userId}/sleep-logs/last-night`

- Path: `userId` (UUID)
- Response: 200 - `{"id": "uuid", "userId": "uuid", "sleepDate": "2024-01-15", "bedTime": "23:00:00", "wakeTime": "07:00:00", "mood": "GOOD", "durationInSeconds": 28800}`
- Errors:
    - 404: User not found or no sleep log exists

**GET** `/v1/users/{userId}/sleep-logs/stats`

- Path: `userId` (UUID)
- Query: `days` (integer, optional, default=30)
- Response: 200 - `rangeStart`, `rangeEnd`, `averageDurationInSeconds`, `averageBedTime`, `averageWakeTime`,
  `moodFrequencies`

## Setup

### Prerequisites

- Docker & Docker Compose
- JDK 17+
- Ports 5432 (Postgres) and 8080 (API) available

### Quick Start

```bash
# From repository root
docker-compose up --build
```

### Development Setup

```bash
# Start database
docker-compose up -d db

# Run unit tests (default)
./gradlew test

# Run integration tests (opt-in). Integration tests live under
# src/integrationTest/kotlin and should be annotated with @Tag("integration").
./gradlew integrationTest

# Run both (include integrations in 'check')
./gradlew check -PincludeIntegrationTests

# Run application
./gradlew bootRun

# Build JAR
./gradlew build
```

### Test Coverage

Minimum 80% coverage enforced for non-infrastructure code.

```bash
./gradlew check
```

## Tech Stack

- Kotlin 1.6 + Spring Boot 2.7
- PostgreSQL 13 + Flyway migrations
- JUnit 5 + MockK
