# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Accept build arguments for GitHub Packages authentication
ARG GITHUB_ACTOR

# Set environment variables for Gradle to use
ENV GITHUB_ACTOR=${GITHUB_ACTOR}

# Copy Gradle wrapper and settings
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Copy all source code modules
COPY catalog-application catalog-application
COPY catalog-bootstrap catalog-bootstrap
COPY catalog-domain catalog-domain
COPY catalog-infrastructure catalog-infrastructure
COPY catalog-presentation catalog-presentation

# Make the wrapper executable and build the application using securely mounted secret
RUN --mount=type=secret,id=GITHUB_TOKEN \
    --mount=type=cache,target=/root/.gradle \
    export GITHUB_TOKEN=$(cat /run/secrets/GITHUB_TOKEN) && \
    chmod +x gradlew && ./gradlew build -x test

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built jar from the builder stage
# Assuming the jar name follows the pattern: module-name-version.jar
COPY --from=builder /app/catalog-bootstrap/build/libs/catalog-bootstrap-1.0-SNAPSHOT.jar app.jar

# Expose port based on application.yml (assuming server.port=8083)
EXPOSE 8083

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
