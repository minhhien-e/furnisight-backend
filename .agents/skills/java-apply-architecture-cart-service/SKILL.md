---
name: java-apply-architecture-cart-service
description: Structure or refactor Spring Boot features for cart-service using Hexagonal Architecture (Domain, Application, Adapter, Bootstrap). Use when creating a new backend endpoint, integrating a new service, or organizing business logic in cart-service.
---

# Apply Spring Boot Hexagonal Architecture for cart-service

Follow the repository's existing Hexagonal Architecture for cart-service.

## Required Flow

```text
HTTP/gRPC/Kafka Request -> Adapter (Controller/Listener) -> DTO Validation -> Application (Use Case/Service) -> Domain (Entities) -> Adapter (Repository) -> Database
```

Organize every feature by layer in `cart-service`:

```text
cart-service/
  cart-service-domain/
    entities/
    exceptions/
  cart-service-application/
    dto/
    ports/ (inbound, outbound)
    service/
  cart-service-adapter/
    in/ (web, grpc, messaging)
    out/ (persistence, integration, messaging)
  cart-service-bootstrap/
    config/
    CartServiceApplication.java
```

## Implementation Order

1. Define the core business logic and state in `cart-service-domain`. This module must have NO external dependencies.
2. Define Use Cases (interfaces) and Input/Output Ports in `cart-service-application/ports`.
3. Implement the Use Cases in `cart-service-application/service`.
4. Create inbound adapters in `cart-service-adapter/in` (e.g., REST Controllers, Kafka Listeners) that implement inbound ports or use the Application services.
5. Create outbound adapters in `cart-service-adapter/out` (e.g., JPA Repositories, REST Clients) that implement outbound ports from the Application layer.
6. Configure beans and application entry points in `cart-service-bootstrap`.

## Rules

- **Thin Controllers**: Do not write loops, conditionals, or database queries in the Controller.
- **Dependency Inversion**: Application layer should define the interface (Port) for external interactions, and the Adapter layer should implement it.
- **No Circular Dependencies**: Dependencies strictly flow inwards: Bootstrap -> Adapter -> Application -> Domain.
