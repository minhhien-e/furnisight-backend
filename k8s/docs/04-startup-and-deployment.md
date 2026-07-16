# Chương 4: Hướng Dẫn Khởi Chạy (Startup & Deployment)

Việc khởi chạy một hệ thống Microservices trên K8s không thể thực hiện ngẫu nhiên. Nếu bạn bật `user-service` trước khi bật Database, ứng dụng sẽ gặp lỗi kết nối (CrashLoopBackOff). Bạn bắt buộc phải tuân theo thứ tự sau.

## Bước 1: Build Docker Images (Tùy chọn)

Nếu bạn chạy K3s trên chính máy tính/Server mà bạn đang dev, bạn có thể build image trực tiếp vào hệ thống của Containerd (trình chạy container mặc định của K3s) mà không cần phải đẩy lên DockerHub.

## Bước 2: Khởi tạo Namespace

Mọi tài nguyên sẽ được cô lập trong các Namespace để không tranh chấp với nhau.
Mở Terminal, trỏ vào thư mục `k8s/` và chạy:

```bash
kubectl apply -f config/namespaces.yaml
```
*(Nếu bạn không có file namespace, hãy tạo bằng lệnh: `kubectl create namespace furnisight-apps` và `kubectl create namespace furnisight-infras`)*

## Bước 3: Nạp Cấu Hình (Config & Secrets)

Chạy tất cả các cấu hình môi trường và kịch bản khởi tạo DB.

```bash
kubectl apply -f config/
```
Lệnh này sẽ nạp:
- `secret-env.yaml` (Mật khẩu, Key)
- `shared-env.yaml` (Cấu hình dùng chung toàn dự án)
- Các `*-env.yaml` lẻ khác.
- Kèm theo việc tạo ConfigMap cho các file script `.sql` và `.js` (Nên chạy file bash `setup-configmaps.sh` nếu dự án có cấu hình riêng cho việc này).

## Bước 4: Khởi Chạy Hạ Tầng (Infrastructure)

Bao gồm Postgres, MongoDB, Kafka, Redis, Zookeeper. Phải chờ cho nhóm này báo `Running` thì mới được bật các Services.

```bash
kubectl apply -f infrastructure/
```
Kiểm tra bằng lệnh: `kubectl get pods -n furnisight-infras -w` (Bấm Ctrl+C để thoát chế độ watch).

## Bước 5: Khởi Chạy Microservices

Sau khi Database sẵn sàng, tiến hành bật các khối ứng dụng (Java & Python).

```bash
kubectl apply -f services/
```
K8s sẽ đọc các Deployment YAML và bắt đầu kéo Image, gắn ConfigMap/Secret vào và khởi chạy Container.
K8s tự động kiểm tra `readinessProbe` (như health check `/actuator/health`). Chỉ khi Service báo xanh, nó mới cho phép traffic đi vào.

## Bước 6: Mở Cửa Ra Internet (Ingress)

Cuối cùng, kích hoạt Ingress để phân luồng API.

```bash
kubectl apply -f ingress/
```

> [!TIP]
> **Tự động phục hồi:** Bạn không cần bận tâm về thứ tự tắt/bật lại của một Pod đơn lẻ. Nếu RabbitMQ bị sập, `cart-service` sẽ báo lỗi và bị K8s khởi động lại. Khi K8s khởi động lại xong mà RabbitMQ đã sống lại, toàn bộ hệ thống sẽ tự động khôi phục hoàn hảo.
