FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

ARG GITHUB_ACTOR
ENV GITHUB_ACTOR=${GITHUB_ACTOR}

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY promotion-adapter promotion-adapter
COPY promotion-application promotion-application
COPY promotion-bootstrap promotion-bootstrap
COPY promotion-domain promotion-domain

RUN --mount=type=secret,id=GITHUB_TOKEN \
    --mount=type=cache,id=gradle-cache-promotion-service,target=/root/.gradle \
    export GITHUB_TOKEN=$(cat /run/secrets/GITHUB_TOKEN) && \
    chmod +x gradlew && ./gradlew --no-daemon build -x test

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=builder --chown=spring:spring /app/promotion-bootstrap/build/libs/promotion-bootstrap-1.0-SNAPSHOT.jar app.jar

USER spring:spring

EXPOSE 8080
EXPOSE 9096

ENTRYPOINT ["java", "-jar", "app.jar"]
