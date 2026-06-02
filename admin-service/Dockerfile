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

# Tiến hành cấp quyền thực thi và build JAR, sử dụng Cache Mount và Secret Mount để tối ưu bảo mật & tốc độ
RUN --mount=type=secret,id=GITHUB_TOKEN \
    --mount=type=cache,target=/home/gradle/.gradle \
    export GITHUB_TOKEN=$(cat /run/secrets/GITHUB_TOKEN) && \
    gradle build -x test

# =========================================================================
# Stage 2: Run (Môi trường chạy thực tế siêu nhẹ và bảo mật)
# =========================================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Tạo nhóm hệ thống và tài khoản không đặc quyền để cô lập ứng dụng (Security Compliance)
RUN addgroup -S spring && adduser -S spring -G spring

# Sao chép tệp JAR đã biên dịch và phân quyền sở hữu cho tài khoản spring
# Lưu ý: Tìm file jar sinh ra trừ file plain
COPY --from=builder --chown=spring:spring /app/build/libs/admin-service-0.0.1-SNAPSHOT.jar app.jar

# Chuyển quyền thực thi sang tài khoản không đặc quyền
USER spring:spring

# Expose cổng kết nối tiêu chuẩn nội bộ 8080
EXPOSE 8080

# Chạy ứng dụng Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
