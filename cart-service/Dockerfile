# =========================================================================
# Stage 1: Build
# =========================================================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

ARG GITHUB_ACTOR
ENV GITHUB_ACTOR=${GITHUB_ACTOR}

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

RUN --mount=type=secret,id=GITHUB_TOKEN \
    --mount=type=cache,id=gradle-cache-cart-service,target=/root/.gradle \
    export GITHUB_TOKEN=$(cat /run/secrets/GITHUB_TOKEN) && \
    chmod +x gradlew && ./gradlew --no-daemon build -x test

# =========================================================================
# Stage 2: Run
# =========================================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder --chown=spring:spring /app/build/libs/cart-service-0.0.1-SNAPSHOT.jar app.jar

USER spring:spring

EXPOSE 8080
EXPOSE 9094

ENTRYPOINT ["java", "-jar", "app.jar"]
