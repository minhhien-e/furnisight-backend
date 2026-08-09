# =========================================================================
# Stage 1: Build (Biên dịch ứng dụng sử dụng Multi-stage Build)
# =========================================================================
FROM gradle:8-jdk17-alpine AS builder

WORKDIR /app

# Khai báo build-args để xác thực với GitHub Packages
ARG GITHUB_ACTOR
ENV GITHUB_ACTOR=${GITHUB_ACTOR}

# Sao chép cấu hình
COPY build.gradle settings.gradle ./

# Sao chép mã nguồn
COPY src src

# Build JAR sử dụng Cache Mount và Secret Mount
RUN --mount=type=secret,id=GITHUB_TOKEN \
    --mount=type=cache,id=gradle-cache-admin-service,target=/home/gradle/.gradle \
    export GITHUB_TOKEN=$(cat /run/secrets/GITHUB_TOKEN) && \
    gradle --no-daemon build -x test

# =========================================================================
# Stage 2: Run
# =========================================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder --chown=spring:spring /app/build/libs/admin-service-0.0.1-SNAPSHOT.jar app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
