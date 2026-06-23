---
name: java-communication-standards
description: Enforces strict communication rules. Inter-service uses gRPC, frontend to backend uses REST, and admin portal uses REST to admin-service which then uses gRPC. No mock data or fallbacks allowed. Use when implementing communication between services or exposing endpoints.
---

# Backend Communication Standards

## Overview
This skill enforces the communication architecture and data integrity patterns for all microservices in the system.

## Communication Rules

### 1. Inter-Service Communication MUST Use gRPC
- When one internal microservice needs to request data or trigger an action in another microservice (e.g., `order-service` calling `user-service`), it **MUST** use **gRPC**.
- RESTful HTTP calls (via `RestClient`, `RestTemplate`, or `FeignClient`) between internal microservices are strictly prohibited. REST is only allowed for external integrations.

### 2. Frontend to Backend Communication MUST Use REST
- External clients (Mobile App, Web App) must communicate with the backend services using **RESTful APIs**.
- Backend services must expose standard Spring Web `@RestController` endpoints (e.g., `/api/v1/...`) for external consumption.

### 3. Admin Portal Communication Flow
- The Admin Frontend MUST communicate with the `admin-service` using RESTful APIs.
- The `admin-service` acts as a Backend-For-Frontend (BFF). It **MUST** translate these REST requests into gRPC calls to communicate with the corresponding internal services.
- **Flow**: Admin FE -> (REST) -> `admin-service` -> (gRPC) -> `<target>-service`.

## Data Integrity Rules

### 4. No Mocks, No Fallbacks
- **No Mock Data**: Do not create or return hardcoded/mock data in the application logic. All data must be fetched directly from the database or the source microservice.
- **No Silent Fallbacks**: If an inter-service gRPC call fails, you must propagate the error or handle it explicitly. **DO NOT** return empty lists, default dummy objects, or fallback mock data. 
- The system must fail loudly when a dependency is unreachable to prevent hiding systemic errors or data inconsistencies.
