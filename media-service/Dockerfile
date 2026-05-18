FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY . .
# Cấp quyền thực thi cho gradlew
RUN chmod +x ./gradlew
# Chạy lệnh build tạo bootJar từ module bootstrap
RUN ./gradlew :media-bootstrap:bootJar -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copy file JAR từ stage builder sang (dùng wildcard phòng trường hợp tên phiên bản thay đổi)
COPY --from=builder /app/media-bootstrap/build/libs/media-bootstrap-*.jar app.jar

# Tạo thư mục storage cho local
RUN mkdir -p /app/storage

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
