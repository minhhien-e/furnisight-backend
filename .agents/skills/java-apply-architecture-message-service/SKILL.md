---
name: java-apply-architecture-message-service
description: Structure or refactor Spring Boot features for message-service using Hexagonal Architecture (Domain, Application, Adapter, Bootstrap). Use when creating a new backend endpoint, integrating a new service, or organizing business logic in message-service.
---

# Apply Spring Boot Hexagonal Architecture for message-service

Follow the repository's existing Hexagonal Architecture for message-service.

## Required Flow

```text
HTTP/gRPC/Kafka Request -> Adapter (Controller/Listener) -> DTO Validation -> Application (Use Case/Service) -> Domain (Entities) -> Adapter (Repository) -> Database
```

Organize every feature by layer in `message-service`:

```text
message-service/
  message-service-domain/
    entities/
    exceptions/
  message-service-application/
    dto/
    ports/ (inbound, outbound)
    service/
  message-service-adapter/
    in/ (web, grpc, messaging)
    out/ (persistence, integration, messaging)
  message-service-bootstrap/
    config/
    MessageServiceApplication.java
```

## Implementation Order

1. Define the core business logic and state in `message-service-domain`. This module must have NO external dependencies.
2. Define Use Cases (interfaces) and Input/Output Ports in `message-service-application/ports`.
3. Implement the Use Cases in `message-service-application/service`.
4. Create inbound adapters in `message-service-adapter/in` (e.g., REST Controllers, Kafka Listeners) that implement inbound ports or use the Application services.
5. Create outbound adapters in `message-service-adapter/out` (e.g., JPA Repositories, REST Clients) that implement outbound ports from the Application layer.
6. Configure beans and application entry points in `message-service-bootstrap`.

## Rules

- **Thin Controllers**: Do not write loops, conditionals, or database queries in the Controller.
- **Dependency Inversion**: Application layer should define the interface (Port) for external interactions, and the Adapter layer should implement it.
- **No Circular Dependencies**: Dependencies strictly flow inwards: Bootstrap -> Adapter -> Application -> Domain.
