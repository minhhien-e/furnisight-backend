# Hướng dẫn chạy hệ thống

Tài liệu này chứa các lệnh để chạy hệ thống backend với Docker Compose và Kubernetes (k8s).

## Docker Compose

### 1. Build các images
```bash
docker compose build
```

### 2. Khởi chạy hệ thống (Up)
```bash
docker compose up -d
```

### 3. Xem logs
```bash
# Xem logs của toàn bộ các services
docker compose logs -f

# Xem logs của 1 service cụ thể (ví dụ: api-gateway, user-service, v.v.)
docker compose logs -f <tên_service>
```

### 4. Dừng và xóa containers
```bash
docker compose down
```

---

## Kubernetes (k8s)

### 1. Áp dụng (Apply) các cấu hình
```bash
# Apply toàn bộ các file yaml trong thư mục k8s
kubectl apply -f k8s/
```

### 2. Xem trạng thái các tài nguyên
```bash
# Xem danh sách pods
kubectl get pods

# Xem danh sách services
kubectl get svc

# Xem danh sách deployments
kubectl get deployments
```

### 3. Xem logs của Pod
```bash
# Lấy tên pod từ lệnh get pods, sau đó xem logs:
kubectl logs -f <tên_pod>
```

### 4. Gỡ bỏ (Delete) các tài nguyên
```bash
kubectl delete -f k8s/
```
