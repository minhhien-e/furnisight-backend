# Identity Service

This service manages user identities, authentications, and related domains. It's built using a modern 5-module Clean Architecture pattern to separate concerns clearly and ensure maintaiability.

## Architecture Structure

The project is structured into 5 distinct modules:

1. **`identity-domain`**: The innermost layer. Contains enterprise logic, domain models (entities, value objects), and repository interfaces. Does not depend on any outer layers or frameworks.
2. **`identity-application`**: The application layer. Contains use cases, services, commands, and queries. Coordinates the domain objects to execute business rules. Depends solely on the `identity-domain`.
3. **`identity-infrastructure`**: The implementation layer. Contains adapters for databases (JPA, Mongo, etc.), external APIs, messaging systems, etc. Depends on `identity-application` and `identity-domain`.
4. **`identity-presentation`**: The outer interface layer. Contains REST Controllers, gRPC endpoints, and handles all incoming requests mapping them to application services. Depends on `identity-application` and `identity-domain`.
5. **`identity-bootstrap`**: The application launcher. The module that contains the `SpringBootApplication` class, dependency injection configuration, properties, and startup tasks. It wires all other modules together to run the application.
