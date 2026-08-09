# =========================================================================
# Stage 1: Build (Biên dịch ứng dụng sử dụng Multi-stage Build)
# =========================================================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Khai báo build-args để xác thực với GitHub Packages
ARG GITHUB_ACTOR
ENV GITHUB_ACTOR=${GITHUB_ACTOR}

# Sao chép các tệp cấu hình Gradle wrapper
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Sao chép toàn bộ mã nguồn của các phân vùng kiến trúc Clean Architecture
COPY order-adapter order-adapter
COPY order-application order-application
COPY order-bootstrap order-bootstrap
COPY order-domain order-domain

# Tiến hành cấp quyền thực thi và build JAR, sử dụng Cache Mount và Secret Mount để tối ưu bảo mật & tốc độ
RUN --mount=type=secret,id=GITHUB_TOKEN \
    --mount=type=cache,id=gradle-cache-order-service,target=/root/.gradle \
    export GITHUB_TOKEN=$(cat /run/secrets/GITHUB_TOKEN) && \
    chmod +x gradlew && ./gradlew --no-daemon build -x test

# =========================================================================
# Stage 2: Run (Môi trường chạy thực tế siêu nhẹ và bảo mật)
# =========================================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Tạo nhóm hệ thống và tài khoản không đặc quyền để cô lập ứng dụng (Security Compliance)
RUN addgroup -S spring && adduser -S spring -G spring

# Sao chép tệp JAR đã biên dịch và phân quyền sở hữu cho tài khoản spring
COPY --from=builder --chown=spring:spring /app/order-bootstrap/build/libs/order-bootstrap-1.0-SNAPSHOT.jar app.jar

# Chuyển quyền thực thi sang tài khoản không đặc quyền
USER spring:spring

# Expose cổng HTTP tiêu chuẩn 8085 và cổng gRPC tiêu chuẩn 9095
EXPOSE 8085
EXPOSE 9095

# Chạy ứng dụng Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
