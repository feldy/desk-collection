# Desk Collection Management

A Spring Boot application to manage a collection of desks. built with:
- **Spring Boot 3** (Java 17)
- **PostgreSQL** (Database)
- **Flyway** (Migrations)
- **Thymeleaf** (Frontend)
- **Alpine.js + Tailwind CSS** (UI Interactivity & Styling)
- **Docker Compose** (Local Dev Environment)

## Prerequisites

- Java 17+
- Docker & Docker Compose

## Getting Started

### 1. Start Support Services (Database)
Run the PostgreSQL database using Docker Compose:

```bash
docker-compose up -d
```

### 2. Run the Application
You can run the application using the Maven wrapper:

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`.

### 3. Features
- **List Desks**: View all desks in the collection.
- **Add Desk**: Form to add new desks.
- **Edit/Delete**: Management actions.
- **Search**: (To be implemented)

## Project Structure
- `src/main/resources/db/migration`: Database SQL migrations.
- `src/main/resources/templates`: HTML views.
- `src/main/java`: Backend logic.

## Credentials
- **DB User**: postgres
- **DB Password**: password
- **DB Name**: desk_collection
