---
name: oma-backend-gateway
description: Implement and verify backend work for gateway using Spring Boot, PostgreSQL, and Kafka. Backend work must follow the existing architecture and use real database connections without mock data.
---

# Backend Agent for gateway

Build backend features using the architecture already present in `gateway`.

## Required Context

1. Always inspect `gateway` modules (domain, application, adapter, bootstrap) to understand the current implementation.
2. The project uses Java Spring Boot, JPA/Hibernate, MapStruct, and Flyway.
3. Review Flyway migrations in `gateway-bootstrap/src/main/resources/db/migration` before planning database changes.

## Backend Rules

1. **Architecture**: Follow the Hexagonal architecture defined in `java-apply-architecture-gateway`.
2. **Business Logic**: Keep controllers thin. Put all business logic in the Application Service layer.
3. **Data Access**: Use Spring Data JPA Repositories in the Adapter Out layer.
4. **Validation**: Use `jakarta.validation.constraints` (e.g., `@NotBlank`, `@NotNull`) on DTOs.
5. **No Mock Data**: Do not create mock objects, arrays, or hardcoded dummy data inside the services. All queries must hit the real database.
6. **Error Handling**: Use `@RestControllerAdvice` in the Adapter layer to map Domain Exceptions to appropriate HTTP status codes (e.g., 404 for NotFound, 400 for BadRequest).
7. **Mapping**: Use `MapStruct` to map between Domain Entities, DTOs, and JPA Entities.
8. **Testing**: Write unit tests (`*Test.java`) for services and controllers.

## Verification

Run:
```bash
./gradlew :gateway:build
```
Do not report completion while Java compilation errors or test failures remain.
