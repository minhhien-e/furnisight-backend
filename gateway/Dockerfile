# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Gradle wrapper and settings
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Copy source code
COPY src src

# Make the wrapper executable and build the application
RUN --mount=type=cache,target=/root/.gradle \
    chmod +x gradlew && ./gradlew build -x test

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/build/libs/gateway-0.0.1-SNAPSHOT.jar app.jar

# Expose port based on application.yaml
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
