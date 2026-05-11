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
COPY user-application user-application
COPY user-bootstrap user-bootstrap
COPY user-domain user-domain
COPY user-infrastructure user-infrastructure
COPY user-presentation user-presentation

# Make the wrapper executable and build the application using securely mounted secret
RUN --mount=type=secret,id=GITHUB_TOKEN \
    export GITHUB_TOKEN=$(cat /run/secrets/GITHUB_TOKEN) && \
    chmod +x gradlew && ./gradlew build -x test

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/user-bootstrap/build/libs/user-bootstrap-1.0-SNAPSHOT.jar app.jar

# Expose port based on application.yml (server.port=8081)
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
