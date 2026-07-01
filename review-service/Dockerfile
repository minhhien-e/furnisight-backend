FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

ARG GITHUB_ACTOR
ENV GITHUB_ACTOR=${GITHUB_ACTOR}

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY review-domain review-domain
COPY review-application review-application
COPY review-adapter review-adapter
COPY review-bootstrap review-bootstrap

RUN --mount=type=secret,id=GITHUB_TOKEN \
    --mount=type=cache,id=gradle-cache-review-service,target=/root/.gradle \
    export GITHUB_TOKEN=$(cat /run/secrets/GITHUB_TOKEN) && \
    sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew --no-daemon build -x test

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder --chown=spring:spring /app/review-bootstrap/build/libs/review-bootstrap-1.0-SNAPSHOT.jar app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
