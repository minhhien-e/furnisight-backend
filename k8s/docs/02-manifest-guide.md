# Chương 2: Hướng dẫn Viết File Cấu Hình (Manifests)

Trong Kubernetes, bạn không thao tác bằng lệnh thủ công mà áp dụng phương pháp **Declarative (Khai báo)** thông qua các file YAML (gọi là Manifests). Bạn viết ra "trạng thái mong muốn", K8s sẽ tự động làm cho hệ thống khớp với trạng thái đó.

## 1. Cấu trúc cơ bản của File YAML

Một file YAML luôn bao gồm 4 phần chính:

```yaml
apiVersion: apps/v1 # 1. Phiên bản API của K8s quản lý tài nguyên này
kind: Deployment # 2. Loại tài nguyên (Deployment, Service, ConfigMap...)
metadata: # 3. Dữ liệu nhận dạng
  name: user-service # Tên của tài nguyên
  namespace: furnisight-apps # Không gian tên (Giúp cách ly các dự án)
spec: # 4. Đặc tả (Cấu hình chi tiết)
  replicas: 1
  template: ...
```

## 2. Viết Deployment (Quản lý Pod/Container)

Đây là cấu hình quan trọng nhất để khởi chạy một Microservice.

```yaml
spec:
  replicas: 2 # Số lượng container chạy song song (High Availability)
  selector:
    matchLabels:
      app: user-service # Phải khớp với nhãn (label) của Pod
  template:
    metadata:
      labels:
        app: user-service
    spec:
      containers:
        - name: user-service
          image: furnisight-user-service:latest
          ports:
            - containerPort: 8080 # Port REST API
            - containerPort: 9090 # Port gRPC
          envFrom: # Nạp cấu hình từ ConfigMap & Secret
            - configMapRef:
                name: shared-env # Nạp 9 biến môi trường chung (Kafka, Redis, gRPC Targets...)
            - configMapRef:
                name: user-service-env# Nạp biến môi trường riêng (DB)
            - secretRef:
                name: furnisight-secrets # Nạp mật khẩu, token
          resources: # Giới hạn CPU/RAM tránh treo Node
            requests:
              memory: 256Mi
              cpu: 100m
            limits:
              memory: 512Mi
              cpu: 500m
```

## 3. Quản Lý Môi Trường (ConfigMap & Secret)

Trong kiến trúc của Furnisight, chúng ta áp dụng **Shared Config Pattern** (Kế thừa cấu hình) để đảm bảo tính gọn gàng (DRY):

1. **`shared-env.yaml`**: Chứa toàn bộ cấu hình hạ tầng chung (Địa chỉ Kafka, Redis) và **Service Registry** (Toàn bộ các URL gRPC tĩnh như `dns:///catalog-service:9090`). File này được inject vào mọi Deployment.
2. **`<service>-env.yaml`**: Chứa cấu hình cụ thể chỉ dành cho 1 service đó (VD: Thông số URL kết nối DB riêng).
3. **`secret-env.yaml`**: Lưu tập trung toàn bộ mật khẩu, Token, API Key của hệ thống.

## 4. Viết Service (Cân bằng tải nội bộ)

```yaml
apiVersion: v1
kind: Service
metadata:
  name: user-service # Tên này sẽ biến thành DNS (VD: http://user-service:8080)
spec:
  type: ClusterIP # Chỉ gọi được nội bộ trong K8s
  ports:
    - name: http
      port: 8080 # Port mà Service lắng nghe
      targetPort: 8080 # Trỏ vào Port của Container
  selector:
    app: user-service # Tìm tất cả Pod có nhãn này để cân bằng tải vào
```
