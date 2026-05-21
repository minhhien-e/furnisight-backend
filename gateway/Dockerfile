# =========================================================================
# Stage 1: Build (Biên dịch ứng dụng sử dụng Multi-stage Build)
# =========================================================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Sao chép các tệp cấu hình Gradle wrapper
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Sao chép toàn bộ mã nguồn của Gateway
COPY src src

# Tiến hành cấp quyền thực thi và build JAR, sử dụng Cache Mount để tối ưu tốc độ build
RUN --mount=type=cache,target=/root/.gradle \
    chmod +x gradlew && ./gradlew build -x test

# =========================================================================
# Stage 2: Run (Môi trường chạy thực tế siêu nhẹ và bảo mật)
# =========================================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Tạo nhóm hệ thống và tài khoản không đặc quyền để cô lập ứng dụng (Security Compliance)
RUN addgroup -S spring && adduser -S spring -G spring

# Sao chép tệp JAR đã biên dịch và phân quyền sở hữu cho tài khoản spring
COPY --from=builder --chown=spring:spring /app/build/libs/gateway-0.0.1-SNAPSHOT.jar app.jar

# Chuyển quyền thực thi sang tài khoản không đặc quyền
USER spring:spring

# Expose cổng kết nối tiêu chuẩn 8080
EXPOSE 8080

# Chạy ứng dụng Spring Cloud Gateway
ENTRYPOINT ["java", "-jar", "app.jar"]
