# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

ARG GITHUB_ACTOR
ENV GITHUB_ACTOR=${GITHUB_ACTOR}

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

RUN --mount=type=secret,id=GITHUB_TOKEN \
    --mount=type=cache,target=/root/.gradle \
    export GITHUB_TOKEN=$(cat /run/secrets/GITHUB_TOKEN) && \
    chmod +x gradlew && ./gradlew build -x test

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder --chown=spring:spring /app/build/libs/*.jar app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
